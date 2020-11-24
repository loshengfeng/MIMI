package com.dabenxiang.mimi.view.club

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.paging.PagingData
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.club.adapter.ClubTabAdapter
import com.dabenxiang.mimi.view.club.topic.TopicItemListener
import com.dabenxiang.mimi.view.club.topic.TopicListAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_tab_club.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class ClubTabFragment : BaseFragment() {

    companion object{
        const val TAB_FOLLOW = 0
        const val TAB_RECOMMEND = 1
        const val TAB_LATEST = 2
        const val TAB_CLIP = 3
        const val TAB_PICTURE = 4
        const val TAB_NOVEL = 5
    }

    private val viewModel: ClubTabViewModel by viewModels()

    private val topicListAdapter by lazy {
        TopicListAdapter(object : TopicItemListener {
            override fun itemClicked(drink: MemberClubItem, position: Int) {
                    //TODO
            }

        })
    }

    override fun getLayoutId() = R.layout.fragment_tab_club
    override fun setupObservers() {}
    override fun setupListeners() {}

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel.clubCount.observe(this, {
            topic_group.visibility = if(it <=0) View.GONE else View.VISIBLE
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        club_view_pager.adapter = ClubTabAdapter(this)
        club_view_pager.isSaveEnabled =false
        TabLayoutMediator(club_tabs, club_view_pager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()

        topic_tabs.adapter = topicListAdapter

    }

    override fun onResume() {
        super.onResume()
        Timber.i("ClubTabFragment onResume")
        getClubItemList()
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            TAB_FOLLOW -> getString(R.string.club_tab_follow)
            TAB_RECOMMEND -> getString(R.string.club_tab_recommend)
            TAB_LATEST -> getString(R.string.club_tab_latest)
            TAB_CLIP -> getString(R.string.club_tab_clip)
            TAB_PICTURE -> getString(R.string.club_tab_picture)
            TAB_NOVEL -> getString(R.string.club_tab_novel)
            else -> null
        }
    }

    private fun getClubItemList() {
        Timber.i("getClubItemList")
        CoroutineScope(Dispatchers.IO).launch {
            topicListAdapter.submitData(PagingData.empty())
            viewModel.getClubItemList()
                .collectLatest {
                    topicListAdapter.submitData(it)
                }
        }
    }

}


