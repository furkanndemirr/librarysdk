package com.librarysdk

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.librarysdk.sdk.TuracSDK
import com.librarysdk.sdk.Components.InitComponents
import com.librarysdk.sdk.Components.PermissionComponents
import com.librarysdk.sdk.Contents.ContextInfo
import com.librarysdk.sdk.InterFaces.PermissionCallback
import com.librarysdk.sdk.InterFaces.PermissionTextCallback
import com.librarysdk.sdk.InterFaces.StatusCallback
import com.librarysdk.sdk.helper.PrefHelper
import com.librarysdk.sdk.Constants.PermissionStatus
import android.util.Log

class LibrarysdkModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
  private val MyToplamaClass = MyToplamaClass()
  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  fun multiply(a: Double, b: Double, promise: Promise) {
    promise.resolve(a * b)
  }
  @ReactMethod
    fun addNumbers(a: Double, b: Double, promise: Promise) {
        try {
            val result = MyToplamaClass.addNumbers(a, b)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }
@ReactMethod
    fun initializeTuracSDK(apiKey: String, promise: Promise) {
        try {
            TuracSDK.init(reactApplicationContext, apiKey, object : StatusCallback {
                override fun onSuccess(status: String) {
                    if (status == PermissionStatus.KAYIT_YOK.toString()) {
                        TuracSDK.getTicariPermissionText(object : PermissionTextCallback {
                            override fun onSuccess(permissionText: String) {
                                TuracSDK.permissionTicariAdd(true)
                                promise.resolve("Permission added successfully.")
                            }

                            override fun onFailure(t: Throwable) {
                                Log.e("TuracSDK", "Failed to get permission text: ${t.message}")
                                promise.reject("PERMISSION_TEXT_ERROR", t.message)
                            }
                        })
                    } else {
                        promise.resolve("SDK Initialized with status: $status")
                    }
                }

                override fun onFailure(t: Throwable) {
                    Log.e("TuracSDK", "Initialization failed: ${t.message}")
                    promise.reject("INIT_ERROR", t.message)
                }
            })
        } catch (e: Exception) {
            Log.e("TuracSDK", "Unexpected error: ${e.message}")
            promise.reject("UNEXPECTED_ERROR", e.message)
        }
    }
  companion object {
    const val NAME = "Librarysdk"
  }
}
