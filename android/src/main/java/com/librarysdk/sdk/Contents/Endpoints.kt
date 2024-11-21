package com.librarysdk.sdk.Contents

import com.librarysdk.sdk.Data.Api.ApiClient
import com.librarysdk.sdk.Data.Model.Application.Application
import com.librarysdk.sdk.Data.Model.Endpoint.Endpoint
import com.librarysdk.sdk.InterFaces.ApplicationCallback
import com.librarysdk.sdk.InterFaces.EndpointCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object Endpoints {
    fun getAllEndpoints (callback: EndpointCallback) {
        ApiClient.getAllEndpoints().enqueue(object : Callback<List<Endpoint>> {
            override fun onResponse(call: Call<List<Endpoint>>, response: Response<List<Endpoint>>) {
                if (response.isSuccessful) {
                    val endPointList = response.body()
                    if (endPointList != null && endPointList.size > 0) {
                        callback.onSuccess(endPointList)
                    } else {
                        callback.onFailure(Throwable("Endpoint List empty or null"))
                    }
                } else {
                    callback.onFailure(Throwable("Response not successfulq"))
                }
            }

            override fun onFailure(call: Call<List<Endpoint>>, t: Throwable) {
                callback.onFailure(t)
            }
        })
    }
}