package com.dabenxiang.mimi.view.personal

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MeItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class PersonalViewModel : BaseViewModel() {

    var byteArray: ByteArray? = null

    private val _meItem = MutableLiveData<ApiResult<MeItem>>()
    val meItem: LiveData<ApiResult<MeItem>> = _meItem

    private val _apiSignOut = MutableLiveData<ApiResult<Nothing>>()
    val apiSignOut: LiveData<ApiResult<Nothing>> = _apiSignOut

    private val _imageBitmap = MutableLiveData<ApiResult<Bitmap>>()
    val imageBitmap: LiveData<ApiResult<Bitmap>> = _imageBitmap

    fun getMe() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMe()
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.body()?.content))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _meItem.value = it
                }
        }
    }

    fun getAttachment() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .getAttachment(accountManager.getProfile().avatarAttachmentId)
                if (!result.isSuccessful) throw HttpException(result)
                byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                emit(ApiResult.success(bitmap))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _imageBitmap.value = it }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            accountManager.signOut().collect {
                _apiSignOut.value = it
            }
        }
    }
}