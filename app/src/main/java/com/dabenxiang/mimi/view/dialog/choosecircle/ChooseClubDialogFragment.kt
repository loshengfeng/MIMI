package com.dabenxiang.mimi.view.dialog.choosecircle

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.PostAttachmentListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.fragment_dialog_choose_club.*
import kotlinx.android.synthetic.main.fragment_dialog_choose_upload_method.txt_cancel
import timber.log.Timber

class ChooseClubDialogFragment : BaseDialogFragment() {

    private val viewModel: ChooseClubDialogViewModel by viewModels()

    private lateinit var adapter: ChooseClubAdapter

    companion object {
        fun newInstance(): ChooseClubDialogFragment {
            val fragment = ChooseClubDialogFragment()
            return fragment
        }
    }

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_choose_club
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txt_cancel.setOnClickListener {
            dismiss()
        }

        adapter = ChooseClubAdapter(attachmentListener)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.getClubList()
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.memberClubItem.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Success -> {
                    val memberClubItems = it.result.content ?: arrayListOf()
                    adapter.submitList(memberClubItems)
                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })

        viewModel.attachmentByTypeResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Success -> {
                    val attachmentItem = it.result
                    LruCacheUtils.putLruCache(attachmentItem.id!!, attachmentItem.bitmap!!)
                    adapter.updateItem(attachmentItem.position!!)
                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })
    }

    private val attachmentListener = object : PostAttachmentListener {
        override fun getAttachment(id: String, position: Int) {
            viewModel.getAttachment(id, position)
        }
    }
}