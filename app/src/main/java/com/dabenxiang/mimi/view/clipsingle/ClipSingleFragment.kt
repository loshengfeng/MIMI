package com.dabenxiang.mimi.view.clipsingle

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.dialog.ReportDialogFragment
import com.dabenxiang.mimi.view.dialog.comment.CommentDialogFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.player.PlayerViewModel
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.item_clip.*
import timber.log.Timber

class ClipSingleFragment : BaseFragment() {

    private val viewModel: ClipSingleViewModel by viewModels()

    companion object {
        const val KEY_DATA = "data"

        fun createBundle(item: PlayItem): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    override val bottomNavigationVisibility = View.GONE

    private var exoPlayer: SimpleExoPlayer? = null

    private var playItem: PlayItem? = null

    override fun getLayoutId() = R.layout.item_clip

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (arguments?.getSerializable(KEY_DATA) as PlayItem).let { data ->
            playItem = data
            viewModel.getDecryptSetting(data.source ?: "")?.run {
                viewModel.decryptCover(data.cover ?: "", this) {
                    Glide.with(requireContext()).load(it).into(iv_cover)
                }
            } ?: run {
                Glide.with(requireContext()).load(data.cover).into(iv_cover)
            }

            tv_title.text = data.title
            iv_favorite.text = data.favoriteCount.toString()
            tv_comment.text = data.commentCount.toString()
            modifyFavorite()
            ib_back.visibility = View.VISIBLE

            viewModel.getM3U8(data)
        }
    }

    fun modifyFavorite() {
        Timber.d("@@modifyFavorite: ${playItem?.favorite}")
        val favoriteRes =
            takeIf { playItem?.favorite == true }?.let { R.drawable.btn_favorite_forvideo_s }
                ?: let { R.drawable.btn_favorite_forvideo_n }
        iv_favorite.setCompoundDrawablesRelativeWithIntrinsicBounds(0, favoriteRes, 0, 0)
    }

    override fun setupListeners() {
        ib_replay.setOnClickListener { view ->
            exoPlayer?.also { player ->
                player.seekTo(0)
                player.playWhenReady = true
            }
            view.visibility = View.GONE
        }

        player_view.setOnClickListener {
            takeIf { exoPlayer?.isPlaying ?: false }?.also {
                exoPlayer?.playWhenReady = false
                ib_play.visibility = View.VISIBLE
            } ?: run {
                exoPlayer?.playWhenReady = true
                ib_play.visibility = View.GONE
            }
        }

        ib_play.setOnClickListener {
            takeUnless { exoPlayer?.isPlaying ?: true }?.also {
                exoPlayer?.playWhenReady = true
                ib_play.visibility = View.GONE
            }
        }

        iv_favorite.setOnClickListener {
            playItem?.run { viewModel.modifyFavorite(this, this.favorite != true) }
        }

        tv_more.setOnClickListener {
            viewModel.videoEpisodeItem?.run {
                Pair(this.videoStreams?.get(0), this.reported ?: false)
            }?.run {
                val (videoStream, isReported) = this
                showMoreDialog(videoStream?.id ?: 0, PostType.VIDEO, isReported)
            }
        }

        tv_comment.setOnClickListener {
            viewModel.videoItem?.run { showCommentDialog(this) }
        }

        ib_back.setOnClickListener { findNavController().navigateUp() }
    }

