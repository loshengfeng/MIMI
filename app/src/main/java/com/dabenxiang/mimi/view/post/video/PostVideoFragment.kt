package com.dabenxiang.mimi.view.post.video

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.PostVideoItemListener
import com.dabenxiang.mimi.model.api.vo.MediaItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PostVideoAttachment
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.model.vo.ViewerItem
import com.dabenxiang.mimi.view.adapter.viewHolder.ScrollVideoAdapter
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.player.ui.ClipPlayerFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.post.utility.PostManager
import com.dabenxiang.mimi.view.post.viewer.PostViewerFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.UriUtils
import kotlinx.android.synthetic.main.fragment_post_article.edt_hashtag
import kotlinx.android.synthetic.main.fragment_post_article.edt_title
import kotlinx.android.synthetic.main.fragment_post_pic.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit


class PostVideoFragment : BasePostFragment() {

    private var haveMainTag = false
    private var isEdit = false
    private var page = ""

    override fun getLayoutId(): Int {
        return R.layout.fragment_post_video
    }

    private val videoAttachmentList = arrayListOf<PostVideoAttachment>()
    private val deleteVideoList = arrayListOf<PostVideoAttachment>()

    private lateinit var adapter: ScrollVideoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSettings()
        btn_tag_confirm.setOnClickListener { hashTagConfirm() }
    }

    override fun initSettings() {
        adapter = ScrollVideoAdapter(postPicItemListener)
        adapter.submitList(videoAttachmentList)
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = adapter

        edt_hashtag.imeOptions = EditorInfo.IME_ACTION_DONE

        arguments?.let {
            isEdit = it.getBoolean(MyPostFragment.EDIT, false)
            page = it.getString(PAGE, "")
        }
    }

    override fun setupListeners() {
        super.setupListeners()

        requireActivity().onBackPressedDispatcher.addCallback(
            owner = viewLifecycleOwner,
            onBackPressed = { discardDialog() }
        )

        tv_clean.setOnClickListener {

            GeneralUtils.hideKeyboard(requireActivity())

            if (checkFieldIsEmpty()) {
                return@setOnClickListener
            }

            if (videoAttachmentList.isEmpty()) {
                Toast.makeText(requireContext(), R.string.post_warning_video, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (!checkTagCountIsValid()) {
                return@setOnClickListener
            }

            navigation()
        }

        tv_back.setOnClickListener {
            discardDialog()
        }

    }

    private fun navigation() {
        val title = edt_title.text.toString()
        var searchPostItem: SearchPostItem? = null
        var memberClubItem: MemberClubItem? =null

        arguments?.let {
            isEdit = it.getBoolean(MyPostFragment.EDIT, false)
            page = it.getString(PAGE, "")
            val data = it.getSerializable(SearchPostFragment.KEY_DATA)
            if (data != null) {
                if (data is SearchPostItem) {
                    searchPostItem = data
                } else if (data is MemberClubItem){
                    memberClubItem = data
                }
            }
        }

        val request = getRequest(title, PostType.VIDEO.value)

        setVideoTime()

        val bundle = Bundle()
        bundle.putString(POST_TYPE, VIDEO)
        bundle.putParcelable(MEMBER_REQUEST, request)
        bundle.putParcelableArrayList(VIDEO_DATA, videoAttachmentList)
        bundle.putParcelableArrayList(DELETE_ATTACHMENT, deleteVideoList)
        bundle.putLong(POST_ID, postId)

        if (isEdit && page == MY_POST) {
            val item = arguments?.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem
            bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
            findNavController().navigate(R.id.action_postVideoFragment_to_myPostFragment, bundle)
        } else if (isEdit && page == SEARCH) {
            val item = arguments?.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem
            bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
            bundle.putSerializable(SearchPostFragment.KEY_DATA, searchPostItem)
            findNavController().navigate(
                R.id.action_postVideoFragment_to_searchPostFragment,
                bundle
            )
        } else if (isEdit && page == CLUB) {
            val item = arguments?.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem
            bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
            bundle.putSerializable(SearchPostFragment.KEY_DATA, memberClubItem)
            findNavController().navigate(
                R.id.action_postVideoFragment_to_topicDetailFragment,
                bundle
            )
        } else if (isEdit && page == VIDEO) {
            val item = arguments?.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem
            bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
            bundle.putSerializable(SearchPostFragment.KEY_DATA, memberClubItem)
            bundle.putLong(ClipPlayerFragment.KEY_PLAYER_SRC, item.id)
            findNavController().navigate(
                R.id.action_postVideoFragment_to_clipPlayerFragment,
                bundle
            )
        } else if (isEdit && page == TAB) {
            val item = arguments?.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem
            bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
            findNavController().navigate(R.id.action_postVideoFragment_to_clubTabFragment, bundle)
        } else {
            findNavController().navigate(R.id.action_postVideoFragment_to_clubTabFragment, bundle)
        }
    }

    override fun handleVideo() {
        val trimmerUri = arguments?.getString(BUNDLE_TRIMMER_URI)
        val picUri = arguments?.getString(BUNDLE_COVER_URI)
        val postVideoAttachment = PostVideoAttachment(videoUrl = trimmerUri!!, picUrl = picUri!!)
        videoAttachmentList.add(postVideoAttachment)
    }

    override fun setUI(item: MediaItem, memberPostItem: MemberPostItem) {
        val trimmerUri = arguments?.getString(BUNDLE_TRIMMER_URI, "")
        val picUri = arguments?.getString(BUNDLE_COVER_URI, "")
        if (trimmerUri!!.isBlank()) {
            val postVideoAttachment = PostVideoAttachment(
                videoAttachmentId = item.videoParameter.id,
                length = item.videoParameter.length,
                picAttachmentId = item.picParameter[0].id,
                ext = item.picParameter[0].ext
            )
            videoAttachmentList.add(postVideoAttachment)

            haveMainTag = true
        } else {
            val postVideoAttachment = PostVideoAttachment(videoUrl = trimmerUri, picUrl = picUri!!)
            videoAttachmentList.add(postVideoAttachment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_VIDEO_CAPTURE -> {
                    handTakeVideo(data)
                }
            }
        }
    }

    private fun handTakeVideo(data: Intent?) {
        val videoUri: Uri? = data?.data
        val myUri = Uri.fromFile(File(UriUtils.getPath(requireContext(), videoUri!!) ?: ""))

        if (PostManager().isVideoTimeValid(myUri, requireContext())) {

            val bundle = Bundle()
            bundle.putString(EditVideoFragment.BUNDLE_VIDEO_URI, myUri.toString())
            bundle.putString(PAGE, page)

            if (isEdit) {
                val item = arguments?.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem

                bundle.putBoolean(MyPostFragment.EDIT, true)
                bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
            }

            findNavController().navigate(
                R.id.action_postVideoFragment_to_editVideoFragment,
                bundle
            )
        } else {
            Toast.makeText(requireContext(), R.string.post_video_length_error, Toast.LENGTH_SHORT)
                .show()

        }
    }

    private fun openRecorder() {
        val requestList = getNotGrantedPermissions(externalPermissions + cameraPermissions)
        if (requestList.size > 0) {
            requestPermissions(
                requestList.toTypedArray(),
                PERMISSION_VIDEO_REQUEST_CODE
            )
        } else {
            PostManager().selectVideo(this@PostVideoFragment)
        }
    }

    private fun deleteVideo(item: PostVideoAttachment) {
        deleteVideoList.clear()
        if (item.videoAttachmentId == videoAttachmentList[0].videoAttachmentId) {
            deleteVideoList.add(item)
        }

        videoAttachmentList.clear()
    }

    private val postPicItemListener by lazy {
        PostVideoItemListener(
            { id, view -> viewModel.loadImage(id, view, LoadImageType.PICTURE_THUMBNAIL) },
            { openRecorder() },
            { item -> deleteVideo(item) },
            { viewerItem -> openViewerPage(viewerItem) }
        )
    }

    private fun openViewerPage(viewerItem: ViewerItem) {
        val bundle = Bundle()
        bundle.putSerializable(PostViewerFragment.VIEWER_DATA, viewerItem)
        findNavController().navigate(R.id.action_postVideoFragment_to_postViewerFragment, bundle)
    }

    private fun setVideoTime() {
        if (videoAttachmentList[0].videoAttachmentId.isBlank()) {
            val timeInMillisec = PostManager().getVideoTime(
                Uri.parse(videoAttachmentList[0].videoUrl),
                requireContext()
            )

            val length = String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(timeInMillisec),
                TimeUnit.MILLISECONDS.toMinutes(timeInMillisec) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(
                        timeInMillisec
                    )
                ),
                TimeUnit.MILLISECONDS.toSeconds(timeInMillisec) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(timeInMillisec)
                )
            )
            videoAttachmentList[0].length = length
        }
    }

    private fun requestPermissions() {
        val requestList = getNotGrantedPermissions(externalPermissions + cameraPermissions)

        if (requestList.size == 0) {
            PostManager().selectVideo(this@PostVideoFragment)
        }
    }

    private fun discardDialog() {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.whether_to_discard_content,
                messageIcon = R.drawable.ico_default_photo,
                firstBtn = getString(R.string.btn_cancel),
                secondBtn = getString(R.string.btn_confirm),
                isMessageIcon = false,
                secondBlock = {
                    handleBackEvent()
                }
            )
        ).show(requireActivity().supportFragmentManager)
    }

    private fun handleBackEvent() {
        if (isEdit && page == MY_POST) {
            Navigation.findNavController(requireView()).popBackStack(R.id.myPostFragment, false)
        } else if (isEdit && page == ADULT) {
            Navigation.findNavController(requireView()).popBackStack(R.id.adultHomeFragment, false)
        } else if (isEdit && page == SEARCH) {
            Navigation.findNavController(requireView()).popBackStack(R.id.searchPostFragment, false)
        } else if (isEdit && page == CLUB) {
            Navigation.findNavController(requireView()).popBackStack(R.id.topicDetailFragment, false)
        } else {
            navigateTo(NavigateItem.PopBackStack(R.id.clubTabFragment, true))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Timber.i("onRequestPermissionsResult")
        if (requestCode == PERMISSION_VIDEO_REQUEST_CODE) {
            if (getNotGrantedPermissions(externalPermissions + cameraPermissions).isEmpty()) {
                PostManager().selectVideo(this@PostVideoFragment)
            } else {
                requestPermissions()
            }
        }
    }
}