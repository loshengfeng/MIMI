package com.dabenxiang.mimi.view.mypost

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PostAttachmentItem
import com.dabenxiang.mimi.model.vo.PostVideoAttachment
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.adapter.MyPostPagedAdapter
import com.dabenxiang.mimi.view.adapter.viewHolder.PicturePostHolder
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.mypost.MyPostViewModel.Companion.TYPE_COVER
import com.dabenxiang.mimi.view.mypost.MyPostViewModel.Companion.TYPE_VIDEO
import com.dabenxiang.mimi.view.mypost.MyPostViewModel.Companion.USER_ID_ME
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.DELETE_ATTACHMENT
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.MEMBER_REQUEST
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.PIC_URI
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.POST_ID
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.REQUEST
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.TAG
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.TITLE
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.UPLOAD_ARTICLE
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.UPLOAD_PIC
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.UPLOAD_VIDEO
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.VIDEO_DATA
import com.dabenxiang.mimi.view.post.utility.PostManager
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.dabenxiang.mimi.widget.utility.UriUtils
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_my_post.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import timber.log.Timber


class MyPostFragment : BaseFragment() {

    private lateinit var adapter: MyPostPagedAdapter

    private val viewModel: MyPostViewModel by viewModels()

    private val picParameterList = arrayListOf<PicParameter>()
    private val uploadPicList = arrayListOf<PicParameter>()
    private var deletePicList = arrayListOf<String>()
    private var uploadVideoList = arrayListOf<PostVideoAttachment>()
    private var deleteVideoItem = arrayListOf<PostVideoAttachment>()

    private var postMemberRequest = PostMemberRequest()

    private var uploadCurrentPicPosition = 0
    private var deleteCurrentPicPosition = 0

    private var snackBar: Snackbar? = null

    private var userId: Long = USER_ID_ME
    private var userName: String = ""
    private var isAdult: Boolean = true
    private var isAdultTheme: Boolean = false

