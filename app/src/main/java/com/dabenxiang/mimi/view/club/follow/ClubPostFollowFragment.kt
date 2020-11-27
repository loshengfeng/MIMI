package com.dabenxiang.mimi.view.club.follow

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
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
import kotlinx.android.synthetic.main.fragment_club_follow.*
import kotlinx.android.synthetic.main.item_ad.view.*
import kotlinx.android.synthetic.main.item_club_is_not_login.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class ClubPostFollowFragment : BaseFragment() {

    private val viewModel: ClubFollowViewModel by viewModels()
    private val accountManager: AccountManager by inject()

    private val adapter: ClubPostFollowAdapter by lazy {
        ClubPostFollowAdapter(requireActivity(), postListener,  memberPostFuncItem, attachmentListener)
    }

    override fun getLayoutId() = R.layout.fragment_club_follow

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.i("ClubPostFollowFragment onAttach")
        viewModel.showProgress.observe(this, {
            layout_refresh.isRefreshing = it
        })

        viewModel.postCount.observe(this, {
            if (it == 0) {
                id_empty_group.visibility = View.VISIBLE
                recycler_view.visibility = View.INVISIBLE
            } else {
                id_empty_group.visibility = View.GONE
                recycler_view.visibility = View.VISIBLE
            }
            layout_refresh.isRefreshing = false
        })

        viewModel.adResult.observe(this, {
            when (it) {
                is ApiResult.Success -> {
                    it.result?.let { item ->
                        Glide.with(context).load(item.href).into(layout_ad.iv_ad)
                        layout_ad.iv_ad.setOnClickListener {
                            GeneralUtils.openWebView(context, item.target ?: "")
                        }
                    }
                }
                is ApiResult.Error -> {
                    layout_ad.visibility = View.GONE
                    onApiError(it.throwable)
                }

                else -> {
                    layout_ad.visibility = View.GONE
                    onApiError(Exception("Unknown Error!"))
                }
            }
        })

        viewModel.likePostResult.observe(this, {
            when (it) {
                is ApiResult.Success -> {
                    it.result?.let { position ->
                        adapter.notifyItemChanged(position)
                    }
                }

                else -> {
                    onApiError(Exception("Unknown Error!"))
                }
            }
        })

        viewModel.favoriteResult.observe(this, {
            when (it) {
                is ApiResult.Success -> {
                    it.result?.let { position ->
                        adapter.notifyItemChanged(position)
                    }
                }
                else -> {
                    onApiError(Exception("Unknown Error!"))
                }
            }
        })

        mainViewModel?.deletePostResult?.observe(this,  {
            when (it) {
                is ApiResult.Success -> {
                    adapter.removedPosList.add(it.result)
                    adapter.notifyItemChanged(it.result)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })


        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler_view.adapter = adapter

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.getData(adapter)
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

    override fun onResume() {
        super.onResume()
        Timber.i("onResume isLogin:${accountManager.isLogin()}")
        loginPageToggle(accountManager.isLogin())
        if (accountManager.isLogin() && viewModel.postCount.value ?: -1 <= 0) {
            viewModel.getData(adapter)
        }
        viewModel.getAd()
    }

    private val memberPostFuncItem by lazy {
        MemberPostFuncItem(
                {},
                { id, view, type -> },
                { item, items, isFollow, func -> },
                { item, isLike, func -> },
                { item, isFavorite, func -> }
        )
    }

    private val postListener = object : MyPostListener {

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            checkStatus { viewModel.likePost(item, position, isLike) }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            checkStatus {
                when (adultTabType) {
                    AdultTabType.PICTURE -> {
                        val bundle = PictureDetailFragment.createBundle(item, 1)
                        navigateTo(
                                NavigateItem.Destination(
                                        R.id.action_clubTabFragment_to_clubPicFragment,
                                        bundle
                                )
                        )
                    }
                    AdultTabType.TEXT -> {
                        val bundle = TextDetailFragment.createBundle(item, 1)
                       navigateTo(
                            NavigateItem.Destination(
                                    R.id.action_clubTabFragment_to_clubTextFragment,
                                    bundle
                            )
                    )
                    }
                }
            }
        }

        override fun onFavoriteClick(item: MemberPostItem, position: Int, isFavorite: Boolean, type: AttachmentType) {
            checkStatus {
                viewModel.favoritePost(item, position, isFavorite)
            }
        }

        override fun onFollowClick(items: List<MemberPostItem>, position: Int, isFollow: Boolean) {

        }

        override fun onMoreClick(item: MemberPostItem, position: Int) {
            onMoreClick(item, position) {
                it as MemberPostItem
            }
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            if (!accountManager.isLogin()) {
                loginPageToggle(false)
                return
            }

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
                    val bundle = ClipFragment.createBundle(arrayListOf(item), 0)
//                    navigateTo(
//                            NavigateItem.Destination(
//                                    R.id.action_myPostFragment_to_clipFragment,
//                                    bundle
//                            )
//                    )
                }
                else -> {
                }
            }
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {}

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {}

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
    }

    private val attachmentListener = object : AttachmentListener {
        override fun onGetAttachment(id: Long?, view: ImageView, type: LoadImageType) {
            viewModel.loadImage(id, view, type)
        }

        override fun onGetAttachment(id: String, parentPosition: Int, position: Int) {
        }
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
