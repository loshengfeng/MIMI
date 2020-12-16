package com.dabenxiang.mimi.model.db

import androidx.paging.PagingSource
import androidx.room.*
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.model.enums.PostType

@Dao
interface PostDBItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostDBItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemberPostItemAll(posts: List<MemberPostItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: PostDBItem) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMemberPostItem(item: MemberPostItem) : Long

    @Query("SELECT * FROM PostDBItems WHERE id= :id")
    fun getItemById(id:Long): PostDBItem?

    @Query("SELECT * FROM MemberPostItems WHERE id= :id")
    fun getMemberPostItemById(id:Long): MemberPostItem?

    @Query("SELECT * FROM PostDBItems")
    fun pagingSourceAll(): PagingSource<Int, PostDBItem>

    @Transaction
    @Query("SELECT * FROM PostDBItems WHERE postType= :postType ")
    fun pagingSourceByPostType(postType: PostType): PagingSource<Int, MemberPostWithPostDBItem>

    @Transaction
    @Query("SELECT * FROM PostDBItems WHERE clubTabItemType= :clubTabItemType ")
    fun pagingSourceByClubTab(clubTabItemType: ClubTabItemType): PagingSource<Int, MemberPostWithPostDBItem>

    @Query("DELETE FROM PostDBItems WHERE postType= :postType")
    suspend fun deleteItemByPostType(postType: PostType)

    @Query("DELETE FROM PostDBItems WHERE clubTabItemType= :clubTabItemType")
    suspend fun deleteItemByClubTab(clubTabItemType: ClubTabItemType)

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

        @ColumnInfo(name = "postDBId")
        var postDBId: Long,

        @ColumnInfo(name = "postType")
        val postType: PostType,

        @ColumnInfo(name = "clubTabItemType")
        val clubTabItemType: ClubTabItemType
)

data class MemberPostWithPostDBItem(
       @Relation(parentColumn = "id", entityColumn = "postDBId")
       val postDBItem:PostDBItem,

       @Embedded
       var memberPostItem: MemberPostItem
)