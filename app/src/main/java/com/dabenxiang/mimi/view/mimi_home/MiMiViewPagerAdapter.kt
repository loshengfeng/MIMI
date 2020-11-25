package com.dabenxiang.mimi.view.mimi_home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.model.api.vo.SecondMenuItem
import com.dabenxiang.mimi.model.enums.LayoutType
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
        //TODO: 依據不同的Tab Type, 呈現不同的Fragment
        val item = secondMenuItems[position]
        return when (item.type) {
            LayoutType.RECOMMEND -> RecommendFragment(item.menus)
            LayoutType.ACTRESS -> RecommendFragment(item.menus)
            else -> GeneralVideoFragment(item.category)
        }
    }
}