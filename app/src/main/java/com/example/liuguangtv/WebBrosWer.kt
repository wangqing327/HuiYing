package com.example.liuguangtv

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.webkit.JavascriptInterface
import com.example.liuguangtv.databinding.ActivityWebbroswerBinding
import com.example.liuguangtv.utils.JieXi
import com.example.liuguangtv.utils.Utils
import com.example.liuguangtv.utils.WebViewJavaScriptFunction
import com.example.liuguangtv.utils.X5WebView
import com.tencent.smtt.sdk.WebView


class WebBrosWer : BrosWerAndPlayerParent() {
    private lateinit var binding: ActivityWebbroswerBinding
    private val FULLCREEN = 1
    private val EXITFULLCREEN = 2

    /**
     * 腾讯接口时用的，用于指示是否全屏状态
     */
    //private var JSisFullScreen = false
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityWebbroswerBinding.inflate(layoutInflater)
        initRoot(binding.root)
        super.onCreate(savedInstanceState)
        webViewOnReceivedTitle = object : WebViewOnReceivedTitle {
            override fun webViewOnReceivedTitle(webView: WebView?, title: String?) {
                try {
                    webView?.url?.let { Utils.titleMap.put(it, title!!) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
        //用于显示网页标题
        x5WebView.setTitleChanged(object : X5WebView.TitleChanged {
            override fun titleChanged(url: String) {
                supportActionBar?.title = Utils.titleMap[url]
            }
        })
        x5WebView.addJavascriptInterface(jsCalled, "Android")
        //视频解析被单击
        openJieXiClick = View.OnClickListener {
            runActivityJieXi()
        }
        //StatService.start(this)
    }

    val handler = Handler { msg ->
        when (msg.what) {
            FULLCREEN -> {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or Window.FEATURE_ACTION_BAR_OVERLAY)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                isFullScreen = true
                supportActionBar?.hide()
                if (Build.VERSION.SDK_INT >= 28) {
                    val lp = window.attributes
                    lp.layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    window.attributes = lp
                }
            }
            EXITFULLCREEN -> {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or Window.FEATURE_ACTION_BAR)
                isFullScreen = false
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                if (Build.VERSION.SDK_INT >= 28) {
                    val lp = window.attributes
                    lp.layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
                    window.attributes = lp
                }
                supportActionBar?.show()
            }
        }
        true
    }

    /**
     * js交互回调
     */
    private val jsCalled = object : WebViewJavaScriptFunction {

        override fun onJsFunctionCalled(vararg tags: String) {
            tags.forEach {
                println(it)
            }
        }

        //和js文件约定自定义的接口，js文件中用Android.fullCalled回调到此接口中从而实现交互
        //利用此接口来实现全屏事件的回调

        @JavascriptInterface
        fun fullCalled(status: String) {
            // 腾讯视频 全屏为true，非全屏为false
            if ("true" == status) {
                handler.sendEmptyMessage(FULLCREEN)
            } else {
                handler.sendEmptyMessage(EXITFULLCREEN)
            }
        }
    }

    private fun runActivityJieXi() {
        if (!Utils.isJiexiUrl(x5WebView.url, this)) {
            Utils.showToast(this, R.string.jxwzcw)
        } else {
            val intent = Intent(this, VideoJieXi::class.java)
            var url = x5WebView.url
            if (url.startsWith("https://yun.linhut.cn/index.php?url=")) {
                url = url.split("url=")[1]
            }
            intent.putExtra("url", JieXi.build(this).getJieKouByIndex(0) + url)
            startActivity(intent)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            when {
                isFullScreen -> {
                    //当前是全屏状态，先退出全屏
                    val res = Utils.referParser(x5WebView.url)
                    if ("" != res) {
                        handler.sendEmptyMessage(EXITFULLCREEN)
                    }
                    return true
                }
                parentIsShow -> {
                    binding.urlParent.visibility = View.GONE
                    parentIsShow = false
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    invalidateOptionsMenu()
                    return true
                }
                x5WebView.canGoBack() -> {
                    x5WebView.goBack()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (x5WebView.canGoBack()) {
                    //删除一个网页标题项
                    Utils.titleMap.remove(x5WebView.url)
                    x5WebView.goBack()
                } else {
                    finish()
                }
            }
            R.id.openjiexi -> {
                runActivityJieXi()
            }
            R.id.openMenu -> {
                createMenu()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}