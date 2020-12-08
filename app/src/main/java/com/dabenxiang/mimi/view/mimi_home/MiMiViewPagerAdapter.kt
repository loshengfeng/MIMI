package com.dabenxiang.mimi.view.mimi_home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.model.api.vo.SecondMenuItem
import com.dabenxiang.mimi.model.enums.LayoutType
import com.dabenxiang.mimi.view.actor.ActorFragment
import com.dabenxiang.mimi.view.generalvideo.GeneralVideoFragment
import com.dabenxiang.mimi.view.recommend.RecommendFragment

class MiMiViewPagerAdapter(
    fragment: Fragment,
    private val secondMenuItems: List<SecondMenuItem>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return secondMenuItems.size
    }

    override fun createFragment(position: Int): Fragment {
        val item = secondMenuItems[position]
        return when (item.type) {
            LayoutType.RECOMMEND -> RecommendFragment(item.menus)
            LayoutType.ACTOR -> ActorFragment()
            else -> GeneralVideoFragment(item.category)
        }
    }
}