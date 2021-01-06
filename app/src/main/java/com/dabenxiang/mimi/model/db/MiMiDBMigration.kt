package com.dabenxiang.mimi.model.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/*
  As you add and change features in your app, you need to modify your Room entity classes to reflect these changes.
  please add a migration classes and describe the changed information to below.
  Then add the new migration classes Name to DB builder and increase DB version.
*/

//Just make a example for test migration function
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE PostDBItems ADD COLUMN creationTime LONG")
    }
}
