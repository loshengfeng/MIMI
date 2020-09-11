package com.dabenxiang.mimi.view.home.category

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
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
import com.dabenxiang.mimi.model.vo.CategoriesItem
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.adapter.FilterTabAdapter
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.adapter.HomeVideoListAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.home.HomeTemplate
import com.dabenxiang.mimi.view.home.viewholder.*
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_categories.*
import java.util.concurrent.atomic.AtomicBoolean
import com.dabenxiang.mimi.model.api.vo.CategoriesItem as CategoriesData

class CategoriesFragment : BaseFragment() {

    companion object {
        private const val REQUEST_LOGIN = 1000

        const val KEY_DATA = "data"
        const val KEY_CATEGORY = "category"
        const val TEXT_ALL = "全部"

        fun createBundle(title: String, categories: String?, item: CategoriesData?): Bundle {
            val data = CategoriesItem()
            data.title = title
            data.categories = categories

            return Bundle().also {
                it.putSerializable(KEY_DATA, data)
                it.putSerializable(KEY_CATEGORY, item)
            }
        }
    }

    private val viewModel: CategoriesViewModel by viewModels()

    private var filterLLList: List<LinearLayout> = listOf()
    private var filterRVList: List<RecyclerView> = listOf()
    private var filterTVList: List<TextView> = listOf()
    private var filterAdapterList = mutableMapOf<Int, FilterTabAdapter>()
    private var filterDataList: ArrayList<List<String>> = arrayListOf()

    private val videoListAdapter by lazy {
        val isAdult = mainViewModel?.adultMode?.value ?: false
        HomeVideoListAdapter(adapterListener, isAdult)
    }

