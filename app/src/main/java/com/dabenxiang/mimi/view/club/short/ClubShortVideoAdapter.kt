package com.dabenxiang.mimi.view.club.post

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.model.api.vo.MemberPostItem

class ClubShortVideoAdapter(f: Fragment, val data: MemberPostItem): FragmentStateAdapter(f) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {

        return  ClubVideoCommentFragment.createBundle(data)
    }
}