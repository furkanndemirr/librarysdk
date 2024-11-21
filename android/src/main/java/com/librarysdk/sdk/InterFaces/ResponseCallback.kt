package com.librarysdk.sdk.InterFaces

interface ResponseCallback {
    fun onSuccess(app: Boolean)
    fun onFailure(t: Throwable)
}