package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.adapter.viewHolder.ClipViewHolder
import com.dabenxiang.mimi.view.player.PlayerViewModel
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
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

class ClipAdapter(
    val context: Context,
    val memberPostItems: ArrayList<MemberPostItem>,
    val videoMap: HashMap<Long, Bitmap>,
    var currentPosition: Int
) : ListAdapter<MemberPostItem, ClipViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<MemberPostItem>() {
                override fun areItemsTheSame(
                    oldItem: MemberPostItem,
                    newItem: MemberPostItem
                ): Boolean {
                    return false
                }

                override fun areContentsTheSame(
                    oldItem: MemberPostItem,
                    newItem: MemberPostItem
                ): Boolean {
                    return false
                }
            }
    }

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

    override fun onBindViewHolder(holder: ClipViewHolder, position: Int) {
        val item = memberPostItems[position]
        val contentItem = Gson().fromJson(item.content, ContentItem::class.java)
        Timber.d("@@onBindViewHolder position:$position, content: $contentItem")
        if (currentPosition == position) {
            processClip(holder.playerView, position)
        } else {
            holder.playerView.player?.also { it.playWhenReady = false }
        }
    }

    private fun processClip(playerView: PlayerView, position: Int) {
        Timber.d("@@@processClip position:$position")
        val item = memberPostItems[position]
        val contentItem = Gson().fromJson(item.content, ContentItem::class.java)
        if (TextUtils.isEmpty(contentItem.shortVideo.url)) {
            playerView.player?.also { (it as SimpleExoPlayer).stop() }
        } else {
            setupPlayer(playerView, contentItem.shortVideo.url)
        }
    }

    private fun setupPlayer(playerView: PlayerView, url: String) {
        Timber.d("@@@setupPlayer url:$url")
        if (playerView.player == null) {
            val exoPlayer = SimpleExoPlayer.Builder(context).build()
            playerView.player = exoPlayer
            exoPlayer.also { player ->
                player.repeatMode = Player.REPEAT_MODE_OFF
                player.playWhenReady = true
                player.volume = PlayerViewModel.volume
//            it.seekTo(viewModel.currentWindow, viewModel.playbackPosition)
//            it.addListener(playbackStateListener)
//            it.addAnalyticsListener(playerAnalyticsListener)
//
//            initTouchListener()
            }
        }
        playerView.player?.also { (it as SimpleExoPlayer).stop() }
        val agent = Util.getUserAgent(context, context.getString(R.string.app_name))
        val sourceFactory = DefaultDataSourceFactory(context, agent)
        getMediaSource(url, sourceFactory)?.also { mediaSource ->
            playerView.player?.also { (it as SimpleExoPlayer).prepare(mediaSource) }
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