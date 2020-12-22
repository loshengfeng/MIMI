package com.dabenxiang.mimi.view.club.post

import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.dabenxiang.mimi.view.club.text.ClubTextDetailFragment
import com.dabenxiang.mimi.view.club.text.ClubTextDetailViewModel
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.NestedCommentNode
import com.dabenxiang.mimi.view.player.RootCommentNode
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_club_comment.*

class ClubCommentFragment : BaseFragment() {

    private val viewModel: ClubTextDetailViewModel by viewModels()

    private var memberPostItem: MemberPostItem? = null

    private var textDetailAdapter: ClubCommentAdapter? = null
    private var commentAdapter: CommentAdapter? = null

    private var adWidth = 0
    private var adHeight = 0

    var moreDialog: MoreDialogFragment? = null

    private var replyCommentBlock: (() -> Unit)? = null
    private var commentLikeBlock: (() -> Unit)? = null

    var replyRootNode: RootCommentNode? = null

    var lastClickY = 0

    companion object {
        const val KEY_DATA = "data"
        const val KEY_IS_DARK_MODE = "is_dark_mode"
        const val KEY_AD_CODE = "AD_CODE"
        fun createBundle(
            item: MemberPostItem,
            isDarkMode: Boolean = false,
            adCode: String = ""
        ): ClubCommentFragment {
            val bundle = Bundle().also {
                it.putSerializable(KEY_DATA, item)
                it.putBoolean(KEY_IS_DARK_MODE, isDarkMode)
                it.putString(KEY_AD_CODE, adCode)
            }

            val fragment = ClubCommentFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId() = R.layout.fragment_club_comment

    override fun setupObservers() {
        mainViewModel?.getAdResult?.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResult.Success -> {
                    textDetailAdapter?.setupAdItem(it.result)
                    textDetailAdapter?.notifyItemChanged(0)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.replyCommentResult.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is ApiResult.Empty -> replyCommentBlock?.also { it() }
                    is ApiResult.Error -> onApiError(it.throwable)
                }
            }
        })

        viewModel.commentLikeResult.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is ApiResult.Empty -> commentLikeBlock?.also { it() }
                    is ApiResult.Error -> onApiError(it.throwable)
                }
            }
        })

        viewModel.commentDeleteLikeResult.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is ApiResult.Empty -> commentLikeBlock?.also { it() }
                    is ApiResult.Error -> onApiError(it.throwable)
                }
            }
        })

        viewModel.postCommentResult.observe(this, { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is ApiResult.Success -> {
                        val isParent = et_message.tag == null

                        GeneralUtils.hideKeyboard(requireActivity())
                        et_message.text = null
                        et_message.tag = null
                        tv_replay_name.text = null
                        tv_replay_name.visibility = View.GONE

                        memberPostItem?.commentCount =
                            memberPostItem?.commentCount?.let { count -> count + 1 } ?: run { 1 }

                        if (isParent) {
                            memberPostItem?.also { memberPostItem ->
                                viewModel.getCommentInfo(
                                    memberPostItem.id,
                                    viewModel.currentCommentType,
                                    commentAdapter!!
                                )
                                recyclerView.scrollToPosition(1)
                            }
                        } else {
                            replyRootNode?.also { parentNode ->
                                val parentIndex = commentAdapter?.getItemPosition(parentNode)!!
                                if (parentNode.isExpanded) {
                                    commentAdapter?.addData(
                                        parentIndex + 1,
                                        NestedCommentNode(
                                            parentNode as RootCommentNode,
                                            it.result
                                        )
                                    )
                                    adjustScroll(parentIndex)
                                } else {
                                    replyCommentBlock = {
                                        commentAdapter?.expand(
                                            position = parentIndex,
                                            animate = false,
                                            notify = true,
                                            parentPayload = CommentAdapter.EXPAND_COLLAPSE_PAYLOAD
                                        )
                                        adjustScroll(parentIndex)
                                    }
                                    viewModel.getReplyComment(parentNode, memberPostItem!!)
                                }
                            }
                        }
                    }
                    is ApiResult.Error -> onApiError(it.throwable)
                }
            }
        })
    }

    private fun adjustScroll(parentIndex: Int) {
        commentAdapter?.recyclerView?.also { rv ->
            recyclerView.postDelayed({
                val vChild = rv.getChildAt(parentIndex + 1)
                val vChildLoc = IntArray(2)
                vChild.getLocationOnScreen(vChildLoc)

                val barLoc = IntArray(2)
                layout_edit_bar.getLocationOnScreen(barLoc)
                val barTop = barLoc[1] - layout_edit_bar.height / 2

                val center = barTop - content.height / 2

                recyclerView.scrollBy(0, vChildLoc[1] - center)
            }, 500)
        }
    }

    override fun setupListeners() {
        btn_send.setOnClickListener {
            checkStatus {
                memberPostItem?.id?.let { id ->
                    et_message.text.toString().takeIf { !TextUtils.isEmpty(it) }?.let { comment ->
                        Pair(id, comment)
                    }?.also { (id, comment) ->
                        val replyId = et_message.tag?.let { rid -> rid as Long }
                        val replyName = tv_replay_name.text.toString()
                        viewModel.postComment(id, replyId, "$replyName $comment")
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adWidth = GeneralUtils.getAdSize(requireActivity()).first
        adHeight = GeneralUtils.getAdSize(requireActivity()).second

        memberPostItem = arguments?.get(ClubTextDetailFragment.KEY_DATA) as MemberPostItem
        val isDarkMode = arguments?.getBoolean(KEY_IS_DARK_MODE)
        viewModel.getPostDetail(memberPostItem!!)
        mainViewModel?.setStatusBarMode(isDarkMode!!)

        textDetailAdapter = ClubCommentAdapter(
            requireContext(),
            memberPostItem!!,
            onTextDetailListener,
            onItemClickListener
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = textDetailAdapter
        recyclerView.addOnLayoutChangeListener { view, left, top, right, bottom, oLeft, oTop, oRight, oBottom ->
            if (oBottom > bottom && bottom < lastClickY) {
                recyclerView.scrollBy(0, lastClickY - bottom)
                lastClickY = bottom + 1
            }
        }

        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (e.action == MotionEvent.ACTION_UP) lastClickY = e.getY(0).toInt()
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            }

        })
        val adCode = arguments?.getString(KEY_AD_CODE) ?: ""
        mainViewModel?.getAd(adCode, adWidth, adHeight, 1)
    }

    private val onTextDetailListener = object : ClubCommentAdapter.OnTextDetailListener {
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

        override fun onReplyComment(
            replyId: Long?,
            replyName: String?,
            parentNode: RootCommentNode
        ) {
            checkStatus {
                takeUnless { replyId == null }?.also {
                    replyRootNode = parentNode
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
            val item = SearchPostItem(type = type, tag = tag)
            val bundle = SearchPostFragment.createBundle(item)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_searchPostFragment,
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
            navigateTo(NavigateItem.Destination(R.id.action_to_myPostFragment, bundle))
        }
    }

    private val onItemClickListener = object : OnItemClickListener {
        override fun onItemClick() {
            GeneralUtils.hideKeyboard(requireActivity())
            et_message.setText("")
        }
    }

    private val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
        override fun onProblemReport(item: BaseMemberPostItem, isComment: Boolean) {
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