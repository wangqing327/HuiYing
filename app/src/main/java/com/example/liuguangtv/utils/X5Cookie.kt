package com.example.liuguangtv.utils

import com.tencent.smtt.sdk.CookieManager
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

class X5Cookie {
    companion object {
        @OptIn(InternalCoroutinesApi::class)
        val instance: CookieManager by lazy {
            synchronized(this) {
                CookieManager.getInstance()
            }
        }


        fun removeAllCookies() {
            instance.removeAllCookies(null)
        }
    }
}