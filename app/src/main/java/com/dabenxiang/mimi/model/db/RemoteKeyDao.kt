package com.dabenxiang.mimi.model.db

import androidx.room.*
import com.dabenxiang.mimi.model.enums.PostType

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(remoteKey: RemoteKey)

    @Query("SELECT * FROM remote_keys WHERE type = :type")
    suspend fun remoteKeyByType(type: PostType): RemoteKey

    @Query("DELETE FROM remote_keys WHERE type = :type")
    suspend fun deleteByType(type: PostType)

}
@Entity(tableName = "remote_keys")
data class RemoteKey(
        @PrimaryKey
        @ColumnInfo(name = "type")
        val type: PostType,
        val offset: Long?
)