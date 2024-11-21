package com.librarysdk.sdk.InterFaces

import com.librarysdk.sdk.Data.Model.Endpoint.Endpoint

interface EndpointCallback {
    fun onSuccess(endpointList: List<Endpoint>)
    fun onFailure(t: Throwable)
}