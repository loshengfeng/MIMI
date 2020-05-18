package com.dabenxiang.mimi.view.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.SecondCategoriesItem
import com.dabenxiang.mimi.model.holder.CarouselHolderItem
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.adapter.HomeCategoriesAdapter
import com.dabenxiang.mimi.view.adapter.HomeTabAdapter
import com.dabenxiang.mimi.view.adapter.HomeVideoListAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.view.search.SearchVideoFragment
import com.dabenxiang.mimi.widget.view.setBtnSolidDolor
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class AdultHomeFragment : BaseFragment<HomeViewModel>() {

    companion object {
        var lastPosition = 0
    }

    private val viewModel by viewModel<HomeViewModel>()

    override fun fetchViewModel(): HomeViewModel? {
        return viewModel
    }

    override fun getLayoutId() = R.layout.fragment_home

    private val tabAdapter by lazy {
        HomeTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
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
            Timber.d("$item")

            val bundle = CategoriesFragment.createBundle(item.title ?: "")

            navigateTo(NavigateItem.Destination(R.id.action_homeFragment_to_categoriesFragment, bundle))
        }

        override fun onVideoClick(view: View, item: PlayerData) {
            Timber.d("$item")

            val intent = Intent(activity!!, PlayerActivity::class.java)
            intent.putExtras(PlayerActivity.createBundle(item))
            startActivity(intent)
        }

        override fun onLoadAdapter(adapter: HomeCategoriesAdapter, src: HomeTemplate.Categories) {
            viewModel.loadNestedCategoriesList(adapter, src)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("onCreate Home")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("onViewCreated Home")

        setupAdultUI()

        LinearLayoutManager(activity).also { layoutManager ->
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            recyclerview_tab.layoutManager = layoutManager
        }

        recyclerview_tab.adapter = tabAdapter

        LinearLayoutManager(activity).also { layoutManager ->
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerview_content.layoutManager = layoutManager
        }

        recyclerview_content.adapter = adapter

        GridLayoutManager(activity, 2).also { layoutManager ->
            recyclerview_videos.layoutManager = layoutManager
        }

        recyclerview_videos.adapter = videoListAdapter

        viewModel.videoList.observe(viewLifecycleOwner, Observer {
            videoListAdapter.submitList(it)
        })
    }

    private fun setupAdultUI() {
        layout_top.background = requireActivity().getDrawable(R.color.adult_color_status_bar)

        layout_search_bar.background = requireActivity().getDrawable(R.color.adult_color_background)
        iv_bg_search.setBtnSolidDolor(requireActivity().getColor(R.color.adult_color_search_bar))

        iv_search.setImageResource(R.drawable.ic_adult_btn_search)
        tv_search.setTextColor(requireActivity().getColor(R.color.adult_color_search_text))

        recyclerview_content.background = requireActivity().getDrawable(R.color.adult_color_background)
        recyclerview_videos.background = requireActivity().getDrawable(R.color.adult_color_background)

        btn_filter.setTextColor(requireActivity().getColor(R.color.adult_color_search_text))
        btn_filter.setBtnSolidDolor(
            requireActivity().getColor(R.color.color_white_1_30),
            requireActivity().getColor(R.color.color_red_1),
            resources.getDimension(R.dimen.dp_6)
        )
    }

    private fun loadFirstTab(list: List<SecondCategoriesItem>?) {
        recyclerview_videos.visibility = View.GONE
        recyclerview_content.visibility = View.VISIBLE

        val templateList = mutableListOf<HomeTemplate>()

        templateList.add(HomeTemplate.Banner(imgUrl = "https://tspimg.tstartel.com/upload/material/95/28511/mie_201909111854090.png"))
        templateList.add(HomeTemplate.Carousel(getTempCarouselList()))

        if (list != null) {
            for (item in list) {
                templateList.add(HomeTemplate.Header(null, item.name))
                templateList.add(HomeTemplate.Categories(item.name))
            }
        }

        adapter.setDataSrc(templateList)
    }

    //TODO: Testing
    private fun getTempCarouselList(): List<CarouselHolderItem> {
        val list = mutableListOf<CarouselHolderItem>()

        repeat(5) {
            list.add(CarouselHolderItem("https://tspimg.tstartel.com/upload/material/95/28511/mie_201909111854090.png"))
        }

        return list
    }

    private fun loadCategories(keyword: String?) {
        recyclerview_videos.visibility = View.VISIBLE
        recyclerview_content.visibility = View.GONE

        viewModel.setupVideoList(true, keyword)
    }

    override fun setupObservers() {
        mainViewModel?.also { mainViewModel ->
            mainViewModel.categoriesData.observe(viewLifecycleOwner, Observer { item ->

                val list = mutableListOf<String>()
                item.categories?.also { level1 ->
                    for (i in 0 until level1.count()) {
                        val detail = level1[i]
                        list.add(detail.name ?: "")
                    }

                    tabAdapter.setTabList(list, lastPosition)

                    loadFirstTab(level1[0].categories)
                }
            })
        }

        viewModel.tabLayoutPosition.observe(viewLifecycleOwner, Observer { position ->
            lastPosition = position

            tabAdapter.setLastSelectedIndex(lastPosition)

            when (position) {
                0 -> {
                    btn_filter.visibility = View.GONE
                    loadFirstTab(mainViewModel?.categoriesData?.value?.categories?.get(position)?.categories)
                }
                else -> {
                    btn_filter.visibility = View.VISIBLE
                    loadCategories(mainViewModel?.categoriesData?.value?.categories?.get(position)?.name)
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
