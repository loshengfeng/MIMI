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
    fun getItemById(id:Long): PostDBItem?

    @Query("SELECT * FROM MemberPostItems WHERE id= :id")
    fun getMemberPostItemById(id:Long): MemberPostItem?

    @Query("SELECT * FROM PostDBItems")
    fun pagingSourceAll(): PagingSource<Int, PostDBItem>

    @Transaction
    @Query("SELECT * FROM PostDBItems WHERE postType= :postType ")
    fun pagingSourceByPostType(postType: PostType): PagingSource<Int, PostDBItem>

    @Transaction
    @Query("SELECT * FROM PostDBItems WHERE clubTabItemType= :clubTabItemType ORDER BY timestamp")
    fun pagingSourceByClubTab(clubTabItemType: ClubTabItemType): PagingSource<Int, PostDBItem>

    @Query("DELETE FROM PostDBItems WHERE postType= :postType")
    suspend fun deleteItemByPostType(postType: PostType)

    @Query("DELETE FROM PostDBItems WHERE clubTabItemType= :clubTabItemType")
    suspend fun deleteItemByClubTab(clubTabItemType: ClubTabItemType)

    @Query("DELETE FROM PostDBItems")
    suspend fun deleteAll()

    @Query("DELETE FROM PostDBItems WHERE id = :id")
    suspend fun deleteItem(id: Long)

}

@Entity(tableName = "PostDBItems",
        foreignKeys = [
            ForeignKey(
                    entity = MemberPostItem::class,
                    parentColumns = ["id"],
                    childColumns =["postDBId"],
                    onUpdate =CASCADE)
        ])
data class PostDBItem(
        @PrimaryKey
        @ColumnInfo(name = "id")
        val id: Long,

        @ColumnInfo(name = "postDBId")
        var postDBId: Long,

        @ColumnInfo(name = "postType")
        val postType: PostType,

        @ColumnInfo(name = "clubTabItemType")
        val clubTabItemType: ClubTabItemType,

        @ColumnInfo(name = "timestamp")
        var timestamp:Long
)

data class MemberPostWithPostDBItem(

        @ColumnInfo(name = "postDBItem")
       val postDBItem:PostDBItem,

       @Relation(parentColumn = "id", entityColumn = "postDBId")
       var memberPostItem: MemberPostItem
)