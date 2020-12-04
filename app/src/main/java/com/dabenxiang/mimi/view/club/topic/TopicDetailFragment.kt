package com.dabenxiang.mimi.view.club.topic

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.player.ui.ClipPlayerFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_club_topic.*

class TopicDetailFragment : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"

        fun createBundle(
            item: MemberClubItem
        ): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
        // UI spec only three tabs use StringArray ?
        val tabTitle = App.self.resources.getStringArray(R.array.club_hot_topic_tabs).toMutableList()
    }

    private val viewModel: TopicDetailViewModel by viewModels()

    private val memberClubItem by lazy { arguments?.getSerializable(KEY_DATA) as MemberClubItem }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId(): Int {
        return R.layout.fragment_club_topic
    }

    override fun setupFirstTime() {
        super.setupFirstTime()

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()

        setUpUI()
    }

    private fun setUpUI() {
        tv_title.text = memberClubItem.title
        tv_desc.text = memberClubItem.description
        tv_follow_count.text = memberClubItem.followerCount.toString()
        tv_post_count.text = memberClubItem.postCount.toString()
        updateFollow()
        val bitmap = LruCacheUtils.getLruCache(memberClubItem.avatarAttachmentId.toString())
        bitmap?.also { Glide.with(requireContext()).load(it).circleCrop().into(iv_avatar) }

        viewPager.reduceDragSensitivity()

        viewPager.isUserInputEnabled = true

        viewPager.offscreenPageLimit = 3

        viewPager.adapter =
            TopicPagerAdapter(
                TopicDetailFuncItem({ orderBy, funUpdateList, funUpdateCount ->
                    getPost(
                        orderBy,
                        funUpdateList,
                        funUpdateCount
                    )
                },
                    { id, view, resId -> viewModel.loadImage(id, view, resId) },
                    { item, items, isFollow, func -> followMember(item, items, isFollow, func) },
                    { item, isLike, func -> likePost(item, isLike, func) }
                ),
                adultListener
            )

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitle[position]
            viewPager.setCurrentItem(tab.position, true)
        }.attach()
    }

    override fun setupObservers() {
        viewModel.followClubResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    updateFollow()
                }
                is Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.deletePostResult?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    val adapter =
                        (viewPager.adapter as TopicPagerAdapter).getListAdapter(tabLayout.selectedTabPosition)
                    adapter.removedPosList.add(it.result)
                    adapter.notifyItemChanged(it.result)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.cleanRemovedPosList.observe(viewLifecycleOwner, Observer {
            val adapter =
                (viewPager.adapter as TopicPagerAdapter).getListAdapter(tabLayout.selectedTabPosition)
            adapter.removedPosList.clear()
        })

        viewModel.updateCountHottest.observe(viewLifecycleOwner, Observer {
            updateCountHottest.invoke(it)
        })

        viewModel.updateCountNewest.observe(viewLifecycleOwner, Observer {
            updateCountNewest.invoke(it)
        })

        viewModel.updateCountVideo.observe(viewLifecycleOwner, Observer {
            updateCountVideo.invoke(it)
        })
    }

    override fun setupListeners() {

        ib_back.setOnClickListener { findNavController().navigateUp() }
        tv_follow.setOnClickListener {
            checkStatus {
                viewModel.followClub(
                    memberClubItem,
                    !memberClubItem.isFollow
                )
            }
        }
    }

    private fun updateFollow() {
        val isFollow = memberClubItem.isFollow
        if (isFollow) {
            tv_follow.text = requireContext().getString(R.string.followed)
            tv_follow.background = ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_white_1_stroke_radius_16
            )
            tv_follow.setTextColor(requireContext().getColor(R.color.color_black_1_60))
        } else {
            tv_follow.text = requireContext().getString(R.string.follow)
            tv_follow.background = ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_red_1_stroke_radius_16
            )
            tv_follow.setTextColor(requireContext().getColor(R.color.color_red_1))
        }
    }

    override fun navigationToText(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_clubTextFragment,
                bundle
            )
        )
    }

    override fun navigationToPicture(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_clubPicFragment,
                bundle
            )
        )
    }

    override fun navigationToClip(b: Bundle) {
        val item = arguments?.get(MyPostFragment.MEMBER_DATA) as MemberPostItem
        val bundle = ClipPlayerFragment.createBundle(item.id)

        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_clipPlayerFragment,
                bundle
            )
        )
    }

    private val adultListener = object : AdultListener {
        override fun onFollowPostClick(item: MemberPostItem, position: Int, isFollow: Boolean) {}

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {}

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            checkStatus {
                when (adultTabType) {
                    AdultTabType.PICTURE -> {
                        val bundle = PictureDetailFragment.createBundle(item, 1)
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_to_clubPicFragment,
                                bundle
                            )
                        )
                    }
                    AdultTabType.TEXT -> {
                        val bundle = TextDetailFragment.createBundle(item, 1)
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_to_clubTextFragment,
                                bundle
                            )
                        )
                    }
                    AdultTabType.CLIP -> {
                        val bundle = ClipPlayerFragment.createBundle(item.id, 1)
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_to_clipPlayerFragment,
                                bundle
                            )
                        )
                    }
                    else -> {
                    }
                }
            }
        }

        override fun onMoreClick(item: MemberPostItem, position: Int) {
            onMoreClick(item, position) {

                val searchPostItem = arguments?.getSerializable(KEY_DATA)

                val bundle = Bundle()
                bundle.putBoolean(MyPostFragment.EDIT, true)
                bundle.putString(BasePostFragment.PAGE, BasePostFragment.CLUB)
                bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
                bundle.putSerializable(SearchPostFragment.KEY_DATA, searchPostItem)

                it as MemberPostItem
                when (item.type) {
                    PostType.TEXT -> {
                        findNavController().navigate(
                            R.id.action_topicDetailFragment_to_postArticleFragment,
                            bundle
                        )
                    }
                    PostType.IMAGE -> {
                        findNavController().navigate(
                            R.id.action_topicDetailFragment_to_postPicFragment,
                            bundle
                        )
                    }
                    PostType.VIDEO -> {
                        findNavController().navigate(
                            R.id.action_topicDetailFragment_to_postVideoFragment,
                            bundle
                        )
                    }
                    else -> {
                    }
                }
            }
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = PictureDetailFragment.createBundle(item, 0)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_to_clubPicFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 0)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_to_clubTextFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.CLIP -> {
                    val bundle = ClipPlayerFragment.createBundle(item.id)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_to_clipPlayerFragment,
                            bundle
                        )
                    )
                }
                else -> {
                }
            }
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {
            val bundle = ClipFragment.createBundle(ArrayList(item), position)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_clipFragment,
                    bundle
                )
            )
        }

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {
            checkStatus {
                val bundle = ClipFragment.createBundle(ArrayList(item), position)
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_to_clipFragment,
                        bundle
                    )
                )
            }
        }

        override fun onChipClick(type: PostType, tag: String) {
            val item = SearchPostItem(type = type, tag = tag)
            val bundle = SearchPostFragment.createBundle(item)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_topicDetailFragment_to_searchPostFragment,
                    bundle
                )
            )
        }

        override fun onAvatarClick(userId: Long, name: String) {
            val bundle = MyPostFragment.createBundle(
                userId, name,
                isAdult = true,
                isAdultTheme = true
            )
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_topicDetailFragment_to_myPostFragment,
                    bundle
                )
            )
        }

        override fun onFavoriteClick(item: MemberPostItem, position: Int, isFavorite: Boolean) {

        }
    }

    private lateinit var updateCountHottest: (Int) -> Unit
    private lateinit var updateCountNewest: (Int) -> Unit
    private lateinit var updateCountVideo: (Int) -> Unit

    private fun getPost(
        orderBy: OrderBy,
        updateList: ((PagedList<MemberPostItem>) -> Unit),
        updateCount: (Int) -> Unit
    ) {
        viewModel.getMemberPosts(memberClubItem.tag, orderBy, updateList)
        when (orderBy) {
            OrderBy.HOTTEST -> updateCountHottest = updateCount
            OrderBy.NEWEST -> updateCountNewest = updateCount
            OrderBy.VIDEO -> updateCountVideo = updateCount
        }
    }

    private fun followMember(
        memberPostItem: MemberPostItem,
        items: List<MemberPostItem>,
        isFollow: Boolean,
        update: (Boolean) -> Unit
    ) {
        checkStatus { viewModel.followMember(memberPostItem, items, isFollow, update) }
    }

    private fun likePost(
        memberPostItem: MemberPostItem,
        isLike: Boolean,
        update: (Boolean, Int) -> Unit
    ) {
        checkStatus { viewModel.likePost(memberPostItem, isLike, update) }
    }
}

/**
 * Reduces drag sensitivity of [ViewPager2] widget
 */
fun ViewPager2.reduceDragSensitivity() {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView

    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(recyclerView) as Int
    touchSlopField.set(recyclerView, touchSlop * 2)       // "8" was obtained experimentally
}