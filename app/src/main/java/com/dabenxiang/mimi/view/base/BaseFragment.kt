package com.dabenxiang.mimi.view.base

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.HttpErrorMsgType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PostAttachmentItem
import com.dabenxiang.mimi.model.vo.PostVideoAttachment
import com.dabenxiang.mimi.view.clip.ClipFragment
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
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.MEMBER_REQUEST
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.PIC_URI
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.UPLOAD_ARTICLE
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.UPLOAD_PIC
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.UPLOAD_VIDEO
import com.dabenxiang.mimi.view.post.utility.PostManager
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils.showToast
import com.dabenxiang.mimi.widget.utility.UriUtils
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.net.UnknownHostException

abstract class BaseFragment : Fragment() {

    open var mainViewModel: MainViewModel? = null
    var progressHUD: KProgressHUD? = null

    var mView: View? = null
    var firstCreateView = false

    var snackBar: Snackbar? = null

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

        if (arguments?.getBoolean(PlayerActivity.KEY_IS_FROM_PLAYER) == true) {
            mainViewModel?.isFromPlayer = true
        }

        handleBackStackData()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mainViewModel?.postArticleResult?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    postType = PostType.TEXT
                    setSnackBarPostStatus(it.result)
                    mainViewModel?.clearLiveDataValue()

                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.uploadPicItem?.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                return@Observer
            }
            Log.d("arvin", "uploadPicItem = " + it)
            val data = arguments?.getSerializable(MyPostFragment.MEMBER_DATA)
            if (data == null) {
                uploadPicItem.add(it)
            } else {
                uploadPicList[uploadCurrentPicPosition] = it
            }
        })

        mainViewModel?.postPicResult?.observe(viewLifecycleOwner, Observer {
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
                            mainViewModel?.postAttachment(pic.uri, requireContext(), HomeViewModel.TYPE_PIC)
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


                            val postId = arguments?.getLong(BasePostFragment.POST_ID)

                            mainViewModel?.postPic(postId!!, postMemberRequest, content)
                        } else {
                            val pic = uploadPicList[uploadCurrentPicPosition]
                            mainViewModel?.postAttachment(pic.url, requireContext(),
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

        mainViewModel?.postVideoMemberResult?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    if (deletePicList.isNotEmpty()) {
                        mainViewModel?.deleteAttachment(deletePicList[deleteCurrentPicPosition])
                    } else if (deleteVideoItem.isNotEmpty()) {
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

        mainViewModel?.postCoverResult?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    picParameter.id = it.result.toString()
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

        mainViewModel?.postVideoResult?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    arguments?.let { bundle ->
                        val id = bundle.getLong(BasePostFragment.POST_ID, 0)

                        if (id.toInt() == 0) {
                            val mediaItem = MediaItem()
                            val videoParameter = VideoParameter(
                                id = it.result.toString(),
                                length = uploadVideoList[0].length
                            )
                            mediaItem.picParameter.add(picParameter)
                            mediaItem.videoParameter = videoParameter
                            mediaItem.textContent = postMemberRequest.content
                            val content = Gson().toJson(mediaItem)
                            memberPostItem.content = content
                            Timber.d("Post video content item : $content")
                            mainViewModel?.clearLiveDataValue()
                            mainViewModel?.postPic(request = postMemberRequest, content = content)

                            postType = PostType.VIDEO
                        } else {
                            uploadVideoList[0].videoAttachmentId = it.result.toString()

                            val postId = arguments?.getLong(BasePostFragment.POST_ID)

                            val mediaItem = MediaItem()
                            val videoParameter = VideoParameter(
                                id = uploadVideoList[0].videoAttachmentId,
                                length = uploadVideoList[0].length
                            )

                            val picParameter = PicParameter(
                                id = uploadVideoList[0].picAttachmentId,
                                ext = uploadVideoList[0].ext
                            )

                            mediaItem.picParameter.add(picParameter)
                            mediaItem.videoParameter = videoParameter
                            mediaItem.textContent = postMemberRequest.content
                            val content = Gson().toJson(mediaItem)
                            memberPostItem.content = content
                            Timber.d("Post id : $postId")
                            Timber.d("Request : $postMemberRequest")
                            Timber.d("Post video content item : $content")
                            mainViewModel?.postPic(postId!!, postMemberRequest, content)
                        }
                    }
                }
                is ApiResult.Error -> {
                    resetAndCancelJob(it.throwable, getString(R.string.post_error))
                }
            }
        })

        mainViewModel?.postDeleteAttachment?.observe(viewLifecycleOwner, Observer {
            deleteCurrentPicPosition += 1
            if (deleteCurrentPicPosition > deletePicList.size - 1) {
                setSnackBarPostStatus(postId)
            } else {
                mainViewModel?.deleteAttachment(deletePicList[deleteCurrentPicPosition])
            }
        })

        mainViewModel?.postDeleteCoverAttachment?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> mainViewModel?.deleteVideoAttachment(
                    deleteVideoItem[0].picAttachmentId,
                    MyPostViewModel.TYPE_VIDEO
                )
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.postDeleteVideoAttachment?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> setSnackBarPostStatus(postId)
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })
    }

    private fun handleBackStackData() {
        arguments?.let {
            val isNeedArticleUpload = it.getBoolean(UPLOAD_ARTICLE, false)
            val isNeedPicUpload = it.getBoolean(UPLOAD_PIC, false)
            val isNeedVideoUpload = it.getBoolean(UPLOAD_VIDEO)

            when {
                isNeedArticleUpload -> {
                    it.remove(UPLOAD_ARTICLE)
                    showSnackBar()

                    val title = it.getString(BasePostFragment.TITLE)
                    val request = it.getString(BasePostFragment.REQUEST)
                    val tags = it.getStringArrayList(BasePostFragment.TAG)
                    val data = it.getSerializable(MyPostFragment.MEMBER_DATA)

                    memberPostItem.title = title!!
                    memberPostItem.content = request!!
                    memberPostItem.tags = tags

                    if (data != null) {
                        memberPostItem = data as MemberPostItem
                    }
                    mainViewModel?.postArticle(title, request, tags!!, memberPostItem)
                }
                isNeedPicUpload -> {
                    it.remove(UPLOAD_PIC)

                    showSnackBar()
                    postType = PostType.IMAGE

                    val memberRequest = it.getParcelable<PostMemberRequest>(MEMBER_REQUEST)
                    val picUriList = it.getParcelableArrayList<PostAttachmentItem>(PIC_URI)

                    postMemberRequest = memberRequest!!

                    val data = it.getSerializable(MyPostFragment.MEMBER_DATA)

                    if (data != null) {
                        deletePicList = it.getStringArrayList(BasePostFragment.DELETE_ATTACHMENT)!!
                        memberPostItem = data as MemberPostItem
                        postId = it.getLong(BasePostFragment.POST_ID)

                        for (pic in picUriList!!) {
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
                            memberPostItem.content = content
                            Timber.d("Post pic content item : $content")

                            mainViewModel?.postPic(postId, postMemberRequest, content)
                        }
                    } else {
                        uploadPicUri.addAll(picUriList!!)
                        val pic = uploadPicUri[uploadCurrentPicPosition]
                        mainViewModel?.postAttachment(pic.uri, requireContext(), HomeViewModel.TYPE_PIC)

                        memberPostItem.title = memberRequest.title
                        memberPostItem.tags = memberRequest.tags
                    }
                }
                isNeedVideoUpload -> {
                    it.remove(UPLOAD_VIDEO)
                    postType = PostType.VIDEO
                    showSnackBar()

                    val memberRequest = it.getParcelable<PostMemberRequest>(MEMBER_REQUEST)

                    deleteVideoItem = it.getParcelableArrayList(BasePostFragment.DELETE_ATTACHMENT)!!
                    uploadVideoList = it.getParcelableArrayList(BasePostFragment.VIDEO_DATA)!!
                    postId = it.getLong(BasePostFragment.POST_ID, 0)

                    if (postId.toInt() == 0) {
                        postMemberRequest = memberRequest!!
                        memberPostItem.title = memberRequest.title
                        memberPostItem.tags = memberRequest.tags
                        mainViewModel?.postAttachment(uploadVideoList[0].picUrl, requireContext(),
                            HomeViewModel.TYPE_COVER
                        )
                    } else {
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

                            mediaItem.textContent = postMemberRequest.content
                            mediaItem.videoParameter = videoParameter
                            mediaItem.picParameter.add(picParameter)

                            mediaItem.textContent = postMemberRequest.content
                            val content = Gson().toJson(mediaItem)
                            memberPostItem.content = content
                            Timber.d("Post video content item : $content")

                            mainViewModel?.postPic(postId, postMemberRequest, content)
                        }
                    }
                }
                else -> {

                }
            }
        }
    }

    abstract fun getLayoutId(): Int

    abstract fun setupObservers()

    abstract fun setupListeners()

    open fun setupFirstTime() {}

    open fun statusBarVisibility() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    open fun initSettings() {}

    open val bottomNavigationVisibility: Int = View.VISIBLE

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
        activity?.also {
            (it as MainActivity).setAdult(value)
        }
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
        onApiError(t)
        mainViewModel?.cancelJob()
        snackBar?.dismiss()
        uploadCurrentPicPosition = 0
        uploadPicUri.clear()
        Timber.e(t)

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
        memberPostItem.content = content
        mainViewModel?.clearLiveDataValue()
        mainViewModel?.postPic(request = postMemberRequest, content = content)
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
                val bundle = TextDetailFragment.createBundle(memberPostItem, -1)
                navigationToText(bundle)
            }

            PostType.IMAGE -> {
                val bundle = PictureDetailFragment.createBundle(memberPostItem, -1)
                navigationToPicture(bundle)
            }

            PostType.VIDEO -> {
                val bundle = ClipFragment.createBundle(arrayListOf(memberPostItem), 0, false)
                navigationToClip(bundle)
            }
        }
    }

    private fun navigationToText(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_adultHomeFragment_to_textDetailFragment,
                bundle
            )
        )
    }

    private fun navigationToPicture(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_adultHomeFragment_to_pictureDetailFragment,
                bundle
            )
        )
    }

    private fun navigationToClip(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_adultHomeFragment_to_clipFragment,
                bundle
            )
        )
    }

    fun onMoreClick(item: MemberPostItem, items: ArrayList<MemberPostItem>, onEdit: (BaseMemberPostItem) -> Unit){
        val isMe = mainViewModel?.accountManager?.getProfile()?.userId == item.creatorId
        if (isMe) {
            showMeMoreDialog(item, items, onEdit)
        } else {
            showMoreDialog(item)
        }
    }

    private var meMoreDialog: MyPostMoreDialogFragment? = null
    private fun showMeMoreDialog(
        item: MemberPostItem,
        items: ArrayList<MemberPostItem>,
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
                        secondBlock = { mainViewModel?.deletePost(item as MemberPostItem, items) },
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

    private var moreDialog: MoreDialogFragment? = null
    private fun showMoreDialog(item: MemberPostItem){
        val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
            override fun onProblemReport(item: BaseMemberPostItem) {
                moreDialog?.dismiss()
                checkStatus { (requireActivity() as MainActivity).showReportDialog(item) }
            }

            override fun onCancel() {
                moreDialog?.dismiss()
            }
        }
        moreDialog = MoreDialogFragment.newInstance(item, onMoreDialogListener).also {
            it.show(
                requireActivity().supportFragmentManager,
                MoreDialogFragment::class.java.simpleName
            )
        }
    }
}
