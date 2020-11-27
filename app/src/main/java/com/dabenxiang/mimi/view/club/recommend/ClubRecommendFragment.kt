package com.dabenxiang.mimi.view.club.recommend

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_club_latest.*
import kotlinx.android.synthetic.main.fragment_club_recommend.id_empty_group
import kotlinx.android.synthetic.main.fragment_club_recommend.layout_ad
import kotlinx.android.synthetic.main.fragment_club_recommend.layout_refresh
import kotlinx.android.synthetic.main.fragment_club_recommend.recycler_view
import kotlinx.android.synthetic.main.item_ad.view.*
import kotlinx.android.synthetic.main.item_club_is_not_login.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class ClubRecommendFragment : BaseFragment() {

    private val viewModel: ClubRecommendViewModel by viewModels()
    private val accountManager: AccountManager by inject()
    private var adapter: ClubRecommendAdapter? = null


    override fun getLayoutId() = R.layout.fragment_club_recommend

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        Timber.i("onResume isLogin:${accountManager.isLogin()}")
        loginPageToggle(accountManager.isLogin())
        if (accountManager.isLogin() && viewModel.clubCount.value ?: -1 <= 0) {
            getData()
        }
        viewModel.getAd()
    }

    private val memberPostFuncItem by lazy {
        MemberPostFuncItem(
                {},
                { id, view, type -> },
                { item, items, isFollow, func -> followMember(item, items, isFollow, func) },
                { item, isLike, func -> },
                { item, isFavorite, func -> }
        )
    }

    private val attachmentListener = object : AttachmentListener {
        override fun onGetAttachment(id: Long?, view: ImageView, type: LoadImageType) {
            viewModel.loadImage(id, view, type)
        }

        override fun onGetAttachment(id: String, parentPosition: Int, position: Int) {
        }
    }

    private val postListener = object : MyPostListener {
        override fun onMoreClick(item: MemberPostItem, position: Int) {
            onMoreClick(item, position) { it as MemberPostItem }
        }

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            checkStatus { viewModel.likePost(item, position, isLike) }
        }

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {
            checkStatus {
                val bundle = ClipFragment.createBundle(ArrayList(mutableListOf(item[position])), 0)
//                navigationToVideo(bundle)
            }
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {
            val bundle = ClipFragment.createBundle(ArrayList(mutableListOf(item[position])), 0)
//            navigationToVideo(bundle)
        }

        override fun onChipClick(type: PostType, tag: String) {
            val item = SearchPostItem(type, tag)
            val bundle = SearchPostFragment.createBundle(item)
            navigateTo(
                    NavigateItem.Destination(
                            R.id.action_clubTabFragment_to_searchPostFragment,
                            bundle
                    )
            )
        }

        override fun onAvatarClick(userId: Long, name: String) {
            val bundle = MyPostFragment.createBundle(
                    userId, name,
                    isAdult = true,
                    isAdultTheme = true
            )
            navigateTo(
                    NavigateItem.Destination(
                            R.id.action_clubTabFragment_to_myPostFragment,
                            bundle
                    )
            )
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = PictureDetailFragment.createBundle(item, 0)
                    navigateTo(
                            NavigateItem.Destination(
                                    R.id.action_clubTabFragment_to_clubPicFragment,
                                    bundle
                            )
                    )
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 0)
                    navigateTo(
                            NavigateItem.Destination(
                                    R.id.action_clubTabFragment_to_clubTextFragment,
                                    bundle
                            )
                    )
                }
                AdultTabType.CLIP -> {
                    //todo 跳轉到短視頻內頁
                }
            }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            // todo Go to comment in detail
            checkStatus {
                when (adultTabType) {
                    AdultTabType.PICTURE -> {
                        val bundle = PictureDetailFragment.createBundle(item, 1)
//                        navigationToPicture(bundle)
                    }
                    AdultTabType.TEXT -> {
                        val bundle = TextDetailFragment.createBundle(item, 1)
//                        navigationToText(bundle)
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
            checkStatus {
                viewModel.favoritePost(item, position, isFavorite)
            }
        }

        override fun onFollowClick(
                items: List<MemberPostItem>,
                position: Int,
                isFollow: Boolean
        ) {
            checkStatus { viewModel.followPost(ArrayList(items), position, isFollow) }
        }
    }

    override fun initSettings() {
        tv_login.setOnClickListener {
            navigateTo(
                    NavigateItem.Destination(
                            R.id.action_to_loginFragment,
                            LoginFragment.createBundle(LoginFragment.TYPE_LOGIN)
                    )
            )
        }

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            getData()
        }

        adapter = ClubRecommendAdapter(requireContext(),
                false,
                postListener,
                attachmentListener,
                memberPostFuncItem)
        recycler_view.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recycler_view.adapter = adapter
    }

    override fun setupFirstTime() {
        initSettings()
    }

    override fun setupObservers() {
        viewModel.adResult.observe(this, {
            when (it) {
                is ApiResult.Success -> {
                    it.result?.let { item ->
                        Glide.with(requireContext()).load(item.href).into(layout_ad.iv_ad)
                        layout_ad.iv_ad.setOnClickListener {
                            GeneralUtils.openWebView(requireContext(), item.target ?: "")
                        }
                    }
                }
                is ApiResult.Error -> {
                    layout_ad.visibility =View.GONE
                    onApiError(it.throwable)
                }

                else -> {
                    layout_ad.visibility =View.GONE
                    onApiError(Exception("Unknown Error!"))
                }
            }


        })

        viewModel.showProgress.observe(this, {
            layout_refresh.isRefreshing = it
        })

        viewModel.clubCount.observe(this, Observer {
            if (it <= 0) {
                id_empty_group.visibility = View.VISIBLE
                recycler_view.visibility = View.INVISIBLE
            } else {
                id_empty_group.visibility = View.GONE
                recycler_view.visibility = View.VISIBLE
            }
            layout_refresh.isRefreshing = false
        })

        viewModel.postItemListResult.observe(viewLifecycleOwner, Observer {
            adapter?.submitList(it)
        })

        viewModel.followResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Empty -> {
                    adapter?.notifyItemRangeChanged(
                            0,
                            viewModel.totalCount,
                            ClubRecommendAdapter.PAYLOAD_UPDATE_FOLLOW
                    )
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.likePostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    adapter?.notifyItemChanged(
                            it.result,
                            ClubRecommendAdapter.PAYLOAD_UPDATE_LIKE
                    )
                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })

        viewModel.favoriteResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    adapter?.notifyItemChanged(
                            it.result,
                            ClubRecommendAdapter.PAYLOAD_UPDATE_FAVORITE
                    )
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })
    }

    private fun getData(){
//        viewModel.getAd()
        viewModel.getPostItemList()
    }

    private fun followMember(
            memberPostItem: MemberPostItem,
            items: List<MemberPostItem>,
            isFollow: Boolean,
            update: (Boolean) -> Unit
    ) {
        checkStatus { viewModel.followMember(memberPostItem, ArrayList(items), isFollow, update) }
    }

    private fun loginPageToggle(isLogin: Boolean) {
        if (isLogin) {
            id_not_login_group.visibility = View.GONE
            layout_refresh.visibility = View.VISIBLE
        } else {
            id_not_login_group.visibility = View.VISIBLE
            layout_refresh.visibility = View.INVISIBLE
        }
    }
}
