package com.dabenxiang.mimi.view.player.ui

import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.extension.handleException
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.VideoEpisodeItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.api.vo.VideoM3u8Source
import com.dabenxiang.mimi.model.enums.HttpErrorMsgType
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.club.post.ClubCommentFragment
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.player.PlayerViewModel
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.OrientationDetector
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.custom_playback_control.*
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.fragment_v2_player.*
import kotlinx.android.synthetic.main.fragment_v2_player.iv_player
import kotlinx.android.synthetic.main.fragment_v2_player.player_view
import kotlinx.android.synthetic.main.fragment_v2_player.recharge_reminder
import kotlinx.android.synthetic.main.fragment_v2_player.tv_forward_backward
import kotlinx.android.synthetic.main.fragment_v2_player.tv_sound_tune
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.UnknownHostException
import kotlin.math.abs
import kotlin.math.round

class PlayerV2Fragment: BaseFragment(), AnalyticsListener, Player.EventListener {

    companion object {
        private const val KEY_PLAYER_SRC = "KEY_PLAYER_SRC"
        private const val KEY_IS_COMMENT = "KEY_IS_COMMENT"
        private const val JUMP_TIME = 1000
        private const val SWIPE_DISTANCE_UNIT = 25
        private const val SWIPE_SOUND_LEAST = 100

        fun createBundle(item: PlayerItem, isComment: Boolean = false): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_PLAYER_SRC, item)
                it.putBoolean(KEY_IS_COMMENT, isComment)
            }
        }
    }

    private val viewModel: PlayerV2ViewModel by activityViewModels()

    private var player: SimpleExoPlayer? = null
    private var orientationDetector: OrientationDetector? = null

    override fun getLayoutId(): Int {
        return R.layout.fragment_v2_player
    }

    override fun setupObservers() {
        viewModel.videoContentSource.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Loading -> progressHUD.show()
                is ApiResult.Success -> {
                    parsingVideoContent(it.result)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }

        viewModel.episodeContentSource.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Loading -> {
                    if(!progressHUD.isShowing)
                        progressHUD.show()
                }
                is ApiResult.Success -> {
                    parsingEpisodeContent(it.result)
                }
                is ApiResult.Error -> {
                    when (it.throwable) {
                        is PlayerV2ViewModel.NotDeductedException -> {
                            showRechargeReminder(true)
                            if(progressHUD.isShowing)
                                progressHUD.dismiss()
                        }
                        else -> onApiError(it.throwable)
                    }
                }
            }
        }

        viewModel.m3u8ContentSource.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Loading -> {
                    if(!progressHUD.isShowing)
                        progressHUD.show()
                }
                is ApiResult.Loaded -> progressHUD.dismiss()
                is ApiResult.Success -> {
                    parsionM3u8Content(it.result)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }

        viewModel.videoStreamingUrl.observe(viewLifecycleOwner) {
            if(!it.isNullOrEmpty()) {
                setupPlayUrl(it, (viewModel.m3u8SourceUrl != it) )
                viewModel.m3u8SourceUrl = it
            }
        }

        viewModel.showRechargeReminder.observe(viewLifecycleOwner) {
            showRechargeReminder(it)
        }

        viewModel.fastForwardTime.observe(viewLifecycleOwner) {
            tv_forward_backward.text = "${if (it > 0) "+" else ""}${it / 1000}秒"
        }

        viewModel.soundLevel.observe(viewLifecycleOwner) {
            //Timber.d("sound level: $it")
            val soundLevel = round(it * 10) * 10
            tv_sound_tune.text = "音量${soundLevel.toInt()}%"
        }
    }

    override fun setupListeners() {

        orientationDetector =
            OrientationDetector(
                requireActivity(),
                SensorManager.SENSOR_DELAY_NORMAL
            ).also { detector ->
                detector.setChangeListener(object : OrientationDetector.OnChangeListener {
                    override fun onChanged(orientation: Int) {

                        Timber.i("detector onChanged")
                        viewModel.currentOrientation = orientation
                        if (viewModel.lockFullScreen) {
                            when (orientation) {
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> {
                                    requireActivity().requestedOrientation = orientation
                                }
                            }
                        } else {
                            requireActivity().requestedOrientation = orientation
                        }
                        adjustPlayerSize()
                    }
                })
            }

        exo_play_pause.setOnClickListener {
            Timber.d("exo_play_pause confirmed")
            player?.also {
                it.playWhenReady.also { playing ->
                    it.playWhenReady = !playing
                    viewModel.setPlaying(!playing)
                    if (!playing)
                        exo_play_pause.setImageDrawable(requireContext().getDrawable(R.drawable.exo_icon_pause))
                    else
                        exo_play_pause.setImageDrawable(requireContext().getDrawable(R.drawable.exo_icon_play))
                }
            }
        }

        iv_player.setOnClickListener {
            if (it.visibility == View.VISIBLE) {
                player?.playWhenReady = true
                viewModel.setPlaying(true)
                exo_play_pause.setImageDrawable(requireContext().getDrawable(R.drawable.exo_icon_pause))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        player_pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                when(position) {
                    0 -> {
                        return PlayerDescriptionFragment()
                    }
                    else -> {
                        val memberPostItem = MemberPostItem()
                        memberPostItem.id = viewModel.videoContentId
                        return ClubCommentFragment.createBundle(memberPostItem)
                    }
                }
            }

        }

        TabLayoutMediator(tabs, player_pager) { tab, position ->
            when(position) {
                0 -> tab.text = "视频简介"
                1 -> tab.text = "评论"
            }

        }.attach()
    }

    override fun onStart() {
        super.onStart()
        setupPlayer()
        player_view.onResume()
    }

    override fun onResume() {
        super.onResume()

        getVideoContent()

        if (player == null) {
            setupPlayer()
            player_view.onResume()
        }

        orientationDetector?.apply { enable() }
    }

    override fun onPause() {
        Timber.d("player activity onPause")
        super.onPause()

        orientationDetector?.apply {disable()}
        player_view.onPause()
        releasePlayer()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        player_view.onPause()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearLiveData()
    }

    private fun showRechargeReminder(isShow: Boolean) {
        Timber.i("showRechargeReminder")
        player_view.visibility = if (isShow) View.INVISIBLE else View.VISIBLE
        recharge_reminder.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    /**
     * parsing the video content
     */
    private fun parsingVideoContent(videoItem: VideoItem) {
        viewModel.parsingVideoContent(videoItem)
    }

    private fun parsingEpisodeContent(videoEpisodeItem: VideoEpisodeItem) {
        viewModel.parsingEpisodeContent(videoEpisodeItem)
    }

    private fun parsionM3u8Content(videoM3u8Source: VideoM3u8Source) {
        viewModel.parsingM3u8Content(videoM3u8Source)
    }

    private fun setupPlayUrl(url: String, isReset: Boolean) {
        val agent = Util.getUserAgent(requireContext(), getString(R.string.app_name))
        val sourceFactory = DefaultDataSourceFactory(requireContext(), agent)

        viewModel.getMediaSource(url, sourceFactory)?.also {
            Timber.d("player ready confirmed")
            player?.prepare(it, isReset, isReset)
        }
    }

    /**
     * get video or clip content
     */
    private fun getVideoContent() {
        (arguments?.getSerializable(KEY_PLAYER_SRC) as PlayerItem?)?.also {
            viewModel.videoContentId = it.videoId
            viewModel.getVideoContent()
        }
    }

    /**
     *  init player
     */
    private fun setupPlayer() {
        if (player == null) {
            player = SimpleExoPlayer.Builder(requireContext()).build()
            player_view.player = player

            player?.also { player ->
                player.repeatMode = Player.REPEAT_MODE_OFF
                player.playWhenReady = viewModel.isPlaying.value ?: true
                player.seekTo(viewModel.currentWindow, viewModel.playbackPosition)
                player.volume = PlayerViewModel.volume
                player.addListener(this)
                player.addAnalyticsListener(this)
                viewModel.setPlaying(true)
                initTouchListener()
            }
        }
    }

    /**
     * Player.EventListener
     */
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        val stateString: String = when (playbackState) {
            ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE"
            ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING"
            ExoPlayer.STATE_READY -> {
//                viewModel.activateLoading(false)
                player_view.visibility = View.VISIBLE
                "ExoPlayer.STATE_READY"
            }

            ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED"
            else -> "UNKNOWN_STATE"
        }
        Timber.d("Changed state to $stateString playWhenReady: $playWhenReady")
//        if (playbackState == ExoPlayer.STATE_ENDED && (viewModel.episodePosition.value!! < episodeAdapter.itemCount - 1)) {
//            viewModel.setStreamPosition(viewModel.episodePosition.value!! + 1)
//        }
    }

    /**
     * Player.EventListener
     */
    override fun onLoadingChanged(isLoading: Boolean) {
        Timber.d("onLoadingChanged")
    }

    /**
     * Player.EventListener
     */
    override fun onPositionDiscontinuity(reason: Int) {
        //Timber.d("onPositionDiscontinuity: $reason")
    }

    /**
     * Player.EventListener
     */
    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        Timber.d("onTimelineChanged")
    }

    /**
     * Player.EventListener
     */
    override fun onPlayerError(error: ExoPlaybackException) {
        when (error.type) {
            ExoPlaybackException.TYPE_SOURCE -> {
                Timber.d("error: TYPE_SOURCE")
//                viewModel.activateLoading(true)
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
        viewModel.sendVideoReport()
    }

    /**
     * AnalyticsListener
     */
    override fun onRenderedFirstFrame(
        eventTime: AnalyticsListener.EventTime,
        surface: Surface?
    ) {
        Timber.d("AnalyticsListener onRenderedFirstFrame")
    }

    /**
     * AnalyticsListener
     */
    override fun onDroppedVideoFrames(
        eventTime: AnalyticsListener.EventTime,
        droppedFrames: Int,
        elapsedMs: Long
    ) {
        Timber.d("AnalyticsListener onDroppedVideoFrames")
    }

    /**
     * AnalyticsListener
     */
    override fun onAudioUnderrun(
        eventTime: AnalyticsListener.EventTime,
        bufferSize: Int,
        bufferSizeMs: Long,
        elapsedSinceLastFeedMs: Long
    ) {
        Timber.d("AnalyticsListener onAudioUnderrun")
    }

    private fun initTouchListener() {
        var originX = 0f
        var originY = 0f
        var isMove = false
        player_view?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //Timber.d("ACTION_DOWN")
                    if (!player_view.controllerAutoShow)
                        player_view.showController()
                    originX = event.x
                    originY = event.y
                    isMove = false
                }
                MotionEvent.ACTION_MOVE -> {
                    //Timber.d("ACTION_MOVE")
                    val dx = event.x - originX
                    val dy = event.y - originY
                    if (isMove)
                        player_view.showController()
                    isMove = if (abs(dx) >= SWIPE_DISTANCE_UNIT || abs(dy) >= SWIPE_DISTANCE_UNIT) {
                        if (abs(dx) > abs(dy)) {
                            if (viewModel.isPlaying.value == true) {
                                tv_forward_backward.visibility = View.VISIBLE
                                tv_sound_tune.visibility = View.GONE
                                if (dx > 0)
                                    viewModel.setFastForwardTime((dx.toInt() / SWIPE_DISTANCE_UNIT) * JUMP_TIME)
                                else
                                    viewModel.setRewindTime(abs((dx.toInt() / SWIPE_DISTANCE_UNIT) * JUMP_TIME))
                            }
                        } else {
                            tv_forward_backward.visibility = View.GONE
                            tv_sound_tune.visibility = View.VISIBLE
                            if (abs(dy) > PlayerV2Fragment.SWIPE_SOUND_LEAST) {
                                if (dy > 0)
                                    viewModel.setSoundLevel(player!!.volume - 0.1f)
                                else
                                    viewModel.setSoundLevel(player!!.volume + 0.1f)
                            }
                        }
                        true
                    } else false
                }
                MotionEvent.ACTION_UP -> {
                    //Timber.d("ACTION_UP")
                    val dx = event.x - originX
                    val dy = event.y - originY
                    isMove = if (abs(dx) >= SWIPE_DISTANCE_UNIT || abs(dy) >= SWIPE_DISTANCE_UNIT) {
                        if (abs(dx) > abs(dy)) {
                            if (viewModel.isPlaying.value == true) {
                                if (dx > 0) {
                                    val fastForwardMs =
                                        (dx.toInt() / SWIPE_DISTANCE_UNIT) * JUMP_TIME
                                    fastForward(fastForwardMs)

                                } else {
                                    val rewindMs =
                                        abs((dx.toInt() / SWIPE_DISTANCE_UNIT) * JUMP_TIME)
                                    rewind(rewindMs)
                                }
                            }
                        } else {
                            if (abs(dy) > SWIPE_SOUND_LEAST) {
                                if (dy > 0)
                                    soundDown()
                                else
                                    soundUp()
                            }
                        }
                        true
                    } else {
                        false
                    }

                    tv_forward_backward.visibility = View.GONE
                    tv_sound_tune.visibility = View.GONE
                }
                else -> {

                }
            }
            isMove
        }
    }

    private fun rewind(rewindMs: Int) {
        Timber.i("fastForward rewind=$rewindMs")
        player?.takeIf { it.isCurrentWindowSeekable && rewindMs > 0 }?.apply {
            viewModel.setRewindTime(rewindMs)
            seekTo(currentWindowIndex,
                if (currentPosition > rewindMs) currentPosition - rewindMs else 0)
        }

    }

    private fun fastForward(fastForwardMs: Int) {
        Timber.i("fastForward fastForwardMs=$fastForwardMs")
        player?.takeIf { it.isCurrentWindowSeekable && fastForwardMs > 0 }?.apply {
            viewModel.setFastForwardTime(fastForwardMs)
            seekTo(currentWindowIndex, currentPosition + fastForwardMs)
        }
    }

    private fun soundUp() {
        player?.apply { volume += 0.1f}
    }

    private fun soundDown() {
        player?.apply {volume -= 0.1f}
    }

    /**
     * 調整 player 的寬與高
     */
    private fun adjustPlayerSize() {

        val screenSize = GeneralUtils.getScreenSize(requireActivity())
        if (requireActivity().requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            val params = player_view.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = 0
            player_view.layoutParams = params
            requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            fullScreenUISet(false)
        } else {
            val params = player_view.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            player_view.layoutParams = params
            requireActivity().window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            fullScreenUISet(true)
        }
    }

    private fun commentEditorHide() {
        Timber.i("commentEditorHide")
        CoroutineScope(Dispatchers.Main).launch {
//            tv_replay_name?.visibility = View.GONE
//            (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)?.apply {
//                hideSoftInputFromWindow(et_message.windowToken, 0)
//            }
        }
    }

    private fun fullScreenUISet(isFullScreen: Boolean) {
        if (isFullScreen) {
            commentEditorHide()
//            recycler_info.visibility = View.GONE
//            bottom_func_bar.visibility = View.GONE
//            bottom_func_input.visibility = View.GONE
        } else {
//            recycler_info?.visibility = View.VISIBLE
//            bottom_func_bar?.visibility = View.VISIBLE
//            bottom_func_input.visibility = View.GONE
        }
    }

    private fun onApiError(throwable: Throwable) {
        when (val errorHandler = throwable.handleException { e -> viewModel.processException(e) }) {
            is ExceptionResult.RefreshTokenExpired -> viewModel.logoutLocal()
            is ExceptionResult.HttpError -> handleHttpError(errorHandler)
            is ExceptionResult.Crash -> {
                if (errorHandler.throwable is UnknownHostException) {
                    showCrashDialog(HttpErrorMsgType.CHECK_NETWORK)
                } else {
                    GeneralUtils.showToast(requireContext(), errorHandler.throwable.toString())
                }
            }
        }
    }

    private fun showCrashDialog(type: HttpErrorMsgType = HttpErrorMsgType.API_FAILED) {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.error_device_binding_title,
                message = when (type) {
                    HttpErrorMsgType.API_FAILED -> getString(R.string.api_failed_msg)
                    HttpErrorMsgType.CHECK_NETWORK -> getString(R.string.server_error)
                },
                messageIcon = R.drawable.ico_default_photo,
                secondBtn = getString(R.string.btn_close)
            )
        ).show(requireActivity().supportFragmentManager)
    }

    private fun releasePlayer() {
        player?.also { player ->
            viewModel.playbackPosition = player.currentPosition
            viewModel.currentWindow = player.currentWindowIndex
            viewModel.setPlaying(player.playWhenReady)
            player.removeListener(this)
            player.release()
        }
        player = null
    }
}