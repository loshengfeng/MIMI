package com.dabenxiang.mimi.model.db

import androidx.room.*

@Dao
interface DBRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(remoteKey: DBRemoteKey)

    @Query("SELECT * FROM db_remote_keys WHERE pageName = :pageName")
    suspend fun remoteKeyByType(pageName: String): DBRemoteKey

    @Query("DELETE FROM db_remote_keys WHERE pageName = :pageName")
    suspend fun deleteByType(pageName: String)

}

@Entity(tableName = "db_remote_keys")
data class DBRemoteKey(
    @PrimaryKey
    @ColumnInfo(name = "pageName")
    val pageName: String ="",
    val offset: Long?
)

//@Entity(tableName = "club_tab_remote_keys")
//data class ClubTabRemoteKey(
//        @PrimaryKey
//        @ColumnInfo(name = "type")
//        val type: ClubTabItemType,
//        val offset: Long?
//)
//
//@Entity(tableName = "post_type_remote_keys")
//data class PostTypeRemoteKey(
//    @PrimaryKey
//    @ColumnInfo(name = "type")
//    val type: PostType,
//    val offset: Long?
//)