package com.dabenxiang.mimi.di

import android.content.Context
import androidx.room.Room
import com.dabenxiang.mimi.DB_NAME
import com.dabenxiang.mimi.model.db.MiMiDB
import org.koin.dsl.module

val dbModule = module {
    single { provideAppDatabase(get()) }
    single { provideUserDao(get()) }
}

fun provideAppDatabase(context: Context): MiMiDB {
    return Room.databaseBuilder(context, MiMiDB::class.java, DB_NAME)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
}
fun provideUserDao(db: MiMiDB)= db.memberPostDao()

