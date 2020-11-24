package com.dabenxiang.mimi.view.home.category

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.VideoSearchItem
import com.dabenxiang.mimi.model.vo.CarouselHolderItem
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.adapter.FilterTabAdapter
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.adapter.HomeVideoListAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.home.HomeTemplate
import com.dabenxiang.mimi.view.home.viewholder.*
import com.dabenxiang.mimi.view.player.ui.PlayerFragment
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_mimi_categories.*
import java.util.concurrent.atomic.AtomicBoolean

class MimiCategoriesFragment : BaseFragment() {

    companion object {
        private const val REQUEST_LOGIN = 1000

        const val KEY_DATA = "data"
        const val KEY_CATEGORY = "category"

        const val TEXT_ALL = "全部"

        fun createBundle(category: String): Bundle {
            return Bundle().also {
                it.putString(KEY_CATEGORY, category)
            }
        }
    }

    private val viewModel: CategoriesViewModel by viewModels()

    private var category: String = ""
    private var filterLLList: List<LinearLayout> = listOf()
    private var filterRVList: List<RecyclerView> = listOf()
    private var filterTVList: List<TextView> = listOf()
    private var filterAdapterList = mutableMapOf<Int, FilterTabAdapter>()
    private var filterDataList: ArrayList<List<String>> = arrayListOf()

    private val videoListAdapter by lazy {
        HomeVideoListAdapter(adapterListener)
    }

