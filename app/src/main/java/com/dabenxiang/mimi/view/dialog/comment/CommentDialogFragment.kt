package com.dabenxiang.mimi.view.dialog.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PostLikeRequest
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import com.dabenxiang.mimi.view.player.CommentLoadMoreView
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.RootCommentNode
import kotlinx.android.synthetic.main.fragment_dialog_comment.*

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
                if (replyId != null) {
                }
            }

            override fun expandReply(parentNode: RootCommentNode, succeededBlock: () -> Unit) {
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
                if (replyId != null) {
                }
            }

            override fun setCommentLikeType(replyId: Long?, isLike: Boolean, succeededBlock: () -> Unit) {
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
                loadCommentLikeBlock = succeededBlock
                data?.id?.let { postId ->
                    replyId?.let { replyId ->
                        Pair(postId, replyId)
                    }?.also { (postId, replyId) ->
                    viewModel.deleteCommentLike(postId, replyId)
                    }
                }
            }
        }).apply {
            loadMoreModule.apply {
                isEnableLoadMore = true
                isAutoLoadMore = true
                isEnableLoadMoreIfNotFullPage = false
                loadMoreView = CommentLoadMoreView(true)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (arguments?.getSerializable(KEY_DATA) as MemberPostItem).also {
            data = it
            tv_comment_count.text = String.format(requireContext().getString(R.string.clip_comment_count), it.commentCount)

            rv_comment.adapter = playerInfoAdapter
            lifecycleScope.launchWhenResumed {
                viewModel.setupCommentDataSource(it.id, playerInfoAdapter)
            }
        }

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
//                        progressHUD.dismiss()
                    }
                }
            }
        })
    }
}