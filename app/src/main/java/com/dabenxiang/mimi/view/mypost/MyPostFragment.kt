package com.dabenxiang.mimi.view.mypost

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.callback.OnMeMoreDialogListener
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.adapter.MyPostPagedAdapter
import com.dabenxiang.mimi.view.adapter.viewHolder.PicturePostHolder
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.dialog.comment.MyPostMoreDialogFragment
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.mypost.MyPostViewModel.Companion.USER_ID_ME
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.fragment_my_post.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import timber.log.Timber


class MyPostFragment : BaseFragment() {

    private lateinit var adapter: MyPostPagedAdapter

    private var meMoreDialog: MyPostMoreDialogFragment? = null
    private var moreDialog: MoreDialogFragment? = null

    private val viewModel: MyPostViewModel by viewModels()

    private var userId: Long = USER_ID_ME
    private var userName: String = ""
    private var isAdult: Boolean = true
    private var isAdultTheme: Boolean = false

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    companion object {
        const val EDIT = "edit"
        const val MEMBER_DATA = "member_data"
        const val TYPE_PIC = "type_pic"

        private const val KEY_USER_ID = "KEY_USER_ID"
        private const val KEY_USER_NAME = "KEY_USER_NAME"
        private const val KEY_IS_ADULT = "KEY_IS_ADULT"
        private const val KEY_IS_ADULT_THEME = "KEY_IS_ADULT_THEME"

        fun createBundle(
            userId: Long,
            userName: String,
            isAdult: Boolean,
            isAdultTheme: Boolean
        ): Bundle {
            return Bundle().also {
                it.putLong(KEY_USER_ID, userId)
                it.putString(KEY_USER_NAME, userName)
                it.putBoolean(KEY_IS_ADULT, isAdult)
                it.putBoolean(KEY_IS_ADULT_THEME, isAdultTheme)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_my_post
    }

    override fun setupFirstTime() {
        initSettings()
        viewModel.getMyPost(userId, isAdult)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        useAdultTheme(isAdultTheme)
        requireActivity().onBackPressedDispatcher.addCallback {
            navigateTo(NavigateItem.Up)
        }
    }

    override fun initSettings() {
        arguments?.let {
            userId = it.getLong(KEY_USER_ID, USER_ID_ME)
            userName = it.getString(KEY_USER_NAME, "")
            isAdult = it.getBoolean(KEY_IS_ADULT, true)
            isAdultTheme = it.getBoolean(KEY_IS_ADULT_THEME, false)
        }

        adapter = MyPostPagedAdapter(
            requireContext(),
            isAdultTheme,
            myPostListener,
            attachmentListener,
            memberPostFuncItem
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        cl_layout_bg.isSelected = isAdultTheme
        cl_bg.isSelected = isAdultTheme
        tv_title.text = if (userId == USER_ID_ME) getString(R.string.personal_my_post) else userName
        tv_title.isSelected = isAdultTheme
        tv_back.isSelected = isAdultTheme
        if (isAdultTheme) layout_refresh.setColorSchemeColors(requireContext().getColor(R.color.color_red_1))
    }

    override fun setupObservers() {
        viewModel.showProgress.observe(this, Observer {
            layout_refresh.isRefreshing = it
        })

        viewModel.myPostItemListResult.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        viewModel.attachmentByTypeResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
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
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.attachmentResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
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
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.likePostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    adapter.notifyItemChanged(
                        it.result,
                        MyPostPagedAdapter.PAYLOAD_UPDATE_LIKE
                    )
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.favoriteResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    adapter.notifyItemChanged(
                        it.result,
                        MyPostPagedAdapter.PAYLOAD_UPDATE_FAVORITE
                    )
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.followResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Empty -> {
                    adapter.notifyItemRangeChanged(
                        0,
                        viewModel.totalCount,
                        MyPostPagedAdapter.PAYLOAD_UPDATE_FOLLOW
                    )
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.deletePostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    adapter.removedPosList.add(it.result)
                    adapter.notifyItemChanged(it.result)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.cleanRemovedPosList.observe(viewLifecycleOwner, Observer {
            adapter.removedPosList.clear()
        })
    }

    private fun navigationToText(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_myPostFragment_to_textDetailFragment,
                bundle
            )
        )
    }

    private fun navigationToPicture(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_myPostFragment_to_pictureDetailFragment,
                bundle
            )
        )
    }

