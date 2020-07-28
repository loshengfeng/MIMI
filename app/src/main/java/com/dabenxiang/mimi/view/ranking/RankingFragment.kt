package com.dabenxiang.mimi.view.ranking

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import kotlinx.android.synthetic.main.fragment_picture_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*

class RankingFragment : BaseFragment() {

    companion object {
        fun createBundle(): Bundle {
            return Bundle().also {
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback { navigateTo(NavigateItem.Up) }

        text_toolbar_title.text = getString(R.string.text_ranking)
        toolbarContainer.toolbar.navigationIcon =
            requireContext().getDrawable(R.drawable.btn_back_white_n)
        toolbarContainer.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

    }


    override fun getLayoutId(): Int {
        return R.layout.fragment_ranking
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {

    }
}