    private val adapterListener = object : HomeAdapter.EventListener {
        override fun onVideoClick(view: View, item: PlayerItem) {
            val bundle = PlayerFragment.createBundle(item)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_categoriesFragment_to_navigation_player,
                    bundle
                )
            )
        }

        override fun onHeaderItemClick(view: View, item: HomeTemplate.Header) {}
        override fun onClipClick(view: View, item: List<MemberPostItem>, position: Int) {}
        override fun onPictureClick(view: View, item: MemberPostItem) {}
        override fun onClubClick(view: View, item: MemberClubItem) {}
        override fun onLoadBannerViewHolder(vh: HomeBannerViewHolder) {}
        override fun onLoadClipViewHolder(vh: HomeClipViewHolder) {}
        override fun onLoadPictureViewHolder(vh: HomePictureViewHolder) {}
        override fun onLoadClubViewHolder(vh: HomeClubViewHolder) {}
        override fun onClickBanner(item: CarouselHolderItem) {}

        override fun onLoadStatisticsViewHolder(
            vh: HomeStatisticsViewHolder,
            src: HomeTemplate.Statistics
        ) {
        }

        override fun onLoadCarouselViewHolder(
            vh: HomeCarouselViewHolder,
            src: HomeTemplate.Carousel
        ) {
        }
    }

    override fun getLayoutId() = R.layout.fragment_mimi_categories

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    private val isAdult = true

    override fun setupFirstTime() {
        super.setupFirstTime()

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()

        category = arguments?.getString(KEY_CATEGORY, "") ?: ""
        tv_title.text = category

        recyclerview_content.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerview_content.adapter = videoListAdapter

//        viewModel.getCategoryDetail(category, true)
        viewModel.getVideoFilterList(null, null, null, true)
        progressHUD?.show()
    }

    override fun setupObservers() {
        filterLLList = listOf(ll_filter_0, ll_filter_1)
        filterRVList = listOf(rl_filter_0, rl_filter_1)
        filterTVList = listOf(tv_all_1)

        viewModel.getCategoryDetailResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    progressHUD?.dismiss()
                    setupFilterArea(it.result)
                }
                is Error -> {
                    progressHUD?.dismiss()
                }
            }
        })

        viewModel.videoList.observe(viewLifecycleOwner, Observer {
            videoListAdapter.submitList(it)
        })

        viewModel.filterList.observe(viewLifecycleOwner, Observer { data ->
            progressHUD?.dismiss()
            videoListAdapter.submitList(data)
        })

        viewModel.filterCategoryResult.observe(viewLifecycleOwner, Observer {
            filterAdapterList[1]?.updateList(it.areas, null)
            filterAdapterList[2]?.updateList(it.years, null)
        })

        viewModel.onTotalCountResult.observe(viewLifecycleOwner, Observer {
            cl_no_data.visibility =
                it.takeIf { it == 0L }?.let { View.VISIBLE } ?: let { View.GONE }
        })

        setupFilterArea()
    }

    override fun setupListeners() {

        ib_back.setOnClickListener {
            navigateTo(NavigateItem.Up)
        }

        iv_search.setOnClickListener {
            val bundle = SearchVideoFragment.createBundle()
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_searchVideoFragment,
                    bundle
                )
            )
        }

        bar_collapsing_filter.setOnClickListener {
            bar_filter.translationY = 0f
            setCollapsingFilterBar(View.GONE)
        }

        tv_all_1.setOnClickListener {
            updateFirstTab(1, true)
            viewModel.updatedFilterPosition(1, null)
            filterAdapterList[1]?.updateLastSelected(null)
            doOnTabSelected()
        }
    }

    override fun onResume() {
        super.onResume()
        recyclerview_content.addOnScrollListener(onScrollListener)
    }

    override fun onPause() {
        super.onPause()
        recyclerview_content.removeOnScrollListener(onScrollListener)
    }

    private fun setupFilterArea(item: VideoSearchItem? = null) {
        var notEmptyCount = 0
        val typeList = arrayListOf("最新", "熱門")
//        data.categories?.forEach { item -> typeList.add(item.name) }
        takeIf { typeList.isNotEmpty() }?.also { notEmptyCount++ }
        setupFilter(0, typeList)
        val firstPosition = viewModel.filterPositionData(0)?.value ?: 0
        val secondCategory = arrayListOf("最新1", "最新2", "最新3", "最新4", "最新5", "最新6")
//            val thirdCategory = arrayListOf<String>()
//            data.categories?.get(firstPosition)?.categories?.forEach { item ->
//                secondCategory.add(
//                    item.name
//                )
//            }
        takeIf { secondCategory.isNotEmpty() }?.also { notEmptyCount++ }
        setupFilter(1, secondCategory)
//            setupFilter(2, thirdCategory)

        filterTVList.forEach { tv -> tv.visibility = View.VISIBLE }
        setupCollapsingText()
        adjustContentRV(notEmptyCount)
    }

    private fun setupFilter(index: Int, list: List<String>) {
        if (index < filterDataList.size) filterDataList.removeAt(index)
        filterDataList.add(index, list)
        val adapter = FilterTabAdapter(object : FilterTabAdapter.FilterTabAdapterListener {
            override fun onSelectedFilterTab(
                recyclerView: RecyclerView,
                position: Int,
                keyword: String
            ) {
                viewModel.updatedFilterPosition(index, position)
                takeIf { index == 0 }?.also { //選擇第一欄
                    //更新第二欄
                    val secondCategory =
                        if(position ==0)arrayListOf("最新1", "最新2", "最新3", "最新4", "最新5", "最新6")
                        else arrayListOf("熱門1", "熱門2", "熱門3", "熱門4", "熱門5", "熱門6")
                    setupFilter(1, secondCategory)
                    adjustContentRV(2)
                    updateFirstTab(index, false)
                    viewModel.updatedFilterPosition(index, position)
                    filterAdapterList[0]?.updateLastSelected(position)
                    for (i in 1..2) { //更新其它欄至"全部"
                        updateFirstTab(i, true)
                        viewModel.updatedFilterPosition(i, null)
                        filterAdapterList[i]?.updateLastSelected(null)
                    }
                } ?: run {
                    updateFirstTab(index, false)
                    viewModel.updatedFilterPosition(index, position)
                    filterAdapterList[index]?.notifyDataSetChanged()
                    filterAdapterList[index]?.updateLastSelected(position)
                }
                doOnTabSelected()
            }
        }, isAdult)
        adapter.submitList(list, if (index == 0) 0 else null)

        filterRVList[index].adapter = adapter
        filterAdapterList[index] = adapter
        filterLLList[index].visibility = if (list.isEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }

        viewModel.filterPositionData(index)?.observe(viewLifecycleOwner, Observer { position ->
            adapter.setLastSelectedIndex(position)
            setupCollapsingText()
        })
    }

    private fun doOnTabSelected() {
        val filterKeyList: ArrayList<String?> = arrayListOf()
        filterDataList.forEachIndexed { index, list ->
            val lastPosition = viewModel.filterPositionData(index)?.value

            lastPosition?.takeIf { it < list.size }?.let { list[it] }.also {
                when (index) {
                    0 -> { //第1欄category格式: ex. 电影,剧情片
                        val key = it.takeUnless { it == TEXT_ALL }?.let { key ->
                            if (isAdult) key else "$category,$key"
                        } ?: let {
                            if (isAdult) null else category
                        }
                        filterKeyList.add(key)
                    }
                    else -> {
                        it.takeUnless { it == TEXT_ALL }?.also { key ->
                            filterKeyList.add(key)
                        } ?: run { filterKeyList.add(null) }
                    }
                }
            }
        }

        progressHUD?.show()
        val tag = if (filterKeyList[1] == TEXT_ALL) "" else filterKeyList[1] ?: ""
        viewModel.getVideoFilterList(filterKeyList[0], null, null, isAdult, tag)
    }

    private fun setupCollapsingText() {
        val sb = StringBuilder()
        val isFirst = AtomicBoolean(true)
        filterDataList.forEachIndexed { index, list ->
            val lastPosition = viewModel.filterPositionData(index)?.value
            if (list.isNotEmpty()) {
                val key = lastPosition?.takeIf { it < list.size }?.let { list[it] }
                    ?: let { TEXT_ALL }
                takeIf { isFirst.compareAndSet(true, false) }?.also {
                    sb.append(key)
                } ?: run { sb.append(", ").append(key) }
            }
        }.takeIf { sb.isNotEmpty() }?.run { tv_collapsing_filter.text = sb.toString() }
    }

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val filterTranslationY = bar_filter.translationY
            when {
                filterTranslationY > 0 -> bar_filter.translationY = 0f
                filterTranslationY <= -bar_filter.height.toFloat() && dy > 0 -> bar_filter.translationY =
                    -bar_filter.height.toFloat()
                filterTranslationY <= 0 -> {
                    bar_filter.translationY = if (bar_filter.translationY - dy > 0f) {
                        0f
                    } else {
                        bar_filter.translationY - dy
                    }
                }
            }

            when {
                filterTranslationY <= -bar_filter.height.toFloat() && dy > 0 -> setCollapsingFilterBar(
                    View.VISIBLE
                )
                else -> setCollapsingFilterBar(View.GONE)
            }
        }
    }

    private val collapsingFilterAnimator = ObjectAnimator()

    private fun setCollapsingFilterBar(visibility: Int) {
        if (collapsingFilterAnimator.isRunning) {
            return
        } else if (bar_collapsing_filter.visibility == visibility) {
            return
        }
        val start = if (visibility == View.VISIBLE) {
            0f
        } else {
            1f
        }
        val end = if (visibility == View.VISIBLE) {
            1f
        } else {
            0f
        }
        collapsingFilterAnimator.target = bar_collapsing_filter
        collapsingFilterAnimator.setPropertyName("alpha")
        collapsingFilterAnimator.setFloatValues(start, end)
        collapsingFilterAnimator.duration = 500L
        collapsingFilterAnimator.removeAllListeners()
        collapsingFilterAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                if (visibility != View.VISIBLE) {
                    bar_collapsing_filter.visibility = View.GONE
                }
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {
                if (visibility == View.VISIBLE) {
                    bar_collapsing_filter.visibility = View.VISIBLE
                }
            }
        })
        collapsingFilterAnimator.start()
    }

    private fun updateFirstTab(index: Int, isSelect: Boolean) {
        takeIf { isSelect }?.also {
            tv_all_1.setTextColor(requireContext().getColor(R.color.color_white_1))
            tv_all_1.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_red_1_radius_6)
        } ?: run {
            tv_all_1.setTextColor(requireContext().getColor(R.color.normal_color_text))
            tv_all_1.background = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_LOGIN -> {
                    findNavController().navigate(
                        R.id.action_categoriesFragment_to_loginFragment,
                        data?.extras
                    )
                }
            }
        }
    }

    private fun adjustContentRV(notEmptyCount: Int) {
        recyclerview_content.setPadding(
            0,
            GeneralUtils.dpToPx(requireContext(), 50) * notEmptyCount,
            0,
            0
        )
    }
}