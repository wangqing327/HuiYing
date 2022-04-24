package com.example.liuguangtv

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import android.text.util.Linkify
import android.view.*
import android.view.View.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.liuguangtv.databinding.ActivityWebbroswerBinding
import com.example.liuguangtv.databinding.PopupmenuBinding
import com.example.liuguangtv.settingactivitys.DownLoadActivity
import com.example.liuguangtv.settingactivitys.DownLoadFileInfo
import com.example.liuguangtv.settingactivitys.ui.main.PageViewModel
import com.example.liuguangtv.utils.*
import com.example.liuguangtv.utils.database.DatabaseFactory
import com.example.liuguangtv.utils.database.History
import com.example.liuguangtv.utils.database.HistoryDao
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


/**
 * 浏览器窗口和播放窗口的基类
 */
abstract class BrosWerAndPlayerParent : AppCompatActivity() {
    /**
     * 是否处在全屏状态
     */
    protected var isFullScreen = false

    /**
     * 全屏时的容器，必须设置
     */
    protected lateinit var fullView: FrameLayout

    /**
     * X5内核的浏览器，必须设置
     */
    protected lateinit var x5WebView: X5WebView

    /**
     * 进度条，必须设置
     */
    protected lateinit var progressBar: ProgressBar
    private var itemSelected = 0

    /**
     * 传过来的欲解析的视频地址，只要VideoJieXi不销毁，将一直持有
     */
    protected var videoUrl = ""
    protected var webViewOnReceivedTitle: WebViewOnReceivedTitle? = null
    protected var webViewOnProgressChanged: WebViewOnProgressChanged? = null
    protected var openJieXiClick: OnClickListener? = null

    private lateinit var database: DatabaseFactory
    private lateinit var dao: HistoryDao

    /**
     * 网址框是否在显示，true->显示状态  false->不在显示状态
     */
    protected var parentIsShow = false

    protected var floatButton: FloatButton? = null

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var viewModel: PageViewModel

    private lateinit var binding: ActivityWebbroswerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = DatabaseFactory.getDataBase(this)
        dao = database.getHistoryDao()

        x5WebView.pageStar = object : SniffVideoUrlCallBack {
            override fun callback(videoUrl: String) {
                floatButton?.initSet()
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        window.setFormat(PixelFormat.TRANSLUCENT)

        viewModel = ViewModelProvider(this).get(PageViewModel::class.java)
        floatButton?.setClickCalled(flatButtonClick)
        //pc模式
        if (ShareUtils.getBoolean(this, ShareUtils.WebMode, false)) {
            x5WebView.settings.userAgentString =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:99.0) Gecko/20100101 Firefox/99.0"
        }
        //当嗅探到网页中的视频资源时回调接口
        x5WebView.sniffVideoUrlCallBack = object : SniffVideoUrlCallBack {
            override fun callback(videoUrl: String) {
                lifecycleScope.launch(Dispatchers.Main) {
                    floatButton?.videoCountAdd(DownLoadFileInfo(x5WebView.title, videoUrl))
                }
                //Utils.showToast(this@WebBrosWer, "视频地址:$videoUrl")
            }
        }

        swipeRefreshLayout.setOnRefreshListener {
            x5WebView.reload()
        }


        intent.getStringExtra("url")?.let {
            initWebView(it)
            videoUrl = it
        }
    }

