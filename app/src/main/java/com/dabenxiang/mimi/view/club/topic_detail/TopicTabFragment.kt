package com.dabenxiang.mimi.view.club.topic_detail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.adapter.ClubTabAdapter
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_club_topic_v2.*
import kotlinx.android.synthetic.main.fragment_club_topic_v2.view.*
import timber.log.Timber

class TopicTabFragment : BaseFragment() {

    lateinit var tabLayoutMediator: TabLayoutMediator

    companion object {
        const val TAB_HOTTEST = 0
        const val TAB_LATEST = 1
        const val TAB_VIDEO = 2

        const val KEY_DATA = "data"

        fun createBundle(
                item: MemberClubItem
        ): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    val viewModel: TopicViewModel by viewModels()
    lateinit var memberClubItem:MemberClubItem

    private val topicItem: MemberClubItem by lazy {
        Timber.i("topicItem arguments =$arguments")
        (arguments?.getSerializable(KEY_DATA) as MemberClubItem)
    }

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
            TAB_HOTTEST to { TopicListFragment(topicItem, OrderBy.HOTTEST, topicItem.tag) },
            TAB_LATEST to { TopicListFragment(topicItem, OrderBy.NEWEST, topicItem.tag)  },
            TAB_VIDEO to {  TopicListFragment(topicItem, OrderBy.VIDEO, topicItem.tag)  }
    )

    override fun getLayoutId() = R.layout.fragment_club_topic_v2

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.adWidth = GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second

        viewModel.getClubInfo.observe(this, {
            when (it) {
                is ApiResult.Success -> {
                    setupUI(it.result)
                }
                is ApiResult.Error -> {
                    cl_no_data.visibility = View.VISIBLE
                    onApiError(it.throwable)
                }
            }
        })

        viewModel.followClubResult.observe(this,  {
            when (it) {
                is ApiResult.Success -> {
                    updateFollow()
                    viewModel.getMembersClub(topicItem.id)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getLayoutId(), container, false)
        view.view_pager.adapter = ClubTabAdapter(
                tabFragmentsCreators,
                childFragmentManager,
                lifecycle
        )
//        view.view_pager.offscreenPageLimit = 1
        val tabTitles = resources.getStringArray(R.array.club_hot_topic_tabs)
        tabLayoutMediator = TabLayoutMediator(view.tabs, view.view_pager) { tab, position ->
            tab.text = tabTitles[position]
        }
        tabLayoutMediator.attach()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ib_back.setOnClickListener {
            navigateTo(NavigateItem.Up)
        }

        tv_follow.setOnClickListener {
            if (this::memberClubItem.isInitialized) {
                checkStatus {
                    viewModel.followClub(
                            memberClubItem,
                            !memberClubItem.isFollow
                    )
                }
            }
        }

        viewModel.getMembersClub(topicItem.id)
    }

    private fun setupUI(item:MemberClubItem) {
        Timber.i("MemberClubItem =$topicItem")
        val isUpdateSetUp =this::memberClubItem.isInitialized

        memberClubItem = item
        memberClubItem?.let{item->
            tv_title.text = item.title
            tv_desc.text = item.description

            tv_desc.post {
                if (!isUpdateSetUp && tv_desc.lineCount > 1) {
                    val params = toolbar_layout.layoutParams
                    params.height = toolbar_layout.height +tv_desc.height/2
                    toolbar_layout.layoutParams = params
                }
            }
            
            tv_follow_count.text = item.followerCount.toString()
            tv_post_count.text = item.postCount.toString()
            updateFollow()
            val bitmap = LruCacheUtils.getLruCache(item.avatarAttachmentId.toString())

            bitmap.takeIf { it != null }?.also {
                Glide.with(requireContext()).load(bitmap).circleCrop().into(iv_avatar)
            } ?: run {
                viewModel.loadImage(item.avatarAttachmentId, iv_avatar, LoadImageType.AVATAR_CS)
            }
        }

        cl_no_data.visibility = GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tabLayoutMediator.isInitialized) tabLayoutMediator.detach()
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    private fun updateFollow() {
        tv_follow.visibility = View.VISIBLE
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
}