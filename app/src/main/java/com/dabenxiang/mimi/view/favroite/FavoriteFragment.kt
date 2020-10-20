package com.dabenxiang.mimi.view.favroite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.enums.FunctionType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.adapter.FavoriteAdapter
import com.dabenxiang.mimi.view.adapter.FavoriteTabAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
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
        var lastPrimaryIndex = TYPE_MIMI
    }

    private val viewModel: FavoriteViewModel by viewModels()

    private val favoriteAdapter by lazy { FavoriteAdapter(listener) }

    private var interactionListener: InteractionListener? = null

    private val primaryAdapter by lazy {
        FavoriteTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                setTabPosition(index)
                viewModel.initData(lastPrimaryIndex)
            }
        }, true)
    }

    private var needRefresh = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.playList.observe(this, Observer {
            if (lastPrimaryIndex != TYPE_SHORT_VIDEO)
                favoriteAdapter.submitList(it)
        })
        viewModel.postList.observe(this, Observer {
            if (lastPrimaryIndex == TYPE_SHORT_VIDEO)
                favoriteAdapter.submitList(it)
        })
        viewModel.dataCount.observe(this, Observer { refreshUi(it) })

        viewModel.cleanResult.observe(this, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Empty -> {
                    viewModel.videoIDList.clear()
                    viewModel.initData(lastPrimaryIndex)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.likeResult.observe(this, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> favoriteAdapter.notifyDataSetChanged()
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.followResult.observe(this, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> favoriteAdapter.notifyDataSetChanged()
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.favoriteResult.observe(this, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> {
                    viewModel.initData(lastPrimaryIndex)
                    GeneralUtils.showToast(
                        requireContext(),
                        getString(R.string.favorite_delete_favorite)
                    )
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.reportResult.observe(this, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.isEmailConfirmed.observe(this, Observer {
            when (it) {
                is Success -> {
                    if (!it.result) {
                        interactionListener?.changeNavigationPosition(R.id.navigation_personal)
                    } else {
                        initView()
                    }
                }
                is Error -> onApiError(it.throwable)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback {
            interactionListener?.changeNavigationPosition(
                R.id.navigation_adult
            )
        }
        useAdultTheme(false)
        initSettings()
        favoriteAdapter.notifyDataSetChanged()
    }

    override fun setupFirstTime() {
        val primaryList = listOf(
            getString(R.string.favorite_tab_mimi),
            getString(R.string.favorite_tab_short)
        )

        primaryAdapter.submitList(primaryList, lastPrimaryIndex)

        rv_content.adapter = favoriteAdapter

        viewModel.initData(lastPrimaryIndex)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_post_favorite
    }

    override fun setupObservers() {
    }

    private fun initView() {
        item_is_not_Login.visibility = View.GONE
        item_is_Login.visibility = View.VISIBLE
        tv_clean.visibility = View.VISIBLE
        rv_primary.adapter = primaryAdapter
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
                R.id.tv_login -> {
                    needRefresh = true
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_postFavoriteFragment_to_loginFragment,
                            LoginFragment.createBundle(LoginFragment.TYPE_LOGIN)
                        )
                    )
                }
                R.id.tv_register -> {
                    needRefresh = true
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_postFavoriteFragment_to_loginFragment,
                            LoginFragment.createBundle(LoginFragment.TYPE_REGISTER)
                        )
                    )
                }
            }
        }.also {
            tv_clean.setOnClickListener(it)
            tv_login.setOnClickListener(it)
            tv_register.setOnClickListener(it)
        }

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.initData(lastPrimaryIndex)
        }
    }

    override fun initSettings() {
        tv_back.visibility = View.GONE
        tv_title.text = getString(R.string.favorite_title)

        when (viewModel.isLogin()) {
            true -> {
                //TODO: 目前先不判斷是否有驗證過
//                viewModel.checkEmailConfirmed()
                initView()
            }
            false -> {
                item_is_not_Login.visibility = View.VISIBLE
                item_is_Login.visibility = View.GONE
                tv_version_is_not_login.text = BuildConfig.VERSION_NAME
                tv_clean.visibility = View.GONE
            }
        }
        if (needRefresh) {
            needRefresh = false
            viewModel.initData(lastPrimaryIndex)
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
    }

    private fun setTabPosition(index: Int) {
        lastPrimaryIndex = index
        primaryAdapter.setLastSelectedIndex(lastPrimaryIndex)
    }

    private val listener = object : FavoriteAdapter.EventListener {

        override fun onGetAttachment(id: Long?, view: ImageView, type: LoadImageType) {
            viewModel.loadImage(id, view, type)
        }

        override fun onVideoClick(item: Any, position: Int?) {
            when (item) {
                is PlayItem -> {
                    val playerData = PlayerItem(
                        item.videoId ?: 0, item.isAdult ?: false
                    )
                    val intent = Intent(requireContext(), PlayerActivity::class.java)
                    intent.putExtras(PlayerActivity.createBundle(playerData))
                    startActivity(intent)
                }
                is MemberPostItem -> {
                    position?.let { goShortVideoDetailPage(item, position) }
                }
            }
        }


        override fun onFunctionClick(
            type: FunctionType,
            view: View,
            item: Any,
            position: Int?
        ) {
            when (type) {
                FunctionType.LIKE -> {
                    when (item) {
                        is PlayItem -> {
                            viewModel.currentPlayItem = item
                            item.videoId?.let {
                                viewModel.modifyLike(it)
                            }
                        }
                        is MemberPostItem -> {
                            viewModel.currentPostItem = item
                            viewModel.modifyPostLike(item.id)
                        }
                    }
                }

                FunctionType.FAVORITE -> {
                    // 點擊後加入收藏,
                    GeneralDialog.newInstance(
                        GeneralDialogData(
                            titleRes = R.string.favorite_delete_this_favorite,
                            messageIcon = R.drawable.ico_default_photo,
                            secondBtn = getString(R.string.btn_confirm),
                            secondBlock = {
                                when (item) {
                                    is PlayItem -> {
                                        viewModel.currentPlayItem = item
                                        item.videoId?.let {
                                            viewModel.modifyFavorite(it)
                                        }
                                    }
                                    is MemberPostItem -> {
                                        viewModel.currentPostItem = item
                                        viewModel.removePostFavorite(item.id)
                                    }
                                }
                            },
                            firstBtn = getString(R.string.cancel),
                            isMessageIcon = false
                        )
                    ).show(requireActivity().supportFragmentManager)
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
                        is MemberPostItem -> {
                            position?.let { goShortVideoDetailPage(item, position) }
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
                        is MemberPostItem -> {
                            if (item.creatorId == 0L) {
                                GeneralUtils.showToast(
                                    requireContext(),
                                    getString(R.string.unexpected_error)
                                )
                            } else {
                                viewModel.currentPostItem = item
                                viewModel.modifyFollow(item.creatorId, item.isFollow)
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
            useAdultTheme(false)
            if (lastPrimaryIndex == TYPE_MIMI) {
                val bundle = SearchVideoFragment.createBundle(tag = text)
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_to_searchVideoFragment,
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
                    R.id.action_to_myPostFragment,
                    bundle
                )
            )
        }
    }

    private val onCleanDialogListener = object : OnCleanDialogListener {
        override fun onClean() {
            if (lastPrimaryIndex == TYPE_SHORT_VIDEO) {
                viewModel.deleteAllPostFavorite()
            } else {
                viewModel.deleteFavorite()
            }
        }
    }

    /**
     * 進到短影片的詳細頁面
     */
    private fun goShortVideoDetailPage(
        item: MemberPostItem,
        position: Int
    ) {
        if (item.tags == null || item.tags!!.first().isEmpty()) {
            GeneralUtils.showToast(
                requireContext(),
                getString(R.string.unexpected_error)
            )
        } else {
            useAdultTheme(false)
            val bundle = ClipFragment.createBundle(viewModel.currentPostList, position, false)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_postFavoriteFragment_to_clipFragment,
                    bundle
                )
            )
        }

    }
}