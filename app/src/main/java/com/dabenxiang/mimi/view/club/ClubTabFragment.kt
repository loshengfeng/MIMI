package com.dabenxiang.mimi.view.club

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.paging.PagingData
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.adapter.ClubTabAdapter
import com.dabenxiang.mimi.view.club.adapter.TopicItemListener
import com.dabenxiang.mimi.view.club.adapter.TopicListAdapter
import com.dabenxiang.mimi.view.club.topic.TopicDetailFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_tab_club.*
import kotlinx.android.synthetic.main.fragment_tab_club.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    private lateinit var tabLayoutMediator: TabLayoutMediator
    private val viewModel: ClubTabViewModel by viewModels()

    private val topicListAdapter by lazy {
        TopicListAdapter(object : TopicItemListener {
            override fun itemClicked(clubItem: MemberClubItem, position: Int) {
                val bundle = TopicDetailFragment.createBundle(clubItem)
                navigateTo(
                        NavigateItem.Destination(
                                R.id.action_clubTabFragment_to_topicDetailFragment,
                                bundle
                        )
                )
            }

            override fun getAttachment(id: Long?, view: ImageView, type: LoadImageType) {
                viewModel.loadImage(id, view, type)
            }

        })
    }


    override fun getLayoutId() = R.layout.fragment_tab_club
    override fun setupObservers() {}
    override fun setupListeners() {}

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel.clubCount.observe(this, {
            topic_group.visibility = if (it <= 0) View.GONE else View.VISIBLE
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getClubItemList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(getLayoutId(), container, false)
        view.club_view_pager.adapter = ClubTabAdapter(childFragmentManager, lifecycle)
        view.club_view_pager.offscreenPageLimit =7
        tabLayoutMediator = TabLayoutMediator(view.club_tabs,  view.club_view_pager) { tab, position ->
            tab.text = getTabTitle(position)
        }
        tabLayoutMediator.attach()
        view.topic_tabs.adapter = topicListAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        search_bar.addTextChangedListener {
            //TODO search
        }

    }

    override fun onResume() {
        super.onResume()
        Timber.i("ClubTabFragment onResume")

    }
    
    override fun onDestroy() {
        super.onDestroy()
        Timber.i("ClubTabFragment onDestroy")

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
            delay(100)
            topicListAdapter.submitData(PagingData.empty())
            viewModel.getClubItemList()
                .collectLatest {
                    topicListAdapter.submitData(it)
                }
        }
    }

}


