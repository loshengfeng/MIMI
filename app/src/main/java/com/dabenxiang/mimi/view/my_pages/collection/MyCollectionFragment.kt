package com.dabenxiang.mimi.view.my_pages.collection

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.enums.MyCollectionTabItemType
import com.dabenxiang.mimi.view.my_pages.base.BaseMyPagesTabFragment
import com.dabenxiang.mimi.view.my_pages.base.MyViewPagerAdapter
import com.dabenxiang.mimi.view.my_pages.collection.favorites.MyCollectionFavoritesFragment
import com.dabenxiang.mimi.view.my_pages.collection.mimi_video.MyCollectionMimiVideoFragment
import kotlinx.android.synthetic.main.fragment_my.*
import kotlinx.android.synthetic.main.fragment_my.view.*

class MyCollectionFragment: BaseMyPagesTabFragment() {

    companion object {
        const val TAB_MiMI_VIDEO = 0
        const val TAB_SHORT_VIDEO = 1
        const val TAB_FAVORITES = 2
    }

    override val viewModel: MyCollectionViewModel by viewModels()

    override val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
            TAB_MiMI_VIDEO to { MyCollectionMimiVideoFragment(MyCollectionTabItemType.MIMI_VIDEO) },
            TAB_SHORT_VIDEO to { MyCollectionMimiVideoFragment(MyCollectionTabItemType.SHORT_VIDEO) },
            TAB_FAVORITES to {  MyCollectionFavoritesFragment(MyCollectionTabItemType.POST)  }
    )

    override val viewPagerAdapter: MyViewPagerAdapter by lazy {
        MyViewPagerAdapter(
                tabFragmentsCreators,
                childFragmentManager,
                lifecycle)
    }

    override fun setFragmentTitle() {
        tool_bar.toolbar_title.text = getString(R.string.personal_follow)
    }

    override fun getTabTitle(position: Int): String? {
        return when (position) {
            TAB_MiMI_VIDEO -> getString(R.string.follow_tab_mimi_video)
            TAB_SHORT_VIDEO -> getString(R.string.follow_tab_small_video)
            TAB_FAVORITES -> getString(R.string.follow_tab_post)
            else -> null
        }
    }
}