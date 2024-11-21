package com.librarysdk.sdk.InterFaces;

interface StatusCallback {
    fun onSuccess(status: String)
    fun onFailure(t: Throwable)
}