package com.dabenxiang.mimi.view.player.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BasePlayerFragment
import com.dabenxiang.mimi.view.club.post.ClubCommentFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_v2_player.*

class ClipPlayerFragment : BasePlayerFragment() {

    companion object {
        const val KEY_PLAYER_SRC = "KEY_PLAYER_SRC"
        private const val KEY_POSITION = "position"

        fun createBundle(id: Long, position: Int = 0): Bundle {
            return Bundle().also {
                it.putLong(KEY_PLAYER_SRC, id)
                it.putSerializable(KEY_POSITION, position)
            }
        }
    }

    private val viewModel: ClipPlayerViewModel by viewModels()

    override fun getViewPagerCount() = 2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player_pager.post {
            val position = arguments?.getInt(KEY_POSITION, 0)
            player_pager.currentItem = position ?: 0
        }
    }

    override fun createViewPagerFragment(position: Int): Fragment = when (position) {
        0 -> ClipPlayerDescriptionFragment()
        else -> {
            val memberPostItem = MemberPostItem()
            memberPostItem.id = viewModel.videoContentId
            ClubCommentFragment.createBundle(memberPostItem, true)
        }
    }

    override fun getTabTitle(tab: TabLayout.Tab, position: Int) {
        val tabs = resources.getStringArray(R.array.clip_play_tabs)
        val view = View.inflate(requireContext(), R.layout.custom_tab, null)
        val textView = view?.findViewById<TextView>(R.id.tv_title)
        textView?.text = tabs[position]
        textView?.textSize = 16f
        tab.customView = view
    }

    override fun onResume() {
        super.onResume()
        getPostContent()
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.memberPostContentSource.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Loading -> progressHUD.show()
                is ApiResult.Loaded -> progressHUD.dismiss()
                is ApiResult.Success -> {
                    parsingPostContent(it.result)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }

        viewModel.videoStreamingUrl.observe(viewLifecycleOwner) {
            setupPlayUrl(it, true)
        }

        viewModel.attachmentResult.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Success -> setupPlayUrl(it.result, true)
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }
    }

    private fun getPostContent() {
        (arguments?.getLong(KEY_PLAYER_SRC))?.also {
            contentId = it
            viewModel.videoContentId = it
            viewModel.getPostDetail()
        }
    }

    private fun parsingPostContent(postItem: MemberPostItem) {
        viewModel.parsingM3u8Source(
            requireContext(),
            Gson().fromJson(postItem.content, MediaContentItem::class.java)
        )
    }
}