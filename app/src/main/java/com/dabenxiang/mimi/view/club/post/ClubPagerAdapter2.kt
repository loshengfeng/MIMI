package com.dabenxiang.mimi.view.club.post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.adapter.MemberPostPagedAdapter
import com.dabenxiang.mimi.view.clubdetail.ClubPagerViewHolder

class ClubPagerAdapter2(f: Fragment, val data: MemberPostItem): FragmentStateAdapter(f) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        if (position == 0) {
            return ClubTextDetailFragment.createBundle(data)
        } else {
            return ClubCommentFragment.createBundle(data)
        }
    }
}