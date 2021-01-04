package com.dabenxiang.mimi.model.db

import androidx.paging.PagingSource
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.dabenxiang.mimi.model.api.vo.AdItem
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

    @Query("SELECT * FROM PostDBItems WHERE pageCode = :pageCode and postDBId = :postDBId limit 1")
    fun getPostDBItem(pageCode:String, postDBId:Long): PostDBItem?

    @Query("SELECT * FROM PostDBItems WHERE pageCode = :pageCode limit 1")
    fun getFirstPostDBItem(pageCode:String): PostDBItem?

    @Query("SELECT * FROM PostDBItems WHERE postDBId = :postDBId")
    fun getPostDBItems(postDBId:Long): List<PostDBItem>?

    @Query("SELECT * FROM PostDBItems WHERE pageCode = :pageCode  ORDER BY timestamp")
    fun getPostDBItemsByTime(pageCode:String): List<MemberPostWithPostDBItem>?

    @Query("SELECT DISTINCT postDBId FROM PostDBItems WHERE pageCode = :pageCode ")
    fun getPostDBIdsByPageCode(pageCode:String): List<Long>?

    @Query("SELECT * FROM MemberPostItems WHERE id= :id")
    fun getMemberPostItemById(id:Long): MemberPostItem?

    @Query("SELECT * FROM MemberPostItems WHERE videoId= :videoId")
    fun getMemberPostItemByVideoId(videoId:Long): MemberPostItem?

    @Transaction
    @Query("SELECT * FROM PostDBItems WHERE pageCode= :pageCode ORDER BY timestamp")
    fun pagingSourceByPageCode(pageCode: String): PagingSource<Int, MemberPostWithPostDBItem>

    @Query("DELETE FROM PostDBItems WHERE pageCode = :pageCode")
    suspend fun deleteItemByPageCode(pageCode: String)

    @Query("DELETE FROM PostDBItems WHERE pageCode = :pageCode and postDBId = :postDBId")
    suspend fun deleteItemByPageCode(pageCode: String, postDBId:Long)

    @Query("DELETE FROM MemberPostItems WHERE id = :id")
    suspend fun deleteMemberPostItem(id:Long)

    @Query("DELETE FROM PostDBItems WHERE postDBId = :postDBId")
    suspend fun deleteItem(postDBId: Long)

    @Query("DELETE FROM PostDBItems")
    suspend fun deleteAll()

    @Query("DELETE FROM MemberPostItems")
    suspend fun deleteAllMemberPostItems()
}


@Entity(
    tableName = "PostDBItems"
)
data class PostDBItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long =0,

    @ColumnInfo(name = "postDBId")
    var postDBId: Long,

    @ColumnInfo(name = "postType")
    var postType: PostType,

    @ColumnInfo(name = "pageCode")
    var pageCode: String,

    @ColumnInfo(name = "timestamp")
    var timestamp: Long,

    @ColumnInfo(name = "index")
    var index: Int,
)

data class MemberPostWithPostDBItem(
       @Embedded
       var postDBItem:PostDBItem,

       @Relation(parentColumn = "postDBId", entityColumn = "id")
       var memberPostItem: MemberPostItem
)

