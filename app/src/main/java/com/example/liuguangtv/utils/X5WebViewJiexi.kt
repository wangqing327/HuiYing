package com.example.liuguangtv.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import com.example.liuguangtv.BrosWerAndPlayerParent
import com.example.liuguangtv.R
import com.example.liuguangtv.utils.database.DatabaseFactory
import com.example.liuguangtv.utils.database.History
import com.example.liuguangtv.utils.database.HistoryDao
import com.rxjava.rxlife.life
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.export.external.interfaces.WebResourceResponse
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import rxhttp.RxHttp


class X5WebViewJiexi(context: Context, attributeSet: AttributeSet?) :
    WebView(context, attributeSet) {
    private var i = 0
    var sniffVideoUrlCallBack: SniffVideoUrlCallBack? = null

    /**
     * 是否有播放链接
     * 当有播放链接时，此值为真，用于保存播放记录
     */
    private lateinit var dao: HistoryDao
    private var client: WebViewClient = object : WebViewClient() {
        /**
         * 防止加载网页时调起系统浏览器
         */
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                view.loadUrl(url)
            }
            return true
        }

        override fun onPageFinished(webView: WebView, p1: String) {
            super.onPageFinished(webView, p1)
            if (p1.contains("m1907.cn")) {
                val js = Utils.getFileStreamtoString(Utils.openAssetsFile(context, "css.txt"))
                webView.evaluateJavascript(js, null)
            }
            //广告拦截提示
            if (ShareUtils.getBoolean(context, ShareUtils.AdblockTipState, true)) {
                if (i > 0) {
                    Utils.showToast(
                        context,
                        String.format(context.getString(R.string.ci), i)
                    )
                }
            }
        }

        override fun shouldInterceptRequest(
            p0: WebView,
            p1: WebResourceRequest
        ): WebResourceResponse? {
            val requestUrl = p1.url.toString()
            var webTitle: String? = null
            var webUrl: String? = null
            //取url必须在主线程上回调
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                webTitle = title
                webUrl = url
            }
            var count = 0
            while (webUrl.isNullOrEmpty() && count < 1000000) {
                ++count
            }
            val isHad: Boolean = Adblock.isHad(webUrl, requestUrl)
            //是否启用了视频嗅探
            if (!isHad!!) {
                //先初步确定是不是带关键字的网址:mp4,m3u8
                if (Utils.isVideoUrl(requestUrl)) {
                    RxHttp.get(requestUrl)
                        .asOkResponse()
                        //回调在主线程上，以便更新UI
                        //.observeOn(AndroidSchedulers.mainThread())
                        .life(context as BrosWerAndPlayerParent)
                        .subscribe { response ->
                            //分析返回请求头的类型，进一步确认视频链接是否正确
                            //个别网站会返回image/png，操了，如无名小站
                            val type = response.headers["Content-Type"]
                            if (type.equals("application/vnd.apple.mpegurl", true) ||
                                type.equals("video/mp4", true) ||
                                type.equals("image/png", true) ||
                                type.equals("text/html;charset=utf-8", true)
                            ) {
                                if (VideoFormat.isVideoLink(response.headers["Content-Type"])) {
                                    //保存播放记录
                                    if (!(webTitle.isNullOrEmpty()) && !(webUrl.isNullOrEmpty())) {
                                        val id = dao.queryDataByUrl(webUrl!!, 0)
                                        val history = History(id, webTitle!!, webUrl!!)
                                        dao.insertData(history)
                                    }
                                    if (ShareUtils.getBoolean(context, ShareUtils.VideoXt, true)) {
                                        //视频地址
                                        sniffVideoUrlCallBack?.callback(requestUrl)
                                    }
                                }
                            }
                        }
                }
            } else if (ShareUtils.getBoolean(context, ShareUtils.AdblockIsOpen, true)) {
                return if (!isHad) {
                    super.shouldInterceptRequest(p0, p1)
                } else {
                    ++i
                    ShareUtils.intNodeAdd(context, ShareUtils.AdblockCount)
                    WebResourceResponse(null, null, null)
                }
            }
            return super.shouldInterceptRequest(p0, p1)
        }

        override fun onPageStarted(p0: WebView?, p1: String?, p2: Bitmap?) {
            super.onPageStarted(p0, p1, p2)
            i = 0
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewSettings() {
        dao = DatabaseFactory.getDataBase(context).getHistoryDao()
        settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            allowFileAccess = true
            layoutAlgorithm = LayoutAlgorithm.NARROW_COLUMNS
            setSupportZoom(true)
            builtInZoomControls = true
            useWideViewPort = true
            setSupportMultipleWindows(true)
            loadWithOverviewMode = true
            setAppCacheEnabled(false)
            databaseEnabled = true
            domStorageEnabled = true
            setGeolocationEnabled(true)
            setAppCacheMaxSize(Long.MAX_VALUE)
            setPluginState(WebSettings.PluginState.ON_DEMAND)
            setRenderPriority(WebSettings.RenderPriority.HIGH);
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        // settingsExtension.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY)
        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension
        // settings 的设计
    }

    //region
    /*    	@Override
        protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
            boolean ret = super.drawChild(canvas, child, drawingTime);
            canvas.save();
            Paint paint = new Paint();
            paint.setColor(0x7fff0000);
            paint.setTextSize(24.f);
            paint.setAntiAlias(true);
            if (getX5WebViewExtension() != null) {
                canvas.drawText(this.getContext().getPackageName() + "-pid:"
                        + android.os.Process.myPid(), 10, 50, paint);
                canvas.drawText(
                        "X5  Core:" + QbSdk.getTbsVersion(this.getContext()), 10,
                        100, paint);
            } else {
                canvas.drawText(this.getContext().getPackageName() + "-pid:"
                        + android.os.Process.myPid(), 10, 50, paint);
                canvas.drawText("Sys Core", 10, 100, paint);
            }
            canvas.drawText(Build.MANUFACTURER, 10, 150, paint);
            canvas.drawText(Build.MODEL, 10, 200, paint);
            canvas.restore();
            return ret;
        }*/
    //endregion
    init {
        this.webViewClient = client
        this.view.isClickable = true
        initWebViewSettings()
    }
}
