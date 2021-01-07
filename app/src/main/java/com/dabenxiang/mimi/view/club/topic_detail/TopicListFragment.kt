package com.dabenxiang.mimi.view.club.topic_detail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.*
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.base.AdAdapter
import com.dabenxiang.mimi.view.club.base.PostItemAdapter
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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber

class TopicListFragment(private val memberClubItem: MemberClubItem, private val orderBy: OrderBy, private val topicTag:String) : BaseFragment() {

    private val viewModel: TopicListViewModel by viewModels()

    private val adTop: AdAdapter by lazy {
        AdAdapter(requireContext())
    }

    private val adBottom: AdAdapter by lazy {
        AdAdapter(requireContext())
    }

    private val adapter: PostItemAdapter by lazy {
        PostItemAdapter(requireContext(), postListener, viewModel.viewModelScope, AD_GAP)
    }

    override fun getLayoutId() = R.layout.fragment_club_item

    companion object {
        const val KEY_DATA = "data"
        const val AD_CODE = "community"
        const val AD_GAP: Int = 5
        fun createBundle(item: MemberPostItem): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    val pageCode = TopicListFragment::class.simpleName + topicTag + orderBy.toString()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.i("TopicListFragment topicTag=$topicTag")
        viewModel.adWidth = GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second
        viewModel.getTopAd(AD_CODE+"_top")

        viewModel.showProgress.observe(this) {
            layout_refresh.isRefreshing = it
        }

        viewModel.postCount.observe(this) {
            emptyPageToggle(it <=0)
        }

        viewModel.topAdResult.observe(this, {
            adTop.adItem = it
            adTop.visibility = View.VISIBLE
            adTop.notifyDataSetChanged()
        })

        viewModel.bottomAdResult.observe(this) {
            adBottom.adItem = it
            adBottom.visibility = View.VISIBLE
            adBottom.notifyDataSetChanged()
        }

        viewModel.likePostResult.observe(this, {
            when (it) {
                is ApiResult.Success -> {
                    it.result.let { position ->
                        adapter.notifyItemChanged(position, PostItemAdapter.UPDATE_INTERACTIVE)
                    }
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.favoriteResult.observe(this, {
            when (it) {
                is ApiResult.Success -> {
                    it.result.let { position ->
                        adapter.notifyItemChanged(position, PostItemAdapter.UPDATE_INTERACTIVE)
                    }
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })
    }

    private fun emptyPageToggle(isHide:Boolean){
        if (isHide) {
            timeout = 0
            id_empty_group.visibility = View.VISIBLE
            text_page_empty.text = getText(R.string.empty_post)
            posts_list.visibility = View.INVISIBLE
        } else {
            id_empty_group.visibility = View.GONE
            posts_list.visibility = View.VISIBLE

        }
        layout_refresh.isRefreshing = false

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        @OptIn(ExperimentalCoroutinesApi::class)
        viewModel.viewModelScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                if(adapter.snapshot().items.isEmpty() && timeout >0){
                    layout_refresh?.isRefreshing = true
                }else{
                    layout_refresh?.isRefreshing = loadStates.refresh is LoadState.Loading
                }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        viewModel.viewModelScope.launch {
            viewModel.posts(pageCode, topicTag, orderBy).flowOn(Dispatchers.IO).collectLatest {
                adapter.submitData(it)
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        viewModel.viewModelScope.launch {
            @OptIn(FlowPreview::class)
            adapter.loadStateFlow
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .onEach { delay(1000) }
                .collect {
                    if(adapter.snapshot().items.isEmpty() && timeout >0) {
                        timeout--
                        adapter.refresh()
                    }
                }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("pageCode=$pageCode onViewCreated")
        posts_list.adapter = ConcatAdapter(adTop, adapter, adBottom)

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
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

        if(adapter.snapshot().items.isEmpty()){
            Timber.i("pageCode=$pageCode  onResume")
            viewModel.viewModelScope.launch {
                adapter.refresh()
            }

        }
    }

    private val postListener = object : MyPostListener {
        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            if (viewModel.accountManager.isLogin()) {
                viewModel.likePost(item, position, isLike)
            } else {
                navigateTo(
                        NavigateItem.Destination(
                                R.id.action_to_loginFragment,
                                LoginFragment.createBundle(LoginFragment.TYPE_LOGIN)
                        )
                )
            }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            checkStatus {
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

        override fun onLoginClick() {
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_loginFragment,
                    LoginFragment.createBundle(LoginFragment.TYPE_LOGIN)
                )
            )
        }

        override fun onRegisterClick() {
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_loginFragment,
                    LoginFragment.createBundle(LoginFragment.TYPE_REGISTER)
                )
            )
        }

        override fun onMoreClick(item: MemberPostItem, position: Int) {
            Timber.i("TopicListFragment onMoreClick")
            onMoreClick(item, position, isFromPostPage = true) {

                val bundle = Bundle()
                bundle.putBoolean(MyPostFragment.EDIT, true)
                bundle.putString(BasePostFragment.PAGE, BasePostFragment.CLUB)
                bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
                bundle.putSerializable(KEY_DATA, memberClubItem)

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
            checkStatus {
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
