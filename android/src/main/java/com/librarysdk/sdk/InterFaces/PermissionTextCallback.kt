package com.librarysdk.sdk.InterFaces

interface PermissionTextCallback {
    fun onSuccess(permissionText: String)
    fun onFailure(t: Throwable)
}