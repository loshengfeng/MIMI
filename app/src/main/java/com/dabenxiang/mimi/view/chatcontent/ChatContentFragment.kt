package com.dabenxiang.mimi.view.chatcontent

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.ChatContentItem
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.enums.ChatMessageType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.VideoDownloadStatusType
import com.dabenxiang.mimi.model.manager.mqtt.callback.MessageListener
import com.dabenxiang.mimi.view.adapter.ChatContentAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.dialog.preview.ImagePreviewDialogFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_chat_content.*
import org.eclipse.paho.client.mqttv3.MqttMessage
import timber.log.Timber
import java.io.File

class ChatContentFragment : BaseFragment() {

    companion object {
        private const val KEY_CHAT_LIST_ITEM = "chat_list_item"
        private const val KEY_TRACE_LOG_ID = "trace_log_id"
        private const val KEY_IS_ONLINE = "is_online"
        private const val INTENT_SELECT_IMG: Int = 100
        private const val PRELOAD_ITEM: Int = 4
        fun createBundle(
            item: ChatListItem,
            traceLogId: Long = -1,
            isOnline: Boolean = false
        ): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_CHAT_LIST_ITEM, item)
                it.putLong(KEY_TRACE_LOG_ID, traceLogId)
                it.putBoolean(KEY_IS_ONLINE, isOnline)
            }
        }
    }

    private val viewModel: ChatContentViewModel by viewModels()

    private lateinit var imagePreviewDialog: ImagePreviewDialogFragment

    private val adapter by lazy { ChatContentAdapter(viewModel.pref, listener) }
    private var senderAvatarId = ""

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback {
            viewModel.setLastRead()
            navigateTo(NavigateItem.Up)
        }

        initSettings()

        arguments?.getLong(KEY_TRACE_LOG_ID)?.also {
            viewModel.traceLogId = it
        }

        arguments?.getBoolean(KEY_IS_ONLINE)?.also {
            viewModel.isOnline = it
        }

        arguments?.getSerializable(KEY_CHAT_LIST_ITEM)?.let { data ->
            data as ChatListItem
            textTitle.text = data.name
            senderAvatarId = data.avatarAttachmentId.toString()
            data.id?.let { id ->
                viewModel.chatId = id
                viewModel.getChatContent()
                viewModel.setLastRead()
                if (mainViewModel?.isMqttConnect == true) {
                    mainViewModel?.subscribeToTopic(viewModel.getChatTopic())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel?.messageListenerMap?.put(viewModel.getChatTopic(), messageListener)
    }

    override fun onPause() {
        super.onPause()
        mainViewModel?.messageListenerMap?.remove(viewModel.getChatTopic())
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_chat_content
    }

    override fun initSettings() {
        super.initSettings()
        recyclerContent.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        recyclerContent.adapter = adapter
    }

    override fun setupObservers() {
        Timber.d("${ChatContentFragment::class.java.simpleName}_setupObservers")

        viewModel.chatListResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    viewModel.isLoading = false
                    adapter.setData(it.result)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.attachmentResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> {
                    // 更新為下載中
                    val fileName = it.arg as String
                    viewModel.videoCache[fileName]?.downloadStatus =
                        VideoDownloadStatusType.DOWNLOADING
                    viewModel.videoCache[fileName]?.position?.let { position ->
                        adapter.update(
                            position
                        )
                    }
                }
                is Success -> {
                    val fileName = it.result.substringBefore(".").substringAfterLast("/")
                    viewModel.videoCache[fileName]?.downloadStatus = VideoDownloadStatusType.FINISH
                    adapter.update(viewModel.videoCache[fileName]?.position ?: -1)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.postAttachmentResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.fileAttachmentTooLarge.observe(viewLifecycleOwner, Observer { result ->
            if (result) {
                GeneralUtils.showToast(
                    requireContext(),
                    getString(R.string.chat_content_file_too_large)
                )
            }
        })

        viewModel.cachePushData.observe(viewLifecycleOwner, Observer {
            adapter.insertItem(it)
        })

        viewModel.updatePushData.observe(viewLifecycleOwner, Observer {
            if (!TextUtils.isEmpty(it.payload?.ext)) {
                adapter.updateCacheData(it, viewModel.fileUploadCache)
            }
        })

        viewModel.setLastReadResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.updateOrderChatStatusResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Error -> onApiError(it.throwable)
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
                if (editChat.text.length < 500) {
                    viewModel.messageType = ChatMessageType.TEXT.ordinal
                    viewModel.pushMsgWithCacheData(editChat.text.toString())
                    editChat.text.clear()
                } else {
                    GeneralUtils.showToast(
                        requireContext(),
                        getString(R.string.chat_content_text_too_much)
                    )
                }
            }
        }
        btnAdd.setOnClickListener {
            openChooser()
        }

        recyclerContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager: LinearLayoutManager =
                    recyclerView.layoutManager as LinearLayoutManager

                if (!viewModel.isLoading && !viewModel.noMore) {
                    if (linearLayoutManager != null
                        && linearLayoutManager.findLastCompletelyVisibleItemPosition() == adapter.itemCount - PRELOAD_ITEM
                    ) {
                        // bottom of list!
                        viewModel.getChatContent()
                        viewModel.isLoading = true
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            data?.let {
                val uriImage = (if (it.data != null) {
                    it.data
                } else {
                    val extras = it.extras
                    val imageBitmap = extras!!["data"] as Bitmap?
                    Uri.parse(
                        MediaStore.Images.Media.insertImage(
                            requireContext().contentResolver,
                            imageBitmap,
                            null,
                            null
                        )
                    )
                }) ?: return

                viewModel.postAttachment(uriImage, requireContext())
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private val listener = object : ChatContentAdapter.EventListener {
        override fun onGetAttachment(id: Long?, view: ImageView, type: LoadImageType) {
            viewModel.loadImage(id, view, type)
        }

        override fun onImageClick(imageArray: ByteArray?) {
            imagePreviewDialog = ImagePreviewDialogFragment.newInstance(imageArray, null).also {
                it.show(
                    requireActivity().supportFragmentManager,
                    MoreDialogFragment::class.java.simpleName
                )
            }
        }

        override fun onVideoClick(item: ChatContentItem?, position: Int) {
            item?.run {
                if (payload?.content == null || payload.ext == null) {
                    GeneralUtils.showToast(
                        requireContext(),
                        getString(R.string.image_preview_file_invlid)
                    )
                } else {
                    // init videoCache
                    viewModel.videoCache[payload.content] = item
                    if (!File(
                            viewModel.getVideoPath(
                                requireContext(),
                                payload.content,
                                payload.ext
                            )
                        ).exists()
                    ) {
                        item.downloadStatus = VideoDownloadStatusType.DOWNLOADING
                        item.position = position
                        // update videoCache
                        viewModel.videoCache[payload.content] = item
                        viewModel.getAttachment(requireContext(), payload.content)
                    } else {
                        viewModel.videoCache[payload.content]?.let { cacheItem ->
                            if (cacheItem.payload?.content == null || cacheItem.payload.ext == null) {
                                GeneralUtils.showToast(
                                    requireContext(),
                                    getString(R.string.image_preview_file_invlid)
                                )
                            } else {
                                if (cacheItem.downloadStatus == VideoDownloadStatusType.FINISH) {
                                    GeneralUtils.openPlayerIntent(
                                        requireContext(),
                                        viewModel.getVideoPath(
                                            requireContext(),
                                            cacheItem.payload.content,
                                            cacheItem.payload.ext
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun getSenderAvatar(): String {
            return senderAvatarId
        }
    }

    /**
     * 打開多媒體選擇器
     */
    private fun openChooser() {
        val requestList = getNotGrantedPermissions(externalPermissions + cameraPermissions)
        if (requestList.size > 0) {
            requestPermissions(
                requestList.toTypedArray(),
                PERMISSION_GALLERY_REQUEST_CODE
            )
        } else {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/* video/*"

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

            val chooser = Intent(Intent.ACTION_CHOOSER)
            chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent)
            chooser.putExtra(
                Intent.EXTRA_TITLE,
                requireContext().getString(R.string.chat_content_media_chooser)
            )

            val intentArray = arrayOf(cameraIntent, videoIntent)
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
            startActivityForResult(chooser, INTENT_SELECT_IMG)
        }
    }

    private val messageListener = object : MessageListener {
        override fun onMsgReceive(message: MqttMessage) {
            viewModel.processMessage(message)
        }
    }
}