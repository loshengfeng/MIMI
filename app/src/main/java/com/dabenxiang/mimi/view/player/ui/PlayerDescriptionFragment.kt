package com.dabenxiang.mimi.view.player.ui

import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.extension.setNot
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.VideoEpisodeItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.BaseVideoItem
import com.dabenxiang.mimi.view.adapter.TopTabAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.dialog.ReportDialogFragment
import com.dabenxiang.mimi.view.player.GuessLikeVideoAdapter
import com.dabenxiang.mimi.view.player.GuessLikeVideoAdapter.OnGarbageItemClick
import com.dabenxiang.mimi.view.player.SelectEpisodeAdapter
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_player_description.*
import kotlinx.android.synthetic.main.head_guess_like.*
import kotlinx.android.synthetic.main.head_source.*
import kotlinx.android.synthetic.main.head_video_info.*
import kotlinx.android.synthetic.main.item_ad.*
import kotlinx.android.synthetic.main.item_comment_interactive.*
import kotlinx.android.synthetic.main.item_video_tag.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class PlayerDescriptionFragment : BaseFragment() {

    private val viewModel: PlayerV2ViewModel by viewModels({requireParentFragment()})

    private val descriptionViewModel = PlayerDescriptionViewModel()

    override fun getLayoutId(): Int {
        return R.layout.fragment_player_description
    }

    override val isStatusBarDark: Boolean = true

    override val bottomNavigationVisibility: Int
        get() = View.GONE

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
                viewModel.stopVideoPlayer.setNot()
                viewModel.isResetPlayer = true
                if(viewModel.selectEpisodePosition.value != 0) viewModel.selectStreamSourceIndex(0)
                viewModel.videoContentId = (baseVideoItem as BaseVideoItem.Video).id!!
                viewModel.getVideoContent()
                nestedScrollView.smoothScrollTo(0, 0)
            }

            override fun onTagClick(tag: String) {
                searchVideo(tag)
            }

        })
    }

    private var streamId: Long = 0
    private var isReported: Boolean = false
    private lateinit var videoItem: VideoItem

    private var moreDialog: MoreDialogFragment? = null
    private var reportDialog: ReportDialogFragment? = null

    private val onReportDialogListener = object : ReportDialogFragment.OnReportDialogListener {
        override fun onSend(item: BaseMemberPostItem, content: String, postItem: MemberPostItem?) {
            if (TextUtils.isEmpty(content)) {
                GeneralUtils.showToast(App.applicationContext(), getString(R.string.report_error))
            } else {
                descriptionViewModel.report(streamId, content)
            }
            reportDialog?.dismiss()
        }

        override fun onCancel() {
            reportDialog?.dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // for ui init
        val adWidth = GeneralUtils.getAdSize(requireActivity()).first
        val adHeight = GeneralUtils.getAdSize(requireActivity()).second
        descriptionViewModel.getAd(adWidth, adHeight)
    }

    override fun setupObservers() {
        descriptionViewModel.videoChangedResult.observe(viewLifecycleOwner){
            when (it) {
                is ApiResult.Success -> {
                    mainViewModel?.videoItemChangedList?.value?.set(it.result.id, it.result)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }

        viewModel.videoContentSource.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Success -> {
                    videoItem = it.result
                    setUI()
                }
            }
        }

        viewModel.episodeContentSource.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Success -> {
                    updateStreamInfo(it.result)
                    streamId = it.result.videoStreams?.get(0)?.id ?: 0L
                    isReported = it.result.videoStreams?.get(0)?.reported ?: false
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
                else -> {
                }
            }
        }

        descriptionViewModel.videoList.observe(viewLifecycleOwner) {
            guessLikeAdapter.submitList(it)
        }

        viewModel.selectSourcesPosition.observe(viewLifecycleOwner) {
            sourceListAdapter.setLastSelectedIndex(it)
            Timber.w("Select source index $it")
            episodeAdapter.setLastSelectedIndex(0)
        }

        viewModel.selectEpisodePosition.observe(viewLifecycleOwner) {
            Timber.w("Select index is $it and old index is ${episodeAdapter.getSelectedPosition()}")
            if (it != episodeAdapter.getSelectedPosition()) {
                isReported = false
                episodeAdapter.setLastSelectedIndex(it)
                viewModel.getVideoContent()
            }
        }

        descriptionViewModel.reportResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.also { apiResult ->
                when (apiResult) {
                    is ApiResult.Loading -> progressHUD.show()
                    is ApiResult.Loaded -> progressHUD.dismiss()
                    is ApiResult.Empty -> {
                        isReported = true
                        GeneralUtils.showToast(requireContext(), getString(R.string.report_success))
                    }
                    is ApiResult.Error -> onApiError(apiResult.throwable)
                }
            }
        }

        descriptionViewModel.likeResult.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Loading -> progressHUD.show()
                is ApiResult.Loaded -> progressHUD.dismiss()
                is ApiResult.Success -> {
                    videoItem = it.result
                    setUILike()
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }

        descriptionViewModel.favoriteResult.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Loading -> progressHUD.show()
                is ApiResult.Loaded -> progressHUD.dismiss()
                is ApiResult.Success -> {
                    videoItem = it.result
                    setUIFavorite()
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }
    }

    override fun setupListeners() {
        btn_show_introduction.setOnClickListener {
            viewModel.showIntroduction.setNot()
        }
    }

    private fun setInteractiveListener() {
        imgLike.setOnClickListener {
            checkStatus { descriptionViewModel.like(videoItem, LikeType.LIKE) }
        }
        imgDislike.setOnClickListener {
            checkStatus { descriptionViewModel.like(videoItem, LikeType.DISLIKE) }
        }
        imgFavorite.setOnClickListener {
            checkStatus { descriptionViewModel.favorite(videoItem) }
        }
        imgMore.setOnClickListener {
            Timber.d("onMoreClick, item:$videoItem")
            videoItem.sources?.get(0)?.videoEpisodes?.get(0)?.videoStreams?.get(0)?.run {
                showMoreDialog(this.id ?: 0, PostType.VIDEO, isReported, videoItem.deducted)
            }
        }
    }

    private fun showMoreDialog(
        id: Long,
        type: PostType,
        isReported: Boolean,
        deducted: Boolean?,
        isComment: Boolean = false
    ) {
        Timber.i("id: $id")
        Timber.i("isReported: $isReported")

        moreDialog = MoreDialogFragment.newInstance(
            MemberPostItem(id = id, type = type, reported = isReported, deducted = deducted?:false),
            onMoreDialogListener,
            isComment,
            mainViewModel?.checkIsLogin() ?: false
        ).also {
            it.show(
                requireActivity().supportFragmentManager,
                MoreDialogFragment::class.java.simpleName
            )
        }
    }

    private val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
        override fun onProblemReport(item: BaseMemberPostItem, isComment: Boolean) {
            if ((item as MemberPostItem).reported) {
                GeneralUtils.showToast(
                    App.applicationContext(),
                    getString(R.string.already_reported)
                )
            } else {
                reportDialog =
                    ReportDialogFragment.newInstance(
                        item = item,
                        listener = onReportDialogListener,
                        isComment = isComment
                    ).also {
                        it.show(
                            requireActivity().supportFragmentManager,
                            ReportDialogFragment::class.java.simpleName
                        )
                    }
            }
            moreDialog?.dismiss()
        }

        override fun onCancel() {
            moreDialog?.dismiss()
        }
    }

    private fun setUI() {
        val subTitleColor = requireContext().getColor(R.color.color_black_1_50)

        btn_show_introduction.setTextColor(subTitleColor)
        tv_introduction.setTextColor(subTitleColor)
        tv_info.setTextColor(subTitleColor)
        tv_introduction.setBackgroundResource(R.drawable.bg_black_stroke_1_radius_2)

        recyclerview_source_list.adapter = sourceListAdapter
        recyclerview_episode.adapter = episodeAdapter

        recyclerview_guess_like.adapter = guessLikeAdapter

        val performers = videoItem.performers

        var tags = ""
        (videoItem.tags as List<String>).indices.mapNotNull {
            (videoItem.tags as List<String>)[it]
        }.forEach {
            tags = tags.plus(it).plus(",")
        }
        Timber.d("videoItem.tags ${videoItem.tags}, tag $tags")
        if(viewModel.videoContentId != descriptionViewModel.videoContentId) {
            descriptionViewModel.setupGuessLikeList(
                tags,
                performers,
                true,
                viewModel.videoContentId
            )
            descriptionViewModel.videoContentId = viewModel.videoContentId
        }

        val dateString = videoItem.updateTime?.let { date ->
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        }

        tv_title.text = videoItem.title
        tv_info.text = String.format(
            getString(R.string.player_info_format),
            dateString ?: "",
            videoItem.country
        )
        if (!videoItem.description.isNullOrEmpty()) {
            tv_introduction.text =
                Html.fromHtml(videoItem.description, Html.FROM_HTML_MODE_COMPACT)
        }

        if (videoItem.tags != null)
            setupChipGroup(videoItem.tags as List<String>)

        if (videoItem.sources == null || videoItem.sources!!.isEmpty()) {
            recyclerview_source_list.visibility = View.GONE
        } else {
            if (videoItem.sources!!.size == 1) recyclerview_source_list.visibility = View.GONE

            val result = mutableListOf<String>()
            for (item in videoItem.sources!!) {
                item.name?.also {
                    result.add(it)
                }
            }
            Timber.i("setupSourceList =${result[0]}")
            sourceListAdapter.submitList(result, 0)
        }
        setUILike()
        setUIFavorite()
        setInteractiveListener()
        setStreamInfo()
    }

    private fun setUILike() {
        imgLike.setImageResource(if (videoItem.likeType == LikeType.LIKE) R.drawable.ico_nice_s else R.drawable.ico_nice)
        txtLikeCount.text =
            String.format(getString(R.string.club_like_count), videoItem.likeCount)
        txtLikeCount.setTextColor(
            resources.getColor(
                if (videoItem.likeType == LikeType.LIKE) R.color.color_red_1 else R.color.color_black_1_60,
                null
            )
        )

        imgDislike.setImageResource(if (videoItem.likeType == LikeType.DISLIKE) R.drawable.ico_bad_s else R.drawable.ico_bad)
        txtDisLikeCount.text =
            String.format(getString(R.string.club_dislike_count), videoItem.dislikeCount)
        txtDisLikeCount.setTextColor(
            resources.getColor(
                if (videoItem.likeType == LikeType.DISLIKE) R.color.color_red_1 else R.color.color_black_1_60,
                null
            )
        )
    }

    private fun setUIFavorite() {
        imgFavorite.setImageResource(if (videoItem.favorite) R.drawable.btn_favorite_white_s else R.drawable.btn_favorite_white_n)
        txtFavorite.setTextColor(
            resources.getColor(
                if (videoItem.favorite) R.color.color_red_1 else R.color.color_black_1_60,
                null
            )
        )
    }

    private fun setStreamInfo() {
        val videoStream = arrayListOf<VideoEpisodeItem.VideoStream>()
        videoItem.sources?.get(0)?.videoEpisodes?.get(0)?.videoStreams?.forEach {
            videoStream.add(
                VideoEpisodeItem.VideoStream(
                    it.id,
                    it.sign,
                    it.streamName,
                    it.utcTime,
                    false
                ))
        }
        val videoEpisode = videoItem.sources?.get(0)?.videoEpisodes?.get(0).run {
            val videoEpisodeItem = VideoEpisodeItem(
                this?.episode,
                this?.episodePublishTime,
                this?.id,
                this?.reported,
                videoStream)
            videoEpisodeItem
        }.let {
            it
        }
        updateStreamInfo(videoEpisode)
    }

    private fun updateStreamInfo(videoEpisodeItem: VideoEpisodeItem) {
        val result = mutableListOf<String>()
        if (videoEpisodeItem.videoStreams != null
            && videoEpisodeItem.videoStreams.isNotEmpty()
        ) {
            for (item in videoEpisodeItem.videoStreams) {
                Timber.i("videoStreams =${item.id}")
                if (item.id != null) {
                    result.add(item.streamName ?: "")
                }
            }
        }
        episodeAdapter.submitList(result, viewModel.selectEpisodePosition.value)
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