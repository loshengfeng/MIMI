package com.dabenxiang.mimi.view.player.ui

import android.os.Bundle
import android.view.Surface
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
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
import com.dabenxiang.mimi.view.base.BasePlayerFragment
import com.dabenxiang.mimi.view.club.post.ClubCommentFragment
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_v2_player.*
import timber.log.Timber
import java.net.UnknownHostException

class PlayerV2Fragment: BasePlayerFragment() {

    companion object {
        private const val KEY_PLAYER_SRC = "KEY_PLAYER_SRC"
        private const val KEY_IS_COMMENT = "KEY_IS_COMMENT"

        fun createBundle(item: PlayerItem, isComment: Boolean = false): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_PLAYER_SRC, item)
                it.putBoolean(KEY_IS_COMMENT, isComment)
            }
        }
    }

    private val viewModel: PlayerV2ViewModel by activityViewModels()

    override fun setupObservers() {
        super.setupObservers()
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
    }

    override fun getViewPagerCount(): Int {
        return 2
    }

    override fun createViewPagerFragment(position: Int): Fragment {
        when(position) {
            0 -> {
                return PlayerDescriptionFragment()
            }
            else -> {
                val memberPostItem = MemberPostItem()
                memberPostItem.id = viewModel.videoContentId
                return ClubCommentFragment.createBundle(memberPostItem, true)
            }
        }
    }

    override fun getTabTitle(tab: TabLayout.Tab, position: Int) {
        when(position) {
            0 -> tab.text = "视频简介"
            1 -> tab.text = "评论"
        }
    }

    override fun onResume() {
        super.onResume()
        getVideoContent()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearLiveData()
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

    /**
     * get video or clip content
     */
    private fun getVideoContent() {
        (arguments?.getSerializable(KEY_PLAYER_SRC) as PlayerItem?)?.also {
            contentId = it.videoId
            viewModel.videoContentId = it.videoId
            viewModel.getVideoContent()
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


}