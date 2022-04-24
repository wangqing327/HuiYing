package com.example.liuguangtv.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.liuguangtv.R
import com.rxjava.rxlife.life
import rxhttp.RxHttp

class Adblock {

    companion object {
        private const val default = "default"

        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context

        @SuppressLint("StaticFieldLeak")
        private var _instance: Adblock? = null

        /**
         * 广告过滤库
         */
        private var adburls: MutableMap<String, MutableList<String>> = mutableMapOf()

        /**
         * 元素操作库
         */
        private var elementJs: MutableMap<String, MutableList<String>> = mutableMapOf()

        /**
         * 广告库初始化，不然会报错
         */
        @JvmStatic
        fun getInstance(context: Context): Adblock {
            return _instance ?: synchronized(this) {
                val instance = Adblock()
                this.context = context
                buildAdurls(context)
                _instance = instance
                instance
            }
        }

        private fun buildAdurls(context: Context) {
            RxHttp.get(Utils.HOME + "adblock.txt")
                .setSync()
                .asList(String::class.java)
                .life(context as AppCompatActivity)
                .subscribe({
                    buildAdMaps(it)
                }, {
                    //网络连接失败，从本地载入广告链接过滤地址
                    buildAdMaps(
                        context.resources.getStringArray(R.array.adBlockUrls)
                            .toMutableList()
                    )
                })
        }

        /**
         * 解析广告地址或元素对象
         */
        private fun buildAdMaps(list: MutableList<String>) {
            try {
                list.forEach {
                    if (it.startsWith("##")) {
                        buildElementMaps(it)
                        return@forEach
                    }
                    val adb = it.split("\$domain=")
                    if (adb.size == 2) {
                        if (adburls[adb[1]] == null) {
                            adburls[adb[1]] = mutableListOf()
                        }
                        if (adb[1].indexOf("|") > 0) {
                            val domain = adb[1].split("|")
                            domain.forEach { w ->
                                if (adburls[w] == null) {
                                    adburls[w] = mutableListOf()
                                }
                                if (w.isNotEmpty()) {
                                    adburls[w]?.add(adb[0])
                                }
                            }
                        } else {
                            adburls[adb[1]]?.add(adb[0])
                        }
                    } else {
                        //如果没有指定domain，将此地址默认加入到default中
                        if (adburls[default] == null) {
                            adburls[default] = mutableListOf()
                        }
                        adburls[default]?.add(adb[0])
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * 构建隐藏指定元素的规则列表，欲处理数据前两个字符为##
         */
        private fun buildElementMaps(element: String) {
            val adb = element.split("\$domain=")
            if (adb.size == 2) {
                if (elementJs[adb[1]] == null) {
                    elementJs[adb[1]] = mutableListOf()
                }
                if (adb[1].indexOf("|") > 0) {
                    val domain = adb[1].split("|")
                    domain.forEach { w ->
                        if (elementJs[w] == null) {
                            elementJs[w] = mutableListOf()
                        }
                        if (w.isNotEmpty()) {
                            elementJs[w]?.add(adb[0])
                        }
                    }
                } else {
                    elementJs[adb[1]]?.add(adb[0])
                }
            } else {
                //如果没有指定domain，将此地址默认加入到default中
                if (elementJs[default] == null) {
                    elementJs[default] = mutableListOf()
                }
                elementJs[default]?.add(adb[0])
            }
        }

        /**
         * 是否广告链接
         * @param webUrl 网页链接，地址栏中的链接
         * @param sourceUrl 资源链接，请求欲加载的资源链接
         * @return true-广告链接 false-非广告链接
         */
        @JvmStatic
        fun isHad(webUrl: String?, sourceUrl: String): Boolean {
            if (!webUrl.isNullOrEmpty()) {
                adburls.forEach {
                    if (webUrl.contains(it.key)) {
                        it.value.forEach { s ->
                            if (ppurl(sourceUrl, s)) {
                                return true
                            }
                        }
                    }
                }
            } else {
                adburls.forEach {
                    if(it.key != default){
                        it.value.forEach { value->
                            if(ppurl(sourceUrl,value)){
                                return true
                            }
                         }
                    }
                }
            }
            adburls[default]?.forEach {
                if (ppurl(sourceUrl, it)) {
                    return true
                }
            }
            return false
        }

        /**
         * 匹配url，根据情况选择正则或普通寻找文本
         * @param sourceUrl 资源链接
         * @param adbUrl 广告链接
         */
        @JvmStatic
        private fun ppurl(sourceUrl: String, adbUrl: String): Boolean {
            var v2 = adbUrl
            var rreg = false  //是否启用正则匹配
            //如果有通配符，将*转换为正则通配符
            if (adbUrl.indexOf("*") != -1) {
                v2 = v2.replace(".", "\\.") //将.替换为正则的\\.
                v2 = v2.replace("*", ".*?")
                rreg = true
            }
            return if (rreg) {
                sourceUrl.contains(Regex(v2))
            } else {
                sourceUrl.contains(v2)
            }
        }

        /**
         * 根据webUrl地址返回JS文件，主要用于屏蔽或隐藏元素
         * @param webUrl host
         */
        @JvmStatic
        fun returnJs(webUrl: String): String {
            var els: MutableList<String>? = null
            elementJs.forEach {
                if (webUrl.contains(it.key)) {
                    els = elementJs[it.key]
                    return@forEach
                }
            }
            val line: MutableList<String> = mutableListOf()
            els?.forEach {
                line.add(it.replace("##", ""))
            }
            els = elementJs[default]
            els?.forEach {
                line.add(it.replace("##", ""))
            }
            if (line.size == 0) {
                return ""
            }
            var js = "let a = ["
            line.forEach {
                js += "'$it',"
            }
            js = js.substring(0, js.length - 1) + "]\nrunjs()\n"
            js += Utils.getFileStreamtoString(
                Utils.openAssetsFile(context, "adblock.js")
            )
            return js
        }
    }
}