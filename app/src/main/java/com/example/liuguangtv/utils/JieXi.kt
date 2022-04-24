package com.example.liuguangtv.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.liuguangtv.R
import com.google.gson.Gson
import com.rxjava.rxlife.life
import rxhttp.RxHttp

class JieXi(val urls: MutableList<JSONInterface>) {
    companion object {
        private var jieXi: JieXi? = null
        fun build(context: Context): JieXi {
            return jieXi ?: kotlin.synchronized(this) {
                val lists = buildJieXiUrls(context)
                val instance = JieXi(lists)
                jieXi = instance
                instance
            }
        }

        private fun buildJieXiUrls(context: Context): MutableList<JSONInterface> {
            var result: MutableList<JSONInterface> = mutableListOf()
            RxHttp.get(Utils.HOME + "jiexi.txt")
                .setSync() //同步执行
                .asList(JSONInterface::class.java)
                .life(context as AppCompatActivity)
                .subscribe({
                    //urls = it
                    result = buildJiexiName(context, it)
                }, {
                    val str =
                        Utils.getFileStreamtoString(context.assets.open("JiexiJiekou.txt"))
                    val gson = Gson()
                    result = gson.fromJson(str, Array<JSONInterface>::class.java).toMutableList()
                    result = buildJiexiName(context, result)
                })
            return result
        }

        private fun buildJiexiName(
            context: Context,
            jsonInterface: MutableList<JSONInterface>
        ): MutableList<JSONInterface> {
            var i = 0
            jsonInterface.forEach {
                ++i
                it.setName(context.getString(R.string.jiexijiekou) + i)
            }
            return jsonInterface
        }
    }


    fun getJieKouByName(s: String): String {
        urls.forEach {
            if (it.getName() == s) {
                return it.getUrl()
            }
        }
        return ""
    }


    /**
     * 根据接口序号返回解析地址
     * @param index  ---从0开始
     */
    fun getJieKouByIndex(index: Int): String {
        return urls[index].getUrl()
    }

    /**
     * 判断一个网址是否是解析接口网址
     */
    fun isJieKouUrl(url: String): Boolean {
        urls.forEach {
            if (url.startsWith(it.getUrl())) {
                return true
            }
        }
        return false
    }

    /**
     * 返回所有解析地址
     */
    fun getJieXiUrls(): MutableList<String> {
        val array: MutableList<String> = mutableListOf()
        urls.forEach {
            array.add(it.getUrl())
        }
        return array
    }

    /**
     * 返回所有解析名称数组
     */
    fun getJieXiTitles(): MutableList<String> {
        val array: MutableList<String> = mutableListOf()
        urls.forEach {
            array.add(it.getName())
        }
        return array
    }
}
