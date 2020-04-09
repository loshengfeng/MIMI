package com.dabenxiang.mimi.view.home

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
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
        override fun onHeaderItemClick(view: View, template: HomeTemplate.Header) {
            Timber.d("$template")
        }

        override fun onVideoClick(view: View) {

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

        for (i in 1..10) {
            layout_top_tap.addTab(layout_top_tap.newTab().setText("第${i}層"))
        }
    }

    private fun loadHome() {
        btn_all.visibility = View.GONE

        val templateList = mutableListOf<HomeTemplate>()
        templateList.add(HomeTemplate.Banner)
        templateList.add(HomeTemplate.Carousel)
        templateList.add(HomeTemplate.Header(null, "分類1"))
        templateList.add(HomeTemplate.Categories())
        templateList.add(HomeTemplate.Header(null, "分類2"))
        templateList.add(HomeTemplate.Categories())

        adapter.setDataSrc(templateList)
    }

    private fun loadCategories() {
        btn_all.visibility = View.VISIBLE

        val templateList = mutableListOf<HomeTemplate>()
        templateList.add(HomeTemplate.Banner)
        templateList.add(HomeTemplate.VideoList)

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
