package com.example.liuguangtv.settingactivitys.ui.main

import android.annotation.SuppressLint
import android.view.View
import android.widget.CheckBox
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.liuguangtv.R
import com.example.liuguangtv.utils.database.History

/**
 * 记录与收藏页面适配器
 */
class AdapterJl(layoutResId: Int) :
    BaseItemDraggableAdapter<History, BaseViewHolder>(layoutResId, null) {
    /**
     * 当前视图checkBox是否可显示
     */
    private var isVisible = false

    /**
     * 是否全选
     */
    private var allSelect = false

    @SuppressLint("ResourceType")
    override fun convert(helper: BaseViewHolder, item: History) {
        helper.setText(R.id.title, item.title)
        helper.setText(R.id.url, item.url)
        helper.setText(R.id.date, item.time)
        val checkBox = helper.getView<CheckBox>(R.id.delete)
        if (allSelect) {
            checkBox.visibility = View.VISIBLE
            checkBox.isChecked = item.checked
        } else {
            checkBox.visibility = View.GONE
        }
        helper.addOnClickListener(R.id.delete)
    }

    /**
     * 设置checkbox是否选中
     * @param position
     * @param checked 是否选中
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setItemChecked(position: Int, checked: Boolean) {
        data[position].checked = checked
        notifyItemChanged(position)
    }

    /**
     * 改变checkbox选中状态
     * @param position
     */
    fun changeItemChecked(position: Int) {
        data[position].checked = data[position].checked.not()
        notifyItemChanged(position)
    }

    /**
     * 改变全部checkbox的选中状态,全选OR全不选
     */
    @SuppressLint("NotifyDataSetChanged")
    fun changeAllCheckboxSelected() {
        if (!allSelect) {
            return
        }
        allSelect = allSelect.not()
        data.forEach {
            it.checked = allSelect
        }
        notifyDataSetChanged()
    }

    /**
     * 将全部项目设为选中
     */
    fun setAllItemChecked() {
        isVisible = true
        data.forEach {
            it.checked = isVisible
        }
        notifyDataSetChanged()
    }

    /**
     * 将全部项目设为未选中状态
     */
    fun setAllItemNotChecked() {
        isVisible = false
        data.forEach {
            it.checked = isVisible
        }
        notifyDataSetChanged()
    }

    /**
     * 取当前表项是否选中
     */
    fun getItemIsChecked(position: Int): Boolean {
        return data[position].checked
    }

    /**
     * 将全部项目设为编辑模式，即显示前面的选择框,并初始化为全部不选中状态
     */
    fun setEditMode() {
        changeEditMode1(true)
    }

    private fun changeEditMode1(edit: Boolean) {
        if (allSelect == edit) {
            return
        }
        allSelect = edit
        data.forEach {
            it.checked = false
        }
        notifyDataSetChanged()
    }

    /**
     * 取消全部项目的编辑状态
     */
    fun unEditMode() {
        changeEditMode1(false)
    }

    /**
     * 改变全部项目是否处于编辑模式
     */
    fun changeEditMode(edit: Boolean) {
        changeEditMode1(edit)
    }

    /**
     * 取项目中所有已选择的项目集合
     */
    fun getAllCheckedItems(): MutableList<History> {
        val lists = mutableListOf<History>()
        data.forEach {
            if (it.checked) {
                lists.add(it)
            }
        }
        return lists
    }

    /**
     * 是否有选中项目，有一个处于选中状态就够了，主要用于Toast提示
     */
    fun isChecked(): Boolean {
        data.forEach {
            if (it.checked) {
                return true
            }
        }
        return false
    }

    /**
     * 清除所有选中的项目
     */
    fun removeAllSelectedItem() {
        val list = getAllCheckedItems()
        data.removeAll(list)
        notifyDataSetChanged()
    }
}