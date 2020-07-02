package com.dabenxiang.mimi.view.home

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.model.holder.CarouselHolderItem
import com.dabenxiang.mimi.view.adapter.*
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.view.ViewPagerIndicator
import com.to.aboomy.pager2banner.Banner
import kotlinx.android.synthetic.main.item_banner.view.*
import kotlinx.android.synthetic.main.item_carousel.view.*
import kotlinx.android.synthetic.main.item_header.view.*
import kotlinx.android.synthetic.main.item_home_leaderboard.view.*
import kotlinx.android.synthetic.main.item_home_recommend.view.*
import kotlinx.android.synthetic.main.item_home_statistics.view.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope

abstract class HomeViewHolder<VM : HomeTemplate>(
    itemView: View,
    protected val nestedListener: HomeAdapter.EventListener,
    protected val isAdult: Boolean
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

class HeaderViewHolder(
    itemView: View,
    nestedListener: HomeAdapter.EventListener,
    isAdult: Boolean
) :
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

class HomeCarouselViewHolder(
    itemView: View,
    listener: HomeAdapter.EventListener,
    isAdult: Boolean
) :
    HomeViewHolder<HomeTemplate.Carousel>(itemView, listener, isAdult) {

    private val banner: Banner = itemView.banner
    private val pagerIndicator: ViewPagerIndicator = itemView.pager_indicator
    private val nestedAdapter by lazy {
        CarouselAdapter(nestedListener, isAdult)
    }

    private val dp8 by lazy { itemView.resources.getDimensionPixelSize(R.dimen.dp_8) }

    init {
        banner.adapter = nestedAdapter
        banner.setPageMargin(dp8, dp8)

        //banner.setPageTransformer(ScaleInTransformer())
        //nestedAdapter.submitList(it.carouselList)
    }

    override fun updated() {
        data?.also {
            if (nestedAdapter.currentList.isEmpty()) {
                nestedListener.onLoadCarouselViewHolder(this, it)
            }
        }
    }

    fun submitList(list: List<CarouselHolderItem>?) {
        nestedAdapter.submitList(list)
        pagerIndicator.setViewPager2(banner.viewPager2, list?.count() ?: 0)
    }
}

class HomeStatisticsViewHolder(
    itemView: View,
    listener: HomeAdapter.EventListener,
    isAdult: Boolean
) :
    HomeViewHolder<HomeTemplate.Statistics>(itemView, listener, isAdult) {

    private val recyclerView: RecyclerView = itemView.recyclerview_categories
    private val nestedAdapter by lazy {
        HomeStatisticsAdapter(nestedListener, isAdult)
    }

    init {
        LinearLayoutManager(itemView.context).also { layoutManager ->
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            recyclerView.layoutManager = layoutManager
        }

        recyclerView.adapter = nestedAdapter

        LinearSnapHelper().attachToRecyclerView(recyclerView)
    }

    private var activeTask: Deferred<Any>? = null

    suspend fun activeTask(block: suspend () -> Any): Any {
        activeTask?.cancelAndJoin()

        return coroutineScope {
            val newTask = async {
                block()
            }

            newTask.invokeOnCompletion {
                activeTask = null
            }

            activeTask = newTask
            newTask.await()
        }
    }

    override fun updated() {
        data?.also {
            if (nestedAdapter.itemCount == 0) {
                nestedListener.onLoadStatisticsViewHolder(this, it)
            }
        }
    }

    fun submitList(list: List<BaseVideoItem.Video>?) {
        nestedAdapter.submitList(list)
    }
}

class HomeLeaderboardViewHolder(
    itemView: View,
    listener: HomeAdapter.EventListener,
    isAdult: Boolean
) :
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

class HomeRecommendViewHolder(
    itemView: View,
    listener: HomeAdapter.EventListener,
    isAdult: Boolean
) :
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