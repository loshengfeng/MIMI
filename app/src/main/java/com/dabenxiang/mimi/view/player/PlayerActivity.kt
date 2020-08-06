package com.dabenxiang.mimi.view.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.hardware.SensorManager
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDeepLinkBuilder
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
import com.dabenxiang.mimi.model.enums.CommentViewType
import com.dabenxiang.mimi.model.enums.HttpErrorMsgType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.VideoConsumeResult
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.adapter.TopTabAdapter
import com.dabenxiang.mimi.view.base.BaseActivity
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.dialog.*
import com.dabenxiang.mimi.view.login.LoginActivity
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
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
import kotlinx.android.synthetic.main.head_comment.view.*
import kotlinx.android.synthetic.main.head_guess_like.view.*
import kotlinx.android.synthetic.main.head_no_comment.view.*
import kotlinx.android.synthetic.main.head_source.view.*
import kotlinx.android.synthetic.main.head_video_info.view.*
import kotlinx.android.synthetic.main.item_ad.view.*
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

class PlayerActivity : BaseActivity() {

    companion object {
        const val REQUEST_CODE = 111
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

    private val sourceListAdapter by lazy {
        TopTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                Timber.i("TopTabAdapter onClickItemIndex")
                viewModel.setSourceListPosition(index)
            }
        }, obtainIsAdult())
    }

    private val episodeAdapter by lazy {
        SelectEpisodeAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                Timber.i("SelectEpisodeAdapter onClickItemIndex $index")
                viewModel.setStreamPosition(index)
            }
        }, obtainIsAdult())
    }

    private val guessLikeAdapter by lazy {
        GuessLikeAdapter(object :
            GuessLikeAdapter.GuessLikeAdapterListener {
            override fun onVideoClick(view: View, item: PlayerItem) {
//                val intent = Intent(this@PlayerActivity, PlayerActivity::class.java)
//                intent.putExtras(createBundle(item))
//                startActivity(intent)
//
//                finish()
                isFirstInit = true
                viewModel.clearStreamData()
                loadVideo(item)
            }
        }, obtainIsAdult())
    }

    private val progressHUD by lazy {
        KProgressHUD.create(this).setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
    }

    private val adInfo by lazy {
        layoutInflater.inflate(R.layout.item_ad, recycler_info.parent as ViewGroup, false)
    }

    private val headVideoInfo by lazy {
        layoutInflater.inflate(R.layout.head_video_info, recycler_info.parent as ViewGroup, false)
    }

    private val headSource by lazy {
        layoutInflater.inflate(R.layout.head_source, recycler_info.parent as ViewGroup, false)
    }

    private val headGuessLike by lazy {
        layoutInflater.inflate(R.layout.head_guess_like, recycler_info.parent as ViewGroup, false)
    }

    private val headComment by lazy {
        layoutInflater.inflate(R.layout.head_comment, recycler_info.parent as ViewGroup, false)
    }

    private val headNoComment by lazy {
        layoutInflater.inflate(R.layout.head_no_comment, recycler_info.parent as ViewGroup, false)
    }

    private val playerInfoAdapter by lazy {
        Timber.i("playerInfoAdapter")
        CommentAdapter(obtainIsAdult(), object : CommentAdapter.PlayerInfoListener {
            override fun sendComment(replyId: Long?, replyName: String?) {
                Timber.i("playerInfoAdapter sendComment")
                currentReplyId = null
                currentreplyName = null
                if (replyId != null) {
                    currentReplyId = replyId
                    currentreplyName = replyName
                    commentEditorOpen()
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
                Timber.i("playerInfoAdapter setCommentLikeType")
                loadCommentLikeBlock = succeededBlock
                replyId?.also {
                    val type = if (isLike) 0 else 1
                    viewModel.postCommentLike(replyId, PostLikeRequest(type))
                }
            }

            override fun removeCommentLikeType(replyId: Long?, succeededBlock: () -> Unit) {
                loadCommentLikeBlock = succeededBlock
                replyId?.also {
                    viewModel.deleteCommentLike(replyId)
                }
            }

            override fun getBitmap(id: Long, succeededBlock: (Bitmap) -> Unit) {
                Timber.i("playerInfoAdapter getBitmap")
                viewModel.getBitmap(id.toString(), succeededBlock)
            }

            override fun onMoreClick(item: MembersPostCommentItem) {
                Timber.i("playerInfoAdapter onMoreClick")
            }

            override fun onAvatarClick(userId: Long, name: String) {
                // TODO:
                Timber.d("onAvatarClick nav to member post")
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

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isAdult = obtainIsAdult()

        playerInfoAdapter.addHeaderView(adInfo)
        playerInfoAdapter.addHeaderView(headVideoInfo)
        playerInfoAdapter.addHeaderView(headSource)
        playerInfoAdapter.addHeaderView(headGuessLike)
        playerInfoAdapter.addHeaderView(headComment)
        playerInfoAdapter.addHeaderView(headNoComment)
        playerInfoAdapter.loadMoreModule.loadMoreView =
            CommentLoadMoreView(isAdult, CommentViewType.VIDEO)

        recycler_info.adapter = playerInfoAdapter

        val backgroundColor = if (isAdult) {
            getColor(R.color.adult_color_background)
        } else {
            getColor(R.color.normal_color_background)
        }
        recycler_info.setBackgroundColor(backgroundColor)

        val titleColor =
            if (isAdult) {
                R.color.adult_color_text
            } else {
                R.color.normal_color_text
            }.let {
                getColor(it)
            }

        headVideoInfo.tv_title.setTextColor(titleColor)
        headSource.title_source.setTextColor(titleColor)
        headGuessLike.title_guess_like.setTextColor(titleColor)
        headComment.title_comment.setTextColor(titleColor)
        headNoComment.title_no_comment.setTextColor(titleColor)

        val subTitleColor =
            if (isAdult) {
                R.color.color_white_1_50
            } else {
                R.color.color_black_1_50
            }.let {
                getColor(it)
            }
        headVideoInfo.btn_show_introduction.setTextColor(subTitleColor)
        headVideoInfo.tv_introduction.setTextColor(subTitleColor)
        headVideoInfo.tv_info.setTextColor(subTitleColor)
        headVideoInfo.tv_introduction.setBackgroundResource(
            if (isAdult) {
                R.drawable.bg_white_stroke_1_radius_2
            } else {
                R.drawable.bg_black_stroke_1_radius_2
            }
        )

        val lineColor =
            if (isAdult) getColor(R.color.color_white_1_10) else getColor(R.color.color_black_1_05)
        headSource.line_source.setBackgroundColor(lineColor)
        headComment.line_comment.setBackgroundColor(lineColor)
        headComment.line_separate.setBackgroundColor(lineColor)

        headVideoInfo.btn_show_introduction.setOnClickListener {
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
            headVideoInfo.tv_introduction.visibility = if (isShow) View.VISIBLE else View.GONE
            headVideoInfo.btn_show_introduction.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                drawableRes,
                0
            )
        })

        bottom_func_bar.setBackgroundResource(
            if (isAdult) {
                R.drawable.bg_adult_top_line
            } else {
                R.drawable.bg_gray_2_top_line
            }
        )

        bottom_func_bar.setBackgroundResource(
            if (isAdult) {
                R.drawable.bg_adult_top_line
            } else {
                R.drawable.bg_gray_2_top_line
            }
        )

        if (isAdult) {
            btn_write_comment.setTextColor(getColor(R.color.color_white_1_30))
            et_message.setTextColor(getColor(R.color.color_white_1_30))
//            btn_write_comment.setBtnSolidColor(getColor(R.color.color_black_1_20))
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

        viewModel.isPlaying.observe(this, Observer {
            iv_player.visibility = when (it) {
                true -> View.GONE
                false -> View.VISIBLE
            }
        })

        viewModel.sourceListPosition.observe(this, Observer {
            if (it == -1) return@Observer
            sourceListAdapter.setLastSelectedIndex(it)
            viewModel.sourceList?.get(it)?.videoEpisodes?.also { videoEpisodes ->
                Timber.i("videoEpisodes =$videoEpisodes")
                setupStream(videoEpisodes)
            }

            scrollToBottom()
        })

        viewModel.episodePosition.observe(this, Observer {
            Timber.i("episodePosition =$it")
            if (it >= 0) {
                episodeAdapter.setLastSelectedIndex(it)
                viewModel.checkConsumeResult()
            }
            scrollToBottom()
        })

        viewModel.isPageCallback.observe(this, Observer {
            scrollToBottom()
        })

        viewModel.apiStreamResult.observe(this, Observer {
            when (it) {
                is Loading -> progressHUD.show()
                is Loaded -> progressHUD.dismiss()
                is Empty -> loadVideo()
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.apiPostCommentResult.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Loading -> progressHUD.show()
                    is Loaded -> progressHUD.dismiss()
                    is Empty -> {
                        currentReplyId = null
                        currentreplyName = null
                        headNoComment.title_no_comment.visibility = View.GONE
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

        viewModel.apiCommentLikeResult.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Loading -> progressHUD.show()
                    is Loaded -> progressHUD.dismiss()
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

        viewModel.apiDeleteCommentLikeResult.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Loading -> progressHUD.show()
                    is Loaded -> progressHUD.dismiss()
                    is Empty -> loadCommentLikeBlock?.also { it() }
                    is Error -> onApiError(it.throwable)
                }
            }
        })

//        var isFirstInit = true
        viewModel.apiVideoInfo.observe(this, Observer {
            when (it) {
                is Loading -> progressHUD.show()
                is Loaded -> progressHUD.dismiss()
                is Success -> {
                    val result = it.result
                    viewModel.category = result.categories?.get(0) ?: ""

                    if (isFirstInit) {
                        isFirstInit = false
                        headVideoInfo.tv_title.text = result.title

                        if (!result.description.isNullOrBlank())
                            headVideoInfo.tv_introduction.text =
                                Html.fromHtml(result.description, Html.FROM_HTML_MODE_COMPACT)

                        val dateString = result.updateTime?.let { date ->
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                        }

                        headVideoInfo.tv_info.text = String.format(
                            getString(R.string.player_info_format),
                            dateString ?: "",
                            result.country
                        )

                        viewModel.sourceList = result.sources

                        if (result.tags != null)
                            setupChipGroup(result.tags as List<String>)

                        setupSourceList(viewModel.sourceList)

                        val categoriesString = result.categories?.last()
                        viewModel.setupGuessLikeList(categoriesString, isAdult)
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
                        headNoComment.title_no_comment.visibility = View.VISIBLE
                        headNoComment.title_no_comment.setTextColor(titleColor)
                        val bgColor =
                            if (isAdult) {
                                R.color.color_white_1_10
                            } else {
                                R.color.color_black_1_10
                            }.let { colorRes ->
                                getColor(colorRes)
                            }
                        headNoComment.title_no_comment.setBtnSolidColor(
                            bgColor,
                            bgColor,
                            resources.getDimension(R.dimen.dp_10)
                        )
                    } else {
                        headNoComment.title_no_comment.visibility = View.GONE

                        lifecycleScope.launchWhenResumed {
                            viewModel.setupCommentDataSource(playerInfoAdapter)
                        }
                    }
                }
                is Error -> onApiError(it.throwable)
            }
            scrollToBottom()
        })

        viewModel.isSelectedNewestComment.observe(this, Observer {
            if (it) {
                headComment.tv_newest.setTextColor(getColor(R.color.color_red_1))
                headComment.tv_hottest.setTextColor(subTitleColor)
            } else {
                headComment.tv_newest.setTextColor(subTitleColor)
                headComment.tv_hottest.setTextColor(getColor(R.color.color_red_1))
            }
        })

        tv_comment.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (isAdult) R.drawable.ico_messege_adult else R.drawable.ico_messege_adult_gray,
            0,
            0,
            0
        )

        tv_like.setTextColor(titleColor)
        iv_favorite.setTextColor(titleColor)
        tv_comment.setTextColor(titleColor)

        iv_share.setImageResource(if (isAdult) R.drawable.btn_share_white_n else R.drawable.btn_share_gray_n)
        iv_more.setImageResource(if (isAdult) R.drawable.btn_more_white_n else R.drawable.btn_more_gray_n)

        tv_replay_name.setTextColor(if (obtainIsAdult()) {
            R.color.color_white_1
        } else {
            R.color.color_black_1
        }.let {
            getColor(it)
        })


        headComment.tv_newest.setOnClickListener {
            viewModel.updatedSelectedNewestComment(true)
            lifecycleScope.launch {
                viewModel.setupCommentDataSource(playerInfoAdapter)
            }
        }

        headComment.tv_hottest.setOnClickListener {
            viewModel.updatedSelectedNewestComment(false)
            lifecycleScope.launch {
                viewModel.setupCommentDataSource(playerInfoAdapter)
            }
        }

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

            iv_favorite.setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)
        })

        viewModel.favoriteVideoCount.observe(this, Observer {
            iv_favorite.text = it.toString()
        })

        viewModel.commentCount.observe(this, Observer {
            tv_comment.text = it.toString()
        })

        viewModel.consumeResult.observe(this, Observer {
            consumeDialog?.dismiss()
            when (it) {
                VideoConsumeResult.PAID -> {
                    viewModel.getStreamUrl(obtainIsAdult())
                }
                VideoConsumeResult.PAID_YET -> {
                    consumeDialog = showCostPointDialog()
                }
                VideoConsumeResult.POINT_NOT_ENOUGH -> {
                    consumeDialog = showPointNotEnoughDialog()
                }
            }
            scrollToBottom()
        })

        viewModel.apiLoadReplyCommentResult.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.also { apiResult ->
                when (apiResult) {
                    is Loading -> progressHUD.show()
                    is Loaded -> {
                        loadReplyCommentBlock = null
                        progressHUD.dismiss()
                    }
                    is Empty -> {
                        loadReplyCommentBlock?.also {
                            it()
                        }
                    }
                }
            }
        })

        headSource.recyclerview_source_list.adapter = sourceListAdapter
        headSource.recyclerview_episode.adapter = episodeAdapter

        headGuessLike.recyclerview_guess_like.adapter = guessLikeAdapter
        LinearSnapHelper().attachToRecyclerView(headGuessLike.recyclerview_guess_like)

        viewModel.videoList.observe(this, Observer {
            guessLikeAdapter.submitList(it)
            scrollToBottom()
        })

        viewModel.recyclerViewGuessLikeVisible.observe(this, Observer {
            headGuessLike.title_guess_like.visibility = it
            headGuessLike.recyclerview_guess_like.visibility = it

            scrollToBottom()
        })

        btn_full_screen.setOnClickListener {
            viewModel.lockFullScreen = !viewModel.lockFullScreen

            switchScreenOrientation()
        }

        orientationDetector =
            OrientationDetector(this, SensorManager.SENSOR_DELAY_NORMAL).also { detector ->
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

                        adjustPlayerSize()
                    }
                })
            }

        btn_write_comment.setOnClickListener {
            currentReplyId = null
            currentreplyName = null
            commentEditorOpen()
            commentEditorToggle(true)
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
            viewModel.modifyFavorite()
        }

        viewModel.apiAddFavoriteResult.observe(this, Observer { event ->
            event?.getContentIfNotHandled()?.also { apiResult ->
                when (apiResult) {
                    is Loading -> progressHUD.show()
                    is Loaded -> {
                        progressHUD.dismiss()
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

        tv_like.setOnClickListener {
            viewModel.modifyLike()
        }

        viewModel.apiAddLikeResult.observe(this, Observer { event ->
            event?.getContentIfNotHandled()?.also { apiResult ->
                when (apiResult) {
                    is Loading -> progressHUD.show()
                    is Loaded -> {
                        progressHUD.dismiss()
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

        iv_share.setOnClickListener {
            GeneralUtils.copyToClipboard(
                baseContext,
                viewModel.getShareUrl(
                    viewModel.category,
                    viewModel.videoId,
                    viewModel.episodeId.toString()
                )
            )
            GeneralUtils.showToast(baseContext, getString(R.string.copy_url))
        }

        iv_more.setOnClickListener {

            moreDialog = MoreDialogFragment.newInstance(
                MemberPostItem(
                    id = obtainVideoId(),
                    type = PostType.VIDEO,
                    reported = viewModel.isReported

                ), onMoreDialogListener
            ).also {

                it.show(
                    supportFragmentManager,
                    MoreDialogFragment::class.java.simpleName
                )
            }
        }

        viewModel.apiReportResult.observe(this, Observer { event ->
            event?.getContentIfNotHandled()?.also { apiResult ->
                when (apiResult) {
                    is Loading -> progressHUD.show()
                    is Loaded -> progressHUD.dismiss()
                    is Empty -> {
                        viewModel.isReported = true
                        GeneralUtils.showToast(this, getString(R.string.report_success))
                    }
                    is Error -> onApiError(apiResult.throwable)
                }
            }
        })

        viewModel.getAdResult.observe(this, Observer {
            when (it) {
                is Success -> {
                    Glide.with(this)
                        .load(it.result.href)
                        .into(adInfo.iv_ad)

                    adInfo.iv_ad.setOnClickListener { view ->
                        GeneralUtils.openWebView(this, it.result.target)
                    }
                }
                is Error -> onApiError(it.throwable)
            }
        })

        //Detect key keyboard shown/hide
        this.addKeyboardToggleListener { shown ->
            if (!shown) commentEditorToggle(false)
        }

        val adWidth = ((GeneralUtils.getScreenSize(this).first) * 0.333).toInt()
        val adHeight = (GeneralUtils.getScreenSize(this).second * 0.0245).toInt()
        viewModel.getAd(adWidth, adHeight)
    }

    private val onReportDialogListener = object : ReportDialogFragment.OnReportDialogListener {
        override fun onSend(item: BaseMemberPostItem, content: String, postItem: MemberPostItem?) {
            if (TextUtils.isEmpty(content)) {
                GeneralUtils.showToast(App.applicationContext(), getString(R.string.report_error))
            } else {
                when (item) {
                    is MemberPostItem -> viewModel.sentReport(content)
                }
            }
            reportDialog?.dismiss()
        }

        override fun onCancel() {
            reportDialog?.dismiss()
        }
    }

    private val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
        override fun onProblemReport(item: BaseMemberPostItem) {
            if (viewModel.isReported) {
                GeneralUtils.showToast(
                    App.applicationContext(),
                    getString(R.string.already_reported)
                )
            } else {
                reportDialog = ReportDialogFragment.newInstance(item, onReportDialogListener).also {
                    it.show(
                        supportFragmentManager,
                        ReportDialogFragment::class.java.simpleName
                    )
                }
            }
            moreDialog?.dismiss()
        }

        override fun onCancel() {
            moreDialog?.dismiss()
        }
    }

    private fun commentEditorToggle(enable: Boolean) {
        when (enable) {
            true -> {
                bottom_func_input.visibility = View.VISIBLE
                bottom_func_bar.visibility = View.GONE
            }
            else -> {
                bottom_func_input.visibility = View.GONE
                bottom_func_bar.visibility = View.VISIBLE
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
                }

                tv_replay_name.visibility = View.VISIBLE
            }
            et_message.let {
                it.requestFocusFromTouch()
                val lManager: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                lManager.showSoftInput(it, 0)
            }
        }
        commentEditorToggle(true)
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

        if (!viewModel.accountManager.isLogin() && (dialog == null || dialog?.isVisible == false)) {
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

    override fun onDestroy() {
        super.onDestroy()
        dialog = null
        consumeDialog = null
        loadReplyCommentBlock = null
        loadCommentLikeBlock = null
    }

    override fun onBackPressed() {
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            super.onBackPressed()
        } else {
            viewModel.lockFullScreen = !viewModel.lockFullScreen
            switchScreenOrientation()
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

    private fun loadVideo(
        playerItem: PlayerItem = PlayerItem(
            -1,
            false
        )
    ) {
        if (playerItem.videoId != -1L) {
            viewModel.videoId = playerItem.videoId
            viewModel.getVideoInfo()
        } else if (viewModel.nextVideoUrl == null) {
            if (viewModel.apiVideoInfo.value == null) {
                (intent.extras?.getSerializable(KEY_PLAYER_SRC) as PlayerItem?)?.also {
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
            setupPlayUrl(viewModel.currentVideoUrl!!, isReset)
        }
    }

    private fun setupPlayUrl(url: String, isReset: Boolean) {
        val agent = Util.getUserAgent(this, getString(R.string.app_name))
        val sourceFactory = DefaultDataSourceFactory(this, agent)

        viewModel.getMediaSource(url, sourceFactory)?.also {
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
                    player_view.visibility = View.VISIBLE
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

    private fun obtainVideoId(): Long {
        return (intent.extras?.getSerializable(KEY_PLAYER_SRC) as PlayerItem?)?.videoId ?: 0
    }

    private fun obtainIsAdult(): Boolean {
        return (intent.extras?.getSerializable(KEY_PLAYER_SRC) as PlayerItem?)?.isAdult ?: false
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

    private fun setupSourceList(list: List<Source>?) {
        if (list == null) {
            headSource.recyclerview_source_list.visibility = View.GONE
        } else {
            val size = list.size
            if (size == 0) {
                headSource.recyclerview_source_list.visibility = View.GONE
            } else {
                if (size == 1) {
                    headSource.recyclerview_source_list.visibility = View.GONE
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
        // 成人取得Streaming邏輯不同
        Timber.i("setupStream =${list.size}")
        if (obtainIsAdult()) {
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
        } else {
            for (item in list) {
                if (item.id != null) {
                    result.add(item.episode ?: "")
                }
            }
            result.sort()
            for (i in 0..(result.size - 1)) {
                Timber.d("${result.get(i)}")
            }
        }

        episodeAdapter.submitList(result, -1)
        viewModel.setStreamPosition(-1)

        scrollToBottom()
    }

    private fun setupChipGroup(list: List<String>?) {
        headVideoInfo.reflow_group.removeAllViews()

        if (list == null) {
            return
        }

        list.indices.mapNotNull {
            list[it]
        }.forEach {
            val chip = layoutInflater.inflate(
                R.layout.chip_item,
                headVideoInfo.reflow_group,
                false
            ) as Chip
            chip.text = it

            val isAdult = obtainIsAdult()

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

            chip.setOnClickListener(
                View.OnClickListener {
                    val bundle = SearchVideoFragment.createBundle(
                        tag = chip.text.toString(),
                        isAdult = obtainIsAdult()
                    )
                    deepLinkTo(
                        MainActivity::class.java,
                        R.navigation.navigation_home,
                        R.id.searchVideoFragment,
                        bundle
                    )
                }
            )

            headVideoInfo.reflow_group.addView(chip)
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
                message = when (type) {
                    HttpErrorMsgType.API_FAILED -> getString(R.string.api_failed_msg)
                    HttpErrorMsgType.CHECK_NETWORK -> getString(R.string.server_error)
                },
                messageIcon = R.drawable.ico_default_photo,
                secondBtn = getString(R.string.btn_close)
            )
        ).show(supportFragmentManager)
    }

    private fun showPointNotEnoughDialog(): GeneralDialog {
        return GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.point_not_enough,
                messageIcon = R.drawable.ico_topup,
                isHtml = true,
                message = getString(R.string.point_not_enough_message),
                firstBtn = getString(R.string.btn_cancel),
                secondBtn = getString(R.string.recharge)
            )
        )
            .setCancel(false)
            .show(supportFragmentManager)
    }

    private fun showCostPointDialog(): GeneralDialog {
        val message = String.format(
            getString(R.string.cost_point_message),
            viewModel.availablePoint,
            viewModel.costPoint
        )

        return GeneralDialog.newInstance(
            GeneralDialogData(
                titleString = headVideoInfo.tv_title.text.toString(),
                messageIcon = R.drawable.ico_topup,
                isHtml = true,
                message = message,
                firstBtn = getString(R.string.btn_cancel),
                secondBtn = getString(R.string.btn_confirm),
                secondBlock = { viewModel.getStreamUrl(obtainIsAdult()) }
            )
        )
            .setCancel(false)
            .show(supportFragmentManager)
    }

    /**
     * 調整 player 的寬與高
     */
    private fun adjustPlayerSize() {
        val screenSize = GeneralUtils.getScreenSize(this)
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            val params = player_view.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = 0
            player_view.layoutParams = params
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        } else {
            val params = player_view.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
//            params.height =
//                min(screenSize.first, screenSize.second) - GeneralUtils.getStatusBarHeight(
//                    baseContext
//                )
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            player_view.layoutParams = params
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            var windowParams = window.attributes
            windowParams.flags =
                windowParams.flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            window.attributes = windowParams
        }
    }

    /**
     * 切換螢幕方向
     */
    private fun switchScreenOrientation() {
        requestedOrientation =
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

    private fun scrollToBottom() {
        if (intent.extras?.getBoolean(KEY_IS_COMMENT) == true) {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun deepLinkTo(
        activity: Class<out Activity>,
        navGraphId: Int,
        destId: Int,
        bundle: Bundle?
    ) {
        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(activity)
            .setGraph(navGraphId)
            .setDestination(destId)
            .setArguments(bundle)
            .createPendingIntent()

        pendingIntent.send()
    }

}