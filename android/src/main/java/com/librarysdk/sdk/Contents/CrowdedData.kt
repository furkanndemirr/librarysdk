package com.librarysdk.sdk.Contents

import android.util.Log
import com.google.gson.Gson
import com.librarysdk.sdk.Constants.EndpointList
import com.librarysdk.sdk.Data.Api.ApiClient
import com.librarysdk.sdk.Data.Model.CrowdData.CrowdData
import com.librarysdk.sdk.Data.Model.Device.Device
import com.librarysdk.sdk.InterFaces.ResponseCallback
import com.librarysdk.sdk.helper.PrefHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object CrowdedData {

    fun addCrowdedData(crowdData: CrowdData, callback: ResponseCallback) {
        val addCrowdedDataUrl = PrefHelper.getString(EndpointList.ADD_CROWD_SOURCE.endpoint) ?: ""
        val gson = Gson()
        val crowdDataJson = gson.toJson(crowdData)
        Log.i("API Response", "Request successful. URL: $addCrowdedDataUrl, Data: $crowdDataJson")
        if (addCrowdedDataUrl.isNotEmpty()) {
            ApiClient.addCrowdedData(addCrowdedDataUrl, crowdData).enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful) {
                        val result = response.body() ?: false  // Eğer body null ise false döndür
                        Log.i("API Response", "Request successful. Result: $result")
                        callback.onSuccess(result)
                    } else {
                        Log.e("API Response", "Request failed with status code: ${response.code()} and message: ${response.message()}")
                        callback.onSuccess(false)
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Log.i("bura2", "sad")
                    callback.onFailure(t)
                }
            })
        }
    }
}