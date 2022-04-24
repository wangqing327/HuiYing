package com.example.liuguangtv

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.SearchView.OnQueryTextListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.example.liuguangtv.databinding.ActivityMainBinding
import com.example.liuguangtv.settingactivitys.JlScActivity
import com.example.liuguangtv.settingactivitys.SettingAdblockActivity
import com.example.liuguangtv.settingactivitys.SettingBroswermodeActivity
import com.example.liuguangtv.settingactivitys.SettingVideoXt
import com.example.liuguangtv.utils.*
import com.example.liuguangtv.utils.CacheClear.getFormatSize
import com.example.liuguangtv.utils.database.DatabaseFactory
import com.example.liuguangtv.utils.database.History
import com.example.liuguangtv.utils.database.ItemTableDao
import com.example.liuguangtv.utils.database.MyDatabaseTable
import com.rxjava.rxlife.life
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rxhttp.RxHttp
import update.UpdateAppUtils
import java.io.File
import java.lang.reflect.Method


class MainActivity : AppCompatActivity(), AppBroadcast.ReceivedData {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mAdapter: AdapterIndex
    private lateinit var database: DatabaseFactory
    private lateinit var itemTableDao: ItemTableDao
    private var lastTime = 0L
    private val searchUrls: MutableMap<Int, String> = mutableMapOf(
        0 to "https://cupfox.app/search?key=",
        1 to "https://www.dianyinggou.com/so/",
        2 to "https://m.btnull.org/s/go.php?t=1&q=",
        3 to "https://m.video.360kan.com/s?q=",
        4 to "http://www.402dy.com/index.php?mode=search&q="
    )

    /**
     * 是否处于首页列表编辑模式
     */
    private var editMode = false

    /**
     * 广播服务
     */
    private var appBroadcast: AppBroadcast? = null

