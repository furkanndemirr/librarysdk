package com.librarysdk.sdk.Data.Model.Endpoint
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName;

public class Endpoint {

    @SerializedName("name")
    @Expose
    val name: String = "";

    @SerializedName("uri")
    @Expose
    var uri: String = "";

}