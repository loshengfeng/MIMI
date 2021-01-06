package com.dabenxiang.mimi.view.dialog.comment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.CommentViewType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.CommentLoadMoreView
import com.dabenxiang.mimi.view.player.NestedCommentNode
import com.dabenxiang.mimi.view.player.RootCommentNode
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.fragment_dialog_comment.*


class CommentDialogFragment : BaseDialogFragment() {

    private val viewModel: CommentDialogViewModel by viewModels()
    private var data: VideoItem? = null

    companion object {
        private const val KEY_DATA = "KEY_DATA"
        private var commentListener: CommentListener? = null

        fun newInstance(
            item: VideoItem,
            listener: CommentListener
        ): CommentDialogFragment {
            val fragment = CommentDialogFragment()
            commentListener = listener
            val args = Bundle()
            args.putSerializable(KEY_DATA, item)
            fragment.arguments = args
            return fragment
        }
    }

    interface CommentListener {
        fun onAvatarClick(userId: Long, name: String)
        fun onUpdateCommentCount(count: Int)
    }

    private var loadReplyCommentBlock: (() -> Unit)? = null
    private var loadCommentLikeBlock: (() -> Unit)? = null

    var replyRootNode: RootCommentNode? = null

    var moreDialog: MoreDialogFragment? = null

    private val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
        override fun onProblemReport(item: BaseMemberPostItem, isComment: Boolean) {
            moreDialog?.dismiss()
            showReportDialog(item, isComment)
        }

