package com.dabenxiang.mimi.view.myfollow.follow_people

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils

class  MyFollowPeopleFragment : BaseFragment() {
    private val viewModel: MyFollowPeopleViewModel by viewModels()
    override fun getLayoutId() = R.layout.fragment_my_follow

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
    }


}
