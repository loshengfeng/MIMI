package com.dabenxiang.mimi.view.my_pages.collection

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.my_pages.base.BaseMyPagesTabFragment
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import com.dabenxiang.mimi.view.my_pages.base.MyPagesViewModel
import com.dabenxiang.mimi.view.my_pages.pages.favorites.MyFavoritesFragment
import com.dabenxiang.mimi.view.my_pages.pages.mimi_video.MyCollectionMimiVideoFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_my.*
import kotlinx.android.synthetic.main.fragment_my.view.*

class MyCollectionFragment: BaseMyPagesTabFragment() {

    companion object {
        const val TAB_MiMI_VIDEO = 0
        const val TAB_SHORT_VIDEO = 1
        const val TAB_FAVORITES = 2
    }

    override val viewModel: MyPagesViewModel by viewModels()

    override val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
            TAB_MiMI_VIDEO to { MyCollectionMimiVideoFragment(TAB_MiMI_VIDEO, MyPagesType.FAVORITE_MIMI_VIDEO) },
            TAB_SHORT_VIDEO to { MyCollectionMimiVideoFragment(TAB_SHORT_VIDEO, MyPagesType.FAVORITE_SHORT_VIDEO) },
            TAB_FAVORITES to { MyFavoritesFragment(TAB_FAVORITES, MyPagesType.FAVORITE_POST)  }
    )

    override fun setFragmentTitle() {
        tool_bar.toolbar_title.text = getString(R.string.personal_follow)
    }

    override fun getTabTitle(position: Int): String {
        val tabs = resources.getStringArray(R.array.favorite_tabs)
        return tabs[position]
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override val onTabSelectedListener: TabLayout.OnTabSelectedListener
        get() = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { changeCleanBtnIsEnable(it) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        }

}