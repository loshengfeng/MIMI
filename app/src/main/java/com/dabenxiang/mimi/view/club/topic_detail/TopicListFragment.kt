package com.dabenxiang.mimi.view.club.topic_detail

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.*
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.pages.ClubItemAdapter
import com.dabenxiang.mimi.view.club.pic.ClubPicFragment
import com.dabenxiang.mimi.view.club.text.ClubTextFragment
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.player.ui.ClipPlayerFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_club_item.*
import kotlinx.android.synthetic.main.fragment_my_collection_favorites.layout_refresh
import kotlinx.android.synthetic.main.item_club_is_not_login.*
import timber.log.Timber
import java.util.*

class TopicListFragment(private val orderBy: OrderBy, private val topicTag:String) : BaseFragment() {

    private val viewModel: TopicListViewModel by viewModels()
    private val topicviewmodel: TopicViewModel by viewModels({requireParentFragment()})

    private val adapter: TopicListAdapter by lazy {
        TopicListAdapter(requireContext(), postListener, viewModel.viewModelScope)
    }

    override fun getLayoutId() = R.layout.fragment_club_item

    companion object {
        const val KEY_DATA = "data"
        fun createBundle(item: MemberPostItem): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.i("TopicListFragment topicTag=$topicTag")
        viewModel.adWidth = GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second

        viewModel.showProgress.observe(this) {
            layout_refresh.isRefreshing = it
        }

        viewModel.postCount.observe(this) {

            if (it == 0) {
                id_empty_group.visibility = View.VISIBLE
                text_page_empty.text = getText(R.string.empty_post)
//                list_short.visibility = View.INVISIBLE
            } else {
                id_empty_group.visibility = View.GONE
                list_short.visibility = View.VISIBLE
            }
            layout_refresh.isRefreshing = false
        }

        viewModel.followResult.observe(this, Observer {
            when (it) {
                is ApiResult.Empty -> {
                    adapter?.notifyItemRangeChanged(
                            0,
                            adapter.snapshot().items.size,
                            ClubItemAdapter.PAYLOAD_UPDATE_FOLLOW
                    )
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.likePostResult.observe(this, Observer {
            when (it) {
                is ApiResult.Success -> {
                    adapter?.notifyItemChanged(
                            it.result,
                            ClubItemAdapter.PAYLOAD_UPDATE_LIKE
                    )
                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })

        viewModel.favoriteResult.observe(this, Observer {
            when (it) {
                is ApiResult.Success -> {
                    adapter?.notifyItemChanged(
                            it.result,
                            ClubItemAdapter.PAYLOAD_UPDATE_FAVORITE
                    )
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list_short.adapter = adapter

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.getData(adapter, topicTag, orderBy)
        }

        tv_register.setOnClickListener {
            navigateTo(
                    NavigateItem.Destination(
                            R.id.action_to_loginFragment,
                            LoginFragment.createBundle(LoginFragment.TYPE_REGISTER)
                    )
            )
        }

        tv_login.setOnClickListener {
            navigateTo(
                    NavigateItem.Destination(
                            R.id.action_to_loginFragment,
                            LoginFragment.createBundle(LoginFragment.TYPE_LOGIN)
                    )
            )
        }

        mainViewModel?.deletePostResult?.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResult.Success -> {
                    Timber.i("deletePostResult")
                    checkRemovedItems()
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

    }


    override fun initSettings() {}

    private fun loginPageToggle(isLogin: Boolean) {
        Timber.i("loginPageToggle= $isLogin")
        if (isLogin) {
            id_not_login_group.visibility = View.GONE
            layout_refresh.visibility = View.VISIBLE
        } else {
            id_not_login_group.visibility = View.VISIBLE
            layout_refresh.visibility = View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()

        if (adapter.snapshot().items.isEmpty()) {
            viewModel.getData(adapter, topicTag, orderBy)
        } else if (mainViewModel?.deletePostIdList?.value?.isNotEmpty() == true) {
            checkRemovedItems()
        }

        viewModel.getAd()
    }

    private fun checkRemovedItems(){
        val idList = adapter.snapshot().items.map { item ->
            item.id
        }
        Timber.i("idList =$idList")
        idList.forEach { id ->
            if (mainViewModel?.deletePostIdList?.value?.contains(id) == true) {
                val pos = idList.indexOf(id)
                Timber.i("id =$id pos =$pos")
                adapter.removedPosList.add(id)
                adapter.notifyItemChanged(pos)
            }
        }
    }

    private val postListener = object : MyPostListener {
        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            if (viewModel.accountManager.isLogin()) {
                viewModel.likePost(item, position, isLike)
            } else {
                item.likeCount -= 1
                item.likeType =
                        if (item.likeType == LikeType.LIKE) null else LikeType.LIKE
                navigateTo(
                        NavigateItem.Destination(
                                R.id.action_to_loginFragment,
                                LoginFragment.createBundle(LoginFragment.TYPE_LOGIN)
                        )
                )
            }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = ClubPicFragment.createBundle(item, 1)
                    navigateTo(
                            NavigateItem.Destination(
                                    R.id.action_to_clubPicFragment,
                                    bundle
                            )
                    )
                }
                AdultTabType.TEXT -> {
                    val bundle = ClubTextFragment.createBundle(item, 1)
                    navigateTo(
                            NavigateItem.Destination(
                                    R.id.action_to_clubTextFragment,
                                    bundle
                            )
                    )
                }
                AdultTabType.CLIP -> {
                    val bundle = ClipPlayerFragment.createBundle(item.id, 1)
                    navigateTo(
                            NavigateItem.Destination(
                                    R.id.action_to_clipPlayerFragment,
                                    bundle
                            )
                    )
                }
            }
        }

        override fun onFavoriteClick(
                item: MemberPostItem,
                position: Int,
                isFavorite: Boolean,
                type: AttachmentType
        ) {
            if (viewModel.accountManager.isLogin()) {
                viewModel.favoritePost(item, position, isFavorite)
            } else {
                item.favoriteCount -= 1
                item.isFavorite = !item.isFavorite
                navigateTo(
                        NavigateItem.Destination(
                                R.id.action_to_loginFragment,
                                LoginFragment.createBundle(LoginFragment.TYPE_LOGIN)
                        )
                )
            }
        }

        override fun onFollowClick(items: List<MemberPostItem>, position: Int, isFollow: Boolean) {

        }

        override fun onAvatarClick(userId: Long, name: String) {
            val bundle = MyPostFragment.createBundle(
                    userId, name,
                    isAdult = true,
                    isAdultTheme = true
            )
            navigateTo(
                    NavigateItem.Destination(
                            R.id.action_to_myPostFragment,
                            bundle
                    )
            )
        }

        override fun onMoreClick(item: MemberPostItem, position: Int) {
            Timber.i("TopicListFragment onMoreClick")
            onMoreClick(item, position) {

                val searchPostItem = arguments?.getSerializable(KEY_DATA)

                val bundle = Bundle()
                bundle.putBoolean(MyPostFragment.EDIT, true)
                bundle.putString(BasePostFragment.PAGE, BasePostFragment.CLUB)
                bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
                bundle.putSerializable(SearchPostFragment.KEY_DATA, searchPostItem)

                it as MemberPostItem
                when (item.type) {
                    PostType.TEXT -> {
                        findNavController().navigate(
                                R.id.action_topicDetailFragment_to_postArticleFragment,
                                bundle
                        )
                    }
                    PostType.IMAGE -> {
                        findNavController().navigate(
                                R.id.action_topicDetailFragment_to_postPicFragment,
                                bundle
                        )
                    }
                    PostType.VIDEO -> {
                        findNavController().navigate(
                                R.id.action_topicDetailFragment_to_postVideoFragment,
                                bundle
                        )
                    }
                    else -> {
                    }
                }
            }
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            Timber.d("onItemClick =${adultTabType}, ${item.likeType}")
            when (adultTabType) {
                AdultTabType.TEXT -> {
                    val bundle = ClubTextFragment.createBundle(item)
                    navigateTo(
                            NavigateItem.Destination(
                                    R.id.action_to_clubTextFragment,
                                    bundle
                            )
                    )
                }
                AdultTabType.PICTURE -> {
                    val bundle = ClubPicFragment.createBundle(item)
                    navigateTo(
                            NavigateItem.Destination(
                                    R.id.action_to_clubPicFragment,
                                    bundle
                            )
                    )
                }
                AdultTabType.CLIP -> {
                    val bundle = ClipPlayerFragment.createBundle(item.id)
                    navigateTo(
                            NavigateItem.Destination(
                                    R.id.action_to_clipPlayerFragment,
                                    bundle
                            )
                    )
                }
                else -> {
                }
            }
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {
            Timber.d("onClipItemClick")
        }

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {}

        override fun onChipClick(postType: PostType, tag: String) {
            Timber.d("onChipClick")
            val item = SearchPostItem(type = PostType.TEXT_IMAGE_VIDEO, tag = tag)
            val bundle = SearchPostFragment.createBundle(item)
            navigateTo(
                    NavigateItem.Destination(
                            R.id.action_to_searchPostFragment,
                            bundle
                    )
            )
        }
    }
}
