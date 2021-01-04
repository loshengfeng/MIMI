package com.dabenxiang.mimi.view.my_pages.like

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.enums.MyCollectionTabItemType
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.my_pages.base.BaseMyPagesTabFragment
import com.dabenxiang.mimi.view.my_pages.base.MyPagesViewModel
import com.dabenxiang.mimi.view.my_pages.pages.like.LikeMimiVideoFragment
import com.dabenxiang.mimi.view.my_pages.pages.like.LikePostFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_my.*
import kotlinx.android.synthetic.main.fragment_my.view.*


class LikeFragment : BaseMyPagesTabFragment() {

    override val viewModel: MyPagesViewModel by viewModels()

    companion object {
        const val TAB_MiMI_VIDEO = 0
        const val TAB_POST = 1
    }

    override val tabFragmentsCreators: Map<Int, () -> Fragment> =mapOf(
           TAB_MiMI_VIDEO to { LikeMimiVideoFragment(TAB_MiMI_VIDEO, MyCollectionTabItemType.MIMI_VIDEO) },
           TAB_POST to {  LikePostFragment(TAB_POST, MyCollectionTabItemType.POST)  }
    )

    override fun setFragmentTitle() {
        tool_bar.toolbar_title.text = getString(R.string.like_title)
    }

    override fun getTabTitle(position: Int): String {
        val tabs = resources.getStringArray(R.array.like_tabs)
        return tabs[position]
    }

    override fun deleteAll() {
        CleanDialogFragment.newInstance(
            listener = onCleanDialogListener,
            msgResId = R.string.like_delete_all
        ).also {
            it.show(
                requireActivity().supportFragmentManager,
                CleanDialogFragment::class.java.simpleName
            )
        }
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
