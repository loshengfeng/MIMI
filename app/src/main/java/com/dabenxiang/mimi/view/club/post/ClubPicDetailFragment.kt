package com.dabenxiang.mimi.view.club.post

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.ClubPostFuncItem
import com.dabenxiang.mimi.callback.OnItemClickListener
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
import com.dabenxiang.mimi.view.picturedetail.PhotoGridAdapter
import com.dabenxiang.mimi.view.picturedetail.PictureDetailAdapter
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.RootCommentNode
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_club_text_detail.*
import kotlinx.android.synthetic.main.fragment_order.*
import kotlinx.android.synthetic.main.fragment_picture_detail.*
import kotlinx.android.synthetic.main.item_setting_bar.*

class ClubPicDetailFragment : BaseFragment() {

    private val viewModel: ClubTextDetailViewModel by viewModels()

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    private var memberPostItem: MemberPostItem? = null

    private var pictureDetailAdapter: PictureDetailAdapter? = null

    var moreDialog: MoreDialogFragment? = null

    private var adWidth = 0
    private var adHeight = 0

    companion object {
        const val KEY_DATA = "data"
        fun createBundle(item: MemberPostItem): ClubPicDetailFragment {
            val bundle = Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }

            val fragment = ClubPicDetailFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getLayoutId() = R.layout.fragment_club_pic_detail

    override fun setupObservers() {
    }

    override fun setupListeners() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        adHeight = (adWidth * 0.142).toInt()

        memberPostItem = arguments?.get(ClubPicDetailFragment.KEY_DATA) as MemberPostItem

        pictureDetailAdapter = PictureDetailAdapter(
            requireContext(),
            memberPostItem!!,
            onPictureDetailListener,
            onPhotoGridItemClickListener,
            onItemClickListener
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = pictureDetailAdapter

        viewModel.getPostDetail(memberPostItem!!)
        mainViewModel?.getAd(adWidth, adHeight)
    }

    private val onTextDetailListener = object : ClubTextDetailAdapter.OnTextDetailListener {
        override fun onGetAttachment(id: Long?, view: ImageView) {
            viewModel.loadImage(id, view, LoadImageType.AVATAR)
        }

        override fun onFollowClick(item: MemberPostItem, position: Int, isFollow: Boolean) {
            checkStatus { viewModel.followPost(item, position, isFollow) }
        }

        override fun onMoreClick(item: MemberPostItem) {
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
                    R.id.action_clubTextDetailFragment_to_searchPostFragment,
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

    private fun followMember(
        memberPostItem: MemberPostItem,
        items: List<MemberPostItem>,
        isFollow: Boolean,
        update: (Boolean) -> Unit
    ) {
        checkStatus { viewModel.followMember(memberPostItem, ArrayList(items), isFollow, update) }
    }

    private fun favoritePost(
        memberPostItem: MemberPostItem,
        isFavorite: Boolean,
        update: (Boolean, Int) -> Unit
    ) {
        checkStatus { viewModel.favoritePost(memberPostItem, isFavorite, update) }
    }

    private fun likePost(
        memberPostItem: MemberPostItem,
        isLike: Boolean,
        update: (Boolean, Int) -> Unit
    ) {
        checkStatus { viewModel.likePost(memberPostItem, isLike, update) }
    }

    private val clubPostFuncItem by lazy {
        ClubPostFuncItem(
            {},
            { id, view, type -> viewModel.loadImage(id, view, type) },
            { item, items, isFollow, func -> followMember(item, items, isFollow, func) },
            { item, isLike, func -> likePost(item, isLike, func) },
            { item, isFavorite, func -> favoritePost(item, isFavorite, func) }
        )
    }

    private val onPictureDetailListener = object : PictureDetailAdapter.OnPictureDetailListener {
        override fun onGetAttachment(id: Long?, view: ImageView, type: LoadImageType) {
            viewModel.loadImage(id, view, type)
        }

        override fun onFollowClick(item: MemberPostItem, position: Int, isFollow: Boolean) {
            checkStatus { viewModel.followPost(item, position, isFollow)}
        }

        override fun onGetCommandInfo(adapter: CommentAdapter, type: CommentType) {
//            commentAdapter = adapter
//            viewModel.getCommentInfo(
//                memberPostItem!!.id,
//                type,
//                commentAdapter!!
//            )
        }

        override fun onGetReplyCommand(
            parentNode: RootCommentNode,
            succeededBlock: () -> Unit
        ) {
//            replyCommentBlock = succeededBlock
//            viewModel.getReplyComment(parentNode, memberPostItem!!)
        }

        override fun onCommandLike(
            commentId: Long?,
            isLike: Boolean,
            succeededBlock: () -> Unit
        ) {
            checkStatus {
//                commentLikeBlock = succeededBlock
//                val type = if (isLike) LikeType.LIKE else LikeType.DISLIKE
//                viewModel.postCommentLike(commentId!!, type, memberPostItem!!)
            }
        }

        override fun onCommandDislike(commentId: Long?, succeededBlock: () -> Unit) {
            checkStatus {
//                commentLikeBlock = succeededBlock
//                viewModel.deleteCommentLike(commentId!!, memberPostItem!!)
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
                isAdultTheme = false
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
//            GeneralUtils.hideKeyboard(requireActivity())
//            layout_bar.visibility = View.VISIBLE
//            layout_edit_bar.visibility = View.INVISIBLE
//            et_message.setText("")
        }
    }

}