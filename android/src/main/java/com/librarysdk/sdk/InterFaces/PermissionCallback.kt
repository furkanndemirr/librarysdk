package com.librarysdk.sdk.InterFaces
import com.librarysdk.sdk.Data.Model.Permission.*

interface PermissionCallback {
    fun onSuccess(app: String)
    fun onFailure(t: Throwable)
}