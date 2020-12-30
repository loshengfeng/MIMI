package com.dabenxiang.mimi.view.base

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.OnMeMoreDialogListener
import com.dabenxiang.mimi.extension.handleException
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.HttpErrorMsgType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PostAttachmentItem
import com.dabenxiang.mimi.model.vo.PostVideoAttachment
import com.dabenxiang.mimi.view.club.pic.ClubPicFragment
import com.dabenxiang.mimi.view.club.text.ClubTextFragment
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.dialog.comment.MyPostMoreDialogFragment
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.home.HomeViewModel
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.main.MainViewModel
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.mypost.MyPostViewModel
import com.dabenxiang.mimi.view.player.ui.ClipPlayerFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.MEMBER_REQUEST
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.PIC_URI
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.UPLOAD_ARTICLE
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.UPLOAD_PIC
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.UPLOAD_VIDEO
import com.dabenxiang.mimi.view.post.utility.PostManager
import com.dabenxiang.mimi.widget.utility.GeneralUtils.showToast
import com.dabenxiang.mimi.widget.utility.UriUtils
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tab_club.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.io.Serializable
import java.net.UnknownHostException
import java.util.*

abstract class BaseFragment : Fragment() {

    companion object {
        const val PERMISSION_EXTERNAL_REQUEST_CODE = 637
        const val PERMISSION_CAMERA_REQUEST_CODE = 699
        const val PERMISSION_VIDEO_REQUEST_CODE = 20001
        const val PERMISSION_PIC_REQUEST_CODE = 20002
        const val PERMISSION_GALLERY_REQUEST_CODE = 20003
    }

    open var mainViewModel: MainViewModel? = null
    lateinit var progressHUD: KProgressHUD

    var mView: View? = null
    var firstCreateView = false

    val externalPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val cameraPermissions = externalPermissions + arrayOf(Manifest.permission.CAMERA)

    var snackBar: Snackbar? = null

    private var moreDialog: MoreDialogFragment? = null

    private var postType = PostType.TEXT

    private var uploadCurrentPicPosition = 0
    private var deleteCurrentPicPosition = 0

    private var uploadPicUri = arrayListOf<PostAttachmentItem>()
    private var uploadPicItem = arrayListOf<PicParameter>()
    private var deletePicList = arrayListOf<String>()
    private val uploadPicList = arrayListOf<PicParameter>()
    private val picParameterList = arrayListOf<PicParameter>()
    private var deleteVideoItem = arrayListOf<PostVideoAttachment>()
    private var uploadVideoList = arrayListOf<PostVideoAttachment>()

    private var memberPostItem = MemberPostItem()
    private var postMemberRequest = PostMemberRequest()
    private var picParameter = PicParameter()
    private var videoParameter = VideoParameter()

