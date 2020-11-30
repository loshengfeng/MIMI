package com.dabenxiang.mimi.view.actorvideos


import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.generalvideo.GeneralVideoAdapter
import com.dabenxiang.mimi.view.generalvideo.paging.VideoLoadStateAdapter
import com.dabenxiang.mimi.view.player.ui.PlayerV2Fragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.view.GridSpaceItemDecoration
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_actor_videos.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs


class ActorVideosFragment : BaseFragment() {
    companion object {
        const val KEY_DATA = "data"

        fun createBundle(
                id: Long = 0L
        ): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, id)
            }
        }
    }

    private val generalVideoAdapter by lazy {
        GeneralVideoAdapter(false, onItemClick)
    }

    private val onItemClick: (StatisticsItem) -> Unit = {
        navToPlayer(PlayerItem(it.id))
    }

    private val viewModel: ActorVideosViewModel by viewModels()

    private var actorName: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_actor_videos
    }

    override fun setupFirstTime() {
        super.setupFirstTime()
    }

    override fun initSettings() {
        super.initSettings()
//        tv_title.text = getString(R.string.actor_videos_title)
        actor_toolbar_title.text = getString(R.string.actor_videos_title)
            arguments?.getSerializable(KEY_DATA)?.let { id ->
                id as Long
                viewModel.getActorVideosById(id)
            }

        generalVideoAdapter.addLoadStateListener(loadStateListener)

        val loadStateAdapter = VideoLoadStateAdapter(generalVideoAdapter)

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
            .also { it.spanSizeLookup = gridLayoutSpanSizeLookup }

        rv_video.also {
            it.layoutManager = gridLayoutManager
            it.setHasFixedSize(true)
            it.adapter = generalVideoAdapter.withLoadStateFooter(loadStateAdapter)
            it.addItemDecoration(
                    GridSpaceItemDecoration(
                            2,
                            GeneralUtils.dpToPx(requireContext(), 10),
                            GeneralUtils.dpToPx(requireContext(), 20),
                            false
                    )
            )
        }
    }

    override fun setupObservers() {
        viewModel.actorVideosByIdResult.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResult.Success -> {
                    val item = it.result
                    tv_name.text = item.name
                    tv_total_click.text = item.totalClick.toString() + getString(R.string.actor_hot_unit)
                    tv_total_video.text = item.totalVideo.toString() + getString(R.string.actor_videos_unit)
                    viewModel.loadImage(item.attachmentId, iv_avatar, LoadImageType.AVATAR_CS)
                    viewModel.loadImage(item.attachmentId, actor_toolbar_avatar, LoadImageType.AVATAR_CS)
                    actorName = item.name
                    getVideoData(actorName)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

    }

    override fun setupListeners() {
        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            if(actorName != "")
                getVideoData(actorName)
            else
                arguments?.getSerializable(KEY_DATA)?.let { id ->
                    id as Long
                    viewModel.getActorVideosById(id)
                }
        }
        actor_toolbar.setNavigationOnClickListener {
            navigateTo(NavigateItem.Up)
        }

        app_bar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->

            when {
                verticalOffset == 0 -> {
                    actor_toolbar_avatar.visibility = View.GONE
                    actor_toolbar_title.text = getString(R.string.actor_videos_title)
                }

                abs(verticalOffset) >= appBarLayout.totalScrollRange -> {
                    actor_toolbar_avatar.visibility =View.VISIBLE
                    actor_toolbar_title.text = actorName
                }
            }

        })
    }

    private val loadStateListener = { loadStatus: CombinedLoadStates ->
        when (loadStatus.refresh) {
            is LoadState.Error -> {
                Timber.e("Refresh Error: ${(loadStatus.refresh as LoadState.Error).error.localizedMessage}")
                onApiError((loadStatus.refresh as LoadState.Error).error)

                layout_empty_data?.run { this.visibility = View.VISIBLE }
                tv_empty_data?.run { this.text = getString(R.string.error_video) }
                rv_video?.run { this.visibility = View.INVISIBLE }
                layout_refresh?.run { this.isRefreshing = false }
            }
            is LoadState.Loading -> {
                layout_empty_data?.run { this.visibility = View.VISIBLE }
                tv_empty_data?.run { this.text = getString(R.string.load_video) }
                rv_video?.run { this.visibility = View.INVISIBLE }
                layout_refresh?.run { this.isRefreshing = true }
            }
            is LoadState.NotLoading -> {
                if (generalVideoAdapter.isDataEmpty()) {
                    layout_empty_data?.run { this.visibility = View.VISIBLE }
                    tv_empty_data?.run { this.text = getString(R.string.empty_video) }
                    rv_video?.run { this.visibility = View.INVISIBLE }
                } else {
                    layout_empty_data?.run { this.visibility = View.INVISIBLE }
                    rv_video?.run { this.visibility = View.VISIBLE }
                }

                layout_refresh?.run { this.isRefreshing = false }
            }
        }

        when (loadStatus.append) {
            is LoadState.Error -> {
                Timber.e("Append Error:${(loadStatus.append as LoadState.Error).error.localizedMessage}")
            }
            is LoadState.Loading -> {
                Timber.d("Append Loading endOfPaginationReached:${(loadStatus.append as LoadState.Loading).endOfPaginationReached}")
            }
            is LoadState.NotLoading -> {
                Timber.d("Append NotLoading endOfPaginationReached:${(loadStatus.append as LoadState.NotLoading).endOfPaginationReached}")
            }
        }
    }

    private val gridLayoutSpanSizeLookup =
        object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (generalVideoAdapter.getItemViewType(position)) {
                    GeneralVideoAdapter.VIEW_TYPE_VIDEO -> 1
                    else -> 2
                }
            }
        }

    private fun getVideoData(actorName: String){
        lifecycleScope.launch {
            viewModel.getVideoByCategory(actorName)
                .collectLatest {
                    generalVideoAdapter.submitData(it)
                }
        }
    }

    private fun navToPlayer(item: PlayerItem){
        val bundle = PlayerV2Fragment.createBundle(item)
        navigateTo(
                NavigateItem.Destination(
                        R.id.action_to_navigation_player,
                        bundle
                )
        )
    }
}