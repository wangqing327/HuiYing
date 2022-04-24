package com.example.liuguangtv.utils

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import rxhttp.RxHttp

class DownLoadThread(val fileUrl: String) {
    fun createTask(){
        val result = RxHttp.get(fileUrl)
            .asAppendDownload("data/data/video.mp4",AndroidSchedulers.mainThread()) {
                val downsize = it.currentSize //已下载大小
                val countsize = it.totalSize //总大小
                val progress = it.progress  //下载进度 0-100
            }


    }
    fun getFileSize(): String {
        return CacheClear.getFormatSize(1000)
    }
}