package com.dabenxiang.mimi.view.my.follow.follow_list

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.callback.BaseItemListener
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import com.dabenxiang.mimi.model.enums.ClickType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.my.follow.MyFollowFragment
import com.dabenxiang.mimi.view.my.follow.MyFollowViewModel
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.topic.TopicDetailFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_my_follow_list.*
import kotlinx.android.synthetic.main.fragment_my_follow_list.layout_refresh

class MyFollowListFragment(val type: Int) : BaseFragment() {
    private val viewModel: MyFollowListViewModel by viewModels()
    private val myFollowViewModel: MyFollowViewModel by viewModels({ requireParentFragment() })
    private var memberAdapter: MemberFollowPeopleAdapter? = null
    private var clubAdapter: ClubFollowPeopleAdapter? = null


    override fun getLayoutId() = R.layout.fragment_my_follow_list

    private val listener: BaseItemListener = object : BaseItemListener {
        override fun onItemClick(item: Any, type: ClickType) {
            when (this@MyFollowListFragment.type) {
                // 關注的人
                MyFollowFragment.TAB_FOLLOW_PEOPLE -> {
                    (item as MemberFollowItem)
                    val userId = item.userId
                    val name = item.friendlyName

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
                        else -> {
                        }
                    }
                }

                // 關注的圈子
                MyFollowFragment.TAB_FOLLOW_CLUB -> {
                    (item as ClubFollowItem)

                    when (type) {
                        ClickType.TYPE_ITEM -> {
                            val clubItem = MemberClubItem(id = item.id, avatarAttachmentId = item.avatarAttachmentId,
                                    tag = item.tag, title = item.name, description = item.description, followerCount = item.followerCount, postCount = item.postCount)

                            val bundle = TopicDetailFragment.createBundle(clubItem)
                            navigateTo(
                                    NavigateItem.Destination(
                                            R.id.action_to_topicDetailFragment,
                                            bundle
                                    )
                            )
                        }
                        ClickType.TYPE_FOLLOW -> {
                            val list = ArrayList<ClubFollowItem>()
                            list.add(item)
                            viewModel.cleanAllFollowClub(list)
                        }
                        else -> {
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
                    //TODO Need adapter
                    MyFollowFragment.TAB_FOLLOW_PEOPLE -> viewModel.cleanAllFollowMember(memberAdapter?.snapshot()?.items
                            ?: listOf())
                    MyFollowFragment.TAB_FOLLOW_CLUB -> viewModel.cleanAllFollowClub(clubAdapter?.snapshot()?.items
                            ?: listOf())
                }
            }
        }

        viewModel.cleanResult.observe(this) {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Loaded -> progressHUD?.dismiss()
                is ApiResult.Empty -> {
                    getData()
                }
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
                memberAdapter = MemberFollowPeopleAdapter(requireContext(), listener)
                recycler_view.adapter = memberAdapter
            }
            MyFollowFragment.TAB_FOLLOW_CLUB -> {
                clubAdapter = ClubFollowPeopleAdapter(requireContext(), listener)
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


    fun getData() {
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
