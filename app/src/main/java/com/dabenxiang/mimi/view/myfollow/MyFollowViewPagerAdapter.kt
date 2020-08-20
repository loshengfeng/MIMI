package com.dabenxiang.mimi.view.myfollow

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.ClubFollowAdapter
import com.dabenxiang.mimi.view.adapter.MemberFollowAdapter
import com.dabenxiang.mimi.view.myfollow.MyFollowFragment.Companion.NO_DATA
import com.dabenxiang.mimi.view.myfollow.MyFollowFragment.Companion.TYPE_CLUB
import com.dabenxiang.mimi.view.myfollow.MyFollowFragment.Companion.TYPE_MEMBER
import kotlinx.android.synthetic.main.view_my_follow_pager_content.view.*

class MyFollowViewPagerAdapter(
    val context: Context,
    private val memberFollowAdapter: MemberFollowAdapter,
    private val clubFollowAdapter: ClubFollowAdapter,
    val onSwipeRefresh: () -> Unit
) : PagerAdapter() {

    private var viewMember: View? = null
    private var viewClub: View? = null

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.view_my_follow_pager_content, container, false)

        view.rv_content.adapter = when (position) {
            TYPE_MEMBER -> memberFollowAdapter
            TYPE_CLUB -> clubFollowAdapter
            else -> null
        }

        view.layout_refresh.setOnRefreshListener {
            view.layout_refresh.isRefreshing = false
            onSwipeRefresh()
        }

        container.addView(view)
        when (position) {
            TYPE_MEMBER -> viewMember = view
            TYPE_CLUB -> viewClub = view
            else -> {
            }
        }
        return view
    }

    fun refreshUi(position: Int, size: Int) {
        val view = getCurrentView(position)
        view?.rv_content?.visibility = when (size) {
            NO_DATA -> View.GONE
            else -> View.VISIBLE
        }

        view?.item_no_data?.visibility = when (size) {
            NO_DATA -> View.VISIBLE
            else -> View.GONE
        }
        view?.tv_all?.text =
            if (position == TYPE_MEMBER)
                context.getString(R.string.follow_members_total_num, size.toString())
            else
                context.getString(R.string.follow_clubs_total_num, size.toString())
    }

    fun changeIsRefreshing(position: Int, isRefreshing: Boolean) {
        getCurrentView(position)?.layout_refresh?.isRefreshing = isRefreshing
    }

    private fun getCurrentView(position: Int): View? {
        return when (position) {
            TYPE_MEMBER -> viewMember
            TYPE_CLUB -> viewClub
            else -> null
        }
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount() = 2

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            TYPE_MEMBER -> context.getString(R.string.follow_people)
            TYPE_CLUB -> context.getString(R.string.follow_circle)
            else -> null
        }
    }
}