package com.dabenxiang.mimi.view.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.CategoriesItem
import com.dabenxiang.mimi.model.holder.CarouselHolderItem
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.adapter.HomeCategoriesAdapter
import com.dabenxiang.mimi.view.adapter.TopTabAdapter
import com.dabenxiang.mimi.view.adapter.HomeVideoListAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.view.search.SearchVideoFragment
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseFragment<HomeViewModel>() {

    companion object {
        var lastPosition = 0
    }

    private val viewModel by viewModel<HomeViewModel>()

    override fun fetchViewModel(): HomeViewModel? {
        return viewModel
    }

    override fun getLayoutId() = R.layout.fragment_home

    private val tabAdapter by lazy {
        TopTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                viewModel.setTopTabPosition(index)
            }
        }, false)
    }

    private val adapter by lazy {
        HomeAdapter(requireContext(), adapterListener, false)
    }

    private val videoListAdapter by lazy {
        HomeVideoListAdapter(adapterListener, false)
    }

    private val adapterListener = object : HomeAdapter.EventListener {
        override fun onHeaderItemClick(view: View, item: HomeTemplate.Header) {
            val bundle = CategoriesFragment.createBundle(item.title, item.categories)
            navigateTo(NavigateItem.Destination(R.id.action_homeFragment_to_categoriesFragment, bundle))
        }

        override fun onVideoClick(view: View, item: PlayerData) {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtras(PlayerActivity.createBundle(item))
            startActivityForResult(intent, PlayerActivity.REQUEST_CODE)
        }

        override fun onLoadAdapter(adapter: HomeCategoriesAdapter, src: HomeTemplate.Categories) {
            viewModel.loadNestedCategoriesList(adapter, src)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            backToDesktop()
        }

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

    private fun loadFirstTab(root: CategoriesItem?) {
        recyclerview_videos.visibility = View.GONE
        refresh_home.visibility = View.VISIBLE

        val templateList = mutableListOf<HomeTemplate>()

        templateList.add(HomeTemplate.Banner(imgUrl = "https://tspimg.tstartel.com/upload/material/95/28511/mie_201909111854090.png"))
        templateList.add(HomeTemplate.Carousel(getTempCarouselList()))

        if (root?.categories != null) {
            for (item in root.categories) {
                templateList.add(HomeTemplate.Header(null, item.name, item.name))
                templateList.add(HomeTemplate.Categories(item.name, item.name, false))
            }
        }

        adapter.submitList(templateList)
    }

    //TODO: Testing
    private fun getTempCarouselList(): List<CarouselHolderItem> {
        val list = mutableListOf<CarouselHolderItem>()

        repeat(2) {
            list.add(CarouselHolderItem("https://tspimg.tstartel.com/upload/material/95/28511/mie_201909111854090.png"))
        }

        return list
    }

    private fun loadCategories(keyword: String?) {
        recyclerview_videos.visibility = View.VISIBLE
        refresh_home.visibility = View.GONE

        viewModel.setupVideoList(keyword, false)
    }

    override fun setupObservers() {
        mainViewModel?.also { mainViewModel ->
            mainViewModel.categoriesData.observe(viewLifecycleOwner, Observer { item ->

                val list = mutableListOf<String>()
                list.add("首頁")

                val normal = item.getNormal()
                normal?.categories?.also { level2 ->
                    for (i in 0 until level2.count()) {
                        val detail = level2[i]
                        list.add(detail.name)
                    }

                    tabAdapter.submitList(list, lastPosition)
                    loadFirstTab(normal)
                }
            })
        }

        viewModel.tabLayoutPosition.observe(viewLifecycleOwner, Observer { position ->
            lastPosition = position

            tabAdapter.setLastSelectedIndex(lastPosition)

            when (position) {
                0 -> {
                    btn_filter.visibility = View.GONE
                    loadFirstTab(mainViewModel?.categoriesData?.value?.getNormal())
                }
                else -> {
                    btn_filter.visibility = View.VISIBLE
                    loadCategories(mainViewModel?.categoriesData?.value?.getNormal()?.categories?.get(position - 1)?.name)
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
