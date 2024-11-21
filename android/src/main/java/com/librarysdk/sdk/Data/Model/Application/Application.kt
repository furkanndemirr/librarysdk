package com.librarysdk.sdk.Data.Model.Application
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName;

public class Application {

    @SerializedName("id")
    @Expose
    var id: Int = 0;

    @SerializedName("name")
    @Expose
    val name: String = "";

    @SerializedName("guid")
    @Expose
    var guid: String = "";

    @SerializedName("isActive")
    @Expose
    var isActive: Boolean = false;
}