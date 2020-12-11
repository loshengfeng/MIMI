package com.dabenxiang.mimi.view.post.viewer

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.VideoView
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.vo.ViewerItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.video.trimmer.interfaces.OnVideoListener
import kotlinx.android.synthetic.main.fragment_post_viewer.*
import timber.log.Timber


class PostViewerFragment : BaseFragment(), OnVideoListener {

    var mediaController: MediaController? = null

    companion object {
        const val VIEWER_DATA = "viewer_data"
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId(): Int {
        return R.layout.fragment_post_viewer
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewerItem = arguments?.getSerializable(VIEWER_DATA) as ViewerItem
        if (!viewerItem.isVideo) {
            if (viewerItem.attachmentId.isBlank()) {
                val uriP = Uri.parse(viewerItem.url)
                iv_cover.setImageURI(uriP)
            } else {
                LruCacheUtils.getLruCache(viewerItem.attachmentId)?.also { bitmap ->
                    Glide.with(requireContext()).load(bitmap).into(iv_cover)
                }
            }
        } else {
            initVideoView(viewerItem.url)
        }

        ib_back.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun setupObservers() {
    }

    override fun setupListeners() {
    }

    fun initVideoView(path: String) {
        videoView.visibility = View.VISIBLE
        videoView.setMediaController(null)
        mediaController = MediaController (requireContext())
        //設定videoview的控制條
        videoView.setMediaController(mediaController);
        videoView.post{
            //設定顯示控制條
            mediaController?.show(0);
        }
        //設定播放完成以後監聽
        videoView.setOnCompletionListener {
            Timber.d("onCompletion ")
        }
        //設定發生錯誤監聽，如果不設定videoview會向用戶提示發生錯誤
        videoView.setOnErrorListener { mp, what, extra ->
            Timber.d("onError = $what")
            false
        }
        //設定在視訊檔案在載入完畢以後的回撥函式
        videoView.setOnPreparedListener { Timber.d("neo onPrepared ") }
        //設定videoView的點選監聽
        videoView.setVideoPath(path)
    }

    override fun onVideoPrepared() {
        Timber.d("onVideoPrepared")
    }
}