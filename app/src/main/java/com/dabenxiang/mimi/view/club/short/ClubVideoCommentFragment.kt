package com.dabenxiang.mimi.view.club.post

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.OnItemClickListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.MembersPostCommentItem
import com.dabenxiang.mimi.model.enums.CommentType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.RootCommentNode
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.view.textdetail.ClubVideoCommentAdapter
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_club_comment.btn_send
import kotlinx.android.synthetic.main.fragment_club_comment.et_message
import kotlinx.android.synthetic.main.fragment_club_comment.layout_edit_bar
import kotlinx.android.synthetic.main.fragment_club_comment.tv_replay_name
import kotlinx.android.synthetic.main.fragment_club_text_detail.recyclerView

class ClubVideoCommentFragment : BaseFragment() {

    private val viewModel: ClubTextDetailViewModel by viewModels()

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    private var memberPostItem: MemberPostItem? = null

    private var textDetailAdapter: ClubVideoCommentAdapter? = null
    private var commentAdapter: CommentAdapter? = null

    private var adWidth = 0
    private var adHeight = 0

    var moreDialog: MoreDialogFragment? = null

    private var replyCommentBlock: (() -> Unit)? = null
    private var commentLikeBlock: (() -> Unit)? = null

    companion object {
        const val KEY_DATA = "data"
        fun createBundle(item: MemberPostItem): ClubCommentFragment {
            val bundle = Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }

            val fragment = ClubCommentFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getLayoutId() = R.layout.fragment_club_comment

    override fun setupObservers() {
        mainViewModel?.getAdResult?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    textDetailAdapter?.setupAdItem(it.result)
                    textDetailAdapter?.notifyItemChanged(0)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.replyCommentResult.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is ApiResult.Empty -> replyCommentBlock?.also { it() }
                    is ApiResult.Error -> onApiError(it.throwable)
                }
            }
        })

        viewModel.commentLikeResult.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is ApiResult.Empty -> commentLikeBlock?.also { it() }
                    is ApiResult.Error -> onApiError(it.throwable)
                }
            }
        })

        viewModel.commentDeleteLikeResult.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is ApiResult.Empty -> commentLikeBlock?.also { it() }
                    is ApiResult.Error -> onApiError(it.throwable)
                }
            }
        })

        viewModel.postCommentResult.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is ApiResult.Empty -> {
                        GeneralUtils.hideKeyboard(requireActivity())
                        et_message.text = null
                        et_message.tag = null
                        tv_replay_name.text = null
                        tv_replay_name.visibility = View.GONE

                        memberPostItem?.commentCount =
                            memberPostItem?.commentCount?.let { count -> count + 1 } ?: run { 1 }

                        memberPostItem?.also { memberPostItem ->
                            viewModel.getCommentInfo(
                                memberPostItem.id,
                                viewModel.currentCommentType,
                                commentAdapter!!
                            )
                        }
                        textDetailAdapter?.notifyItemChanged(3)
                    }
                    is ApiResult.Error -> onApiError(it.throwable)
                }
            }
        })
    }

    override fun setupListeners() {
        btn_send.setOnClickListener {
            checkStatus {
                memberPostItem?.id?.let { id ->
                    et_message.text.toString().takeIf { !TextUtils.isEmpty(it) }?.let { comment ->
                        Pair(id, comment)
                    }?.also { (id, comment) ->
                        val replyId = et_message.tag?.let { rid -> rid as Long }
                        viewModel.postComment(id, replyId, comment)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        adHeight = (adWidth * 0.142).toInt()

        memberPostItem = arguments?.get(ClubTextDetailFragment.KEY_DATA) as MemberPostItem
        viewModel.getPostDetail(memberPostItem!!)

        textDetailAdapter = ClubVideoCommentAdapter(
            requireContext(),
            memberPostItem!!,
            onTextDetailListener,
            onItemClickListener
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = textDetailAdapter

        mainViewModel?.getAd(adWidth, adHeight)
    }

    private val onTextDetailListener = object : ClubVideoCommentAdapter.OnTextDetailListener {
        override fun onGetAttachment(id: Long?, view: ImageView) {
            viewModel.loadImage(id, view, LoadImageType.AVATAR)
        }

        override fun onGetCommandInfo(adapter: CommentAdapter, type: CommentType) {
            commentAdapter = adapter
            viewModel.getCommentInfo(
                memberPostItem!!.id,
                type,
                commentAdapter!!
            )
        }

        override fun onGetReplyCommand(parentNode: RootCommentNode, succeededBlock: () -> Unit) {
            replyCommentBlock = succeededBlock
            viewModel.getReplyComment(parentNode, memberPostItem!!)
        }

        override fun onCommandLike(commentId: Long?, isLike: Boolean, succeededBlock: () -> Unit) {
            checkStatus {
                commentLikeBlock = succeededBlock
                val type = if (isLike) LikeType.LIKE else LikeType.DISLIKE
                viewModel.postCommentLike(commentId!!, type, memberPostItem!!)
            }
        }

        override fun onCommandDislike(commentId: Long?, succeededBlock: () -> Unit) {
            checkStatus {
                commentLikeBlock = succeededBlock
                viewModel.deleteCommentLike(commentId!!, memberPostItem!!)
            }
        }

        override fun onReplyComment(replyId: Long?, replyName: String?) {
            checkStatus {
                takeUnless { replyId == null }?.also {
                    layout_edit_bar.visibility = View.VISIBLE

                    GeneralUtils.showKeyboard(requireContext())
                    et_message.requestFocus()
                    et_message.tag = replyId
                    tv_replay_name.text = replyName.takeIf { it != null }?.let {
                        tv_replay_name.visibility = View.VISIBLE
                        String.format(requireContext().getString(R.string.clip_username), it)
                    } ?: run { "" }
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

        override fun onChipClick(type: PostType, tag: String) {
            val item = SearchPostItem(type, tag)
            val bundle = SearchPostFragment.createBundle(item)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_textDetailFragment_to_searchPostFragment,
                    bundle
                )
            )
        }

        override fun onOpenWebView(url: String) {
            GeneralUtils.openWebView(requireContext(), url)
        }

        override fun onAvatarClick(userId: Long, name: String) {
            val bundle = MyPostFragment.createBundle(
                userId, name,
                isAdult = true,
                isAdultTheme = false
            )
            navigateTo(NavigateItem.Destination(R.id.action_clubTextDetailFragment_to_myPostFragment, bundle))
        }
    }

    private val onItemClickListener = object : OnItemClickListener {
        override fun onItemClick() {
            GeneralUtils.hideKeyboard(requireActivity())
            et_message.setText("")
        }
    }

    private val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
        override fun onProblemReport(item: BaseMemberPostItem, isComment:Boolean) {
            moreDialog?.dismiss()
            checkStatus {
                (requireActivity() as MainActivity).showReportDialog(
                    item,
                    memberPostItem,
                    isComment
                )
            }
        }

        override fun onCancel() {
            moreDialog?.dismiss()
        }
    }
}