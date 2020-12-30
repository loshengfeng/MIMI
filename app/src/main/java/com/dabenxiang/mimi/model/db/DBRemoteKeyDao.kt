package com.dabenxiang.mimi.model.db

import androidx.room.*

@Dao
interface DBRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(remoteKey: DBRemoteKey)

    @Query("SELECT * FROM db_remote_keys WHERE pageCode = :pageCode")
    suspend fun remoteKeyByPageCode(pageCode: String): DBRemoteKey?

    @Query("DELETE FROM db_remote_keys WHERE pageCode = :pageCode")
    suspend fun deleteByPageCode(pageCode: String)

}

@Entity(tableName = "db_remote_keys")
data class DBRemoteKey(
    @PrimaryKey
    @ColumnInfo(name = "pageCode")
    val pageCode: String ="",
    val offset: Long?
)
