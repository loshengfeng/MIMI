package com.dabenxiang.mimi.view.player

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.extension.setBtnSolidDolor
import com.dabenxiang.mimi.extension.setNot
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.model.api.vo.handleException
import com.dabenxiang.mimi.model.enums.HttpErrorMsgType
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.base.BaseActivity
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.login.LoginActivity
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.OrientationDetector
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.chip.Chip
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.custom_playback_control.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.round


class PlayerActivity : BaseActivity() {

    companion object {
        const val REQUEST_CODE = 111
        private const val KEY_PLAYER_SRC = "KEY_PLAYER_SRC"
        private const val JUMP_TIME = 1000
        private const val SWIPE_DISTANCE_UNIT = 25
        private const val SWIPE_SOUND_LEAST = 100

        fun createBundle(data: PlayerData): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_PLAYER_SRC, data)
            }
        }
    }

    private val viewModel by viewModel<PlayerViewModel>()

    private var player: SimpleExoPlayer? = null
    private var orientationDetector: OrientationDetector? = null
    private var dialog: GeneralDialog? = null

    private val progressHUD by lazy {
        KProgressHUD.create(this).setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_player
    }

    fun getIsAdult() = (intent.extras?.getSerializable(KEY_PLAYER_SRC) as PlayerData?)?.isAdult ?: false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isAdult = getIsAdult()
        val backgroundColor = if (isAdult) {
            getColor(R.color.adult_color_background)
        } else {
            getColor(R.color.normal_color_background)
        }
        scrollView.setBackgroundColor(backgroundColor)

        val titleColor =
            if (isAdult) {
                R.color.adult_color_text
            } else {
                R.color.normal_color_text
            }.let {
                getColor(it)
            }
        tv_title.setTextColor(titleColor)

        val subTitleColor =
            if (isAdult) {
                R.color.color_white_1_50
            } else {
                R.color.color_black_1_50
            }.let {
                getColor(it)
            }
        btn_show_introduction.setTextColor(subTitleColor)
        tv_introduction.setTextColor(subTitleColor)
        tv_info.setTextColor(subTitleColor)
        tv_introduction.setBackgroundResource(
            if (isAdult) {
                R.drawable.bg_white_stroke_1_radius_2
            } else {
                R.drawable.bg_black_stroke_1_radius_2
            }
        )

        btn_show_introduction.setOnClickListener {
            viewModel.showIntroduction.setNot()
        }

        viewModel.showIntroduction.observe(this, Observer { isShow ->
            val drawableRes =
                if (isAdult) {
                    if (isShow) {
                        R.drawable.btn_arrowup_white_n
                    } else {
                        R.drawable.btn_arrowdown_white_n
                    }
                } else {
                    if (isShow) {
                        R.drawable.btn_arrowup_gray_n
                    } else {
                        R.drawable.btn_arrowdown_gray_n
                    }
                }
            tv_introduction.visibility = if (isShow) View.VISIBLE else View.GONE
            btn_show_introduction.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)
        })

        bottom_func_bar.setBackgroundResource(
            if (isAdult) {
                R.drawable.bg_adult_top_line
            } else {
                R.drawable.bg_gray_2_top_line
            }
        )

        if (isAdult) {
            btn_write_comment.setTextColor(getColor(R.color.color_white_1_30))
            btn_write_comment.setBtnSolidDolor(getColor(R.color.color_black_1_20))
        }

        viewModel.fastForwardTime.observe(this, Observer {
            tv_forward_backward.text = "${if (it > 0) "+" else ""}${it / 1000}秒"
        })

        viewModel.soundLevel.observe(this, Observer {
            //Timber.d("sound level: $it")
            val soundLevel = round(it * 10) * 10
            tv_sound_tune.text = "音量${soundLevel.toInt()}%"
        })

        viewModel.isLoadingActive.observe(this, Observer {
            if (it) {
                progress_video.visibility = View.VISIBLE
            } else {
                progress_video.visibility = View.GONE
            }
        })

        viewModel.currentVideoUrl.observe(this, Observer {
            it?.also { url ->
                setupPlayUrl(url)
            }
        })

        viewModel.isPlaying.observe(this, Observer {
            iv_player.visibility = when (it) {
                true -> View.GONE
                false -> View.VISIBLE
            }
        })

        var isFirstInit = true
        viewModel.apiVideoInfo.observe(this, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD.show()
                is ApiResult.Error -> {
                    progressHUD.dismiss()
                    onApiError(it.throwable)
                }
                is ApiResult.Success -> {
                    progressHUD.dismiss()

                    val result = it.result
                    Timber.d("Result: $result")

                    if (isFirstInit) {
                        isFirstInit = false
                        tv_title.text = result.title
                        tv_introduction.text = result.description

                        val dateString = result.updateTime?.let { date ->
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                        }

                        tv_info.text = String.format(getString(R.string.player_info_format), dateString ?: "", result.country)

                        setupChipGroup(result.tags)
                    }

                    viewModel.likeVideo.value = result.like
                    viewModel.likeVideoCount.value = result.likeCount
                    viewModel.favoriteVideo.value = result.favorite
                    viewModel.favoriteVideoCount.value = result.favoriteCount
                    viewModel.commentCount.value = result.commentCount

                    viewModel.isDeducted = result.deducted ?: false
                    viewModel.costPoint = result.point ?: 0L
                    viewModel.availablePoint = result.availablePoint ?: 0L
                }
            }
        })

        tv_comment.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (isAdult) R.drawable.ico_messege_adult else R.drawable.ico_messege_adult_gray, 0, 0, 0
        )

        tv_like.setTextColor(titleColor)
        tv_favorite.setTextColor(titleColor)
        tv_comment.setTextColor(titleColor)

        iv_share.setImageResource(if (isAdult) R.drawable.btn_share_white_n else R.drawable.btn_share_gray_n)
        iv_more.setImageResource(if (isAdult) R.drawable.btn_more_white_n else R.drawable.btn_more_gray_n)

        viewModel.likeVideo.observe(this, Observer {
            val res = when (it) {
                true -> R.drawable.ico_nice_s
                else ->
                    when (isAdult) {
                        true -> R.drawable.ico_nice
                        else -> R.drawable.ico_nice_gray
                    }
            }

            tv_like.setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)
        })

        viewModel.likeVideoCount.observe(this, Observer {
            tv_like.text = it.toString()
        })

        viewModel.favoriteVideo.observe(this, Observer {
            val res = when (it) {
                true -> R.drawable.btn_favorite_white_s
                else ->
                    when (isAdult) {
                        true -> R.drawable.btn_favorite_white_n
                        else -> R.drawable.btn_favorite_n
                    }
            }

            tv_favorite.setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)
        })

        viewModel.favoriteVideoCount.observe(this, Observer {
            tv_favorite.text = it.toString()
        })

        viewModel.commentCount.observe(this, Observer {
            tv_comment.text = it.toString()
        })

        btn_full_screen.setOnClickListener {
            viewModel.lockFullScreen = !viewModel.lockFullScreen

            requestedOrientation =
                if (viewModel.lockFullScreen) {
                    when (viewModel.currentOrientation) {
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> viewModel.currentOrientation
                        else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
        }

        orientationDetector = OrientationDetector(this, SensorManager.SENSOR_DELAY_NORMAL).also { detector ->
            detector.setChangeListener(object : OrientationDetector.OnChangeListener {
                override fun onChanged(orientation: Int) {
                    viewModel.currentOrientation = orientation

                    if (viewModel.lockFullScreen) {
                        when (orientation) {
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> {
                                requestedOrientation = orientation
                            }
                        }
                    } else {
                        requestedOrientation = orientation
                    }
                }
            })
        }

        player_view.isEnabled = false

//        btn.setOnClickListener {
//            setupPlayUrl("https://cdn-file.pinsewu.com/group1/M00/06/8C/wKgBm11BjXOAWRXKAAA_XDt-0eI56.m3u8_web")
//        }
    }

    override fun onStart() {
        super.onStart()

        if (Util.SDK_INT > 23) {
            setupPlayer()

            player_view.onResume()
        }
    }

    override fun onResume() {
        super.onResume()

        if (viewModel.accountManager.isLogin.value == false && (dialog == null || dialog?.isVisible == false)) {
            openLoginDialog()
        } else {
            loadVideo()
        }

        //hideSystemUi()

        if ((Util.SDK_INT <= 23 || player == null)) {
            setupPlayer()

            player_view.onResume()
        }

        orientationDetector?.also {
            //if (it.canDetectOrientation())
            it.enable()
        }
    }

    override fun onPause() {
        super.onPause()

        dialog?.dismiss()

        orientationDetector?.also {
            it.disable()
        }

        if ((Util.SDK_INT <= 23)) {
            player_view.onPause()

            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()

        if (Util.SDK_INT > 23) {
            player_view.onPause()

            releasePlayer()
        }
    }

    private fun setupPlayer() {
        if (player == null) {
            player = SimpleExoPlayer.Builder(this).build()
            player_view.player = player

            player?.also { player ->
                player.repeatMode = Player.REPEAT_MODE_OFF
                player.playWhenReady = viewModel.isPlaying.value ?: true
                player.seekTo(viewModel.currentWindow, viewModel.playbackPosition)
                player.volume = PlayerViewModel.volume
                player.addListener(playbackStateListener)
                player.addAnalyticsListener(playerAnalyticsListener)

                initTouchListener()
            }
        }
    }

    private fun loadVideo() {
        if (viewModel.currentVideoUrl.value == null) {
            (intent.extras?.getSerializable(KEY_PLAYER_SRC) as PlayerData?)?.also {
                viewModel.videoId = it.videoId
                viewModel.getVideoInfo()
            }
        } else {
            // OnStart or OnResume 設定 video src
            setupPlayUrl(viewModel.currentVideoUrl.value!!)
        }
    }

    private fun setupPlayUrl(url: String) {
        val agent = Util.getUserAgent(this, getString(R.string.app_name))
        val sourceFactory = DefaultDataSourceFactory(this, agent)

        viewModel.getMediaSource(url, sourceFactory)?.also {
            player?.prepare(it, false, false)
        }
    }

    private fun initTouchListener() {
        var originX = 0f
        var originY = 0f
        var isMove = false
        player_view?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //Timber.d("ACTION_DOWN")
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
                            if (abs(dy) > SWIPE_SOUND_LEAST) {
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
                                    val fastForwardMs = (dx.toInt() / SWIPE_DISTANCE_UNIT) * JUMP_TIME
                                    fastForward(fastForwardMs)

                                } else {
                                    val rewindMs = abs((dx.toInt() / SWIPE_DISTANCE_UNIT) * JUMP_TIME)
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
                        player?.also {
                            it.playWhenReady.also { playing ->
                                it.playWhenReady = !playing
                                viewModel.setPlaying(!playing)
                            }
                        }
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

    private fun releasePlayer() {
        player?.also { player ->
            viewModel.playbackPosition = player.currentPosition
            viewModel.currentWindow = player.currentWindowIndex
            viewModel.setPlaying(player.playWhenReady)
            player.removeListener(playbackStateListener)
            player.release()
        }

        player = null
    }

    private fun hideSystemUi() {
        player_view.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun rewind(rewindMs: Int) {
        if (player != null && player!!.isCurrentWindowSeekable && rewindMs > 0) {
            viewModel.setRewindTime(rewindMs)
            val newPos = if (player!!.currentPosition - rewindMs > 0) player!!.currentPosition - rewindMs else 0
            player!!.seekTo(player!!.currentWindowIndex, newPos)
        }
    }

    private fun fastForward(fastForwardMs: Int) {
        if (player != null && player!!.isCurrentWindowSeekable && fastForwardMs > 0) {
            viewModel.setFastForwardTime(fastForwardMs)
            player!!.seekTo(player!!.currentWindowIndex, player!!.currentPosition + fastForwardMs)
        }
    }


    private fun soundUp() {
        player?.also {
            it.volume = it.volume + 0.1f
        }
    }

    private fun soundDown() {
        player?.also {
            it.volume = it.volume - 0.1f
        }
    }

    private val playbackStateListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING"
                ExoPlayer.STATE_READY -> {
                    viewModel.activateLoading(false)
                    player_view.visibility = View.VISIBLE
                    "ExoPlayer.STATE_READY"
                }

                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED"
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
                    viewModel.activateLoading(true)
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

    private val playerAnalyticsListener = object : AnalyticsListener {
        override fun onRenderedFirstFrame(eventTime: AnalyticsListener.EventTime, surface: Surface?) {
            Timber.d("AnalyticsListener onRenderedFirstFrame")
        }

        override fun onDroppedVideoFrames(eventTime: AnalyticsListener.EventTime, droppedFrames: Int, elapsedMs: Long) {
            Timber.d("AnalyticsListener onDroppedVideoFrames")
        }

        override fun onAudioUnderrun(
            eventTime: AnalyticsListener.EventTime,
            bufferSize: Int,
            bufferSizeMs: Long,
            elapsedSinceLastFeedMs: Long
        ) {
            Timber.d("AnalyticsListener onAudioUnderrun")
        }
    }

    private fun openLoginDialog() {
        val registerBlock = {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtras(LoginFragment.createBundle(LoginFragment.TYPE_REGISTER))
            startActivity(intent)
        }

        val loginBlock = {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtras(LoginFragment.createBundle(LoginFragment.TYPE_LOGIN))
            startActivity(intent)
        }

        val data = GeneralDialogData(
            titleRes = R.string.login_yet,
            messageIcon = R.drawable.ico_default_photo,
            message = getString(R.string.login_message),
            firstBtn = getString(R.string.btn_register),
            firstBlock = registerBlock,
            secondBtn = getString(R.string.btn_login),
            secondBlock = loginBlock,
            closeBlock = { finish() }
        )

        dialog = GeneralDialog.newInstance(data).apply {
            isCancelable = false
            show(supportFragmentManager)
        }
    }

    private fun setupChipGroup(list: List<String>?) {
        reflow_group.removeAllViews()

        if (list == null) {
            return
        }

        list.indices.mapNotNull {
            list[it]
        }.forEach {
            val chip = layoutInflater.inflate(R.layout.chip_item, reflow_group, false) as Chip
            chip.text = it

            val isAdult = getIsAdult()

            chip.setTextColor(
                if (isAdult) {
                    R.color.color_white_1_50
                } else {
                    R.color.color_black_1_50
                }.let { colorRes ->
                    getColor(colorRes)
                }
            )

            chip.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this, if (isAdult) {
                        R.color.adult_color_status_bar
                    } else {
                        R.color.color_black_1_10
                    }
                )
            )

            reflow_group.addView(chip)
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
                    GeneralUtils.showToast(this, errorHandler.throwable.toString())
                }
            }
        }
    }

    private fun handleHttpError(errorHandler: ExceptionResult.HttpError) {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.error_device_binding_title,
                message = errorHandler.httpExceptionItem.errorItem.toString(),
                messageIcon = R.drawable.ico_default_photo,
                secondBtn = getString(R.string.btn_confirm)
            )
        ).show(supportFragmentManager)
    }

    private fun showCrashDialog(type: HttpErrorMsgType = HttpErrorMsgType.API_FAILED) {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.error_device_binding_title,
                message = when(type) {
                    HttpErrorMsgType.API_FAILED -> getString(R.string.api_failed_msg)
                    HttpErrorMsgType.CHECK_NETWORK -> getString(R.string.server_error)
                },
                messageIcon = R.drawable.ico_default_photo,
                secondBtn = getString(R.string.btn_close)
            )
        ).show(supportFragmentManager)
    }
}