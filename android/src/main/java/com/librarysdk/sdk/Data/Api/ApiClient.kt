package com.librarysdk.sdk.Data.Api
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.librarysdk.sdk.Constants.Constant
import com.librarysdk.sdk.Data.Model.Device.Device
import com.librarysdk.sdk.Data.Model.Permission.Permission
import com.librarysdk.sdk.helper.PrefHelper
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.librarysdk.sdk.Data.Model.CrowdData.CrowdData

object ApiClient {

    fun create(): ApiCall {
        var gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(Constant.baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiCall::class.java)
    }
    private val api = create()

    val token = PrefHelper.getString(PrefHelper.Token) ?: ""

    fun getAllEndpoints() =
        api.getAllEndpoints(token)

    fun getApplication(url: String, appkey: String ) =
        api.getApplication(token, url, appkey)

    fun getPermissionText(url: String, permissionType: String ) =
        api.getPermissionText(token, url, permissionType)

    fun getPermissionStatus(url: String, appId: Number, deviceId: String, permissionType: String ) =
        api.getPermissionStatus(token, url, appId.toInt(), deviceId, permissionType)

    fun permissionAdd(url: String, permission: Permission ) =
        api.permissionAdd(token, url, permission)

    fun deviceAdd(url: String, device: Device) =
        api.deviceAdd(token, url, device)

    fun addCrowdedData(url: String, crowdData: CrowdData) =
        api.addCrowdedData(token, url, crowdData)
}