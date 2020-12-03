package com.dabenxiang.mimi.view.ranking

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.RankingFuncItem
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.api.vo.error.SUCCESS
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clipsingle.ClipSingleFragment
import com.dabenxiang.mimi.view.club.pic.ClubPicFragment
import com.dabenxiang.mimi.view.my_pages.pages.mimi_video.CollectionFuncItem
import com.dabenxiang.mimi.view.player.ui.ClipPlayerFragment
import com.dabenxiang.mimi.view.player.ui.PlayerV2Fragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_picture_detail.toolbarContainer
import kotlinx.android.synthetic.main.fragment_ranking.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class RankingFragment : BaseFragment() {

    companion object {
        private const val REQUEST_LOGIN = 1000

        fun createBundle(): Bundle {
            return Bundle().also {
            }
        }
    }

    private val viewModel: RankingViewModel by viewModels()

    private val videosAdapter by lazy {
        RankingVideosAdapter(requireActivity(),
            RankingFuncItem(
                onVideoItemClick = {
                    val playerData =
                        PlayerItem(it.id)
                    val bundle = PlayerV2Fragment.createBundle(playerData)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_rankingFragment_to_navigation_player,
                            bundle
                        )
                    )
                },
                getBitmap = { id, view ->
                    viewModel.loadImage(
                        id,
                        view,
                        LoadImageType.PICTURE_THUMBNAIL
                    )
                }
            )
        )
    }

    private val clipAdapter by lazy {
        RankingClipAdapter(
            requireContext(),
            RankingFuncItem(
                onClipItemClick = { item ->
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_to_clipSingleFragment,
                            ClipSingleFragment.createBundle(item)
                        )
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

    private val pictureAdapter by lazy {
        RankingAdapter(requireActivity(),
            RankingFuncItem(
                onItemClick = { items, position ->
                    val memberPostItems = items.mapNotNull { it.detail }
                        .let {
                            arrayListOf<MemberPostItem>().also { arrayList ->
                                arrayList.addAll(it)
                            }
                        }

                    when (viewModel.postTypeSelected) {
                        PostType.IMAGE -> {
                            val bundle = ClubPicFragment.createBundle(memberPostItems[position])
                            navigateTo(
                                NavigateItem.Destination(
                                    R.id.action_to_clubPicFragment,
                                    bundle
                                )
                            )
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
            )
        )
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        text_toolbar_title.text = getString(R.string.text_ranking)
        toolbarContainer.toolbar.navigationIcon =
            requireContext().getDrawable(R.drawable.btn_back_black_n)
        toolbarContainer.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        layout_refresh.setColorSchemeColors(requireContext().getColor(R.color.color_red_1))
        setupAdapter()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_ranking
    }

    override fun setupObservers() {
        viewModel.showProgress.observe(viewLifecycleOwner, Observer {
            layout_refresh.isRefreshing = it
        })

        viewModel.rankingVideosList.observe(viewLifecycleOwner, Observer {
            videosAdapter.submitList(it)
        })

        viewModel.rankingList.observe(viewLifecycleOwner, Observer {
            pictureAdapter.submitList(it)
        })

        viewModel.rankingClipList.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> layout_refresh.isRefreshing = true
                is ApiResult.Loaded -> layout_refresh.isRefreshing = false
                is ApiResult.Success -> clipAdapter.updateData(it.result as ArrayList<VideoItem>)
                is ApiResult.Error -> onApiError(it.throwable)
                else -> {
                }
            }
        })
    }

    private fun setupAdapter() {
        Timber.i("post type=${tab_type_filter.selectedTabPosition} ")
        rv_ranking_content.adapter =
            when (tab_type_filter.selectedTabPosition) {
                0 -> videosAdapter
                1 -> clipAdapter
                else -> pictureAdapter
            }
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
                rv_ranking_content.adapter =
                    when (tab.position) {
                        0 -> videosAdapter
                        1 -> clipAdapter
                        else -> pictureAdapter
                    }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_LOGIN -> {
                    findNavController().navigate(
                        R.id.action_rankingFragment_to_loginFragment,
                        data?.extras
                    )
                }
            }
        }
    }

    fun getRankingList() {
        when (viewModel.postTypeSelected) {
            PostType.VIDEO_ON_DEMAND -> viewModel.getVideosRanking()
            PostType.VIDEO -> viewModel.getRankingClipList()
            PostType.IMAGE -> viewModel.getRankingPostList()
        }
    }
}