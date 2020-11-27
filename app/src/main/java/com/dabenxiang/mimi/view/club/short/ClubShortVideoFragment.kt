package com.dabenxiang.mimi.view.club.post

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.adapter.MemberPostPagedAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.club.recommend.ClubRecommendAdapter
import com.dabenxiang.mimi.view.club.recommend.ClubRecommendViewModel
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.flurry.sdk.it
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_club_latest.*
import kotlinx.android.synthetic.main.fragment_club_latest.id_empty_group
import kotlinx.android.synthetic.main.fragment_club_latest.layout_ad
import kotlinx.android.synthetic.main.fragment_club_latest.layout_refresh
import kotlinx.android.synthetic.main.fragment_club_latest.recycler_view
import kotlinx.android.synthetic.main.fragment_club_post_text.*
import kotlinx.android.synthetic.main.fragment_club_short.*
import kotlinx.android.synthetic.main.fragment_club_text.*
import kotlinx.android.synthetic.main.fragment_order.*
import kotlinx.android.synthetic.main.fragment_order.viewPager
import kotlinx.android.synthetic.main.item_ad.view.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import timber.log.Timber

class ClubShortVideoFragment : BaseFragment() {
    private val viewModel: ClubShortVideoViewModel by viewModels()
    private var adapter: ClubShortVideoAdapter? = null
    override fun getLayoutId() = R.layout.fragment_club_short
    override val bottomNavigationVisibility: Int
        get() = View.GONE

    private var memberPostItem: MemberPostItem? = null

    companion object {
        const val KEY_DATA = "data"
        fun createBundle(item: MemberPostItem): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
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
//            onMoreClick(item, ArrayList(adapter?.currentList as List<MemberPostItem>), onEdit = {
//                it as MemberPostItem
//            })
        }

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
//            checkStatus { viewModel.likePost(item, position, isLike) }
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

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = PictureDetailFragment.createBundle(item, 0)
//                    navigateTo(
//                        NavigateItem.Destination(
//                            R.id.action_clubTabFragment_to_clubPicDetailFragment,
//                            bundle
//                        )
//                    )
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 0)
//                    navigateTo(
//                        NavigateItem.Destination(
//                            R.id.action_clubTabFragment_to_clubTextDetailFragment,
//                            bundle
//                        )
//                    )
                }
                AdultTabType.CLIP -> {
                    //todo 跳轉到短視頻內頁
                }
            }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
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

    override fun setupListeners() {
//        tv_back.setOnClickListener {
//            navigateTo(NavigateItem.Up)
//        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
//        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initSettings() {
        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            getData()
        }
        adapter = ClubShortVideoAdapter(requireContext(),
            false,
            postListener,
            attachmentListener,
            memberPostFuncItem)
        list_short.adapter = adapter
    }

    private fun getData(){
        viewModel.getAd()
        viewModel.getPostItemList()
    }

    private fun followMember(
        memberPostItem: MemberPostItem,
        items: List<MemberPostItem>,
        isFollow: Boolean,
        update: (Boolean) -> Unit
    ) {
        checkStatus {
            viewModel.followMember(memberPostItem, ArrayList(items), isFollow, update)
        }
    }
}