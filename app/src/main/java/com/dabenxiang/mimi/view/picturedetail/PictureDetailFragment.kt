package com.dabenxiang.mimi.view.picturedetail

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_picture_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*
import timber.log.Timber

class PictureDetailFragment : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"

        fun createBundle(item: MemberPostItem): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val item = arguments?.getSerializable(KEY_DATA) as MemberPostItem
        Timber.d("item: $item")

        text_toolbar_title.text = getString(R.string.picture_detail_title)
        toolbarContainer.toolbar.navigationIcon =
            requireContext().getDrawable(R.drawable.btn_back_white_n)
        toolbarContainer.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_picture_detail
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {

    }

}