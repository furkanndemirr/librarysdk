package com.librarysdk.sdk.Data.Model.Device

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Device {
        var appId: Int = 0;
        var sdkVersion: String = "";
        var deviceId: String = "";
        var ip: String = ""
        var port: String = ""
        var loginEmail: String = ""
        var loginMsisdn: String = ""
        var androidOwnerEmail: String = ""
        var androidOwnerMsisdn: String = ""
        var imei: String = "";
        var model: String = "";
        var brand: String = "";
        var manufacturer: String = "";
        var deviceType: String = "";
        var systemName: String = "";
        var systemVersion: String = "";
        var isRoot: Boolean = false
        var isJailbreak: Boolean = false


}