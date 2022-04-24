package com.example.liuguangtv.utils

import android.graphics.Color
import constant.UiType
import model.UiConfig
import model.UpdateConfig
import update.UpdateAppUtils

object Updates {
    /**
     * 保存路径
     */
    //private val p = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
    /*private val path = Environment.getExternalStorageDirectory().absolutePath + "/YingHuiCache"

    *//**
     * 保存文件名
     *//*
    private const val apkName = "new.apk"*/

    fun build(apkUrl: String,isForce: Boolean,content:String,verCode:Int,verName:String) {
        UpdateAppUtils
            .getInstance()
            .apkUrl(apkUrl)
            .updateConfig(buildUpdateConfig(isForce,verName,verCode))
            .uiConfig(buildUIConfig())
            .updateContent(content)
 /*           .setUpdateDownloadListener(object : UpdateDownloadListener {
                // do something
                override fun onDownload(progress: Int) {

                }

                override fun onError(e: Throwable) {

                }

                override fun onFinish() {

                }

                override fun onStart() {

                }
            })*/
            .update()
    }

    private fun buildUIConfig(): UiConfig {
        // ui配置
        val uiConfig = UiConfig().apply {
            uiType = UiType.PLENTIFUL
            cancelBtnText = "下次再说"
            titleTextColor = Color.BLACK
            titleTextSize = 18f
            contentTextColor = Color.parseColor("#88e16531")
        }
        return uiConfig
    }

    private fun buildUpdateConfig(isForce: Boolean,verName: String,verCode: Int): UpdateConfig {
        // 更新配置
        val updateConfig = UpdateConfig().apply {
            isDebug = false //输出调试信息
            force = isForce //强制更新
            checkWifi = false //检查wifi
            needCheckMd5 = false //检查md5
            isShowNotification = true
            alwaysShowDownLoadDialog = true
            showDownloadingToast = false
            serverVersionCode = verCode
            serverVersionName = verName
            //设置下面两项，会下载失败
            /*apkSavePath = path
            apkSaveName = apkName*/
        }
        return updateConfig
    }
}