package com.dabenxiang.mimi.view.mimi_home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.model.api.vo.SecondMenusItem
import com.dabenxiang.mimi.model.enums.LayoutType
import com.dabenxiang.mimi.view.recommend.RecommendFragment

class MiMiViewPagerAdapter(
    fragment: Fragment,
    private val secondMenusItems: List<SecondMenusItem>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return secondMenusItems.size
    }

    override fun createFragment(position: Int): Fragment {
        //TODO: 依據不同的Tab Type, 呈現不同的Fragment
        val item = secondMenusItems[position]
        return when (item.type) {
            LayoutType.RECOMMEND -> RecommendFragment()
            LayoutType.GENERAL -> RecommendFragment()
            else -> RecommendFragment()
        }
    }
}