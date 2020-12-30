package com.dabenxiang.mimi.model.db

import android.content.Context
import androidx.room.*
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*

@Database(
    entities = [
        DBRemoteKey::class,
        PostDBItem::class,
        MemberPostItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    MemberPostItemConverters::class,
    PostTypeConverter::class,
    DateConverter::class,
    LikeTypeConverter::class,
    StringArrayListConverters::class,
    PostStatusConverter::class,
    AdItemConverters::class,
    ClubTabItemTypeConverter::class,
    SourceArrayListConverters::class,
    VideoEpisodeArrayListConverters::class
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

    abstract fun postDBItemDao(): PostDBItemDao
    abstract fun remoteKeyDao(): DBRemoteKeyDao
}

class StringArrayListConverters {
    @TypeConverter
    fun fromString(value: String?): ArrayList<String> {
        val listType: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String?>?): String {
        return Gson().toJson(list)
    }
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