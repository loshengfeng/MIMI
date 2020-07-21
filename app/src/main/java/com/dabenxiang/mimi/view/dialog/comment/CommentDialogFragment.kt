package com.dabenxiang.mimi.view.dialog.comment

import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.CommentViewType
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.dialog.ReportDialogFragment
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.CommentLoadMoreView
import com.dabenxiang.mimi.view.player.RootCommentNode
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.fragment_dialog_comment.*
import timber.log.Timber


class CommentDialogFragment : BaseDialogFragment() {

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

    var moreDialog: MoreDialogFragment? = null
    var reportDialog: ReportDialogFragment? = null

    private val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
        override fun onProblemReport(item: BaseMemberPostItem) {
            Timber.d("@@onProblemReport")
            moreDialog?.dismiss()
            reportDialog = ReportDialogFragment.newInstance(item, onReportDialogListener).also {
                Timber.d("@@ReportDialogFragment")
                it.show(
                    requireActivity().supportFragmentManager,
                    ReportDialogFragment::class.java.simpleName
                )
            }
        }

        override fun onCancel() {
            moreDialog?.dismiss()
        }
    }

    private val onReportDialogListener = object : ReportDialogFragment.OnReportDialogListener {
        override fun onSend(item: BaseMemberPostItem, content: String) {
            if (TextUtils.isEmpty(content)) {
                GeneralUtils.showToast(requireContext(), getString(R.string.report_error))
            } else {
                reportDialog?.dismiss()
                when (item) {
                    is MemberPostItem -> viewModel.sendPostReport(item, content)
                    else -> {
                        data?.also {
                            viewModel.sendCommentPostReport(
                                it,
                                (item as MembersPostCommentItem),
                                content
                            )
                        }
                    }
                }
            }
        }

        override fun onCancel() {
            reportDialog?.dismiss()
        }
    }

    private val playerInfoAdapter by lazy {
        CommentAdapter(true, object : CommentAdapter.PlayerInfoListener {
            override fun sendComment(replyId: Long?, replyName: String?) {
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

            override fun replyComment(replyId: Long?, replyName: String?) {
                takeUnless { replyId == null }?.also {
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

            override fun getBitmap(id: Long, succeededBlock: (Bitmap) -> Unit) {
                viewModel.getBitmap(id.toString(), succeededBlock)
            }

            override fun onMoreClick(item: MembersPostCommentItem) {
                moreDialog = MoreDialogFragment.newInstance(item, onMoreDialogListener).also {
                    it.show(
                        requireActivity().supportFragmentManager,
                        MoreDialogFragment::class.java.simpleName
                    )
                }
            }

        }, CommentViewType.CLIP).apply {
            loadMoreModule.apply {
                isEnableLoadMore = true
                isAutoLoadMore = true
                isEnableLoadMoreIfNotFullPage = false
                loadMoreView = CommentLoadMoreView(true, CommentViewType.CLIP)
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
            .transform(BlurTransformation(25, 5))
            .into(iv_blur)

        (arguments?.getSerializable(KEY_DATA) as MemberPostItem).also { memberPostItem ->
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
                    is Loading -> {
//                        progressHUD.show()
                    }
                    is Empty -> {
                        loadReplyCommentBlock?.also { it() }
                    }
                    is Loaded -> {
                        loadReplyCommentBlock = null
                    }
                }
            }
        })

        viewModel.apiPostCommentResult.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Empty -> {
                        GeneralUtils.hideKeyboard(requireActivity())
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

                        data?.also { memberPostItem ->
                            viewModel.setupCommentDataSource(memberPostItem.id, playerInfoAdapter)
                        }

                    }
                    is Error -> {
                    }
                }
            }
        })

        viewModel.apiCommentLikeResult.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Empty -> {
                        loadCommentLikeBlock = loadCommentLikeBlock?.let {
                            it()
                            null
                        }
                    }
                    is Error -> {
                    }
                }
            }
        })

        viewModel.apiDeleteCommentLikeResult.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Empty -> {
                        loadCommentLikeBlock = loadCommentLikeBlock?.let {
                            it()
                            null
                        }
                    }
                    is Error -> {
                    }
                }
            }
        })

        viewModel.postReportResult.observe(this, Observer {
            when (it) {
                is Empty -> {
                    GeneralUtils.showToast(requireContext(), getString(R.string.report_success))
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.postCommentReportResult.observe(this, Observer {
            when (it) {
                is Empty -> {
                    GeneralUtils.showToast(requireContext(), getString(R.string.report_success))
                }
                is Error -> Timber.e(it.throwable)
            }
        })
    }
}