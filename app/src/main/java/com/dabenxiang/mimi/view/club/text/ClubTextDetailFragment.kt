package com.dabenxiang.mimi.view.club.text

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.ClubPostFuncItem
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_club_text_detail.*

class ClubTextDetailFragment : BaseFragment() {

    private val viewModel: ClubTextDetailViewModel by viewModels()

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    private var memberPostItem: MemberPostItem? = null

    private var textDetailAdapter: ClubTextDetailAdapter? = null

    var moreDialog: MoreDialogFragment? = null

    private var adWidth = 0
    private var adHeight = 0

    companion object {
        const val KEY_DATA = "data"
        fun createBundle(item: MemberPostItem): ClubTextDetailFragment {
            val bundle = Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }

            val fragment =
                ClubTextDetailFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getLayoutId() = R.layout.fragment_club_text_detail

    override fun setupObservers() {
        viewModel.postChangedResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success<*> -> {
                    it.result as MemberPostItem
                    mainViewModel?.itemChangedList?.value?.set(it.result.id, it.result)
                }
                is ApiResult.Error<*> -> onApiError(it.throwable)
            }
        })

        mainViewModel?.deletePostResult?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    navigateTo(NavigateItem.Up)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.postDetailResult.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResult.Success -> {
                    val item = it.result.content
                    textDetailAdapter?.updateContent(item!!)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.getAdResult?.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResult.Success -> {
                    textDetailAdapter?.setupAdItem(it.result)
                    textDetailAdapter?.notifyItemChanged(0)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.followPostResult.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResult.Success -> textDetailAdapter?.notifyItemChanged(it.result)
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adWidth = GeneralUtils.getAdSize(requireActivity()).first
        adHeight = GeneralUtils.getAdSize(requireActivity()).second

        memberPostItem = arguments?.get(KEY_DATA) as MemberPostItem

        textDetailAdapter =
            ClubTextDetailAdapter(
                requireContext(),
                memberPostItem!!,
                onTextDetailListener,
                null,
                clubPostFuncItem
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
            checkStatus { viewModel.followPost(item, position, isFollow) }
        }

        override fun onMoreClick(item: MemberPostItem) {
            onMoreClick(item, -1) {
                it as MemberPostItem

                val bundle = Bundle()
                item.id
                bundle.putBoolean(MyPostFragment.EDIT, true)
                bundle.putString(BasePostFragment.PAGE, BasePostFragment.TEXT)
                bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)

                findNavController().navigate(
                    R.id.action_to_postArticleFragment,
                    bundle
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
            navigateTo(NavigateItem.Destination(R.id.action_to_myPostFragment, bundle))
        }
    }

    override fun onDetach() {
        mainViewModel?.clearDeletePostResult()
        super.onDetach()
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
}