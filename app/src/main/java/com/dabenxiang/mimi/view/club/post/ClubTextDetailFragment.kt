package com.dabenxiang.mimi.view.club.post

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.OnItemClickListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.MembersPostCommentItem
import com.dabenxiang.mimi.model.enums.CommentType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.RootCommentNode
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_club_text_detail.*

class ClubTextDetailFragment : BaseFragment() {

    private val viewModel: ClubTextDetailViewModel by viewModels()

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    private var memberPostItem: MemberPostItem? = null

    private var textDetailAdapter: ClubTextDetailAdapter? = null

    private var adWidth = 0
    private var adHeight = 0

    companion object {
        const val KEY_DATA = "data"
        fun createBundle(item: MemberPostItem): ClubTextDetailFragment {
            val bundle = Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }

            val fragment = ClubTextDetailFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getLayoutId() = R.layout.fragment_club_text_detail

    override fun setupObservers() {
        viewModel.postDetailResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    val item = it.result.content
                    textDetailAdapter?.updateContent(item!!)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.getAdResult?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    textDetailAdapter?.setupAdItem(it.result)
                    textDetailAdapter?.notifyItemChanged(0)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        adHeight = (adWidth * 0.142).toInt()

        memberPostItem = arguments?.get(KEY_DATA) as MemberPostItem

        textDetailAdapter = ClubTextDetailAdapter(
            requireContext(),
            memberPostItem!!,
            onTextDetailListener,
            onItemClickListener
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = textDetailAdapter

        viewModel.getPostDetail(memberPostItem!!)
        mainViewModel?.getAd(adWidth, adHeight)
    }

    private val onTextDetailListener = object : ClubTextDetailAdapter.OnTextDetailListener {
        override fun onGetAttachment(id: Long?, view: ImageView) {
            viewModel.loadImage(id, view, LoadImageType.AVATAR)
        }

        override fun onFollowClick(item: MemberPostItem, position: Int, isFollow: Boolean) {
//            checkStatus { viewModel.followPost(item, position, isFollow) }
        }

        override fun onGetCommandInfo(adapter: CommentAdapter, type: CommentType) {
        }

        override fun onGetReplyCommand(parentNode: RootCommentNode, succeededBlock: () -> Unit) {
//            replyCommentBlock = succeededBlock
//            viewModel.getReplyComment(parentNode, memberPostItem!!)
        }

        override fun onCommandLike(commentId: Long?, isLike: Boolean, succeededBlock: () -> Unit) {
//            checkStatus {
//                commentLikeBlock = succeededBlock
//                val type = if (isLike) LikeType.LIKE else LikeType.DISLIKE
//                viewModel.postCommentLike(commentId!!, type, memberPostItem!!)
//            }
        }

        override fun onCommandDislike(commentId: Long?, succeededBlock: () -> Unit) {
//            checkStatus {
//                commentLikeBlock = succeededBlock
//                viewModel.deleteCommentLike(commentId!!, memberPostItem!!)
//            }
        }

        override fun onReplyComment(replyId: Long?, replyName: String?) {
//            checkStatus {
//                takeUnless { replyId == null }?.also {
//                    layout_bar.visibility = View.INVISIBLE
//                    layout_edit_bar.visibility = View.VISIBLE
//
//                    GeneralUtils.showKeyboard(requireContext())
//                    et_message.requestFocus()
//                    et_message.tag = replyId
//                    tv_replay_name.text = replyName.takeIf { it != null }?.let {
//                        tv_replay_name.visibility = View.VISIBLE
//                        String.format(requireContext().getString(R.string.clip_username), it)
//                    } ?: run { "" }
//                }
//            }
        }

        override fun onMoreClick(item: MembersPostCommentItem) {
//            moreDialog = MoreDialogFragment.newInstance(item, onMoreDialogListener, true).also {
//                it.show(
//                    requireActivity().supportFragmentManager,
//                    MoreDialogFragment::class.java.simpleName
//                )
//            }
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
            navigateTo(NavigateItem.Destination(R.id.action_to_myPostFragment, bundle))
        }
    }

    private val onItemClickListener = object : OnItemClickListener {
        override fun onItemClick() {
            GeneralUtils.hideKeyboard(requireActivity())
//            layout_bar.visibility = View.VISIBLE
//            layout_edit_bar.visibility = View.INVISIBLE
//            et_message.setText("")
        }
    }
}