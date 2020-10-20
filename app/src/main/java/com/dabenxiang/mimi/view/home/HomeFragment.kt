package com.dabenxiang.mimi.view.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.CategoriesItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.vo.CarouselHolderItem
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.model.vo.categoryBannerItemCarouselHolderItem
import com.dabenxiang.mimi.model.vo.statisticsItemToVideoItem
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

    companion object {
        private const val REQUEST_LOGIN = 1000
    }

    private val viewModel: HomeViewModel by viewModels()

    private var lastTabPosition = 0

    private val homeBannerViewHolderMap = hashMapOf<Int, HomeBannerViewHolder>()
    private val homeCarouselViewHolderMap = hashMapOf<Int, HomeCarouselViewHolder>()
    private val carouselMap = hashMapOf<Int, HomeTemplate.Carousel>()

    private val homeStatisticsViewHolderMap = hashMapOf<Int, HomeStatisticsViewHolder>()
    private val statisticsMap = hashMapOf<Int, HomeTemplate.Statistics>()

    override fun getLayoutId() = R.layout.fragment_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.showProgress.observe(this, Observer { showProgress ->
            showProgress?.takeUnless { it }?.also { refresh.isRefreshing = it }
        })

        mainViewModel?.categoriesData?.observe(this, Observer {
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

        mainViewModel?.getAdHomeResult?.observe(this, Observer {
            when (val response = it.second) {
                is Success -> {
                    val viewHolder = homeBannerViewHolderMap[it.first]
                    viewHolder?.updateItem(response.result)
                }
                is Error -> onApiError(response.throwable)
            }
        })

        viewModel.videoList.observe(this, Observer {
            when (lastTabPosition) {
                1 -> movieListAdapter.submitList(it)
                2 -> dramaListAdapter.submitList(it)
                3 -> varietyListAdapter.submitList(it)
                else -> animationListAdapter.submitList(it)
            }
        })

        viewModel.carouselResult.observe(this, Observer {
            when (val response = it.second) {
                is Success -> {
                    val viewHolder = homeCarouselViewHolderMap[it.first]
                    val carousel = carouselMap[it.first]
                    val carouselHolderItems =
                        response.result.content?.categoryBannerItemCarouselHolderItem()
                    viewHolder?.submitList(carouselHolderItems)
                }
                is Error -> onApiError(response.throwable)
            }
        })

        viewModel.videosResult.observe(this, Observer {
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

    override fun setupFirstTime() {
        requireActivity().onBackPressedDispatcher.addCallback { backToDesktop() }
        recyclerview_tab.adapter = tabAdapter
        setupRecyclerByPosition(0)

        btn_ranking.visibility = View.GONE
        iv_post.visibility = View.GONE

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()
        if (mainViewModel?.normal == null) {
            mainViewModel?.getHomeCategories()
        }
    }

    override fun setupObservers() {
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
            val category =
                if (lastTabPosition != 0) mainViewModel?.normal?.categories?.get(lastTabPosition - 1)?.name else null
            val bundle = SearchVideoFragment.createBundle(category = category ?: "")
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_searchVideoFragment,
                    bundle
                )
            )
        }
    }

    private fun setupRecyclerByPosition(position: Int) {
        cl_no_data.visibility = View.GONE

        rv_home.visibility = View.GONE
        rv_video.visibility = View.GONE
        rv_follow.visibility = View.GONE
        rv_clip.visibility = View.GONE
        rv_picture.visibility = View.GONE
        rv_text.visibility = View.GONE
        rv_club.visibility = View.GONE

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
                rv_video.visibility = View.VISIBLE
                takeIf { rv_video.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_video.layoutManager = GridLayoutManager(requireContext(), 3)
                    rv_video.adapter = movieListAdapter
                    getData(position)
                }
            }
            2 -> {
                rv_follow.visibility = View.VISIBLE
                takeIf { rv_follow.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_follow.layoutManager = GridLayoutManager(requireContext(), 3)
                    rv_follow.adapter = dramaListAdapter
                    getData(position)
                }
            }
            3 -> {
                rv_clip.visibility = View.VISIBLE
                takeIf { rv_clip.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_clip.layoutManager = GridLayoutManager(requireContext(), 3)
                    rv_clip.adapter = varietyListAdapter
                    getData(position)
                }
            }
            else -> {
                rv_picture.visibility = View.VISIBLE
                takeIf { rv_picture.adapter == null }?.also {
                    refresh.isRefreshing = true
                    rv_picture.layoutManager = GridLayoutManager(requireContext(), 3)
                    rv_picture.adapter = animationListAdapter
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
            { id, view, type -> viewModel.loadImage(id, view, type) },
            { _, _, _, _ -> }
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
            startActivityForResult(intent, REQUEST_LOGIN)
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

        override fun onClickBanner(item: CarouselHolderItem) {
            GeneralUtils.openWebView(requireContext(), item.url)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_LOGIN -> {
                    findNavController().navigate(
                        R.id.action_homeFragment_to_loginFragment,
                        data?.extras
                    )
                }
            }
        }
    }
}
