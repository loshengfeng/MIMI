package com.dabenxiang.mimi.view.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.extension.setBtnSolidColor
import com.dabenxiang.mimi.model.api.vo.CategoriesItem
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.adapter.HomeVideoListAdapter
import com.dabenxiang.mimi.view.adapter.TopTabAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.view.search.SearchVideoFragment
import kotlinx.android.synthetic.main.fragment_home.*

class AdultHomeFragment : BaseFragment<HomeViewModel>() {

    companion object {
        var lastPosition = 0
    }

    private val viewModel: HomeViewModel by viewModels()

    override fun fetchViewModel(): HomeViewModel? {
        return viewModel
    }

    override fun getLayoutId() = R.layout.fragment_home

    private val tabAdapter by lazy {
        TopTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                viewModel.setTopTabPosition(index)
            }
        }, true)
    }

    private val adapter by lazy {
        HomeAdapter(requireContext(), adapterListener, true)
    }

    private val videoListAdapter by lazy {
        HomeVideoListAdapter(adapterListener, true)
    }

    private val adapterListener = object : HomeAdapter.EventListener {
        override fun onHeaderItemClick(view: View, item: HomeTemplate.Header) {
            val bundle = CategoriesFragment.createBundle(item.title ?: "", item.categories)

            navigateTo(NavigateItem.Destination(R.id.action_homeFragment_to_categoriesFragment, bundle))
        }

        override fun onVideoClick(view: View, item: PlayerData) {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtras(PlayerActivity.createBundle(item))
            startActivity(intent)
        }

        override fun onLoadStatisticsViewHolder(vh: HomeStatisticsViewHolder, src: HomeTemplate.Statistics) {
            viewModel.loadNestedStatisticsList(vh, src)
        }

        override fun onLoadCarouselViewHolder(vh: HomeCarouselViewHolder, src: HomeTemplate.Carousel) {
            viewModel.loadNestedStatisticsListForCarousel(vh, src)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            backToDesktop()
        }

        setupAdultUI()

        recyclerview_tab.adapter = tabAdapter

        recyclerview_home.adapter = adapter

        recyclerview_videos.adapter = videoListAdapter

        refresh_home.setColorSchemeColors(requireContext().getColor(R.color.color_red_1))
        refresh_home.setOnRefreshListener {
            refresh_home.isRefreshing = false

            mainViewModel?.loadHomeCategories()
        }

        viewModel.videoList.observe(viewLifecycleOwner, Observer {
            videoListAdapter.submitList(it)
        })
    }

    private fun setupAdultUI() {
        layout_top.background = requireActivity().getDrawable(R.color.adult_color_status_bar)

        layout_search_bar.background = requireActivity().getDrawable(R.color.adult_color_background)
        iv_bg_search.setBtnSolidColor(requireActivity().getColor(R.color.adult_color_search_bar))

        iv_search.setImageResource(R.drawable.adult_btn_search)
        tv_search.setTextColor(requireActivity().getColor(R.color.adult_color_search_text))

        recyclerview_home.background = requireActivity().getDrawable(R.color.adult_color_background)
        recyclerview_videos.background = requireActivity().getDrawable(R.color.adult_color_background)

        btn_filter.setTextColor(requireActivity().getColor(R.color.adult_color_search_text))
        btn_filter.setBtnSolidColor(
            requireActivity().getColor(R.color.color_white_1_30),
            requireActivity().getColor(R.color.color_red_1),
            resources.getDimension(R.dimen.dp_6)
        )
    }

    private fun loadFirstTab(root: CategoriesItem?) {
        recyclerview_videos.visibility = View.GONE
        refresh_home.visibility = View.VISIBLE

        val templateList = mutableListOf<HomeTemplate>()

        templateList.add(HomeTemplate.Banner(imgUrl = "https://tspimg.tstartel.com/upload/material/95/28511/mie_201909111854090.png"))
        templateList.add(HomeTemplate.Carousel(true))

        if (root?.categories != null) {
            for (item in root.categories) {
                templateList.add(HomeTemplate.Header(null, item.name, item.name))
                when (item.name) {
                    "蜜蜜影视" -> templateList.add(HomeTemplate.Statistics(item.name, null, true))
                    "关注" -> { }
                    "短视频" -> { }
                    "图片" -> { }
                    "短文" -> { }
                    "圈子" -> { }
                }
            }
        }

        adapter.submitList(templateList)
    }

    private fun loadCategories(keyword: String?) {
        recyclerview_videos.visibility = View.VISIBLE
        refresh_home.visibility = View.GONE

        viewModel.setupVideoList(keyword, true)
    }

    override fun setupObservers() {
        mainViewModel?.also { mainViewModel ->
            mainViewModel.categoriesData.observe(viewLifecycleOwner, Observer { item ->

                val list = mutableListOf<String>()
                list.add("首页")

                val adult = item.getAdult()
                adult?.categories?.also { level2 ->
                    for (i in 0 until level2.count()) {
                        val detail = level2[i]
                        list.add(detail.name)
                    }

                    tabAdapter.submitList(list, lastPosition)

                    loadFirstTab(adult)
                }
            })
        }

        viewModel.tabLayoutPosition.observe(viewLifecycleOwner, Observer { position ->
            lastPosition = position

            tabAdapter.setLastSelectedIndex(lastPosition)

            when (position) {
                0 -> {
                    btn_filter.visibility = View.GONE
                    loadFirstTab(mainViewModel?.categoriesData?.value?.getAdult())
                }
                1 -> {
                    btn_filter.visibility = View.VISIBLE
                    loadCategories(null)
                }
                else -> {
                    btn_filter.visibility = View.VISIBLE
                    loadCategories(mainViewModel?.categoriesData?.value?.getAdult()?.categories?.get(position)?.name)
                }
            }
        })
    }

    override fun setupListeners() {
        iv_bg_search.setOnClickListener {
            val bundle = SearchVideoFragment.createBundle("")
            navigateTo(NavigateItem.Destination(R.id.action_homeFragment_to_searchVideoFragment, bundle))
        }
    }
}