    private fun navigationToVideo(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_myPostFragment_to_clipFragment,
                bundle
            )
        )
    }

    override fun setupListeners() {
        View.OnClickListener { btnView ->
            when (btnView.id) {
                R.id.tv_back -> {
                    if (mainViewModel?.isFromPlayer == true)
                        activity?.onBackPressed()
                    else navigateTo(NavigateItem.Up)
                }
            }
        }.also {
            tv_back.setOnClickListener(it)
        }

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.getMyPost(userId, isAdult)
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

    private val onMeMoreDialogListener = object : OnMeMoreDialogListener {
        override fun onCancel() {
            meMoreDialog?.dismiss()
        }

        override fun onDelete(item: BaseMemberPostItem) {
            GeneralDialog.newInstance(
                GeneralDialogData(
                    titleRes = R.string.is_post_delete,
                    messageIcon = R.drawable.ico_default_photo,
                    secondBtn = getString(R.string.btn_confirm),
                    secondBlock = {
                        viewModel.deletePost(
                            item as MemberPostItem,
                            ArrayList(adapter.currentList as List<MemberPostItem>)
                        )
                    },
                    firstBtn = getString(R.string.cancel),
                    isMessageIcon = false
                )
            ).show(requireActivity().supportFragmentManager)
        }

        override fun onEdit(item: BaseMemberPostItem) {
            item as MemberPostItem
            if (item.type == PostType.TEXT) {
                val bundle = Bundle()
                item.id
                bundle.putBoolean(EDIT, true)
                bundle.putSerializable(MEMBER_DATA, item)
                findNavController().navigate(
                    R.id.action_myPostFragment_to_postArticleFragment,
                    bundle
                )
            } else if (item.type == PostType.IMAGE) {
                val bundle = Bundle()
                bundle.putBoolean(EDIT, true)
                bundle.putSerializable(MEMBER_DATA, item)
                findNavController().navigate(R.id.action_myPostFragment_to_postPicFragment, bundle)
            } else if (item.type == PostType.VIDEO) {
                val bundle = Bundle()
                bundle.putBoolean(EDIT, true)
                bundle.putSerializable(MEMBER_DATA, item)
                findNavController().navigate(
                    R.id.action_myPostFragment_to_postVideoFragment,
                    bundle
                )
            }

            meMoreDialog?.dismiss()
        }
    }

    private val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
        override fun onProblemReport(item: BaseMemberPostItem) {
            moreDialog?.dismiss()
            checkStatus { (requireActivity() as MainActivity).showReportDialog(item) }
        }

        override fun onCancel() {
            moreDialog?.dismiss()
        }
    }

    private val myPostListener = object : MyPostListener {
        override fun onMoreClick(item: MemberPostItem) {
            val isMe = viewModel.accountManager.getProfile().userId == item.creatorId
            if (isMe) {
                meMoreDialog =
                    MyPostMoreDialogFragment.newInstance(item, onMeMoreDialogListener)
                        .also {
                            it.show(
                                requireActivity().supportFragmentManager,
                                MoreDialogFragment::class.java.simpleName
                            )
                        }
            } else {
                moreDialog =
                    MoreDialogFragment.newInstance(item, onMoreDialogListener).also {
                        it.show(
                            requireActivity().supportFragmentManager,
                            MoreDialogFragment::class.java.simpleName
                        )
                    }
            }
        }

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            checkStatus { viewModel.likePost(item, position, isLike) }
        }

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {
            checkStatus {
                val bundle = ClipFragment.createBundle(ArrayList(mutableListOf(item[position])), 0)
                navigationToVideo(bundle)
            }
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {
            val bundle = ClipFragment.createBundle(ArrayList(mutableListOf(item[position])), 0)
            navigationToVideo(bundle)
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
                    navigationToPicture(bundle)
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 0)
                    navigationToText(bundle)
                }
            }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            checkStatus {
                when (adultTabType) {
                    AdultTabType.PICTURE -> {
                        val bundle = PictureDetailFragment.createBundle(item, 1)
                        navigationToPicture(bundle)
                    }
                    AdultTabType.TEXT -> {
                        val bundle = TextDetailFragment.createBundle(item, 1)
                        navigationToText(bundle)
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
            checkStatus { viewModel.favoritePost(item, position, isFavorite) }
        }

        override fun onFollowClick(
            items: List<MemberPostItem>,
            position: Int,
            isFollow: Boolean
        ) {
            checkStatus { viewModel.followPost(ArrayList(items), position, isFollow) }
        }
    }

    private val memberPostFuncItem by lazy {
        MemberPostFuncItem(
            {},
            { id, function -> getBitmap(id, function) },
            { _, _, _, _ -> }
        )
    }

    private fun getBitmap(id: String, update: ((String) -> Unit)) {
        viewModel.getBitmap(id, update)
    }
}