    private val adapterListener = object : HomeAdapter.EventListener {
        override fun onVideoClick(view: View, item: PlayerItem) {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtras(PlayerActivity.createBundle(item))
            startActivityForResult(intent, REQUEST_LOGIN)
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

    override fun getLayoutId(): Int {
        return R.layout.fragment_categories
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    private val isAdult by lazy { mainViewModel?.adultMode?.value ?: false }
    private val data by lazy { arguments?.getSerializable(KEY_DATA) as CategoriesItem }

    override fun setupFirstTime() {
        super.setupFirstTime()

        requireActivity().onBackPressedDispatcher.addCallback { navigateTo(NavigateItem.Up) }

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()

        (arguments?.getSerializable(KEY_DATA) as CategoriesItem?)?.also { data ->
            tv_title.text = data.title

            cl_root.background =
                if (isAdult) {
                    R.color.adult_color_background
                } else {
                    R.color.normal_color_background
                }.let { res ->
                    ContextCompat.getDrawable(requireContext(), res)
                }

            tv_no_data.setTextColor(takeIf { isAdult }?.let {
                requireActivity().getColorStateList(R.color.color_white_1)
            } ?: run {
                requireActivity().getColorStateList(R.color.color_black_2_50)
            })

            layout_top.background =
                if (isAdult) {
                    R.color.adult_color_status_bar
                } else {
                    R.color.normal_color_status_bar
                }.let { res ->
                    ContextCompat.getDrawable(requireContext(), res)
                }

            ib_back.setImageResource(
                if (isAdult) {
                    R.drawable.adult_btn_back
                } else {
                    R.drawable.normal_btn_back
                }
            )

            iv_search.setImageResource(
                if (isAdult) {
                    R.drawable.adult_btn_search
                } else {
                    R.drawable.normal_btn_search
                }
            )

            tv_title.setTextColor(
                if (isAdult) {
                    R.color.adult_color_text
                } else {
                    R.color.normal_color_text
                }.let { res ->
                    requireActivity().getColor(res)
                }
            )

            bar_collapsing_filter.setBackgroundColor(
                if (isAdult) {
                    R.color.color_black_3
                } else {
                    R.color.color_gray_12
                }.let { res ->
                    requireActivity().getColor(res)
                }
            )

            bar_filter.setBackgroundColor(
                if (isAdult) {
                    R.color.color_black_3
                } else {
                    R.color.color_gray_12
                }.let { res ->
                    requireActivity().getColor(res)
                }
            )

            tv_collapsing_filter.setTextColor(
                if (isAdult) {
                    R.color.color_white_1
                } else {
                    R.color.color_black_1
                }.let { res ->
                    requireActivity().getColor(res)
                }
            )

            recyclerview_content.layoutManager = GridLayoutManager(
                requireContext(), when (isAdult) {
                    true -> 2
                    else -> 3
                }
            )
            recyclerview_content.adapter = videoListAdapter

            viewModel.getCategoryDetail(data.title, isAdult)
            if (isAdult) {
                viewModel.getVideoFilterList(null, null, null, isAdult)
            } else {
                viewModel.setupVideoList(data.categories, isAdult)
            }
            progressHUD?.show()
        }
    }

    override fun setupObservers() {
        filterLLList = listOf(ll_filter_0, ll_filter_1, ll_filter_2)
        filterRVList = listOf(rl_filter_0, rl_filter_1, rl_filter_2)
        filterTVList = listOf(tv_all_0, tv_all_1, tv_all_2)

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
    }

    override fun setupListeners() {
        ib_back.setOnClickListener {
            navigateTo(NavigateItem.Up)
        }

        iv_search.setOnClickListener {
            val bundle = SearchVideoFragment.createBundle(category = tv_title.text.toString())
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

        tv_all_0.setOnClickListener {
            repeat(3) { //全部欄位設為"全部"
                updateFirstTab(it, true)
                viewModel.updatedFilterPosition(it, null)
                filterAdapterList[it]?.updateLastSelected(null)
            }
            doOnTabSelected()

            if (isAdult) { //第一欄按"全部" -> 清空第二欄
                setupFilter(1, arrayListOf())
                adjustContentRV(1)
            }
        }

        tv_all_1.setOnClickListener {
            updateFirstTab(1, true)
            viewModel.updatedFilterPosition(1, null)
            filterAdapterList[1]?.updateLastSelected(null)
            doOnTabSelected()
        }

        tv_all_2.setOnClickListener {
            updateFirstTab(2, true)
            viewModel.updatedFilterPosition(2, null)
            filterAdapterList[2]?.updateLastSelected(null)
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

    private fun setupFilterArea(item: VideoSearchItem?) {
        (arguments?.getSerializable(KEY_CATEGORY) as CategoriesData?)?.also { data ->
            var notEmptyCount = 0
            val typeList = arrayListOf<String>()
            data.categories?.forEach { item -> typeList.add(item.name) }
            takeIf { typeList.isNotEmpty() }?.also { notEmptyCount++ }
            setupFilter(0, typeList)
            if (isAdult) {
                val firstPosition = viewModel.filterPositionData(0)?.value ?: 0
                val secondCategory = arrayListOf<String>()
                val thirdCategory = arrayListOf<String>()
                data.categories?.get(firstPosition)?.categories?.forEach { item ->
                    secondCategory.add(
                        item.name
                    )
                }
                takeIf { secondCategory.isNotEmpty() }?.also { notEmptyCount++ }
                setupFilter(1, secondCategory)
                setupFilter(2, thirdCategory)
            } else {
                val areasCategory = item?.category?.areas ?: arrayListOf()
                val yearsCategory = item?.category?.years ?: arrayListOf()
                takeIf { areasCategory.isNotEmpty() }?.also { notEmptyCount++ }
                takeIf { yearsCategory.isNotEmpty() }?.also { notEmptyCount++ }
                setupFilter(1, areasCategory)
                setupFilter(2, yearsCategory)
            }

            filterTVList.forEach { tv -> tv.visibility = View.VISIBLE }
            setupCollapsingText()
            adjustContentRV(notEmptyCount)
        }
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
                    if (isAdult) { //更新第二欄
                        (arguments?.getSerializable(KEY_CATEGORY) as CategoriesData?)?.also { data ->
                            val secondCategory = arrayListOf<String>()
                            data.categories?.get(position)?.categories?.forEach { item ->
                                secondCategory.add(item.name)
                            }
                            setupFilter(1, secondCategory)
                        }
                        adjustContentRV(2)
                    }
                    updateFirstTab(index, false)
                    viewModel.updatedFilterPosition(index, position)
                    for (i in 1..2) { //更新其它欄至"全部"
                        updateFirstTab(i, true)
                        viewModel.updatedFilterPosition(i, null)
                        filterAdapterList[i]?.updateLastSelected(null)
                    }
                } ?: run {
                    updateFirstTab(index, false)
                    viewModel.updatedFilterPosition(index, position)
                    filterAdapterList[index]?.notifyDataSetChanged()
                }
                doOnTabSelected()
            }
        }, isAdult)
        adapter.submitList(list, null)

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
                            if (isAdult) key else "${data.title},$key"
                        } ?: let {
                            if (isAdult) null else data.title
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
        if (isAdult) {
            val tag = if (filterKeyList[1] == TEXT_ALL) "" else filterKeyList[1] ?: ""
            viewModel.getVideoFilterList(filterKeyList[0], null, null, isAdult, tag)
        } else {
            viewModel.getVideoFilterList(
                filterKeyList[0], filterKeyList[1], filterKeyList[2], isAdult
            )
        }
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
        val tv = when (index) {
            0 -> tv_all_0
            1 -> tv_all_1
            else -> tv_all_2
        }
        takeIf { isSelect }?.also {
            tv.setTextColor(requireContext().getColor(R.color.color_white_1))
            tv.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_red_1_radius_6)
        } ?: run {
            tv.setTextColor(takeIf { isAdult }?.let { requireContext().getColor(R.color.color_white_1) }
                ?: let { requireContext().getColor(R.color.normal_color_text) })
            tv.background = null
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