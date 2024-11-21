package com.librarysdk.sdk.InterFaces
import com.librarysdk.sdk.Data.Model.Application.Application

interface ApplicationCallback {
    fun onSuccess(app: Application)
    fun onFailure(t: Throwable)
}