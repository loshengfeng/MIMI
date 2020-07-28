package com.dabenxiang.mimi.view.chatcontent

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.api.vo.MQTTChatItem
import com.dabenxiang.mimi.view.adapter.ChatContentAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_chat_content.*
import timber.log.Timber

class ChatContentFragment : BaseFragment() {

    private val viewModel: ChatContentViewModel by viewModels()
    private val adapter by lazy { ChatContentAdapter(listener) }
    private val listener = object : ChatContentAdapter.EventListener {
        override fun onGetAttachment(id: String, position: Int) {
            viewModel.getAttachment(id, position)
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
                viewModel.getChatContent(id)
                viewModel.init(id.toString())
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

        viewModel.chatListResult.observe(viewLifecycleOwner, Observer { adapter.submitList(it) })

        viewModel.attachmentResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    val attachmentItem = it.result
                    LruCacheUtils.putLruCache(attachmentItem.id!!, attachmentItem.bitmap!!)
                    adapter.update(attachmentItem.position ?: 0)
                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })

    }

    override fun setupListeners() {
        Timber.d("${ChatContentFragment::class.java.simpleName}_setupListeners")

        btnClose.setOnClickListener {
            Navigation.findNavController(requireView()).navigateUp()
        }

        btnSend.setOnClickListener {
            val mqttData = MQTTChatItem("", editChat.text.toString(), "2020-07-28T11:26:04+00:00", 0)
            viewModel.publishMsg(Gson().toJson(mqttData))
        }
    }
}