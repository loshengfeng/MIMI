package com.dabenxiang.mimi.view.club.post

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.view.club.pic.ClubPicDetailFragment
import com.dabenxiang.mimi.view.club.text.ClubTextDetailFragment

class ClubPostPagerAdapter(f: Fragment, val data: MemberPostItem, val type: AdultTabType): FragmentStateAdapter(f) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        val adcode =
            if(type == AdultTabType.TEXT)
                "text_inside"
            else
                "image_inside"
        return if (position == 0) {
            if (type == AdultTabType.TEXT) {
                ClubTextDetailFragment.createBundle(data, adcode)
            } else {
                ClubPicDetailFragment.createBundle(data, adcode)
            }
        } else {
            ClubCommentFragment.createBundle(data, adCode = adcode)
        }
    }
}