    override fun setupObservers() {
        viewModel.getM3U8Result.observe(this, {
            when (it) {
                is ApiResult.Loading -> progressHUD.show()
                is ApiResult.Loaded -> progressHUD.dismiss()
                is ApiResult.Success -> {
                    viewModel.getDecryptSetting(playItem?.source ?: "")?.run {
                        viewModel.decryptM3U8(it.result, this) { decryptUrl ->
                            setupPlayer(player_view, decryptUrl)
                        }
                    } ?: run {
                        setupPlayer(player_view, it.result)
                    }
                }
                else -> {
                }
            }
        })

        viewModel.favoriteResult.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResult.Loading -> progressHUD.show()
                is ApiResult.Loaded -> progressHUD.dismiss()
                is ApiResult.Success -> modifyFavorite()
                is ApiResult.Error -> onApiError(it.throwable)
                else -> {
                }
            }
        })

        viewModel.videoReport.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResult.Loading -> progressHUD.show()
                is ApiResult.Loaded -> progressHUD.dismiss()
                is ApiResult.Empty -> GeneralUtils.showToast(
                    requireContext(),
                    getString(R.string.report_success)
                )
                is ApiResult.Error -> onApiError(it.throwable)
                else -> {
                }
            }
        })
    }

    override fun resetObservers() {
        viewModel.resetLiveData()
    }

    private fun setupPlayer(playerView: PlayerView, uri: String) {
        exoPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        exoPlayer?.also { player ->
            player.repeatMode = Player.REPEAT_MODE_OFF
            player.playWhenReady = true
            player.volume = PlayerViewModel.volume
            player.addListener(playbackStateListener)
        }
        playerView.player = exoPlayer
        val agent =
            Util.getUserAgent(requireContext(), requireContext().getString(R.string.app_name))
        val sourceFactory = DefaultDataSourceFactory(context, agent)
        getMediaSource(uri, sourceFactory)?.also { mediaSource ->
            playerView.player?.also {
                (it as SimpleExoPlayer).prepare(mediaSource, true, true)
            }
        }
    }

    private val playbackStateListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE"
                ExoPlayer.STATE_BUFFERING -> {
                    progress_video?.visibility = View.VISIBLE
                    "ExoPlayer.STATE_BUFFERING"
                }
                ExoPlayer.STATE_READY -> {
                    progress_video?.visibility = View.GONE
                    iv_cover?.visibility = View.GONE
                    "ExoPlayer.STATE_READY"
                }
                ExoPlayer.STATE_ENDED -> {
                    ib_replay?.visibility = View.VISIBLE
                    "ExoPlayer.STATE_ENDED"
                }
                else -> "UNKNOWN_STATE"
            }
            Timber.d("Changed state to $stateString playWhenReady: $playWhenReady")
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            Timber.d("onLoadingChanged")
        }

        override fun onPositionDiscontinuity(reason: Int) {
            Timber.d("onPositionDiscontinuity: $reason")
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            Timber.d("onTimelineChanged")
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            when (error.type) {
                ExoPlaybackException.TYPE_SOURCE -> {
                    Timber.d("error: TYPE_SOURCE")
                    //showErrorDialog("SOURCE")
                }
                ExoPlaybackException.TYPE_RENDERER -> {
                    Timber.d("error: TYPE_RENDERER")
                    //showErrorDialog("RENDERER")
                }
                ExoPlaybackException.TYPE_REMOTE -> {
                    Timber.d("error: TYPE_REMOTE")
                    //showErrorDialog("REMOTE")
                }
                ExoPlaybackException.TYPE_OUT_OF_MEMORY -> {
                    Timber.d("error: OUT_OF_MEMORY")
                    //showErrorDialog("OUT_OF_MEMORY")
                }
                ExoPlaybackException.TYPE_UNEXPECTED -> {
                    Timber.d("error: TYPE_UNEXPECTED")
                    //showErrorDialog("UNEXPECTED")
                }
                else -> {
                    Timber.d("error: UNKNOWN")
                    //showErrorDialog("UNKNOWN")
                }
            }
        }
    }

    private fun getMediaSource(
        uriString: String,
        sourceFactory: DefaultDataSourceFactory
    ): MediaSource? {
        val uri = Uri.parse(uriString)

        val sourceType = Util.inferContentType(uri)
        Timber.d("#sourceType: $sourceType")

        return when (sourceType) {
            C.TYPE_DASH ->
                DashMediaSource.Factory(sourceFactory)
                    .createMediaSource(uri)
            C.TYPE_HLS ->
                HlsMediaSource.Factory(sourceFactory)
                    .createMediaSource(uri)
            C.TYPE_SS ->
                SsMediaSource.Factory(sourceFactory)
                    .createMediaSource(uri)
            C.TYPE_OTHER -> {
                when {
                    uriString.startsWith("rtmp://") ->
                        ProgressiveMediaSource.Factory(RtmpDataSourceFactory())
                            .createMediaSource(uri)
                    uriString.contains("m3u8") -> HlsMediaSource.Factory(sourceFactory)
                        .createMediaSource(uri)
                    else ->
                        ProgressiveMediaSource.Factory(sourceFactory)
                            .createMediaSource(uri)
                }
            }
            else -> null
        }
    }

    private fun showCommentDialog(item: VideoItem) {
        val listener = object : CommentDialogFragment.CommentListener {
            override fun onAvatarClick(userId: Long, name: String) {
                val bundle = MyPostFragment.createBundle(
                    userId, name,
                    isAdult = true,
                    isAdultTheme = true
                )
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_to_myPostFragment,
                        bundle
                    )
                )
            }

            override fun onUpdateCommentCount(count: Int) {
                tv_comment.text = count.toString()
            }
        }
        CommentDialogFragment.newInstance(item, listener).also {
            it.isCancelable = true
            it.show(
                requireActivity().supportFragmentManager,
                CommentDialogFragment::class.java.simpleName
            )
        }
    }

    private var moreDialog: MoreDialogFragment? = null
    private var reportDialog: ReportDialogFragment? = null

    private fun showMoreDialog(
        id: Long,
        type: PostType,
        isReported: Boolean,
        isComment: Boolean = false
    ) {
        Timber.i("id=$id")
        Timber.i("isReported=$isReported")
        moreDialog = MoreDialogFragment.newInstance(
            MemberPostItem(id = id, type = type, reported = isReported),
            onMoreDialogListener,
            isComment
        ).also {
            it.show(
                requireActivity().supportFragmentManager,
                MoreDialogFragment::class.java.simpleName
            )
        }
    }

    private val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
        override fun onProblemReport(item: BaseMemberPostItem, isComment: Boolean) {
            checkStatus {
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
        }

        override fun onCancel() {
            moreDialog?.dismiss()
        }
    }

    private val onReportDialogListener = object : ReportDialogFragment.OnReportDialogListener {
        override fun onSend(item: BaseMemberPostItem, content: String, postItem: MemberPostItem?) {
            if (TextUtils.isEmpty(content)) {
                GeneralUtils.showToast(App.applicationContext(), getString(R.string.report_error))
            } else {
                when (item) {
                    is MemberPostItem -> {
                        viewModel.sendVideoReport(item.id.toString())
                    }
                }
            }
            reportDialog?.dismiss()
        }

        override fun onCancel() {
            Timber.i("reportDialog onCancel reportDialog=$reportDialog ")
            reportDialog?.dismiss()
        }
    }
}