package com.dabenxiang.mimi.view.picturedetail

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.OnItemClickListener
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.ImageItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.CommentType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.fullpicture.FullPictureFragment
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.RootCommentNode
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.fragment_picture_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*
import timber.log.Timber

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

    private var memberPostItem: MemberPostItem? = null

    private var replyCommentBlock: (() -> Unit)? = null
    private var commentLikeBlock: (() -> Unit)? = null
    private var avatarBlock: ((Bitmap) -> Unit)? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        memberPostItem = arguments?.getSerializable(KEY_DATA) as MemberPostItem

        requireActivity().onBackPressedDispatcher.addCallback { navigateTo(NavigateItem.Up) }

        val memberPostItem = arguments?.getSerializable(KEY_DATA) as MemberPostItem

        val position = arguments?.getInt(KEY_POSITION) ?: 0

        text_toolbar_title.text = getString(R.string.picture_detail_title)
        toolbarContainer.toolbar.navigationIcon =
            requireContext().getDrawable(R.drawable.btn_back_white_n)
        toolbarContainer.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        pictureDetailAdapter = PictureDetailAdapter(
            requireContext(),
            memberPostItem,
            onPictureDetailListener,
            onPhotoGridItemClickListener,
            onItemClickListener
        )
        recycler_picture_detail.layoutManager = LinearLayoutManager(context)
        recycler_picture_detail.adapter = pictureDetailAdapter
        recycler_picture_detail.scrollToPosition(position)

        text_toolbar_title.setOnClickListener {
            GeneralUtils.closeKeyboard(requireContext())
            layout_bar.visibility = View.VISIBLE
            layout_edit_bar.visibility = View.INVISIBLE
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_picture_detail
    }

    override fun setupObservers() {
        viewModel.attachmentResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    val item = it.result
                    LruCacheUtils.putLruCache(item.id!!, item.bitmap!!)
                    pictureDetailAdapter?.updatePhotoGridItem(item.position!!)
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.followPostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> pictureDetailAdapter?.notifyItemChanged(it.result)
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.avatarResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> avatarBlock?.invoke(it.result)
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.replyCommentResult.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Empty -> replyCommentBlock?.also { it() }
                    is Error -> Timber.e(it.throwable)
                }
            }
        })

        viewModel.commentLikeResult.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Empty -> commentLikeBlock?.also { it() }
                    is Error -> Timber.e(it.throwable)
                }
            }
        })

        viewModel.commentDeleteLikeResult.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.also {
                when (it) {
                    is Empty -> commentLikeBlock?.also { it() }
                    is Error -> Timber.e(it.throwable)
                }
            }
        })
    }

    override fun setupListeners() {
    }

    private val onPictureDetailListener = object : PictureDetailAdapter.OnPictureDetailListener {
        override fun onGetAttachment(id: String, position: Int) {
            viewModel.getAttachment(id, position)
        }

        override fun onFollowClick(item: MemberPostItem, position: Int, isFollow: Boolean) {
            viewModel.followPost(item, position, isFollow)
        }

        override fun onGetCommandInfo(adapter: CommentAdapter, type: CommentType) {
            viewModel.getCommentInfo(
                memberPostItem!!.id,
                type,
                adapter
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
            commentLikeBlock = succeededBlock
            val type = if (isLike) LikeType.LIKE else LikeType.DISLIKE
            viewModel.postCommentLike(commentId!!, type, memberPostItem!!)
        }

        override fun onCommandDislike(commentId: Long?, succeededBlock: () -> Unit) {
            commentLikeBlock = succeededBlock
            viewModel.deleteCommentLike(commentId!!, memberPostItem!!)
        }

        override fun onGetCommandAvatar(id: Long, succeededBlock: (Bitmap) -> Unit) {
            avatarBlock = succeededBlock
            viewModel.getAvatar(id.toString())
        }

        override fun onReplyComment(replyId: Long?, replyName: String?) {
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
            GeneralUtils.closeKeyboard(requireContext())
            layout_bar.visibility = View.VISIBLE
            layout_edit_bar.visibility = View.INVISIBLE
        }
    }
}