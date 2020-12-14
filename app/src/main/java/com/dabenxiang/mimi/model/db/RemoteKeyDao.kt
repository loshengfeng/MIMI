package com.dabenxiang.mimi.model.db

import androidx.room.*
import com.dabenxiang.mimi.model.enums.ClubTabItemType

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(remoteKey: RemoteKey)

    @Query("SELECT * FROM remote_keys WHERE type = :type")
    suspend fun remoteKeyByType(type: ClubTabItemType): RemoteKey

    @Query("DELETE FROM remote_keys WHERE type = :type")
    suspend fun deleteByType(type: ClubTabItemType)

}
@Entity(tableName = "remote_keys")
data class RemoteKey(
        @PrimaryKey
        @ColumnInfo(name = "type")
        val type: ClubTabItemType,
        val offset: Long?
)