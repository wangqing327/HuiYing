package com.example.liuguangtv.utils.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "myTable")
data class MyDatabaseTable(

    /**
     * 主键ID
     */
    @PrimaryKey(autoGenerate = true)
    var id: Int,
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
     * 图标网络路径
     */
    @ColumnInfo val icoUrl: String?,
    /**
     * 图标标题,就一个字
     */
    @ColumnInfo val icoTitle: String?,
    /**
     * 排序序号
     */
    @ColumnInfo var zIndex: Int
) {
    constructor(
        title: String,
        url: String,
        icoUrl: String?,
        icoTitle: String?
    ) : this(
        0,
        title,
        url,
        icoUrl,
        icoTitle,
        0
    )

    constructor(title: String, url: String, icoUrl: String?) : this(
        title,
        url,
        icoUrl,
        null
    )

    constructor(title: String, url: String) : this(
        title,
        url,
        null
    )

    constructor(id: Int, title: String, url: String, icoUrl: String?) : this(
        id, title, url, icoUrl, null, 0
    )
}
