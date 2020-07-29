package com.dabenxiang.mimi.view.mypost

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.serializable.SearchPostItem
import com.dabenxiang.mimi.model.vo.PostAttachmentItem
import com.dabenxiang.mimi.view.adapter.MyPostPagedAdapter
import com.dabenxiang.mimi.view.adapter.viewHolder.PicturePostHolder
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.dialog.comment.MyPostMoreDialogFragment
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.post.pic.PostPicFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_my_post.*
import timber.log.Timber


class MyPostFragment : BaseFragment() {

    private lateinit var adapter: MyPostPagedAdapter

    private var moreDialog: MyPostMoreDialogFragment? = null

    private val viewModel: MyPostViewModel by viewModels()

    private val picParameterList = arrayListOf<PicParameter>()
    private val uploadPicList = arrayListOf<PicParameter>()
    private var deletePicList = arrayListOf<String>()

    private var postMemberRequest = PostMemberRequest()

    private var uploadCurrentPicPosition = 0
    private var deleteCurrentPicPosition = 0

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    companion object {
        const val EDIT = "edit"
        const val MEMBER_DATA = "member_data"
        const val TYPE_PIC = "type_pic"
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

        val isNeedPicUpload =
            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
                PostPicFragment.UPLOAD_PIC
            )
        if (isNeedPicUpload?.value != null) {
            deletePicList = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<ArrayList<String>>(PostPicFragment.DELETE_ATTACHMENT)?.value!!
            val memberRequest =
                findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<PostMemberRequest>(
                    PostPicFragment.MEMBER_REQUEST
                )
            val picList = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<ArrayList<PostAttachmentItem>>(
                PostPicFragment.PIC_URI
            )

            postMemberRequest = memberRequest!!.value!!

            for (pic in picList?.value!!) {
                if (pic.attachmentId.isBlank()) {
                    uploadPicList.add(PicParameter(url = pic.uri))
                } else {
                    val picParameter = PicParameter(id = pic.attachmentId, ext = pic.ext)
                    picParameterList.add(picParameter)
                }
            }

            if (uploadPicList.isNotEmpty()) {
                val pic = uploadPicList[uploadCurrentPicPosition]
                viewModel.postAttachment(pic.url, requireContext(), TYPE_PIC)
            } else {
                val mediaItem = MediaItem()

                for (pic in picList.value!!) {
                    mediaItem.picParameter.add(
                        PicParameter(
                            id = pic.attachmentId,
                            ext = pic.ext
                        )
                    )
                }

                mediaItem.textContent = postMemberRequest.content

                val content = Gson().toJson(mediaItem)
                Timber.d("Post pic content item : $content")

                val postId =
                    findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Long>(
                        PostPicFragment.POST_ID
                    )
                viewModel.postPic(postId?.value!!, postMemberRequest, content)
            }
        }
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

        viewModel.uploadPicItem.observe(viewLifecycleOwner, Observer {
            uploadPicList[uploadCurrentPicPosition] = it
        })

        viewModel.postPicResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Success -> {
                    uploadPicList[uploadCurrentPicPosition].id = it.result.toString()
                    uploadCurrentPicPosition += 1

                    if (uploadCurrentPicPosition > uploadPicList.size - 1) {
                        val mediaItem = MediaItem()
                        mediaItem.textContent = postMemberRequest.content
                        mediaItem.picParameter.addAll(picParameterList)
                        mediaItem.picParameter.addAll(uploadPicList)

                        val content = Gson().toJson(mediaItem)
                        Timber.d("Post pic content item : $content")

                        val postId =
                            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Long>(
                                PostPicFragment.POST_ID
                            )
                        viewModel.postPic(postId?.value!!, postMemberRequest, content)
                    } else {
                        val pic = uploadPicList[uploadCurrentPicPosition]
                        viewModel.postAttachment(pic.url, requireContext(), TYPE_PIC)
                    }
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.postVideoMemberResult.observe(viewLifecycleOwner, Observer {
            if (deletePicList.isNotEmpty()) {
                viewModel.deleteAttachment(deletePicList[deleteCurrentPicPosition])
            } else {
                //TODO UI
            }
        })

        viewModel.postDeleteAttachment.observe(viewLifecycleOwner, Observer {
            deleteCurrentPicPosition += 1
            if (deleteCurrentPicPosition > deletePicList.size - 1) {
                //TODO finish
            } else {
                viewModel.deleteAttachment(deletePicList[deleteCurrentPicPosition])
            }
        })
    }

    override fun setupListeners() {
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
            } else if (item.type == PostType.IMAGE) {
                var bundle = Bundle()
                bundle.putBoolean(EDIT, true)
                bundle.putSerializable(MEMBER_DATA, item)
                findNavController().navigate(R.id.action_myPostFragment_to_postPicFragment, bundle)
            }

            moreDialog?.dismiss()
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