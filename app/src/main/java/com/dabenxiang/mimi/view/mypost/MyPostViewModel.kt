package com.dabenxiang.mimi.view.mypost

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PicParameter
import com.dabenxiang.mimi.model.api.vo.PostMemberRequest
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.AttachmentItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.home.HomeViewModel
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.dabenxiang.mimi.widget.utility.UriUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.net.URLEncoder

class MyPostViewModel : BaseViewModel() {

    private val _myPostItemListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val myPostItemListResult: LiveData<PagedList<MemberPostItem>> = _myPostItemListResult

    private var _attachmentByTypeResult = MutableLiveData<ApiResult<AttachmentItem>>()
    val attachmentByTypeResult: LiveData<ApiResult<AttachmentItem>> = _attachmentByTypeResult

    private var _attachmentResult = MutableLiveData<ApiResult<AttachmentItem>>()
    val attachmentResult: LiveData<ApiResult<AttachmentItem>> = _attachmentResult

    private var _likePostResult = MutableLiveData<ApiResult<Int>>()
    val likePostResult: LiveData<ApiResult<Int>> = _likePostResult

    private var _favoriteResult = MutableLiveData<ApiResult<Int>>()
    val favoriteResult: LiveData<ApiResult<Int>> = _favoriteResult

    private var _followResult = MutableLiveData<ApiResult<Nothing>>()
    val followResult: LiveData<ApiResult<Nothing>> = _followResult

    private var _deletePostResult = MutableLiveData<ApiResult<Int>>()
    val deletePostResult: LiveData<ApiResult<Int>> = _deletePostResult

    private val _uploadPicItem = MutableLiveData<PicParameter>()
    val uploadPicItem: LiveData<PicParameter> = _uploadPicItem

    private val _postPicResult = MutableLiveData<ApiResult<Long>>()
    val postPicResult: LiveData<ApiResult<Long>> = _postPicResult

    private val _postVideoMemberResult = MutableLiveData<ApiResult<Long>>()
    val postVideoMemberResult: LiveData<ApiResult<Long>> = _postVideoMemberResult

    private val _postDeleteAttachment = MutableLiveData<ApiResult<Nothing>>()
    val postDeleteAttachment: LiveData<ApiResult<Nothing>> = _postDeleteAttachment

    private val _postDeleteVideoAttachment = MutableLiveData<ApiResult<Nothing>>()
    val postDeleteVideoAttachment: LiveData<ApiResult<Nothing>> = _postDeleteVideoAttachment

    private val _postDeleteCoverAttachment = MutableLiveData<ApiResult<Nothing>>()
    val postDeleteCoverAttachment: LiveData<ApiResult<Nothing>> = _postDeleteCoverAttachment

    private val _uploadCoverItem = MutableLiveData<PicParameter>()
    val uploadCoverItem: LiveData<PicParameter> = _uploadCoverItem

    private val _postCoverResult = MutableLiveData<ApiResult<Long>>()
    val postCoverResult: LiveData<ApiResult<Long>> = _postCoverResult

    private val _postVideoResult = MutableLiveData<ApiResult<Long>>()
    val postVideoResult: LiveData<ApiResult<Long>> = _postVideoResult

    private val _postArticleResult = MutableLiveData<ApiResult<Long>>()
    val postArticleResult: LiveData<ApiResult<Long>> = _postArticleResult

    private val _cleanRemovedPosList = MutableLiveData<Nothing>()
    val cleanRemovedPosList: LiveData<Nothing> = _cleanRemovedPosList

    var totalCount: Int = 0

    private var job = Job()

    companion object {
        const val TYPE_PIC = "type_pic"
        const val TYPE_COVER = "type_cover"
        const val TYPE_VIDEO = "type_video"
        const val USER_ID_ME: Long = -1
    }

    fun getMyPost(userId: Long, isAdult: Boolean) {
        viewModelScope.launch {
            getMyPostPagingItems(userId, isAdult).asFlow()
                .collect { _myPostItemListResult.value = it }
        }
    }

    private fun getMyPostPagingItems(
        userId: Long,
        isAdult: Boolean
    ): LiveData<PagedList<MemberPostItem>> {
        val dataSourceFactory = object : DataSource.Factory<Int, MemberPostItem>() {
            override fun create(): DataSource<Int, MemberPostItem> {
                return MyPostDataSource(
                    userId,
                    isAdult,
                    pagingCallback,
                    viewModelScope,
                    domainManager
                )
            }
        }

        val config = PagedList.Config.Builder()
            .setPrefetchDistance(4)
            .build()
        return LivePagedListBuilder(dataSourceFactory, config).build()
    }

    private val pagingCallback = object : PagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {
            Timber.e(throwable)
        }

