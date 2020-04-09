package com.dabenxiang.mimi.view.home

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.CarouselHolderItem
import com.dabenxiang.mimi.model.holder.VideoHolderItem
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.base.BaseFragment2
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class HomeFragment : BaseFragment2<HomeViewModel>() {

    private val viewModel by viewModel<HomeViewModel>()

    override fun fetchViewModel(): HomeViewModel? {
        return viewModel
    }

    override fun getLayoutId() = R.layout.fragment_home

    private val adapter by lazy {
        HomeAdapter(context!!, adapterListener)
    }

    private val adapterListener = object : HomeAdapter.EventListener {
        override fun onHeaderItemClick(view: View, item: HomeTemplate.Header) {
            Timber.d("$item")
        }

        override fun onVideoClick(view: View, item: VideoHolderItem) {
            Timber.d("$item")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("onCreate Home")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("onViewCreated Home")

        activity?.also { activity ->
            LinearLayoutManager(activity).also { layoutManager ->
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                recyclerview_content.layoutManager = layoutManager
            }

            recyclerview_content.adapter = adapter
        }

        loadSample()
    }

    //TODO: Testing
    private fun loadSample() {
        loadHome()

        //viewModel.loadHomeCategories()

        repeat(10) { i ->
            layout_top_tap.addTab(layout_top_tap.newTab().setText("第${i}層"))
        }
    }

    private fun loadHome() {
        btn_all.visibility = View.GONE

        val templateList = mutableListOf<HomeTemplate>()
        templateList.add(HomeTemplate.Banner(imgUrl = "https://tspimg.tstartel.com/upload/material/95/28511/mie_201909111854090.png"))
        templateList.add(HomeTemplate.Carousel(getTempCarouselList()))
        templateList.add(HomeTemplate.Header(null, "分類1"))
        templateList.add(HomeTemplate.Categories(getTempVideoList()))
        templateList.add(HomeTemplate.Header(null, "分類2"))
        templateList.add(HomeTemplate.Categories(getTempVideoList()))

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
                    imgUrl = "https://i2.kknews.cc/SIG=1nkii03/470400035pnr3n5r3s7n.jpg"
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
        btn_all.visibility = View.VISIBLE

        val templateList = mutableListOf<HomeTemplate>()
        templateList.add(HomeTemplate.Banner(imgUrl = "https://tspimg.tstartel.com/upload/material/95/28511/mie_201909111854090.png"))
        templateList.add(HomeTemplate.VideoList(getTempVideoList()))

        adapter.setDataSrc(templateList)
    }

    override fun setupObservers() {
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

                val position = tab!!.position

                mainViewModel?.enableNightMode?.value = position == 1

                when (position) {
                    0 -> {
                        loadHome()
                    }

                    1 -> {

                    }
                    else -> {
                        //viewModel.navigateTo(NavigateItem.Destination(R.id.action_homeFragment_to_categoriesFragment))
                        loadCategories()
                    }
                }
            }
        })
    }
}
