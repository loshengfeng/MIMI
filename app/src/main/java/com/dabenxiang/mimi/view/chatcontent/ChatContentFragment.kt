package com.dabenxiang.mimi.view.chatcontent

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ChatContentItem
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.enums.ChatMessageType
import com.dabenxiang.mimi.model.enums.VideoDownloadStatusType
import com.dabenxiang.mimi.model.vo.AttachmentItem
import com.dabenxiang.mimi.view.adapter.ChatContentAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.dialog.preview.ImagePreviewDialogFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.fragment_chat_content.*
import timber.log.Timber
import java.io.File

class ChatContentFragment : BaseFragment() {

    private val INTENT_SELECT_IMG: Int = 100

    private lateinit var imagePreviewDialog: ImagePreviewDialogFragment
    private val viewModel: ChatContentViewModel by viewModels()
    private val adapter by lazy { ChatContentAdapter(listener) }


    private val listener = object : ChatContentAdapter.EventListener {
        override fun onGetAttachment(id: String, position: Int) {
            viewModel.getAttachment(requireContext(), id, position)
        }

        override fun onImageClick(bitmap: Bitmap) {
            imagePreviewDialog = ImagePreviewDialogFragment.newInstance(bitmap, null).also {
                it.show(
                        requireActivity().supportFragmentManager,
                        MoreDialogFragment::class.java.simpleName
                )
            }
        }

        override fun onVideoClick(item: ChatContentItem?, position: Int) {
            item?.run {
                if (payload?.content == null || payload.ext == null) {
                    GeneralUtils.showToast(requireContext(), getString(R.string.image_preview_file_invlid))
                } else {
                    // init videoCache
                    viewModel.videoCache[payload.content] = item
                    if (!File(viewModel.getVideoPath(requireContext(), payload.content, payload.ext)).exists()) {
                        item.downloadStatus = VideoDownloadStatusType.DOWNLOADING
                        item.position = position
                        // update videoCache
                        viewModel.videoCache[payload.content] = item
                        viewModel.getAttachment(requireContext(), payload.content, position, viewModel.TAG_VIDEO)
                    } else {
                        viewModel.videoCache[payload.content]?.let { cacheItem ->
                            if (cacheItem.payload?.content == null || cacheItem.payload.ext == null) {
                                GeneralUtils.showToast(requireContext(), getString(R.string.image_preview_file_invlid))
                            } else {
                                if (cacheItem.downloadStatus == VideoDownloadStatusType.FINISH) {
                                    GeneralUtils.openPlayerIntent(requireContext(), viewModel.getVideoPath(requireContext(), cacheItem.payload.content, cacheItem.payload.ext))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val KEY_CHAT_LIST_ITEM = "chat_list_item"
        fun createBundle(item: ChatListItem): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_CHAT_LIST_ITEM, item)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
        arguments?.getSerializable(KEY_CHAT_LIST_ITEM)?.let { data ->
            data as ChatListItem
            textTitle.text = data.name
            data.id?.let { id ->
                viewModel.chatId = id
                viewModel.getChatContent()
                viewModel.initMQTT(id.toString())
                viewModel.connect()
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_chat_content
    }

    override fun initSettings() {
        super.initSettings()
        recyclerContent.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        recyclerContent.adapter = adapter
    }

    override fun setupObservers() {
        Timber.d("${ChatContentFragment::class.java.simpleName}_setupObservers")

        viewModel.chatListResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    viewModel.isLoading = false
                    adapter.setData(it.result)
                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })

        viewModel.attachmentResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> {
                    // 更新為下載中
                    val fileName = it.arg as String
                    viewModel.videoCache[fileName]?.downloadStatus = VideoDownloadStatusType.DOWNLOADING
                    viewModel.videoCache[fileName]?.position?.let { position ->
                        adapter.update(position)
                    }
                }
                is ApiResult.Success -> {
                    when (it.result) {
                        is AttachmentItem -> {
                            val attachmentItem = it.result
                            LruCacheUtils.putLruCache(attachmentItem.id!!, attachmentItem.bitmap!!)
                            adapter.update(attachmentItem.position ?: -1)
                        }
                        is String -> {
                            val fileName = it.result.substringBefore(".").substringAfterLast("/")
                            viewModel.videoCache[fileName]?.downloadStatus = VideoDownloadStatusType.FINISH
                            adapter.update(viewModel.videoCache[fileName]?.position
                                    ?: -1)
                        }
                    }
                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })

        viewModel.postAttachmentResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    viewModel.publishMsg(it.result.id.toString(), it.result.ext)
                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })
        viewModel.fileAttachmentTooLarge.observe(viewLifecycleOwner, Observer { result ->
            if (result) {
                GeneralUtils.showToast(requireContext(), getString(R.string.chat_content_file_too_large))
            }
        })
    }

    override fun setupListeners() {
        Timber.d("${ChatContentFragment::class.java.simpleName}_setupListeners")

        btnClose.setOnClickListener {
            Navigation.findNavController(requireView()).navigateUp()
        }

        btnSend.setOnClickListener {
            if (editChat.text.isNotEmpty()) {
                viewModel.messageType = ChatMessageType.TEXT.ordinal
                viewModel.publishMsg(editChat.text.toString())
                editChat.text.clear()
            }
        }
        btnAdd.setOnClickListener {
            openChooser()
        }

        recyclerContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager

                if (!viewModel.isLoading && !viewModel.noMore) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == adapter.itemCount - 4) {
                        //bottom of list!
                        viewModel.getChatContent()
                        viewModel.isLoading = true
                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            data?.let {
                val uriImage: Uri?

                uriImage = if (it.data != null) {
                    it.data
                } else {
                    val extras = it.extras
                    val imageBitmap = extras!!["data"] as Bitmap?
                    Uri.parse(MediaStore.Images.Media.insertImage(requireContext().contentResolver, imageBitmap, null, null));
                }

                if (uriImage == null) {
                    return
                }
                viewModel.postAttachment(uriImage, requireContext())
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 打開多媒體選擇器
     */
    private fun openChooser() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/* video/*"

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

        val chooser = Intent(Intent.ACTION_CHOOSER)
        chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent)
        chooser.putExtra(Intent.EXTRA_TITLE, requireContext().getString(R.string.chat_content_media_chooser))

        val intentArray = arrayOf(cameraIntent, videoIntent)
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
        startActivityForResult(chooser, INTENT_SELECT_IMG)
    }
}