package com.dabenxiang.mimi.view.player.ui

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.extension.setNot
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.head_video_info.*
import java.text.SimpleDateFormat
import java.util.*

class PlayerDescriptionFragment : BaseFragment() {

    private val viewModel: PlayerV2ViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_player_description
    }

    override fun setupObservers() {
        viewModel.videoContentSource.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Success -> {
                    setUI(it.result)
                }
            }
        }

        viewModel.showIntroduction.observe(viewLifecycleOwner) { isShow ->
            val drawableRes =
                if (isShow) R.drawable.btn_arrowup_gray_n
                else R.drawable.btn_arrowdown_gray_n
            tv_introduction.visibility = if (isShow) View.VISIBLE else View.GONE
            btn_show_introduction.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                drawableRes,
                0
            )
        }
    }

    override fun setupListeners() {

        btn_show_introduction.setOnClickListener {
            viewModel.showIntroduction.setNot()
        }

    }

    private fun setUI(videoItem: VideoItem) {
        val subTitleColor = requireContext().getColor(R.color.color_black_1_50)

        btn_show_introduction.setTextColor(subTitleColor)
        tv_introduction.setTextColor(subTitleColor)
        tv_info.setTextColor(subTitleColor)
        tv_introduction.setBackgroundResource(R.drawable.bg_black_stroke_1_radius_2)

        val dateString = videoItem.updateTime?.let { date ->
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        }

        tv_title.text = videoItem.title
        tv_info.text = String.format(
            getString(R.string.player_info_format),
            dateString ?: "",
            videoItem.country
        )
        if(!videoItem.description.isNullOrEmpty()) {
            tv_introduction.text =
                Html.fromHtml(videoItem.description, Html.FROM_HTML_MODE_COMPACT)
        }
    }
}