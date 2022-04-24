package com.example.liuguangtv

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.liuguangtv.utils.database.History

/**
 * APP广播，用于将历史记录中的数据添加至首页
 */
class AppBroadcast : BroadcastReceiver() {
    private var receiveData: ReceivedData? = null

    companion object {
        const val BroadCastString = "MY BROADCAST STRING"
        const val DownLoadFileInfo = "MY DOWNLOAD FILE INFO"
    }

    override fun onReceive(p0: Context, p1: Intent) {
        when (p1.action) {
            BroadCastString -> {
                p1.getBundleExtra("bundle")?.apply {
                    getSerializable("history")?.apply {
                        receiveData?.receiveData(this as History)
                    }
                }
            }
            DownLoadFileInfo -> {

            }
        }
    }

    interface ReceivedData {
        fun receiveData(history: History)
    }

    fun setReceiveData(receivedData: ReceivedData) {
        this.receiveData = receivedData
    }
}