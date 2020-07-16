package com.dabenxiang.mimi.view.dialog.comment

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PostCommentRequest
import com.dabenxiang.mimi.model.api.vo.PostLikeRequest
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.CommentLoadMoreView
import com.dabenxiang.mimi.view.player.RootCommentNode
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.fragment_dialog_comment.*
import kotlinx.android.synthetic.main.head_no_comment.view.*
import timber.log.Timber


class CommentDialogFragment: BaseDialogFragment() {

    private val viewModel: CommentDialogViewModel by viewModels()
    private var data: MemberPostItem? = null

    companion object {
        private const val KEY_DATA = "KEY_DATA"

        fun newInstance(
            item: MemberPostItem
        ): CommentDialogFragment {
            val fragment = CommentDialogFragment()
            val args = Bundle()
            args.putSerializable(KEY_DATA, item)
            fragment.arguments = args
            return fragment
        }
    }

    private var loadReplyCommentBlock: (() -> Unit)? = null
    private var loadCommentLikeBlock: (() -> Unit)? = null

    private val playerInfoAdapter by lazy {
        CommentAdapter(true, object : CommentAdapter.PlayerInfoListener {
            override fun sendComment(replyId: Long?, replyName: String?) {
                Timber.d("@@sendComment: $replyId, $replyName")
                showKeyboard()
                et_message.requestFocus()
                et_message.tag = replyId
                tv_replay_name.text = replyName.takeIf { it != null }?.let {
                    tv_replay_name.visibility = View.VISIBLE
                    String.format(requireContext().getString(R.string.clip_username), it)
                } ?: run { "" }
            }

            override fun expandReply(parentNode: RootCommentNode, succeededBlock: () -> Unit) {
                Timber.d("@@expandReply: $parentNode")
                loadReplyCommentBlock = succeededBlock
                data?.id?.let { postId ->
                    parentNode.data.id?.let { commentId ->
                        Pair(postId, commentId)
                    }?.also { (postId, commentId) ->
                        viewModel.loadReplyComment(postId, parentNode, commentId)
                    }
                }
            }

            override fun replyComment(replyId: Long?, replyName: String?) {
                Timber.d("@@replyComment: $replyId, $replyName")
                takeUnless { replyId == null }?.also {
                    showKeyboard()
                    et_message.requestFocus()
                    et_message.tag = replyId
                    tv_replay_name.text = replyName.takeIf { it != null }?.let {
                        tv_replay_name.visibility = View.VISIBLE
                        String.format(requireContext().getString(R.string.clip_username), it)
                    } ?: run { "" }
                }
            }

            override fun setCommentLikeType(replyId: Long?, isLike: Boolean, succeededBlock: () -> Unit) {
                Timber.d("@@setCommentLikeType: $replyId, $isLike")
                loadCommentLikeBlock = succeededBlock
                data?.id?.let { postId ->
                    replyId?.let { replyId ->
                        Pair(postId, replyId)
                    }?.also { (postId, replyId) ->
                        val type = if (isLike) 0 else 1
                        viewModel.postCommentLike(postId, replyId, PostLikeRequest(type))
                    }
                }
            }

            override fun removeCommentLikeType(replyId: Long?, succeededBlock: () -> Unit) {
                Timber.d("@@removeCommentLikeType: $replyId")
                loadCommentLikeBlock = succeededBlock
                data?.id?.let { postId ->
                    replyId?.let { replyId ->
                        Pair(postId, replyId)
                    }?.also { (postId, replyId) ->
                    viewModel.deleteCommentLike(postId, replyId)
                    }
                }
            }

            override fun getBitmap(id: Long, succeededBlock: (Bitmap) -> Unit) {
                viewModel.getBitmap(id.toString(), succeededBlock)
            }
        }, true).apply {
            loadMoreModule.apply {
                isEnableLoadMore = true
                isAutoLoadMore = true
                isEnableLoadMoreIfNotFullPage = false
                loadMoreView = CommentLoadMoreView(true, isClip = true)
            }
        }
    }

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_comment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CommentDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(requireContext())
            .load(R.drawable.bg_comment_dialog)
            .transform(BlurTransformation(25, 5))
            .into(iv_blur)

        (arguments?.getSerializable(KEY_DATA) as MemberPostItem).also { memberPostItem ->
            data = memberPostItem
            tv_comment_count.text = String.format(requireContext().getString(R.string.clip_comment_count), memberPostItem.commentCount)

            rv_comment.adapter = playerInfoAdapter
            lifecycleScope.launchWhenResumed {
                viewModel.setupCommentDataSource(memberPostItem.id, playerInfoAdapter)
            }

        }
    }

    override fun setupListeners() {
        super.setupListeners()

        background.setOnClickListener { dismiss() }

        btn_send.setOnClickListener {
            data?.id?.let { id ->
                et_message.text.toString().takeIf { !TextUtils.isEmpty(it) }?.let { comment ->
                    Pair(id, comment)
                }?.also { (id, comment) ->
                    val replyId = et_message.tag?.let { rid -> rid as Long }
                    viewModel.postComment(id, PostCommentRequest(replyId, comment))
                }
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.apiLoadReplyCommentResult.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.also { apiResult ->
                when (apiResult) {
                    is ApiResult.Loading -> {
//                        progressHUD.show()
                    }
                    is ApiResult.Empty -> {
                        loadReplyCommentBlock?.also { it() }
                    }
                    is ApiResult.Loaded -> {
                        loadReplyCommentBlock = null
                    }
                }
            }
        })

        viewModel.apiPostCommentResult.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is ApiResult.Loading -> {}
                    is ApiResult.Loaded -> {}
                    is ApiResult.Empty -> {
                        closeKeyboard()
                        et_message.text = null
                        et_message.tag = null
                        tv_replay_name.text = null
                        tv_replay_name.visibility = View.GONE

//                        headNoComment.title_no_comment.visibility = View.GONE
//                        viewModel.commentCount.value = viewModel.commentCount.value?.plus(1)

                        data?.also { memberPostItem ->
                            viewModel.setupCommentDataSource(memberPostItem.id, playerInfoAdapter)
                        }

                    }
                    is ApiResult.Error -> {}
                }
            }
        })

        viewModel.apiCommentLikeResult.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is ApiResult.Loading -> {}
                    is ApiResult.Loaded -> {}
                    is ApiResult.Empty -> {
                        loadCommentLikeBlock = loadCommentLikeBlock?.let {
                            it()
                            null
                        }
                    }
                    is ApiResult.Error -> {}
                }
            }
        })

        viewModel.apiDeleteCommentLikeResult.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is ApiResult.Loading -> {}
                    is ApiResult.Loaded -> {}
                    is ApiResult.Empty -> {
                        loadCommentLikeBlock = loadCommentLikeBlock?.let {
                            it()
                            null
                        }
                    }
                    is ApiResult.Error -> {}
                }
            }
        })
    }

    private fun showKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun closeKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }
}