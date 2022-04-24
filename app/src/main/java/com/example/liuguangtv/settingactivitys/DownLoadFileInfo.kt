package com.example.liuguangtv.settingactivitys

import java.io.Serializable

data class DownLoadFileInfo(
    val name: String,
    val link: String,
    var checked: Boolean
) : Serializable {
    constructor(name: String, link: String) : this(name, link, false)
}

