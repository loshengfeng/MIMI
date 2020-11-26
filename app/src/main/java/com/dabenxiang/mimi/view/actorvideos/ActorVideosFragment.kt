package com.dabenxiang.mimi.view.actorvideos

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ActorCategoriesItem
import com.dabenxiang.mimi.model.api.vo.ActorVideosItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import kotlinx.android.synthetic.main.item_setting_bar.*
import kotlinx.android.synthetic.main.item_actor_videos.iv_avatar
import kotlinx.android.synthetic.main.item_actor_videos.tv_name
import kotlinx.android.synthetic.main.item_actor_videos.tv_total_click
import kotlinx.android.synthetic.main.item_actor_videos.tv_total_video

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

    private val viewModel: ActorVideosViewModel by viewModels()

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

}