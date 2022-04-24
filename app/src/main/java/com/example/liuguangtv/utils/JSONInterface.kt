package com.example.liuguangtv.utils

class JSONInterface {

    private var name: String = ""
    private var category: String = ""
    private var url: String = ""
    fun setName(name: String) {
        this.name = name
    }

    fun getName(): String {
        return name
    }

    fun setCategory(category: String) {
        this.category = category
    }

    fun getCategory(): String {
        return category
    }

    fun setUrl(url: String) {
        this.url = url
    }

    fun getUrl(): String {
        return url
    }
}