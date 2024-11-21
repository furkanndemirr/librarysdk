package com.librarysdk.sdk.Contents

import android.content.Context

object ContextInfo {
    private var appContext: Context? = null
    fun getContext(): Context {
        return appContext ?: throw IllegalStateException("MyLibrary is not initialized")
    }
    fun setContext(context: Context) {
        appContext = context
    }
}