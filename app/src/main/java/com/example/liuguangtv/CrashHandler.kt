package com.example.liuguangtv

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import com.example.liuguangtv.utils.Utils
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

@SuppressLint("SimpleDateFormat")
class CrashHandler
/** 保证只有一个CrashHandler实例  */
private constructor() : Thread.UncaughtExceptionHandler {
    // 系统默认的UncaughtException处理类
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    private lateinit var mContext: Context

    // 用来存储设备信息和异常信息
    private val infos: MutableMap<String, String> = HashMap()

    // 用于格式化日期,作为日志文件名的一部分
    private val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd")

    /**
     * 初始化
     *
     * @param context
     */
    fun init(context: Context) {
        mContext = context
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
        autoClear(5)
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler!!.uncaughtException(thread, ex)
        } else {
            SystemClock.sleep(3000)
            // 退出程序
            Process.killProcess(Process.myPid())
            exitProcess(1)
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息; 否则返回false.
     */
    private fun handleException(ex: Throwable?): Boolean {
        if (ex == null) return false
        try {
            // 使用Toast来显示异常信息
            object : Thread() {
                override fun run() {
                    Looper.prepare()
                    Utils.showToast(mContext, R.string.exceptionMsg)
                    Looper.loop()
                }
            }.start()
            // 收集设备参数信息
            collectDeviceInfo(mContext)
            // 保存日志文件
            saveCrashInfoFile(ex)
            SystemClock.sleep(3000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    fun collectDeviceInfo(ctx: Context?) {
        try {
            val pm = ctx!!.packageManager
            val pi = pm.getPackageInfo(
                ctx.packageName,
                PackageManager.GET_ACTIVITIES
            )
            if (pi != null) {
                val versionName = pi.versionName + ""
                val versionCode = pi.versionCode.toString() + ""
                infos["版本名称"] = versionName
                infos["版本号"] = versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "an error occured when collect package info", e)
        }
        val fields = Build::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                infos[field.name] = field[null].toString()
            } catch (e: Exception) {
                Log.e(TAG, "an error occured when collect crash info", e)
            }
        }
    }

    /**
     * 保存错误信息到文件中
     * @param ex
     * @return 返回文件名称,便于将文件传送到服务器
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun saveCrashInfoFile(ex: Throwable): String? {
        val sb = StringBuffer()
        try {
            val sDateFormat = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss"
            )
            val date = sDateFormat.format(Date())
            sb.append(
                """
                     
                     $date
                     
                     """.trimIndent()
            )
            for ((key, value) in infos) {
                sb.append("$key=$value\n")
            }
            val writer: Writer = StringWriter()
            val printWriter = PrintWriter(writer)
            ex.printStackTrace(printWriter)
            var cause = ex.cause
            while (cause != null) {
                cause.printStackTrace(printWriter)
                cause = cause.cause
            }
            printWriter.flush()
            printWriter.close()
            val result = writer.toString()
            sb.append(result)
            return writeFile(sb.toString())
        } catch (e: Exception) {
            Log.e(
                TAG,
                "an error occured while writing file...",
                e
            )
            sb.append("an error occured while writing file...\r\n")
            writeFile(sb.toString())
        }
        return null
    }

    @Throws(Exception::class)
    private fun writeFile(sb: String): String {
        val time = formatter.format(Date())
        val fileName = mContext.cacheDir.toString() + "/crash-$time.log"
        saveFile(fileName, sb) //保存日志
        /*val verCode: Int = Utils.getAppVersionCode(mContext)
        val verName: String = Utils.getAppVersionName(mContext)
        val QueryStringName = "?action=seterror"
        val params: MutableMap<String, String> = HashMap()
        params["error"] u= sb
        params["verCode"] = verCode.toString() + ""
        params["verName"] = verName*/
        //上传
        /*val Url: String = Config.URL_SetPhone.toString() + QueryStringName
        Log.i("SetError URL ", Url)
        val aaa: String = URLX.submitPostData(Url, params, "utf-8")*/
        //Log.i("SetError", aaa)
        /*  RxHttp.postForm(Utils.HOME+"seterror.php")
              .addFile("file",fileName)
              .asString()
              .subscribe()*/
        return fileName
    }

    /**
     * 保存文件
     */
    private fun saveFile(fileName: String, sb: String) {
        var writer: BufferedWriter? = null
        try {
            val out = FileOutputStream(fileName)
            writer = BufferedWriter(OutputStreamWriter(out))
            writer.write(sb)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (writer != null) {
                try {
                    writer.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 文件删除
     * @param day 文件保存天数
     */
    private fun autoClear(autoClearDay: Int) {
//       FileUtil.delete(getGlobalpath(), new FilenameFilter() {
//
//           @Override
//           public boolean accept(File file, String filename) {
//               String s = FileUtil.getFileNameWithoutExtension(filename);
//               int day = autoClearDay < 0 ? autoClearDay : -1 * autoClearDay;
//               String date = "crash-" + DateUtil.getOtherDay(day);
//               return date.compareTo(s) >= 0;
//           }
//       });
    }


    companion object {
        var TAG = "MyCrash"

        /** 获取CrashHandler实例 ,单例模式  */
        @SuppressLint("StaticFieldLeak")
        val instance = CrashHandler()
        val globalpath: String
            get() = (Environment.getExternalStorageDirectory().absolutePath
                    + File.separator + "crash" + File.separator)

        fun setTag(tag: String) {
            TAG = tag
        }
    }
}