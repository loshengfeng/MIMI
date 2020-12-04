package com.dabenxiang.mimi.view.clip

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.view.player.PlayerViewModel
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import timber.log.Timber

class ClipAdapter(
    private val context: Context,
    private var currentPosition: Int = 0,
    private var clipFuncItem: ClipFuncItem = ClipFuncItem()
) : PagingDataAdapter<VideoItem, ClipViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<VideoItem>() {
                override fun areItemsTheSame(
                    oldItem: VideoItem,
                    newItem: VideoItem
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: VideoItem,
                    newItem: VideoItem
                ): Boolean {
                    return oldItem == newItem
                }
            }
        const val PAYLOAD_UPDATE_UI = 0
        const val PAYLOAD_UPDATE_AFTER_M3U8 = 1
        const val PAYLOAD_UPDATE_SCROLL_AWAY = 2
        const val ERROR_CODE_ACCOUNT_OVERDUE = 402
    }

    private var currentViewHolder: ClipViewHolder? = null

    private var exoPlayer: SimpleExoPlayer? = null
    private var lastWindowIndex = 0
    private var m3u8Url: String? = null
    private var isOverdue: Boolean = false

    fun getM3U8() {
        getItem(currentPosition)?.run {
            clipFuncItem.getM3U8(this, currentPosition, ::updateAfterM3U8)
        }
    }

    private fun updateAfterM3U8(pos: Int, url: String, errorCode: Int) {
        currentPosition.takeIf { it == pos }?.run {
            setM3U8Result(url, errorCode)
            notifyItemChanged(this, PAYLOAD_UPDATE_AFTER_M3U8)
        }
    }

    private fun setM3U8Result(url: String, errorCode: Int) {
        m3u8Url = url
        isOverdue = errorCode == ERROR_CODE_ACCOUNT_OVERDUE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClipViewHolder {
        return ClipViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_clip,
                parent,
                false
            )
        )
    }

    fun setClipFuncItem(clipFuncItem: ClipFuncItem) {
        this.clipFuncItem = clipFuncItem
    }

    fun getVideoItem(position: Int): VideoItem? {
        return getItem(position)
    }

    fun updateCurrentPosition(position: Int) {
        currentPosition = position
    }

    fun getCurrentPos(): Int {
        return currentPosition
    }

    fun releasePlayer() {
        currentViewHolder?.also {
            it.playerView.hideController()
            it.ivCover.visibility = View.VISIBLE
        }
        exoPlayer?.also { player ->
            player.playWhenReady = false
            player.removeListener(playbackStateListener)
            player.release()
        }
        exoPlayer = null
    }

    fun pausePlayer() {
        exoPlayer?.also { player ->
            player.playWhenReady = false
            currentViewHolder?.also { it.ibPlay.visibility = View.VISIBLE }
        }
    }

    override fun onBindViewHolder(
        holder: ClipViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        Timber.d("onBindViewHolder position:$position, currentPosition: $currentPosition, payloads: $payloads")
        val item = getItem(position) ?: VideoItem()

        payloads.takeIf { it.isNotEmpty() }?.also {
            when (it[0] as Int) {
                PAYLOAD_UPDATE_UI -> {
                    holder.onBind(item, clipFuncItem)
                }
                PAYLOAD_UPDATE_SCROLL_AWAY -> {
                    holder.ivCover.visibility = View.VISIBLE
                    holder.onBind(item, clipFuncItem)
                }
                PAYLOAD_UPDATE_AFTER_M3U8 -> {
                    holder.progress.visibility = View.GONE
                    holder.updateAfterM3U8(item, clipFuncItem, position, isOverdue)
                    takeUnless { isOverdue }?.run {
                        processUpdateAfterM3U8Payload(holder, position)
                    }
                }
            }
        } ?: run {
            holder.onBind(item, clipFuncItem)
            holder.progress.visibility = View.VISIBLE
        }
    }

    override fun onBindViewHolder(holder: ClipViewHolder, position: Int) {
    }

    private fun processUpdateAfterM3U8Payload(holder: ClipViewHolder, position: Int) {
        takeIf { currentPosition == position }?.also {
            currentViewHolder = holder
            holder.progress.visibility = View.VISIBLE
        } ?: run {
            holder.ivCover.visibility = View.VISIBLE
            holder.progress.visibility = View.GONE
        }
        holder.ibReplay.setOnClickListener { view ->
            exoPlayer?.also { player ->
                player.seekTo(0)
                player.playWhenReady = true
            }
            view.visibility = View.GONE
        }
        holder.playerView.setOnClickListener {
            takeIf { exoPlayer?.isPlaying ?: false }?.also {
                exoPlayer?.playWhenReady = false
                holder.ibPlay.visibility = View.VISIBLE
            } ?: run {
                exoPlayer?.playWhenReady = true
                holder.ibPlay.visibility = View.GONE
            }
        }
        holder.ibPlay.setOnClickListener {
            takeUnless { exoPlayer?.isPlaying ?: true }?.also {
                exoPlayer?.playWhenReady = true
                holder.ibPlay.visibility = View.GONE
            }
        }
        holder.btnRetry.setOnClickListener {
            holder.btnRetry.visibility = View.GONE
            holder.progress.visibility = View.VISIBLE
            getM3U8()
        }
        processClip(holder.playerView, m3u8Url, position)
    }

    private fun processClip(playerView: PlayerView, url: String?, position: Int) {
        Timber.d("processClip position:$position, url:$url")
        url?.takeIf { currentPosition == position }?.run {
            releasePlayer()
            setupPlayer(playerView, this)
        }
    }

    private fun setupPlayer(playerView: PlayerView, uri: String) {
        Timber.d("setupPlayer uri:$uri, tag:${playerView.tag}")
        exoPlayer = SimpleExoPlayer.Builder(context).build()
        exoPlayer?.also { player ->
            player.repeatMode = Player.REPEAT_MODE_OFF
            player.playWhenReady = true
            player.volume = PlayerViewModel.volume
            player.addListener(playbackStateListener)
        }
        playerView.player = exoPlayer
        val agent = Util.getUserAgent(context, context.getString(R.string.app_name))
        val sourceFactory = DefaultDataSourceFactory(context, agent)
        GeneralUtils.getMediaSource(uri, sourceFactory)?.also { mediaSource ->
            playerView.player?.also {
                playerView.tag = uri
                (it as SimpleExoPlayer).prepare(mediaSource, true, true)
            }
        }
    }

    private val playbackStateListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE"
                ExoPlayer.STATE_BUFFERING -> {
                    currentViewHolder?.progress?.visibility = View.VISIBLE
                    "ExoPlayer.STATE_BUFFERING"
                }
                ExoPlayer.STATE_READY -> {
                    currentViewHolder?.progress?.visibility = View.GONE
                    currentViewHolder?.ivCover?.visibility = View.GONE
                    "ExoPlayer.STATE_READY"
                }
                ExoPlayer.STATE_ENDED -> {
                    clipFuncItem.scrollToNext(currentPosition + 1)
                    currentViewHolder?.ibReplay?.visibility = View.VISIBLE
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
            val latestWindowIndex: Int = exoPlayer?.currentWindowIndex ?: 0
            if (latestWindowIndex != lastWindowIndex) {
                lastWindowIndex = latestWindowIndex
            }
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

            currentViewHolder?.progress?.visibility = View.GONE
            currentViewHolder?.btnRetry?.visibility = View.VISIBLE
            getVideoItem(currentPosition)?.videoEpisodes?.get(0)?.videoStreams?.get(0)?.id?.also { id ->
                clipFuncItem.onPlayerError(id.toString(), error.message ?: "error: UNKNOWN")
            }


        }
    }
}
