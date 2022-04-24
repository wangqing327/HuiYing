package com.example.liuguangtv.utils

class UpdateJson {
    private var versionName: String =""
    private var versionCode = 0
    private var isFore = false
    private var updateContent: String = ""
    private var apkUrl: String = ""
    fun setVersionName(versionName: String) {
        this.versionName = versionName
    }

    fun getVersionName(): String {
        return versionName
    }

    fun setVersionCode(versionCode: Int) {
        this.versionCode = versionCode
    }

    fun getVersionCode(): Int {
        return versionCode
    }

    fun setIsFore(isFore: Boolean) {
        this.isFore = isFore
    }

    fun getIsFore(): Boolean {
        return isFore
    }

    fun setUpdateContent(updateContent: String) {
        this.updateContent = updateContent
    }

    fun getUpdateContent(): String {
        return updateContent
    }

    fun setApkUrl(apkUrl: String) {
        this.apkUrl = apkUrl
    }

    fun getApkUrl(): String {
        return apkUrl
    }
}