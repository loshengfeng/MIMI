package com.dabenxiang.mimi.view.myfollow.follow_list

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.callback.BaseItemListener
import com.dabenxiang.mimi.model.enums.ClickType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.myfollow.MyFollowFragment
import com.dabenxiang.mimi.view.myfollow.MyFollowViewModel
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_my_follow_list.*

class MyFollowListFragment(val type: Int) : BaseFragment() {
    private val viewModel: MyFollowListViewModel by viewModels()
    private val myFollowViewModel: MyFollowViewModel by viewModels({ requireParentFragment() })
    private var memberAdapter: MemberFollowPeopleAdapter? = null
    private var clubAdapter: ClubFollowPeopleAdapter? = null


    override fun getLayoutId() = R.layout.fragment_my_follow_list

    private val listener: BaseItemListener = object : BaseItemListener {
        override fun onItemClick(item: Any, type: ClickType) {
            when (type) {
                ClickType.TYPE_ITEM -> {
//                    val bundle = MyPostFragment.createBundle(
//                            userId, name,
//                            isAdult = true,
//                            isAdultTheme = false
//                    )
//                    navigateTo(NavigateItem.Destination(R.id.action_fansListFragment_to_navigation_my_post, bundle))
                }
                ClickType.TYPE_FOLLOW -> {
                    //todo 取消 follow
                }
            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myFollowViewModel.deleteFollow.observe(this, {
            if (type == it) {
                when (it) {
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
        when (type) {
            MyFollowFragment.TAB_FOLLOW_PEOPLE -> {
                memberAdapter = MemberFollowPeopleAdapter(requireContext(), listener)
                recycler_view.adapter = memberAdapter
            }
            MyFollowFragment.TAB_FOLLOW_CLUB -> {
                clubAdapter = ClubFollowPeopleAdapter(requireContext(), listener)
                recycler_view.adapter = clubAdapter
            }
        }

    }

    override fun onResume() {
        super.onResume()
        when (type) {
            MyFollowFragment.TAB_FOLLOW_PEOPLE -> {
                memberAdapter?.let { viewModel.getMemberData(it) }
            }
            MyFollowFragment.TAB_FOLLOW_CLUB -> {
                clubAdapter?.let { viewModel.getClubFollowData(it) }
            }
        }
    }
}
