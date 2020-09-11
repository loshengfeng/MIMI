package com.dabenxiang.mimi.view.picturedetail

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.OnItemClickListener
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.ImageItem
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
import com.dabenxiang.mimi.view.fullpicture.FullPictureFragment
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.RootCommentNode
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_picture_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*

class PictureDetailFragment : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"
        const val KEY_POSITION = "position"
        fun createBundle(item: MemberPostItem, position: Int): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
                it.putInt(KEY_POSITION, position)
            }
        }
    }

    private val viewModel: PictureDetailViewModel by viewModels()

    private var pictureDetailAdapter: PictureDetailAdapter? = null
    private var commentAdapter: CommentAdapter? = null

    private var memberPostItem: MemberPostItem? = null

    private var replyCommentBlock: (() -> Unit)? = null
    private var commentLikeBlock: (() -> Unit)? = null

    private var moreDialog: MoreDialogFragment? = null

    private var adWidth = 0
    private var adHeight = 0

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        useAdultTheme(true)

        memberPostItem = arguments?.getSerializable(KEY_DATA) as MemberPostItem
        val position = arguments?.getInt(KEY_POSITION) ?: 0

        requireActivity().onBackPressedDispatcher.addCallback { navigateTo(NavigateItem.Up) }

        adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        adHeight = (adWidth * 0.142).toInt()

        text_toolbar_title.text = getString(R.string.picture_detail_title)
        toolbarContainer.toolbar.navigationIcon =
            requireContext().getDrawable(R.drawable.btn_back_white_n)
        toolbarContainer.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        pictureDetailAdapter = PictureDetailAdapter(
            requireContext(),
            memberPostItem!!,
            onPictureDetailListener,
            onPhotoGridItemClickListener,
            onItemClickListener
        )
        recycler_picture_detail.layoutManager = LinearLayoutManager(context)
        recycler_picture_detail.adapter = pictureDetailAdapter
        recycler_picture_detail.scrollToPosition(position)

        if (memberPostItem!!.likeType == LikeType.LIKE) {
            iv_like.setImageResource(R.drawable.ico_nice_s)
        } else {
            iv_like.setImageResource(R.drawable.ico_nice)
        }
        tv_like_count.text = memberPostItem!!.likeCount.toString()
        tv_comment_count.text = memberPostItem!!.commentCount.toString()

        mainViewModel?.getAd(adWidth, adHeight)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_picture_detail
    }

    override fun setupObservers() {
        viewModel.followPostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> pictureDetailAdapter?.notifyItemChanged(it.result)
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.replyCommentResult.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Empty -> replyCommentBlock?.also { it() }
                    is Error -> onApiError(it.throwable)
                }
            }
        })

        viewModel.commentLikeResult.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Empty -> commentLikeBlock?.also { it() }
                    is Error -> onApiError(it.throwable)
                }
            }
        })

        viewModel.commentDeleteLikeResult.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Empty -> commentLikeBlock?.also { it() }
                    is Error -> onApiError(it.throwable)
                }
            }
        })

        viewModel.postCommentResult.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Empty -> {
                        GeneralUtils.hideKeyboard(requireActivity())
                        et_message.text = null
                        et_message.tag = null
                        tv_replay_name.text = null
                        tv_replay_name.visibility = View.GONE

                        layout_bar.visibility = View.VISIBLE
                        layout_edit_bar.visibility = View.INVISIBLE

                        memberPostItem?.commentCount =
                            memberPostItem?.commentCount?.let { count -> count + 1 } ?: run { 1 }
                        tv_comment_count.text = memberPostItem?.commentCount.toString()

                        memberPostItem?.also { memberPostItem ->
                            viewModel.getCommentInfo(
                                memberPostItem.id,
                                viewModel.currentCommentType,
                                commentAdapter!!
                            )
                        }
                        pictureDetailAdapter?.notifyItemChanged(3)
                    }
                    is Error -> onApiError(it.throwable)
                }
            }
        })

        viewModel.likePostResult.observe(this, Observer {
            when (it) {
                is Success -> {
                    val item = it.result
                    if (item.likeType == LikeType.LIKE) {
                        iv_like.setImageResource(R.drawable.ico_nice_s)
                    } else {
                        iv_like.setImageResource(R.drawable.ico_nice)
                    }
                    tv_like_count.text = item.likeCount.toString()

                }
                is Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.getAdResult?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    pictureDetailAdapter?.setupAdItem(it.result)
                    pictureDetailAdapter?.notifyItemChanged(0)
                }
                is Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {
        iv_bar.setOnClickListener {
            checkStatus {
                layout_edit_bar.visibility = View.VISIBLE
                layout_bar.visibility = View.INVISIBLE
                GeneralUtils.showKeyboard(requireContext())
                et_message.requestFocus()
                et_message.setText("")
            }
        }

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

        iv_like.setOnClickListener {
            checkStatus {
                val likeType = memberPostItem?.likeType
                val isLike = likeType == LikeType.LIKE
                viewModel.likePost(memberPostItem!!, !isLike)
            }
        }

        iv_more.setOnClickListener {
            moreDialog = MoreDialogFragment.newInstance(
                memberPostItem!!,
                onMoreDialogListener
            ).also {
                it.show(
                    requireActivity().supportFragmentManager,
                    MoreDialogFragment::class.java.simpleName
                )
            }
        }
    }

    private val onPictureDetailListener = object : PictureDetailAdapter.OnPictureDetailListener {
        override fun onGetAttachment(id: Long?, view: ImageView, type: LoadImageType) {
            viewModel.loadImage(id, view, type)
        }

        override fun onFollowClick(item: MemberPostItem, position: Int, isFollow: Boolean) {
            checkStatus { viewModel.followPost(item, position, isFollow)}
        }

        override fun onGetCommandInfo(adapter: CommentAdapter, type: CommentType) {
            commentAdapter = adapter
            viewModel.getCommentInfo(
                memberPostItem!!.id,
                type,
                commentAdapter!!
            )
        }

        override fun onGetReplyCommand(
            parentNode: RootCommentNode,
            succeededBlock: () -> Unit
        ) {
            replyCommentBlock = succeededBlock
            viewModel.getReplyComment(parentNode, memberPostItem!!)
        }

        override fun onCommandLike(
            commentId: Long?,
            isLike: Boolean,
            succeededBlock: () -> Unit
        ) {
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
                    layout_bar.visibility = View.INVISIBLE
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
                    R.id.action_pictureDetailFragment_to_searchPostFragment,
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
                isAdultTheme = true
            )
            navigateTo(NavigateItem.Destination(R.id.action_to_myPostFragment, bundle))
        }
    }

    private val onPhotoGridItemClickListener = object : PhotoGridAdapter.OnItemClickListener {
        override fun onItemClick(position: Int, imageItems: ArrayList<ImageItem>) {
            val bundle = FullPictureFragment.createBundle(position, imageItems)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_pictureDetailFragment_to_pictureFragment,
                    bundle
                )
            )
        }
    }

    private val onItemClickListener = object : OnItemClickListener {
        override fun onItemClick() {
            GeneralUtils.hideKeyboard(requireActivity())
            layout_bar.visibility = View.VISIBLE
            layout_edit_bar.visibility = View.INVISIBLE
            et_message.setText("")
        }
    }

    private val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
        override fun onProblemReport(item: BaseMemberPostItem, isComment:Boolean) {
            moreDialog?.dismiss()
            checkStatus { (requireActivity() as MainActivity).showReportDialog(item, memberPostItem, isComment) }
        }

        override fun onCancel() {
            moreDialog?.dismiss()
        }
    }
}