    /**
     * 配置程序主VIEW，继承类必须要实现
     */
    protected fun initRoot(view: View) {
        binding = ActivityWebbroswerBinding.bind(view)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        x5WebView = binding.webview
        fullView = binding.fullView
        progressBar = binding.progressBar
        floatButton = binding.floatButton
        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.BLACK)
        setToolbar(binding.toolbar)
    }

    /**
     * 初始化并载入url
     */
    private fun initWebView(url: String?) {
        //初始化webview
        x5WebView.apply {
            overScrollMode = OVER_SCROLL_ALWAYS
            webChromeClient = chromeClient
            if (x5WebView.x5WebViewExtension != null) {
                loadUrl(url)
            } else {
                //x5内核未加载成功，引导用户修复
                AlertDialog.Builder(this@BrosWerAndPlayerParent)
                    .setMessage(R.string.webxf)
                    .setCancelable(false)
                    .setPositiveButton(
                        R.string.qd
                    ) { _, _ ->
                        loadUrl("http://debugtbs.qq.com")
                    }
                    .create().show()
            }
            //addJavascriptInterface(jsCalled, "Android")
        }
    }

    private var chromeClient = object : WebChromeClient() {
        /**
         * a标签target=_blank时自动调用
         */
        override fun onCreateWindow(
            p0: WebView?,
            p1: Boolean,
            p2: Boolean,
            msg: Message?
        ): Boolean {
            val newWebView = X5WebView(this@BrosWerAndPlayerParent, null)
            newWebView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(p0: WebView?, url: String?): Boolean {
                    x5WebView.loadUrl(url)
                    return true
                }
            }
            val transport = msg?.obj as WebView.WebViewTransport
            transport.webView = newWebView
            msg.sendToTarget()
            return true
        }

        override fun onProgressChanged(p0: WebView, p1: Int) {
            super.onProgressChanged(p0, p1)
            progressBar.progress = p1
            if (progressBar.progress == 100) {
                progressBar.progress = 0
                swipeRefreshLayout.isRefreshing = false
            }
            webViewOnProgressChanged?.webViewOnProgressChanged(p0, p1)
        }


        override fun onShowCustomView(p0: View, p1: IX5WebChromeClient.CustomViewCallback?) {
            setFull(p0)
            super.onShowCustomView(p0, p1)
        }


        override fun onHideCustomView() {
            super.onHideCustomView()
            exitFull()
        }

        override fun onReceivedTitle(p0: WebView?, p1: String?) {
            super.onReceivedTitle(p0, p1)
            webViewOnReceivedTitle?.webViewOnReceivedTitle(p0, p1)
        }
    }

    /**
     * 浏览器进度改变回调，可以在其中做些注入之类的工件
     */
    interface WebViewOnProgressChanged {
        fun webViewOnProgressChanged(webView: WebView, progress: Int)
    }

    /**
     * 浏览器显示标题事件，可在其中保存用于显示在标题中
     */
    interface WebViewOnReceivedTitle {
        fun webViewOnReceivedTitle(webView: WebView?, title: String?)
    }

    override fun onConfigurationChanged(config: Configuration) {
        super.onConfigurationChanged(config)
        when (config.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                isFullScreen = true
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                isFullScreen = false
            }
        }
    }


    /**
     * 设置全屏
     */
    private fun setFull(v: View) {
        supportActionBar?.hide()
        floatButton?.hide()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }
        window.decorView.systemUiVisibility = (SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                SYSTEM_UI_FLAG_FULLSCREEN or Window.FEATURE_ACTION_BAR_OVERLAY)
        x5WebView.visibility = GONE
        fullView.visibility = VISIBLE
        fullView.addView(v)
        setScreenLand()
    }

    /**
     * 退出全屏
     */
    private fun exitFull() {
        supportActionBar?.show()
        floatButton?.show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
            window.attributes = lp
        }
        window.decorView.systemUiVisibility = (SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or Window.FEATURE_ACTION_BAR/* or SYSTEM_UI_FLAG_VISIBLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN*/)

        x5WebView.visibility = VISIBLE
        fullView.visibility = GONE
        fullView.removeAllViews()
        setScreenPort()
    }

    /**
     * 将屏幕方向设置为纵向
     */
    private fun setScreenPort() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    /**
     * 将屏幕方向设置为横向
     */
    private fun setScreenLand() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    /**
     * 创建右上角弹出的菜单
     */
    fun createMenu() {
        val pw = PopupWindow(this)
        val popupView = PopupmenuBinding.bind(
            inflate(
                this,
                R.layout.popupmenu, null
            )
        )
        val menuClicked = OnClickListener { v ->
            pw.dismiss()
            when (v.id) {
                R.id.shuaxin -> {
                    floatButton?.initSet()
                    x5WebView.reload()
                }
                R.id.menuopenjiexi -> {
                    openJieXiClick?.onClick(v)
                }
                R.id.closeweb -> {
                    finish()
                    //清空网页标题map
                    Utils.titleMap = mutableMapOf()
                }
                //加入收藏
                R.id.addFavorite -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val id = dao.queryDataByUrl(x5WebView.url, 1)
                        val history = History(id, x5WebView.title, x5WebView.url, 1)
                        dao.insertData(history)
                    }
                }
                //加入首页
                R.id.addHome -> {
                    val history = History(x5WebView.title, x5WebView.url)
                    val bundle = Bundle()
                    bundle.putSerializable("history", history)
                    val intent = Intent()
                    intent.action = AppBroadcast.BroadCastString
                    intent.putExtra("bundle", bundle)
                    sendBroadcast(intent)
                }
            }
        }
        popupView.shuaxin.setOnClickListener(menuClicked)
        popupView.menuopenjiexi.setOnClickListener(menuClicked)
        popupView.closeweb.setOnClickListener(menuClicked)
        popupView.addFavorite.setOnClickListener(menuClicked)
        popupView.addHome.setOnClickListener(menuClicked)
        pw.apply {
            contentView = popupView.root
            pw.width = ViewGroup.LayoutParams.WRAP_CONTENT
            pw.height = ViewGroup.LayoutParams.WRAP_CONTENT
            isFocusable = true
            isOutsideTouchable = true
            showAsDropDown(x5WebView, 15, 5, Gravity.END)
        }
    }


    /**
     * 创建一个弹窗来显示解析接口选择
     */
    @SuppressLint("DiscouragedPrivateApi")
    protected fun createpw() {
        val pw = PopupWindow(this)
        val view = inflate(this, R.layout.popupjiexiview2, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.jiexilist)
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = linearLayoutManager
        val adapter = AdapterJiekou(R.layout.item_tv, itemSelected)
        adapter.onItemClickListener =
            BaseQuickAdapter.OnItemClickListener { _adapter, _, position ->
                x5WebView.loadUrl(
                    JieXi.build(this).getJieKouByName(_adapter.getItem(position).toString())
                            + videoUrl
                )
                pw.dismiss()
                itemSelected = position
            }
        val array: MutableList<String> = JieXi.build(this).getJieXiTitles()
        recyclerView.adapter = adapter
        adapter.addData(array)
        recyclerView.scrollToPosition(itemSelected)
        val close = view.findViewById<TextView>(R.id.textView3)
        close.setOnClickListener {
            pw.dismiss()
        }
        val close2 = view.findViewById<ImageView>(R.id.imageView4)
        close2.setOnClickListener {
            pw.dismiss()
        }
        pw.apply {
            contentView = view
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
//            白色背景
            setBackgroundDrawable(ColorDrawable(Color.argb(255, 255, 255, 255)))
            isFocusable = true
            isOutsideTouchable = true
            showAtLocation(x5WebView, Gravity.BOTTOM, 0, 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.webmenu, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        x5WebView.sniffVideoUrlCallBack = null
        x5WebView.tChanged = null
        x5WebView.pageStar = null
        x5WebView.destroy()
    }

    override fun onPause() {
        super.onPause()
        x5WebView.onPause()
    }

    override fun onResume() {
        super.onResume()
        x5WebView.onResume()
    }

    private fun setToolbar(toolbar: androidx.appcompat.widget.Toolbar) {
        val parent = toolbar.findViewById<ConstraintLayout>(R.id.urlParent)
        val close = parent.findViewById<TextView>(R.id.qc)
        val editText = parent.findViewById<EditText>(R.id.urlEdit)
        editText.autoLinkMask = Linkify.WEB_URLS

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                val u = editText.text.toString()
                if (u != x5WebView.url) {
                    x5WebView.loadUrl(converKeywordLoadOrSearch(u))
                }
                close.performClick()
            }
            true
        }

        toolbar.setOnClickListener {
            parent.visibility = VISIBLE
            parentIsShow = true
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            editText.setText(x5WebView.url)
            editText.requestFocus()
            Utils.showSoftInput(this, editText)
            invalidateOptionsMenu()
        }

        close.setOnClickListener {
            Utils.hideSoftInput(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            parentIsShow = false
            parent.visibility = GONE
            invalidateOptionsMenu()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.apply {
            findItem(R.id.openjiexi).isVisible = parentIsShow.not()
            findItem(R.id.openMenu).isVisible = parentIsShow.not()
            return true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * floatButton单击事件
     */
    private val flatButtonClick = OnClickListener {
        floatButton?.let {
            Utils.videoFileLinks = it.getVideoLinks()
            val intent = Intent(this@BrosWerAndPlayerParent, DownLoadActivity::class.java)
            startActivity(intent)
        }
    }

    private val HTTP = "http://"
    private val HTTPS = "https://"
    private val FILE = "file://"

    /**
     * 将关键字转换成最后转换的url
     *
     * @param url
     * @return
     */
    private fun converKeywordLoadOrSearch(url: String): String {
        var keyword = url
        keyword = keyword.trim { it <= ' ' }
        if (keyword.startsWith("www.")) {
            keyword = HTTP + keyword
        } else if (keyword.startsWith("ftp.")) {
            keyword = "ftp://$keyword"
        }
        val containsPeriod = keyword.contains(".")
        val isIPAddress = (TextUtils.isDigitsOnly(keyword.replace(".", ""))
                && keyword.replace(".", "").length >= 4 && keyword.contains("."))
        val aboutScheme = keyword.contains("about:")
        val validURL = ((keyword.startsWith("ftp://") || keyword.startsWith(HTTP)
                || keyword.startsWith(FILE) || keyword.startsWith(HTTPS))
                || isIPAddress)
        val isSearch = (keyword.contains(" ") || !containsPeriod) && !aboutScheme
        if (isIPAddress
            && (!keyword.startsWith(HTTP) || !keyword.startsWith(HTTPS))
        ) {
            keyword = HTTP + keyword
        }
        val converUrl: String
        if (isSearch) {
            try {
                keyword = URLEncoder.encode(keyword, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            converUrl = "http://www.baidu.com/s?wd=$keyword&ie=UTF-8"
        } else if (!validURL) {
            converUrl = HTTP + keyword
        } else {
            converUrl = keyword
        }
        return converUrl
    }
}