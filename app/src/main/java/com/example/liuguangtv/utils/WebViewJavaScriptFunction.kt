package com.example.liuguangtv.utils

import android.webkit.JavascriptInterface

interface WebViewJavaScriptFunction {
    /**
     * 回调接口
     * @param tags 可变参数
     */
    @JavascriptInterface
    fun onJsFunctionCalled(vararg tags: String)
}