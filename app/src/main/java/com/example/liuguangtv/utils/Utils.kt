package com.example.liuguangtv.utils

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Process
import android.text.TextPaint
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.liuguangtv.MainActivity
import com.example.liuguangtv.R
import com.example.liuguangtv.settingactivitys.DownLoadFileInfo
import com.tencent.smtt.sdk.QbSdk
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.regex.Pattern


/**
 * 常用工具集
 */
object Utils {
    private const val videoUrl = "^http.*?/.+(\\.mp4|\\.m3u8).*"
    private val pattern = Pattern.compile(videoUrl, Pattern.MULTILINE)

    private var jxurls: Array<String>? = null

    /**
     * https://www.wangqing.work/video/data/
     */
    const val HOME = "https://www.wangqing.work/video/data/"

    /**
     * 网页title
     */
    var titleMap: MutableMap<String, String> = mutableMapOf()
    /**
     * 截获的下载文件链接在两个Activity之间传递所用，用后即清
     */

    var videoFileLinks:MutableList<DownLoadFileInfo>? = null

    /**
     * 将inputStream转为String
     * @param inputStream 文件流数据
     * @return 返回String文本
     */
    fun getFileStreamtoString(inputStream: InputStream): String {
        val outputStream = ByteArrayOutputStream()
        try {
            val byte = ByteArray(1024)
            var i = 0
            while (i != -1) {
                i = inputStream.read(byte)
                outputStream.write(byte, 0, i)
            }

        } catch (e: Exception) {
            try {
                inputStream.close()
                outputStream.close()
            } catch (e1: Exception) {
                // e1.printStackTrace()
            }
            //e.printStackTrace()
        }
        return outputStream.toString()
    }

    fun getFileStreamtoByteArrayOutPut(inputStream: InputStream): ByteArrayOutputStream {
        val outputStream = ByteArrayOutputStream()
        try {
            val byte = ByteArray(1024)
            var i = 0
            while (i != -1) {
                i = inputStream.read(byte)
                outputStream.write(byte, 0, i)
            }

        } catch (e: Exception) {
            try {
                inputStream.close()
                outputStream.close()
            } catch (e1: Exception) {
                // e1.printStackTrace()
            }
            //e.printStackTrace()
        }
        return outputStream
    }

    /**
     * 打开Assets资产文件
     * @param context 上下文环境
     * @param fileName 文件名称
     * @return 返回InputStream文件流
     */
    fun openAssetsFile(context: Context, fileName: String): InputStream {
        return context.assets.open(fileName)
    }

    /**
     * 是否媒体文件网址
     */
    fun isVideoUrl(url: String): Boolean {
        return pattern.matcher(url).find()
    }

    /**
     * 显示一个Toast
     */
    fun showToast(context: Context, msg: String) {
        showToast(context, msg, Toast.LENGTH_SHORT)
    }

    fun showToast(context: Context, msg: Int) {
        showToast(context, msg, Toast.LENGTH_SHORT)
    }

    fun showToast(context: Context, msg: Int, duration: Int) {
        Toast.makeText(context, msg, duration).show()
    }

    fun showToast(context: Context, msg: String, duration: Int) {
        Toast.makeText(context, msg, duration).show()
    }

    /**
     * drawable转换为Bitmap
     * @param drawable-drawable数据
     * @param w-宽度
     * @param h-高度
     */
    fun drawableToBitmap(drawable: Drawable, w: Int, h: Int): Bitmap {
        var height = drawable.intrinsicHeight
        var width = drawable.intrinsicWidth
        if (height == -1) {
            height = h
            width = w
        }
        drawable.setBounds(0, 0, width, height)
        val config =
            if (drawable.alpha !== PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        val bitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(bitmap)
        // 将drawable 内容画到画布中
        // 将drawable 内容画到画布中
        drawable.draw(canvas)
        return bitmap


    }

    /**
     * drawable转换为Bitmap
     * @param drawable-drawable数据
     * @see drawableToBitmap
     * @return 默认返回的是35X35的Bitmap
     *
     */
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        return drawableToBitmap(drawable, 35, 35)
    }

