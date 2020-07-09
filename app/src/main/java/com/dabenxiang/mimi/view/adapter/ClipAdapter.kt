package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.graphics.Bitmap
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
import java.io.File

class ClipAdapter(
    private val context: Context,
    private val memberPostItems: ArrayList<MemberPostItem>,
    private val clipMap: HashMap<Long, File>,
    private val coverMap: HashMap<Long, Bitmap>,
    private var currentPosition: Int,
    private val getClip: (Long, Int) -> Unit,
    private val getCover: (Long, Int) -> Unit
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

    override fun onViewRecycled(holder: ClipViewHolder) {
        super.onViewRecycled(holder)
        holder.playerView.player?.release()
    }

    fun updateCurrentPosition(position: Int) {
        currentPosition = position
    }

    fun getCurrentPos(): Int {
        return currentPosition
    }

    override fun onBindViewHolder(holder: ClipViewHolder, position: Int) {
        Timber.d("onBindViewHolder position:$position, currentPosition: $currentPosition")
        val item = memberPostItems[position]
        val contentItem = Gson().fromJson(item.content, ContentItem::class.java)
        takeIf { contentItem.images.isNotEmpty() }?.also {
            contentItem.images[0].also { image ->
                if (TextUtils.isEmpty(image.url)) {
                    image.id.takeIf { !TextUtils.isEmpty(it) }?.toLong()?.also { id ->
                        takeIf { coverMap.containsKey(id) }?.also {
                            Glide.with(holder.coverView.context).load(coverMap[id]).into(holder.coverView)
                        } ?: run { getCover(id, position) }
                    }
                } else {
                    Glide.with(holder.coverView.context).load(image.url).into(holder.coverView)
                }
            }
        }
        processClip(holder.playerView, contentItem.shortVideo.id, contentItem.shortVideo.url, position)
    }

    private fun processClip(playerView: PlayerView, id: String, url: String, position: Int) {
        Timber.d("processClip position:$position")
        takeUnless { currentPosition == position }?.also { playerView.visibility = View.GONE }
        val item = memberPostItems[position]
        val contentItem = Gson().fromJson(item.content, ContentItem::class.java)
        playerView.player?.also { it.playWhenReady = false }

        if (TextUtils.isEmpty(url)) {
            if (clipMap.containsKey(id.toLong())) {
                takeIf { currentPosition == position }?.also { setupPlayer(playerView, clipMap[id.toLong()]?.toURI().toString()) }
            } else {
                getClip(id.toLong(), position)
            }
        } else {
            takeIf { currentPosition == position }?.also { setupPlayer(playerView, contentItem.shortVideo.url) }
        }
    }

    private fun setupPlayer(playerView: PlayerView, uri: String) {
        Timber.d("setupPlayer uri:$uri, tag:${playerView.tag}")
        takeIf { playerView.player == null || playerView.tag != uri }?.also {
            val exoPlayer = SimpleExoPlayer.Builder(context).build()
            playerView.player = exoPlayer
            exoPlayer.also { player ->
                player.repeatMode = Player.REPEAT_MODE_OFF
                player.playWhenReady = true
                player.volume = PlayerViewModel.volume
            }
            val agent = Util.getUserAgent(context, context.getString(R.string.app_name))
            val sourceFactory = DefaultDataSourceFactory(context, agent)
            getMediaSource(uri, sourceFactory)?.also { mediaSource ->
                playerView.player?.also {
                    playerView.tag = uri
                    (it as SimpleExoPlayer).prepare(mediaSource, true, true)
                }
            }
        }
        playerView.visibility = View.VISIBLE
        playerView.player?.also { it.playWhenReady = true }
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