    private var postId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let { mainViewModel = ViewModelProvider(it).get(MainViewModel::class.java) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        takeIf { mView == null }?.let {
            firstCreateView = true
            mView = inflater.inflate(getLayoutId(), container, false)
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel?.setNavTransparent(isNavTransparent)
        mainViewModel?.setStatusBarMode(isStatusBarDark)
        statusBarVisibility()

        progressHUD = KProgressHUD.create(context)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)

        activity?.bottom_navigation?.visibility = bottomNavigationVisibility

        takeIf { firstCreateView }?.run {
            setupFirstTime()
            firstCreateView = false
        }

        setupListeners()
        setupObservers()

        handlePostClub()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resetObservers()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mainViewModel?.postArticleResult?.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResult.Success -> {
                    setSnackBarPostStatus(it.result)
                    mainViewModel?.clearLiveDataValue()

                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.uploadPicItemResult?.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                return@Observer
            }
            val data = arguments?.getSerializable(MyPostFragment.MEMBER_DATA)
            if (data == null) {
                uploadPicItem.add(it)
            } else {
                uploadPicList[uploadCurrentPicPosition] = it
            }
        })

        mainViewModel?.postPicResult?.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResult.Success -> {
                    val data = arguments?.getSerializable(MyPostFragment.MEMBER_DATA)
                    if (data == null) {
                        uploadPicItem[uploadCurrentPicPosition].id = it.result.toString()
                        uploadCurrentPicPosition += 1

                        if (uploadCurrentPicPosition > uploadPicUri.size - 1) {
                            uploadPhoto()
                        } else {
                            val pic = uploadPicUri[uploadCurrentPicPosition]
                            mainViewModel?.clearPicResultValue()
                            mainViewModel?.postAttachment(
                                pic.uri,
                                requireContext(),
                                HomeViewModel.TYPE_PIC
                            )
                        }
                    } else {
                        uploadPicList[uploadCurrentPicPosition].id = it.result.toString()
                        uploadCurrentPicPosition += 1

                        if (uploadCurrentPicPosition > uploadPicList.size - 1) {
                            val mediaItem = MediaItem()
                            mediaItem.textContent = postMemberRequest.content
                            mediaItem.picParameter.addAll(picParameterList)
                            mediaItem.picParameter.addAll(uploadPicList)

                            val content = Gson().toJson(mediaItem)
                            Timber.d("Post pic content item : $content")

                            memberPostItem.postContent = content

                            val postId = arguments?.getLong(BasePostFragment.POST_ID)
                            mainViewModel?.clearLiveDataValue()
                            mainViewModel?.postPicOrVideo(postId!!, postMemberRequest, content, HomeViewModel.TYPE_PIC)
                        } else {
                            val pic = uploadPicList[uploadCurrentPicPosition]
                            mainViewModel?.clearPicResultValue()
                            mainViewModel?.postAttachment(
                                pic.url, requireContext(),
                                MyPostFragment.TYPE_PIC
                            )
                        }
                    }
                }
                is ApiResult.Error -> {
                    resetAndCancelJob(it.throwable, getString(R.string.post_error))
                }
            }
        })

        mainViewModel?.postPicMemberResult?.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResult.Success -> {
                    deleteTempFile()
                    if (deletePicList.isNotEmpty()) {
                        mainViewModel?.deleteAttachment(deletePicList[deleteCurrentPicPosition])
                    } else {
                        setSnackBarPostStatus(it.result)
                        mainViewModel?.clearLiveDataValue()
                    }
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.postVideoMemberResult?.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResult.Success -> {
                    deleteTempFile()
                    videoParameter = VideoParameter()

                  if (deleteVideoItem.isNotEmpty()) {
                        mainViewModel?.deleteVideoAttachment(
                            deleteVideoItem[0].picAttachmentId,
                            MyPostFragment.TYPE_PIC
                        )
                    } else {
                        setSnackBarPostStatus(it.result)
                        mainViewModel?.clearLiveDataValue()
                    }
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.uploadCoverItem?.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                return@Observer
            }

            arguments?.let { bundle ->
                val id = bundle.getLong(BasePostFragment.POST_ID, 0)

                if (id.toInt() == 0) {
                    picParameter = it
                } else {
                    uploadVideoList[0].ext = it.ext
                }
            }
        })

        mainViewModel?.postCoverResult?.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResult.Success -> {
                    picParameter.id = it.result.toString()

                    uploadVideoList[0].picAttachmentId = it.result.toString() // Update video

                    mainViewModel?.clearLiveDataValue()
                    val realPath =
                        UriUtils.getPath(requireContext(), Uri.parse(uploadVideoList[0].videoUrl))
                    uploadVideoList[0].videoUrl = realPath!!

                    val outPutPath = PostManager().getCompressPath(realPath, requireContext())
                    compressVideoAndUpload(realPath, outPutPath)
                }
                is ApiResult.Error -> {
                    resetAndCancelJob(it.throwable, getString(R.string.post_error))
                }
            }
        })

        mainViewModel?.postVideoResult?.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResult.Success -> {
                    arguments?.let { bundle ->
                        val id = bundle.getLong(BasePostFragment.POST_ID, 0)

                        if (id.toInt() == 0) {
                            videoParameter.id = it.result.toString()
                            videoParameter.length = uploadVideoList[0].length

                            val memberRequest = bundle.getParcelable<PostMemberRequest>(MEMBER_REQUEST)

                            val mediaItem = MediaItem()
                            mediaItem.picParameter.add(picParameter)
                            mediaItem.videoParameter = videoParameter
                            mediaItem.textContent = postMemberRequest.content
                            val content = Gson().toJson(mediaItem)
                            memberPostItem.postContent = content
                            postMemberRequest.title = memberRequest!!.title
                            Timber.d("Post video content item : $content")
                            mainViewModel?.clearLiveDataValue()
                            mainViewModel?.postPicOrVideo(request = postMemberRequest, content = content, type = HomeViewModel.TYPE_VIDEO)

                            postType = PostType.VIDEO
                        } else {
                            uploadVideoList[0].videoAttachmentId = it.result.toString()

                            val postId = arguments?.getLong(BasePostFragment.POST_ID)

                            videoParameter.id = uploadVideoList[0].videoAttachmentId
                            videoParameter.length = uploadVideoList[0].length

                            val memberRequest = bundle.getParcelable<PostMemberRequest>(MEMBER_REQUEST)

                            val picParameter = PicParameter(
                                id = uploadVideoList[0].picAttachmentId,
                                ext = uploadVideoList[0].ext
                            )

                            val mediaItem = MediaItem()
                            mediaItem.picParameter.add(picParameter)
                            mediaItem.videoParameter = videoParameter
                            mediaItem.textContent = postMemberRequest.content
                            val content = Gson().toJson(mediaItem)
                            postMemberRequest.type = memberRequest!!.type
                            postMemberRequest.title = memberRequest.title
                            postMemberRequest.content = content
                            postMemberRequest.tags = memberRequest.tags
                            memberPostItem.postContent = content
                            Timber.d("Post id : $postId")
                            Timber.d("Request : $postMemberRequest")
                            Timber.d("Post video content item : $content")
                            mainViewModel?.postPicOrVideo(postId!!, postMemberRequest, content, HomeViewModel.TYPE_VIDEO)
                        }
                    }
                }
                is ApiResult.Error -> {
                    resetAndCancelJob(it.throwable, getString(R.string.post_error))
                }
            }
        })

        mainViewModel?.postDeleteAttachment?.observe(viewLifecycleOwner, {
            deleteCurrentPicPosition += 1
            if (deleteCurrentPicPosition > deletePicList.size - 1) {
                setSnackBarPostStatus(postId)
                deleteCurrentPicPosition = 0
            } else {
                mainViewModel?.deleteAttachment(deletePicList[deleteCurrentPicPosition])
            }
        })

        mainViewModel?.postDeleteCoverAttachment?.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResult.Success -> mainViewModel?.deleteVideoAttachment(
                    deleteVideoItem[0].picAttachmentId,
                    MyPostViewModel.TYPE_VIDEO
                )
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.postDeleteVideoAttachment?.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResult.Success -> {
                    setSnackBarPostStatus(postId)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.uploadVideoParameter?.observe(viewLifecycleOwner, {
            videoParameter.ext = it
        })
    }

    private fun deleteTempFile() {
        try {
            if (uploadVideoList.isEmpty()) {
                return
            }
            val picPath = UriUtils.getPath(requireContext(), Uri.parse(uploadVideoList[0].picUrl))

            val videoFile = File(uploadVideoList[0].videoUrl)
            val picFile = File(picPath)
            videoFile.delete()
            picFile.delete()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun setPostTpe(type: PostType) {
        arguments?.remove(UPLOAD_ARTICLE)
        arguments?.remove(UPLOAD_PIC)
        arguments?.remove(UPLOAD_VIDEO)

        showSnackBar()
        postType = type
    }

    private fun combinationDataAndPostText(bundle: Bundle) {
        val title = bundle.getString(BasePostFragment.TITLE)
        val request = bundle.getString(BasePostFragment.REQUEST)
        val tags = bundle.getStringArrayList(BasePostFragment.TAG)
        val data = bundle.getSerializable(MyPostFragment.MEMBER_DATA)

        if (data != null) { //If update novel
            memberPostItem = data as MemberPostItem
        }

        memberPostItem.title = title!!
        memberPostItem.postContent = request!!
        memberPostItem.tags = tags

        mainViewModel?.postArticle(title, request, tags!!, memberPostItem)
    }

    fun handlePostClub() {
        mainViewModel?.uploadData?.value?.let {
            Timber.i("handlePostClub = $it")
            val isNeedArticleUpload = it.getBoolean(UPLOAD_ARTICLE, false)
            val isNeedPicUpload = it.getBoolean(UPLOAD_PIC, false)
            val isNeedVideoUpload = it.getBoolean(UPLOAD_VIDEO)
            mainViewModel?.uploadData?.value =null
            when {
                isNeedArticleUpload -> {
                    setPostTpe(PostType.TEXT)
                    combinationDataAndPostText(it)
                }
                isNeedPicUpload -> {
                    uploadPicFlow(it)
                }
                isNeedVideoUpload -> {
                    uploadVideoFlow(it)
                }
                else -> {

                }
            }
        }
    }

    private fun uploadPicFlow(bundle: Bundle) {
        setPostTpe(PostType.IMAGE)

        val memberRequest = bundle.getParcelable<PostMemberRequest>(MEMBER_REQUEST)
        val picUriList = bundle.getParcelableArrayList<PostAttachmentItem>(PIC_URI)

        postMemberRequest = memberRequest!!

        val data = bundle.getSerializable(MyPostFragment.MEMBER_DATA)

        if (data != null) {
            updatePicPostAttachment(bundle, data, picUriList!!)
        } else {
            handelNewPicPostAttachment(picUriList!!, memberRequest)
        }
    }

    private fun uploadVideoFlow(bundle: Bundle) {
        setPostTpe(PostType.VIDEO)

        val memberRequest = bundle.getParcelable<PostMemberRequest>(MEMBER_REQUEST)

        deleteVideoItem =
            bundle.getParcelableArrayList(BasePostFragment.DELETE_ATTACHMENT)!!
        uploadVideoList = bundle.getParcelableArrayList(BasePostFragment.VIDEO_DATA)!!
        postId = bundle.getLong(BasePostFragment.POST_ID, 0)

        if (postId.toInt() == 0) {
            postVideoCoverAttachment(memberRequest!!)
        } else {
            updateVideoPost(memberRequest!!)
        }
    }

    private fun handelNewPicPostAttachment(
        picUriList: ArrayList<PostAttachmentItem>,
        memberRequest: PostMemberRequest
    ) {
        uploadPicUri.addAll(picUriList)
        val pic = uploadPicUri[uploadCurrentPicPosition]
        mainViewModel?.postAttachment(pic.uri, requireContext(), HomeViewModel.TYPE_PIC)

        memberPostItem.title = memberRequest.title
        memberPostItem.tags = memberRequest.tags
    }

    private fun updatePicPostAttachment(
        bundle: Bundle,
        data: Serializable,
        picUriList: ArrayList<PostAttachmentItem>
    ) {
        deletePicList = bundle.getStringArrayList(BasePostFragment.DELETE_ATTACHMENT)!! // Delete pic attachment list
        memberPostItem = data as MemberPostItem
        postId = bundle.getLong(BasePostFragment.POST_ID)

        for (pic in picUriList) {
            if (pic.attachmentId.isBlank()) {
                uploadPicList.add(PicParameter(url = pic.uri))
            } else {
                val picParameter = PicParameter(id = pic.attachmentId, ext = pic.ext)
                picParameterList.add(picParameter)
            }
        }

        if (uploadPicList.isNotEmpty()) {
            val pic = uploadPicList[uploadCurrentPicPosition]
            mainViewModel?.postAttachment(pic.url, requireContext(), MyPostFragment.TYPE_PIC)
        } else {
            val mediaItem = MediaItem()

            for (pic in picUriList) {
                mediaItem.picParameter.add(
                    PicParameter(
                        id = pic.attachmentId,
                        ext = pic.ext
                    )
                )
            }

            mediaItem.textContent = postMemberRequest.content
            val content = Gson().toJson(mediaItem)

            val memberRequest = bundle.getParcelable<PostMemberRequest>(MEMBER_REQUEST)

            memberPostItem.title = memberRequest!!.title
            memberPostItem.postContent = content
            Timber.d("Post pic content item : $content")

            mainViewModel?.postPicOrVideo(postId, postMemberRequest, content, HomeViewModel.TYPE_PIC)
        }
    }

    private fun postVideoCoverAttachment(memberRequest: PostMemberRequest) {
        postMemberRequest = memberRequest
        memberPostItem.title = memberRequest.title
        memberPostItem.tags = memberRequest.tags
        mainViewModel?.postAttachment(
            uploadVideoList[0].picUrl, requireContext(),
            HomeViewModel.TYPE_COVER
        )
    }

    private fun updateVideoPost(memberRequest: PostMemberRequest) {
        if (uploadVideoList[0].picAttachmentId.isBlank()) {
            mainViewModel?.postAttachment(
                uploadVideoList[0].picUrl, requireContext(),
                MyPostViewModel.TYPE_COVER
            )
        } else {
            val mediaItem = MediaItem()

            val videoParameter = VideoParameter(
                id = uploadVideoList[0].videoAttachmentId,
                length = uploadVideoList[0].length
            )

            val picParameter = PicParameter(
                id = uploadVideoList[0].picAttachmentId,
                ext = uploadVideoList[0].ext
            )

            mediaItem.textContent = memberRequest.content
            mediaItem.videoParameter = videoParameter
            mediaItem.picParameter.add(picParameter)

            val content = Gson().toJson(mediaItem)
            memberPostItem.postContent = content
            Timber.d("Post video content item : $content")

            mainViewModel?.postPicOrVideo(postId, memberRequest, content, HomeViewModel.TYPE_VIDEO)
        }
    }

    abstract fun getLayoutId(): Int

    open fun setupObservers() {}

    open fun resetObservers() {}

    open fun setupListeners() {}

    open fun setupFirstTime() {}

    open fun statusBarVisibility() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    open fun setUpStatusBarColor() {
        activity?.also {
            (it as MainActivity).window.statusBarColor =
                requireContext().getColor(R.color.normal_color_status_bar)
        }
    }

    open fun initSettings() {}

    open val bottomNavigationVisibility: Int = View.VISIBLE

    open val isNavTransparent: Boolean = false
    open val isStatusBarDark: Boolean = false

    open fun navigateTo(item: NavigateItem) {
        lifecycleScope.launch {
            navigationTaskJoinOrRun {
                findNavController().also { navController ->
                    when (item) {
                        NavigateItem.Up -> navController.navigateUp()
                        is NavigateItem.PopBackStack -> navController.popBackStack(
                            item.fragmentId,
                            item.inclusive
                        )
                        is NavigateItem.Destination -> {
                            if (item.bundle == null) {
                                navController.navigate(item.action)
                            } else {
                                navController.navigate(item.action, item.bundle)
                            }
                        }
                    }
                }
                delay(1000L)
            }
        }
    }

    private var navigationTask: Deferred<Any>? = null

    private suspend fun navigationTaskJoinOrRun(block: suspend () -> Any): Any {
        navigationTask?.let {
            return it.await()
        }

        return coroutineScope {
            val newTask = async {
                block()
            }

            newTask.invokeOnCompletion {
                navigationTask = null
            }

            navigationTask = newTask
            newTask.await()
        }
    }

    fun backToDesktop() {
        activity?.moveTaskToBack(true)
    }

    fun useAdultTheme(value: Boolean) {
//        activity?.also {
//            (it as MainActivity).setAdult(value)
//        }
    }

    open fun onApiError(
        throwable: Throwable,
        onHttpErrorBlock: ((ExceptionResult.HttpError) -> Unit)? = null
    ) {
        when (val errorHandler =
            throwable.handleException { ex -> mainViewModel?.processException(ex) }) {
            is ExceptionResult.RefreshTokenExpired -> logoutLocal()
            is ExceptionResult.HttpError -> {
                if (onHttpErrorBlock == null) handleHttpError(errorHandler)
                else onHttpErrorBlock(errorHandler)
            }
            is ExceptionResult.Crash -> {
                if (errorHandler.throwable is UnknownHostException) {
                    showCrashDialog(HttpErrorMsgType.CHECK_NETWORK)
                } else if(errorHandler.throwable is NoSuchElementException) {
                    Timber.e("${errorHandler.throwable}")
                } else {
                    showToast(requireContext(), errorHandler.throwable.toString())
                }
            }
        }
    }

    open fun handleHttpError(errorHandler: ExceptionResult.HttpError) {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.error_device_binding_title,
                message = errorHandler.httpExceptionItem.errorItem.toString(),
                messageIcon = R.drawable.ico_default_photo,
                secondBtn = getString(R.string.btn_confirm)
            )
        ).show(requireActivity().supportFragmentManager)
    }

    private fun showCrashDialog(type: HttpErrorMsgType = HttpErrorMsgType.API_FAILED) {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.error_device_binding_title,
                message = when (type) {
                    HttpErrorMsgType.API_FAILED -> getString(R.string.api_failed_msg)
                    HttpErrorMsgType.CHECK_NETWORK -> getString(R.string.server_error)
                },
                messageIcon = R.drawable.ico_default_photo,
                secondBtn = getString(R.string.btn_close)
            )
        ).show(requireActivity().supportFragmentManager)
    }

    fun getNotGrantedPermissions(permissions: Array<String>): ArrayList<String> {
        val requestList = arrayListOf<String>()
        permissions.indices.forEach {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permissions[it]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestList += permissions[it]
            }
        }
        return requestList
    }

    private fun logoutLocal() {
        view?.let {
            mainViewModel?.logoutLocal()
        }
    }

    fun checkStatus(onConfirmed: () -> Unit) {
        mainViewModel?.checkStatus(onConfirmed)
    }

    private fun showSnackBar() {
        mainViewModel?.setIsShowSnackBar(true)
        snackBar = PostManager().showSnackBar(
            snackBarLayout,
            this,
            object : PostManager.CancelDialogListener {
                override fun onCancel() {
                    cancelDialog()
                }
            })
    }

    private fun cancelDialog() {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.whether_to_discard_content,
                messageIcon = R.drawable.ico_default_photo,
                firstBtn = getString(R.string.btn_cancel),
                secondBtn = getString(R.string.btn_confirm),
                isMessageIcon = false,
                secondBlock = {
                    resetAndCancelJob()
                }
            )
        ).show(requireActivity().supportFragmentManager)
    }

    private fun resetAndCancelJob(t: Throwable = Throwable(), msg: String = "") {
        if (t.message != null) {
            onApiError(t)
        }

        mainViewModel?.cancelJob()
        snackBar?.dismiss()
        uploadCurrentPicPosition = 0
        uploadPicUri.clear()
        Timber.e(t)

        mainViewModel?.setIsShowSnackBar(false)

        if (msg.isNotBlank()) {
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadPhoto() {
        val mediaItem = MediaItem()
        mediaItem.textContent = postMemberRequest.content

        for (item in uploadPicItem) {
            mediaItem.picParameter.add(
                PicParameter(
                    id = item.id.toString(),
                    ext = item.ext
                )
            )
        }

        val content = Gson().toJson(mediaItem)
        Timber.d("Post pic content item : $content")
        memberPostItem.postContent = content
        mainViewModel?.clearLiveDataValue()
        mainViewModel?.postPicOrVideo(request = postMemberRequest, content = content, type = HomeViewModel.TYPE_PIC)
    }

    private fun compressVideoAndUpload(realPath: String, outPutPath: String) {
        PostManager().videoCompress(
            realPath,
            outPutPath,
            object : PostManager.VideoCompressListener {
                override fun onSuccess() {
                    uploadVideoList[0].videoUrl = outPutPath
                    mainViewModel?.postAttachment(
                        uploadVideoList[0].videoUrl,
                        requireContext(),
                        HomeViewModel.TYPE_VIDEO
                    )
                }

                override fun onFail() {
                    resetAndCancelJob(Throwable(), getString(R.string.post_error))
                }
            })
    }

    private fun setSnackBarPostStatus(postId: Long = 0) {
        if (snackBar == null) {
            return
        }

        mainViewModel?.setIsShowSnackBar(false)

        PostManager().dismissSnackBar(
            snackBar!!,
            postId,
            memberPostItem,
            mainViewModel,
            object : PostManager.SnackBarListener {
                override fun onClick(memberPostItem: MemberPostItem) {
                    postNavigation(memberPostItem)
                }
            })

        uploadCurrentPicPosition = 0
        uploadPicUri.clear()
    }

    private fun postNavigation(memberPostItem: MemberPostItem) {
        when (postType) {
            PostType.TEXT -> {
                val bundle = ClubTextFragment.createBundle(memberPostItem)
                navigationToText(bundle)
            }

            PostType.IMAGE -> {
                val bundle = ClubPicFragment.createBundle(memberPostItem)
                navigationToPicture(bundle)
            }

            PostType.VIDEO -> {
                val bundle = ClipPlayerFragment.createBundle(memberPostItem.id)
                navigationToClip(bundle)
            }
        }
    }

    open fun navigationToText(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_clubTextFragment,
                bundle
            )
        )
    }

    open fun navigationToPicture(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_clubPicFragment,
                bundle
            )
        )
    }

    open fun navigationToClip(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_clipPlayerFragment,
                bundle
            )
        )
    }

    fun onMoreClick(
        item: MemberPostItem,
        position: Int,
        deducted: Boolean = true,
        isFromPostPage: Boolean = false,
        onEdit: (BaseMemberPostItem) -> Unit,
    ) {
        val isMe = mainViewModel?.accountManager?.getProfile()?.userId == item.creatorId
        if (isMe) {
            showMeMoreDialog(item, position, onEdit)
        } else {
            if (deducted)
                showMoreDialog(item, isFromPostPage)
        }
    }

    private var meMoreDialog: MyPostMoreDialogFragment? = null
    private fun showMeMoreDialog(
        item: MemberPostItem,
        position: Int,
        onEdit: (BaseMemberPostItem) -> Unit
    ) {
        val onMeMoreDialogListener = object : OnMeMoreDialogListener {
            override fun onCancel() {
                meMoreDialog?.dismiss()
            }

            override fun onDelete(item: BaseMemberPostItem) {
                GeneralDialog.newInstance(
                    GeneralDialogData(
                        titleRes = R.string.is_post_delete,
                        messageIcon = R.drawable.ico_default_photo,
                        secondBtn = getString(R.string.btn_confirm),
                        secondBlock = { mainViewModel?.deletePost(item as MemberPostItem, position) },
                        firstBtn = getString(R.string.cancel),
                        isMessageIcon = false
                    )
                ).show(requireActivity().supportFragmentManager)
            }

            override fun onEdit(item: BaseMemberPostItem) {
                onEdit(item)
                meMoreDialog?.dismiss()
            }
        }
        meMoreDialog = MyPostMoreDialogFragment.newInstance(item, onMeMoreDialogListener)
            .also {
                it.show(
                    requireActivity().supportFragmentManager,
                    MoreDialogFragment::class.java.simpleName
                )
            }
    }

    private fun showMoreDialog(item: MemberPostItem, isFromPostPage: Boolean) {
        val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
            override fun onProblemReport(item: BaseMemberPostItem, isComment: Boolean) {
                moreDialog?.dismiss()
                (requireActivity() as MainActivity).showReportDialog(
                        item,
                        isComment = isComment
                )
            }

            override fun onCancel() {
                moreDialog?.dismiss()
            }
        }

        val isLogin = mainViewModel?.checkIsLogin()
        moreDialog = MoreDialogFragment.newInstance(item, onMoreDialogListener, isLogin = isLogin!!, isFromPostPage = isFromPostPage).also {
            it.show(
                requireActivity().supportFragmentManager,
                MoreDialogFragment::class.java.simpleName
            )
        }
    }
}
