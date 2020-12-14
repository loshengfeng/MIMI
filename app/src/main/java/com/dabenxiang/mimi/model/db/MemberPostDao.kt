package com.dabenxiang.mimi.model.db

import androidx.paging.PagingSource
import androidx.room.*
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType
import com.google.gson.annotations.SerializedName

@Dao
interface PostDBItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostDBItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: PostDBItem) : Long

    @Query("SELECT * FROM PostDBItems WHERE id= :id")
    fun getItemById(id:Long): PostDBItem?

    @Query("SELECT * FROM PostDBItems")
    fun pagingSourceAll(): PagingSource<Int, PostDBItem>

    @Query("SELECT * FROM PostDBItems WHERE postType= :postType")
    fun pagingSourceByPostType(postType: PostType): PagingSource<Int, PostDBItem>

    @Query("SELECT * FROM PostDBItems WHERE isFollow = 1")
    fun pagingSourceByFollow(): PagingSource<Int, PostDBItem>

    @Query("DELETE FROM PostDBItems WHERE isFollow = 1")
    suspend fun deleteItemByFollow()

    @Query("SELECT * FROM PostDBItems WHERE isLatest = 1")
    fun pagingSourceByLatest(): PagingSource<Int, PostDBItem>

    @Query("DELETE FROM PostDBItems WHERE isLatest = 1")
    suspend fun deleteItemByLatest()

    @Query("SELECT * FROM PostDBItems WHERE isHottest = 1")
    fun pagingSourceByHottest(): PagingSource<Int, PostDBItem>

    @Query("DELETE FROM PostDBItems WHERE isHottest = 1")
    suspend fun deleteItemByHottest()

    @Query("DELETE FROM PostDBItems WHERE postType= :postType")
    suspend fun deleteItemByPostType(postType: PostType)

    @Query("DELETE FROM PostDBItems")
    suspend fun deleteAll()

    @Query("DELETE FROM PostDBItems WHERE id = :id")
    suspend fun deleteItem(id: Long)

}

@Entity(tableName = "PostDBItems")
data class PostDBItem(
        @PrimaryKey
        @ColumnInfo(name = "id")
        val id: Long,

        @ColumnInfo(name = "isFollow")
        var isFollow:Boolean = false,

        @ColumnInfo(name = "isHottest")
        var isHottest:Boolean = false,

        @ColumnInfo(name = "isLatest")
        var isLatest:Boolean = false,

        @ColumnInfo(name = "postType")
        val postType: PostType,

        @ColumnInfo(name = "memberPostItem")
        var memberPostItem: MemberPostItem
)
