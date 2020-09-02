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
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PostAttachmentItem
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.model.vo.ViewerItem
import com.dabenxiang.mimi.view.adapter.ScrollPicAdapter
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.post.utility.PostManager
import com.dabenxiang.mimi.view.post.viewer.PostViewerFragment.Companion.VIEWER_DATA
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.widget.utility.FileUtil
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
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = adapter

        tv_clean.isEnabled = true
        val img = requireContext().getDrawable(R.drawable.btn_close_n)
        tv_back.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null)

        edt_hashtag.imeOptions = EditorInfo.IME_ACTION_DONE

        useAdultTheme(false)
    }

    override fun setupListeners() {
        super.setupListeners()

        tv_clean.setOnClickListener {

            if (checkFieldIsEmpty()) {
                return@setOnClickListener
            }

            if (adapter.getData().isEmpty()) {
                Toast.makeText(requireContext(), R.string.post_warning_pic, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            navigation()
        }
    }

    private fun navigation() {
        var isEdit = false
        val title = edt_title.text.toString()
        var page = ""
        var searchPostItem: SearchPostItem? = null

        arguments?.let {
            isEdit = it.getBoolean(MyPostFragment.EDIT, false)
            page = it.getString(PAGE, "")
            val data = it.getSerializable(SearchPostFragment.KEY_DATA)
            if (data != null) {
                searchPostItem = data as SearchPostItem
            }
        }

        val request = getRequest(title, PostType.IMAGE.value)

        val bundle = Bundle()
        bundle.putBoolean(UPLOAD_PIC, true)
        bundle.putParcelable(MEMBER_REQUEST, request)
        bundle.putParcelableArrayList(PIC_URI, adapter.getData())
        bundle.putStringArrayList(DELETE_ATTACHMENT, deletePicList)
        bundle.putLong(POST_ID, postId)

        if (isEdit && page == MY_POST) {
            val item = arguments?.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem
            bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
            findNavController().navigate(R.id.action_postPicFragment_to_myPostFragment, bundle)
        } else if (isEdit && page == ADULT) {
            val item = arguments?.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem
            bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
            findNavController().navigate(R.id.action_postPicFragment_to_adultHomeFragment, bundle)
        } else if (isEdit && page == SEARCH) {
            val item = arguments?.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem
            bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
            bundle.putSerializable(SearchPostFragment.KEY_DATA, searchPostItem)
            findNavController().navigate(R.id.action_postPicFragment_to_searchPostFragment, bundle)
        } else if (isEdit && page == CLUB) {
            val item = arguments?.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem
            bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
            bundle.putSerializable(SearchPostFragment.KEY_DATA, searchPostItem)
            findNavController().navigate(R.id.action_postPicFragment_to_clubDetailFragment, bundle)
        } else {
            findNavController().navigate(R.id.action_postPicFragment_to_adultHomeFragment, bundle)
        }
    }


    override fun handlePic() {
        val uriList = arguments?.getStringArrayList(BUNDLE_PIC_URI)!!
        for (uri in uriList) {
            val postAttachmentItem = PostAttachmentItem(uri = uri!!)
            attachmentList.add(postAttachmentItem)
        }
        txt_picCount.text = String.format(getString(R.string.select_pic_count, attachmentList.size,
            PHOTO_LIMIT
        ))
    }

    override fun setUI(item: MediaItem) {
        for (pic in item.picParameter) {
            val postAttachmentItem = PostAttachmentItem()
            postAttachmentItem.attachmentId = pic.id
            postAttachmentItem.ext = pic.ext
            attachmentList.add(postAttachmentItem)
        }

        txt_picCount.text = String.format(getString(R.string.select_pic_count, attachmentList.size,
            PHOTO_LIMIT
        ))
    }

    private fun updateCountPicView() {
        attachmentList.clear()
        attachmentList.addAll(adapter.getData())
        adapter.notifyDataSetChanged()
        txt_picCount.text = String.format(getString(R.string.select_pic_count, attachmentList.size,
            PHOTO_LIMIT
        ))
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
        val uriList = PostManager().getPicsUri(clipData, requireContext())
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
            postAttachmentItem.uri = UriUtils.getPath(requireContext(), uri)!!
        }

        val uriDataList = adapter.getData()
        uriDataList.add(postAttachmentItem)
    }

    private val postPicItemListener by lazy {
        PostPicItemListener(
            { id, function -> getBitmap(id, function) },
            { item -> handleDeletePic(item) },
            { updateCountPicView() },
            { addPic() },
            { viewerItem -> openViewerPage(viewerItem) }
        )
    }

    private fun getBitmap(id: String, update: ((String) -> Unit)) {
        viewModel.getBitmap(id, update)
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