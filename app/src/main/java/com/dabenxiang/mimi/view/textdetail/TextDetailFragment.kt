package com.dabenxiang.mimi.view.textdetail

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.OnItemClickListener
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.MembersPostCommentItem
import com.dabenxiang.mimi.model.enums.CommentType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.RootCommentNode
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_text_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*

class TextDetailFragment : BaseFragment() {

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

    private var memberPostItem: MemberPostItem? = null

    private val viewModel: TextDetailViewModel by viewModels()

    private var textDetailAdapter: TextDetailAdapter? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        memberPostItem =
            arguments?.getSerializable(PictureDetailFragment.KEY_DATA) as MemberPostItem
        val position = arguments?.getInt(PictureDetailFragment.KEY_POSITION) ?: 0

        requireActivity().onBackPressedDispatcher.addCallback { navigateTo(NavigateItem.Up) }

        text_toolbar_title.text = getString(R.string.text_detail_title)
        toolbarContainer.toolbar.navigationIcon =
            requireContext().getDrawable(R.drawable.btn_back_white_n)
        toolbarContainer.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        textDetailAdapter = TextDetailAdapter(
            requireContext(),
            memberPostItem!!,
            onTextDetailListener,
            onItemClickListener
        )

        recycler_text_detail.layoutManager = LinearLayoutManager(context)
        recycler_text_detail.adapter = textDetailAdapter
        recycler_text_detail.scrollToPosition(position)

        if (memberPostItem!!.likeType == LikeType.LIKE) {
            iv_like.setImageResource(R.drawable.ico_nice_s)
        } else {
            iv_like.setImageResource(R.drawable.ico_nice)
        }

        tv_like_count.text = memberPostItem!!.likeCount.toString()
        tv_comment_count.text = memberPostItem!!.commentCount.toString()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_text_detail
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {

    }

    private val onTextDetailListener = object : TextDetailAdapter.OnTextDetailListener {
        override fun onGetAttachment(id: String, position: Int) {

        }

        override fun onFollowClick(item: MemberPostItem, position: Int, isFollow: Boolean) {

        }

        override fun onGetCommandInfo(adapter: CommentAdapter, type: CommentType) {

        }

        override fun onGetReplyCommand(parentNode: RootCommentNode, succeededBlock: () -> Unit) {

        }

        override fun onCommandLike(commentId: Long?, isLike: Boolean, succeededBlock: () -> Unit) {

        }

        override fun onCommandDislike(commentId: Long?, succeededBlock: () -> Unit) {

        }

        override fun onGetCommandAvatar(id: Long, succeededBlock: (Bitmap) -> Unit) {

        }

        override fun onReplyComment(replyId: Long?, replyName: String?) {

        }

        override fun onMoreClick(item: MembersPostCommentItem) {

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
}