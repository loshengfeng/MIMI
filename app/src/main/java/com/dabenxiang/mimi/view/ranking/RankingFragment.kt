package com.dabenxiang.mimi.view.ranking

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.RankingFuncItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.adapter.RankingAdapter
import com.dabenxiang.mimi.view.adapter.RankingVideosAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_picture_detail.toolbarContainer
import kotlinx.android.synthetic.main.fragment_ranking.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*
import kotlinx.coroutines.delay
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
                        PlayerItem(it.id!!, true)
                    val intent = Intent(requireContext(), PlayerActivity::class.java)
                    intent.putExtras(PlayerActivity.createBundle(playerData))
                    startActivityForResult(intent, REQUEST_LOGIN)
                },
                getBitmap = { id, view -> viewModel.loadImage(id, view, LoadImageType.PICTURE_THUMBNAIL) }
            )
        )
    }

    private val adapter by lazy {
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
                        PostType.VIDEO -> {
                            val bundle = ClipFragment.createBundle(
                                memberPostItems, position, false
                            )
                            navigateTo(
                                NavigateItem.Destination(
                                    R.id.action_rankingFragment_to_clipFragment,
                                    bundle
                                )
                            )
                        }
                        PostType.IMAGE -> {
                            val bundle = PictureDetailFragment.createBundle(
                                memberPostItems[position], 0
                            )
                            navigateTo(
                                NavigateItem.Destination(
                                    R.id.action_rankingFragment_to_pictureDetailFragment,
                                    bundle
                                )
                            )
                        }
                    }

                },
                getBitmap = { id, view -> viewModel.loadImage(id, view, LoadImageType.PICTURE_THUMBNAIL) }
            )
        )
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback { navigateTo(NavigateItem.Up) }

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

        viewModel.rankingVideosList.observe(viewLifecycleOwner, Observer {
            videosAdapter.submitList(it)
            lifecycleScope.launch {
                delay(500)
                layout_refresh?.isRefreshing = false
            }
        })

        viewModel.rankingList.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            lifecycleScope.launch {
                delay(500)
                layout_refresh?.isRefreshing = false
            }
        })
    }

    private fun setupAdapter() {
        Timber.i("post type=${tab_type_filter.selectedTabPosition} ")
        layout_refresh.isRefreshing = true
        rv_ranking_content.adapter =
            if (tab_type_filter.selectedTabPosition == 0) videosAdapter else adapter
        viewModel.setStatisticsTypeFunction(tab_temporal_filter.selectedTabPosition)
        viewModel.setPostType(tab_type_filter.selectedTabPosition)
        viewModel.getRankingList()
    }

    override fun setupListeners() {

        tab_temporal_filter.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                layout_refresh.isRefreshing = true
                viewModel.setStatisticsTypeFunction(tab.position)
                viewModel.getRankingList()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        tab_type_filter.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                layout_refresh.isRefreshing = true
                rv_ranking_content.adapter = if (tab.position == 0) videosAdapter else adapter
                viewModel.setPostType(tab.position)
                viewModel.getRankingList()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.getRankingList()
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
}