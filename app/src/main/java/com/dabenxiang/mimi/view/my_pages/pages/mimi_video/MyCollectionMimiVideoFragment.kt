package com.dabenxiang.mimi.view.my_pages.pages.mimi_video

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.MyCollectionVideoListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.MyCollectionTabItemType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clipsingle.ClipSingleFragment
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.dabenxiang.mimi.view.my_pages.base.MyPagesViewModel
import com.dabenxiang.mimi.view.player.ui.PlayerV2Fragment
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_my_collection_videos.*
import kotlinx.android.synthetic.main.item_ad.view.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class MyCollectionMimiVideoFragment(val tab:Int, val type: MyCollectionTabItemType, val isLike: Boolean = false) : BaseFragment() {
    private val viewModel: MyCollectionMimiVideoViewModel by viewModels()
    private val myPagesViewModel: MyPagesViewModel by viewModels({requireParentFragment()})
    private val accountManager: AccountManager by inject()

    private val clipFuncItem by lazy {
        CollectionFuncItem(
                { source -> viewModel.getDecryptSetting(source) },
                { videoItem, decryptSettingItem, function -> viewModel.decryptCover(videoItem, decryptSettingItem, function) }
        )
    }

    private val adapter: MyCollectionMimiVideoAdapter by lazy {
        MyCollectionMimiVideoAdapter(requireContext(), clipFuncItem, listener, type)
    }

    override fun getLayoutId() = R.layout.fragment_my_collection_videos

    companion object {
        const val KEY_DATA = "data"
        fun createBundle(item: MemberPostItem): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    private val listener = object : MyCollectionVideoListener {
        override fun onMoreClick(item: PlayItem, position: Int) {

        }

        override fun onLikeClick(item: PlayItem, position: Int, isLike: Boolean) {
            checkStatus {
                viewModel.likePost(MemberPostItem(id = item.videoId
                        ?: 0, likeType = LikeType.LIKE), position, isLike)
            }
        }

        override fun onClipCommentClick(item: List<PlayItem>, position: Int) {

        }

        override fun onChipClick(type: PostType, tag: String) {
            Timber.d("onChipClick")
            val bundle = SearchVideoFragment.createBundle(tag)
            navigateTo(
                    NavigateItem.Destination(
                            R.id.action_to_searchVideoFragment,
                            bundle
                    )
            )
        }

        override fun onItemClick(item: PlayItem, type: MyCollectionTabItemType) {
            if (this@MyCollectionMimiVideoFragment.type == MyCollectionTabItemType.MIMI_VIDEO) {
                val bundle = PlayerV2Fragment.createBundle(PlayerItem(item.videoId ?: 0))
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_to_playerV2Fragment,
                        bundle
                    ))
            } else {
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_to_clipSingleFragment,
                        ClipSingleFragment.createBundle(item)
                    ))
            }
        }

        override fun onCommentClick(item: PlayItem, type: MyCollectionTabItemType) {
            Timber.d("onCommentClick, item = ${item}")
            val bundle = PlayerV2Fragment.createBundle(PlayerItem(item.videoId ?: 0), true)
            navigateTo(
                    NavigateItem.Destination(
                            R.id.action_to_playerV2Fragment,
                            bundle
                    ))
        }

        override fun onFavoriteClick(item: PlayItem, position: Int, isFavorite: Boolean, type: MyCollectionTabItemType) {
            val dialog = CleanDialogFragment.newInstance(object : OnCleanDialogListener {
                override fun onClean() {
                    checkStatus { viewModel.deleteMIMIVideoFavorite(item.videoId.toString()) }
                }
            })

            dialog.setMsg(getString(
                when(isLike) {
                    true -> R.string.like_delete_favorite_message
                    false -> R.string.follow_delete_favorite_message
            }))
            dialog.show(
                        requireActivity().supportFragmentManager,
                        CleanDialogFragment::class.java.simpleName
                )

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.adWidth = GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second

        viewModel.deleteFavoriteResult.observe(this) {
            viewModel.getData(adapter, type, isLike)
        }

        viewModel.showProgress.observe(this) {
            layout_refresh.isRefreshing = it
        }

        viewModel.postCount.observe(this) {
            Timber.i("postCount= $it")
            if (it == 0) {
                text_page_empty.text = if (isLike) getString(R.string.like_empty_msg) else getString(R.string.follow_empty_msg)
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
                    adapter.notifyItemChanged(
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
                    adapter.notifyItemChanged(
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
                    viewModel.getData(adapter, type, isLike)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        myPagesViewModel.deleteAll.observe(this,  {
            if(tab == it){
                if(isLike) viewModel.deleteAllLike(adapter.snapshot().items)
                else viewModel.deleteVideos(adapter.snapshot().items)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list_short.adapter = adapter

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.getData(adapter, type, isLike)
        }

        img_page_empty.setImageDrawable(ContextCompat.getDrawable(requireContext(),
            when(isLike) {
                false -> R.drawable.img_history_empty_2
                true -> R.drawable.img_love_empty
            }
        ))
    }

    override fun initSettings() {

    }

    override fun onResume() {
        super.onResume()

        if (viewModel.postCount.value ?: -1 <= 0) {
            viewModel.getData(adapter, type, isLike)
        }
    }


    private val attachmentListener = object : AttachmentListener {
        override fun onGetAttachment(id: Long?, view: ImageView, type: LoadImageType) {
            viewModel.loadImage(id, view, type)
        }

        override fun onGetAttachment(id: String, parentPosition: Int, position: Int) {
        }
    }
}