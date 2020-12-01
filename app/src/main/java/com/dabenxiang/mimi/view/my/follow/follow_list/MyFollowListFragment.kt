package com.dabenxiang.mimi.view.my.follow.follow_list

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.BaseItemListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import com.dabenxiang.mimi.model.enums.ClickType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.my.follow.MyFollowFragment
import com.dabenxiang.mimi.view.my.follow.MyFollowViewModel
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_club_short.*
import kotlinx.android.synthetic.main.fragment_my_follow_list.*
import kotlinx.android.synthetic.main.fragment_my_follow_list.layout_refresh

class MyFollowListFragment(val type: Int) : BaseFragment() {
    private val viewModel: MyFollowListViewModel by viewModels()
    private val myFollowViewModel: MyFollowViewModel by viewModels({ requireParentFragment() })

    private val memberAdapter by lazy {
         MemberFollowPeopleAdapter(requireContext(), listener)
    }
    private val clubAdapter: ClubFollowPeopleAdapter  by lazy {
        ClubFollowPeopleAdapter(requireContext(), listener)
    }

    override fun getLayoutId() = R.layout.fragment_my_follow_list

    private val listener: BaseItemListener = object : BaseItemListener {
        override fun onItemClick(item: Any, type: ClickType) {
            var userId = 0L
            var name = ""
            when (this@MyFollowListFragment.type) {
                // 關注的人
                MyFollowFragment.TAB_FOLLOW_PEOPLE -> {
                    (item as MemberFollowItem)
                    userId = item.userId
                    name = item.friendlyName

                    when (type) {
                        ClickType.TYPE_ITEM -> {
                            val bundle = MyPostFragment.createBundle(
                                    userId, name,
                                    isAdult = true,
                                    isAdultTheme = false
                            )
                            navigateTo(NavigateItem.Destination(R.id.action_myFollowFragment_to_navigation_my_post, bundle))
                        }
                        ClickType.TYPE_FOLLOW -> {
                            val list = ArrayList<MemberFollowItem>()
                            list.add(MemberFollowItem(id = item.id))
                            viewModel.cleanAllFollowMember(list)
                        }
                    }
                }

                // 關注的圈子
                MyFollowFragment.TAB_FOLLOW_CLUB -> {
                    (item as ClubFollowItem)
//                    userId = item.clubId
//                    name = item.name

                    when (type) {
                        ClickType.TYPE_ITEM -> {
                        }
                        ClickType.TYPE_FOLLOW -> {
                            val list = ArrayList<ClubFollowItem>()
                            list.add(item)
                            viewModel.cleanAllFollowClub(list)
                        }
                    }
                }
            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myFollowViewModel.deleteFollow.observe(this) {
            if (type == it) {
                when (it) {
                    MyFollowFragment.TAB_FOLLOW_PEOPLE -> {
                        viewModel.cleanAllFollowMember(memberAdapter.snapshot().items)
                    }
                    MyFollowFragment.TAB_FOLLOW_CLUB -> {
                        viewModel.cleanAllFollowClub(clubAdapter?.snapshot()?.items)
                    }
                }
            }
        }

        viewModel.cleanResult.observe(this) {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Loaded -> progressHUD?.dismiss()
                is ApiResult.Empty -> getData()
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (type) {
            MyFollowFragment.TAB_FOLLOW_PEOPLE -> {
                recycler_view.adapter = memberAdapter
            }
            MyFollowFragment.TAB_FOLLOW_CLUB -> {
                recycler_view.adapter = clubAdapter
            }
        }

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            getData()
        }
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    private fun getData(){
        when (type) {
            MyFollowFragment.TAB_FOLLOW_PEOPLE -> {
                memberAdapter.let { viewModel.getMemberData(it) }
            }
            MyFollowFragment.TAB_FOLLOW_CLUB -> {
                clubAdapter.let { viewModel.getClubFollowData(it) }
            }
        }
    }
}
