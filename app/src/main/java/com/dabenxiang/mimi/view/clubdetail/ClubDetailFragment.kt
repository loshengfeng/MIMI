package com.dabenxiang.mimi.view.clubdetail

import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.fragment_club_detail.*

class ClubDetailFragment() : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"

        fun createBundle(
            item: MemberClubItem
        ): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId(): Int {
        return R.layout.fragment_club_detail
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (arguments?.getSerializable(KEY_DATA) as MemberClubItem).also { item ->
            tv_title.text = item.title
            tv_desc.text = item.description
            tv_follow_count.text = item.followerCount.toString()
            tv_post_count.text = item.postCount.toString()

            val bitmap = LruCacheUtils.getLruCache(item.avatarAttachmentId.toString())
            bitmap?.also { Glide.with(requireContext()).load(it).circleCrop().into(iv_avatar) }
        }
    }

    override fun setupObservers() {
    }

    override fun setupListeners() {
    }
}