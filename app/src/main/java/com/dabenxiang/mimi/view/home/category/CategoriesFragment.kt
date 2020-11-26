package com.dabenxiang.mimi.view.home.category

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.vo.CarouselHolderItem
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.adapter.CategoryVideoListAdapter
import com.dabenxiang.mimi.view.adapter.FilterTabAdapter
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.home.HomeTemplate
import com.dabenxiang.mimi.view.home.viewholder.*
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_categories.*

class CategoriesFragment : BaseFragment() {

    companion object {
        private const val REQUEST_LOGIN = 1000

        const val KEY_ORDER_BY = "orderByType"
        const val KEY_CATEGORY = "category"
        const val SORT = 0
        const val CATEGORY = 1

        fun createBundle(category: String, orderByType: Int): Bundle {
            return Bundle().also {
                it.putString(KEY_CATEGORY, category)
                it.putInt(KEY_ORDER_BY, orderByType)
            }
        }
    }

    private val viewModel: CategoriesViewModel by viewModels()

    private var orderByType = StatisticsOrderType.LATEST.value
    private var category = ""
    private var lstFilterRV: List<RecyclerView> = listOf()
    private var lstFilterText: ArrayList<List<String>> = arrayListOf()

    private val videoListAdapter by lazy {
        CategoryVideoListAdapter(adapterListener)
    }

    private val adapterListener = object : HomeAdapter.EventListener {
        override fun onVideoClick(view: View, item: PlayerItem) {
            // TODO: 跳至播放頁面
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

    override fun getLayoutId() = R.layout.fragment_categories

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun setupFirstTime() {
        super.setupFirstTime()

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()

        category = arguments?.getString(KEY_CATEGORY) ?: ""
        orderByType = arguments?.getInt(KEY_ORDER_BY) ?: StatisticsOrderType.LATEST.value
        setupTitle()

        recyclerview_content.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerview_content.adapter = videoListAdapter

        lstFilterRV = listOf(rl_filter_0, rl_filter_1)

        viewModel.getCategoryResult.observe(this, Observer {
            when (it) {
                is Success -> {
                    tv_all_1.visibility = View.VISIBLE
                    setupFilter(
                        SORT,
                        arrayListOf(
                            getString(R.string.category_newest),
                            getString(R.string.category_top_hit)
                        )
                    )
                    setupFilter(CATEGORY, it.result)
                    setupCollapsingText()
                    adjustContentRV(2)
                    getVideos()
                }
                is Error -> onApiError(it.throwable)
                else -> {
                }
            }
        })

        viewModel.getCategory()
    }

    override fun setupObservers() {
        viewModel.showProgress.observe(this, Observer { showProgress ->
            if (showProgress) progressHUD.show()
            else progressHUD.dismiss()
        })

        viewModel.videoList.observe(viewLifecycleOwner, Observer { data ->
            videoListAdapter.submitList(data)
        })

        viewModel.onTotalCountResult.observe(viewLifecycleOwner, Observer {
            cl_no_data.visibility =
                it.takeIf { it == 0L }?.let { View.VISIBLE } ?: let { View.GONE }
        })
    }

    override fun setupListeners() {

        ib_back.setOnClickListener {
            navigateTo(NavigateItem.Up)
        }

        iv_search.setOnClickListener {
            val bundle = SearchVideoFragment.createBundle(category = category)
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
            updateFirstTab(true)
            category = ""
            (lstFilterRV[CATEGORY].adapter as FilterTabAdapter).updateLastSelected(null)
            setupTitle()
            setupCollapsingText()
            getVideos()
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

    private fun setupFilter(index: Int, list: List<String>) {
        lstFilterText.add(index, list)
        val adapter = FilterTabAdapter(object : FilterTabAdapter.FilterTabAdapterListener {
            override fun onSelectedFilterTab(
                recyclerView: RecyclerView,
                position: Int,
                keyword: String
            ) {
                if (index == CATEGORY) {
                    category = keyword
                    updateFirstTab(false)
                    setupTitle()
                } else {
                    orderByType = if (position == 0) StatisticsOrderType.LATEST.value
                    else StatisticsOrderType.HOTTEST.value
                }
                val adapter = lstFilterRV[index].adapter as FilterTabAdapter
                adapter.notifyDataSetChanged()
                adapter.updateLastSelected(position)

                setupCollapsingText()
                getVideos()
            }
        })
        val initSelectIndex =
            when {
                index == SORT && orderByType == StatisticsOrderType.LATEST.value -> 0
                index == SORT && orderByType == StatisticsOrderType.HOTTEST.value -> 1
                index == CATEGORY && lstFilterText[CATEGORY].contains(category) ->
                    lstFilterText[CATEGORY].indexOf(category)
                else -> null
            }
        adapter.submitList(list, initSelectIndex)
        updateFirstTab(initSelectIndex == null)

        lstFilterRV[index].adapter = adapter
    }

    private fun getVideos() {
        viewModel.getVideoFilterList(category, orderByType)
    }

    @SuppressLint("SetTextI18n")
    private fun setupCollapsingText() {
        val textSorting =
            if (orderByType == StatisticsOrderType.LATEST.value) getString(R.string.category_newest)
            else getString(R.string.category_top_hit)
        val textCategory =
            if (category.isBlank()) getString(R.string.all)
            else category
        tv_collapsing_filter.text = "$textSorting, $textCategory"
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

    private fun updateFirstTab(isSelect: Boolean) {
        takeIf { isSelect }?.also {
            tv_all_1.setTextColor(requireContext().getColor(R.color.color_white_1))
            tv_all_1.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_red_1_radius_6)
        } ?: run {
            tv_all_1.setTextColor(requireContext().getColor(R.color.normal_color_text))
            tv_all_1.background = null
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (resultCode == Activity.RESULT_OK) {
//            when (requestCode) {
//                REQUEST_LOGIN -> {
//                    findNavController().navigate(
//                        R.id.action_categoriesFragment_to_loginFragment,
//                        data?.extras
//                    )
//                }
//            }
//        }
//    }

    private fun adjustContentRV(notEmptyCount: Int) {
        recyclerview_content.setPadding(
            0,
            GeneralUtils.dpToPx(requireContext(), 50) * notEmptyCount,
            0,
            0
        )
    }

    private fun setupTitle() {
        tv_title.text =
                if (category.isBlank()) getString(R.string.home_tab_video)
                else category
    }
}