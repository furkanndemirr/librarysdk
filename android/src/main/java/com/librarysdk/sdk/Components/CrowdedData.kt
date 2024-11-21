package com.librarysdk.sdk.Components

import com.librarysdk.sdk.Constants.PermissionType
import com.librarysdk.sdk.Contents.InitServices
import com.librarysdk.sdk.Contents.InitServices.permissionAdd
import com.librarysdk.sdk.Data.Model.CrowdData.CrowdData
import com.librarysdk.sdk.InterFaces.ResponseCallback
import com.librarysdk.sdk.helper.PrefHelper
import  com.librarysdk.sdk.Contents.CrowdedData.addCrowdedData

object CrowdedData {
    fun addCrowdedDataComponent(crowdData: CrowdData) {
        addCrowdedData(crowdData, object : ResponseCallback {
            override fun onSuccess(response: Boolean) {
                if(response)
                    println("eklendi")
                //TODO log
            }

            override fun onFailure(t: Throwable) {
                //TODO log
            }
        })
    }
}