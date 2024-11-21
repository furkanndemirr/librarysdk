package com.librarysdk.sdk.InterFaces

interface PublicIpCallback {
    fun onSuccess(publicIp: String)
    fun onFailure(t: Throwable)
}