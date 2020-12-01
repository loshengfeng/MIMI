package com.dabenxiang.mimi.view.like

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyLikeListener
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem
import com.dabenxiang.mimi.view.adapter.ClubLikeAdapter
import com.dabenxiang.mimi.view.adapter.MiMiLikeAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_club_short.*
import kotlinx.android.synthetic.main.fragment_my_follow.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import timber.log.Timber


class LikeFragment : BaseFragment() {
    private val viewModel: LikeViewModel by viewModels()

    companion object {
        const val NO_DATA = 0
        const val TYPE_POST = 0
        const val TYPE_MIMI = 1
    }

    private val clublikeAdapter by lazy { ClubLikeAdapter(listener) }

    private val mimilikeAdapter by lazy { MiMiLikeAdapter(listener) }

    private val listener = object : MyLikeListener {
        override fun onMoreClick(item: PostFavoriteItem, position: Int) {
            TODO("Not yet implemented")
        }

        override fun onLikeClick(item: PostFavoriteItem, position: Int, isLike: Boolean) {
            TODO("Not yet implemented")
        }

        override fun onClipCommentClick(item: List<PostFavoriteItem>, position: Int) {
            TODO("Not yet implemented")
        }

        override fun onChipClick(item: PostFavoriteItem, tag: String) {
            Timber.i("LikeFragment onChipClick  $item")
//            val bundle = PlayerV2Fragment.createBundle(PlayerItem(item.videoId ?: 0))
//            navigateTo(
//                NavigateItem.Destination(
//                    R.id.action_myFollowFragmentV2_to_playerV2Fragment,
//                    bundle
//                )
//            )
        }

        override fun onItemClick(item: PostFavoriteItem, type: Int) {
            Timber.i("LikeFragment onItemClick  $item")
            val bundle = MyPostFragment.createBundle(
                item.posterId, item.title,
                isAdult = true,
                isAdultTheme = true
            )
            navigateTo(
                NavigateItem.Destination(R.id.action_to_myPostFragment, bundle)
            )
        }

        override fun onCommentClick(item: PostFavoriteItem, type: Int) {
            TODO("Not yet implemented")
        }

        override fun onFavoriteClick(
            item: PostFavoriteItem,
            position: Int,
            isFavorite: Boolean,
            attachmenttype: Int
        ) {
            TODO("Not yet implemented")
        }
    }

    private var vpAdapter: LikeViewPagerAdapter? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.showProgress.observe(this) {
            layout_refresh.isRefreshing = it
        }
        viewModel.clubCount.observe(this, Observer {
            if (layout_tab.selectedTabPosition == TYPE_POST) refreshUi(TYPE_POST, it)
        })

        viewModel.mimiCount.observe(this, Observer {
            if (layout_tab.selectedTabPosition == TYPE_MIMI) refreshUi(TYPE_MIMI, it)
        })

        viewModel.clubDetail.observe(this, Observer {
            when (it) {
                is Success -> {
//                    val bundle = ClipPlayerFragment.createBundle(0)
//                    navigateTo(
//                        NavigateItem.Destination(
//                            R.id.action_likeFragment_to_clipPlayerFragment,
//                            bundle
//                        )
//                    )
                }
                is Error -> onApiError(it.throwable)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        useAdultTheme(false)
    }

    override fun setupFirstTime() {
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_my_like
    }

    override fun setupObservers() {}

    private val onCleanDialogListener = object : OnCleanDialogListener {
        override fun onClean() {
            if (layout_tab.selectedTabPosition == TYPE_POST) {
                viewModel.cleanAllFollowMember()
            } else {
                viewModel.cleanAllFollowClub()
            }
        }
    }

    override fun setupListeners() {
        View.OnClickListener { btnView ->
            when (btnView.id) {
                R.id.tv_back -> navigateTo(NavigateItem.Up)
                R.id.tv_clean -> CleanDialogFragment.newInstance(
                    onCleanDialogListener,
                    if (layout_tab.selectedTabPosition == TYPE_POST)
                        R.string.follow_clean_member_dlg_msg
                    else
                        R.string.follow_clean_club_dlg_msg
                ).also {
                    it.show(
                        requireActivity().supportFragmentManager,
                        CleanDialogFragment::class.java.simpleName
                    )
                }
            }
        }.also {
            tv_back.setOnClickListener(it)
            tv_clean.setOnClickListener(it)
        }
    }

    override fun initSettings() {
        layout_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.getData(clublikeAdapter, mimilikeAdapter, tab.position)
            }
        })
        vpAdapter = LikeViewPagerAdapter(
            requireContext(),
            mimilikeAdapter,
            clublikeAdapter
        ) {
            viewModel.getData(clublikeAdapter, mimilikeAdapter, 0)
        }
        vp.adapter = vpAdapter
        layout_tab.setupWithViewPager(vp)

        tv_clean.visibility = View.VISIBLE
        tv_title.setText(R.string.like_title)
    }

    private fun refreshUi(type: Int, size: Int) {
        tv_clean.isEnabled = size != NO_DATA
        vpAdapter?.refreshUi(type, size)
    }
}
