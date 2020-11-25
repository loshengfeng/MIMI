package com.dabenxiang.mimi.view.club.latest

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.paging.PagingData
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.club.ClubTabFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_club_latest.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class ClubLatestFragment : BaseFragment() {

    private val viewModel: ClubLatestModel by viewModels()
    private val adapter by lazy {
        ClubLatestAdapter(requireActivity(), postListener, "", memberPostFuncItem)
    }

    override fun getLayoutId() = R.layout.fragment_club_latest

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.i("ClubLatestFragment onAttach")
        viewModel.clubCount.observe(this, Observer {

        })

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.adapter = adapter
        getData()
    }

    private val memberPostFuncItem by lazy {
        MemberPostFuncItem(
                {},
                { id, view, type ->  },
                { item, items, isFollow, func ->  },
                { item, isLike, func ->  },
                { item, isFavorite, func -> }
        )
    }

    private val postListener = object : AdultListener {
        override fun onFollowPostClick(item: MemberPostItem, position: Int, isFollow: Boolean) {
            //replace by closure
        }

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            //replace by closure
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
        }

        override fun onMoreClick(item: MemberPostItem, items: List<MemberPostItem>) {
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
//                    val bundle = PictureDetailFragment.createBundle(item, 0)
//                    navigationToPicture(bundle)
                }
                AdultTabType.TEXT -> {
//                    val bundle = TextDetailFragment.createBundle(item, 0)
//                    navigationToText(bundle)
                }
                AdultTabType.CLIP -> {
//                    val bundle = ClipFragment.createBundle(arrayListOf(item), 0)
//                    navigationToClip(bundle)
                }
                else -> {
                }
            }
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {}

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {}

        override fun onChipClick(type: PostType, tag: String) {}

        override fun onAvatarClick(userId: Long, name: String) {}
    }

    private fun getData() {
        CoroutineScope(Dispatchers.IO).launch {
            adapter.submitData(PagingData.empty())
            viewModel.getPostItemList(ClubTabFragment.TAB_LATEST)
                    .collectLatest {
                        adapter.submitData(it)
                    }
        }
    }

}
