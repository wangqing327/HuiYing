package com.example.liuguangtv.utils.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.liuguangtv.utils.Utils
import org.jetbrains.annotations.NotNull
import java.io.Serializable

@Entity(tableName = "history")
class History(

    /**
     * 主键ID
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    /**
     * 列表标题
     */
    @NotNull
    @ColumnInfo var title: String,
    /**
     * 网站链接
     */
    @ColumnInfo
    @NotNull val url: String,
    /**
     * 加入时间
     */
    @NotNull
    @ColumnInfo
    val time: String,
    /**
     * 时间戳
     */
    @NotNull
    @ColumnInfo val stamp: Long,
    /**
     * 类型 0:历史记录 1:我的收藏
     */
    @NotNull
    @ColumnInfo
    var tag: Int
): Serializable {
    @Ignore
    var checked: Boolean = false

    @Ignore
    constructor(id: Int, title: String, url: String, time: String, stamp: Long)
            : this(id, title, url, time, stamp, 0)

    @Ignore
    constructor(title: String, url: String)
            : this(0, title, url)

    @Ignore
    constructor(id: Int, title: String, url: String)
            : this(id, title, url, 0)

    @Ignore
    constructor(id: Int, title: String, url: String, tag: Int)
            : this(id, title, url, Utils.getFormatDate(),
        System.currentTimeMillis() / 1000, tag
    )

    @Ignore
    constructor(title: String, url: String, tag: Int)
            : this(0, title, url, tag)
}