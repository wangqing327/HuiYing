package com.example.liuguangtv.utils

/**
 * 嗅探到视频时的回调接口
 */
interface SniffVideoUrlCallBack {
    /**
     * 该函数可能回调多次，请注意排除重复地址
     * @param videoUrl 嗅探到的媒体网络地址
     */
    fun callback(videoUrl:String)
}