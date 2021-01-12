package com.dabenxiang.mimi.view.ranking

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.RankingFuncItem
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clipsingle.ClipSingleFragment
import com.dabenxiang.mimi.view.club.pic.ClubPicFragment
import com.dabenxiang.mimi.view.my_pages.pages.mimi_video.CollectionFuncItem
import com.dabenxiang.mimi.view.player.ui.PlayerV2Fragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_picture_detail.toolbarContainer
import kotlinx.android.synthetic.main.fragment_ranking.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*
import timber.log.Timber

class RankingFragment : BaseFragment() {

    companion object {
        fun createBundle(): Bundle {
            return Bundle()
        }
    }

    private val viewModel: RankingViewModel by viewModels()

    private val adapter by lazy {
        RankingAdapter(requireActivity(),
            RankingFuncItem(
                onItemClick = { items, position ->
                    val item = items[position]
                    when(PostType.getTypeByValue(item.type)){
                        PostType.VIDEO_ON_DEMAND -> {
                            val playerData = PlayerItem(item.id)
                            val bundle = PlayerV2Fragment.createBundle(playerData)
                            navigateTo(
                                NavigateItem.Destination(
                                    R.id.action_rankingFragment_to_navigation_player,
                                    bundle
                                )
                            )
                        }
                        PostType.SMALL_CLIP -> {
                            viewModel.getInteractiveHistory(item)
                        }
                        PostType.IMAGE -> {
                            viewModel.getPostDetail(item.id)
                        }
                    }
                },
                getBitmap = { id, view ->
                    viewModel.loadImage(
                        id,
                        view,
                        LoadImageType.PICTURE_THUMBNAIL
                    )
                }
            ),
            CollectionFuncItem(
                { source -> viewModel.getDecryptSetting(source) },
                { videoItem, decryptSettingItem, function ->
                    viewModel.decryptCover(
                        videoItem,
                        decryptSettingItem,
                        function
                    )
                }
            )
        )
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        text_toolbar_title.text = getString(R.string.text_ranking)
        toolbarContainer.toolbar.navigationIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.btn_back_black_n)
        toolbarContainer.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        layout_refresh.setColorSchemeColors(requireContext().getColor(R.color.color_red_1))
        setupAdapter()
    }

    override fun setupFirstTime() {
        super.setupFirstTime()

        val tabs = resources.getStringArray(R.array.ranking_tabs)
        for (i in 0 until tab_temporal_filter.tabCount) {
            val tab = tab_temporal_filter.getTabAt(i)
            val tabView = View.inflate(requireContext(), R.layout.custom_tab, null)
            val textView = tabView?.findViewById<TextView>(R.id.tv_title)
            textView?.text = tabs[i]
            tab?.customView = tabView
        }
        tab_temporal_filter.getTabAt(0)?.select()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_ranking
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel.showProgress.observe(this, {
            layout_refresh.isRefreshing = it
        })

        viewModel.rankingList.observe(this, {
            adapter.submitList(it)
        })

        viewModel.postDetail.observe(this, {
            when(it) {
                is ApiResult.Loading -> layout_refresh.isRefreshing = true
                is ApiResult.Loaded ->  layout_refresh.isRefreshing = false
                is ApiResult.Success -> {
                    val bundle = ClubPicFragment.createBundle(it.result)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_to_clubPicFragment,
                            bundle
                        )
                    )
                }
            }
        })

        viewModel.clipInteractiveHistory.observe(this, {
            when(it){
                is ApiResult.Success -> {
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_to_clipSingleFragment,
                            ClipSingleFragment.createBundle(it.result)
                        ))
                }
            }
        })
    }

    private fun setupAdapter() {
        Timber.i("post type=${tab_type_filter.selectedTabPosition} ")
        rv_ranking_content.adapter =adapter
        viewModel.setStatisticsTypeFunction(tab_temporal_filter.selectedTabPosition)
        viewModel.setPostType(tab_type_filter.selectedTabPosition)
        getRankingList()
    }

    override fun setupListeners() {
        requireActivity().onBackPressedDispatcher.addCallback(
            owner = viewLifecycleOwner,
            onBackPressed = { navigateTo(NavigateItem.Up) }
        )

        tab_temporal_filter.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.setStatisticsTypeFunction(tab.position)
                getRankingList()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        tab_type_filter.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.setPostType(tab.position)
                getRankingList()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = true
            getRankingList()
        }
    }

    fun getRankingList() {
        adapter.submitList(null)
        viewModel.getRankingList()
    }
}