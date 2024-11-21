package com.librarysdk.sdk.Constants

enum class EndpointList(val endpoint: String) {
    API_TEST("ApiTest"),
    GET_APPLICATION("GetApplication"),
    ADD_DEVICE("AddDevice"),
    GET_PERMISSION_STATUS("GetPermissionStatus"),
    GET_PERMISSION_TEXT("GetPermissionText"),
    ADD_UPDATE_PERMISSION("AddOrUpdatePermission"),
    GET_ALL_ENDPOINTS("GetAllEndpoints"),
    ADD_CROWD_SOURCE("AddCrowdSource")
}