    private var memberPostItem = MemberPostItem()
    private var postType = PostType.TEXT
    private var postId: Long = 0

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    companion object {
        const val EDIT = "edit"
        const val MEMBER_DATA = "member_data"
        const val TYPE_PIC = "type_pic"

        private const val KEY_USER_ID = "KEY_USER_ID"
        private const val KEY_USER_NAME = "KEY_USER_NAME"
        private const val KEY_IS_ADULT = "KEY_IS_ADULT"
        private const val KEY_IS_ADULT_THEME = "KEY_IS_ADULT_THEME"

        fun createBundle(
            userId: Long,
            userName: String,
            isAdult: Boolean,
            isAdultTheme: Boolean
        ): Bundle {
            return Bundle().also {
                it.putLong(KEY_USER_ID, userId)
                it.putString(KEY_USER_NAME, userName)
                it.putBoolean(KEY_IS_ADULT, isAdult)
                it.putBoolean(KEY_IS_ADULT_THEME, isAdultTheme)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_my_post
    }

    override fun setupFirstTime() {
        initSettings()
        viewModel.getMyPost(userId, isAdult)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        useAdultTheme(isAdultTheme)
        requireActivity().onBackPressedDispatcher.addCallback {
            navigateTo(NavigateItem.Up)
        }
    }

    override fun initSettings() {
        arguments?.let {
            userId = it.getLong(KEY_USER_ID, USER_ID_ME)
            userName = it.getString(KEY_USER_NAME, "")
            isAdult = it.getBoolean(KEY_IS_ADULT, true)
            isAdultTheme = it.getBoolean(KEY_IS_ADULT_THEME, false)
        }

        adapter = MyPostPagedAdapter(
            requireContext(),
            isAdultTheme,
            myPostListener,
            attachmentListener,
            memberPostFuncItem
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        cl_layout_bg.isSelected = isAdultTheme
        cl_bg.isSelected = isAdultTheme
        tv_title.text = if (userId == USER_ID_ME) getString(R.string.personal_my_post) else userName
        tv_title.isSelected = isAdultTheme
        tv_back.isSelected = isAdultTheme
        if (isAdultTheme) layout_refresh.setColorSchemeColors(requireContext().getColor(R.color.color_red_1))

        handleUpdatePost()
    }

    private fun handleUpdatePost() {
        val isNeedUpdateArticle = arguments?.getBoolean(UPLOAD_ARTICLE)

        val isNeedPicUpload = arguments?.getBoolean(UPLOAD_PIC)
        val isNeedVideoUpload = arguments?.getBoolean(UPLOAD_VIDEO)

        if (isNeedPicUpload != null && isNeedPicUpload) {
            handlePicUpload()
        } else if (isNeedVideoUpload != null && isNeedVideoUpload) {
            handleVideoUpload()
        } else if (isNeedUpdateArticle != null && isNeedUpdateArticle) {
            arguments?.remove(UPLOAD_ARTICLE)
            showSnackBar()
            val title = arguments?.getString(TITLE)
            val request = arguments?.getString(REQUEST)
            val tags = arguments?.getStringArrayList(TAG)
            memberPostItem = arguments?.getSerializable(MEMBER_DATA) as MemberPostItem
            viewModel.updateArticle(title!!, request!!, tags!!, memberPostItem)
        }
    }

    private fun handlePicUpload() {
        arguments?.remove(UPLOAD_PIC)
        postType = PostType.IMAGE

        showSnackBar()

        deletePicList = arguments?.getStringArrayList(DELETE_ATTACHMENT)!!
        memberPostItem = arguments?.getSerializable(MEMBER_DATA) as MemberPostItem
        postId = arguments?.getLong(POST_ID)!!

        val memberRequest =
            arguments?.getParcelable<PostMemberRequest>(MEMBER_REQUEST)
        val picList = arguments?.getParcelableArrayList<PostAttachmentItem>(PIC_URI)

        postMemberRequest = memberRequest!!

        for (pic in picList!!) {
            if (pic.attachmentId.isBlank()) {
                uploadPicList.add(PicParameter(url = pic.uri))
            } else {
                val picParameter = PicParameter(id = pic.attachmentId, ext = pic.ext)
                picParameterList.add(picParameter)
            }
        }

        if (uploadPicList.isNotEmpty()) {
            val pic = uploadPicList[uploadCurrentPicPosition]
            viewModel.postAttachment(pic.url, requireContext(), MyPostFragment.TYPE_PIC)
        } else {
            val mediaItem = MediaItem()

            for (pic in picList) {
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

            viewModel.postPic(postId, postMemberRequest, content)
        }
    }

    private fun handleVideoUpload() {
        arguments?.remove(UPLOAD_VIDEO)
        postType = PostType.VIDEO

        arguments?.remove(UPLOAD_VIDEO)
        showSnackBar()

        deleteVideoItem = arguments?.getParcelableArrayList(DELETE_ATTACHMENT)!!
        uploadVideoList = arguments?.getParcelableArrayList(VIDEO_DATA)!!
        memberPostItem = arguments?.getSerializable(MEMBER_DATA) as MemberPostItem
        postId = arguments?.getLong(POST_ID)!!

        val memberRequest =
            arguments?.getParcelable<PostMemberRequest>(MEMBER_REQUEST)

        postMemberRequest = memberRequest!!

        if (uploadVideoList[0].picAttachmentId.isBlank()) {
            viewModel.postAttachment(
                uploadVideoList[0].picUrl, requireContext(),
                TYPE_COVER
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

            viewModel.postPic(postId, postMemberRequest, content)
        }
    }

    override fun setupObservers() {
        viewModel.showProgress.observe(this, Observer {
            layout_refresh.isRefreshing = it
        })

        viewModel.myPostItemListResult.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        viewModel.attachmentByTypeResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    val attachmentItem = it.result
                    LruCacheUtils.putLruCache(attachmentItem.id!!, attachmentItem.bitmap!!)

                    when (attachmentItem.type) {
                        AttachmentType.ADULT_TAB_CLIP,
                        AttachmentType.ADULT_TAB_PICTURE,
                        AttachmentType.ADULT_TAB_TEXT -> {
                            adapter.notifyItemChanged(attachmentItem.position!!)
                        }
                        else -> {
                        }
                    }

                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.attachmentResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    val attachmentItem = it.result
                    LruCacheUtils.putLruCache(attachmentItem.id!!, attachmentItem.bitmap!!)
                    when (val holder =
                        adapter.viewHolderMap[attachmentItem.parentPosition]) {
                        is PicturePostHolder -> {
                            if (holder.pictureRecycler.tag == attachmentItem.parentPosition) {
                                adapter.updateInternalItem(holder)
                            }
                        }
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.likePostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    adapter.notifyItemChanged(
                        it.result,
                        MyPostPagedAdapter.PAYLOAD_UPDATE_LIKE
                    )
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.favoriteResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    adapter.notifyItemChanged(
                        it.result,
                        MyPostPagedAdapter.PAYLOAD_UPDATE_FAVORITE
                    )
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.followResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Empty -> {
                    adapter.notifyItemRangeChanged(
                        0,
                        viewModel.totalCount,
                        MyPostPagedAdapter.PAYLOAD_UPDATE_FOLLOW
                    )
                }
                is Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.deletePostResult?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    adapter.removedPosList.add(it.result)
                    adapter.notifyItemChanged(it.result)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.cleanRemovedPosList.observe(viewLifecycleOwner, Observer {
            adapter.removedPosList.clear()
        })

        viewModel.uploadPicItem.observe(viewLifecycleOwner, Observer {
            uploadPicList[uploadCurrentPicPosition] = it
        })

        viewModel.postPicResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    uploadPicList[uploadCurrentPicPosition].id = it.result.toString()
                    uploadCurrentPicPosition += 1

                    if (uploadCurrentPicPosition > uploadPicList.size - 1) {
                        val mediaItem = MediaItem()
                        mediaItem.textContent = postMemberRequest.content
                        mediaItem.picParameter.addAll(picParameterList)
                        mediaItem.picParameter.addAll(uploadPicList)

                        val content = Gson().toJson(mediaItem)
                        Timber.d("Post pic content item : $content")


                        val postId = arguments?.getLong(POST_ID)

                        viewModel.postPic(postId!!, postMemberRequest, content)
                    } else {
                        val pic = uploadPicList[uploadCurrentPicPosition]
                        viewModel.postAttachment(pic.url, requireContext(), TYPE_PIC)
                    }
                }
                is Error -> resetAndCancelJob(
                    it.throwable,
                    getString(R.string.post_error)
                )
            }
        })

        viewModel.postVideoMemberResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    if (deletePicList.isNotEmpty()) {
                        viewModel.deleteAttachment(deletePicList[deleteCurrentPicPosition])
                    } else if (deleteVideoItem.isNotEmpty()) {
                        viewModel.deleteVideoAttachment(
                            deleteVideoItem[0].picAttachmentId,
                            TYPE_PIC
                        )
                    } else {
                        setSnackBarPostStatus(it.result)
                        viewModel.clearLiveData()
                    }
                }
                is Error -> resetAndCancelJob(
                    it.throwable,
                    getString(R.string.post_error)
                )
            }
        })

        viewModel.postDeleteAttachment.observe(viewLifecycleOwner, Observer {
            deleteCurrentPicPosition += 1
            if (deleteCurrentPicPosition > deletePicList.size - 1) {
                setSnackBarPostStatus(postId)
            } else {
                viewModel.deleteAttachment(deletePicList[deleteCurrentPicPosition])
            }
        })

        viewModel.uploadCoverItem.observe(viewLifecycleOwner, Observer {
            uploadVideoList[0].ext = it.ext
        })

        viewModel.postCoverResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    uploadVideoList[0].picAttachmentId = it.result.toString()

                    val realPath =
                        UriUtils.getPath(requireContext(), Uri.parse(uploadVideoList[0].videoUrl))
                    uploadVideoList[0].videoUrl = realPath!!

                    val outPutPath = PostManager().getCompressPath(realPath, requireContext())
                    PostManager().videoCompress(
                        realPath,
                        outPutPath,
                        object : PostManager.VideoCompressListener {
                            override fun onSuccess() {
                                uploadVideoList[0].videoUrl = outPutPath
                                viewModel.postAttachment(
                                    uploadVideoList[0].videoUrl,
                                    requireContext(),
                                    TYPE_VIDEO
                                )
                            }

                            override fun onFail() {
                                resetAndCancelJob(Throwable(), getString(R.string.post_error))
                            }
                        })
                }
                is Error -> resetAndCancelJob(
                    it.throwable,
                    getString(R.string.post_error)
                )
            }
        })

        viewModel.postVideoResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    uploadVideoList[0].videoAttachmentId = it.result.toString()

                    val postId = arguments?.getLong(POST_ID)

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
                    viewModel.postPic(postId!!, postMemberRequest, content)
                }
                is Error -> {
                    Timber.e(it.throwable)

                    resetAndCancelJob(it.throwable, getString(R.string.post_error))

                }
            }
        })

        viewModel.postDeleteCoverAttachment.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> viewModel.deleteVideoAttachment(
                    deleteVideoItem[0].picAttachmentId,
                    TYPE_VIDEO
                )
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.postDeleteVideoAttachment.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> setSnackBarPostStatus(postId)
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.postArticleResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    postType = PostType.TEXT
                    setSnackBarPostStatus(it.result)
                    viewModel.clearLiveData()
                }
                is Error -> onApiError(it.throwable)
            }
        })
    }

    private fun setSnackBarPostStatus(postId: Long = 0) {
        PostManager().dismissSnackBar(
            snackBar!!,
            postId,
            memberPostItem,
            null,
            object : PostManager.SnackBarListener {
                override fun onClick(memberPostItem: MemberPostItem) {
                    postNavigation(memberPostItem)
                }
            })

        uploadCurrentPicPosition = 0
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
                memberPostItem.postFriendlyName = viewModel.pref.profileItem.account
                val bundle = ClipFragment.createBundle(arrayListOf(memberPostItem), -1, false)
                navigationToVideo(bundle)
            }
        }
    }

    private fun navigationToText(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_myPostFragment_to_textDetailFragment,
                bundle
            )
        )
    }

    private fun navigationToPicture(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_myPostFragment_to_pictureDetailFragment,
                bundle
            )
        )
    }

    private fun navigationToVideo(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_myPostFragment_to_clipFragment,
                bundle
            )
        )
    }

    override fun setupListeners() {
        View.OnClickListener { btnView ->
            when (btnView.id) {
                R.id.tv_back -> {
                    if (mainViewModel?.isFromPlayer == true)
                        activity?.onBackPressed()
                    else navigateTo(NavigateItem.Up)
                }
            }
        }.also {
            tv_back.setOnClickListener(it)
        }

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.getMyPost(userId, isAdult)
        }
    }

    private val attachmentListener = object : AttachmentListener {
        override fun onGetAttachment(id: String, position: Int, type: AttachmentType) {
            viewModel.getAttachment(id, position, type)
        }

        override fun onGetAttachment(id: String, parentPosition: Int, position: Int) {
            viewModel.getAttachment(id, parentPosition, position)
        }
    }

    private val myPostListener = object : MyPostListener {
        override fun onMoreClick(item: MemberPostItem) {
            onMoreClick(item, ArrayList(adapter.currentList as List<MemberPostItem>), onEdit = {
                it as MemberPostItem
                when (item.type) {
                    PostType.TEXT -> {
                        val bundle = Bundle()
                        item.id
                        bundle.putBoolean(EDIT, true)
                        bundle.putSerializable(MEMBER_DATA, item)
                        findNavController().navigate(
                            R.id.action_myPostFragment_to_postArticleFragment,
                            bundle
                        )
                    }
                    PostType.IMAGE -> {
                        val bundle = Bundle()
                        bundle.putBoolean(EDIT, true)
                        bundle.putSerializable(MEMBER_DATA, item)
                        findNavController().navigate(
                            R.id.action_myPostFragment_to_postPicFragment,
                            bundle
                        )
                    }
                    PostType.VIDEO -> {
                        val bundle = Bundle()
                        bundle.putBoolean(EDIT, true)
                        bundle.putSerializable(MEMBER_DATA, item)
                        findNavController().navigate(
                            R.id.action_myPostFragment_to_postVideoFragment,
                            bundle
                        )
                    }
                }
            })
        }

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            checkStatus { viewModel.likePost(item, position, isLike) }
        }

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {
            checkStatus {
                val bundle = ClipFragment.createBundle(ArrayList(mutableListOf(item[position])), 0)
                navigationToVideo(bundle)
            }
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {
            val bundle = ClipFragment.createBundle(ArrayList(mutableListOf(item[position])), 0)
            navigationToVideo(bundle)
        }

        override fun onChipClick(type: PostType, tag: String) {
            val item = SearchPostItem(type, tag)
            val bundle = SearchPostFragment.createBundle(item)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_myPostFragment_to_searchPostFragment,
                    bundle
                )
            )
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = PictureDetailFragment.createBundle(item, 0)
                    navigationToPicture(bundle)
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 0)
                    navigationToText(bundle)
                }
            }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            checkStatus {
                when (adultTabType) {
                    AdultTabType.PICTURE -> {
                        val bundle = PictureDetailFragment.createBundle(item, 1)
                        navigationToPicture(bundle)
                    }
                    AdultTabType.TEXT -> {
                        val bundle = TextDetailFragment.createBundle(item, 1)
                        navigationToText(bundle)
                    }
                }
            }
        }

        override fun onFavoriteClick(
            item: MemberPostItem,
            position: Int,
            isFavorite: Boolean,
            type: AttachmentType
        ) {
            checkStatus { viewModel.favoritePost(item, position, isFavorite) }
        }

        override fun onFollowClick(
            items: List<MemberPostItem>,
            position: Int,
            isFollow: Boolean
        ) {
            checkStatus { viewModel.followPost(ArrayList(items), position, isFollow) }
        }
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
        viewModel.cancelJob()
        snackBar?.dismiss()
        uploadCurrentPicPosition = 0
        deleteCurrentPicPosition = 0

        if (msg.isNotBlank()) {
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
        }
    }

    private val memberPostFuncItem by lazy {
        MemberPostFuncItem(
            {},
            { id, function -> getBitmap(id, function) },
            { _, _, _, _ -> }
        )
    }

    private fun getBitmap(id: String, update: ((String) -> Unit)) {
        viewModel.getBitmap(id, update)
    }
}