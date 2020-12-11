package com.dabenxiang.mimi.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dabenxiang.mimi.model.api.vo.MemberPostItem

@Database(
    entities = [MemberPostItem::class],
    version = 1,
    exportSchema = false
)
abstract class MiMiDB : RoomDatabase() {
    companion object {
        fun create(context: Context): MiMiDB {
            val databaseBuilder =
                Room.databaseBuilder(context, MiMiDB::class.java, "mimi_posts.db")

            return databaseBuilder
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    abstract fun memberPostDao(): MemberPostDao
}