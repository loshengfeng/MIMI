package com.dabenxiang.mimi.view.home.category

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.CategoriesItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.serializable.CategoriesData
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.adapter.FilterTabAdapter
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.adapter.HomeVideoListAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.home.HomeTemplate
import com.dabenxiang.mimi.view.home.viewholder.*
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_categories.*
import timber.log.Timber

class CategoriesFragment : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"
        const val KEY_CATEGORY = "category"

        fun createBundle(title: String, categories: String, item: CategoriesItem?): Bundle {
            val data = CategoriesData()
            data.title = title
            data.categories = categories

            return Bundle().also {
                it.putSerializable(KEY_DATA, data)
                it.putSerializable(KEY_CATEGORY, item)
            }
        }
    }

    private val viewModel: CategoriesViewModel by viewModels()

    private val videoListAdapter by lazy {
        val isAdult = mainViewModel?.adultMode?.value ?: false
        HomeVideoListAdapter(adapterListener, isAdult)
    }

    private val adapterListener = object : HomeAdapter.EventListener {
        override fun onHeaderItemClick(view: View, item: HomeTemplate.Header) {
        }

        override fun onVideoClick(view: View, item: PlayerData) {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtras(PlayerActivity.createBundle(item))
            startActivity(intent)
        }

        override fun onClipClick(view: View, item: List<MemberPostItem>, position: Int) {
        }

        override fun onPictureClick(view: View, item: MemberPostItem) {

        }

        override fun onClubClick(view: View, item: MemberClubItem) {

        }

        override fun onLoadBannerViewHolder(vh: HomeBannerViewHolder) {

        }

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

        override fun onLoadClipViewHolder(vh: HomeClipViewHolder) {
            TODO("Not yet implemented")
        }

        override fun onLoadPictureViewHolder(vh: HomePictureViewHolder) {
            TODO("Not yet implemented")
        }

        override fun onLoadClubViewHolder(vh: HomeClubViewHolder) {
            TODO("Not yet implemented")
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_categories
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    private val isAdult by lazy { mainViewModel?.adultMode?.value ?: false }
    private val data by lazy {  arguments?.getSerializable(KEY_DATA) as CategoriesData }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (GeneralUtils.getScreenSize(requireActivity()).second * 0.0245).toInt()

        (arguments?.getSerializable(KEY_DATA) as CategoriesData?)?.also { data ->
            tv_title.text = data.title

            recyclerview_content.background =
                if (isAdult) {
                    R.color.adult_color_background
                } else {
                    R.color.normal_color_background
                }.let { res ->
                    requireActivity().getDrawable(res)
                }

            layout_top.background =
                if (isAdult) {
                    R.color.adult_color_status_bar
                } else {
                    R.color.normal_color_status_bar
                }.let { res ->
                    requireActivity().getDrawable(res)
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

            recyclerview_content.adapter = videoListAdapter


            viewModel.setupVideoList(data.categories, isAdult)

            viewModel.getCategoryDetail(data.title, isAdult)
            progressHUD?.show()
        }
    }

    private var filterViewList: List<RecyclerView> = listOf()
    private var filterAdapterList = mutableMapOf<Int, FilterTabAdapter>()
    private var filterDataList: ArrayList<List<String>> = arrayListOf()

    override fun setupObservers() {
        filterViewList = listOf(filter_0, filter_1, filter_2)

        viewModel.getCategoryDetailResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    progressHUD?.dismiss()
                    Timber.d("@@getCategoryDetailResult: ${it.result}")

                    (arguments?.getSerializable(KEY_CATEGORY) as CategoriesItem?)?.also { data ->
                        val typeList = arrayListOf<String>()
                        data.categories?.forEach { item ->
                            typeList.add(item.name)
                        }
                        setupFilter(0, typeList)
                        setupFilter(1, it.result.category?.areas ?: arrayListOf())
                        setupFilter(2, it.result.category?.years ?: arrayListOf())
                    }

                }
                is ApiResult.Error -> {
                    progressHUD?.dismiss()
                }
            }
        })

        viewModel.videoList.observe(viewLifecycleOwner, Observer {
            videoListAdapter.submitList(it)
        })

        viewModel.filterList.observe(viewLifecycleOwner, Observer {
            progressHUD?.dismiss()
            videoListAdapter.submitList(it)
        })
    }

    override fun setupListeners() {
        ib_back.setOnClickListener {
            navigateTo(NavigateItem.Up)
        }

        iv_search.setOnClickListener {
            //val bundle = SearchVideoFragment.createBundle("", f)
            //navigateTo(NavigateItem.Destination(R.id.action_categoriesFragment_to_searchVideoFragment, bundle))
        }

        bar_collapsing_filter.setOnClickListener {
            bar_filter.translationY = 0f
            setCollapsingFilterBar(View.GONE)
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
        filterDataList.add(index, list)
        val adapter = FilterTabAdapter(object : FilterTabAdapter.FilterTabAdapterListener {
            override fun onSelectedFilterTab(recyclerView: RecyclerView, position: Int, keyword: String) {
                viewModel.updatedFilterPosition(index, position)

                val filterKeyList: ArrayList<String?> = arrayListOf()
                filterDataList.forEachIndexed { index, list ->
                    val lastPosition = viewModel.filterPositionData(index)?.value

                    lastPosition?.takeIf { it < list.size }?.let { list[it] }.also {
                        when(index) {
                            0 -> {
                                it.takeUnless { it == "全部" }?.also { key ->
                                    filterKeyList.add("${data.title},$key")
                                } ?: run { filterKeyList.add(data.title) }

                            }
                            else -> {
                                it.takeUnless { it == "全部" }?.also { key ->
                                    filterKeyList.add(key)
                                } ?: run { filterKeyList.add(null) }
                            }
                        }
                    }
                }

                viewModel.getVideoFilterList(filterKeyList[0], filterKeyList[1], filterKeyList[2], isAdult)
                progressHUD?.show()
            }
        }, isAdult)
        adapter.submitList(list, 0)

        filterViewList[index].adapter = adapter
        filterAdapterList[index] = adapter
        if (list.isEmpty()) {
            filterViewList[index].visibility = View.GONE
        }

        viewModel.filterPositionData(index)?.observe(viewLifecycleOwner, Observer { position ->
            adapter.setLastSelectedIndex(position)

            val sb = StringBuilder()
            filterDataList.forEachIndexed { index, list ->
                val lastPosition = viewModel.filterPositionData(index)?.value
                lastPosition?.takeIf { it < list.size }?.let { list[it] }?.also {
                    if (index == 0) {
                        sb.append(it)
                    } else {
                        sb.append(", ").append(it)
                    }
                }
            }.takeIf { sb.isNotEmpty() }?.run { tv_collapsing_filter.text = sb.toString() }
        })
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
        Timber.e("setCollapsingFilterBar : $visibility")
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
}