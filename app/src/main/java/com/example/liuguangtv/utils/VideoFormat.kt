package com.example.liuguangtv.utils

/**
 * 视频相关处理
 *
 */
object VideoFormat {
    val videoTypeList: Map<String, MutableList<String>> = mutableMapOf(
        "m3u8" to mutableListOf(
            "application/vnd.apple.mpegurl",
            "application/mpegurl",
            "application/x-mpegurl",
            "audio/mpegurl",
            "audio/x-mpegurl",
            "application/octet-stream"
        ),
        "video/mp4" to mutableListOf("application/mp4", "video/h264"),
        "flv" to mutableListOf("video/x-flv"),
        "f4v" to mutableListOf("video/x-f4v"),
        "mpeg" to mutableListOf("video/vnd.mpegurl"),
        "html" to mutableListOf("text/html;charset=utf-8")
    )

    //检测Content-Type 是否是视频链接
    fun isVideoLink(contentType: String?): Boolean {
        if (contentType.isNullOrEmpty()) {
            return false
        }
        //去除所有空格
        val contentType2 = contentType.replace(" ", "")
        videoTypeList.forEach {
            it.value.forEach { type ->
                if (type.equals(contentType2, true)) {
                    return true
                }
            }
        }
        return false
    }
}