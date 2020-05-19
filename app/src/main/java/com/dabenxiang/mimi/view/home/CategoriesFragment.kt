package com.dabenxiang.mimi.view.home

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.serializable.CategoriesData
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.adapter.FilterTabAdapter
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.adapter.HomeCategoriesAdapter
import com.dabenxiang.mimi.view.adapter.HomeVideoListAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.player.PlayerActivity
import kotlinx.android.synthetic.main.fragment_categories.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class CategoriesFragment : BaseFragment<CategoriesViewModel>() {

    companion object {
        const val KEY_DATA = "data"

        fun createBundle(title: String, categories: String): Bundle {
            val data = CategoriesData()
            data.title = title
            data.categories = categories

            return Bundle().also {
                it.putSerializable(KEY_DATA, data)
            }
        }
    }

    private val viewModel by viewModel<CategoriesViewModel>()

    private val videoListAdapter by lazy {
        HomeVideoListAdapter(adapterListener, true)
    }

    private val adapterListener = object : HomeAdapter.EventListener {
        override fun onHeaderItemClick(view: View, item: HomeTemplate.Header) {
        }

        override fun onVideoClick(view: View, item: PlayerData) {
            Timber.d("$item")

            val intent = Intent(activity!!, PlayerActivity::class.java)
            intent.putExtras(PlayerActivity.createBundle(item))
            startActivity(intent)
        }

        override fun onLoadAdapter(adapter: HomeCategoriesAdapter, src: HomeTemplate.Categories) {

        }
    }

    private val adapterFilter = object : FilterTabAdapter.FilterTabAdapterListener {
        override fun onSelectedFilterTab(recyclerView: RecyclerView, position: Int) {
            when (recyclerView) {
                filter_0 -> {
                    viewModel.updatedFilterPosition(0, position)
                }
                filter_1 -> {
                    viewModel.updatedFilterPosition(1, position)
                }
                filter_2 -> {
                    viewModel.updatedFilterPosition(2, position)
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_categories
    }

    override fun fetchViewModel(): CategoriesViewModel? {
        return viewModel
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (arguments?.getSerializable(KEY_DATA) as CategoriesData?)?.also { data ->
            tv_title.text = data.title

            val isAdult = mainViewModel?.adultMode?.value ?: false

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

            iv_back.setImageResource(
                if (isAdult) {
                    R.drawable.ic_adult_btn_back
                } else {
                    R.drawable.ic_normal_btn_back
                }
            )

            iv_search.setImageResource(
                if (isAdult) {
                    R.drawable.ic_adult_btn_search
                } else {
                    R.drawable.ic_normal_btn_search
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

            viewModel.videoList.observe(viewLifecycleOwner, Observer {
                videoListAdapter.submitList(it)
            })

            loadCategories(data.categories, isAdult)
        }
    }

    private fun loadCategories(keyword: String?, isAdult: Boolean) {
        viewModel.setupVideoList(keyword, isAdult)
    }

    override fun setupObservers() {
        val isAdult = mainViewModel?.adultMode?.value ?: false

        val filterViewList = listOf(filter_0, filter_1, filter_2)
        val filterAdapterList = mutableMapOf<Int, FilterTabAdapter>()

        mainViewModel?.categoriesData?.observe(viewLifecycleOwner, Observer { categories ->

            // TODO: Fake data
            repeat(3) { i ->
                val years = mutableListOf<String>()
                repeat(8) { y ->
                    years.add("${2020 - y}")
                }

                val adapter = FilterTabAdapter(adapterFilter, isAdult)
                adapter.setTabList(years, 0)

                filterViewList[i].adapter = adapter
                filterAdapterList[i] = adapter

                // TODO: Observer last position
                viewModel.filterPositionData(i)?.observe(viewLifecycleOwner, Observer { position ->
                    adapter.setLastSelectedPosition(position)

                    val sb = StringBuilder()
                    var isFirst = true
                    for (j in 0..3) {
                        val lastPosition = viewModel.filterPositionData(j)?.value
                        if (lastPosition != null) {
                            if (isFirst) {
                                isFirst = false
                            } else {
                                sb.append(", ")
                            }

                            sb.append(years[lastPosition])
                        }
                    }

                    tv_collapsing_filter.text = sb.toString()
                })
            }
        })
    }

    override fun setupListeners() {
        iv_back.setOnClickListener {
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

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val filterTranslationY = bar_filter.translationY
            when {
                filterTranslationY > 0 -> bar_filter.translationY = 0f
                filterTranslationY <= -bar_filter.height.toFloat() && dy > 0 -> bar_filter.translationY = -bar_filter.height.toFloat()
                filterTranslationY <= 0 -> {
                    bar_filter.translationY = if (bar_filter.translationY - dy > 0f) {
                        0f
                    } else {
                        bar_filter.translationY - dy
                    }
                }
            }

            when {
                filterTranslationY <= -bar_filter.height.toFloat() && dy > 0 -> setCollapsingFilterBar(View.VISIBLE)
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