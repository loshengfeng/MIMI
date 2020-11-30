package com.dabenxiang.mimi.view.like

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
import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.adapter.ClubLikeAdapter
import com.dabenxiang.mimi.view.adapter.MiMiLikeAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_my_follow.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class LikeFragment : BaseFragment() {

    private val viewModel: LikeViewModel by viewModels()

    companion object {
        const val NO_DATA = 0
        const val TYPE_POST = 0
        const val TYPE_MIMI = 1
    }

    private val clublikeAdapter by lazy { ClubLikeAdapter(clubLikeListener) }
    private val clubLikeListener = object : ClubLikeAdapter.EventListener {
        override fun onDetail(item: PostFavoriteItem) {
//            viewModel.getClub(item.clubId)
            val bundle = MyPostFragment.createBundle(
                item.posterId, item.title,
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

        override fun onCancelFollow(clubId: Long, position: Int) {
            viewModel.cancelFollowClub(clubId, position)
        }
    }

    private val mimilikeAdapter by lazy { MiMiLikeAdapter(mimiLikeListener) }
    private val mimiLikeListener = object : MiMiLikeAdapter.EventListener {
        override fun onDetail(item: PostFavoriteItem) {
            val bundle = MyPostFragment.createBundle(
                item.posterId, item.title,
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

    private var vpAdapter: LikeViewPagerAdapter? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.clubCount.observe(this, Observer {
            if (layout_tab.selectedTabPosition == TYPE_POST) refreshUi(TYPE_POST, it)
        })

        viewModel.mimiCount.observe(this, Observer {
            if (layout_tab.selectedTabPosition == TYPE_MIMI) refreshUi(TYPE_MIMI, it)
        })

        viewModel.clubDetail.observe(this, Observer {
            when (it) {
                is Success -> {
//                    val bundle = TopicDetailFragment.createBundle(it.result)
//                    navigateTo(
//                        NavigateItem.Destination(
//                            R.id.action_myFollowFragment_to_topicDetailFragment,
//                            bundle
//                        )
//                    )
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
                    clublikeAdapter.removedPosList.add(it.result)
                    clublikeAdapter.notifyItemChanged(it.result)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.cleanClubRemovedPosList.observe(this, Observer {
            clublikeAdapter.removedPosList.clear()
        })

        viewModel.cancelOneMember.observe(this, Observer {
            when (it) {
                is Loading -> vpAdapter?.changeIsRefreshing(layout_tab.selectedTabPosition, true)
                is Loaded -> vpAdapter?.changeIsRefreshing(layout_tab.selectedTabPosition, false)
                is Success -> {
                    mimilikeAdapter.removedPosList.add(it.result)
                    mimilikeAdapter.notifyItemChanged(it.result)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.cleanMemberRemovedPosList.observe(this, Observer {
            mimilikeAdapter.removedPosList.clear()
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
        return R.layout.fragment_my_like
    }

    override fun setupObservers() {}

    private val onCleanDialogListener = object : OnCleanDialogListener {
        override fun onClean() {
            if (layout_tab.selectedTabPosition == TYPE_POST) {
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
                    if (layout_tab.selectedTabPosition == TYPE_POST)
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
        val tabs = resources.getStringArray(R.array.like_tabs)
        for (i in tabs) {
            layout_tab.addTab(layout_tab.newTab().setText(i))
        }
        layout_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                getData()
            }
        })
        mimilikeAdapter.addLoadStateListener { loadState ->
            handleLoadState(loadState.refresh)
            handleLoadState(loadState.append)
        }
        clublikeAdapter.addLoadStateListener { loadState ->
            handleLoadState(loadState.refresh)
            handleLoadState(loadState.append)
        }
        vpAdapter = LikeViewPagerAdapter(
            requireContext(),
            mimilikeAdapter,
            clublikeAdapter
        ) {
            getData()
        }
        vp.adapter = vpAdapter
        layout_tab.setupWithViewPager(vp)

        tv_clean.visibility = View.VISIBLE
        tv_title.setText(R.string.like_title)
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
                TYPE_MIMI -> {
                    mimilikeAdapter.submitData(PagingData.empty())
                    viewModel.getMemberList()
                        .collectLatest {
                            mimilikeAdapter.submitData(it)
                        }
                }
                TYPE_POST -> {
                    clublikeAdapter.submitData(PagingData.empty())
                    viewModel.getClubList()
                        .collectLatest {
                            clublikeAdapter.submitData(it)
                        }
                }
            }
        }
    }
}
