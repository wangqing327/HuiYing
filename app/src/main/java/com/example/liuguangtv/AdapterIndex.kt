package com.example.liuguangtv

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.liuguangtv.utils.MyApplication
import com.example.liuguangtv.utils.Utils
import com.example.liuguangtv.utils.database.MyDatabaseTable

class AdapterIndex(layoutResId: Int) :
    BaseItemDraggableAdapter<MyDatabaseTable, BaseViewHolder>(layoutResId, null) {

    var visible = false

    @Throws(StringIndexOutOfBoundsException::class)
    override fun convert(holder: BaseViewHolder, item: MyDatabaseTable) {
        val icon = holder.getView<ImageView>(R.id.icon)
        if (item.icoUrl.isNullOrEmpty() || "null" == item.icoUrl) {
            //图标地址等于空，不走Glide了，直接buildBitmap函数造一个出来
            if (item.icoTitle.isNullOrEmpty()) {
                if (item.title.isEmpty()) {
                    buildBitmap(item.url.substring(0, 1), icon)
                } else {
                    buildBitmap(item.title.substring(0, 1), icon)
                }
            } else {
                buildBitmap(item.icoTitle, icon)
            }
        } else {
            Glide.with(icon).load(item.icoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        //网络取图片失败
                        if (item.icoTitle.isNullOrEmpty()) {
                            if (item.title.isEmpty()) {
                                buildBitmap(item.url.substring(0, 1), icon)
                            } else {
                                buildBitmap(item.title.substring(0, 1), icon)
                            }
                        } else {
                            buildBitmap(item.icoTitle, icon)
                        }
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        //不要动，这样就对了
                        return false
                    }

                })
                .into(icon)
        }

        holder.setText(R.id.title, item.title)
        holder.addOnClickListener(R.id.ico_close)
        holder.setVisible(R.id.ico_close, visible)
    }

    /**
     * 生成Bitmap并显示到ImageView中
     */
    private fun buildBitmap(s: String, icon: ImageView) {
        val drawable =
            MyApplication.getContext().getDrawable(R.drawable.cardvewbackground)
        val bitmap =
            drawable?.let { Utils.drawableToBitmap(it, 40, 40) }
        val bm =
            bitmap?.let {
                Utils.writeText2Bitmap(
                    s,
                    it,
                    33f
                )
            }
        icon.setImageBitmap(bm)
    }

    /**
     * 设置删除按钮是否显示
     * @param visible true->显示 false->不显示
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setDeleteVisible(visible: Boolean) {
        visible.also { this.visible = it }
        notifyDataSetChanged()
    }
}