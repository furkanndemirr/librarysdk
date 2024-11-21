package com.librarysdk.sdk.Components
import com.librarysdk.sdk.Constants.PermissionType
import com.librarysdk.sdk.Contents.InitServices
import com.librarysdk.sdk.Contents.InitServices.permissionAdd
import com.librarysdk.sdk.InterFaces.PermissionTextCallback
import com.librarysdk.sdk.InterFaces.ResponseCallback
import com.librarysdk.sdk.helper.PrefHelper


object PermissionComponents {

    fun permissionTicariAdd(status: Boolean) {
        val appId : Number =PrefHelper.getString(PrefHelper.AppId).toString().toInt()
        val deviceId : String =PrefHelper.getString(PrefHelper.DeviceId).toString()
        permissionAdd(appId,deviceId, PermissionType.ticari.toString(), status, object :ResponseCallback{
            override fun onSuccess(response: Boolean) {
                if (response) {
                    InitServices.setOnwTimeWorkRequest()
                }
            }

            override fun onFailure(t: Throwable) {

            }
        })
    }

    fun getTicariPermissionText(callback: PermissionTextCallback) {
        InitServices.getPermissionText( PermissionType.ticari.toString(), object :
            PermissionTextCallback {
            override fun onSuccess(permissionText: String) {
                callback.onSuccess(permissionText)
            }

            override fun onFailure(t: Throwable) {
                callback.onFailure(t)
            }
        })
    }
}