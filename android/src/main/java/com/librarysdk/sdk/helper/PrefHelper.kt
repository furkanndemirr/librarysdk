package com.librarysdk.sdk.helper
import android.content.Context
import android.content.SharedPreferences

object PrefHelper {
    const val GUID : String = "guid"
    const val InitStatus : String = "initStatus"
    const val DeviceId : String = "deviceID"
    const val PhoneNumber : String = "phoneNumber"
    const val AppId : String = "appId"
    const val Imei : String = "imei"
    const val Token : String = "token"


    private const val PREF_NAME = "turacsdk_prefs"
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun putString(key: String, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }
}