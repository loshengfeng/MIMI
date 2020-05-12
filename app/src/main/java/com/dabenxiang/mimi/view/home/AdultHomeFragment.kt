package com.dabenxiang.mimi.view.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.CategoriesItem
import com.dabenxiang.mimi.model.holder.CarouselHolderItem
import com.dabenxiang.mimi.model.holder.VideoHolderItem
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.adapter.HomeCategoriesAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.view.search.SearchVideoFragment
import com.dabenxiang.mimi.widget.view.setBtnSolidDolor
import com.google.android.material.tabs.TabLayout
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

    private val adapter by lazy {
        HomeAdapter(context!!, adapterListener, true)
    }

    private val adapterListener = object : HomeAdapter.EventListener {
        override fun onHeaderItemClick(view: View, item: HomeTemplate.Header) {
            Timber.d("$item")

            val bundle = CategoriesFragment.createBundle(item.id ?: "", item.title ?: "", true)

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

        activity?.also { activity ->
            LinearLayoutManager(activity).also { layoutManager ->
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                recyclerview_content.layoutManager = layoutManager
            }

            recyclerview_content.adapter = adapter
        }
    }

    private fun setupAdultUI() {
        mainViewModel?.adultMode?.value = true

        layout_top.background = requireActivity().getDrawable(R.color.adult_color_status_bar)
        layout_top_tap.setTabTextColors(
            requireActivity().getColor(R.color.adult_color_text),
            requireActivity().getColor(R.color.color_tab_unselected_text)
        )

        layout_search_bar.background = requireActivity().getDrawable(R.color.adult_color_background)
        iv_bg_search.setBtnSolidDolor(requireActivity().getColor(R.color.adult_color_search_bar))

        iv_search.setImageResource(R.drawable.ic_adult_btn_search)
        tv_search.setTextColor(requireActivity().getColor(R.color.adult_color_search_text))

        recyclerview_content.background = requireActivity().getDrawable(R.color.adult_color_background)

        btn_filter.setTextColor(requireActivity().getColor(R.color.adult_color_search_text))
        btn_filter.setBtnSolidDolor(
            requireActivity().getColor(R.color.color_white_1_30),
            requireActivity().getColor(R.color.color_red_1),
            resources.getDimension(R.dimen.dp_6)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mainViewModel?.adultMode?.value = false
    }

    //TODO: Testing
    private fun loadSample() {
        //loadFirstTab()

        /*
        repeat(10) { i ->
            layout_top_tap.addTab(layout_top_tap.newTab().setText("第一層${i + 1}"))
        }
        */
    }

    private fun loadFirstTab(list: List<CategoriesItem>?) {
        val templateList = mutableListOf<HomeTemplate>()

        templateList.add(HomeTemplate.Banner(imgUrl = "https://tspimg.tstartel.com/upload/material/95/28511/mie_201909111854090.png"))
        templateList.add(HomeTemplate.Carousel(getTempCarouselList()))

        if (list != null) {
            for (item in list) {
                templateList.add(HomeTemplate.Header(item.id, null, item.name))
                templateList.add(HomeTemplate.Categories(item.id, item.name))
            }
        }

        adapter.setDataSrc(templateList)
    }

    //TODO: Testing
    private fun getTempVideoList(): List<VideoHolderItem> {
        val list = mutableListOf<VideoHolderItem>()

        repeat(12) {
            list.add(
                VideoHolderItem(
                    title = "標題",
                    resolution = "720P",
                    info = "全30集",
                    imgUrl = "https://i2.kknews.cc/SIG=1nkii03/470400035pnr3n5r3s7n.jpg",
                    isAdult = true
                )
            )
        }

        return list
    }

    //TODO: Testing
    private fun getTempCarouselList(): List<CarouselHolderItem> {
        val list = mutableListOf<CarouselHolderItem>()

        repeat(5) {
            list.add(CarouselHolderItem("https://tspimg.tstartel.com/upload/material/95/28511/mie_201909111854090.png"))
        }

        return list
    }

    private fun loadCategories() {
        val templateList = mutableListOf<HomeTemplate>()
        templateList.add(HomeTemplate.Banner(imgUrl = "https://tspimg.tstartel.com/upload/material/95/28511/mie_201909111854090.png"))
        templateList.add(HomeTemplate.VideoList(getTempVideoList()))

        adapter.setDataSrc(templateList)
    }

    override fun setupObservers() {
        mainViewModel?.also { mainViewModel ->
            mainViewModel.categoriesData.observe(viewLifecycleOwner, Observer { item ->

                layout_top_tap.removeAllTabs()

                item.categories?.also { level1 ->
                    for (i in 0 until level1.count()) {
                        val detail = level1[i]
                        layout_top_tap.addTab(layout_top_tap.newTab().setText(detail.name), i == lastPosition)
                    }

                    loadFirstTab(level1[0].categories)
                }
            })
        }

        viewModel.tabLayoutPosition.observe(viewLifecycleOwner, Observer { position ->
            lastPosition = position

            when (position) {
                0 -> {
                    btn_filter.visibility = View.GONE
                    loadFirstTab(mainViewModel?.categoriesData?.value?.categories?.get(position)?.categories)
                    //mainViewModel?.enableNightMode?.value = false
                }

                layout_top_tap.tabCount - 1 -> {
                    btn_filter.visibility = View.VISIBLE
                    loadFirstTab(mainViewModel?.categoriesData?.value?.categories?.get(position)?.categories)
                    //mainViewModel?.enableNightMode?.value = true
                }
                else -> {
                    btn_filter.visibility = View.VISIBLE
                    loadCategories()
                    //mainViewModel?.enableNightMode?.value = false
                }
            }
        })
    }

    override fun setupListeners() {
        layout_top_tap.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                Timber.d("onTabReselected: ${tab?.position}")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                Timber.d("onTabUnselected: ${tab?.position}")
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                Timber.d("onTabSelected: ${tab?.position}")

                viewModel.setTopTabPosition(tab!!.position)
            }
        })

        iv_bg_search.setOnClickListener {
            val bundle = SearchVideoFragment.createBundle("", "", true)
            navigateTo(NavigateItem.Destination(R.id.action_homeFragment_to_searchVideoFragment, bundle))
        }
    }
}
