package com.dabenxiang.mimi.view.home

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.CategoriesItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.HomeItemType
import com.dabenxiang.mimi.model.holder.statisticsItemToCarouselHolderItem
import com.dabenxiang.mimi.model.holder.statisticsItemToVideoItem
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.adapter.HomeClubAdapter
import com.dabenxiang.mimi.view.adapter.HomeVideoListAdapter
import com.dabenxiang.mimi.view.adapter.TopTabAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.home.viewholder.*
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.view.search.SearchVideoFragment
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber

class HomeFragment : BaseFragment() {

    private val viewModel: HomeViewModel by viewModels()

    private var lastPosition = 0

    private val homeCarouselViewHolderMap = hashMapOf<Int, HomeCarouselViewHolder>()
    private val carouselMap = hashMapOf<Int, HomeTemplate.Carousel>()

    private val homeStatisticsViewHolderMap = hashMapOf<Int, HomeStatisticsViewHolder>()
    private val statisticsMap = hashMapOf<Int, HomeTemplate.Statistics>()

    private val attachmentMap: HashMap<Long, Bitmap> = hashMapOf()

    override fun getLayoutId() = R.layout.fragment_home

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback { backToDesktop() }
        recyclerview_tab.adapter = tabAdapter
        recyclerview_home.adapter = adapter
        recyclerview_videos.adapter = videoListAdapter
        refresh_home.setColorSchemeColors(requireContext().getColor(R.color.color_red_1))

        if (mainViewModel?.normal == null)
            mainViewModel?.loadHomeCategories()
    }

    override fun setupObservers() {
        mainViewModel?.categoriesData?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> refresh_home.isRefreshing = true
                is Loaded -> refresh_home.isRefreshing = false
                is Success -> {
                    val list = mutableListOf<String>()
                    list.add(getString(R.string.home))
                    mainViewModel?.setupNormalCategoriesItem(it.result.content?.getNormal())
                    mainViewModel?.normal?.categories?.also { level2 ->
                        for (i in 0 until level2.count()) {
                            val detail = level2[i]
                            list.add(detail.name)
                        }
                        tabAdapter.submitList(list, lastPosition)
                        loadFirstTab(mainViewModel?.normal)
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.tabLayoutPosition.observe(viewLifecycleOwner, Observer { position ->
            lastPosition = position
            tabAdapter.setLastSelectedIndex(lastPosition)
            when (position) {
                0 -> {
                    btn_filter.visibility = View.GONE
                    loadFirstTab(mainViewModel?.normal)
                }
                else -> {
                    btn_filter.visibility = View.VISIBLE
                    val keyword = mainViewModel?.normal?.categories?.get(position - 1)?.name
                    loadCategories(keyword)
                }
            }
        })

        viewModel.videoList.observe(viewLifecycleOwner, Observer {
            videoListAdapter.submitList(it)
        })

        viewModel.carouselResult.observe(viewLifecycleOwner, Observer {
            when (val response = it.second) {
                is Success -> {
                    val viewHolder = homeCarouselViewHolderMap[it.first]
                    val carousel = carouselMap[it.first]
                    val carouselHolderItems =
                        response.result.content?.statisticsItemToCarouselHolderItem(carousel!!.isAdult)
                    viewHolder?.submitList(carouselHolderItems)
                }
                is Error -> Timber.e(response.throwable)
            }
        })

        viewModel.videosResult.observe(viewLifecycleOwner, Observer {
            when (val response = it.second) {
                is Success -> {
                    val viewHolder = homeStatisticsViewHolderMap[it.first]
                    val statistics = statisticsMap[it.first]
                    val videoHolderItems =
                        response.result.content?.statisticsItemToVideoItem(statistics!!.isAdult)
                    viewHolder?.submitList(videoHolderItems)
                }
                is Error -> Timber.e(response.throwable)
            }
        })
    }

    override fun setupListeners() {
        refresh_home.setOnRefreshListener {
            refresh_home.isRefreshing = false
            mainViewModel?.loadHomeCategories()
        }

        iv_bg_search.setOnClickListener {
            val bundle = SearchVideoFragment.createBundle("")
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_homeFragment_to_searchVideoFragment,
                    bundle
                )
            )
        }
    }

    private val tabAdapter by lazy {
        TopTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                viewModel.setTopTabPosition(index)
            }
        }, false)
    }

    private val clubListener = object : HomeClubAdapter.ClubListener {
        override fun followClub(id: Int, position: Int) {
        }

        override fun cancelFollowClub(id: Int, position: Int) {
        }
    }

    private val adapter by lazy {
        HomeAdapter(
            requireContext(),
            adapterListener,
            false,
            clubListener,
            attachmentListener,
            attachmentMap
        )
    }

    private val videoListAdapter by lazy {
        HomeVideoListAdapter(adapterListener, false)
    }

    private val attachmentListener = object : HomeAdapter.AttachmentListener {
        override fun onGetAttachment(id: Long, position: Int, type: HomeItemType) {

        }
    }

    private val adapterListener = object : HomeAdapter.EventListener {
        override fun onHeaderItemClick(view: View, item: HomeTemplate.Header) {
            val bundle = CategoriesFragment.createBundle(item.title, item.categories)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_homeFragment_to_categoriesFragment,
                    bundle
                )
            )
        }

        override fun onVideoClick(view: View, item: PlayerData) {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtras(PlayerActivity.createBundle(item))
            startActivityForResult(intent, PlayerActivity.REQUEST_CODE)
        }

        override fun onClipClick(view: View, item: List<MemberPostItem>) {
        }

        override fun onPictureClick(view: View, item: MemberPostItem) {

        }

        override fun onClubClick(view: View, item: MemberClubItem) {
        }

        override fun onLoadStatisticsViewHolder(
            vh: HomeStatisticsViewHolder,
            src: HomeTemplate.Statistics
        ) {
            homeStatisticsViewHolderMap[vh.adapterPosition] = vh
            statisticsMap[vh.adapterPosition] = src
            viewModel.loadNestedStatisticsList(vh.adapterPosition, src)
        }

        override fun onLoadCarouselViewHolder(
            vh: HomeCarouselViewHolder,
            src: HomeTemplate.Carousel
        ) {
            homeCarouselViewHolderMap[vh.adapterPosition] = vh
            carouselMap[vh.adapterPosition] = src
            viewModel.loadNestedStatisticsListForCarousel(vh.adapterPosition, src)
        }

        override fun onLoadClipViewHolder(vh: HomeClipViewHolder) {

        }

        override fun onLoadPictureViewHolder(vh: HomePictureViewHolder) {
            TODO("Not yet implemented")
        }

        override fun onLoadClubViewHolder(vh: HomeClubViewHolder) {
            TODO("Not yet implemented")
        }
    }

    private fun loadFirstTab(root: CategoriesItem?) {
        recyclerview_videos.visibility = View.GONE
        refresh_home.visibility = View.VISIBLE

        val templateList = mutableListOf<HomeTemplate>()

        templateList.add(HomeTemplate.Banner(imgUrl = "https://tspimg.tstartel.com/upload/material/95/28511/mie_201909111854090.png"))
        templateList.add(HomeTemplate.Carousel(false))

        if (root?.categories != null) {
            for (item in root.categories) {
                templateList.add(HomeTemplate.Header(null, item.name, item.name))
                templateList.add(HomeTemplate.Statistics(item.name, item.name, false))
            }
        }

        adapter.submitList(templateList)
    }

    private fun loadCategories(keyword: String?) {
        recyclerview_videos.visibility = View.VISIBLE
        refresh_home.visibility = View.GONE

        viewModel.setupVideoList(keyword, false)
    }
}
