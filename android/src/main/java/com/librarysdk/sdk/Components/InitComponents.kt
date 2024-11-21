package com.librarysdk.sdk.Components
import com.librarysdk.sdk.Constants.PermissionStatus
import com.librarysdk.sdk.Constants.PermissionType
import com.librarysdk.sdk.Contents.DeviceInfo
import com.librarysdk.sdk.Contents.InitServices
import com.librarysdk.sdk.Contents.InitServices.checkPermissionService
import com.librarysdk.sdk.Contents.InitServices.deviceAddService
import com.librarysdk.sdk.Contents.InitServices.initService
import com.librarysdk.sdk.Contents.Endpoints.getAllEndpoints
import com.librarysdk.sdk.Data.Model.Application.Application
import com.librarysdk.sdk.Data.Model.Endpoint.Endpoint
import com.librarysdk.sdk.InterFaces.ApplicationCallback
import com.librarysdk.sdk.InterFaces.EndpointCallback
import com.librarysdk.sdk.InterFaces.PermissionCallback
import com.librarysdk.sdk.InterFaces.ResponseCallback
import com.librarysdk.sdk.helper.PrefHelper
import com.librarysdk.sdk.Contents.ContextInfo.getContext
import com.librarysdk.sdk.Data.Model.Device.Device


object InitComponents {
    fun initDevice(appkey: String, loginPhone: String, loginEmail: String,callback : PermissionCallback) {
        PrefHelper.putString(PrefHelper.Token, appkey)
        getAllEndpoints(object : EndpointCallback {
            override fun onSuccess(endpointList: List<Endpoint>) {
                setAllEndpoints(endpointList)
                initService(appkey, object : ApplicationCallback {
                    override fun onSuccess(app: Application) {
                        if (app.isActive == true) {
                            PrefHelper.putString(PrefHelper.AppId,app.id.toString())
                            PrefHelper.putString(PrefHelper.GUID, app.guid)
                            PrefHelper.putString(PrefHelper.InitStatus, app.isActive.toString())
                            PrefHelper.putString(PrefHelper.PhoneNumber, "234566")
                            PrefHelper.putString(PrefHelper.Imei, "deneme")
                            val deviceId : String = DeviceInfo.getDeviceId()
                            if (PrefHelper.getString(PrefHelper.DeviceId) != deviceId) {
                                val device = Device()
                                device.loginEmail = loginEmail
                                device.loginMsisdn = loginPhone
                                deviceAddService(device, object : ResponseCallback {
                                     override fun onSuccess(result: Boolean) {
                                          if (result) {
                                              PrefHelper.putString(PrefHelper.DeviceId, deviceId)
                                              checkPermission(app.id, deviceId, callback)
                                          }
                                     }

                                    override fun onFailure(t: Throwable) {
                                        callback.onFailure(t)
                                    }

                                })

                            } else {
                                checkPermission(app.id, deviceId, callback)
                            }
                        }
                    }
                    override fun onFailure(t: Throwable) {
                        callback.onFailure(t)
                    }
                })
            }

            override fun onFailure(t: Throwable) {
                callback.onFailure(t)
            }
        })




    }

    fun checkPermission( appId: Number, deviceId:String, callback: PermissionCallback) {
        checkPermissionService(appId, deviceId, PermissionType.ticari.toString(), object :PermissionCallback{
            override fun onSuccess(permission: String) {
                if(permission != PermissionStatus.KAYIT_YOK.toString()) {
                    InitServices.setOnwTimeWorkRequest()
                }
                callback.onSuccess(permission)
            }

            override fun onFailure(t: Throwable) {
                callback.onFailure(t)
            }
        })
    }

    fun setAllEndpoints(endpointList : List<Endpoint>) {
        if (endpointList != null && endpointList.size > 0) {
            for (endpoint in endpointList) {
                PrefHelper.putString(endpoint.name, endpoint.uri)
            }
        }
    }
}