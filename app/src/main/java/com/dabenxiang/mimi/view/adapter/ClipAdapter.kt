package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.adapter.viewHolder.ClipViewHolder
import com.dabenxiang.mimi.view.player.PlayerViewModel
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
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
    private val getClip: (String, Int) -> Unit,
    private val getCover: (String, Int) -> Unit
) : ListAdapter<MemberPostItem, ClipViewHolder>(DIFF_CALLBACK) {

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

    override fun onBindViewHolder(holder: ClipViewHolder, position: Int) {
        Timber.d("onBindViewHolder position:$position, currentPosition: $currentPosition")
        takeIf { currentPosition == position }?.also { currentViewHolder = holder }
            ?: run { holder.coverView.visibility = View.VISIBLE }
        val item = memberPostItems[position]
        val contentItem = Gson().fromJson(item.content, ContentItem::class.java)
        contentItem.images?.takeIf { it.isNotEmpty() }?.also { images ->
            images[0].also { image ->
                if (TextUtils.isEmpty(image.url)) {
                    image.id.takeIf { !TextUtils.isEmpty(it) }?.also { id ->
                        LruCacheUtils.getLruCache(id)?.also { bitmap ->
                            Glide.with(holder.coverView.context).load(bitmap).into(holder.coverView)
                        } ?: run { getCover(id, position) }
                    }
                } else {
                    Glide.with(holder.coverView.context).load(image.url).into(holder.coverView)
                }
            }
        }

        processClip(
            holder.playerView,
            contentItem.shortVideo.id,
            contentItem.shortVideo.url,
            position
        )
    }

    private fun processClip(playerView: PlayerView, id: String, url: String, position: Int) {
        Timber.d("processClip position:$position")
        val item = memberPostItems[position]
        val contentItem = Gson().fromJson(item.content, ContentItem::class.java)
        playerView.player?.also { it.stop() }

        if (TextUtils.isEmpty(url)) {
            if (clipMap.containsKey(id)) {
                takeIf { currentPosition == position }?.also {
                    setupPlayer(
                        playerView,
                        clipMap[id]?.toURI().toString()
                    )
                }
            } else {
                getClip(id, position)
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
                    currentViewHolder?.coverView?.visibility = View.GONE
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