        override fun onTotalCount(count: Long, isInitial: Boolean) {
            totalCount = if (isInitial) count.toInt()
            else totalCount.plus(count.toInt())
            if(isInitial) _cleanRemovedPosList.value = null
        }
    }

    fun getAttachment(id: String, position: Int, type: AttachmentType) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                val item = AttachmentItem(
                    id = id,
                    bitmap = bitmap,
                    position = position,
                    type = type
                )
                emit(ApiResult.success(item))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _attachmentByTypeResult.value = it }
        }
    }

    fun getAttachment(id: String, parentPosition: Int, position: Int) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                val item = AttachmentItem(
                    id = id,
                    bitmap = bitmap,
                    parentPosition = parentPosition,
                    position = position
                )
                emit(ApiResult.success(item))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _attachmentResult.value = it }
        }
    }

    fun likePost(item: MemberPostItem, position: Int, isLike: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val likeType = when {
                    isLike -> LikeType.LIKE
                    else -> LikeType.DISLIKE
                }
                val request = LikeRequest(likeType)
                val result = apiRepository.like(item.id, request)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _likePostResult.value = it }
        }
    }

    fun favoritePost(
        item: MemberPostItem,
        position: Int,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFavorite -> apiRepository.addFavorite(item.id)
                    else -> apiRepository.deleteFavorite(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _favoriteResult.value = it }
        }
    }

    fun followPost(items: ArrayList<MemberPostItem>, position: Int, isFollow: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followPost(items[position].creatorId)
                    else -> apiRepository.cancelFollowPost(items[position].creatorId)
                }
                if (!result.isSuccessful) throw HttpException(result)
                items.forEach { item ->
                    item.isFollow = isFollow
                }
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _followResult.value = it }
        }
    }

    fun deletePost(
        item: MemberPostItem,
        items: ArrayList<MemberPostItem>
    ) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.deleteMyPost(item.id)
                if (!result.isSuccessful) throw HttpException(result)
                val position = items.indexOf(item)
                items.remove(item)
                totalCount--
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _deletePostResult.value = it }
        }
    }

    fun postAttachment(pic: String, context: Context, type: String) {
        viewModelScope.launch {
            flow {
                val realPath =  when (type) {
                    HomeViewModel.TYPE_VIDEO -> pic
                    HomeViewModel.TYPE_PIC -> pic
                    else -> UriUtils.getPath(context, Uri.parse(pic))
                }
                val fileNameSplit = realPath?.split("/")
                val fileName = fileNameSplit?.last()
                val extSplit = fileName?.split(".")
                val ext = "." + extSplit?.last()

                if (type == HomeViewModel.TYPE_PIC) {
                    val picParameter = PicParameter(ext = ext)
                    _uploadPicItem.postValue(picParameter)
                } else if (type == HomeViewModel.TYPE_COVER) {
                    val picParameter = PicParameter(ext = ext)
                    _uploadCoverItem.postValue(picParameter)
                }

                Timber.d("Upload photo path : $realPath")
                Timber.d("Upload photo ext : $ext")

                val result = domainManager.getApiRepository().postAttachment(
                    File(realPath),
                    fileName = URLEncoder.encode(fileName, "UTF-8")
                )

                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    if (type == TYPE_PIC) {
                        _postPicResult.postValue(it)
                    } else if (type == TYPE_COVER) {
                        _postCoverResult.postValue(it)
                    } else if (type == TYPE_VIDEO) {
                        _postVideoResult.postValue(it)
                    }
                }
        }
    }

    fun postPic(id: Long, request: PostMemberRequest, content: String) {
        viewModelScope.launch {
            flow {
                request.content = content
                Timber.d("Post member request : $request")
                val resp = domainManager.getApiRepository().updatePost(id, request)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(id))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postVideoMemberResult.value = it }
        }
    }

    fun deleteAttachment(id: String) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().deleteAttachment(id)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postDeleteAttachment.value = it }
        }
    }

    fun deleteVideoAttachment(id: String, type: String) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().deleteAttachment(id)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    if (type == TYPE_COVER) {
                        _postDeleteCoverAttachment.postValue(it)
                    } else if (type == TYPE_VIDEO) {
                        _postDeleteVideoAttachment.postValue(it)
                    }
                }
        }
    }

    fun updateArticle(
        title: String,
        content: String,
        tags: ArrayList<String>,
        item: MemberPostItem
    ) {
        viewModelScope.launch {
            flow {
                val request = PostMemberRequest(
                    title = title,
                    content = content,
                    type = PostType.TEXT.value,
                    tags = tags
                )

                val resp = domainManager.getApiRepository().updatePost(item.id, request)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(item.id))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postArticleResult.value = it }
        }
    }

    fun getBitmap(id: String, update: ((String) -> Unit)) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                LruCacheUtils.putLruCache(id, bitmap)
                emit(ApiResult.success(id))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when (it) {
                        is ApiResult.Success -> {
                            update(it.result)
                        }
                    }
                }
        }
    }

    fun clearLiveData() {
        _postArticleResult.value = null
        _postVideoMemberResult.value = null
    }

    fun cancelJob() {
        job.cancel()
    }
}