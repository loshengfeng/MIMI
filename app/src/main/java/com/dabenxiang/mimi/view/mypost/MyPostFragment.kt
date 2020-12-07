package com.dabenxiang.mimi.view.mypost

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.adapter.MyPostPagedAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.club.pic.ClubPicFragment
import com.dabenxiang.mimi.view.club.text.ClubTextFragment
import com.dabenxiang.mimi.view.mypost.MyPostViewModel.Companion.USER_ID_ME
import com.dabenxiang.mimi.view.player.ui.ClipPlayerFragment
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.MY_POST
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.PAGE
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import kotlinx.android.synthetic.main.fragment_my_post.*
import kotlinx.android.synthetic.main.item_follow_no_data.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import timber.log.Timber


class MyPostFragment : BaseFragment() {

    private lateinit var adapter: MyPostPagedAdapter

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
        useAdultTheme(false)
    }

    override fun initSettings() {
        arguments?.let {
            userId = it.getLong(KEY_USER_ID, USER_ID_ME)
            userName = it.getString(KEY_USER_NAME, "")
            isAdult = it.getBoolean(KEY_IS_ADULT, true)
            isAdultTheme = false
        }

        adapter = MyPostPagedAdapter(
            requireContext(),
            isAdultTheme,
            myPostListener,
            memberPostFuncItem
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        cl_layout_bg.isSelected = isAdultTheme
        cl_bg.isSelected = isAdultTheme
        tv_title.text = if (userId == USER_ID_ME) getString(R.string.personal_my_post) else userName
        tv_title.isSelected = isAdultTheme
        tv_back.isSelected = isAdultTheme
        iv_icon.setImageResource(R.drawable.img_conment_empty)
        tv_text.text = getString(R.string.my_post_no_data)
        if (isAdultTheme) layout_refresh.setColorSchemeColors(requireContext().getColor(R.color.color_red_1))
    }

    override fun setupObservers() {
        viewModel.showProgress.observe(this, {
            layout_refresh.isRefreshing = it
        })

        viewModel.myPostItemListResult.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        viewModel.likePostResult.observe(viewLifecycleOwner, {
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

        viewModel.favoriteResult.observe(viewLifecycleOwner, {
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

        viewModel.followResult.observe(viewLifecycleOwner, {
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

        mainViewModel?.deletePostResult?.observe(viewLifecycleOwner, {
            when (it) {
                is Success -> {
                    adapter.removedPosList.add(it.result)
                    adapter.notifyItemChanged(it.result)

                    viewModel.checkPostEmptyUi(adapter.removedPosList.size)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.cleanRemovedPosList.observe(viewLifecycleOwner, {
            adapter.removedPosList.clear()
        })

        viewModel.isNoData.observe(viewLifecycleOwner, {
            v_no_data.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    override fun navigationToText(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_myPostFragment_to_clubTextFragment,
                bundle
            )
        )
    }

    override fun navigationToPicture(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_myPostFragment_to_clubPicFragment,
                bundle
            )
        )
    }

    private fun navigationToVideo(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_myPostFragment_to_clipPlayerFragment,
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

    private val myPostListener = object : MyPostListener {
        override fun onMoreClick(item: MemberPostItem, position: Int) {
            onMoreClick(item, position) {
                it as MemberPostItem
                when (item.type) {
                    PostType.TEXT -> {
                        val bundle = Bundle()
                        bundle.putBoolean(EDIT, true)
                        bundle.putString(PAGE, MY_POST)
                        bundle.putSerializable(MEMBER_DATA, item)
                        findNavController().navigate(
                            R.id.action_myPostFragment_to_postArticleFragment,
                            bundle
                        )
                    }
                    PostType.IMAGE -> {
                        val bundle = Bundle()
                        bundle.putBoolean(EDIT, true)
                        bundle.putString(PAGE, MY_POST)
                        bundle.putSerializable(MEMBER_DATA, item)
                        findNavController().navigate(
                            R.id.action_myPostFragment_to_postPicFragment,
                            bundle
                        )
                    }
                    PostType.VIDEO -> {
                        val bundle = Bundle()
                        bundle.putBoolean(EDIT, true)
                        bundle.putString(PAGE, MY_POST)
                        bundle.putSerializable(MEMBER_DATA, item)
                        findNavController().navigate(
                            R.id.action_myPostFragment_to_postVideoFragment,
                            bundle
                        )
                    }
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
            val item = SearchPostItem(type = PostType.TEXT_IMAGE_VIDEO, tag = tag)
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
                    val bundle = ClubPicFragment.createBundle(item)
                    navigationToPicture(bundle)
                }
                AdultTabType.TEXT -> {
                    val bundle = ClubTextFragment.createBundle(item)
                    navigationToText(bundle)
                }
                AdultTabType.CLIP -> {
                    val bundle = ClipPlayerFragment.createBundle(item.id)
                    navigationToVideo(bundle)
                }
            }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = ClubPicFragment.createBundle(item, 1)
                    navigationToPicture(bundle)
                }
                AdultTabType.TEXT -> {
                    val bundle = ClubTextFragment.createBundle(item, 1)
                    navigationToText(bundle)
                }
                AdultTabType.CLIP -> {
                    val bundle = ClipPlayerFragment.createBundle(item.id, 1)
                    navigationToVideo(bundle)
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

        override fun onAvatarClick(userId: Long, name: String) {

        }
    }

    private val memberPostFuncItem by lazy {
        MemberPostFuncItem(
            {},
            { id, view, type -> viewModel.loadImage(id, view, type) },
            { _, _, _, _ -> }
        )
    }
}