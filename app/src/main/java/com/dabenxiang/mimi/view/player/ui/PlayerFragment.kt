package com.dabenxiang.mimi.view.player.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.*
import android.view.View.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearSnapHelper
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.extension.addKeyboardToggleListener
import com.dabenxiang.mimi.extension.handleException
import com.dabenxiang.mimi.extension.setBtnSolidColor
import com.dabenxiang.mimi.extension.setNot
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.*
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.model.vo.StatusItem
import com.dabenxiang.mimi.view.adapter.TopTabAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.*
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.player.*
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.view.topup.TopUpFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.OrientationDetector
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.custom_playback_control.*
import kotlinx.android.synthetic.main.head_comment.*
import kotlinx.android.synthetic.main.head_guess_like.*
import kotlinx.android.synthetic.main.head_no_comment.*
import kotlinx.android.synthetic.main.head_source.*
import kotlinx.android.synthetic.main.head_video_info.*
import kotlinx.android.synthetic.main.item_ad.*
import kotlinx.android.synthetic.main.recharge_reminder.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.round

class PlayerFragment : BaseFragment() {

    companion object {
        const val KEY_IS_FROM_PLAYER = "KEY_IS_FROM_PLAYER"
        const val KEY_DEST_ID = "DEST_ID"
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

    private val viewModel: PlayerViewModel by viewModels()

    private var player: SimpleExoPlayer? = null
    private var orientationDetector: OrientationDetector? = null
    private var dialog: GeneralDialog? = null
    private var consumeDialog: GeneralDialog? = null

    private var loadReplyCommentBlock: (() -> Unit)? = null
    private var loadCommentLikeBlock: (() -> Unit)? = null
    private var currentReplyId: Long? = null
    private var currentreplyName: String? = null
    private var moreDialog: MoreDialogFragment? = null
    private var reportDialog: ReportDialogFragment? = null
    private var isFirstInit = true
    private var isKeyboardShown = false
    private var oldPlayerItem: PlayerItem = PlayerItem(-1)
    override val bottomNavigationVisibility = View.GONE

    private val sourceListAdapter by lazy {
        TopTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                Timber.i("TopTabAdapter onClickItemIndex")
                viewModel.setSourceListPosition(index)
            }
        })
    }

    private val episodeAdapter by lazy {
        SelectEpisodeAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                Timber.i("SelectEpisodeAdapter onClickItemIndex $index")
                viewModel.setStreamPosition(index)
            }
        })
    }

    private val guessLikeAdapter by lazy {
        GuessLikeAdapter(object :
            GuessLikeAdapter.GuessLikeAdapterListener {
            override fun onVideoClick(view: View, item: PlayerItem) {
                oldPlayerItem = PlayerItem(viewModel.videoId)
                reloadVideoInfo(item)
            }
        })
    }

    private val playerInfoAdapter by lazy {
        Timber.i("playerInfoAdapter")
        CommentAdapter(object : CommentAdapter.PlayerInfoListener {
            override fun sendComment(replyId: Long?, replyName: String?) {
                viewModel.checkStatus {
                    Timber.i("playerInfoAdapter sendComment")
                    currentReplyId = null
                    currentreplyName = null
                    if (replyId != null) {
                        currentReplyId = replyId
                        currentreplyName = replyName
                        commentEditorOpen()
                    }
                }
            }

            override fun expandReply(parentNode: RootCommentNode, succeededBlock: () -> Unit) {
                Timber.i("playerInfoAdapter expandReply")
                loadReplyCommentBlock = succeededBlock
                parentNode.data.id?.also {
                    viewModel.loadReplyComment(parentNode, it)
                }
            }

            override fun replyComment(replyId: Long?, replyName: String?) {
                Timber.i("playerInfoAdapter replyComment")
                currentReplyId = null
                currentreplyName = null
                if (replyId != null) {
                    currentReplyId = replyId
                    currentreplyName = replyName
                    commentEditorOpen()
                }
            }

            override fun setCommentLikeType(
                replyId: Long?,
                isLike: Boolean,
                succeededBlock: () -> Unit
            ) {
                viewModel.checkStatus {
                    Timber.i("playerInfoAdapter setCommentLikeType")
                    loadCommentLikeBlock = succeededBlock
                    replyId?.also {
                        val type = if (isLike) 0 else 1
                        viewModel.postCommentLike(replyId, PostLikeRequest(type))
                    }
                }
            }

            override fun removeCommentLikeType(replyId: Long?, succeededBlock: () -> Unit) {
                viewModel.checkStatus {
                    loadCommentLikeBlock = succeededBlock
                    replyId?.also {
                        viewModel.deleteCommentLike(replyId)
                    }
                }
            }

            override fun onMoreClick(item: MembersPostCommentItem) {
                Timber.i("playerInfoAdapter onMoreClick")
                if (item.id != null) {
                    viewModel.isCommentReport = true
                    showMoreDialog(item.id, PostType.VIDEO, item.reported ?: false, true)
                }
            }

            override fun onAvatarClick(userId: Long, name: String) {
                Timber.d("onAvatarClick nav to member post with userId: $userId and name: $name")
                val bundle = MyPostFragment.createBundle(
                    userId, name,
                    isAdult = true,
                    isAdultTheme = false
                )
                bundle.putBoolean(KEY_IS_FROM_PLAYER, true)
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_playerFragment_to_myPostFragment,
                        bundle
                    )
                )
            }

            override fun loadAvatar(id: Long?, view: ImageView) {
                viewModel.loadImage(id, view, LoadImageType.AVATAR)
            }
        }, CommentViewType.VIDEO).apply {
            loadMoreModule.apply {
                isEnableLoadMore = true
                isAutoLoadMore = true
                isEnableLoadMoreIfNotFullPage = false
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_player
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    override fun setUpStatusBarColor() {
        activity?.also {
            (it as MainActivity).window.statusBarColor =
                requireContext().getColor(R.color.adult_color_status_bar)
        }
    }

    private fun setupUI() {

        if (recycler_info.parent != null) {
            Timber.i("recyclerInfo removeView")
            playerInfoAdapter.removeAllHeaderView()
        }

        playerInfoAdapter.loadMoreModule.loadMoreView =
            CommentLoadMoreView(CommentViewType.VIDEO)

        recycler_info.adapter = playerInfoAdapter


        val backgroundColor = requireContext().getColor(R.color.normal_color_background)
        recycler_info.setBackgroundColor(backgroundColor)

        val titleColor = requireContext().getColor(R.color.normal_color_text)

        tv_title.setTextColor(titleColor)
        title_source.setTextColor(titleColor)
        title_guess_like.setTextColor(titleColor)
        title_comment.setTextColor(titleColor)
        title_no_comment.setTextColor(titleColor)

        val subTitleColor = requireContext().getColor(R.color.color_black_1_50)

        btn_show_introduction.setTextColor(subTitleColor)
        tv_introduction.setTextColor(subTitleColor)
        tv_info.setTextColor(subTitleColor)
        tv_introduction.setBackgroundResource(R.drawable.bg_black_stroke_1_radius_2)

        recyclerview_source_list.adapter = sourceListAdapter
        recyclerview_episode.adapter = episodeAdapter

        recyclerview_guess_like.adapter = guessLikeAdapter

        if (firstCreateView) {
            LinearSnapHelper().attachToRecyclerView(recyclerview_guess_like)
        }

        bottom_func_bar.setBackgroundResource(R.drawable.bg_gray_2_top_line)
        bottom_func_input.setBackgroundResource(R.drawable.bg_gray_2_top_line)

        btn_write_comment.let {
            it.setTextColor(
                requireContext().getColor(R.color.color_gray_9)
            )
            it.background = requireContext().getDrawable(R.drawable.bg_gray_1_30_radius_18)
        }

        tv_comment.setCompoundDrawablesRelativeWithIntrinsicBounds(
            R.drawable.ico_messege_adult_gray,
            0,
            0,
            0
        )
        et_message.let {
            it.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ico_messege_adult_gray,
                0,
                0,
                0
            )
            it.compoundDrawablePadding = 5
            it.setBackgroundColor(requireContext().getColor(R.color.color_gray_1))
            it.setTextColor(requireContext().getColor(R.color.color_black_1))
            it.setHintTextColor(requireContext().getColor(R.color.color_gray_9))
        }

        iv_bar.setImageResource(R.drawable.bg_gray_1_30_radius_18)

        tv_like.setTextColor(requireContext().getColor(R.color.normal_color_text))
        iv_favorite.setTextColor(requireContext().getColor(R.color.normal_color_text))
        tv_comment.setTextColor(requireContext().getColor(R.color.normal_color_text))

        iv_share.setImageResource(R.drawable.btn_share_gray_n)
        iv_more.setImageResource(R.drawable.btn_more_gray_n)

        tv_replay_name.setTextColor(requireContext().getColor(R.color.color_black_1))

        val adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        val adHeight = (adWidth * 0.142).toInt()
        viewModel.getAd(adWidth, adHeight)

    }

    override fun setupObservers() {
        viewModel.showIntroduction.observe(viewLifecycleOwner, Observer { isShow ->
            val drawableRes =
                if (isShow) R.drawable.btn_arrowup_gray_n
                else R.drawable.btn_arrowdown_gray_n
            tv_introduction.visibility = if (isShow) View.VISIBLE else View.GONE
            btn_show_introduction.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                drawableRes,
                0
            )
        })

        viewModel.fastForwardTime.observe(viewLifecycleOwner, Observer {
            tv_forward_backward.text = "${if (it > 0) "+" else ""}${it / 1000}秒"
        })

        viewModel.soundLevel.observe(viewLifecycleOwner, Observer {
            //Timber.d("sound level: $it")
            val soundLevel = round(it * 10) * 10
            tv_sound_tune.text = "音量${soundLevel.toInt()}%"
        })

        viewModel.isLoadingActive.observe(viewLifecycleOwner, Observer {
            if (it) {
                progress_video.visibility = VISIBLE
            } else {
                progress_video.visibility = View.GONE
            }
        })

        viewModel.isPlaying.observe(viewLifecycleOwner, Observer {
            iv_player.visibility = when (it) {
                true -> View.GONE
                false -> VISIBLE
            }
        })

        viewModel.sourceListPosition.observe(viewLifecycleOwner, Observer {
            if (it == -1) return@Observer
            sourceListAdapter.setLastSelectedIndex(it)
            viewModel.sourceList?.get(it)?.videoEpisodes?.also { videoEpisodes ->
                Timber.i("videoEpisodes =$videoEpisodes")
                setupStream(videoEpisodes)
            }

            scrollToBottom()
        })

        viewModel.episodePosition.observe(viewLifecycleOwner, Observer {
            Timber.i("episodePosition =$it")
            if (it >= 0) {
                episodeAdapter.setLastSelectedIndex(it)
                viewModel.getAdultStreamUrl()
            }
            scrollToBottom()
        })

        viewModel.isPageCallback.observe(viewLifecycleOwner, Observer {
            if (arguments?.getSerializable(KEY_IS_COMMENT) == true) {
                CoroutineScope(Dispatchers.Main).launch { scrollToCommentArea() }
            }
        })

        viewModel.apiStreamResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Empty -> loadVideo()
                is Error -> {
                    when (it.throwable) {
                        is PlayerViewModel.NotDeductedException -> {
                            showRechargeReminder(true)
                            scrollToBottom()
                        }
                        else -> onApiError(it.throwable)
                    }
                }
            }
        })

        viewModel.apiPostCommentResult.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Loading -> progressHUD?.show()
                    is Loaded -> progressHUD?.dismiss()
                    is Empty -> {
                        currentReplyId = null
                        currentreplyName = null
                        title_no_comment.visibility = View.GONE
                        viewModel.commentCount.value = viewModel.commentCount.value?.plus(1)

                        viewModel.setupCommentDataSource(playerInfoAdapter)
                        commentEditorToggle(false)
                        scrollToBottom()
                    }
                    is Error -> {
                        commentEditorToggle(false)
                        onApiError(it.throwable)
                    }
                }
            }
        })

        viewModel.apiCommentLikeResult.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Loading -> progressHUD?.show()
                    is Loaded -> progressHUD?.dismiss()
                    is Empty -> {
                        loadCommentLikeBlock = loadCommentLikeBlock?.let {
                            it()
                            null
                        }
                        scrollToBottom()
                    }
                    is Error -> onApiError(it.throwable)
                }
            }
        })

        viewModel.apiDeleteCommentLikeResult.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Loading -> progressHUD?.show()
                    is Loaded -> progressHUD?.dismiss()
                    is Empty -> loadCommentLikeBlock?.also { it() }
                    is Error -> onApiError(it.throwable)
                }
            }
        })

        viewModel.apiVideoInfo.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> {
                    val result = it.result
                    viewModel.category =
                        if (result.categories.isNotEmpty()) result.categories.get(0) else ""

                    if (isFirstInit) {
                        isFirstInit = false
                        tv_title.text = result.title

                        if (!result.description.isNullOrBlank())
                            tv_introduction.text =
                                Html.fromHtml(result.description, Html.FROM_HTML_MODE_COMPACT)

                        val dateString = result.updateTime?.let { date ->
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                        }

                        tv_info.text = String.format(
                            getString(R.string.player_info_format),
                            dateString ?: "",
                            result.country
                        )

                        viewModel.sourceList = result.sources

                        if (result.tags != null)
                            setupChipGroup(result.tags as List<String>)

                        setupSourceList(viewModel.sourceList)

                        val categoriesString =
                            if (result.categories.isNotEmpty()) result.categories.last() else ""
                        viewModel.setupGuessLikeList(categoriesString, true)
                    }

                    viewModel.likeVideo.value = result.like
                    viewModel.likeVideoCount.value = result.likeCount
                    viewModel.favoriteVideo.value = result.favorite
                    viewModel.favoriteVideoCount.value = result.favoriteCount
                    viewModel.commentCount.value = result.commentCount

                    viewModel.isDeducted = result.deducted ?: false
                    viewModel.costPoint = result.point ?: 0L
                    viewModel.availablePoint = result.availablePoint ?: 0L

                    if (result.commentCount == 0L) {
                        Timber.i(" apiVideoInfo result.commentCount == 0L")
                        title_no_comment.visibility = VISIBLE
                        title_no_comment.setTextColor(requireContext().getColor(R.color.normal_color_text))
                        val bgColor = requireContext().getColor(R.color.color_black_1_10)
                        title_no_comment.setBtnSolidColor(
                            bgColor,
                            bgColor,
                            resources.getDimension(R.dimen.dp_10)
                        )
                    } else {
                        title_no_comment.visibility = View.GONE

                        lifecycleScope.launchWhenResumed {
                            viewModel.setupCommentDataSource(playerInfoAdapter)
                        }
                    }
                }
                is Error -> onApiError(it.throwable)
            }
            scrollToBottom()
        })

        viewModel.isSelectedNewestComment.observe(viewLifecycleOwner, Observer {
            if (it) {
                tv_newest.setTextColor(requireContext().getColor(R.color.color_red_1))
                tv_hottest.setTextColor(requireContext().getColor(R.color.color_black_1_50))
            } else {
                tv_newest.setTextColor(requireContext().getColor(R.color.color_black_1_50))
                tv_hottest.setTextColor(requireContext().getColor(R.color.color_red_1))
            }
        })

        tv_newest.setOnClickListener {
            viewModel.updatedSelectedNewestComment(true)
            lifecycleScope.launch {
                viewModel.setupCommentDataSource(playerInfoAdapter)
            }
        }

        tv_hottest.setOnClickListener {
            viewModel.updatedSelectedNewestComment(false)
            lifecycleScope.launch {
                viewModel.setupCommentDataSource(playerInfoAdapter)
            }
        }

        viewModel.likeVideo.observe(viewLifecycleOwner, Observer {
            val res = when (it) {
                true -> R.drawable.ico_nice_s
                else -> R.drawable.ico_nice_gray
            }

            tv_like.setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)
        })

        viewModel.likeVideoCount.observe(viewLifecycleOwner, Observer {
            tv_like.text = it.toString()
        })

        viewModel.favoriteVideo.observe(viewLifecycleOwner, Observer {
            val res = when (it) {
                true -> R.drawable.btn_favorite_white_s
                else -> R.drawable.btn_favorite_n
            }

            iv_favorite.setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)
        })

        viewModel.favoriteVideoCount.observe(viewLifecycleOwner, Observer {
            iv_favorite.text = it.toString()
        })

        viewModel.commentCount.observe(viewLifecycleOwner, Observer {
            tv_comment.text = it.toString()
        })

        viewModel.consumeResult.observe(viewLifecycleOwner, Observer {
            Timber.i("consumeResult isDeducted:${viewModel.isDeducted}")
            Timber.i("consumeResult VideoConsumeResult:$it")
            consumeDialog?.dismiss()
            when (it) {
                VideoConsumeResult.PAID_YET,
                VideoConsumeResult.PAID -> {
                    showRechargeReminder(false)
                    viewModel.getAdultStreamUrl()
                }
                VideoConsumeResult.POINT_NOT_ENOUGH -> {
                    showRechargeReminder(true)
                }
            }

            scrollToBottom()
        })

        viewModel.apiLoadReplyCommentResult.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.also { apiResult ->
                when (apiResult) {
                    is Loading -> progressHUD?.show()
                    is Loaded -> {
                        loadReplyCommentBlock = null
                        progressHUD?.dismiss()
                    }
                    is Empty -> {
                        loadReplyCommentBlock?.also {
                            it()
                        }
                    }
                }
            }
        })

        viewModel.videoList.observe(viewLifecycleOwner, Observer {
            guessLikeAdapter.submitList(it)
            scrollToBottom()
        })

        viewModel.recyclerViewGuessLikeVisible.observe(viewLifecycleOwner, Observer {
            title_guess_like.visibility = it
            recyclerview_guess_like.visibility = it

            scrollToBottom()
        })

        viewModel.apiAddFavoriteResult.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.also { apiResult ->
                when (apiResult) {
                    is Loading -> progressHUD?.show()
                    is Loaded -> {
                        progressHUD?.dismiss()
                        viewModel.favoriteVideo.value = !(viewModel.favoriteVideo.value ?: false)
                        viewModel.favoriteVideoCount.value =
                            if (viewModel.favoriteVideo.value == true) viewModel.favoriteVideoCount.value?.plus(
                                1
                            ) else viewModel.favoriteVideoCount.value?.minus(1)
                    }
                    is Error -> onApiError(apiResult.throwable)
                }
            }
        })

        viewModel.apiAddLikeResult.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.also { apiResult ->
                when (apiResult) {
                    is Loading -> progressHUD?.show()
                    is Loaded -> {
                        progressHUD?.dismiss()
                        viewModel.likeVideo.value = !(viewModel.likeVideo.value ?: false)
                        viewModel.likeVideoCount.value =
                            if (viewModel.likeVideo.value == true) viewModel.likeVideoCount.value?.plus(
                                1
                            ) else viewModel.likeVideoCount.value?.minus(1)
                    }
                    is Error -> onApiError(apiResult.throwable)
                }
            }
        })

        viewModel.apiReportResult.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.also { apiResult ->
                when (apiResult) {
                    is Loading -> progressHUD?.show()
                    is Loaded -> progressHUD?.dismiss()
                    is Empty -> {
                        if (!viewModel.isCommentReport) {
                            viewModel.isReported = true
                        } else {
                            viewModel.isCommentReport = false
                            viewModel.setupCommentDataSource(playerInfoAdapter)
                        }
                        GeneralUtils.showToast(requireContext(), getString(R.string.report_success))
                    }
                    is Error -> onApiError(apiResult.throwable)
                }
            }
        })

        viewModel.getAdResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    Glide.with(this)
                        .load(it.result.href)
                        .into(iv_ad)

                    iv_ad.setOnClickListener { view ->
                        GeneralUtils.openWebView(requireContext(), it.result.target)
                    }
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.checkStatusResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    when (it.result.status) {
                        StatusItem.NOT_LOGIN -> showNotLoginDialog()
                        StatusItem.LOGIN_BUT_EMAIL_NOT_CONFIRMED -> {
                            iv_player.visibility = VISIBLE
                            exo_play_pause.setImageDrawable(requireContext().getDrawable(R.drawable.exo_icon_play))
                            showEmailConfirmDialog()
                        }
                        StatusItem.LOGIN_AND_EMAIL_CONFIRMED -> it.result.onLoginAndEmailConfirmed()
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.videoReport.observe(viewLifecycleOwner) {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> {
                    Timber.i("videoReported")
                }
                is Error -> onApiError(it.throwable)
            }
        }

        viewModel.showRechargeReminder.observe(viewLifecycleOwner) {
            showRechargeReminder(it)
        }
    }

    override fun setupListeners() {
        requireActivity().onBackPressedDispatcher.addCallback(
            owner = viewLifecycleOwner,
            onBackPressed = {
                if (requireActivity().requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    navigateTo(NavigateItem.Up)
                } else {
                    viewModel.lockFullScreen = !viewModel.lockFullScreen
                    switchScreenOrientation()
                }
            }
        )

        btn_show_introduction.setOnClickListener {
            viewModel.showIntroduction.setNot()
        }

        btn_full_screen.setOnClickListener {
            viewModel.lockFullScreen = !viewModel.lockFullScreen
            switchScreenOrientation()
        }

        btn_write_comment.setOnClickListener {
            viewModel.checkStatus {
                Timber.d("onWriteCommentClick confirmed")
                currentReplyId = null
                currentreplyName = null
                commentEditorOpen()
                commentEditorToggle(true)
            }
        }

        tv_comment.setOnClickListener {
            scrollToCommentArea()
        }

        btn_send.setOnClickListener {
            if (et_message.text.isNotEmpty()) {
                viewModel.postComment(
                    PostCommentRequest(
                        currentReplyId,
                        et_message.text.toString()
                    )
                )
                currentReplyId = null
                currentreplyName = null
                et_message.text.clear()
            }
        }

        iv_favorite.setOnClickListener {
            viewModel.checkStatus {
                Timber.d("onFavoriteClick confirmed")
                viewModel.modifyFavorite()
            }
        }

        tv_like.setOnClickListener {
            viewModel.checkStatus {
                Timber.d("like confirmed")
                viewModel.modifyLike()
            }
        }

        iv_share.setOnClickListener {
            viewModel.checkStatus {
                Timber.d("share confirmed")
                GeneralUtils.copyToClipboard(
                    requireContext(),
                    viewModel.getShareUrl(
                        viewModel.category,
                        viewModel.videoId,
                        viewModel.episodeId.toString()
                    )
                )
                GeneralUtils.showToast(requireContext(), getString(R.string.copy_url))
            }
        }

        iv_more.setOnClickListener {
            Timber.i("viewModel.isReported ${viewModel.isReported}")
            showMoreDialog(viewModel.streamId, PostType.VIDEO, viewModel.isReported)
        }

        requireActivity().addKeyboardToggleListener { shown ->
            isKeyboardShown = shown
            if (!shown && activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                commentEditorToggle(false)
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
            if (it.visibility == VISIBLE) {
                player?.playWhenReady = true
                viewModel.setPlaying(true)
                exo_play_pause.setImageDrawable(requireContext().getDrawable(R.drawable.exo_icon_pause))
            }
        }
        recycler_info.setOnClickListener {
            Timber.i("RecyclerView=setOnClickListener")
        }

        btn_vip.setOnClickListener {
            Timber.i("btn_vip Click")
            val bundle = TopUpFragment.createBundle(this::class.java.simpleName)
            navigateTo(NavigateItem.Destination(R.id.action_to_topup, bundle))
//            mainViewModel?.switchTab?.value = 1
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
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun showRechargeReminder(isShow: Boolean) {
        Timber.i("showRechargeReminder")
        player_view.visibility = if (isShow) INVISIBLE else VISIBLE
        recharge_reminder.visibility = if (isShow) VISIBLE else GONE
    }

    private fun showMoreDialog(
        id: Long,
        type: PostType,
        isReported: Boolean,
        isComment: Boolean = false
    ) {
        Timber.i("id=$id")
        Timber.i("isReported=$isReported")
        moreDialog = MoreDialogFragment.newInstance(
            MemberPostItem(
                id = id,
                type = type,
                reported = isReported
            ), onMoreDialogListener, isComment
        ).also {
            it.show(
                requireActivity().supportFragmentManager,
                MoreDialogFragment::class.java.simpleName
            )
        }
    }

    private val onReportDialogListener = object : ReportDialogFragment.OnReportDialogListener {
        override fun onSend(item: BaseMemberPostItem, content: String, postItem: MemberPostItem?) {
            if (TextUtils.isEmpty(content)) {
                GeneralUtils.showToast(App.applicationContext(), getString(R.string.report_error))
            } else {
                when (item) {
                    is MemberPostItem -> {
                        if (item.type == PostType.VIDEO)
                            viewModel.sendVideoReport(item.id, content)
                        else
                            viewModel.sentReport(item.id, content)
                    }
                }
            }
            reportDialog?.dismiss()
        }

        override fun onCancel() {
            Timber.i("reportDialog onCancel reportDialog=$reportDialog ")
            reportDialog?.dismiss()
        }
    }

    private val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
        override fun onProblemReport(item: BaseMemberPostItem, isComment: Boolean) {
            viewModel.checkStatus {
                if ((item as MemberPostItem).reported) {
                    viewModel.isCommentReport = false
                    GeneralUtils.showToast(
                        App.applicationContext(),
                        getString(R.string.already_reported)
                    )
                } else {
                    reportDialog =
                        ReportDialogFragment.newInstance(
                            item = item,
                            listener = onReportDialogListener,
                            isComment = isComment
                        ).also {
                            it.show(
                                requireActivity().supportFragmentManager,
                                ReportDialogFragment::class.java.simpleName
                            )
                        }
                }
                moreDialog?.dismiss()
            }
        }

        override fun onCancel() {
            moreDialog?.dismiss()
        }
    }

    private fun commentEditorToggle(enable: Boolean) {
        Timber.i("commentEditorToggle enable:$enable")
        when (enable) {
            true -> {
                bottom_func_input?.visibility = VISIBLE
                bottom_func_bar?.visibility = View.GONE
            }
            else -> {
                bottom_func_input?.visibility = View.GONE
                bottom_func_bar?.visibility = VISIBLE
            }
        }

    }

    private fun commentEditorOpen() {
        CoroutineScope(Dispatchers.Main).launch {

            tv_replay_name.let {
                if (currentreplyName == null) {
                    tv_replay_name.visibility = View.GONE
                } else {
                    tv_replay_name.text = "@$currentreplyName"
                    tv_replay_name.visibility = VISIBLE
                }
            }

            et_message.let {
                it.requestFocusFromTouch()
                val lManager: InputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                lManager?.showSoftInput(it, 0)
            }
        }
        commentEditorToggle(true)
    }

    private fun commentEditorHide() {
        Timber.i("commentEditorHide")
        CoroutineScope(Dispatchers.Main).launch {
            tv_replay_name?.visibility = View.GONE
            val lManager: InputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            lManager?.hideSoftInputFromWindow(et_message.windowToken, 0)
        }
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

        loadVideo()

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
        Timber.d("player activity onPause")
        super.onPause()

        dialog?.dismiss()

        orientationDetector?.also {
            it.disable()
        }

        player_view.onPause()
        releasePlayer()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        player_view.onPause()
        releasePlayer()
        dialog = null

        consumeDialog = null
        loadReplyCommentBlock = null
        loadCommentLikeBlock = null
//        viewModel.deleteCacheFile()
    }

    private fun setupPlayer() {
        if (player == null) {
            player = SimpleExoPlayer.Builder(requireContext()).build()
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

    private fun loadVideo(
        playerItem: PlayerItem = PlayerItem(-1)
    ) {
        if (playerItem.videoId != -1L) {
            viewModel.videoId = playerItem.videoId
            viewModel.getVideoInfo()
        } else if (viewModel.nextVideoUrl == null) {
            if (viewModel.apiVideoInfo.value == null) {
                (arguments?.getSerializable(KEY_PLAYER_SRC) as PlayerItem?)?.also {
                    viewModel.videoId = it.videoId
                    viewModel.getVideoInfo()
                }
            }
        } else {
            // OnStart or OnResume 設定 video src
            var isReset = false
            if (viewModel.nextVideoUrl != viewModel.currentVideoUrl) {
                isReset = true
                viewModel.currentVideoUrl = viewModel.nextVideoUrl!!
            }
            Timber.d("Niel url ${viewModel.currentVideoUrl}")
            setupPlayUrl(viewModel.currentVideoUrl!!, isReset)
        }
    }

    private fun setupPlayUrl(url: String, isReset: Boolean) {
        val agent = Util.getUserAgent(requireContext(), getString(R.string.app_name))
        val sourceFactory = DefaultDataSourceFactory(requireContext(), agent)

//        viewModel.downloadM3U8(url)

        viewModel.getMediaSource(url, sourceFactory)?.also {
            Timber.d("player ready confirmed")
            player?.prepare(it, isReset, isReset)
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
                                tv_forward_backward.visibility = VISIBLE
                                tv_sound_tune.visibility = View.GONE
                                if (dx > 0)
                                    viewModel.setFastForwardTime((dx.toInt() / SWIPE_DISTANCE_UNIT) * JUMP_TIME)
                                else
                                    viewModel.setRewindTime(abs((dx.toInt() / SWIPE_DISTANCE_UNIT) * JUMP_TIME))
                            }
                        } else {
                            tv_forward_backward.visibility = View.GONE
                            tv_sound_tune.visibility = VISIBLE
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
                        //                        player?.also {
                        //                            it.playWhenReady.also { playing ->
                        //                                it.playWhenReady = !playing
                        //                                viewModel.setPlaying(!playing)
                        //                            }
                        //                        }
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

        var startClickTime: Long = 0
        recycler_info.setOnTouchListener { v, event ->
            Timber.i("RecyclerView=setOnTouchListener isKeyboardShown=$isKeyboardShown")
            if (!isKeyboardShown) return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startClickTime = System.currentTimeMillis()
                }

                MotionEvent.ACTION_UP -> {
                    var clickDuration: Long = System.currentTimeMillis()
                    if (clickDuration - startClickTime < 100) {
                        commentEditorHide()
                        commentEditorToggle(false)
                    }
                }
            }
            false
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
            val newPos =
                if (player!!.currentPosition - rewindMs > 0) player!!.currentPosition - rewindMs else 0
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
                    player_view.visibility = VISIBLE
                    "ExoPlayer.STATE_READY"
                }

                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED"
                else -> "UNKNOWN_STATE"
            }
            Timber.d("Changed state to $stateString playWhenReady: $playWhenReady")
            if (playbackState == ExoPlayer.STATE_ENDED && (viewModel.episodePosition.value!! < episodeAdapter.itemCount - 1)) {
                viewModel.setStreamPosition(viewModel.episodePosition.value!! + 1)
            }
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            Timber.d("onLoadingChanged")
        }

        override fun onPositionDiscontinuity(reason: Int) {
            //Timber.d("onPositionDiscontinuity: $reason")
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
            viewModel.sendVideoReport()
        }
    }

    private val playerAnalyticsListener = object : AnalyticsListener {
        override fun onRenderedFirstFrame(
            eventTime: AnalyticsListener.EventTime,
            surface: Surface?
        ) {
            Timber.d("AnalyticsListener onRenderedFirstFrame")
        }

        override fun onDroppedVideoFrames(
            eventTime: AnalyticsListener.EventTime,
            droppedFrames: Int,
            elapsedMs: Long
        ) {
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

    private fun setupSourceList(list: List<Source>?) {
        if (list == null) {
            recyclerview_source_list.visibility = View.GONE
        } else {
            val size = list.size
            if (size == 0) {
                recyclerview_source_list.visibility = View.GONE
            } else {
                if (size == 1) {
                    recyclerview_source_list.visibility = View.GONE
                }

                val result = mutableListOf<String>()
                for (item in list) {
                    item.name?.also {
                        result.add(it)
                    }
                }
                Timber.i("setupSourceList =${result[0]}")
                sourceListAdapter.submitList(result, 0)
                viewModel.setSourceListPosition(0)
                scrollToBottom()
                CoroutineScope(Dispatchers.IO).launch {
                    delay(500)
                    viewModel.setStreamPosition(0)
                }
            }
        }
    }

    private fun setupStream(list: List<VideoEpisode>) {
        val result = mutableListOf<String>()
        Timber.i("setupStream =${list.size}")
        if (list.size == 1) {
            val videoStreams = list[0].videoStreams
            if (videoStreams != null && videoStreams.isNotEmpty()) {
                for (item in videoStreams) {
                    Timber.i("videoStreams =${item.id}")
                    if (item.id != null) {
                        result.add(item.streamName ?: "")
                    }
                }
            }
        }

        episodeAdapter.submitList(result, -1)

        scrollToBottom()
    }

    private fun setupChipGroup(list: List<String>?) {
        reflow_group.removeAllViews()

        if (list == null) {
            return
        }

        list.indices.mapNotNull {
            list[it]
        }.forEach {
            val chip = layoutInflater.inflate(
                R.layout.chip_item,
                reflow_group,
                false
            ) as Chip
            chip.text = it

            chip.setTextColor(requireContext().getColor(R.color.color_black_1_50))

            chip.setOnClickListener {
                val bundle = SearchVideoFragment.createBundle(
                    tag = chip.text.toString()
                )
                bundle.putBoolean(KEY_IS_FROM_PLAYER, true)
                findNavController().navigate(
                    R.id.action_playerFragment_to_searchVideoFragment,
                    bundle
                )
            }

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

    private fun reloadVideoInfo(playerItem: PlayerItem) {
        isFirstInit = true
        player?.clearVideoDecoderOutputBufferRenderer()
        player?.stop()
        viewModel.clearStreamData()
        loadVideo(playerItem)
        viewModel.setupCommentDataSource(playerInfoAdapter)
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
            var windowParams = requireActivity().window.attributes
            windowParams.flags =
                windowParams.flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            requireActivity().window.attributes = windowParams
            fullScreenUISet(true)
        }
    }

    /**
     * 切換螢幕方向
     */
    private fun switchScreenOrientation() {
        requireActivity().requestedOrientation =
            if (viewModel.lockFullScreen) {
                when (viewModel.currentOrientation) {
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> viewModel.currentOrientation
                    else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        adjustPlayerSize()
    }

    private fun fullScreenUISet(isFullScreen: Boolean) {
        if (isFullScreen) {
            commentEditorHide()
            recycler_info.visibility = View.GONE
            bottom_func_bar.visibility = View.GONE
            bottom_func_input.visibility = View.GONE
        } else {
            recycler_info?.visibility = VISIBLE
            bottom_func_bar?.visibility = VISIBLE
            bottom_func_input.visibility = View.GONE
        }
    }

    private fun scrollToBottom() {
        if (arguments?.getSerializable(KEY_IS_COMMENT) == true) {
//            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun scrollToCommentArea() {
        val scrollTo: Int = layout_comment.top
        nestedScrollView.smoothScrollTo(0, scrollTo)
    }

    private fun showEmailConfirmDialog() {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.error_email_not_confirmed_title,
                message = getString(R.string.error_email_not_confirmed_msg),
                messageIcon = R.drawable.ico_email,
                firstBtn = getString(R.string.verify_later),
                secondBtn = getString(R.string.verify_immediately),
                secondBlock = {
                    val bundle = Bundle().also { it.putBoolean(KEY_IS_FROM_PLAYER, true) }
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_playerFragment_to_settingFragment,
                            bundle
                        )

                    )
                }
            )
        ).show(requireActivity().supportFragmentManager)
    }


    fun showNotLoginDialog() {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.login_yet,
                message = getString(R.string.login_message),
                messageIcon = R.drawable.ico_default_photo,
                firstBtn = getString(R.string.btn_register),
                secondBtn = getString(R.string.btn_login),
                firstBlock = {
                    val bundle = Bundle()
                    bundle.putInt(LoginFragment.KEY_TYPE, LoginFragment.TYPE_REGISTER)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_playerFragment_to_loginFragment,
                            bundle
                        )
                    )
                },
                secondBlock = {
                    val bundle = Bundle()
                    bundle.putInt(LoginFragment.KEY_TYPE, LoginFragment.TYPE_LOGIN)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_playerFragment_to_loginFragment,
                            bundle
                        )
                    )
                },
                closeBlock = {
                    Timber.d("close!")
                }
            )
        ).show(requireActivity().supportFragmentManager)
    }
}