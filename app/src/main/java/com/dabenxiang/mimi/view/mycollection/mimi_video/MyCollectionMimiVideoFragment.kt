package com.dabenxiang.mimi.view.mycollection.mimi_video

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.MyFollowVideoListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.MyFollowTabItemType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.dabenxiang.mimi.view.mycollection.MyCollectionViewModel
import com.dabenxiang.mimi.view.player.ui.PlayerV2Fragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_club_short.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class MyCollectionMimiVideoFragment(val type: MyFollowTabItemType) : BaseFragment() {
    private val viewModel: MyCollectionMimiVideoViewModel by viewModels()
    private val collectionViewModel: MyCollectionViewModel by viewModels({requireParentFragment()})
    private val accountManager: AccountManager by inject()

    private val adapter: MyCollectionMimiVideoAdapter by lazy {
        MyCollectionMimiVideoAdapter(requireContext(), listener)
    }

    override fun getLayoutId() = R.layout.fragment_my_follow_tab

    companion object {
        const val KEY_DATA = "data"
        fun createBundle(item: MemberPostItem): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    private val listener = object : MyFollowVideoListener {
        override fun onMoreClick(item: PlayItem, position: Int) {

        }

        override fun onLikeClick(item: PlayItem, position: Int, isLike: Boolean) {
            checkStatus { viewModel.likePost(MemberPostItem(id = item.videoId!!, likeType = LikeType.LIKE), position, isLike) }
        }

        override fun onClipCommentClick(item: List<PlayItem>, position: Int) {

        }

        override fun onChipClick(type: PostType, tag: String) {
            Timber.d("onChipClick")
            val item = SearchPostItem(type, tag = tag)
            val bundle = SearchPostFragment.createBundle(item)
            navigateTo(
                    NavigateItem.Destination(
                            R.id.action_to_searchPostFragment,
                            bundle
                    )
            )
        }

        override fun onItemClick(item: PlayItem, type: MyFollowTabItemType) {
            if (this@MyCollectionMimiVideoFragment.type == MyFollowTabItemType.MIMI_VIDEO) {
                val bundle = PlayerV2Fragment.createBundle(PlayerItem(item.videoId ?: 0))
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_to_playerV2Fragment,
                        bundle
                    ))
            } else {
                //TODO Sion ~~~~~~~~
            }
        }

        override fun onCommentClick(item: PlayItem, type: MyFollowTabItemType) {
            Timber.d("onCommentClick, item = ${item}")
            val bundle = PlayerV2Fragment.createBundle(PlayerItem(item.videoId ?: 0), true)
            navigateTo(
                    NavigateItem.Destination(
                            R.id.action_to_playerV2Fragment,
                            bundle
                    ))
        }

        override fun onFavoriteClick(item: PlayItem, position: Int, isFavorite: Boolean, type: MyFollowTabItemType) {
            val dialog = CleanDialogFragment.newInstance(object : OnCleanDialogListener {
                override fun onClean() {
                    checkStatus { viewModel.deleteMIMIVideoFavorite(item.videoId.toString()) }
                }
            })

            dialog.setMsg(getString(R.string.follow_delete_favorite_message,item.title))
            dialog.show(
                        requireActivity().supportFragmentManager,
                        CleanDialogFragment::class.java.simpleName
                )

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()

        viewModel.deleteFavoriteResult.observe(this) {
            viewModel.getData(adapter, type)
        }

        viewModel.showProgress.observe(this) {
            layout_refresh.isRefreshing = it
        }

        viewModel.postCount.observe(this) {
            Timber.i("postCount= $it")
            if (it == 0) {
                id_empty_group.visibility = View.VISIBLE
                list_short.visibility = View.INVISIBLE
            } else {
                id_empty_group.visibility = View.GONE
                list_short.visibility = View.VISIBLE
            }
            layout_refresh.isRefreshing = false
        }

        viewModel.likePostResult.observe(this, Observer {
            when (it) {
                is ApiResult.Success -> {
                    adapter?.notifyItemChanged(
                            it.result,
                            MyCollectionMimiVideoAdapter.PAYLOAD_UPDATE_LIKE
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
                            MyCollectionMimiVideoAdapter.PAYLOAD_UPDATE_FAVORITE
                    )
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.cleanResult.observe(this, {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Loaded -> progressHUD?.dismiss()
                is ApiResult.Empty -> {
                    viewModel.getData(adapter, type)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        collectionViewModel.deleteMiMIs.observe(this,  {
            viewModel.deleteVideos(adapter.snapshot().items)
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list_short.adapter = adapter

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.getData(adapter, type)
        }
    }

    override fun initSettings() {

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

    override fun onResume() {
        super.onResume()
        //According to specs, this page does not need to log in currently
        loginPageToggle(true)

        if (viewModel.postCount.value ?: -1 <= 0) {
            viewModel.getData(adapter, type)
        }
//        viewModel.getAd()
    }


    private val attachmentListener = object : AttachmentListener {
        override fun onGetAttachment(id: Long?, view: ImageView, type: LoadImageType) {
            viewModel.loadImage(id, view, type)
        }

        override fun onGetAttachment(id: String, parentPosition: Int, position: Int) {
        }
    }
}