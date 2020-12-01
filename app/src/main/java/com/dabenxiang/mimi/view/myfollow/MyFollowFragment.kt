package com.dabenxiang.mimi.view.myfollow

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.BaseItemListener
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import com.dabenxiang.mimi.model.enums.ClickType
import com.dabenxiang.mimi.view.adapter.FollowClubAdapter
import com.dabenxiang.mimi.view.adapter.FollowPersonalAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_my_follow.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class MyFollowFragment : BaseFragment() {
    private val viewModel: MyFollowViewModel by viewModels()

    companion object {
        const val NO_DATA = 0
        const val TYPE_PERSONAL = 0
        const val TYPE_CLUB = 1
    }

    private val clubFollowAdapter by lazy { FollowClubAdapter(listener) }
    private val memberFollowAdapter by lazy { FollowPersonalAdapter(listener) }
    private val listener = object : BaseItemListener {
        override fun onItemClick(item: Any, type: ClickType) {
            Timber.i("MyFollowFragment onItemClick $item")
            if (item is MemberFollowItem) {
                if (type == ClickType.TYPE_AUTHOR) {

                } else if (type == ClickType.TYPE_FOLLOW) {

                }
            } else if (item is ClubFollowItem) {
                if (type == ClickType.TYPE_CLUB) {

                } else if (type == ClickType.TYPE_FOLLOW) {

                }
            }
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
            if (layout_tab.selectedTabPosition == TYPE_PERSONAL) refreshUi(TYPE_PERSONAL, it)
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
            if (layout_tab.selectedTabPosition == TYPE_PERSONAL) {
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
                    if (layout_tab.selectedTabPosition == TYPE_PERSONAL)
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
                viewModel.getData(clubFollowAdapter, memberFollowAdapter, tab.position)
            }

        })

        vpAdapter = MyFollowViewPagerAdapter(
            requireContext(),
            memberFollowAdapter,
            clubFollowAdapter
        ) {
            viewModel.getData(clubFollowAdapter, memberFollowAdapter, 0)
        }
        vp.adapter = vpAdapter
        layout_tab.setupWithViewPager(vp)

        tv_clean.visibility = View.VISIBLE
        tv_title.setText(R.string.follow_title)
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
                TYPE_PERSONAL -> {
                    memberFollowAdapter.submitData(PagingData.empty())
                    viewModel.getPersonalList()
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
