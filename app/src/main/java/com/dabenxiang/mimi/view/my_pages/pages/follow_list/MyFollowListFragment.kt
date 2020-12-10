package com.dabenxiang.mimi.view.my_pages.pages.follow_list

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.BaseItemListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import com.dabenxiang.mimi.model.enums.ClickType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.topic_detail.TopicTabFragment
import com.dabenxiang.mimi.view.my_pages.base.MyPagesViewModel
import com.dabenxiang.mimi.view.my_pages.follow.MyFollowFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_my_follow_list.*
import timber.log.Timber

class MyFollowListFragment(val type: Int) : BaseFragment() {

    private val viewModel: MyFollowListViewModel by viewModels()
    private val myPagesViewModel: MyPagesViewModel by viewModels({ requireParentFragment() })

    private val memberAdapter by lazy {
        MemberFollowPeopleAdapter(requireContext(), listener, viewModel.viewModelScope)
    }

    private val clubAdapter: ClubFollowPeopleAdapter by lazy {
        ClubFollowPeopleAdapter(requireContext(), listener, viewModel.viewModelScope)
    }

    override fun getLayoutId() = R.layout.fragment_my_follow_list

    override val bottomNavigationVisibility: Int
        get() = View.GONE

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
                            navigateTo(
                                NavigateItem.Destination(
                                    R.id.action_myFollowFragment_to_navigation_my_post,
                                    bundle
                                )
                            )
                        }
                        ClickType.TYPE_FOLLOW -> {
                            val list = ArrayList<MemberFollowItem>()
                            list.add(item)
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
                            val clubItem = MemberClubItem(
                                id = item.clubId,
                                avatarAttachmentId = item.avatarAttachmentId,
                                tag = item.tag,
                                title = item.name,
                                description = item.description,
                                followerCount = item.followerCount,
                                postCount = item.postCount,
                                isFollow = true
                            )

                            val bundle = TopicTabFragment.createBundle(clubItem)
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
        myPagesViewModel.deleteAll.observe(this) {
            if (type == it) {
                when (it) {
                    MyFollowFragment.TAB_FOLLOW_PEOPLE -> viewModel.cleanAllFollowMember(
                        memberAdapter.snapshot().items
                    )
                    MyFollowFragment.TAB_FOLLOW_CLUB -> viewModel.cleanAllFollowClub(clubAdapter.snapshot().items)
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

        viewModel.postCount.observe(this) {
            when (type) {
                MyFollowFragment.TAB_FOLLOW_PEOPLE -> {
                    tv_title_count.text = getString(R.string.follow_members_total_num, "".plus(it))
                }
                MyFollowFragment.TAB_FOLLOW_CLUB -> {
                    tv_title_count.text = getString(R.string.follow_circle_total_num, "".plus(it))
                }
            }
            if (it == 0) {
                text_page_empty.text = getString(R.string.follow_no_data)
                id_empty_group.visibility = View.VISIBLE
                recycler_view.visibility = View.INVISIBLE
            } else {
                id_empty_group.visibility = View.GONE
                recycler_view.visibility = View.VISIBLE
            }
            myPagesViewModel.changeDataCount(type, it)
            layout_refresh.isRefreshing = false
        }

        viewModel.showProgress.observe(this) {
            layout_refresh.isRefreshing = it
        }

        viewModel.adWidth = GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
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
        if (viewModel.postCount.value ?: -1 <= 0) {
            getData()
        }
    }

    fun getData() {
        when (type) {
            MyFollowFragment.TAB_FOLLOW_PEOPLE -> {
                viewModel.getMemberData(memberAdapter)
            }
            MyFollowFragment.TAB_FOLLOW_CLUB -> {
                viewModel.getClubFollowData(clubAdapter)
            }
        }
    }
}
