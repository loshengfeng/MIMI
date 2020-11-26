package com.dabenxiang.mimi.view.actorvideos

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ActorCategoriesItem
import com.dabenxiang.mimi.model.api.vo.ActorVideosItem
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.generalvideo.GeneralVideoAdapter
import com.dabenxiang.mimi.view.generalvideo.paging.VideoLoadStateAdapter
import kotlinx.android.synthetic.main.fragment_club_recommend.*
import kotlinx.android.synthetic.main.fragment_general_video.*
import kotlinx.android.synthetic.main.fragment_general_video.layout_refresh
import kotlinx.android.synthetic.main.item_setting_bar.*
import kotlinx.android.synthetic.main.item_actor_videos.iv_avatar
import kotlinx.android.synthetic.main.item_actor_videos.tv_name
import kotlinx.android.synthetic.main.item_actor_videos.tv_total_click
import kotlinx.android.synthetic.main.item_actor_videos.tv_total_video
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

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
        GeneralVideoAdapter(onItemClick)
    }

    private val onItemClick: (StatisticsItem) -> Unit = {
        // TODO: 跳至播放頁面
        Timber.d("VideoItem Id: ${it.id}")
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
        tv_title.text = getString(R.string.actor_videos_title)
            arguments?.getSerializable(KEY_DATA)?.let {id ->
                id as Long
                viewModel.getActorVideosById(id)
            }

        val loadStateAdapter = VideoLoadStateAdapter(generalVideoAdapter)

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
            .also { it.spanSizeLookup = gridLayoutSpanSizeLookup }

        rv_video.also {
            it.layoutManager = gridLayoutManager
            it.setHasFixedSize(true)
            it.adapter = generalVideoAdapter.withLoadStateFooter(loadStateAdapter)
        }

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            if(actorName != "")
                getVideoData(actorName)
            else
                arguments?.getSerializable(KEY_DATA)?.let {id ->
                    id as Long
                    viewModel.getActorVideosById(id)
                }
        }
    }

    override fun setupObservers() {
        viewModel.actorVideosByIdResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loaded -> ""
                is ApiResult.Success -> {
                    val item = it.result
                    tv_name.text = item.name
                    tv_total_click.text = item.totalClick.toString() + getString(R.string.actor_hot_unit)
                    tv_total_video.text = item.totalVideo.toString() + getString(R.string.actor_videos_unit)
                    viewModel.loadImage(item.attachmentId, iv_avatar, LoadImageType.AVATAR_CS)
                    actorName = item.name
                    getVideoData(actorName)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

    }

    override fun setupListeners() {
        tv_back.setOnClickListener {
            navigateTo(NavigateItem.Up)
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
                    layout_refresh.isRefreshing = false
                    generalVideoAdapter.submitData(it)
                }
        }
    }

}