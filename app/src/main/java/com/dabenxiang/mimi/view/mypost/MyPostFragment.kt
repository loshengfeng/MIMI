package com.dabenxiang.mimi.view.mypost

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.serializable.SearchPostItem
import com.dabenxiang.mimi.view.adapter.MyPostPagedAdapter
import com.dabenxiang.mimi.view.adapter.viewHolder.PicturePostHolder
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.dialog.comment.MyPostMoreDialogFragment
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.fragment_my_post.*
import timber.log.Timber


class MyPostFragment : BaseFragment() {

    private lateinit var adapter: MyPostPagedAdapter

    private var moreDialog: MyPostMoreDialogFragment? = null

    private val viewModel: MyPostViewModel by viewModels()

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    companion object {
        const val EDIT = "edit"
        const val MEMBER_DATA = "member_data"
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_my_post
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()

        adapter = MyPostPagedAdapter(requireContext(), myPostListener, attachmentListener)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter =  adapter

        viewModel.getMyPost()
    }

    override fun setupObservers() {
        viewModel.myPostItemListResult.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        viewModel.attachmentByTypeResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    val attachmentItem = it.result
                    LruCacheUtils.putLruCache(attachmentItem.id!!, attachmentItem.bitmap!!)

                    when (attachmentItem.type) {
                        AttachmentType.ADULT_TAB_CLIP,
                        AttachmentType.ADULT_TAB_PICTURE,
                        AttachmentType.ADULT_TAB_TEXT -> {
                            adapter.notifyItemChanged(attachmentItem.position!!)
                        }
                        else -> {
                        }
                    }

                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })

        viewModel.attachmentResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    val attachmentItem = it.result
                    LruCacheUtils.putLruCache(attachmentItem.id!!, attachmentItem.bitmap!!)
                    when (val holder =
                        adapter.viewHolderMap[attachmentItem.parentPosition]) {
                        is PicturePostHolder -> {
                            if (holder.pictureRecycler.tag == attachmentItem.parentPosition) {
                                adapter.updateInternalItem(holder)
                            }
                        }
                    }
                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })

        viewModel.likePostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    adapter.notifyItemChanged(
                        it.result,
                        MyPostPagedAdapter.PAYLOAD_UPDATE_LIKE_AND_FOLLOW_UI
                    )
                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })

        viewModel.favoriteResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Loaded -> progressHUD?.dismiss()
                is ApiResult.Success -> {
                    adapter.updateFavoriteItem(it.result.memberPostItem!!, it.result.position!!)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.deletePostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Loaded -> progressHUD?.dismiss()
                is ApiResult.Success -> viewModel.invalidateDataSource()
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {
    }

    private fun addTag(tag: String) {

    }

    private val adultListener = object : AdultListener {
        override fun onFollowPostClick(item: MemberPostItem, position: Int, isFollow: Boolean) {
        }

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = PictureDetailFragment.createBundle(item, 1)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_adultHomeFragment_to_pictureDetailFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 1)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_adultHomeFragment_to_textDetailFragment,
                            bundle
                        )
                    )
                }
                else -> {
                }
            }
        }

        override fun onMoreClick(item: MemberPostItem) {

        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {
        }

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {
        }

        override fun onChipClick(type: PostType, tag: String) {
        }
    }

    private val attachmentListener = object : AttachmentListener {
        override fun onGetAttachment(id: String, position: Int, type: AttachmentType) {
            viewModel.getAttachment(id, position, type)
        }

        override fun onGetAttachment(id: String, parentPosition: Int, position: Int) {
            viewModel.getAttachment(id, parentPosition, position)
        }
    }

    private val onMoreDialogListener = object : MyPostMoreDialogFragment.OnMoreDialogListener {
        override fun onCancel() {
            moreDialog?.dismiss()
        }

        override fun onDelete(item: BaseMemberPostItem) {
            viewModel.deletePost(item as MemberPostItem)
        }

        override fun onEdit(item: BaseMemberPostItem) {
            item as MemberPostItem
            if (item.type == PostType.TEXT) {
                val bundle = Bundle()
                bundle.putBoolean(EDIT, true)
                bundle.putSerializable(MEMBER_DATA, item)
                findNavController().navigate(R.id.action_myPostFragment_to_postArticleFragment, bundle)
                moreDialog?.dismiss()
            }
        }
    }

    private val myPostListener = object : MyPostListener {
        override fun onMoreClick(item: MemberPostItem) {
            moreDialog = MyPostMoreDialogFragment.newInstance(item, onMoreDialogListener).also {
                it.show(
                    requireActivity().supportFragmentManager,
                    MoreDialogFragment::class.java.simpleName
                )
            }
        }

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            viewModel.likePost(item, position, isLike)
        }

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {

            val bundle = ClipFragment.createBundle(ArrayList(item), position)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_myPostFragment_to_clipFragment,
                    bundle
                )
            )
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {
            val bundle = ClipFragment.createBundle(ArrayList(item), position)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_myPostFragment_to_searchPostFragment,
                    bundle
                )
            )
        }

        override fun onChipClick(type: PostType, tag: String) {
            val item = SearchPostItem(type, tag)
            val bundle = SearchPostFragment.createBundle(item)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_myPostFragment_to_searchPostFragment,
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
                            R.id.action_myPostFragment_to_pictureDetailFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 0)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_myPostFragment_to_textDetailFragment,
                            bundle
                        )
                    )
                }
                else -> {
                }
            }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = PictureDetailFragment.createBundle(item, 1)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_myPostFragment_to_pictureDetailFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 1)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_myPostFragment_to_textDetailFragment,
                            bundle
                        )
                    )
                }
                else -> {
                }
            }
        }

        override fun onFavoriteClick(item: MemberPostItem, position: Int, isFavorite: Boolean, type: AttachmentType) {
            viewModel.favoritePost(item, position, isFavorite, type)
        }
    }

    interface MyPostListener {
        fun onMoreClick(item: MemberPostItem)
        fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean)
        fun onClipCommentClick(item: List<MemberPostItem>, position: Int)
        fun onClipItemClick(item: List<MemberPostItem>, position: Int)
        fun onChipClick(type: PostType, tag: String)
        fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType)
        fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType)
        fun onFavoriteClick(item: MemberPostItem, position: Int, isFavorite: Boolean, type: AttachmentType)
    }
}