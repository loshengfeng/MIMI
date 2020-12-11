package com.dabenxiang.mimi.view.my_pages.pages.like

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.MyCollectionTabItemType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.pic.ClubPicFragment
import com.dabenxiang.mimi.view.club.text.ClubTextFragment
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.dabenxiang.mimi.view.my_pages.base.MyPagesViewModel
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.player.ui.ClipPlayerFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_my_collection_favorites.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class LikePostFragment(val tab: Int, val type: MyCollectionTabItemType) : BaseFragment() {

    private val viewModel: LikePostViewModel by viewModels()
    private val myPagesViewModel: MyPagesViewModel by viewModels({ requireParentFragment() })
    private val accountManager: AccountManager by inject()

    private val adapter: LikePostAdapter by lazy {
        LikePostAdapter(requireActivity(), postListener, viewModel.viewModelScope)
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId() = R.layout.fragment_my_collection_favorites

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel.postChangedResult.observe(this, {
            when (it) {
                is ApiResult.Success -> {
                    val changeItem = mainViewModel?.postItemChangedList?.value?.get(it.result.id)
                    if(changeItem != null) {
                        if (it.result.isFavorite) changeItem.favoriteCount++
                        else changeItem.favoriteCount--
                    }
                    mainViewModel?.postItemChangedList?.value?.set(it.result.id, it.result)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

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
            myPagesViewModel.changeDataCount(tab, it)
        })

        viewModel.likePostResult.observe(this, {
            when (it) {
                is ApiResult.Success -> it.result.let { position ->
                    adapter.notifyItemChanged(position)
                }
                else -> {
                    onApiError(Exception("Unknown Error!"))
                }
            }
        })

        viewModel.favoriteResult.observe(this, {
            when (it) {
                is ApiResult.Success -> {
                    it.result.let { position ->
                        adapter.notifyItemChanged(position, LikePostAdapter.PAYLOAD_UPDATE_FAVORITE)
                    }
                }
                else -> {
                    onApiError(Exception("Unknown Error!"))
                }
            }
        })

        viewModel.cleanResult.observe(this, {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Loaded -> progressHUD?.dismiss()
                is ApiResult.Empty -> viewModel.getData(adapter)
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.deletePostResult?.observe(this, {
            when (it) {
                is ApiResult.Success -> {
                    adapter.removedPosList.add(it.result)
                    adapter.notifyItemChanged(it.result)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.adWidth = GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler_view.adapter = adapter

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.getData(adapter)
        }

        myPagesViewModel.deleteAll.observe(viewLifecycleOwner, {
            if (it == tab) viewModel.deleteAllLike(adapter.snapshot().items)
        })

        text_page_empty.text = getString(R.string.like_empty_msg)
        img_page_empty.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.img_love_empty
            )
        )
    }

    override fun onResume() {
        super.onResume()
        Timber.i("onResume isLogin:${accountManager.isLogin()}")
        if (accountManager.isLogin() && viewModel.postCount.value ?: -1 <= 0) {
            viewModel.getData(adapter)
        }  else if (mainViewModel?.postItemChangedList?.value?.isNotEmpty() == true) {
            adapter.changedPosList = mainViewModel?.postItemChangedList?.value ?: HashMap()
            adapter.notifyDataSetChanged()
        }
    }

    private val postListener = object : MyPostListener {

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            val dialog = CleanDialogFragment.newInstance(object : OnCleanDialogListener {
                override fun onClean() {
                    viewModel.likePost(item, position, isLike)
                }
            })

            dialog.setMsg(getString(R.string.like_delete_favorite_message))

            dialog.show(
                requireActivity().supportFragmentManager,
                CleanDialogFragment::class.java.simpleName
            )
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
            viewModel.favoritePost(item, position, isFavorite)
        }

        override fun onFollowClick(items: List<MemberPostItem>, position: Int, isFollow: Boolean) {

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

            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = PictureDetailFragment.createBundle(item, 0)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_to_clubPicFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 0)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_to_clubTextFragment,
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

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {}

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {}

        override fun onChipClick(type: PostType, tag: String) {
            val item = SearchPostItem(type = PostType.FOLLOWED, tag = tag)
            val bundle = SearchPostFragment.createBundle(item)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_searchPostFragment,
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
                    R.id.action_to_myPostFragment,
                    bundle
                )
            )
        }
    }
}
