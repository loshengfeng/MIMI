package com.dabenxiang.mimi.view.myfollow

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.adapter.ClubFollowAdapter
import com.dabenxiang.mimi.view.adapter.MemberFollowAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.topic.TopicDetailFragment
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_my_follow.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyFollowFragment : BaseFragment() {

    private val viewModel: MyFollowViewModel by viewModels()

    companion object {
        const val NO_DATA = 0
        const val TYPE_MEMBER = 0
        const val TYPE_CLUB = 1
    }

    private val clubFollowAdapter by lazy { ClubFollowAdapter(clubFollowListener) }
    private val clubFollowListener = object : ClubFollowAdapter.EventListener {
        override fun onDetail(item: ClubFollowItem) {
            viewModel.getClub(item.clubId)
        }

        override fun onGetAttachment(id: Long, view: ImageView) {
            viewModel.loadImage(id, view, LoadImageType.AVATAR)
        }

        override fun onCancelFollow(clubId: Long, position: Int) {
            viewModel.cancelFollowClub(clubId, position)
        }
    }

    private val memberFollowAdapter by lazy { MemberFollowAdapter(memberFollowListener) }
    private val memberFollowListener = object : MemberFollowAdapter.EventListener {
        override fun onDetail(item: MemberFollowItem) {
            val bundle = MyPostFragment.createBundle(
                item.userId, item.friendlyName,
                isAdult = true,
                isAdultTheme = true
            )
            navigateTo(
                NavigateItem.Destination(R.id.action_myFollowFragment_to_navigation_my_post, bundle)
            )
        }

        override fun onGetAttachment(id: Long, view: ImageView) {
            viewModel.loadImage(id, view, LoadImageType.AVATAR)
        }

        override fun onCancelFollow(userId: Long, position: Int) {
            viewModel.cancelFollowMember(userId, position)
        }
    }

    private var vpAdapter: MyFollowViewPagerAdapter? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.clubCount.observe(this, Observer {
            if (layout_tab.selectedTabPosition == TYPE_CLUB) refreshUi(TYPE_CLUB, it)
        })

        viewModel.memberCount.observe(this, Observer {
            if (layout_tab.selectedTabPosition == TYPE_MEMBER) refreshUi(TYPE_MEMBER, it)
        })

        viewModel.clubDetail.observe(this, Observer {
            when (it) {
                is Success -> {
                    val bundle = TopicDetailFragment.createBundle(it.result)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_myFollowFragment_to_topicDetailFragment,
                            bundle
                        )
                    )
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.cleanResult.observe(this, Observer {
            when (it) {
                is Loading -> vpAdapter?.changeIsRefreshing(layout_tab.selectedTabPosition, true)
                is Loaded -> vpAdapter?.changeIsRefreshing(layout_tab.selectedTabPosition, false)
                is Empty -> getData()
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.cancelOneClub.observe(this, Observer {
            when (it) {
                is Loading -> vpAdapter?.changeIsRefreshing(layout_tab.selectedTabPosition, true)
                is Loaded -> vpAdapter?.changeIsRefreshing(layout_tab.selectedTabPosition, false)
                is Success -> {
                    clubFollowAdapter.removedPosList.add(it.result)
                    clubFollowAdapter.notifyItemChanged(it.result)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.cleanClubRemovedPosList.observe(this, Observer {
            clubFollowAdapter.removedPosList.clear()
        })

        viewModel.cancelOneMember.observe(this, Observer {
            when (it) {
                is Loading -> vpAdapter?.changeIsRefreshing(layout_tab.selectedTabPosition, true)
                is Loaded -> vpAdapter?.changeIsRefreshing(layout_tab.selectedTabPosition, false)
                is Success -> {
                    memberFollowAdapter.removedPosList.add(it.result)
                    memberFollowAdapter.notifyItemChanged(it.result)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.cleanMemberRemovedPosList.observe(this, Observer {
            memberFollowAdapter.removedPosList.clear()
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        useAdultTheme(false)
    }

    override fun setupFirstTime() {
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_my_follow
    }

    override fun setupObservers() {}

    private val onCleanDialogListener = object : OnCleanDialogListener {
        override fun onClean() {
            if (layout_tab.selectedTabPosition == TYPE_MEMBER) {
                viewModel.cleanAllFollowMember()
            } else {
                viewModel.cleanAllFollowClub()
            }
        }
    }

    override fun setupListeners() {
        View.OnClickListener { btnView ->
            when (btnView.id) {
                R.id.tv_back -> navigateTo(NavigateItem.Up)
                R.id.tv_clean -> CleanDialogFragment.newInstance(
                    onCleanDialogListener,
                    if (layout_tab.selectedTabPosition == TYPE_MEMBER)
                        R.string.follow_clean_member_dlg_msg
                    else
                        R.string.follow_clean_club_dlg_msg
                ).also {
                    it.show(
                        requireActivity().supportFragmentManager,
                        CleanDialogFragment::class.java.simpleName
                    )
                }
            }
        }.also {
            tv_back.setOnClickListener(it)
            tv_clean.setOnClickListener(it)
        }
    }

    override fun initSettings() {
        layout_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                getData()
            }

        })
        memberFollowAdapter.addLoadStateListener { loadState ->
            handleLoadState(loadState.refresh)
            handleLoadState(loadState.append)
        }
        clubFollowAdapter.addLoadStateListener { loadState ->
            handleLoadState(loadState.refresh)
            handleLoadState(loadState.append)
        }
        vpAdapter = MyFollowViewPagerAdapter(
            requireContext(),
            memberFollowAdapter,
            clubFollowAdapter
        ) {
            getData()
        }
        vp.adapter = vpAdapter
        layout_tab.setupWithViewPager(vp)

        tv_clean.visibility = View.VISIBLE
        tv_title.setText(R.string.follow_title)
    }

    private fun handleLoadState(loadState: LoadState) {
        when (loadState) {
            is LoadState.Loading -> vpAdapter?.changeIsRefreshing(
                layout_tab.selectedTabPosition,
                true
            )
            is LoadState.NotLoading -> vpAdapter?.changeIsRefreshing(
                layout_tab.selectedTabPosition,
                false
            )
            is LoadState.Error -> onApiError(loadState.error)
        }
    }

    private fun refreshUi(type: Int, size: Int) {
        tv_clean.isEnabled = size != NO_DATA
        vpAdapter?.refreshUi(type, size)
    }

    private var job: Job? = null
    private fun getData() {
        job?.cancel()
        job = lifecycleScope.launch {
            when (layout_tab.selectedTabPosition) {
                TYPE_MEMBER -> {
                    memberFollowAdapter.submitData(PagingData.empty())
                    viewModel.getMemberList()
                        .collectLatest {
                            memberFollowAdapter.submitData(it)
                        }
                }
                TYPE_CLUB -> {
                    clubFollowAdapter.submitData(PagingData.empty())
                    viewModel.getClubList()
                        .collectLatest {
                            clubFollowAdapter.submitData(it)
                        }
                }
            }
        }
    }
}
