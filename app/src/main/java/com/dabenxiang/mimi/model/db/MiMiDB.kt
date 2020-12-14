package com.dabenxiang.mimi.model.db

import android.content.Context
import androidx.room.*
import com.dabenxiang.mimi.model.api.vo.AdItemConverters
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.*
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList

@Database(
        entities = [MemberPostItem::class, RemoteKey::class],
        version = 1,
        exportSchema = false
)
@TypeConverters(
        PostTypeConverter::class,
        DateConverter::class,
        LikeTypeConverter::class,
        StringArrayListConverters::class,
        PostStatusConverter::class,
        AdItemConverters::class)
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
    abstract fun remoteKeys(): RemoteKeyDao
}

class StringArrayListConverters {

    @TypeConverter
    fun listToJson(value: List<String>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<String>::class.java).toList() as ArrayList
}

object DateConverter {
    @TypeConverter
    fun toDate(dateLong: Long?): Date? {
        return dateLong?.let { Date(it) }
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
}