        override fun onCancel() {
            moreDialog?.dismiss()
        }
    }

    fun showReportDialog(item: BaseMemberPostItem, isComment: Boolean) {
        (requireActivity() as MainActivity).showReportDialog(
            item,
            MemberPostItem(
                id = data?.id ?: 0,
                type = PostType.VIDEO,
                reported = data?.reported ?: false
            ),
            isComment
        )
    }

    private val playerInfoAdapter by lazy {
        CommentAdapter(object : CommentAdapter.PlayerInfoListener {
            override fun sendComment(
                replyId: Long?,
                replyName: String?,
                parentNode: RootCommentNode
            ) {
                replyRootNode = parentNode
                GeneralUtils.showKeyboard(requireContext())
                et_message.requestFocus()
                et_message.tag = replyId
                tv_replay_name.text = replyName.takeIf { it != null }?.let {
                    tv_replay_name.visibility = View.VISIBLE
                    String.format(requireContext().getString(R.string.clip_username), it)
                } ?: run { "" }
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

            override fun replyComment(
                replyId: Long?,
                replyName: String?,
                parentNode: RootCommentNode
            ) {
                takeUnless { replyId == null }?.also {
                    replyRootNode = parentNode
                    GeneralUtils.showKeyboard(requireContext())
                    et_message.requestFocus()
                    et_message.tag = replyId
                    tv_replay_name.text = replyName.takeIf { it != null }?.let {
                        tv_replay_name.visibility = View.VISIBLE
                        String.format(requireContext().getString(R.string.clip_username), it)
                    } ?: run { "" }
                }
            }

            override fun setCommentLikeType(
                replyId: Long?,
                isLike: Boolean,
                succeededBlock: () -> Unit
            ) {
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

            override fun onMoreClick(item: MembersPostCommentItem) {
                moreDialog = MoreDialogFragment.newInstance(item, onMoreDialogListener, true).also {
                    it.show(
                        requireActivity().supportFragmentManager,
                        MoreDialogFragment::class.java.simpleName
                    )
                }
            }

            override fun onAvatarClick(userId: Long, name: String) {
                dismiss()
                commentListener?.onAvatarClick(userId, name)
            }

            override fun loadAvatar(id: Long?, view: ImageView) {
                viewModel.loadImage(id, view, LoadImageType.AVATAR)
            }

        }, CommentViewType.CLIP).apply {
            loadMoreModule.apply {
                isEnableLoadMore = true
                isAutoLoadMore = true
                isEnableLoadMoreIfNotFullPage = false
                loadMoreView = CommentLoadMoreView(CommentViewType.CLIP)
            }
        }
    }

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_comment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(requireContext())
            .load(R.drawable.bg_comment_dialog)
            .transform(BlurTransformation(15, 5))
            .into(iv_blur)

        (arguments?.getSerializable(KEY_DATA) as VideoItem).also { memberPostItem ->
            data = memberPostItem
            tv_comment_count.text = String.format(
                requireContext().getString(R.string.clip_comment_count),
                memberPostItem.commentCount
            )

            rv_comment.adapter = playerInfoAdapter
            lifecycleScope.launchWhenResumed {
                viewModel.setupCommentDataSource(memberPostItem.id, playerInfoAdapter)
            }

            takeIf { memberPostItem.commentCount == 0 }?.also {
                tv_no_data.visibility = View.VISIBLE
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
                    val replyName = tv_replay_name.text.toString()
                    viewModel.postComment(id, PostCommentRequest(replyId, "$replyName $comment"))
                }
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.apiLoadReplyCommentResult.observe(this, { event ->
            event.getContentIfNotHandled()?.also { apiResult ->
                when (apiResult) {
                    is Loading -> {
                    }
                    is Empty -> loadReplyCommentBlock?.also { it() }
                    is Loaded -> loadReplyCommentBlock = null
                    else -> {
                    }
                }
            }
        })

        viewModel.apiPostCommentResult.observe(this, { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Success -> {
                        val isParent = et_message.tag == null

                        GeneralUtils.hideKeyboard(this)
                        et_message.text = null
                        et_message.tag = null
                        tv_replay_name.text = null
                        tv_replay_name.visibility = View.GONE

                        tv_no_data.visibility = View.GONE
                        data?.commentCount =
                            data?.commentCount?.let { count -> count + 1 } ?: run { 1 }
                        tv_comment_count.text = String.format(
                            requireContext().getString(R.string.clip_comment_count),
                            data?.commentCount
                        )

                        if (isParent) {
                            data?.also { memberPostItem ->
                                viewModel.setupCommentDataSource(
                                    memberPostItem.id,
                                    playerInfoAdapter
                                )
                                rv_comment.scrollToPosition(1)
                            }
                        } else {
                            replyRootNode?.also { parentNode ->
                                val parentIndex = playerInfoAdapter.getItemPosition(parentNode)
                                if (parentNode.isExpanded) {
                                    playerInfoAdapter.addData(
                                        parentIndex + 1,
                                        NestedCommentNode(
                                            parentNode as RootCommentNode,
                                            it.result
                                        )
                                    )
                                } else {
                                    loadReplyCommentBlock = {
                                        playerInfoAdapter.expand(
                                            position = parentIndex,
                                            animate = false,
                                            notify = true,
                                            parentPayload = CommentAdapter.EXPAND_COLLAPSE_PAYLOAD
                                        )
                                    }
                                    viewModel.loadReplyComment(
                                        data?.id ?: 0,
                                        parentNode,
                                        parentNode.data.id ?: 0
                                    )
                                }
                            }
                        }

                        commentListener?.onUpdateCommentCount(data?.commentCount?.toInt() ?: 0)
                    }
                    is Error -> onApiError(it.throwable)
                    else -> {
                    }
                }
            }
        })

        viewModel.apiCommentLikeResult.observe(this, { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Empty -> {
                        loadCommentLikeBlock = loadCommentLikeBlock?.let {
                            it()
                            null
                        }
                    }
                    is Error -> onApiError(it.throwable)
                    else -> {
                    }
                }
            }
        })

        viewModel.apiDeleteCommentLikeResult.observe(this, { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Empty -> {
                        loadCommentLikeBlock = loadCommentLikeBlock?.let {
                            it()
                            null
                        }
                    }
                    is Error -> onApiError(it.throwable)
                    else -> {
                    }
                }
            }
        })

    }
}