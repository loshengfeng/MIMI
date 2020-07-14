package com.dabenxiang.mimi.view.clip

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.player.PlayerViewModel
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
import com.google.gson.Gson
import timber.log.Timber
import java.io.File

class ClipAdapter(
    private val context: Context,
    private val memberPostItems: ArrayList<MemberPostItem>,
    private val clipMap: HashMap<String, File>,
    private var currentPosition: Int,
    private val clipFuncItem: ClipFuncItem
) : ListAdapter<MemberPostItem, ClipViewHolder>(
    DIFF_CALLBACK
) {

    companion object {
        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<MemberPostItem>() {
                override fun areItemsTheSame(
                    oldItem: MemberPostItem,
                    newItem: MemberPostItem
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: MemberPostItem,
                    newItem: MemberPostItem
                ): Boolean {
                    return oldItem == newItem
                }
            }
        const val PAYLOAD_UPDATE_UI = 0
    }

    private var currentViewHolder: ClipViewHolder? = null

    private var exoPlayer: SimpleExoPlayer? = null
    private var lastWindowIndex = 0

    override fun getItemCount(): Int {
        return memberPostItems.count()
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

    fun updateCurrentPosition(position: Int) {
        currentPosition = position
    }

    fun getCurrentPos(): Int {
        return currentPosition
    }

    fun releasePlayer() {
        exoPlayer?.also { player ->
            player.playWhenReady = false
            player.removeListener(playbackStateListener)
            player.release()
        }
        exoPlayer = null
    }

    override fun onBindViewHolder(holder: ClipViewHolder, position: Int, payloads: MutableList<Any>) {
        Timber.d("onBindViewHolder position:$position, currentPosition: $currentPosition, payloads: $payloads")
        val item = memberPostItems[position]
        val contentItem = Gson().fromJson(item.content, ContentItem::class.java)
        payloads.takeIf { it.isNotEmpty() }?.also {
            when(it[0] as Int) {
                PAYLOAD_UPDATE_UI -> {
                    holder.onBind(item, clipFuncItem, position)
                }
            }
        }?: run {
            holder.onBind(item, clipFuncItem, position)

            takeIf { currentPosition == position }?.also {
                currentViewHolder = holder
                holder.progress.visibility = View.VISIBLE
            } ?: run {
                holder.ivCover.visibility = View.VISIBLE
                holder.progress.visibility = View.GONE
            }

            holder.ibReplay.setOnClickListener {
                exoPlayer?.also { player ->
                    player.seekTo(0)
                    player.playWhenReady = true
                }
                it.visibility = View.GONE
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
                takeIf { exoPlayer?.isPlaying ?: false }?.also {
                    exoPlayer?.playWhenReady = false
                    holder.ibPlay.visibility = View.VISIBLE
                }
            }

            processClip(
                holder.playerView,
                contentItem.shortVideo.id,
                contentItem.shortVideo.url,
                position
            )
        }
    }

    override fun onBindViewHolder(holder: ClipViewHolder, position: Int) {
    }

    private fun processClip(playerView: PlayerView, id: String, url: String, position: Int) {
        Timber.d("processClip position:$position")
        val item = memberPostItems[position]
        val contentItem = Gson().fromJson(item.content, ContentItem::class.java)

        if (TextUtils.isEmpty(url)) {
            if (clipMap.containsKey(id)) {
                takeIf { currentPosition == position}?.also {
                    setupPlayer(
                        playerView,
                        clipMap[id]?.toURI().toString()
                    )
                }
            } else {
                clipFuncItem.getClip(id, position)
            }
        } else {
            takeIf { currentPosition == position }?.also {
                setupPlayer(
                    playerView,
                    contentItem.shortVideo.url
                )
            }
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
        getMediaSource(uri, sourceFactory)?.also { mediaSource ->
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
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING"
                ExoPlayer.STATE_READY -> {
                    currentViewHolder?.progress?.visibility = View.GONE
                    currentViewHolder?.ivCover?.visibility = View.GONE
                    "ExoPlayer.STATE_READY"
                }
                ExoPlayer.STATE_ENDED -> {
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
}
