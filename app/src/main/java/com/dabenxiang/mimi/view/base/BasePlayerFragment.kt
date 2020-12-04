package com.dabenxiang.mimi.view.base

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.topup.TopUpFragment
import com.dabenxiang.mimi.widget.utility.OrientationDetector
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.custom_playback_control.*
import kotlinx.android.synthetic.main.fragment_v2_player.*
import kotlinx.android.synthetic.main.fragment_v2_player.view.*
import kotlinx.android.synthetic.main.recharge_reminder.*
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.round

abstract class BasePlayerFragment : BaseFragment(), AnalyticsListener, Player.EventListener {

    private val JUMP_TIME = 1000
    private val SWIPE_DISTANCE_UNIT = 25
    private val SWIPE_SOUND_LEAST = 100
    private var volume: Float = 1f
    open var streamId = 0L

    private var player: SimpleExoPlayer? = null
    private var orientationDetector: OrientationDetector? = null

    private val playerViewModel: BasePlayerViewModel by activityViewModels()
    override val isStatusBarDark: Boolean = true

    open var contentId: Long = 0

    override fun getLayoutId() = R.layout.fragment_v2_player

    abstract fun getViewPagerCount(): Int

    abstract fun createViewPagerFragment(position: Int): Fragment

    abstract fun getTabTitle(tab: TabLayout.Tab, position: Int)

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getLayoutId(), container, false)
        view.player_pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = getViewPagerCount()
            override fun createFragment(position: Int) = createViewPagerFragment(position)
        }
        TabLayoutMediator(view.tabs, view.player_pager) { tab, position ->
            getTabTitle(tab, position)
        }.attach()
        return view
    }

    override fun onStart() {
        super.onStart()
        setupPlayer()
        player_view.onResume()
    }

    override fun onResume() {
        super.onResume()

        if (player == null) {
            setupPlayer()
            player_view.onResume()
        }

        orientationDetector?.apply { enable() }
    }

    override fun onPause() {
        Timber.d("player activity onPause")
        super.onPause()

        orientationDetector?.apply { disable() }
        player_view.onPause()
        releasePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player_view.onPause()
        releasePlayer()

        if (requireActivity().requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            playerViewModel.lockFullScreen = !playerViewModel.lockFullScreen
            switchScreenOrientation()
        }
    }

    override fun setupObservers() {
        playerViewModel.isPlaying.observe(viewLifecycleOwner) {
            iv_player.visibility = when (it) {
                true -> View.GONE
                false -> View.VISIBLE
            }
        }

        playerViewModel.showRechargeReminder.observe(viewLifecycleOwner) {
            showRechargeReminder(it)
        }

        playerViewModel.fastForwardTime.observe(viewLifecycleOwner) {
            tv_forward_backward.text = "${if (it > 0) "+" else ""}${it / 1000}秒"
        }

        playerViewModel.soundLevel.observe(viewLifecycleOwner) {
            //Timber.d("sound level: $it")
            val soundLevel = round(it * 10) * 10
            tv_sound_tune.text = "音量${soundLevel.toInt()}%"
        }
    }

    override fun setupListeners() {
        requireActivity().onBackPressedDispatcher.addCallback(
            owner = viewLifecycleOwner,
            onBackPressed = {
                if (requireActivity().requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    playerViewModel.lockFullScreen = !playerViewModel.lockFullScreen
                    switchScreenOrientation()
                } else {
                    navigateTo(NavigateItem.Up)
                }
            }
        )
        orientationDetector =
            OrientationDetector(
                requireActivity(),
                SensorManager.SENSOR_DELAY_NORMAL
            ).also { detector ->
                detector.setChangeListener(object : OrientationDetector.OnChangeListener {
                    override fun onChanged(orientation: Int) {

                        Timber.i("detector onChanged")
                        playerViewModel.currentOrientation = orientation
                        if (playerViewModel.lockFullScreen) {
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
                    playerViewModel.setPlaying(!playing)
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
                playerViewModel.setPlaying(true)
                exo_play_pause.setImageDrawable(requireContext().getDrawable(R.drawable.exo_icon_pause))
            }
        }

        btn_full_screen.setOnClickListener {
            playerViewModel.lockFullScreen = !playerViewModel.lockFullScreen
            switchScreenOrientation()
        }

        btn_vip.setOnClickListener {
            val bundle = TopUpFragment.createBundle(this::class.java.simpleName)
            navigateTo(NavigateItem.Destination(R.id.action_to_topup, bundle))
        }

        btn_promote.setOnClickListener {
            val bundle = Bundle()
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_inviteVipFragment,
                    bundle
                )
            )
        }
    }

    fun showRechargeReminder(isShow: Boolean) {
        Timber.i("showRechargeReminder")
        player_view.visibility = if (isShow) View.INVISIBLE else View.VISIBLE
        if (isShow)
            iv_player.visibility = View.INVISIBLE
        recharge_reminder.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    fun setupPlayUrl(url: String, isReset: Boolean) {
        val agent = Util.getUserAgent(requireContext(), getString(R.string.app_name))
        val sourceFactory = DefaultDataSourceFactory(requireContext(), agent)

        playerViewModel.getMediaSource(url, sourceFactory)?.also {
            Timber.d("player ready confirmed")
            player?.prepare(it, isReset, isReset)
        }
    }

    fun stopPlay() {
        player?.stop()
        player?.clearVideoDecoderOutputBufferRenderer()
    }

    fun sendVideoReport() {
        playerViewModel.sendVideoReport(streamId)
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
                player.playWhenReady = playerViewModel.isPlaying.value ?: true
                player.seekTo(playerViewModel.currentWindow, playerViewModel.playbackPosition)
                player.volume = volume
                player.addListener(this)
                player.addAnalyticsListener(this)
                playerViewModel.setPlaying(player.playWhenReady)
                val play_pause_id =
                    if (player.playWhenReady) R.drawable.exo_icon_pause else R.drawable.exo_icon_play
                exo_play_pause.setImageDrawable(requireContext().getDrawable(play_pause_id))
                initTouchListener()
            }
        }
    }

    private fun releasePlayer() {
        player?.also { player ->
            playerViewModel.playbackPosition = player.currentPosition
            playerViewModel.currentWindow = player.currentWindowIndex
            playerViewModel.setPlaying(player.playWhenReady)
            player.removeListener(this)
            player.release()
        }
        player = null
    }

    @SuppressLint("ClickableViewAccessibility")
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
                            if (playerViewModel.isPlaying.value == true) {
                                tv_forward_backward.visibility = View.VISIBLE
                                tv_sound_tune.visibility = View.GONE
                                if (dx > 0)
                                    playerViewModel.setFastForwardTime((dx.toInt() / SWIPE_DISTANCE_UNIT) * JUMP_TIME)
                                else
                                    playerViewModel.setRewindTime(abs((dx.toInt() / SWIPE_DISTANCE_UNIT) * JUMP_TIME))
                            }
                        } else {
                            tv_forward_backward.visibility = View.GONE
                            tv_sound_tune.visibility = View.VISIBLE
                            if (abs(dy) > SWIPE_SOUND_LEAST) {
                                if (dy > 0)
                                    playerViewModel.setSoundLevel(player!!.volume - 0.1f)
                                else
                                    playerViewModel.setSoundLevel(player!!.volume + 0.1f)
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
                            if (playerViewModel.isPlaying.value == true) {
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
            playerViewModel.setRewindTime(rewindMs)
            seekTo(
                currentWindowIndex,
                if (currentPosition > rewindMs) currentPosition - rewindMs else 0
            )
        }

    }

    private fun fastForward(fastForwardMs: Int) {
        Timber.i("fastForward fastForwardMs=$fastForwardMs")
        player?.takeIf { it.isCurrentWindowSeekable && fastForwardMs > 0 }?.apply {
            playerViewModel.setFastForwardTime(fastForwardMs)
            seekTo(currentWindowIndex, currentPosition + fastForwardMs)
        }
    }

    private fun soundUp() {
        player?.apply { volume += 0.1f }
    }

    private fun soundDown() {
        player?.apply { volume -= 0.1f }
    }

    /**
     * 調整 player 的寬與高
     */
    private fun adjustPlayerSize() {
        if (requireActivity().requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            val params = player_view.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = 0
            player_view.layoutParams = params
//            activity?.bottom_navigation?.visibility = View.VISIBLE
        } else {
            val params = player_view.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            player_view.layoutParams = params
            requireActivity().window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//            activity?.bottom_navigation?.visibility = View.GONE
        }
    }

    /**
     * 切換螢幕方向
     */
    private fun switchScreenOrientation() {
        requireActivity().requestedOrientation =
            if (playerViewModel.lockFullScreen) {
                when (playerViewModel.currentOrientation) {
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> playerViewModel.currentOrientation
                    else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        adjustPlayerSize()
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
        sendVideoReport()
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
}