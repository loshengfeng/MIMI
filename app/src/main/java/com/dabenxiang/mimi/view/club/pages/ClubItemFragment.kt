package com.dabenxiang.mimi.view.club.pages

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.*
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.ClubTabViewModel
import com.dabenxiang.mimi.view.club.pic.ClubPicFragment
import com.dabenxiang.mimi.view.club.text.ClubTextFragment
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.player.ui.ClipPlayerFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_club_item.*
import kotlinx.android.synthetic.main.fragment_club_item.id_empty_group
import kotlinx.android.synthetic.main.fragment_club_item.layout_refresh
import kotlinx.android.synthetic.main.fragment_club_item.list_short
import kotlinx.android.synthetic.main.fragment_club_item.text_page_empty
import kotlinx.android.synthetic.main.item_club_is_not_login.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import org.koin.android.ext.android.inject
import timber.log.Timber

class ClubItemFragment(val type: ClubTabItemType) : BaseFragment() {

    private val viewModel: ClubItemViewModel by viewModels()
    private val clubTabViewModel: ClubTabViewModel by viewModels({ requireParentFragment() })
    private val accountManager: AccountManager by inject()

    private val adapter: ClubItemAdapter by lazy {
       ClubItemAdapter(requireContext(), postListener, viewModel.viewModelScope)
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

    private fun emptyPageToggle(isHide:Boolean){
        if (isHide) {
             id_empty_group.visibility = View.VISIBLE
                    text_page_empty.text = when (type) {
                ClubTabItemType.FOLLOW -> getText(R.string.empty_follow)
                else -> getText(R.string.empty_post)
            }
            list_short.visibility = View.INVISIBLE
        } else {
            id_empty_group.visibility = View.GONE
            list_short.visibility = View.VISIBLE

        }
        layout_refresh.isRefreshing = false

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.adWidth = GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second

        viewModel.showProgress.observe(this) {
            layout_refresh.isRefreshing = it
        }

        viewModel.followResult.observe(this, Observer {
            when (it) {
                is ApiResult.Empty -> {
                    adapter.notifyItemRangeChanged(
                        0,
                        viewModel.totalCount,
                        ClubItemAdapter.PAYLOAD_UPDATE_FOLLOW
                    )
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

//        viewModel.likePostResult.observe(this, Observer {
//            when (it) {
//                is ApiResult.Success -> {
//                    Timber.i("likePostResult = $it")
//                    adapter.notifyDataSetChanged()
//                }
//                is ApiResult.Error -> Timber.e(it.throwable)
//            }
//        })
//
//        viewModel.favoriteResult.observe(this, Observer {
//            when (it) {
//                is ApiResult.Success -> {
//                    adapter?.notifyItemChanged(
//                        it.result,
//                        ClubItemAdapter.PAYLOAD_UPDATE_FAVORITE
//                    )
//                }
//                is ApiResult.Error -> onApiError(it.throwable)
//            }
//        })

        viewModel.postCount.observe(this) {
            Timber.i("type=$type postCount= $it")
            emptyPageToggle(it <=0)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list_short.adapter = adapter

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            clubTabViewModel.doTask(ClubTabViewModel.REFRESH_TASK)
//            viewModel.getData(adapter, type)
            adapter.refresh()
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
        loginPageToggle(
            if (type == ClubTabItemType.FOLLOW) accountManager.isLogin()
            else true
        )

        if (adapter.snapshot().items.isEmpty()) {
//            layout_refresh.isRefreshing = true
//            adapter.refresh()
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        viewModel.viewModelScope.launch {
            viewModel.posts(type).collectLatest {
                adapter.submitData(it)
            }
        }

        viewModel.getAd()
    }

    private fun checkRemovedItems(){
//        val idList = adapter.snapshot().items.map { item ->
//            item.id
//        }
//        Timber.i("idList =$idList")
//        idList.forEach { id ->
//            if (mainViewModel?.deletePostIdList?.value?.contains(id) == true) {
//                val pos = idList.indexOf(id)
//                Timber.i("id =$id pos =$pos")
//                adapter.notifyItemChanged(pos)
//            }
//        }
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
            onMoreClick(item, position) {
                it as MemberPostItem


                val bundle = Bundle()
                item.id
                bundle.putBoolean(MyPostFragment.EDIT, true)
                bundle.putString(BasePostFragment.PAGE, BasePostFragment.TAB)
                bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)

                when (it.type) {
                    PostType.TEXT -> {
                        findNavController().navigate(
                            R.id.action_to_postArticleFragment,
                            bundle
                        )
                    }
                    PostType.IMAGE -> {
                        findNavController().navigate(
                            R.id.action_to_postPicFragment,
                            bundle
                        )
                    }
                    PostType.VIDEO -> {
                        findNavController().navigate(
                            R.id.action_to_postVideoFragment,
                            bundle
                        )
                    }
                }
            }
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            Timber.d("onItemClick =${adultTabType}, ${item.likeType}")
            when (adultTabType) {
                AdultTabType.TEXT -> {
                    val bundle = ClubTextFragment.createBundle(item, isNeedRefresh = true)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_to_clubTextFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.PICTURE -> {
                    val bundle = ClubPicFragment.createBundle(item, isNeedRefresh = true)
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
            val item = SearchPostItem(
                if (type == ClubTabItemType.HOTTEST || type == ClubTabItemType.LATEST) PostType.TEXT_IMAGE_VIDEO
                else if (type == ClubTabItemType.FOLLOW) PostType.FOLLOWED
                else postType, tag = tag
            )
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