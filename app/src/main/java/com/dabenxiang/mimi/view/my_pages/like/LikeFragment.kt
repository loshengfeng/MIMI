package com.dabenxiang.mimi.view.my_pages.like

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.enums.MyCollectionTabItemType
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.my_pages.base.BaseMyPagesTabFragment
import com.dabenxiang.mimi.view.my_pages.base.MyPagesViewModel
import com.dabenxiang.mimi.view.my_pages.follow.MyFollowFragment
import com.dabenxiang.mimi.view.my_pages.pages.favorites.MyFavoritesFragment
import com.dabenxiang.mimi.view.my_pages.pages.mimi_video.MyCollectionMimiVideoFragment
import kotlinx.android.synthetic.main.fragment_my.*
import kotlinx.android.synthetic.main.fragment_my.view.*


class LikeFragment : BaseMyPagesTabFragment() {

    override val viewModel: MyPagesViewModel by viewModels()

    companion object {
        const val TAB_MiMI_VIDEO = 0
        const val TAB_POST = 1
    }

    override val tabFragmentsCreators: Map<Int, () -> Fragment> =mapOf(
           TAB_MiMI_VIDEO to { MyCollectionMimiVideoFragment(TAB_MiMI_VIDEO, MyCollectionTabItemType.MIMI_VIDEO, true) },
           TAB_POST to {  MyFavoritesFragment(TAB_POST, MyCollectionTabItemType.POST,true)  }
    )

    override fun setFragmentTitle() {
        tool_bar.toolbar_title.text = getString(R.string.like_title)
    }

    // UI spec only two tabs use when ? use StringArray ?
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

}
