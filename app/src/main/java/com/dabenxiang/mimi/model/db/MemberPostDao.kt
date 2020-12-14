package com.dabenxiang.mimi.model.db

import androidx.paging.PagingSource
import androidx.room.*
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType

@Dao
interface MemberPostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<MemberPostItem>)

    @Query("SELECT * FROM MemberPostItems WHERE type = :type")
    fun pagingSourceAll(type: PostType): PagingSource<Int, MemberPostItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: MemberPostItem) : Long

    @Query("DELETE FROM MemberPostItems")
    suspend fun deleteAll()

    @Query("DELETE FROM MemberPostItems WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM MemberPostItems WHERE type = :type")
    suspend fun deleteItemByType(type: PostType)

}

