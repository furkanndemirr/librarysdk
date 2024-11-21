package com.librarysdk.sdk.Data.Model.Permission
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName;

public class Permission {

    @SerializedName("appId")
    @Expose
    var appId: Int = 0;

    @SerializedName("deviceId")
    @Expose
    var deviceId: String = "";

    @SerializedName("permissionType")
    @Expose
    var permissionType: String = "";

    @SerializedName("status")
    @Expose
    var status: Boolean = false;

}
