package com.dabenxiang.mimi.view.player.ui

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.extension.setNot
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.api.vo.VideoEpisodeItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.vo.BaseVideoItem
import com.dabenxiang.mimi.view.adapter.TopTabAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.player.GuessLikeVideoAdapter
import com.dabenxiang.mimi.view.player.GuessLikeVideoAdapter.OnGarbageItemClick
import com.dabenxiang.mimi.view.player.PlayerViewModel
import com.dabenxiang.mimi.view.player.SelectEpisodeAdapter
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.head_guess_like.*
import kotlinx.android.synthetic.main.head_source.*
import kotlinx.android.synthetic.main.head_video_info.*
import kotlinx.android.synthetic.main.item_ad.*
import kotlinx.android.synthetic.main.item_video_tag.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class PlayerDescriptionFragment : BaseFragment() {

    private val viewModel: PlayerV2ViewModel by activityViewModels()

    private val descriptionViewModel = PlayerDescriptionViewModel()

    private val sourceListAdapter by lazy {
        TopTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                Timber.i("TopTabAdapter onClickItemIndex")
                viewModel.selectSourcesIndex(index)
            }
        })
    }

    private val episodeAdapter by lazy {
        SelectEpisodeAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                Timber.i("SelectEpisodeAdapter onClickItemIndex $index")
                viewModel.selectStreamSourceIndex(index)
            }
        })
    }

    private val guessLikeAdapter by lazy {
        GuessLikeVideoAdapter(object : OnGarbageItemClick {

            override fun onStatisticsDetail(baseVideoItem: BaseVideoItem) {

            }

            override fun onTagClick(tag: String) {

            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // for ui init
        val adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        val adHeight = (adWidth * 0.142).toInt()
        descriptionViewModel.getAd(adWidth, adHeight)
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

        viewModel.episodeContentSource.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Success -> {
                    updateStreamInfo(it.result)
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

        descriptionViewModel.getAdResult.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Success -> {
                    Glide.with(this)
                        .load(it.result.href)
                        .into(iv_ad)

                    iv_ad.setOnClickListener { view ->
                        GeneralUtils.openWebView(requireContext(), it.result.target)
                    }
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }

        descriptionViewModel.videoList.observe(viewLifecycleOwner) {
            guessLikeAdapter.submitList(it)
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

        recyclerview_source_list.adapter = sourceListAdapter
        recyclerview_episode.adapter = episodeAdapter

        recyclerview_guess_like.adapter = guessLikeAdapter

        val categoriesString =
            if (videoItem.categories.isNotEmpty()) videoItem.categories.last() else ""
        descriptionViewModel.setupGuessLikeList(categoriesString, true)

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

        if (videoItem.tags != null)
            setupChipGroup(videoItem.tags as List<String>)

        if(videoItem.sources == null || videoItem.sources.isEmpty()) {
            recyclerview_source_list.visibility = View.GONE
        } else {
            if(videoItem.sources.size == 1) recyclerview_source_list.visibility = View.GONE

            val result = mutableListOf<String>()
            for (item in videoItem.sources) {
                item.name?.also {
                    result.add(it)
                }
            }
            Timber.i("setupSourceList =${result[0]}")
            sourceListAdapter.submitList(result, 0)
        }
    }

    private fun updateStreamInfo(videoEpisodeItem: VideoEpisodeItem) {
        val result = mutableListOf<String>()
        if(videoEpisodeItem.videoStreams != null
            && videoEpisodeItem.videoStreams.isNotEmpty()) {
            for (item in videoEpisodeItem.videoStreams) {
                Timber.i("videoStreams =${item.id}")
                if (item.id != null) {
                    result.add(item.streamName ?: "")
                }
            }
        }
        episodeAdapter.submitList(result, 0)
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
                val bundle = SearchVideoFragment.createBundle(
                    tag = chip.text.toString()
                )
                bundle.putBoolean(PlayerFragment.KEY_IS_FROM_PLAYER, true)
                findNavController().navigate(
                    R.id.action_playerFragment_to_searchVideoFragment,
                    bundle
                )
            }

            tag_group.addView(chip)
        }
    }
}