package com.dabenxiang.mimi.view.player.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BasePlayerFragment
import com.dabenxiang.mimi.view.club.post.ClubCommentFragment
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson

class ClipPlayerFragment: BasePlayerFragment() {

    companion object {
        private const val KEY_PLAYER_SRC = "KEY_PLAYER_SRC"

        fun createBundle(id: Long): Bundle {
            return Bundle().also {
                it.putLong(KEY_PLAYER_SRC, id)
            }
        }
    }

    private val viewModel: ClipPlayerViewModel by activityViewModels()

    override fun getViewPagerCount() = 2

    override fun createViewPagerFragment(position: Int): Fragment = when(position) {
        0 -> ClipPlayerDescriptionFragment()
        else -> {
            val memberPostItem = MemberPostItem()
            memberPostItem.id = viewModel.videoContentId
            ClubCommentFragment.createBundle(memberPostItem, true)
        }
    }

    override fun getTabTitle(tab: TabLayout.Tab, position: Int) = when(position) {
        0 -> tab.text = "视频简介"
        else -> tab.text = "评论"
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
                is ApiResult.Success -> {
                    parsingPostContent(it.result)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }

        viewModel.videoStreamingUrl.observe(viewLifecycleOwner) {
            setupPlayUrl(it, true)
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
        viewModel.parsingM3u8Source(Gson().fromJson(postItem.content, MediaContentItem::class.java))
    }
}