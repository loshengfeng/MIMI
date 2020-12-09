package com.dabenxiang.mimi.view.dialog.chooseclub

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.ClubListener
import com.dabenxiang.mimi.callback.PostAttachmentListener
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.adapter.ChooseClubAdapter
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_choose_club.*
import kotlinx.android.synthetic.main.fragment_dialog_choose_upload_method.txt_cancel

class ChooseClubDialogFragment : BaseDialogFragment() {

    private val viewModel: ChooseClubDialogViewModel by viewModels()
    var chooseClubDialogListener: ChooseClubDialogListener? = null

    private lateinit var adapter: ChooseClubAdapter

    companion object {
        fun newInstance(listener: ChooseClubDialogListener? = null): ChooseClubDialogFragment {
            val fragment = ChooseClubDialogFragment()
            fragment.chooseClubDialogListener = listener
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

        adapter = ChooseClubAdapter(attachmentListener, clubListener)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.getClubList()
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.postList.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        viewModel.loadingStatus.observe(viewLifecycleOwner, {
            if (it) {
                progress_bar.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                progress_bar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        })

        viewModel.totalCount.observe(viewLifecycleOwner, {
            adapter.totalCount = it.toInt()
        })
    }

    private val attachmentListener = object : PostAttachmentListener {
        override fun getAttachment(id: Long?, view: ImageView) {
            viewModel.loadImage(id, view, LoadImageType.AVATAR)
        }
    }

    private val clubListener = object : ClubListener {
        override fun onClick(item: MemberClubItem) {
            dismiss()
            chooseClubDialogListener?.onChooseClub(item)
        }
    }
}