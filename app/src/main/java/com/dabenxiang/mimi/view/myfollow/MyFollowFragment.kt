package com.dabenxiang.mimi.view.myfollow

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import com.dabenxiang.mimi.view.adapter.ClubFollowAdapter
import com.dabenxiang.mimi.view.adapter.FavoriteTabAdapter
import com.dabenxiang.mimi.view.adapter.MemberFollowAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clubdetail.ClubDetailFragment
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.dabenxiang.mimi.view.favroite.FavoriteFragment
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.fragment_my_follow.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import timber.log.Timber

class MyFollowFragment : BaseFragment() {

    private val viewModel: MyFollowViewModel by viewModels()

    companion object {
        const val NO_DATA = 0
        const val TYPE_MEMBER = 0
        const val TYPE_CLUB = 1
        var lastTab = TYPE_MEMBER
    }

    private val primaryAdapter by lazy {
        FavoriteTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                setTabPosition(index)
                viewModel.initData(lastTab)
            }
        })
    }

    private fun setTabPosition(index: Int) {
        lastTab = index
        primaryAdapter.setLastSelectedIndex(lastTab)
    }

    private val clubFollowAdapter by lazy { ClubFollowAdapter(clubFollowListener) }
    private val clubFollowListener = object : ClubFollowAdapter.EventListener {
        override fun onDetail(item: ClubFollowItem) {
            viewModel.getClub(item.tag)
        }

        override fun onGetAttachment(id: String, position: Int) {
            viewModel.getAttachment(id, position)
        }

        override fun onCancelFollow(clubId: Long) {
            viewModel.cancelFollowClub(clubId)
        }
    }

    private val memberFollowAdapter by lazy { MemberFollowAdapter(memberFollowListener) }
    private val memberFollowListener = object : MemberFollowAdapter.EventListener {
        override fun onDetail(item: MemberFollowItem) {
            //todo
        }

        override fun onGetAttachment(id: String, position: Int) {
            viewModel.getAttachment(id, position)
        }

        override fun onCancelFollow(userId: Long) {
            viewModel.cancelFollowMember(userId)
        }
    }

    private var interactionListener: InteractionListener? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback {
            navigateTo(NavigateItem.Up)
        }
    }

    override fun onResume() {
        super.onResume()
        initSettings()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            interactionListener = context as InteractionListener
        } catch (e: ClassCastException) {
            Timber.e("AdultHomeFragment interaction listener can't cast")
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_my_follow
    }

    override fun setupObservers() {
        viewModel.showProgress.observe(viewLifecycleOwner, Observer {
            layout_refresh.isRefreshing = it
        })

        viewModel.clubCount.observe(viewLifecycleOwner, Observer {
            refreshUi(TYPE_CLUB, it)
        })

        viewModel.memberCount.observe(viewLifecycleOwner, Observer {
            refreshUi(TYPE_MEMBER, it)
        })

        viewModel.clubList.observe(viewLifecycleOwner, Observer {
            rv_content.adapter = clubFollowAdapter
            clubFollowAdapter.submitList(it)
        })

        viewModel.memberList.observe(viewLifecycleOwner, Observer {
            rv_content.adapter = memberFollowAdapter
            memberFollowAdapter.submitList(it)
        })

        viewModel.attachmentResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    val attachmentItem = it.result
                    LruCacheUtils.putLruCache(attachmentItem.id!!, attachmentItem.bitmap!!)
                    when (lastTab) {
                        TYPE_MEMBER -> memberFollowAdapter.update(attachmentItem.position ?: 0)
                        TYPE_CLUB -> clubFollowAdapter.update(attachmentItem.position ?: 0)
                    }
                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })

        viewModel.clubDetail.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    val bundle = ClubDetailFragment.createBundle(it.result[0])
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_myFollowFragment_to_clubDetailFragment,
                            bundle
                        )
                    )
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.cleanResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> layout_refresh.isRefreshing = true
                is ApiResult.Loaded -> layout_refresh.isRefreshing = false
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })
    }

    private val onCleanDialogListener = object : OnCleanDialogListener {
        override fun onClean() {
            if (lastTab == TYPE_MEMBER) {
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
                    if (lastTab == TYPE_MEMBER)
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

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.initData(lastTab)
        }
    }

    override fun initSettings() {
        super.initSettings()

        rv_primary.adapter = primaryAdapter

        val primaryList = listOf(
            getString(R.string.follow_people),
            getString(R.string.follow_circle)
        )

        primaryAdapter.submitList(primaryList, FavoriteFragment.lastPrimaryIndex)

        tv_clean.visibility = View.VISIBLE
        tv_title.setText(R.string.follow_title)
        tv_all.text = getString(R.string.follow_clubs_total_num, "0")

        rv_content.adapter = memberFollowAdapter

        viewModel.initData(lastTab)
    }

    private fun refreshUi(witch: Int, size: Int) {
        rv_content.visibility = when (size) {
            NO_DATA -> View.GONE
            else -> View.VISIBLE
        }

        item_no_data.visibility = when (size) {
            NO_DATA -> View.VISIBLE
            else -> View.GONE
        }

        tv_clean.isEnabled = size != NO_DATA

        tv_all.text =
            if (witch == TYPE_MEMBER)
                getString(R.string.follow_members_total_num, size.toString())
            else
                getString(R.string.follow_clubs_total_num, size.toString())
    }
}
