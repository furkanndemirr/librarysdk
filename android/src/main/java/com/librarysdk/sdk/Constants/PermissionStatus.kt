package com.librarysdk.sdk.Constants

enum class PermissionStatus(val status: String) {
    ONAY("permissionGranted"),
    RED("noPermission"),
    KAYIT_YOK("permissionNotGranted")
}