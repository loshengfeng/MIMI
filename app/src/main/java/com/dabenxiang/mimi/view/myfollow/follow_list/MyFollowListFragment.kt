package com.dabenxiang.mimi.view.myfollow.follow_list

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.myfollow.MyFollowFragment
import com.dabenxiang.mimi.view.myfollow.MyFollowViewModel
import com.dabenxiang.mimi.widget.utility.GeneralUtils

class  MyFollowListFragment(val type: Int) : BaseFragment() {
    private val viewModel: MyFollowListViewModel by viewModels()
    private val myFollowViewModel: MyFollowViewModel by viewModels({requireParentFragment()})
    override fun getLayoutId() = R.layout.fragment_my_follow_list

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myFollowViewModel.deleteFollow.observe(this, {
            if (type == it) {
               when(it){
                   //TODO Need adapter
                   MyFollowFragment.TAB_FOLLOW_PEOPLE -> viewModel.cleanAllFollowMember(listOf())
                   MyFollowFragment.TAB_FOLLOW_CLUB -> viewModel.cleanAllFollowClub(listOf())
               }
            }
        })

        viewModel.cleanResult.observe(this, {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Loaded -> progressHUD?.dismiss()
                is ApiResult.Empty -> {
                    // TODO viewModel.getData(adapter)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

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