    /**
     * 写出指定文字到Bitmap数据中央并返回
     */
    fun writeText2Bitmap(s: String, bitmap: Bitmap, fontSize: Float): Bitmap {
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG)
        textPaint.apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#FFFFFF")
            textAlign = Paint.Align.CENTER
            textSize = fontSize
        }
        val rect = Rect()
        rect.apply {
            left = 0
            right = bitmap.width
            top = 0
            bottom = bitmap.height
        }
        val fontMetrics = textPaint.fontMetrics
        val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        val baseline = rect.centerY() + distance
        val newb = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newb)
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, null)
        canvas.drawText(s, rect.centerX().toFloat(), baseline, textPaint)
        canvas.save()
        canvas.restore()
        return newb
    }

    /**
     * 清理缓存
     */
    fun clearCache(mContext: Context) {
        //清除所有缓存
        QbSdk.clearAllWebViewCache(mContext, true)
//        val file: File? = Glide.getPhotoCacheDir(mContext)
//        FileUtils.Companion.deleteFolderFile(file.getPath(), true)
    }

    /**
     * 重新启动app
     */
    fun reStarApp(mContext: Context) {
        val intent = Intent(mContext, MainActivity::class.java)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
        mContext.startActivity(intent)
        Process.killProcess(Process.myPid())
    }

    fun AnsiToUtf8(str: String?): String {
        var result = ""
        try {
            result = URLEncoder.encode(str, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        // Log.d("TAG", "AnsiToUtf8: "+result);
        return result.replace("%".toRegex(), "_")
    }

    /**
     * 反转字符串 abc->cba
     *
     * @param str
     * @return
     */
    fun reverse_Str(str: String): String {
        return StringBuilder(str).reverse().toString()
    }

    /**
     * 根据当前设备取得换算权重值
     *
     * @return 权重值
     */
    private fun getScale(mContext: Context): Float {
        return mContext.resources.displayMetrics.density
    }

    /**
     * 根据设备的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dip(context: Context, pxValue: Float): Int {
        return (pxValue / getScale(context) + 0.5f).toInt()
    }

    /**
     * 根据设备分辨率将dp转为px
     *
     * @param dpValue
     * @return
     */
    fun dip2px(context: Context, dpValue: Float): Int {
        return (dpValue * getScale(context) + 0.5f).toInt()
    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    fun getCurrentWidth(mContext: Context): Int {
        return mContext.resources.displayMetrics.widthPixels
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    fun getCurrentHeight(mContext: Context): Int {
        return mContext.resources.displayMetrics.heightPixels
    }

    /**
     * 获取窗口高度，此高度不包含状态栏
     *
     * @return
     */
    fun getWindowHeight(context: Context): Int {
        return getCurrentHeight(context) - getStatusBarHeight(context)
    }

    /**
     * 显示或隐藏输入法，如果当前输入法显示，则隐藏，如果当前输入法隐藏，则唤起输入法
     */
    private fun toggleSoftInput(mContext: Context) {
        // 取得输入法管理器
        val imm: InputMethodManager =
            mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // 显示或者隐藏输入法
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    //输入法是否处于活动状态
    private fun softInputIsShowing(mContext: Context): Boolean {
        val imm: InputMethodManager =
            mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.isActive
    }

    /**
     * 显示输入法键盘
     * @param view 接受键盘输入的控件，如EditText
     */
    fun showSoftInput(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }

    /**
     * 隐藏输入法键盘
     */
    fun hideSoftInput(context: Context) {
        val imm: InputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow((context as AppCompatActivity).window.decorView.windowToken, 0)
    }

    /**
     * 获取状态栏高度
     *
     * @return
     */
    fun getStatusBarHeight(mContext: Context): Int {
        val resources: Resources = mContext.resources
        val resourceId: Int = resources.getIdentifier(
            "status_bar_height", "dimen", "android"
        )
        return resources.getDimensionPixelSize(resourceId)
    }

    /**
     * 对不同的视频网站分析相应的全屏控件
     * @param url 加载的网页地址
     * @return 相应网站全屏按钮的class标识
     */
    fun referParser(url: String): String {
        when {
            url.contains("le.com") -> {
                //hv_botbar_btn可以  hv_ico_screen不行，待解决
                return "hv_ico_screen"   //乐视Tv
            }
            url.contains("bilibili") -> {
                return "mplayer-icon mplayer-icon-widescreen" //bilibili
            }
            url.contains("qq") -> {
                return "txp_btn txp_btn_fullscreen txp_btn_fake"   //腾讯视频
            }
        }
        return ""
    }

    fun returnJS(url: String, className: String): String {
        var js = ""
        when {
            url.contains("qq.com") -> {
                js = "javascript: var bt = document.getElementsByClassName(\"$className\")\n" +
                        "if (bt[0] != null && bt[0] != undefined) {\n" +
                        "    bt[0].onclick = function () {\n" +
                        "        Android.fullCalled(this.getAttribute('data-status'))\n" +
                        "    }\n" +
                        "}"
            }
            url.contains("le.com") -> {
                js = "var bt = document.getElementsByClassName(\"$className\")\n" +
                        "if (bt[0] != null && bt[0] != undefined) {\n" +
                        "    bt[0].addEventListener(\"click\", function() {\n" +
                        "        Android.leshicalled()\n" +
                        "  },false)" +
                        "}"
            }
            url.contains("bilibili.com") -> {
                js = "var bt = document.getElementsByClassName(\"$className\")\n" +
                        "if (bt[0] != null && bt[0] != undefined) {\n" +
                        "    bt[0].onclick = function () {\n" +
                        "        Android.bilibilicalled()\n" +
                        "    }\n" +
                        "}"
            }
        }
        return js
    }

    /**
     * 判断当前播放地址是否包含在可解析地址中
     * @return 为可解析地址返回true，否则返回false
     */
    fun isJiexiUrl(url: String, context: Context): Boolean {
        if (url.isEmpty()) {
            return false
        }
        //https://yun.linhut.cn/index.php?url=http://www.iqiyi.com/v_19rrj5mdb8.html?vfm=f_191_360y
        if (jxurls.isNullOrEmpty()) {
            jxurls = context.resources.getStringArray(R.array.jxurl)
        }
        jxurls?.let {
            it.forEach { s ->
                if (url.contains(s)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 获取程序版本号
     */
    fun getAppVersionCode(context: Context): Int {
        var versionCode = -1
        try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, 0)
            versionCode = pi.versionCode
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return versionCode
    }

    /**
     * 获取程序版本名称
     */
    fun getAppVersionName(context: Context): String {
        var versionName = ""
        try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, 0)
            versionName = pi.versionName
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return versionName
    }



/*    fun setMenuBackgroundColor(activity: Activity) {
        activity.layoutInflater.factory = object : LayoutInflater.Factory {
            @SuppressLint("ResourceAsColor")
            override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
                if (name.equals("com.android.internal.view.menu.IconMenuItemView", true) ||
                    name.equals("com.android.internal.view.menu.ActionMenuItemView", true)
                ) {
                    try {
                        val inflater = activity.layoutInflater
                        val view = inflater.createView(name, null, attrs)
                        if (view is TextView) {
                            Handler().post {
                                view.setTextColor(R.color.text_blue)
                            }
                        }
                        return view
                    } catch (e: InflateException) {
                        e.printStackTrace()
                    } catch (e: ClassNotFoundException) {
                        e.printStackTrace()
                    }
                }
                return null
            }
        }
    }*/
    /**
     * 获取格式化日期
     * @return 2022-03-28 11:00:05
     */
    fun getFormatDate(): String {
        val simple = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return simple.format(System.currentTimeMillis())
    }
}