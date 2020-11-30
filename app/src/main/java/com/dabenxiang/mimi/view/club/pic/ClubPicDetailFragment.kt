package com.dabenxiang.mimi.view.club.pic

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.ClubPostFuncItem
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.ImageItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.text.ClubTextDetailAdapter
import com.dabenxiang.mimi.view.club.text.ClubTextDetailViewModel
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.fullpicture.FullPictureFragment
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_club_text_detail.*

class ClubPicDetailFragment : BaseFragment() {

    private val viewModel: ClubTextDetailViewModel by viewModels()

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    private var memberPostItem: MemberPostItem? = null

    private var pictureDetailAdapter: ClubPicDetailAdapter? = null

    var moreDialog: MoreDialogFragment? = null

    private var adWidth = 0
    private var adHeight = 0

    companion object {
        const val KEY_DATA = "data"
        fun createBundle(item: MemberPostItem): ClubPicDetailFragment {
            val bundle = Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }

            val fragment =
                ClubPicDetailFragment()
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

        memberPostItem = arguments?.get(KEY_DATA) as MemberPostItem

        pictureDetailAdapter =
            ClubPicDetailAdapter(
                requireContext(),
                memberPostItem!!,
                onPictureDetailListener,
                onPhotoGridItemClickListener,
                null,
                clubPostFuncItem
            )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = pictureDetailAdapter

        viewModel.getPostDetail(memberPostItem!!)
        mainViewModel?.getAd(adWidth, adHeight)
    }

    private val onTextDetailListener = object :
        ClubTextDetailAdapter.OnTextDetailListener {
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
            val item = SearchPostItem(type = type, tag = tag)
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
        type: LikeType,
        originType: LikeType?,
        update: (Boolean, MemberPostItem) -> Unit
    ) {
        checkStatus { viewModel.likePost(memberPostItem, isLike, type, originType, update) }
    }

    private val clubPostFuncItem by lazy {
        ClubPostFuncItem(
            {},
            { id, view, type -> viewModel.loadImage(id, view, type) },
            { item, items, isFollow, func -> followMember(item, items, isFollow, func) },
            { item, isLike, type, originType, func -> likePost(item, isLike, type, originType, func) },
            { item, isFavorite, func -> favoritePost(item, isFavorite, func) }
        )
    }

    private val onPictureDetailListener = object : ClubPicDetailAdapter.OnPictureDetailListener {
        override fun onGetAttachment(id: Long?, view: ImageView, type: LoadImageType) {
            viewModel.loadImage(id, view, type)
        }

        override fun onFollowClick(item: MemberPostItem, position: Int, isFollow: Boolean) {
            checkStatus { viewModel.followPost(item, position, isFollow)}
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
            val item = SearchPostItem(type = type, tag = tag)
            val bundle = SearchPostFragment.createBundle(item)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_clubPicFragment_to_searchPostFragment,
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
            navigateTo(NavigateItem.Destination(R.id.action_clubPicFragment_to_myPostFragment, bundle))
        }
    }

    private val onPhotoGridItemClickListener = object : ClubPhotoGridAdapter.OnItemClickListener {
        override fun onItemClick(position: Int, imageItems: ArrayList<ImageItem>) {
            val bundle = FullPictureFragment.createBundle(position, imageItems)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_clubPicFragment_to_pictureFragment,
                    bundle
                )
            )
        }
    }
}