package com.example.liuguangtv.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Debug
import android.os.StrictMode
import com.baidu.mobstat.StatService
import com.example.liuguangtv.CrashHandler
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk
import rxhttp.RxHttpPlugins


class MyApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var mContext: Context
        fun getContext(): Context {
            return mContext
        }
    }

    override fun onCreate() {
        //策略检查 正式版本应取消
        //是否附加了调试器
        if (Debug.isDebuggerConnected()) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    /*.detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()*/
                    .detectAll() //检测所有
                    .penaltyLog()
                    .build()
            )
        }
        super.onCreate()
        mContext = applicationContext
        //取消qbsdk的必须wifi环境才能下载的限制
        QbSdk.setDownloadWithoutWifi(true)
        initX5Tbs(mContext)
        RxHttpPlugins.init(HttpUtils.getInstance().client).setDebug(false)
        // 通过该接口可以控制敏感数据采集，true表示可以采集，false表示不可以采集，
        // 该方法一定要最优先调用，请在StatService.autoTrace(Context context)
        // 之前调用，采集这些数据可以帮助App运营人员更好的监控App的使用情况，
        // 建议有用户隐私策略弹窗的App，用户未同意前设置false,同意之后设置true
        StatService.setAuthorizedState(applicationContext, true)
        StatService.autoTrace(applicationContext, true, true)
//        // 获取测试设备ID
//        // 	7ECE5B993D439785FB9CFC40A0C873AD
//        val testDeviceId = StatService.getTestDeviceId(this)
//        Log.d("BaiduMobStat", "Test DeviceId : $testDeviceId")
        //全局错误处理
        try {
            CrashHandler.instance.init(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 初始化X5内核引擎
     */
    private fun initX5Tbs(context: Context) {
        // 在调用TBS初始化、创建WebView之前进行如下配置
        val map = HashMap<String, Any>()
        map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true
        map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true
        QbSdk.initTbsSettings(map)
        //不回调
        /*val cb: PreInitCallback = object : PreInitCallback {
            override fun onCoreInitFinished() {}
            override fun onViewInitFinished(b: Boolean) {
                if (!b) {
                    //自动下载远程内核并安装
                    if (!QbSdk.canLoadX5(this@MyApplication) && !TbsDownloader.isDownloading()) {
                        TbsDownloader.startDownload(this@MyApplication)
                    }
                }
            }
        }
        QbSdk.initX5Environment(context, cb)*/
    }
}