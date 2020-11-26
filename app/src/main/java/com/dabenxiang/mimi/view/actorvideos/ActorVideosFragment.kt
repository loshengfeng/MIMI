package com.dabenxiang.mimi.view.actorvideos

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
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
            item: ActorVideosItem? = null
        ): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    private val viewModel: ActorVideosViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
        arguments?.getSerializable(KEY_DATA)?.let { item ->
            item as ActorVideosItem
            tv_name.text = item.name
            tv_total_click.text = item.totalClick.toString() + getString(R.string.actor_hot_unit)
            tv_total_video.text = item.totalVideo.toString() + getString(R.string.actor_videos_unit)
            viewModel.loadImage(item.attachmentId, iv_avatar, LoadImageType.AVATAR_CS)
        }
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
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {
        tv_back.setOnClickListener {
            navigateTo(NavigateItem.Up)
        }
    }

}