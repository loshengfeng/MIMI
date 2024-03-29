package com.dabenxiang.mimi.view.post.pic

import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.PostPicItemListener
import com.dabenxiang.mimi.model.api.vo.MediaItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PostClubItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PostAttachmentItem
import com.dabenxiang.mimi.model.vo.ViewerItem
import com.dabenxiang.mimi.view.adapter.ScrollPicAdapter
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.post.utility.PostManager
import com.dabenxiang.mimi.view.post.viewer.PostViewerFragment.Companion.VIEWER_DATA
import com.dabenxiang.mimi.widget.utility.FileUtil
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.UriUtils
import kotlinx.android.synthetic.main.fragment_post_article.edt_hashtag
import kotlinx.android.synthetic.main.fragment_post_article.edt_title
import kotlinx.android.synthetic.main.fragment_post_pic.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import timber.log.Timber
import java.io.File


class PostPicFragment : BasePostFragment() {

    private var file = File("")

    override fun getLayoutId(): Int {
        return R.layout.fragment_post_pic
    }

    private var attachmentList = arrayListOf<PostAttachmentItem>()
    private var deletePicList = arrayListOf<String>()

    private lateinit var adapter: ScrollPicAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSettings()
    }

    override fun initSettings() {
        adapter = ScrollPicAdapter(postPicItemListener)
        adapter.submitList(attachmentList)
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = adapter

        edt_hashtag.imeOptions = EditorInfo.IME_ACTION_DONE
        btn_tag_confirm.setOnClickListener { hashTagConfirm() }
    }

    override fun setupListeners() {
        super.setupListeners()

        tv_clean.setOnClickListener {

            GeneralUtils.hideKeyboard(requireActivity())

            if (checkFieldIsEmpty()) {
                return@setOnClickListener
            }

            if (adapter.getData().isEmpty()) {
                Toast.makeText(requireContext(), R.string.post_warning_pic, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (!checkTagCountIsValid()) {
                return@setOnClickListener
            }

            navigation()
        }
    }

    private fun navigation() {
        val title = edt_title.text.toString()
        var memberPostItem: MemberPostItem? = null

        arguments?.let {
            if (it.containsKey(MyPostFragment.MEMBER_DATA)) {
                memberPostItem = it.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem
            }
        }

        val postClubItem = PostClubItem(type = PostType.IMAGE.value, title = title, tags = getTags(), uploadPics = adapter.getData(), deletePics = deletePicList, memberPostItem = memberPostItem)

        val bundle = Bundle()
        bundle.putSerializable(POST_DATA, postClubItem)

        mainViewModel?.uploadData?.value = bundle
        findNavController().navigateUp()
    }


    override fun handlePic() {
        val uriList = arguments?.getStringArrayList(BUNDLE_PIC_URI)!!
        for (uri in uriList) {
            val postAttachmentItem = PostAttachmentItem(uri = uri!!)
            attachmentList.add(postAttachmentItem)
        }
        txt_picCount.text = String.format(
            getString(
                R.string.select_pic_count, attachmentList.size,
                PHOTO_LIMIT
            )
        )
    }

    override fun setUI(item: MediaItem, memberPostItem: MemberPostItem) {
        for (pic in item.picParameter) {
            val postAttachmentItem = PostAttachmentItem()
            postAttachmentItem.attachmentId = pic.id
            postAttachmentItem.ext = pic.ext
            attachmentList.add(postAttachmentItem)
        }

        txt_picCount.text = String.format(
            getString(
                R.string.select_pic_count, attachmentList.size,
                PHOTO_LIMIT
            )
        )
    }

    private fun updateCountPicView() {
        attachmentList.clear()
        attachmentList.addAll(adapter.getData())
        adapter.notifyDataSetChanged()
        txt_picCount.text = String.format(
            getString(
                R.string.select_pic_count, attachmentList.size,
                PHOTO_LIMIT
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            val clipData = data?.clipData
            if (clipData != null) {
                handleMultiPics(clipData)
            } else {
                handleSinglePic(data)
            }
            updateCountPicView()
        }
    }

    private fun handleMultiPics(clipData: ClipData) {
        val uriList = PostManager().getPicsUri(clipData, requireContext(), adapter.getData().size)
        for (uri in uriList) {
            val uriDataList = adapter.getData()
            val postAttachmentItem = PostAttachmentItem(uri = uri)
            uriDataList.add(postAttachmentItem)
        }
    }

    private fun handleSinglePic(data: Intent?) {
        val postAttachmentItem = PostAttachmentItem()
        postAttachmentItem.uri = file.absolutePath

        val uri = PostManager().getPicUri(data, requireContext(), file)

        if (uri.path!!.isNotBlank()) {
            try {
                postAttachmentItem.uri = UriUtils.getPath(requireContext(), uri)!!
            } catch(e: Exception) {
                GeneralUtils.showToast(requireContext(), "不支援此图片上传")
                onApiError(e)
                return
            }
        }

        val uriDataList = adapter.getData()
        uriDataList.add(postAttachmentItem)
    }

    private val postPicItemListener by lazy {
        PostPicItemListener(
            { id, view -> viewModel.loadImage(id, view, LoadImageType.PICTURE_THUMBNAIL) },
            { item -> handleDeletePic(item) },
            { updateCountPicView() },
            { addPic() },
            { viewerItem -> openViewerPage(viewerItem) }
        )
    }

    private fun handleDeletePic(item: PostAttachmentItem) {
        for (data in attachmentList) {
            if (item.attachmentId == data.attachmentId) {
                deletePicList.add(item.attachmentId)
            }
        }
    }

    private fun addPic() {
        val requestList = getNotGrantedPermissions(externalPermissions + cameraPermissions)
        if (requestList.size > 0) {
            requestPermissions(
                requestList.toTypedArray(),
                PERMISSION_PIC_REQUEST_CODE
            )
        } else {
            file = FileUtil.getTakePhoto(System.currentTimeMillis().toString() + ".jpg")
            PostManager().selectPics(this@PostPicFragment, file)
        }
    }

    private fun openViewerPage(viewerItem: ViewerItem) {
        val bundle = Bundle()
        bundle.putSerializable(VIEWER_DATA, viewerItem)
        findNavController().navigate(R.id.action_postPicFragment_to_postViewerFragment, bundle)
    }

    private fun requestPermissions() {
        val requestList = getNotGrantedPermissions(externalPermissions + cameraPermissions)

        if (requestList.size == 0) {
            file = FileUtil.getTakePhoto(System.currentTimeMillis().toString() + ".jpg")
            PostManager().selectPics(this@PostPicFragment, file)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Timber.i("onRequestPermissionsResult")
        if (requestCode == PERMISSION_PIC_REQUEST_CODE) {
            if (getNotGrantedPermissions(externalPermissions + cameraPermissions).isEmpty()) {
                file = FileUtil.getTakePhoto(System.currentTimeMillis().toString() + ".jpg")
                PostManager().selectPics(this@PostPicFragment, file)
            } else {
                requestPermissions()
            }
        }
    }
}