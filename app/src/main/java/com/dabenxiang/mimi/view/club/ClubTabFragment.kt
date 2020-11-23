package com.dabenxiang.mimi.view.club

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.club.adapter.ClubTabAdapter
import com.dabenxiang.mimi.view.club.topic.TopicTabAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_tab_club.*

class ClubTabFragment : BaseFragment() {

    companion object{
        const val TAB_FOLLOW = 0
        const val TAB_RECOMMEND = 1
        const val TAB_LATEST = 2
        const val TAB_CLIP = 3
        const val TAB_PICTURE = 4
        const val TAB_NOVEL = 5
    }

    override fun getLayoutId() = R.layout.fragment_tab_club
    override fun setupObservers() {}
    override fun setupListeners() {}

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        club_view_pager.adapter = ClubTabAdapter(this)
        club_view_pager.isSaveEnabled =false
        TabLayoutMediator(club_tabs, club_view_pager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()

        val topicTabAdapter = TopicTabAdapter()
        topic_tabs.adapter = topicTabAdapter
        topicTabAdapter.setTestData()
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

}


