package com.dabenxiang.mimi.view.player.ui

import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.extension.throttleFirst
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.item_clip_info.*
import kotlinx.android.synthetic.main.item_video_tag.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.*

class ClipPlayerDescriptionFragment : BaseFragment() {

    override fun getLayoutId() = R.layout.fragment_clip_description

    private val viewModel: ClipPlayerViewModel by activityViewModels()

    private val clipViewModel = ClipPlayerDescriptionViewModel()

    override fun setupObservers() {
        super.setupObservers()
        viewModel.memberPostContentSource.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Loading -> progressHUD.show()
                is ApiResult.Success -> {
                    parsingPostContent(it.result)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }
    }

    override fun setupListeners() {
        super.setupListeners()
        tv_follow.setOnClickListener {
            GlobalScope.launch {
                flow {
                    clipViewModel.followPost(viewModel.videoContentId)
                    (it as TextView).text.let { text ->
                        updataFollow(text != getText(R.string.follow))
                    }
                    emit(null)
                }
                    .throttleFirst(500)
            }
        }
    }

    private fun parsingPostContent(postItem: MemberPostItem) {
        viewModel.loadImage(postItem.avatarAttachmentId, clip_icon, LoadImageType.AVATAR_CS)
        clip_name.text = postItem.postFriendlyName

        tv_follow.visibility =
            if (viewModel.accountManager.getProfile().userId == postItem.creatorId) View.GONE else View.VISIBLE

        updataFollow(postItem.isFollow)

        clip_update_time.setTextColor(App.self.getColor(R.color.color_black_1_50))
        clip_update_time.text = GeneralUtils.getTimeDiff(postItem.creationDate, Date())

        clip_title.setTextColor(App.self.getColor(R.color.color_black_1))
        clip_title.text = postItem.title

        setupChipGroup(postItem.tags)
    }

    private fun updataFollow(isFollow: Boolean) {
        tv_follow.setText(if (isFollow) R.string.followed else R.string.follow)
        tv_follow.setBackgroundResource(if (isFollow) R.drawable.bg_white_1_stroke_radius_16 else R.drawable.bg_red_1_stroke_radius_16)
        tv_follow.setTextColor(App.self.getColor(if (isFollow) R.color.color_black_1_60 else R.color.color_red_1))
    }

    private fun setupChipGroup(list: List<String>?) {
        tag_group.removeAllViews()

        if (list == null) {
            return
        }

        list.indices.mapNotNull {
            list[it]
        }.forEach {
            val chip = layoutInflater.inflate(
                R.layout.chip_item,
                tag_group,
                false
            ) as Chip
            chip.text = it

            chip.setTextColor(requireContext().getColor(R.color.color_black_1_50))

            chip.setOnClickListener {
                searchVideo(chip.text.toString())
            }

            tag_group.addView(chip)
        }
    }

    private fun searchVideo(tag: String) {
        val bundle = SearchVideoFragment.createBundle(
            tag = tag
        )
        bundle.putBoolean(PlayerFragment.KEY_IS_FROM_PLAYER, true)
        findNavController().navigate(
            R.id.action_playerFragment_to_searchVideoFragment,
            bundle
        )
    }
}