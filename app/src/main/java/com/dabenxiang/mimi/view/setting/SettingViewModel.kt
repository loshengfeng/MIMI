package com.dabenxiang.mimi.view.setting

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.AvatarRequest
import com.dabenxiang.mimi.model.api.vo.EmailRequest
import com.dabenxiang.mimi.model.api.vo.ProfileItem
import com.dabenxiang.mimi.model.api.vo.ProfileRequest
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.inject
import retrofit2.HttpException
import timber.log.Timber
import tw.gov.president.manager.submanager.update.VersionManager
import java.io.File
import java.net.URLEncoder

class SettingViewModel : BaseViewModel() {

    private val versionManager: VersionManager by inject()

    var bitmap: Bitmap? = null
    var byteArray: ByteArray? = null

    private val _profileItem = MutableLiveData<ApiResult<ProfileItem>>()
    val profileItem: LiveData<ApiResult<ProfileItem>> = _profileItem

    private val _resendResult = MutableLiveData<ApiResult<Nothing>>()
    val resendResult: LiveData<ApiResult<Nothing>> = _resendResult

    private val _updateResult = MutableLiveData<ApiResult<Nothing>>()
    val updateResult: LiveData<ApiResult<Nothing>> = _updateResult

    private val _postResult = MutableLiveData<ApiResult<Long>>()
    val postResult: LiveData<ApiResult<Long>> = _postResult

    private val _putResult = MutableLiveData<ApiResult<Nothing>>()
    val putResult: LiveData<ApiResult<Nothing>> = _putResult

    private val _isBinding: MutableLiveData<Boolean> = MutableLiveData()
    val isBinding: MutableLiveData<Boolean> = _isBinding

    private val _imageBitmap = MutableLiveData<ApiResult<Bitmap>>()
    val imageBitmap: LiveData<ApiResult<Bitmap>> = _imageBitmap

    var profileData: ProfileItem? = null

    fun getProfile() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getProfile()
                if (!result.isSuccessful) throw HttpException(result)
                profileData = result.body()?.content
                profileData?.let {
                    it.avatarAttachmentId?.also { id -> getAttachment(id) }
                }
                emit(ApiResult.success(result.body()?.content))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _profileItem.value = it }
        }
    }

    fun getAttachment(id: Long) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.getAttachment(id.toString())
                if (!result.isSuccessful) throw HttpException(result)
                byteArray = result.body()?.bytes()
                accountManager.setupMeAvatarCache(byteArray)
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                emit(ApiResult.success(bitmap))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _imageBitmap.value = it }
        }
    }

    fun resendEmail() {
        viewModelScope.launch {
            flow {
                val result =
                    domainManager.getApiRepository().resendEmail(EmailRequest(domainManager.getWebDomain()+ DomainManager.PARAM_RESET_CODE))
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _resendResult.value = it }
        }
    }

    fun updateProfile() {
        viewModelScope.launch {
            flow {
                val request = ProfileRequest(
                    profileData?.friendlyName,
                    profileData?.gender,
                    profileData?.birthday,
                    profileData?.email,
                    domainManager.getWebDomain() + DomainManager.PARAM_RESET_CODE
                )
                val result = domainManager.getApiRepository().updateProfile(request)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _updateResult.value = it }
        }
    }

    fun postAttachment() {
        val fileName =
            StringBuffer(accountManager.getProfile().friendlyName).append(".jpeg").toString()
        val tempImagePath =
            Environment.getExternalStorageDirectory().path.plus(StringBuffer("/").append(fileName))

        bitmap?.also {
            FileUtil.saveBitmapToJpegFile(it, it.width, it.height, destPath = tempImagePath)
        }

        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().postAttachment(
                    File(tempImagePath),
                    fileName = URLEncoder.encode(fileName, "UTF-8")
                )

                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _postResult.value = it }
        }
    }

    fun putAvatar(id: Long) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().putAvatar(AvatarRequest(id))
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _putResult.value = it }
        }
    }

    fun isEmailConfirmed(): Boolean {
        return profileData?.emailConfirmed ?: false
    }

    fun bindingInvitationCodes(context: Context, code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                val isCodeBinding = versionManager.bindingInvitationCodes(code)
                emit(isCodeBinding)
            }.flowOn(Dispatchers.IO)
                .catch { e ->
                    Timber.e(e)
                }
                .collect {
                    Timber.i("bindingInvitationCodes = $it")
                    _isBinding.postValue(it)
                }
        }
    }
}