    /**
     * 记录表项被单击的序列，以实时更新RecyclerView中的标题
     */
    private var itemSelected = -1
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        //添加屏幕常亮的状态，在此取消，在打开网页的activity中设置keepScreenOn=true效果一样
//        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
//            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24_01)
        }
        //注册广播
        appBroadcast = AppBroadcast()
        appBroadcast?.setReceiveData(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(AppBroadcast.BroadCastString)
        registerReceiver(appBroadcast, intentFilter)

        //侧滑界面事件监听，改变home图标
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

            }

            override fun onDrawerOpened(drawerView: View) {
                Utils.hideSoftInput(this@MainActivity)
                binding.included.abCount.text = String.format(
                    resources.getString(R.string.tiao), ShareUtils.getInt(
                        this@MainActivity, ShareUtils.AdblockCount
                    )
                )
                calculateCacheSize()
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu2)
            }

            override fun onDrawerClosed(drawerView: View) {
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24_01)

            }

            override fun onDrawerStateChanged(newState: Int) {

            }

        })
        //侧滑菜单按钮事件统一设置监听
        bindingSideClick()

        binding.search.isSubmitButtonEnabled = true
        binding.search.setOnQueryTextListener(searchViewCallBack)

        //按钮事件
        binding.btnDefault.setOnClickListener(btnClicked)
        binding.btnOk.setOnClickListener(btnClicked)
        binding.btnQx.setOnClickListener(btnClicked)


        //列表设置
        val stManager = StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerView.layoutManager = stManager
        mAdapter = AdapterIndex(R.layout.item_index)
        mAdapter.apply {
            onItemClickListener = itemClickListener
            onItemChildClickListener = itemChildClickListener
            onItemLongClickListener = itemLongClickListener
        }
        database = DatabaseFactory.getDataBase(this)
        itemTableDao = database.getItemTableDao()
        binding.recyclerView.adapter = mAdapter
        initDate()
        checkUpdate()
    }

    /**
     * 绑定侧滑菜单中各种按钮单击事件处理
     */
    private fun bindingSideClick() {
        binding.included.ShareView.setOnClickListener(sideButtonClick)
        binding.included.userImage.setOnClickListener(sideButtonClick)
        binding.included.login.setOnClickListener(sideButtonClick)
        binding.included.register.setOnClickListener(sideButtonClick)
        binding.included.downloadView.setOnClickListener(sideButtonClick)
        binding.included.historyView.setOnClickListener(sideButtonClick)
        binding.included.favoriteView.setOnClickListener(sideButtonClick)
        binding.included.ShareView.setOnClickListener(sideButtonClick)
        binding.included.setBroMode.setOnClickListener(sideButtonClick)
        binding.included.setVideoXt.setOnClickListener(sideButtonClick)
        binding.included.setCacheCLear.setOnClickListener(sideButtonClick)
        binding.included.setAboutWe.setOnClickListener(sideButtonClick)
        binding.included.setAdbCount.setOnClickListener(sideButtonClick)
        binding.included.textView11.setOnClickListener(sideButtonClick)
        binding.included.abCount.text = String.format(
            getString(R.string.tiao),
            ShareUtils.getInt(this, ShareUtils.AdblockCount)
        )
    }

    /**
     * 添加或取消Adapter中拖动排序事件的回调与取消息回调，因为此事件与Item被长按事件有冲突
     * @param add ->true,增加拖动回调事件
     *            ->false,取消拖动回调事件
     */
    private fun changedItemDragListener(add: Boolean) {
        if (add) {
            val itemDragAndSwipeCallback = ItemDragAndSwipeCallback(mAdapter)
            val itemTouchHelper = ItemTouchHelper(itemDragAndSwipeCallback)
            itemTouchHelper.attachToRecyclerView(binding.recyclerView)
            //设置可拖拽排序
            mAdapter.enableDragItem(itemTouchHelper, R.id.itemDiv, true)
            mAdapter.setOnItemDragListener(onItemDragListener)
        } else {
            mAdapter.disableDragItem()
        }
    }

    /**
     * 表项被单击回调
     */
    private val itemClickListener =
        BaseQuickAdapter.OnItemClickListener { adapter, _, position ->
            val item = adapter.getItem(position) as MyDatabaseTable
            if (binding.constraintLayout.visibility == View.VISIBLE) {
                itemSelected = position
                binding.navTitle.setText(item.title)

            } else {
                val intent = Intent(this, WebBrosWer::class.java)
                intent.putExtra("url", item.url)
                startActivity(intent)
                itemSelected = -1
            }
        }

    /**
     * 表项中的删除按钮被单击
     */
    private val itemChildClickListener =
        BaseQuickAdapter.OnItemChildClickListener { adapter, _, position ->
            lifecycleScope.launch(Dispatchers.IO) {
                val table = adapter.getItem(position) as MyDatabaseTable
                if (table.id == 0) {
                    itemTableDao.deleteItemDataByUrl(table.url)
                } else {
                    itemTableDao.deleteItemDataByID(table.id)
                }

                withContext(Dispatchers.Main) {
                    adapter.remove(position)
                }
            }
            itemSelected = -1
        }

    /**
     * 拖动排序回调
     */
    private val onItemDragListener = object : OnItemDragListener {
        override fun onItemDragStart(p0: RecyclerView.ViewHolder?, p1: Int) {}

        override fun onItemDragMoving(
            p0: RecyclerView.ViewHolder?,
            p1: Int,
            p2: RecyclerView.ViewHolder?,
            p3: Int
        ) {
        }

        override fun onItemDragEnd(p0: RecyclerView.ViewHolder?, p1: Int) {
            itemSelected = -1
            binding.navTitle.setText("")
        }
    }

    /**
     * 表项被长按
     */
    private val itemLongClickListener =
        BaseQuickAdapter.OnItemLongClickListener { adapter, _, position ->
            editMode = true
            binding.navTitle.setText("")
            itemSelected = position
            binding.navTitle.setText((adapter.getItem(position) as MyDatabaseTable).title)
            mAdapter.setDeleteVisible(true)
            changedItemDragListener(true)
            binding.constraintLayout.visibility = View.VISIBLE
            true
        }

    /**
     * 装载数据
     */
    private fun initDate() {
        mAdapter.setNewData(null)
        lifecycleScope.launch(Dispatchers.IO) {
            val data = itemTableDao.queryItemAllData()
            withContext(Dispatchers.Main) { mAdapter.addData(data) }
        }
        RxHttp.get(Utils.HOME + "itemlist.txt")
            .asList(MyDatabaseTable::class.java)
            .observeOn(AndroidSchedulers.mainThread()) //在主线程上回调
            .life(this)
            .subscribe({
                //请求网络成功
                mAdapter.addData(it)
            }, {
                //请求网络失败
                Utils.showToast(this, R.string.netError)
            })
        lifecycleScope.launch(Dispatchers.IO) {
            Adblock.getInstance(this@MainActivity)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            RxHttp.get(Utils.HOME + "searchJieKou.txt")
                .asList(String::class.java)
                .life(this@MainActivity)
                .subscribe {
                    var i = 0
                    it.forEach { s ->
                        searchUrls[i++] = s
                    }
                }
        }
    }

    /**
     * 搜索按钮被单击
     */
    private val searchViewCallBack = object : OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            query?.let {
                val intent = Intent(this@MainActivity, WebBrosWer::class.java)
                //电影狗[比较全面]  https://www.dianyinggou.com/so/关键字
                intent.putExtra(
                    "url", searchUrls[ShareUtils.getInt(
                        this@MainActivity,
                        ShareUtils.searchJieKou
                    )] + it
                )
                startActivity(intent)
            }
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            return false
        }

    }

    private val btnClicked = View.OnClickListener { v: View ->
        when (v.id) {
            R.id.btn_qx -> {
                mAdapter.setDeleteVisible(false)
                editMode = false
                changedItemDragListener(false)
                binding.constraintLayout.visibility = View.GONE
            }
            R.id.btn_default -> {
                initDate()
            }
            R.id.btn_ok -> {
                binding.btnQx.performClick()
                val data = mAdapter.data
                //排序
                for (i in 0 until data.size) {
                    data[i].zIndex = i
                }
                if (itemSelected > -1) {
                    data[itemSelected].title = binding.navTitle.text.toString()
                    mAdapter.setData(itemSelected, data[itemSelected])
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    val databaseTable = itemTableDao.queryItemAllData()
                    databaseTable.forEach { i ->
                        data.forEach { d ->
                            if (d.url == i.url) {
                                if (d.id > 0) {
                                    itemTableDao.updateItemDataById(d.id, d.title)
                                } else {
                                    itemTableDao.updateItemDataByUrl(d.url, d.title)
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    /**
     * 检查是否需要更新
     */
    private fun checkUpdate() {
        lifecycleScope.launch {
            UpdateAppUtils.init(this@MainActivity)
            RxHttp.get(Utils.HOME + "update.txt")
                .asClass(UpdateJson::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .life(this@MainActivity)
                .subscribe {
                    val code = Utils.getAppVersionCode(this@MainActivity)
                    if (code < it.getVersionCode()) {
                        Updates.build(
                            it.getApkUrl(),
                            it.getIsFore(),
                            it.getUpdateContent(),
                            it.getVersionCode(),
                            it.getVersionName()
                        )
                    }
                }
        }
    }

    @SuppressLint("ResourceType")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.searchjiekou, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search1 -> {
                ShareUtils.writeInt(this, ShareUtils.searchJieKou, 0)
            }
            R.id.search2 -> {
                ShareUtils.writeInt(this, ShareUtils.searchJieKou, 1)
            }
            R.id.search3 -> {
                ShareUtils.writeInt(this, ShareUtils.searchJieKou, 2)
            }
            R.id.search4 -> {
                ShareUtils.writeInt(this, ShareUtils.searchJieKou, 3)
            }
            R.id.search5 -> {
                ShareUtils.writeInt(this, ShareUtils.searchJieKou, 4)
            }
            android.R.id.home -> {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawers()
                } else {
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                }
            }
        }
        item.setIcon(R.drawable.menu_selected)
        return super.onOptionsItemSelected(item)
    }

    /**
     * 让菜单显示图标
     */
    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        val selected = ShareUtils.getInt(this, ShareUtils.searchJieKou)
        for (index in 0 until menu.size()) {
            val item = menu.getItem(index)
            if (index == selected) {
                item.setIcon(R.drawable.menu_selected)
            } else {
                item.setIcon(R.drawable.menu_null)
            }
        }

        if (menu.javaClass.simpleName.equals("MenuBuilder", ignoreCase = true)) {
            try {
                val method: Method = menu.javaClass.getDeclaredMethod(
                    "setOptionalIconsVisible",
                    java.lang.Boolean.TYPE
                )
                method.isAccessible = true
                method.invoke(menu, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    /**
     * 侧滑菜单单击事件统一处理程序
     */
    private val sideButtonClick = View.OnClickListener {
        when (it.id) {
            //用户头像
            R.id.userImage -> {
                Utils.showToast(this, R.string.dgx)
            }
            //登录按钮
            R.id.login -> {
                Utils.showToast(this, R.string.dgx)
            }
            //注册
            R.id.register -> {
                Utils.showToast(this, R.string.dgx)
            }
            //  离线缓存
            R.id.downloadView -> {
                Utils.showToast(this, R.string.dgx)
            }
            // 历史记录
            R.id.historyView -> {
                val intent = Intent(this, JlScActivity::class.java)
                intent.putExtra("index", 0)
                startActivity(intent)
            }
            //我的收藏
            R.id.favoriteView -> {
                val intent = Intent(this, JlScActivity::class.java)
                intent.putExtra("index", 1)
                startActivity(intent)
            }
            //分享
            R.id.ShareView -> {
                Utils.showToast(this, R.string.dgx)
            }
            //设置浏览器模式
            R.id.setBroMode -> {
                startActivity(Intent(this, SettingBroswermodeActivity::class.java))
            }
            //视频地址嗅探
            R.id.setVideoXt -> {
                startActivity(Intent(this, SettingVideoXt::class.java))
            }
            //缓存垃圾清理
            R.id.setCacheCLear -> {
                deleteAllCache()
            }
            //关于我们
            R.id.setAboutWe -> {
                Utils.showToast(this, R.string.dgx)
            }
            // 累计广告清理计数
            R.id.setAdbCount -> {
                startActivity(Intent(this, SettingAdblockActivity::class.java))
                //binding.drawerLayout.closeDrawers()
            }
            //免责协议
            R.id.textView11 -> {
                Utils.showToast(this, R.string.dgx)
            }
            //常见问题
            R.id.problem -> {
                Utils.showToast(this, R.string.dgx)
            }
        }
    }

    /**
     * 计算应用缓存文件的大小并显示在菜单中
     */
    private fun calculateCacheSize() {
        lifecycleScope.launch(Dispatchers.IO) {
            val paths = getCacheDirs()
            var size = 0L
            paths.forEach {
                size += CacheClear.getFolderSize(it)
            }
            val str = getFormatSize(size)
            withContext(Dispatchers.Main) {
                binding.included.ljxs.text = str
            }
        }
    }

    /**
     * 返回app下的缓存目录及文件数组
     */
    private fun getCacheDirs(): ArrayList<File> {
        val paths = arrayListOf<File>()
        paths.add(cacheDir)
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            externalCacheDir?.let { paths.add(it) }
        }
        val dataPath = "/data/data/$packageName"
        paths.add(File("$dataPath/code_cache"))
        paths.add(File("$dataPath/filescommonCache"))
        paths.add(File("$dataPath/app_x5webview"))
        paths.add(File("$dataPath/app_webview"))
        paths.add(File("$dataPath/app_textures"))
        paths.add(File("$dataPath/files/live_log"))
        return paths
    }

    /**
     * 删除所有缓存
     */
    private fun deleteAllCache() {
        lifecycleScope.launch {
            val paths = getCacheDirs()
            paths.forEach {
                CacheClear.deleteFolderFile(it.path, true)
            }
            withContext(Dispatchers.Main) {
                Utils.showToast(this@MainActivity, R.string.qcwc)
                binding.included.ljxs.text = "0KB"
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (binding.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                binding.drawerLayout.closeDrawers()
                return true
            }
            if (editMode) {
                binding.btnQx.performClick()
                return true
            }
            return backCalled()
        }
        return true
    }

    /**
     * 计算上次按下返回键与现在按下的返回键的时间差，小于2s则finish
     */
    private fun backCalled(): Boolean {
        if (System.currentTimeMillis() - lastTime <= 2000) {
            finish()
        } else {
            lastTime = System.currentTimeMillis()
            Utils.showToast(this, R.string.exitActivity)
        }
        return true
    }

    /**
     * 反注册广播
     */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(appBroadcast)
    }

    //广播接收器接到了数据
    override fun receiveData(history: History) {
        lifecycleScope.launch(Dispatchers.IO) {
            val id = itemTableDao.queryItemByUrl(history.url)
            // == 0,则表中没有数据，在添加
            if (id == 0) {
                val table =
                    MyDatabaseTable(id, history.title, history.url, buildUrlIco(history.url))
                itemTableDao.insertItemData(table)
                withContext(Dispatchers.Main) {
                    mAdapter.addData(0, table)
                }
            }
        }
        Utils.showToast(this, R.string.ytjzzm)
    }

    /**
     * 获取网址图标地址，不一定准确
     */
    @Throws(Exception::class)
    private fun buildUrlIco(url: String): String? {
        /*var index = 0
        //取第三个/后加favicon.ico
        for (i in 1..3) {
            index = url.indexOf("/", index + 1)
        }
        if (index == -1) {
            return null
        }
        val str = url.substring(0, index)*/
        //return "$str/favicon.ico"
        //两个方法二选一
        val uri = Uri.parse(url)
        var http: String = url.substring(0, 5)
        http = if (http.endsWith(":")) {
            //http://
            "$http//"
        } else {
            //https://
            "$http://"
        }
        return http + uri.host + "/favicon.ico"
    }
}