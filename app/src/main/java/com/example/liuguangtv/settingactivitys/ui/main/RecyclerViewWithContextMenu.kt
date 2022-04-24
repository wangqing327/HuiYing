package com.example.liuguangtv.settingactivitys.ui.main

import android.content.Context
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.View
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewWithContextMenu : RecyclerView {
    private val mContextInfo = RecyclerViewContextInfo()

    constructor(context: Context) : super(context)

    constructor(context: Context, @Nullable attrs: AttributeSet?) : super(context, attrs)

    constructor(
        context: Context,
        @Nullable attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle)


    override fun showContextMenuForChild(originalView: View): Boolean {
        getPositionByChild(originalView)
        return super.showContextMenuForChild(originalView)
    }

    override fun showContextMenuForChild(originalView: View, x: Float, y: Float): Boolean {
        getPositionByChild(originalView)
        return super.showContextMenuForChild(originalView, x, y)
    }

    /**
     * 记录当前RecyclerView中Item上下文菜单的Position
     * @param originalView originalView
     */
    private fun getPositionByChild(originalView: View) {
        val layoutManager = layoutManager
        if (layoutManager != null) {
            val position = layoutManager.getPosition(originalView)
            mContextInfo.setPosition(position)
        }
    }

    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo? {
        return mContextInfo
    }

    class RecyclerViewContextInfo : ContextMenu.ContextMenuInfo {
        var position = -1
            private set

        fun setPosition(position: Int): Int {
            return position.also { this.position = it }
        }
    }

}