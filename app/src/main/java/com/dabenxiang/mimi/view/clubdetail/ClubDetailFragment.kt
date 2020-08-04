package com.dabenxiang.mimi.view.clubdetail

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.serializable.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.dialog.ReportDialogFragment
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_club_detail.*
import timber.log.Timber

class ClubDetailFragment : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"

        fun createBundle(
            item: MemberClubItem
        ): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }

        val tabTitle = arrayListOf(
            App.self.getString(R.string.hottest),
            App.self.getString(R.string.newest),
            App.self.getString(R.string.video)
        )
    }

    private val viewModel: ClubDetailViewModel by viewModels()

    private val memberClubItem by lazy { arguments?.getSerializable(KEY_DATA) as MemberClubItem }

    private var moreDialog: MoreDialogFragment? = null
    private var reportDialog: ReportDialogFragment? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId(): Int {
        return R.layout.fragment_club_detail
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback { navigateTo(NavigateItem.Up) }
    }

    override fun setupFirstTime() {
        super.setupFirstTime()

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (GeneralUtils.getScreenSize(requireActivity()).second * 0.0245).toInt()

        setUpUI()
    }

    private fun setUpUI() {
        useAdultTheme(true)
        tv_title.text = memberClubItem.title
        tv_desc.text = memberClubItem.description
        tv_follow_count.text = memberClubItem.followerCount.toString()
        tv_post_count.text = memberClubItem.postCount.toString()
        updateFollow()
        val bitmap = LruCacheUtils.getLruCache(memberClubItem.avatarAttachmentId.toString())
        bitmap?.also { Glide.with(requireContext()).load(it).circleCrop().into(iv_avatar) }

        viewPager.isUserInputEnabled = false

        viewPager.adapter =
            ClubPagerAdapter(
                ClubDetailFuncItem({ orderBy, function -> getPost(orderBy, function) },
                    { id, function -> getBitmap(id, function) },
                    { item, isFollow, func -> followMember(item, isFollow, func) },
                    { item, isLike, func -> likePost(item, isLike, func) }),
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
                is ApiResult.Success -> {
                    updateFollow()
                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })

        viewModel.postReportResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Empty -> {
                    GeneralUtils.showToast(requireContext(), getString(R.string.report_success))
                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })
    }

    override fun setupListeners() {
        ib_back.setOnClickListener { findNavController().navigateUp() }
        tv_follow.setOnClickListener {
            viewModel.followClub(
                memberClubItem,
                !memberClubItem.isFollow
            )
        }
    }

    private fun updateFollow() {
        val isFollow = memberClubItem.isFollow ?: false
        if (isFollow) {
            tv_follow.text = requireContext().getString(R.string.followed)
            tv_follow.background =
                requireContext().getDrawable(R.drawable.bg_white_1_stroke_radius_16)
            tv_follow.setTextColor(requireContext().getColor(R.color.color_white_1))
        } else {
            tv_follow.text = requireContext().getString(R.string.follow)
            tv_follow.background =
                requireContext().getDrawable(R.drawable.bg_red_1_stroke_radius_16)
            tv_follow.setTextColor(requireContext().getColor(R.color.color_red_1))
        }
    }

    private val adultListener = object : AdultListener {
        override fun onFollowPostClick(item: MemberPostItem, position: Int, isFollow: Boolean) {}

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {}

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = PictureDetailFragment.createBundle(item, 1)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_clubDetailFragment_to_pictureDetailFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 1)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_clubDetailFragment_to_textDetailFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.CLIP -> {
                    val bundle = ClipFragment.createBundle(arrayListOf(item), 0, true)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_clubDetailFragment_to_clipFragment,
                            bundle
                        )
                    )
                }
                else -> {
                }
            }
        }

        override fun onMoreClick(item: MemberPostItem) {
            moreDialog = MoreDialogFragment.newInstance(item, onMoreDialogListener).also {
                it.show(
                    requireActivity().supportFragmentManager,
                    MoreDialogFragment::class.java.simpleName
                )
            }
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = PictureDetailFragment.createBundle(item, 0)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_clubDetailFragment_to_pictureDetailFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 0)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_clubDetailFragment_to_textDetailFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.CLIP -> {
                    val bundle = ClipFragment.createBundle(arrayListOf(item), 0)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_clubDetailFragment_to_clipFragment,
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
                    R.id.action_clubDetailFragment_to_clipFragment,
                    bundle
                )
            )
        }

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {
            val bundle = ClipFragment.createBundle(ArrayList(item), position)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_clubDetailFragment_to_clipFragment,
                    bundle
                )
            )
        }

        override fun onChipClick(type: PostType, tag: String) {
            val item = SearchPostItem(type, tag)
            val bundle = SearchPostFragment.createBundle(item)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_clubDetailFragment_to_searchPostFragment,
                    bundle
                )
            )
        }

        override fun onAvatarClick() {
            // TODO:
            Timber.d("onAvatarClick nav to member post")
        }
    }

    private val onReportDialogListener = object : ReportDialogFragment.OnReportDialogListener {
        override fun onSend(item: BaseMemberPostItem, content: String) {
            if (TextUtils.isEmpty(content)) {
                GeneralUtils.showToast(requireContext(), getString(R.string.report_error))
            } else {
                reportDialog?.dismiss()
                when (item) {
                    is MemberPostItem -> viewModel.sendPostReport(item, content)
                }
            }
        }

        override fun onCancel() {
            reportDialog?.dismiss()
        }
    }

    private val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
        override fun onProblemReport(item: BaseMemberPostItem) {
            moreDialog?.dismiss()
            reportDialog = ReportDialogFragment.newInstance(item, onReportDialogListener).also {
                it.show(
                    requireActivity().supportFragmentManager,
                    ReportDialogFragment::class.java.simpleName
                )
            }
        }

        override fun onCancel() {
            moreDialog?.dismiss()
        }
    }

    private fun getPost(orderBy: OrderBy, update: ((PagedList<MemberPostItem>) -> Unit)) {
        viewModel.getMemberPosts(memberClubItem.tag, orderBy, update)
    }

    private fun getBitmap(id: String, update: ((String) -> Unit)) {
        viewModel.getBitmap(id, update)
    }

    private fun followMember(
        memberPostItem: MemberPostItem,
        isFollow: Boolean,
        update: (Boolean) -> Unit
    ) {
        viewModel.followMember(memberPostItem, isFollow, update)
    }

    private fun likePost(
        memberPostItem: MemberPostItem,
        isLike: Boolean,
        update: (Boolean, Int) -> Unit
    ) {
        viewModel.likePost(memberPostItem, isLike, update)
    }
}