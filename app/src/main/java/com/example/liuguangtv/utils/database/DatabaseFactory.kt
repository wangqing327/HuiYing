package com.example.liuguangtv.utils.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(version = 6, entities = [MyDatabaseTable::class, History::class], exportSchema = false)
abstract class DatabaseFactory : RoomDatabase() {
    companion object {
        private var INSTANCE: DatabaseFactory? = null
        private val DATABASE_NAME = "Database"
        fun getDataBase(context: Context): DatabaseFactory {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        private fun buildDatabase(context: Context): DatabaseFactory {
            return Room.databaseBuilder(context, DatabaseFactory::class.java, DATABASE_NAME)
                //.allowMainThreadQueries() //可在主线程操作
                /*.addCallback(object : Callback() {
                    override fun onCreate(@NonNull db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                    }

                    override fun onOpen(@NonNull db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                    }
                })*/
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    abstract fun getItemTableDao(): ItemTableDao
    abstract fun getHistoryDao(): HistoryDao
}
