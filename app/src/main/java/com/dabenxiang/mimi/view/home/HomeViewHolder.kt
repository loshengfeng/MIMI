package com.dabenxiang.mimi.view.home

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.*
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.view.ViewPagerIndicator
import kotlinx.android.synthetic.main.item_banner.view.*
import kotlinx.android.synthetic.main.item_carousel.view.*
import kotlinx.android.synthetic.main.item_header.view.*
import kotlinx.android.synthetic.main.item_home_categories.view.*
import kotlinx.android.synthetic.main.item_home_leaderboard.view.*
import kotlinx.android.synthetic.main.item_home_recommend.view.*
import kotlinx.android.synthetic.main.item_video_list.view.*
import timber.log.Timber

abstract class HomeViewHolder<VM : HomeTemplate>(
    itemView: View, protected val nestedListener: HomeAdapter.EventListener, protected val isAdult: Boolean
) :
    BaseViewHolder(itemView) {

    protected var data: VM? = null

    @Suppress("UNCHECKED_CAST")
    fun bind(bind: HomeTemplate) {
        data = bind as VM
        updated()
    }

    abstract fun updated()
}

class HeaderViewHolder(itemView: View, nestedListener: HomeAdapter.EventListener, isAdult: Boolean) :
    HomeViewHolder<HomeTemplate.Header>(itemView, nestedListener, isAdult) {
    private val ivIcon: ImageView = itemView.iv_icon
    private val tvTitle: TextView = itemView.tv_title
    private val btnMore: View = itemView.btn_more

    init {
        btnMore.setOnClickListener { btn ->
            data?.also {
                nestedListener.onHeaderItemClick(btn, it)
            }
        }
    }

    override fun updated() {
        data?.also { data ->
            if (data.iconRes != null) {
                ivIcon.setImageResource(data.iconRes)
                ivIcon.visibility = View.VISIBLE
            } else {
                ivIcon.setImageDrawable(null)
                ivIcon.visibility = View.GONE
            }

            tvTitle.text = data.title
            tvTitle.setTextColor(
                if (isAdult) {
                    R.color.adult_color_text
                } else {
                    R.color.normal_color_text
                }.let {
                    itemView.resources.getColor(it, null)
                }
            )
        }
    }
}

class HomeBannerViewHolder(itemView: View, listener: HomeAdapter.EventListener, isAdult: Boolean) :
    HomeViewHolder<HomeTemplate.Banner>(itemView, listener, isAdult) {
    private val ivPoster: ImageView = itemView.iv_poster

    override fun updated() {
        data?.also {
            Glide.with(itemView.context)
                .load(data?.imgUrl)
                .into(ivPoster)
        }
    }
}

class HomeCarouselViewHolder(itemView: View, listener: HomeAdapter.EventListener, isAdult: Boolean) :
    HomeViewHolder<HomeTemplate.Carousel>(itemView, listener, isAdult) {

    private val viewPager: ViewPager2 = itemView.viewpager
    private val pagerIndicator: ViewPagerIndicator = itemView.pager_indicator
    private val nestedAdapter by lazy {
        CarouselAdapter(nestedListener)
    }

    init {
        viewPager.adapter = nestedAdapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Timber.d("onPageSelected: $position")
            }
        })
    }

    override fun updated() {
        data?.also {
            nestedAdapter.setDataSrc(it)
            pagerIndicator.setViewPager2(viewPager)
        }
    }
}

class HomeVideoListViewHolder(itemView: View, listener: HomeAdapter.EventListener, isAdult: Boolean) :
    HomeViewHolder<HomeTemplate.VideoList>(itemView, listener, isAdult) {

    private val recyclerView: RecyclerView = itemView.recyclerview_video
    private val nestedAdapter by lazy {
        HomeVideoListAdapter(nestedListener, isAdult)
    }

    init {
        GridLayoutManager(itemView.context, 2).also { layoutManager ->
            recyclerView.layoutManager = layoutManager
        }

        recyclerView.adapter = nestedAdapter
    }

    override fun updated() {
        data?.also {
            nestedAdapter.setDataSrc(it)
            nestedAdapter.notifyDataSetChanged()
        }
    }
}

class HomeCategoriesViewHolder(itemView: View, listener: HomeAdapter.EventListener, isAdult: Boolean) :
    HomeViewHolder<HomeTemplate.Categories>(itemView, listener, isAdult) {

    private val recyclerView: RecyclerView = itemView.recyclerview_categories
    private val nestedAdapter by lazy {
        HomeCategoriesAdapter(nestedListener, isAdult)
    }

    init {
        LinearLayoutManager(itemView.context).also { layoutManager ->
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            recyclerView.layoutManager = layoutManager
        }

        recyclerView.adapter = nestedAdapter
    }

    override fun updated() {
        data?.also {
            nestedAdapter.loadData(it)
        }
    }
}

class HomeLeaderboardViewHolder(itemView: View, listener: HomeAdapter.EventListener, isAdult: Boolean) :
    HomeViewHolder<HomeTemplate.Leaderboard>(itemView, listener, isAdult) {

    private val recyclerView: RecyclerView = itemView.recyclerview_leaderboard
    private val nestedAdapter by lazy {
        LeaderboardAdapter()
    }

    init {
        LinearLayoutManager(itemView.context).also { layoutManager ->
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            recyclerView.layoutManager = layoutManager
        }

        recyclerView.adapter = nestedAdapter
    }

    override fun updated() {
    }
}

class HomeRecommendViewHolder(itemView: View, listener: HomeAdapter.EventListener, isAdult: Boolean) :
    HomeViewHolder<HomeTemplate.Recommend>(itemView, listener, isAdult) {

    private val recyclerView: RecyclerView = itemView.recyclerview_recommend
    private val nestedAdapter by lazy {
        HomeRecommendAdapter()
    }

    init {
        GridLayoutManager(itemView.context, 2).also { layoutManager ->
            recyclerView.layoutManager = layoutManager
        }

        recyclerView.adapter = nestedAdapter
    }

    override fun updated() {
    }
}