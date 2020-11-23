package com.dabenxiang.mimi.view.club

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.MemberPostPagedAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import kotlinx.android.synthetic.main.fragment_club_text.*

class ClubTextFragment : BaseFragment() {

    private val viewModel: ClubViewModel by viewModels()

    override fun getLayoutId() = R.layout.fragment_club_text

    override fun setupObservers() {
        viewModel.textPostItemListResult.observe(this, Observer {
            textPostPagedAdapter.submitList(it)
        })
    }

    override fun setupListeners() {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = textPostPagedAdapter
        viewModel.getTextPosts()
    }

    private val textPostPagedAdapter by lazy {
        MemberPostPagedAdapter(requireActivity(), adultListener, "", memberPostFuncItem)
    }

    private val adultListener = object : AdultListener {
        override fun onFollowPostClick(item: MemberPostItem, position: Int, isFollow: Boolean) {
            //replace by closure
        }

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            //replace by closure
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {

        }

        override fun onMoreClick(item: MemberPostItem, items: List<MemberPostItem>) {
            onMoreClick(
                item,
                ArrayList(items),
                onEdit = {
                    val bundle = Bundle()
                    bundle.putBoolean(MyPostFragment.EDIT, true)
                    bundle.putString(BasePostFragment.PAGE, BasePostFragment.ADULT)
                    bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)

                    it as MemberPostItem
                    when (item.type) {
                        PostType.TEXT -> {
                            findNavController().navigate(
                                R.id.action_adultHomeFragment_to_postArticleFragment,
                                bundle
                            )
                        }
                        PostType.IMAGE -> {
                            findNavController().navigate(
                                R.id.action_adultHomeFragment_to_postPicFragment,
                                bundle
                            )
                        }
                        PostType.VIDEO -> {
                            findNavController().navigate(
                                R.id.action_adultHomeFragment_to_postVideoFragment,
                                bundle
                            )
                        }
                    }
                }
            )
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_clubTabFragment_to_clubTextDetailFragment)
            )
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {
            val bundle =
                ClipFragment.createBundle(ArrayList(item.subList(1, item.size)), position - 1)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_adultHomeFragment_to_clipFragment,
                    bundle
                )
            )
        }

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {
            checkStatus {
                val bundle = ClipFragment.createBundle(ArrayList(item), position, true)
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_adultHomeFragment_to_clipFragment,
                        bundle
                    )
                )
            }
        }

        override fun onChipClick(type: PostType, tag: String) {

        }

        override fun onAvatarClick(userId: Long, name: String) {
            val bundle = MyPostFragment.createBundle(
                userId, name,
                isAdult = true,
                isAdultTheme = true
            )
            navigateTo(NavigateItem.Destination(R.id.action_to_myPostFragment, bundle))
        }
    }

    private val memberPostFuncItem by lazy {
        MemberPostFuncItem(
            {},
            { id, view, type -> viewModel.loadImage(id, view, type) }
        )
    }

}