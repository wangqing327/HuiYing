package com.example.liuguangtv.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.liuguangtv.R

/**
 * RecyclerView分割线
 * @param context 环境
 * @param dividerHeight 分割线高度
 * @param divider 分割线Drawable
 * @param marginLeft 距离左边外边距
 * @param marginRight 距离右边外边距
 * @param lastIsShow 末行是否绘制分隔线
 * @param pxTodp 是否进行将marginLeft和marginRight转换为dp像素
 */
class SimpleDividerItemDecoration(
    private val context: Context,
    private val dividerHeight: Int,
    private val divider: Drawable,
    private val marginLeft: Int,
    private val marginRight: Int,
    private val lastIsShow: Boolean,
    private val pxTodp: Boolean
) :
    RecyclerView.ItemDecoration() {

    /**
     * RecyclerView分割线
     * @param context 环境
     * @param dividerHeight 分割线高度或宽度
     * @param margin 左边和右边的宽度
     * @param lastIsShow 是否绘制最后一行的分隔线
     * @param pxTodp 是否将margin转换为dp值
     * @see SimpleDividerItemDecoration
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    constructor(
        context: Context,
        dividerHeight: Int,
        margin: Int,
        lastIsShow: Boolean,
        pxTodp: Boolean
    )
            : this(
        context, dividerHeight,
        context.getDrawable(R.drawable.itemdecoration)!!,
        if (pxTodp) Utils.px2dip(context, margin.toFloat()) else margin,
        if (pxTodp) Utils.px2dip(context, margin.toFloat()) else margin,
        lastIsShow, pxTodp
    )

    /**
     * RecyclerView分割线
     * @param context 环境
     * @param dividerHeight 分割线高度或宽度
     * @param marginDp 左边和右边的宽度,值默认是以dp度量
     * @param lastIsShow 是否绘制最后一行的分隔线
     * @see SimpleDividerItemDecoration
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    constructor(
        context: Context,
        dividerHeight: Int,
        marginDp: Int,
        lastIsShow: Boolean
    )
            : this(
        context,
        dividerHeight,
        Utils.dip2px(context, marginDp.toFloat()),
        lastIsShow, false
    )

    /**
     * RecyclerView分割线 不绘制取后一行的分隔线
     * @param context 环境
     * @param dividerHeight 分割线高度或宽度
     * @param marginDp 左边和右边的宽度,值默认是以dp度量
     * @see SimpleDividerItemDecoration
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    constructor(
        context: Context,
        dividerHeight: Int,
        marginDp: Int
    )
            : this(
        context,
        dividerHeight,
        marginDp,
        false
    )

    /**
     * RecyclerView分割线 不绘制取后一行的分隔线,距离左右边界默认10Px
     * @param context 环境
     * @param dividerHeight 分割线高度或宽度
     * @see SimpleDividerItemDecoration
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    constructor(
        context: Context,
        dividerHeight: Int
    )
            : this(
        context,
        dividerHeight,
        10, false, false
    )

    /**
     * RecyclerView分割线 不绘制取后一行的分隔线,默认分隔线3像素高
     * @param context 环境
     * @see SimpleDividerItemDecoration
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    constructor(context: Context) : this(context, 2)


    /**
     * 获取分割线尺寸
     */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(0, 0, 0, dividerHeight)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        //super.onDrawOver(c, parent, state)
        val childCount = parent.childCount
        if (childCount < 1) {
            return
        }
        val left = parent.paddingLeft + marginLeft
        val right = parent.width - parent.paddingRight - marginRight
        for (i in 0 until childCount) {
            if (!lastIsShow) {
                if (i == childCount - 1)
                    return
            }
            val child = parent.getChildAt(i)
            val parms = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + parms.bottomMargin
            val bottom = top + dividerHeight
            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }

}