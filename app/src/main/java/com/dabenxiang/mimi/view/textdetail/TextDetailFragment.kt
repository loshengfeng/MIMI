package com.dabenxiang.mimi.view.textdetail

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import kotlinx.android.synthetic.main.fragment_picture_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*

class TextDetailFragment : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"
        const val KEY_POSITION = "position"
        fun createBundle(item: MemberPostItem, position: Int): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
                it.putInt(KEY_POSITION, position)
            }
        }
    }

    private var memberPostItem: MemberPostItem? = null

    private val viewModel: TextDetailViewModel by viewModels()

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        memberPostItem =
            arguments?.getSerializable(PictureDetailFragment.KEY_DATA) as MemberPostItem
        val position = arguments?.getInt(PictureDetailFragment.KEY_POSITION) ?: 0

        requireActivity().onBackPressedDispatcher.addCallback { navigateTo(NavigateItem.Up) }

        text_toolbar_title.text = getString(R.string.text_detail_title)
        toolbarContainer.toolbar.navigationIcon =
            requireContext().getDrawable(R.drawable.btn_back_white_n)
        toolbarContainer.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_text_detail
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {

    }


}