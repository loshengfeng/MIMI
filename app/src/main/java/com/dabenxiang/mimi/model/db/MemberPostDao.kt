package com.dabenxiang.mimi.model.db

import androidx.paging.PagingSource
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
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

    @Update
    fun updatePostDBItem(item: PostDBItem)

    @Update
    fun updateMemberPostItem(item: MemberPostItem)

    @Query("SELECT * FROM PostDBItems WHERE id= :id")
    fun getPostDBItemsById(id:Long): PostDBItem?

    @Query("SELECT * FROM PostDBItems WHERE pageName = :pageName and postDBId = :postDBId limit 1")
    fun getPostDBItem(pageName:String, postDBId:Long): PostDBItem?

    @Query("SELECT * FROM PostDBItems WHERE postDBId = :postDBId")
    fun getPostDBItems(postDBId:Long): List<PostDBItem>?

    @Query("SELECT * FROM PostDBItems WHERE pageName = :pageName and postType = :postType ")
    fun getPostDBItem(pageName:String, postType:PostType): PostDBItem?

    @Query("SELECT * FROM MemberPostItems WHERE id= :id")
    fun getMemberPostItemById(id:Long): MemberPostItem?

    @Query("SELECT * FROM PostDBItems")
    fun pagingSourceAll(): PagingSource<Int, PostDBItem>

    @Transaction
    @Query("SELECT * FROM PostDBItems WHERE pageName= :pageName ORDER BY timestamp")
    fun pagingSourceByClubTab(pageName: String): PagingSource<Int, MemberPostWithPostDBItem>

    @Query("DELETE FROM PostDBItems WHERE postType= :postType")
    suspend fun deleteItemByPostType(postType: PostType)

    @Query("DELETE FROM PostDBItems WHERE pageName = :pageName")
    suspend fun deleteItemByClubTab(pageName: String)

    @Query("DELETE FROM PostDBItems WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM PostDBItems")
    suspend fun deleteAll()
}


@Entity(
    tableName = "PostDBItems",
    indices = [
        Index(value = ["postDBId"], unique = true)
    ]
)
data class PostDBItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long =0,

    @ColumnInfo(name = "postDBId")
    var postDBId: Long,

    @ColumnInfo(name = "postType")
    val postType: PostType,

    @ColumnInfo(name = "pageName")
    val pageName: String,

    @ColumnInfo(name = "timestamp")
    var timestamp: Long,
)

data class MemberPostWithPostDBItem(
       @Embedded
       val postDBItem:PostDBItem,

       @Relation(parentColumn = "postDBId", entityColumn = "id")
       var memberPostItem: MemberPostItem
)

