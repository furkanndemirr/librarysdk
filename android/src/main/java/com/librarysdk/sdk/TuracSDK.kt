package com.librarysdk.sdk

import android.content.Context
import com.librarysdk.sdk.Components.CrowdedData.addCrowdedDataComponent
import com.librarysdk.sdk.Components.InitComponents
import com.librarysdk.sdk.Components.PermissionComponents
import com.librarysdk.sdk.Contents.ContextInfo
import com.librarysdk.sdk.InterFaces.PermissionCallback
import com.librarysdk.sdk.InterFaces.PermissionTextCallback
import com.librarysdk.sdk.InterFaces.StatusCallback
import com.librarysdk.sdk.helper.PrefHelper
import com.librarysdk.sdk.Contents.DeviceInfo

object TuracSDK {
    fun addNumbers(a: Double, b: Double): Double {
        return a + b
    }
    fun init(context: Context, appKey: String, callback: StatusCallback, loginPhone: String = "", loginEmail: String = "") {
        ContextInfo.setContext(context.applicationContext)
        PrefHelper.init(context.applicationContext)
        InitComponents.initDevice(appKey, loginPhone, loginEmail, object : PermissionCallback {
            override fun onSuccess(permission: String) {
                val crowdData = DeviceInfo.getDeviceInfo("appForeground");
                addCrowdedDataComponent(crowdData)
                callback.onSuccess(permission)
            }

            override fun onFailure(t: Throwable) {
                callback.onFailure(t)
            }
        })
    }

    fun getTicariPermissionText(callback: PermissionTextCallback) {
        PermissionComponents.getTicariPermissionText( object : PermissionTextCallback {
            override fun onSuccess(permissionText: String) {
                callback.onSuccess(permissionText)
            }

            override fun onFailure(t: Throwable) {
                callback.onFailure(t)
            }
        })
    }

    fun permissionTicariAdd(permissionStatus: Boolean) {
        PermissionComponents.permissionTicariAdd(permissionStatus)
    }
}