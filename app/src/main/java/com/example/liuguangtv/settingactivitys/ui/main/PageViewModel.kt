package com.example.liuguangtv.settingactivitys.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.liuguangtv.settingactivitys.DownLoadFileInfo
import com.example.liuguangtv.utils.database.History

class PageViewModel : ViewModel() {

    /**
     * 历史记录
     */
    val history = MutableLiveData<MutableList<History>>()

    fun setHistoryValue(list: MutableList<History>) {
        history.value = list
    }

    fun getHistoryValue(): MutableList<History>? {
        return history.value
    }

    /**
     * 收藏
     */
    val favorite = MutableLiveData<MutableList<History>>()

    fun setFavoriteValue(list: MutableList<History>) {
        favorite.value = list
    }

    fun getFavoriteValue(): MutableList<History>? {
        return favorite.value
    }

    /**
     * 将数据添加到收藏记录里面
     */
    fun addHistoryValue(h: History) {
        var historys = favorite.value
        if (historys == null) {
            historys = mutableListOf()
        }
        historys.add(h)
        favorite.postValue(historys)
    }

    /**
     * 视频链接
     */
    val videoLinks = MutableLiveData<MutableList<DownLoadFileInfo>>()
}