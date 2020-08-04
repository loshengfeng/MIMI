package com.dabenxiang.mimi.view.home

import android.content.Intent
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.CategoriesItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.holder.statisticsItemToCarouselHolderItem
import com.dabenxiang.mimi.model.holder.statisticsItemToVideoItem
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.adapter.HomeVideoListAdapter
import com.dabenxiang.mimi.view.adapter.TopTabAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.ClubFuncItem
import com.dabenxiang.mimi.view.home.category.CategoriesFragment
import com.dabenxiang.mimi.view.home.viewholder.*
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : BaseFragment() {

    private val viewModel: HomeViewModel by viewModels()

    private var lastTabPosition = 0

    private val homeBannerViewHolderMap = hashMapOf<Int, HomeBannerViewHolder>()
    private val homeCarouselViewHolderMap = hashMapOf<Int, HomeCarouselViewHolder>()
    private val carouselMap = hashMapOf<Int, HomeTemplate.Carousel>()

    private val homeStatisticsViewHolderMap = hashMapOf<Int, HomeStatisticsViewHolder>()
    private val statisticsMap = hashMapOf<Int, HomeTemplate.Statistics>()

    override fun getLayoutId() = R.layout.fragment_home

    override fun setupFirstTime() {
        requireActivity().onBackPressedDispatcher.addCallback { backToDesktop() }
        recyclerview_tab.adapter = tabAdapter
        setupRecyclerByPosition(0)
        refresh.setColorSchemeColors(requireContext().getColor(R.color.color_red_1))
        btn_ranking.visibility = View.GONE
        iv_post.visibility = View.GONE

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (GeneralUtils.getScreenSize(requireActivity()).second * 0.0245).toInt()

        if (mainViewModel?.normal == null) {
            mainViewModel?.getHomeCategories()
        }
    }

    override fun setupObservers() {
        viewModel.showProgress.observe(viewLifecycleOwner, Observer { showProgress ->
            showProgress?.takeUnless { it }?.also { refresh.isRefreshing = it }
        })

        mainViewModel?.categoriesData?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> refresh.isRefreshing = true
                is Loaded -> refresh.isRefreshing = false
                is Success -> {
                    val list = mutableListOf<String>()
                    list.add(getString(R.string.home))
                    mainViewModel?.setupNormalCategoriesItem(it.result.content?.getNormal())
                    mainViewModel?.normal?.categories?.also { level2 ->
                        for (i in 0 until level2.count()) {
                            val detail = level2[i]
                            list.add(detail.name)
                        }
                        tabAdapter.submitList(list, lastTabPosition)
                        setupHomeData(mainViewModel?.normal)
                    }
                }
                is Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.getAdHomeResult?.observe(viewLifecycleOwner, Observer {
            when (val response = it.second) {
                is Success -> {
                    val viewHolder = homeBannerViewHolderMap[it.first]
                    viewHolder?.updateItem(response.result)
                }
                is Error -> onApiError(response.throwable)
            }
        })

        viewModel.videoList.observe(viewLifecycleOwner, Observer {
            when (lastTabPosition) {
                1 -> movieListAdapter.submitList(it)
                2 -> dramaListAdapter.submitList(it)
                3 -> varietyListAdapter.submitList(it)
                else -> animationListAdapter.submitList(it)
            }
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
                is Error -> onApiError(response.throwable)
            }
        })

        viewModel.videosResult.observe(viewLifecycleOwner, Observer {
            when (val response = it.second) {
                is Loaded -> {
                    val viewHolder = homeStatisticsViewHolderMap[it.first]
                    viewHolder?.hideProgressBar()
                }
                is Success -> {
                    val viewHolder = homeStatisticsViewHolderMap[it.first]
                    val statistics = statisticsMap[it.first]
                    val videoHolderItems =
                        response.result.content?.statisticsItemToVideoItem(statistics!!.isAdult)
                    viewHolder?.submitList(videoHolderItems)
                }
                is Error -> onApiError(response.throwable)
            }
        })
    }

    override fun setupListeners() {
        refresh.setOnRefreshListener {
            refresh.isRefreshing = true
            getData(lastTabPosition)
        }

        btn_filter.setOnClickListener {
            takeIf { lastTabPosition > 0 }?.also {
                val category = mainViewModel?.normal?.categories?.get(lastTabPosition - 1)
                category?.also {
                    val bundle = CategoriesFragment.createBundle(it.name, it.name, category)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_homeFragment_to_categoriesFragment,
                            bundle
                        )
                    )
                }
            }
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

    private fun setupRecyclerByPosition(position: Int) {

        rv_home.visibility = View.GONE
        rv_first.visibility = View.GONE
        rv_second.visibility = View.GONE
        rv_third.visibility = View.GONE
        rv_fourth.visibility = View.GONE
        rv_fifth.visibility = View.GONE
        rv_sixth.visibility = View.GONE

        btn_filter.visibility = View.VISIBLE

        when (position) {
            0 -> {
                btn_filter.visibility = View.GONE
                rv_home.visibility = View.VISIBLE
                takeIf { rv_home.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_home.layoutManager = LinearLayoutManager(requireContext())
                    rv_home.adapter = homeAdapter
                    getData(position)
                }
            }
            1 -> {
                rv_first.visibility = View.VISIBLE
                takeIf { rv_first.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_first.layoutManager = GridLayoutManager(requireContext(), 2)
                    rv_first.adapter = movieListAdapter
                    getData(position)
                }
            }
            2 -> {
                rv_second.visibility = View.VISIBLE
                takeIf { rv_second.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_second.layoutManager = GridLayoutManager(requireContext(), 2)
                    rv_second.adapter = dramaListAdapter
                    getData(position)
                }
            }
            3 -> {
                rv_third.visibility = View.VISIBLE
                takeIf { rv_third.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_third.layoutManager = GridLayoutManager(requireContext(), 2)
                    rv_third.adapter = varietyListAdapter
                    getData(position)
                }
            }
            else -> {
                rv_fourth.visibility = View.VISIBLE
                takeIf { rv_fourth.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_fourth.layoutManager = GridLayoutManager(requireContext(), 2)
                    rv_fourth.adapter = animationListAdapter
                    getData(position)
                }
            }
        }
    }

    private fun getData(position: Int) {
        when (position) {
            0 -> mainViewModel?.getHomeCategories()
            else -> {
                val keyword = mainViewModel?.normal?.categories?.get(position - 1)?.name
                viewModel.getVideos(keyword, false)
            }
        }
    }

    private fun setupHomeData(root: CategoriesItem?) {
        val templateList = mutableListOf<HomeTemplate>()

        templateList.add(HomeTemplate.Banner(AdItem()))
        templateList.add(HomeTemplate.Carousel(false))

        if (root?.categories != null) {
            for (item in root.categories) {
                templateList.add(HomeTemplate.Header(null, item.name, item.name))
                templateList.add(HomeTemplate.Statistics(item.name, item.name, false))
            }
        }

        homeAdapter.submitList(templateList)
    }

    private val tabAdapter by lazy {
        TopTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                setTab(index)
            }
        }, false)
    }

    private fun setTab(index: Int) {
        lastTabPosition = index
        tabAdapter.setLastSelectedIndex(lastTabPosition)
        recyclerview_tab.scrollToPosition(index)
        setupRecyclerByPosition(index)
    }

    private val homeAdapter by lazy {
        HomeAdapter(
            requireContext(),
            adapterListener,
            false,
            memberPostFuncItem,
            ClubFuncItem()
        )
    }

    private val memberPostFuncItem by lazy {
        MemberPostFuncItem(
            {},
            { id, function -> getBitmap(id, function) },
            { _, _, _ -> }
        )
    }

    private val movieListAdapter by lazy {
        HomeVideoListAdapter(adapterListener, false)
    }

    private val dramaListAdapter by lazy {
        HomeVideoListAdapter(adapterListener, false)
    }

    private val varietyListAdapter by lazy {
        HomeVideoListAdapter(adapterListener, false)
    }

    private val animationListAdapter by lazy {
        HomeVideoListAdapter(adapterListener, false)
    }

    private val adapterListener = object : HomeAdapter.EventListener {
        override fun onHeaderItemClick(view: View, item: HomeTemplate.Header) {
            val bundle = CategoriesFragment.createBundle(
                item.title,
                item.categories,
                mainViewModel?.getCategory(item.title, false)
            )
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_homeFragment_to_categoriesFragment,
                    bundle
                )
            )
        }

        override fun onVideoClick(view: View, item: PlayerItem) {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtras(PlayerActivity.createBundle(item))
            startActivityForResult(intent, PlayerActivity.REQUEST_CODE)
        }

        override fun onClipClick(view: View, item: List<MemberPostItem>, position: Int) {
        }

        override fun onPictureClick(view: View, item: MemberPostItem) {

        }

        override fun onClubClick(view: View, item: MemberClubItem) {
        }

        override fun onLoadBannerViewHolder(vh: HomeBannerViewHolder) {
            homeBannerViewHolderMap[vh.adapterPosition] = vh
            mainViewModel?.getAd(
                vh.adapterPosition,
                viewModel.adWidth,
                viewModel.adHeight
            )
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

        }

        override fun onLoadClubViewHolder(vh: HomeClubViewHolder) {

        }
    }

    private fun getBitmap(id: String, update: ((String) -> Unit)) {
        viewModel.getBitmap(id, update)
    }
}
