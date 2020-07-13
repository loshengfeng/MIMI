package com.dabenxiang.mimi.view.home.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.model.holder.CarouselHolderItem
import com.dabenxiang.mimi.view.adapter.*
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.home.HomeTemplate
import com.dabenxiang.mimi.widget.view.ViewPagerIndicator
import com.to.aboomy.pager2banner.Banner
import kotlinx.android.synthetic.main.item_banner.view.*
import kotlinx.android.synthetic.main.item_carousel.view.*
import kotlinx.android.synthetic.main.item_header.view.*
import kotlinx.android.synthetic.main.item_home_clip.view.*
import kotlinx.android.synthetic.main.item_home_club.view.*
import kotlinx.android.synthetic.main.item_home_picture.view.*
import kotlinx.android.synthetic.main.item_home_statistics.view.*

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
        Glide.with(itemView.context)
            .load(data?.imgUrl)
            .into(ivPoster)
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

    private val recyclerView: RecyclerView = itemView.recyclerview_statistics
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

    override fun updated() {
        data?.also {
            nestedListener.onLoadStatisticsViewHolder(this, it)
        }
    }

    fun submitList(list: List<BaseVideoItem.Video>?) {
        nestedAdapter.submitList(list)
    }
}

class HomeClipViewHolder(
    itemView: View,
    listener: HomeAdapter.EventListener,
    isAdult: Boolean,
    attachmentListener: AttachmentListener
) : HomeViewHolder<HomeTemplate.Clip>(itemView, listener, isAdult) {

    private var attachmentType: AttachmentType? = null

    private val recyclerView: RecyclerView = itemView.recyclerview_clip
    private val nestedAdapter by lazy {
        HomeClipAdapter(listener, attachmentListener)
    }

    init {
        LinearLayoutManager(itemView.context).also { layoutManager ->
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            recyclerView.layoutManager = layoutManager
        }
        recyclerView.adapter = nestedAdapter
        LinearSnapHelper().attachToRecyclerView(recyclerView)
    }

    override fun updated() {
        nestedListener.onLoadClipViewHolder(this)
    }

    fun submitList(list: List<MemberPostItem>) {
        nestedAdapter.submitList(list)
    }

    fun updateItem(position: Int) {
        nestedAdapter.notifyItemChanged(position)
    }
}

class HomePictureViewHolder(
    itemView: View,
    listener: HomeAdapter.EventListener,
    isAdult: Boolean,
    attachmentListener: AttachmentListener
) : HomeViewHolder<HomeTemplate.Picture>(itemView, listener, isAdult) {

    private val recyclerView: RecyclerView = itemView.recyclerview_picture
    private val nestedAdapter by lazy {
        HomePictureAdapter(listener, attachmentListener)
    }

    init {
        LinearLayoutManager(itemView.context).also { layoutManager ->
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            recyclerView.layoutManager = layoutManager
        }
        recyclerView.adapter = nestedAdapter
        LinearSnapHelper().attachToRecyclerView(recyclerView)
    }

    override fun updated() {
        nestedListener.onLoadPictureViewHolder(this)
    }

    fun submitList(list: List<MemberPostItem>) {
        nestedAdapter.submitList(list)
    }

    fun updateItem(position: Int) {
        nestedAdapter.notifyItemChanged(position)
    }
}

class HomeClubViewHolder(
    itemView: View,
    listener: HomeAdapter.EventListener,
    isAdult: Boolean,
    clubListener: HomeClubAdapter.ClubListener,
    attachmentListener: AttachmentListener
) : HomeViewHolder<HomeTemplate.Club>(itemView, listener, isAdult) {

    private val recyclerView: RecyclerView = itemView.recyclerview_club
    private val nestedAdapter by lazy {
        HomeClubAdapter(listener, clubListener, attachmentListener)
    }

    init {
        LinearLayoutManager(itemView.context).also { layoutManager ->
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            recyclerView.layoutManager = layoutManager
        }
        recyclerView.adapter = nestedAdapter
        LinearSnapHelper().attachToRecyclerView(recyclerView)
    }

    override fun updated() {
        nestedListener.onLoadClubViewHolder(this)
    }

    fun submitList(list: List<MemberClubItem>) {
        nestedAdapter.submitList(list)
    }

    fun getItem(position: Int): MemberClubItem {
        return nestedAdapter.getMemberClubItems()[position]
    }

    fun updateItem(position: Int) {
        nestedAdapter.notifyItemChanged(position)
    }

    fun updateItemByFollow(position: Int, result: Boolean) {
        val item = getItem(position)
        item.isFollow = result
        updateItem(position)
    }
}
