package com.example.liuguangtv.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * 配置文件操作类
 */
object ShareUtils {
    /**
     * 通用配置节点
     */
    private const val iniNode = "Settings"

    /**
     * 搜索引擎接口节点
     */
    const val searchJieKou = "searchJieKou"

    /**
     * 过滤广告数值统计
     */
    const val AdblockCount = "adblockCount"

    /**
     * 广告过滤提示状态 逻辑型
     */
    const val AdblockTipState = "adblockTipState"

    /**
     * 广告过滤器是否打开 布尔型，默认打开-->真
     */
    const val AdblockIsOpen = "adblockOpen"

    /**
     * 视频嗅探节点
     * 逻辑型，真则执行嗅探，假则不嗅探
     */
    const val VideoXt = "VideoXt"

    /**
     * 浏览器模式 true,手机模式 false,Pc模式
     */
    const val WebMode = "WebMode"

    private fun getShare(context: Context): SharedPreferences {
        return context.getSharedPreferences(iniNode, Context.MODE_PRIVATE)
    }


    /**
     * 写入整数类型的数据
     * @param context 上下文环境
     * @param node 节点名称
     * @param data 欲写入的数据
     */
    fun writeInt(context: Context, node: String, data: Int) {
        getShare(context).edit().putInt(node, data).apply()
    }

    /**
     * 将当前节点的数值+1并写入,并返回写入后的值
     * @param context
     * @param node 节点名称
     * @return 写入后的新值
     * @see intNodeAdd
     */
    fun intNodeAdd(context: Context, node: String): Int {
        return intNodeAdd(context, node, 1)
    }

    /**
     * 将当前节点的数值加上指定的数值并写入,并返回写入后的值
     * @param context
     * @param node 节点名称
     * @return 写入后的新值
     */
    fun intNodeAdd(context: Context, node: String, size: Int): Int {
        val i = getInt(context, node) + size
        writeInt(context, node, i)
        return i
    }

    /**
     * 返回浮点类型的数据
     * @param node 节点名称
     * @return 0.0,如果未查询到数据，将返回0
     */
    fun getInt(context: Context, node: String): Int {
        val share = getShare(context)
        return share.getInt(node, 0)
    }

    /**
     * 写入文本类型的数据
     * @param context 上下文环境
     * @param node 节点名称
     * @param data 欲写入的数据
     */
    fun writeString(context: Context, node: String, data: String) {
        val share = getShare(context)
        share.edit().putString(node, data).apply()
    }

    /**
     * 读取配置文件中的文本类型的数据
     * @param node 节点名称
     * @return 如果未查询到数据，将返回""
     */
    fun getString(context: Context, node: String): String? {
        val share = getShare(context)
        return share.getString(node, "")
    }

    /**
     * 写入布尔类型的数据
     * @param context 上下文环境
     * @param node 节点名称
     * @param data 欲写入的数据
     */
    fun writeBoolean(context: Context, node: String, data: Boolean) {
        val share = getShare(context)
        share.edit().putBoolean(node, data).apply()
    }

    /**
     * 返回布尔类型的数据
     * @param node 节点名称
     * @return 0.0,如果未查询到数据，将返回false
     */
    fun getBoolean(context: Context, node: String): Boolean {
        return getBoolean(context, node, false)
    }

    /**
     * 返回布尔类型的数据
     * @param node 节点名称
     * @return 如果未查询到数据，将返回default
     */
    fun getBoolean(context: Context, node: String, default: Boolean): Boolean {
        val share = getShare(context)
        return share.getBoolean(node, default)
    }

    /**
     * 写入浮点类型的数据
     * @param context 上下文环境
     * @param node 节点名称
     * @param data 欲写入的数据
     */
    fun writeFloat(context: Context, node: String, data: Float) {
        val share = getShare(context)
        share.edit().putFloat(node, data).apply()
    }

    /**
     * 返回浮点类型的数据
     * @param node 节点名称
     * @return 0.0,如果未查询到数据，将返回0f
     */
    fun getFloat(context: Context, node: String): Float {
        val share = getShare(context)
        return share.getFloat(node, 0f)
    }

    /**
     * 写入长整数类型的数据
     * @param context 上下文环境
     * @param node 节点名称
     * @param data 欲写入的数据
     */
    fun writeLong(context: Context, node: String, data: Long) {
        val share = getShare(context)
        share.edit().putLong(node, data).apply()
    }

    /**
     * 返回长整数类型的数据
     * @param node 节点名称
     * @return 0.0,如果未查询到数据，将返回0
     */
    fun getLong(context: Context, node: String): Long {
        val share = getShare(context)
        return share.getLong(node, 0L)
    }
}