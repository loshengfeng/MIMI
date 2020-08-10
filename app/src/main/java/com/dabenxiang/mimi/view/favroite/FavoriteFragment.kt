package com.dabenxiang.mimi.view.favroite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.FunctionType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.adapter.FavoriteAdapter
import com.dabenxiang.mimi.view.adapter.FavoriteTabAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_post_favorite.*
import kotlinx.android.synthetic.main.item_personal_is_not_login.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import timber.log.Timber

class FavoriteFragment : BaseFragment() {

    companion object {
        const val NO_DATA = 0
        const val TAB_PRIMARY = 0
        const val TAB_SECONDARY = 1
        const val TYPE_NORMAL = 0
        const val TYPE_ADULT = 1
        private const val TYPE_MIMI = 0
        const val TYPE_SHORT_VIDEO = 1
        var lastPrimaryIndex = TYPE_NORMAL
        var lastSecondaryIndex = TYPE_MIMI
    }

    private val viewModel: FavoriteViewModel by viewModels()

    private val favoriteAdapter by lazy { FavoriteAdapter(listener) }

    private var interactionListener: InteractionListener? = null

    private val primaryAdapter by lazy {
        FavoriteTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                setTabPosition(TAB_PRIMARY, index)
                viewModel.initData(lastPrimaryIndex, lastSecondaryIndex)
            }
        }, true)
    }

    private val secondaryAdapter by lazy {
        FavoriteTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                setTabPosition(TAB_SECONDARY, index)
                viewModel.initData(lastPrimaryIndex, lastSecondaryIndex)
            }
        }, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback {
            interactionListener?.changeNavigationPosition(
                R.id.navigation_home
            )
        }
        useAdultTheme(false)
        initSettings()
    }

    override fun setupFirstTime() {
        val primaryList = listOf(
            getString(R.string.favorite_normal),
            getString(R.string.favorite_adult)
        )

        primaryAdapter.submitList(primaryList, lastPrimaryIndex)

        rv_secondary.adapter = secondaryAdapter

        val secondaryList = listOf(
            getString(R.string.favorite_tab_mimi),
            getString(R.string.favorite_tab_short)
        )

        secondaryAdapter.submitList(secondaryList, lastSecondaryIndex)

        rv_content.adapter = favoriteAdapter

        viewModel.initData(lastPrimaryIndex, lastSecondaryIndex)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_post_favorite
    }

    override fun setupObservers() {
        viewModel.playList.observe(viewLifecycleOwner, Observer { favoriteAdapter.submitList(it) })
        viewModel.postList.observe(viewLifecycleOwner, Observer { favoriteAdapter.submitList(it) })
        viewModel.dataCount.observe(viewLifecycleOwner, Observer { refreshUi(it) })


        viewModel.cleanResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Empty -> {
                    viewModel.videoIDList.clear()
                    viewModel.initData(lastPrimaryIndex, lastSecondaryIndex)
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })

        viewModel.likeResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Success -> {
                    favoriteAdapter.notifyDataSetChanged()
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })

        viewModel.followResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Success -> {
                    favoriteAdapter.notifyDataSetChanged()
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })


        viewModel.favoriteResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Success -> {
                    viewModel.initData(lastPrimaryIndex, lastSecondaryIndex)
                    GeneralUtils.showToast(
                        requireContext(),
                        getString(R.string.favorite_delete_favorite)
                    )
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })

        viewModel.reportResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Success -> {
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })

        viewModel.attachmentByTypeResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    val attachmentItem = it.result
                    LruCacheUtils.putLruCache(attachmentItem.id!!, attachmentItem.bitmap!!)
                    when (attachmentItem.type) {
                        AttachmentType.ADULT_HOME_CLIP -> {
                            favoriteAdapter.update(attachmentItem.position ?: 0)
                        }
                        AttachmentType.ADULT_AVATAR -> {
                            favoriteAdapter.update(attachmentItem.position ?: 0)
                        }
                        else -> {
                        }
                    }
                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })
    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_clean -> {
                    CleanDialogFragment.newInstance(onCleanDialogListener).also {
                        it.show(
                            requireActivity().supportFragmentManager,
                            CleanDialogFragment::class.java.simpleName
                        )
                    }
                }
                R.id.tv_login -> navigateTo(
                    NavigateItem.Destination(
                        R.id.action_postFavoriteFragment_to_loginFragment,
                        LoginFragment.createBundle(LoginFragment.TYPE_LOGIN)
                    )
                )
                R.id.tv_register -> navigateTo(
                    NavigateItem.Destination(
                        R.id.action_postFavoriteFragment_to_loginFragment,
                        LoginFragment.createBundle(LoginFragment.TYPE_REGISTER)
                    )
                )
            }
        }.also {
            tv_clean.setOnClickListener(it)
            tv_login.setOnClickListener(it)
            tv_register.setOnClickListener(it)
        }

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.initData(lastPrimaryIndex, lastSecondaryIndex)
        }
    }

    override fun initSettings() {
        tv_back.visibility = View.GONE
        tv_title.text = getString(R.string.favorite_title)

        when(viewModel.accountManager.isLogin()) {
            true -> {
                item_is_not_Login.visibility = View.GONE
                item_is_Login.visibility = View.VISIBLE
                tv_clean.visibility = View.VISIBLE
                rv_primary.adapter = primaryAdapter
            }
            false -> {
                item_is_not_Login.visibility = View.VISIBLE
                item_is_Login.visibility = View.GONE
                tv_version_is_not_login.text = BuildConfig.VERSION_NAME
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            interactionListener = context as InteractionListener
        } catch (e: ClassCastException) {
            Timber.e("FavoriteFragment interaction listener can't cast")
        }
    }

    private fun refreshUi(size: Int) {
        rv_content.visibility = when (size) {
            NO_DATA -> View.GONE
            else -> View.VISIBLE
        }

        item_no_data.visibility = when (size) {
            NO_DATA -> View.VISIBLE
            else -> View.GONE
        }

        tv_clean.isEnabled = size > 0

        when (lastPrimaryIndex) {
            TYPE_NORMAL -> layout_adult.visibility = View.GONE
            TYPE_ADULT -> layout_adult.visibility = View.VISIBLE
        }
    }

    private fun setTabPosition(type: Int, index: Int) {
        when (type) {
            TAB_PRIMARY -> {
                lastPrimaryIndex = index
                primaryAdapter.setLastSelectedIndex(lastPrimaryIndex)
            }
            TAB_SECONDARY -> {
                lastSecondaryIndex = index
                secondaryAdapter.setLastSelectedIndex(lastSecondaryIndex)
            }
        }
    }

    private val listener = object : FavoriteAdapter.EventListener {

        override fun onGetAttachment(id: String, position: Int, type: AttachmentType) {
            viewModel.getAttachment(id, position, type)
        }

        override fun onVideoClick(item: Any) {
            when (item) {
                is PlayItem -> {
                    val playerData = PlayerItem(
                        item.videoId ?: 0, item.isAdult ?: false
                    )
                    val intent = Intent(requireContext(), PlayerActivity::class.java)
                    intent.putExtras(PlayerActivity.createBundle(playerData))
                    startActivity(intent)
                }
                is PostFavoriteItem -> {
                    goShortVideoDetailPage(item)
                }
            }
        }


        override fun onFunctionClick(type: FunctionType, view: View, item: Any) {
            when (type) {
                FunctionType.LIKE -> {
                    when (item) {
                        is PlayItem -> {
                            viewModel.currentPlayItem = item
                            item.videoId?.let {
                                viewModel.modifyLike(it)
                            }
                        }
                        is PostFavoriteItem -> {
                            viewModel.currentPostItem = item
                            item.postId?.let {
                                viewModel.modifyPostLike(it)
                            }
                        }
                    }
                }

                FunctionType.FAVORITE -> {
                    // 點擊後加入收藏,
                    when (item) {
                        is PlayItem -> {
                            viewModel.currentPlayItem = item
                            item.videoId?.let {
                                viewModel.modifyFavorite(it)
                            }
                        }
                        is PostFavoriteItem -> {
                            viewModel.currentPostItem = item
                            item.postId?.let {
                                viewModel.removePostFavorite(it)
                            }
                        }
                    }
                }

                FunctionType.SHARE -> {
                    /* 點擊後複製網址 */
                    when (item) {
                        is PlayItem -> {
                            if (item.tags == null || item.tags.first()
                                    .isEmpty() || item.videoId == null
                            ) {
                                GeneralUtils.showToast(requireContext(), "copy url error")
                            } else {
                                GeneralUtils.copyToClipboard(
                                    requireContext(),
                                    viewModel.getShareUrl(item.tags[0], item.videoId, item.episode)
                                )
                                GeneralUtils.showToast(
                                    requireContext(),
                                    requireContext().getString(R.string.copy_url)
                                )
                            }
                        }
                    }
                }

                FunctionType.MSG -> {
                    // 點擊評論，進入播放頁面滾動到最下面
                    when (item) {
                        is PlayItem -> {
                            if (item.tags == null || item.tags.first()
                                    .isEmpty() || item.videoId == null
                            ) {
                                GeneralUtils.showToast(
                                    requireContext(),
                                    getString(R.string.unexpected_error)
                                )
                            } else {
                                val playerData =
                                    PlayerItem(
                                        item.videoId ?: 0, item.isAdult
                                            ?: false
                                    )
                                val intent = Intent(requireContext(), PlayerActivity::class.java)
                                intent.putExtras(PlayerActivity.createBundle(playerData, true))
                                startActivity(intent)
                            }
                        }
                        is PostFavoriteItem -> {
                            goShortVideoDetailPage(item)
                        }
                    }
                }

                FunctionType.MORE -> {
                    // 若已經檢舉過則Disable -> todo: can't determine?
//                    MoreDialogFragment.newInstance(item as BaseItem, onReportDialogListener).also {
//                        it.show(
//                            activity!!.supportFragmentManager,
//                            MoreDialogFragment::class.java.simpleName
//                        )
//                    }
                }
                FunctionType.FOLLOW -> {
                    // 追蹤與取消追蹤
                    when (item) {
                        is PostFavoriteItem -> {
                            if (item.posterId == null || item.posterId == 0L) {
                                GeneralUtils.showToast(
                                    requireContext(),
                                    getString(R.string.unexpected_error)
                                )
                            } else {
                                viewModel.currentPostItem = item
                                viewModel.modifyFollow(item.posterId, item.isFollow ?: false)
                            }
                        }
                    }
                }
                else -> {
                }
            }
        }

        override fun onChipClick(text: String, type: Int?) {
            // 點擊標籤後進入 Search page
            useAdultTheme(lastPrimaryIndex == TYPE_ADULT)
            if (lastSecondaryIndex == TYPE_MIMI) {
                val bundle = SearchVideoFragment.createBundle(tag = text)
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_postFavoriteFragment_to_searchVideoFragment,
                        bundle
                    )
                )
            } else {
                val bundle = SearchPostFragment.createBundle(
                    SearchPostItem(
                        PostType.getTypeByValue(type),
                        text
                    )
                )
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_postFavoriteFragment_to_searchPostFragment,
                        bundle
                    )
                )
            }
        }

        override fun onAvatarClick(userId: Long, name: String) {
            val bundle = MyPostFragment.createBundle(
                userId, name,
                isAdult = true,
                isAdultTheme = true
            )
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_postFavoriteFragment_to_myPostFragment,
                    bundle
                )
            )
        }
    }

    private val onCleanDialogListener = object : OnCleanDialogListener {
        override fun onClean() {
            if (lastPrimaryIndex == TYPE_ADULT && lastSecondaryIndex == TYPE_SHORT_VIDEO) {
                viewModel.deleteAllPostFavorite()
            } else {
                viewModel.deleteFavorite()
            }
        }
    }

    /**
     * 進到短影片的詳細頁面
     */
    private fun goShortVideoDetailPage(item: PostFavoriteItem) {
        if (item.tags == null || item.tags.first()
                .isEmpty() || item.postId == null
        ) {
            GeneralUtils.showToast(
                requireContext(),
                getString(R.string.unexpected_error)
            )
        } else {
            val memberPost: ArrayList<MemberPostItem> = Gson().fromJson(
                Gson().toJson(viewModel.currentPostList),
                object : TypeToken<ArrayList<MemberPostItem>>() {}.type
            )
            memberPost.forEach memberItem@{ memberItem ->
                viewModel.currentPostList.forEach { postItem ->
                    if (postItem.id == memberItem.id) {
                        memberItem.avatarAttachmentId = postItem.posterAvatarAttachmentId
                            ?: 0
                        memberItem.id = postItem.postId ?: 0
                        memberItem.isFavorite = true
                        memberItem.creatorId = postItem.posterId ?: 0
                        memberItem.likeType =
                            if (postItem.likeType == 0) LikeType.LIKE else LikeType.DISLIKE
                        memberItem.postFriendlyName = postItem.posterName
                        return@memberItem
                    }
                }
            }
            useAdultTheme(true)
            val bundle = ClipFragment.createBundle(memberPost, item.position)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_postFavoriteFragment_to_clipFragment,
                    bundle
                )
            )
        }

    }
}