package com.example.liuguangtv.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.webkit.ValueCallback
import com.example.liuguangtv.BrosWerAndPlayerParent
import com.example.liuguangtv.R
import com.example.liuguangtv.utils.database.DatabaseFactory
import com.example.liuguangtv.utils.database.History
import com.example.liuguangtv.utils.database.HistoryDao
import com.rxjava.rxlife.life
import com.tencent.smtt.export.external.extension.proxy.ProxyWebViewClientExtension
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.export.external.interfaces.WebResourceResponse
import com.tencent.smtt.sdk.CookieSyncManager
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import rxhttp.RxHttp


class X5WebView(context: Context, attributeSet: AttributeSet?) :
    WebView(context, attributeSet) {
    /**
     * 对于嗅探到的网络视频地址的回调
     */
    var sniffVideoUrlCallBack: SniffVideoUrlCallBack? = null
    private var i = 0

    /**
     * 网页开始加载回调件
     */
    var pageStar: SniffVideoUrlCallBack? = null
    /**
     * 网页title改变回调事件
     */
    var tChanged: TitleChanged? = null
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

        /**
         * 执行js屏蔽广告
         */
        override fun onPageFinished(webView: WebView, p1: String) {
            super.onPageFinished(webView, p1)

            val js = Adblock.returnJs(p1)
            if (js.isNotEmpty()) {
                webView.evaluateJavascript(js, null)
                //++i
            }

            //当webview为手机模式时才需要注入，pc模式则不注入
            /* val js = Utils.returnJS(p1,Utils.referParser(p1))
             if (js != "") {
                 webView.evaluateJavascript(js,null)
             }*/
            if (ShareUtils.getBoolean(context, ShareUtils.AdblockTipState, true)) {
                if (i > 0) {
                    Utils.showToast(
                        context,
                        String.format(context.getString(R.string.ci), i)
                    )
                    i = 0
                }
            }
        }

        override fun onPageStarted(p0: WebView?, p1: String?, p2: Bitmap?) {
            super.onPageStarted(p0, p1, p2)
            i = 0
            pageStar?.callback(url)
        }

        override fun onPageCommitVisible(p0: WebView, p1: String) {
            super.onPageCommitVisible(p0, p1)
            tChanged?.titleChanged(url)
        }

        override fun shouldInterceptRequest(
            p0: WebView,
            p1: WebResourceRequest
        ): WebResourceResponse? {
            val requestUrl = p1.url.toString()
            var webTitle: String? = null
            var webUrl: String? = null
            //取url必须在主线程上回调
            Handler(Looper.getMainLooper()).post{
                webTitle = title
                webUrl = url
            }
            var count = 0
            while (webUrl.isNullOrEmpty() && count < 1000000) {
                ++count
            }
            //assert(webUrl.isNullOrEmpty().not())
            val isHad: Boolean = Adblock.isHad(webUrl, requestUrl)
            if (!isHad) {
                if (Utils.isVideoUrl(requestUrl)) {
                    RxHttp.get(requestUrl)
                        .asOkResponse()
                        //.observeOn(AndroidSchedulers.mainThread())  //回调在主线程上，以便更新UI
                        .life(context as BrosWerAndPlayerParent)
                        .subscribe { response ->
                            if (VideoFormat.isVideoLink(response.headers["Content-Type"])) {
                                //保存播放记录
                                if (!webTitle.isNullOrEmpty() && !webUrl.isNullOrEmpty()) {
                                    val id = dao.queryDataByUrl(webUrl!!, 0)
                                    val history = History(id, webTitle!!, webUrl!!)
                                    dao.insertData(history)
                                }
                                //当设置为嗅探地址时，回调通知接口
                                if (ShareUtils.getBoolean(context, ShareUtils.VideoXt, true)) {
                                    //视频地址
                                    sniffVideoUrlCallBack?.callback(requestUrl)
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
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewSettings() {
        dao = DatabaseFactory.getDataBase(context).getHistoryDao()
        settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = false
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

        //settingsExtension
        settingsExtension?.apply {
            //设置自动弹窗被拦截时提醒（回调接口）
            setJavaScriptOpenWindowsBlockedNotifyEnabled(true)
            //前进后退缓存，不再重新加载
            setContentCacheEnable(true)
        }
        x5WebViewExtension?.let {
            it.webViewClientExtension = object : ProxyWebViewClientExtension(){
                /**
                 * 当js在非用户操作下打开新页面被内核拦截且宿主也不允许当前页面自动打开时，对应页面会被内核阻止
                 * 如果在IX5WebSettingsExtension设置了setJavaScriptOpenWindowsBlockedNotifyEnabled为true时
                 * 此时会回调宿主当前拦截的所有页面是否允许被打开
                 * 如果callback回调true，后续对应页面的弹窗再被拦截时则会直接按照此授权处理,不再回调该接口通知宿主
                 * 如果callback回调false，页面下次弹出窗口被拦截仍会通知宿主，但此时hadAllowShow的值为true
                 * @param host 页面域名
                 * @param blockedUrlList 被阻止打开的页面url列表
                 * @param callback true:打开拦截页面且后续不再拦截，false:打开宿主页面后续继续拦截,如果已经做过该操作,则后续回调接口中hadAllowShow为true
                 * @param hadAllowShow 是否允许展示过该host的弹出窗口，当曾经设置过callback<false>时该值为true,否则为false
                 * @return 宿主处理了该接口返回true，否则返回false
                 */

                override fun notifyJavaScriptOpenWindowsBlocked(
                    host: String,
                    blockedUrlList: Array<out String>,
                    callback: ValueCallback<Boolean>,
                    hadAllowShow: Boolean
                ): Boolean {
                    //return super.notifyJavaScriptOpenWindowsBlocked(p0, p1, p2, p3)
                    return false
                }
            }
        }
        // manage Cookie
        CookieSyncManager.createInstance(context)
        CookieSyncManager.getInstance().sync()
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


    fun setTitleChanged(TitleChanged: TitleChanged?) {
        this.tChanged = TitleChanged
    }

    interface TitleChanged {
        fun titleChanged(url: String)
    }
}
