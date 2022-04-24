package com.example.liuguangtv.utils.database

import androidx.room.*

/**
 * 首页TableDao
 */
@Dao
interface ItemTableDao {
    /**
     * 首页列表数据 取全部数据
     */
    @Query("SELECT * FROM myTable ORDER BY zIndex")
    fun queryItemAllData(): List<MyDatabaseTable>

    @Query("SELECT id FROM myTable WHERE url = :url")
    fun queryItemByUrl(url:String):Int
    /**
     * 首页列表数据 更新数据
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateItemData(data: List<MyDatabaseTable>)

    /**
     * @param url 依据此值修改title
     * @param title 显示的标题新值
     */
    @Query("UPDATE myTable SET title = :title WHERE url = :url")
    fun updateItemDataByUrl(url: String,title:String)

    /**
     * @param id 依据此值修改title
     * @param title 显示的标题新值
     */
    @Query("UPDATE myTable SET title = :title WHERE id = :id")
    fun updateItemDataById(id: Int,title:String)

    /**
     * 首页列表数据 插入数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItemData(vararg data: MyDatabaseTable)
    /**
     * 首页列表数据 插入数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItemData(data: List<MyDatabaseTable>)

    /**
     * 按zIndex的值删除数据
     */
    @Query("DELETE FROM myTable WHERE id = :id")
    fun deleteItemDataByID(id: Int): Int

    @Query("DELETE FROM myTable WHERE url = :url")
    fun deleteItemDataByUrl(url:String)
    /**
     * 按zIndex的值删除数据
     */
    @Delete
    fun deleteItemData(data: MyDatabaseTable): Int

    /**
     * 删除所有数据
     */
    @Query("DELETE FROM myTable")
    fun deleteItemAllData(): Int

    /**
     * 返回表中的数据总量
     */
    @Query("SELECT COUNT(*) FROM myTable")
    fun getItemDataCount(): Int




}