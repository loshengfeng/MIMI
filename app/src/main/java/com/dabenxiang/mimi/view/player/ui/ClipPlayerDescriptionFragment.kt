package com.dabenxiang.mimi.view.player.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.ReportDialogFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.item_ad.*
import kotlinx.android.synthetic.main.item_clip_info.*
import kotlinx.android.synthetic.main.item_comment_interactive.*
import kotlinx.android.synthetic.main.item_video_tag.*
import java.util.*

class ClipPlayerDescriptionFragment : BaseFragment() {

    override fun getLayoutId() = R.layout.fragment_clip_description

    private val viewModel: ClipPlayerViewModel by viewModels({requireParentFragment()})

    private val clipViewModel = ClipPlayerDescriptionViewModel()

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override val isStatusBarDark: Boolean = true

    private lateinit var detailItem: MemberPostItem

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAdContent()
    }

    override fun setupObservers() {
        super.setupObservers()
        mainViewModel?.deletePostResult?.observe(viewLifecycleOwner){
            when (it) {
                is ApiResult.Success -> {
                    navigateTo(NavigateItem.Up)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }

        viewModel.memberPostContentSource.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Success -> {
                    parsingPostContent(it.result)
                }
            }
        }

        clipViewModel.getAdResult.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Success -> {
                    Glide.with(this)
                        .load(it.result.href)
                        .into(iv_ad)

                    iv_ad.setOnClickListener { view ->
                        GeneralUtils.openWebView(requireContext(), it.result.target)
                    }
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }

        clipViewModel.updateFollow.observe(viewLifecycleOwner) {
            updataFollow(tv_follow.text == getText(R.string.follow))
        }

        clipViewModel.likeResult.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Loading -> progressHUD.show()
                is ApiResult.Loaded -> progressHUD.dismiss()
                is ApiResult.Success -> {
                    detailItem = it.result
                    setUILike()
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }

        clipViewModel.favoriteResult.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Loading -> progressHUD.show()
                is ApiResult.Loaded -> progressHUD.dismiss()
                is ApiResult.Success -> {
                    detailItem = it.result
                    setUIFavorite()
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }
    }

    private fun setInteractiveListener() {
        imgLike.setOnClickListener {
            checkStatus { clipViewModel.likePost(detailItem, LikeType.LIKE)}
        }
        imgDislike.setOnClickListener {
            checkStatus { clipViewModel.likePost(detailItem, LikeType.DISLIKE)}
        }
        imgFavorite.setOnClickListener {
            checkStatus { clipViewModel.favoritePost(detailItem)}
        }
    }

    private fun setMoreListener() {
        imgMore.setOnClickListener {
            onMoreClick(detailItem, -1, deducted = detailItem.deducted) {
                it as MemberPostItem

                val bundle = Bundle()
                detailItem.id
                bundle.putBoolean(MyPostFragment.EDIT, true)
                bundle.putString(BasePostFragment.PAGE, BasePostFragment.VIDEO)
                bundle.putSerializable(MyPostFragment.MEMBER_DATA, detailItem)

                when (it.type) {
                    PostType.TEXT -> {
                        findNavController().navigate(
                                R.id.action_to_postArticleFragment,
                                bundle
                        )
                    }
                    PostType.IMAGE -> {
                        findNavController().navigate(
                                R.id.action_to_postPicFragment,
                                bundle
                        )
                    }
                    PostType.VIDEO -> {
                        findNavController().navigate(
                                R.id.action_to_postVideoFragment,
                                bundle
                        )
                    }
                }
            }
        }
    }

    private fun parsingPostContent(postItem: MemberPostItem) {
        detailItem = postItem
        viewModel.loadImage(postItem.avatarAttachmentId, clip_icon, LoadImageType.AVATAR_CS)
        clip_name.text = postItem.postFriendlyName

        tv_follow.visibility =
            if (viewModel.accountManager.getProfile().userId == postItem.creatorId) View.GONE else View.VISIBLE

        updataFollow(postItem.isFollow)

        clip_update_time.setTextColor(App.self.getColor(R.color.color_black_1_50))
        clip_update_time.text = GeneralUtils.getTimeDiff(postItem.creationDate, Date())

        clip_title.setTextColor(App.self.getColor(R.color.color_black_1))

        clip_title.text = postItem.title

        setupChipGroup(postItem.tags)

        tv_follow.setOnClickListener {
            checkStatus { clipViewModel.followPost(postItem.creatorId, tv_follow.text == getText(R.string.followed)) }
        }

        clip_icon.setOnClickListener {
            clipViewModel.followPost(postItem.creatorId, tv_follow.text == getText(R.string.followed))
        }

        clip_name.setOnClickListener {
            val bundle = MyPostFragment.createBundle(
                postItem.creatorId, (it as TextView).text.toString(),
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

        setUILike()
        setUIFavorite()
        setInteractiveListener()
        setMoreListener()
    }

    private fun setUILike() {
        imgLike.setImageResource(if (detailItem.likeType == LikeType.LIKE) R.drawable.ico_nice_s else R.drawable.ico_nice)
        txtLikeCount.text =
            String.format(getString(R.string.club_like_count), detailItem.likeCount)
        txtLikeCount.setTextColor(
            resources.getColor(
                if (detailItem.likeType == LikeType.LIKE) R.color.color_red_1 else R.color.color_black_1_60,
                null
            )
        )

        imgDislike.setImageResource(if (detailItem.likeType == LikeType.DISLIKE) R.drawable.ico_bad_s else R.drawable.ico_bad)
        txtDisLikeCount.text =
            String.format(getString(R.string.club_dislike_count), detailItem.dislikeCount)
        txtDisLikeCount.setTextColor(
            resources.getColor(
                if (detailItem.likeType == LikeType.DISLIKE) R.color.color_red_1 else R.color.color_black_1_60,
                null
            )
        )

    }

    private fun setUIFavorite() {
        imgFavorite.setImageResource(if (detailItem.isFavorite) R.drawable.btn_favorite_white_s else R.drawable.btn_favorite_white_n)
        txtFavorite.setTextColor(
            resources.getColor(
                if (detailItem.isFavorite) R.color.color_red_1 else R.color.color_black_1_60,
                null
            )
        )
    }

    private fun updataFollow(isFollow: Boolean) {
        tv_follow.setText(if (isFollow) R.string.followed else R.string.follow)
        tv_follow.setBackgroundResource(if (isFollow) R.drawable.bg_white_1_stroke_radius_16 else R.drawable.bg_red_1_stroke_radius_16)
        tv_follow.setTextColor(App.self.getColor(if (isFollow) R.color.color_black_1_60 else R.color.color_red_1))
    }

    private fun setupChipGroup(list: List<String>?) {
        tag_group.removeAllViews()

        if (list == null) {
            return
        }

        list.indices.mapNotNull {
            list[it]
        }.forEach addChipItem@{
            if (tag_group.size == 20) {
                return@addChipItem
            }

            val chip = layoutInflater.inflate(
                R.layout.chip_item,
                tag_group,
                false
            ) as Chip
            chip.text = it

            chip.setTextColor(requireContext().getColor(R.color.color_black_1_50))

            chip.setOnClickListener {
                searchVideo(chip.text.toString())
            }

            tag_group.addView(chip)
        }
    }

    private fun searchVideo(tag: String) {
        val bundle = SearchPostFragment.createBundle(
            SearchPostItem(PostType.VIDEO, tag = tag)
        )
        bundle.putBoolean(PlayerFragment.KEY_IS_FROM_PLAYER, true)
        findNavController().navigate(
            R.id.action_to_searchPostFragment,
            bundle
        )
    }

    private fun getAdContent() {
        // for ui init
        val adWidth = GeneralUtils.getAdSize(requireActivity()).first
        val adHeight = GeneralUtils.getAdSize(requireActivity()).second
        clipViewModel.getAd(adWidth, adHeight)
    }
}