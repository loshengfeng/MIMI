package com.dabenxiang.mimi.model.db

import androidx.paging.PagingSource
import androidx.room.*
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.model.enums.PostType
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

@Dao
interface PostDBItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostDBItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: PostDBItem) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: MemberPostItem) : Long

    @Query("SELECT * FROM PostDBItems WHERE postDBId= :postDBId")
    fun getItemById(postDBId:String): PostDBItem?

    @Query("SELECT * FROM MemberPostItems WHERE id= :id")
    fun getMemberPostItemById(id:Long): MemberPostItem?

    @Query("SELECT * FROM PostDBItems")
    fun pagingSourceAll(): PagingSource<Int, PostDBItem>

    @Query("SELECT * FROM PostDBItems WHERE postType= :postType ")
    fun pagingSourceByPostType(postType: PostType): PagingSource<Int, PostDBItem>

    @Query("SELECT * FROM PostDBItems WHERE clubTabItemType= :clubTabItemType ")
    fun pagingSourceByClubTab(clubTabItemType: ClubTabItemType): PagingSource<Int, PostDBItem>

//    @Query("SELECT * FROM PostDBItems WHERE textOrder >= 0 order by textOrder")
//    fun pagingSourceByTextType(): PagingSource<Int, PostDBItem>
//
//    @Query("DELETE FROM PostDBItems WHERE textOrder >= 0 ")
//    suspend fun deleteItemByTextType()
//
//    @Query("SELECT * FROM PostDBItems WHERE imageOrder >= 0 order by imageOrder")
//    fun pagingSourceByImageType(): PagingSource<Int, PostDBItem>
//
//    @Query("DELETE FROM PostDBItems WHERE imageOrder >= 0")
//    suspend fun deleteItemByImageType()
//
//    @Query("SELECT * FROM PostDBItems WHERE videoOrder >= 0 order by videoOrder")
//    fun pagingSourceByVideoType(): PagingSource<Int, PostDBItem>
//
//    @Query("DELETE FROM PostDBItems WHERE videoOrder >= 0")
//    suspend fun deleteItemByVideoType()
//
//    @Query("SELECT * FROM PostDBItems WHERE followOrder >= 0 order by followOrder ")
//    fun pagingSourceByFollow(): PagingSource<Int, PostDBItem>
//
//    @Query("DELETE FROM PostDBItems WHERE followOrder >= 0")
//    suspend fun deleteItemByFollow()
//
//    @Query("SELECT * FROM PostDBItems WHERE hottestOrder >= 0 order by hottestOrder ")
//    fun pagingSourceByLatest(): PagingSource<Int, PostDBItem>
//
//    @Query("DELETE FROM PostDBItems WHERE  hottestOrder >= 0")
//    suspend fun deleteItemByLatest()
//
//    @Query("SELECT * FROM PostDBItems WHERE hottestOrder >= 0 order by hottestOrder ")
//    fun pagingSourceByHottest(): PagingSource<Int, PostDBItem>
//
//    @Query("DELETE FROM PostDBItems WHERE hottestOrder >= 0")
//    suspend fun deleteItemByHottest()
//
    @Query("DELETE FROM PostDBItems WHERE postType= :postType")
    suspend fun deleteItemByPostType(postType: PostType)

    @Query("DELETE FROM PostDBItems WHERE clubTabItemType= :clubTabItemType")
    suspend fun deleteItemByClubTab(clubTabItemType: ClubTabItemType)

    @Query("DELETE FROM PostDBItems")
    suspend fun deleteAll()

    @Query("DELETE FROM PostDBItems WHERE postDBId = :postDBId")
    suspend fun deleteItem(postDBId: Long)

}

@Dao
interface MemberPostItemDao {

}

@Entity(tableName = "PostDBItems")
data class PostDBItem(
        @PrimaryKey
        @ColumnInfo(name = "postDBId")
        val postDBId: String,

        @ColumnInfo(name = "postType")
        val postType: PostType,

        @ColumnInfo(name = "clubTabItemType")
        val clubTabItemType: ClubTabItemType,

        @Embedded(prefix = "memberPostItem_")
        @Relation(entity = MemberPostItem::class)
        var memberPostItem: MemberPostItem
)
