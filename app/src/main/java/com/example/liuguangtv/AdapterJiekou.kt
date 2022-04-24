package com.example.liuguangtv

import android.graphics.Color
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class AdapterJiekou(layoutResId: Int, private val selected: Int) :
    BaseQuickAdapter<String, BaseViewHolder>(layoutResId) {
    override fun convert(helper: BaseViewHolder, item: String) {
        val tv = helper.getView<TextView>(R.id.tv)
        tv.text = item
        if (helper.layoutPosition == selected) {
            tv.setTextColor(Color.rgb(10, 10, 255))
        } else {
            tv.setTextColor(Color.rgb(41, 36, 38))
        }
    }
}