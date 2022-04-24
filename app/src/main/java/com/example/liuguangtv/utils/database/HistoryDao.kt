package com.example.liuguangtv.utils.database

import androidx.room.*

/**
 * 历史记录 收藏记录Dao
 */
@Dao
interface HistoryDao {
    /**
     * 历史记录和收藏数据 取全部数据
     * @param tag 数据类型 0:播放记录 1:我的收藏
     * @return LiveData<MutableList<History>>
     */
    @Query("SELECT * FROM history WHERE tag = :tag ORDER BY stamp DESC")
    fun queryAllData(tag:Int): MutableList<History>

    @Query("SELECT id FROM history WHERE url = :url AND tag = :tag")
    fun queryDataByUrl(url:String,tag: Int): Int
    /**
     * 更新数据
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateData(data: List<History>)

    /**
     * 插入数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(vararg data: History)

    /**
     * 插入数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(data: History)
    /**
     * 插入数据
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertData(data: List<History>)

    /**
     * 按zIndex的值删除数据
     */
    @Query("DELETE FROM history WHERE id = :id")
    fun deleteDataByID(id: Int): Int

    /**
     * 删除项目
     */
    @Delete
    fun deleteData(data: History): Int

    /**
     * 删除所有数据
     */
    @Query("DELETE FROM history")
    fun deleteAllData(): Int

    /**
     * 返回表中的数据总量
     */
    @Query("SELECT COUNT(*) FROM history")
    fun getDataCount(): Int
}