package com.dabenxiang.mimi.view.my_pages.follow

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.my_pages.base.BaseMyPagesTabFragment
import com.dabenxiang.mimi.view.my_pages.base.MyPagesViewModel
import com.dabenxiang.mimi.view.my_pages.pages.follow_list.MyFollowListFragment
import kotlinx.android.synthetic.main.fragment_my.*
import kotlinx.android.synthetic.main.fragment_my.view.*

class MyFollowFragment : BaseMyPagesTabFragment() {

    companion object {
        const val TAB_FOLLOW_PEOPLE = 0
        const val TAB_FOLLOW_CLUB = 1
    }

    override val viewModel: MyPagesViewModel by viewModels()

    override val tabFragmentsCreators: Map<Int, () -> Fragment> =mapOf(
            TAB_FOLLOW_PEOPLE to { MyFollowListFragment(TAB_FOLLOW_PEOPLE) },
            TAB_FOLLOW_CLUB to { MyFollowListFragment(TAB_FOLLOW_CLUB) }
            )

    override fun setFragmentTitle() {
        tool_bar.toolbar_title.text = getString(R.string.follow_title)
    }

    override fun getTabTitle(position: Int): String? {
        return when (position) {
            TAB_FOLLOW_PEOPLE -> getString(R.string.follow_people)
            TAB_FOLLOW_CLUB -> getString(R.string.follow_circle)
            else -> null
        }
    }

    override fun deleteAll() {
        CleanDialogFragment.newInstance(
            listener = onCleanDialogListener,
            msgResId = if (view?.tabs?.selectedTabPosition == TAB_FOLLOW_PEOPLE)
                R.string.follow_clean_member_dlg_msg else
                R.string.follow_clean_club_dlg_msg
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