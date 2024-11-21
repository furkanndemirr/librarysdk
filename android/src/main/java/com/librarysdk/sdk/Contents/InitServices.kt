package com.librarysdk.sdk.Contents

import android.annotation.SuppressLint
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.librarysdk.sdk.Constants.EndpointList
import com.librarysdk.sdk.Data.Api.*
import com.librarysdk.sdk.Data.Model.Application.Application
import com.librarysdk.sdk.Data.Model.Device.Device
import com.librarysdk.sdk.Data.Model.Permission.Permission
import com.librarysdk.sdk.InterFaces.ApplicationCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.librarysdk.sdk.InterFaces.PermissionCallback
import com.librarysdk.sdk.InterFaces.PermissionTextCallback
import com.librarysdk.sdk.InterFaces.ResponseCallback
import com.librarysdk.sdk.Contents.ContextInfo.getContext
import com.librarysdk.sdk.helper.PrefHelper
import com.librarysdk.sdk.workers.UploadWorker
import java.util.concurrent.TimeUnit

object InitServices {

    @SuppressLint("SuspiciousIndentation")
    fun setOnwTimeWorkRequest(){
        val uploadRequest = PeriodicWorkRequestBuilder<UploadWorker>(15,TimeUnit.MINUTES).build()
        val workManager = WorkManager.getInstance(getContext())
        workManager.enqueueUniquePeriodicWork("MyUniqueWork",ExistingPeriodicWorkPolicy.KEEP,uploadRequest);
    }

    fun initService (appKey : String , callback: ApplicationCallback) {
        val getApplicationUrl = PrefHelper.getString(EndpointList.GET_APPLICATION.endpoint) ?: ""
        if (getApplicationUrl.isNotEmpty()) {
            ApiClient.getApplication(getApplicationUrl, appKey).enqueue(object : Callback<Application> {
                override fun onResponse(call: Call<Application>, response: Response<Application>) {
                    if (response.isSuccessful) {
                        val app = response.body()
                        if (app != null) {
                            callback.onSuccess(app)
                        } else {
                            callback.onFailure(Throwable("Application is inactive or null"))
                        }
                    } else {
                        callback.onFailure(Throwable("Response not successfuls"))
                    }
                }

                override fun onFailure(call: Call<Application>, t: Throwable) {
                    callback.onFailure(t)
                }
            })
        }
    }

    fun checkPermissionService( appId: Number, diveceId: String, type: String, callback: PermissionCallback) {
        val getPermissionStatusUrl = PrefHelper.getString(EndpointList.GET_PERMISSION_STATUS.endpoint) ?: ""
        if (getPermissionStatusUrl.isNotEmpty()) {
            ApiClient.getPermissionStatus(getPermissionStatusUrl, appId, diveceId, type)
                .enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            var permission = response.body()!!
                            callback.onSuccess(permission)
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
        }
    }

    fun deviceAddService( device: Device, callback: ResponseCallback) {
        val deviceAddUrl = PrefHelper.getString(EndpointList.ADD_DEVICE.endpoint) ?: ""
        if (deviceAddUrl.isNotEmpty()) {
            ApiClient.deviceAdd(deviceAddUrl, device).enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful) {
                        val result = response.body()!!
                        callback.onSuccess(result)
                    } else {
                        callback.onSuccess(false)
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    callback.onFailure(t)
                }
            })
        }
    }

    fun getPermissionText( type: String, callback: PermissionTextCallback) {
        val getPermissionTextUrl = PrefHelper.getString(EndpointList.GET_PERMISSION_TEXT.endpoint) ?: ""
        if (getPermissionTextUrl.isNotEmpty()) {
            ApiClient.getPermissionText(getPermissionTextUrl, type)
                .enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            val permissionText = response.body()
                            if (permissionText != null) {
                                callback.onSuccess(permissionText)
                            } else {
                                callback.onFailure(Throwable("Response not successfulf"))
                            }
                        } else {
                            callback.onFailure(Throwable("Response not successfulg"))
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
        }
    }

    fun permissionAdd( appId: Number, deviceId:String, type: String, status: Boolean, callback: ResponseCallback) {
         val permissionReuest = Permission()
        permissionReuest.appId = appId as Int;
        permissionReuest.deviceId = deviceId;
        permissionReuest.permissionType = type;
        permissionReuest.status = status;
        val permissionAddUrl = PrefHelper.getString(EndpointList.ADD_UPDATE_PERMISSION.endpoint) ?: ""
        if (permissionAddUrl.isNotEmpty()) {
            ApiClient.permissionAdd(permissionAddUrl, permissionReuest)
                .enqueue(object : Callback<Boolean> {
                    override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                        if (response.isSuccessful) {
                            val response = response.body()
                            if (response != null) {
                                callback.onSuccess(response)
                            } else {
                                callback.onSuccess(false)
                            }
                        }
                    }

                    override fun onFailure(call: Call<Boolean>, t: Throwable) {
                        callback.onFailure(t)
                    }
                })
        }
    }
}