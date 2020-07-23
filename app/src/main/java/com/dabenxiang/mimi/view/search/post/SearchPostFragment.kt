package com.dabenxiang.mimi.view.search.post

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.serializable.SearchPostItem
import com.dabenxiang.mimi.view.adapter.MemberPostPagedAdapter
import com.dabenxiang.mimi.view.adapter.viewHolder.ClipPostHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.PicturePostHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.TextPostHolder
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.dialog.ReportDialogFragment
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.fragment_search_post.*
import timber.log.Timber

class SearchPostFragment : BaseFragment() {

    companion object {
        private const val KEY_DATA = "data"
        fun createBundle(item: SearchPostItem): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    private val viewModel: SearchPostViewModel by viewModels()

    private var moreDialog: MoreDialogFragment? = null
    private var reportDialog: ReportDialogFragment? = null

    private var currentPostType: PostType = PostType.TEXT
    private var mTag: String = ""
    private var searchText: String = ""
    private var keyword: String = ""

    private var isPostFollow: Boolean = false

    private var adapter: MemberPostPagedAdapter? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (arguments?.getSerializable(KEY_DATA) as SearchPostItem).also {
            currentPostType = it.type
            isPostFollow = it.isPostFollow
            mTag = it.tag
            searchText = it.searchText
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = MemberPostPagedAdapter(
            requireContext(), adultListener, attachmentListener, mTag
        )
        recyclerview_content.layoutManager = LinearLayoutManager(requireContext())
        recyclerview_content.adapter = adapter

        if (!TextUtils.isEmpty(mTag)) {
            viewModel.getSearchPostsByTag(currentPostType, mTag, isPostFollow)
        }

        if (!TextUtils.isEmpty(searchText)) {
            viewModel.getSearchPostsByKeyword(currentPostType, searchText, isPostFollow)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_search_post
    }

    override fun setupObservers() {
        viewModel.postReportResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Empty -> {
                    GeneralUtils.showToast(requireContext(), getString(R.string.report_success))
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.attachmentResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    val attachmentItem = it.result
                    LruCacheUtils.putLruCache(attachmentItem.id!!, attachmentItem.bitmap!!)
                    when (val holder =
                        adapter?.viewHolderMap?.get(attachmentItem.parentPosition)) {
                        is PicturePostHolder -> {
                            if (holder.pictureRecycler.tag == attachmentItem.parentPosition) {
                                adapter?.updateInternalItem(holder)
                            }
                        }
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.followPostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    when (adapter?.viewHolderMap?.get(it.result)) {
                        is ClipPostHolder,
                        is PicturePostHolder,
                        is TextPostHolder -> {
                            adapter?.notifyItemChanged(
                                it.result,
                                MemberPostPagedAdapter.PAYLOAD_UPDATE_LIKE_AND_FOLLOW_UI
                            )
                        }
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.likePostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    when (adapter?.viewHolderMap?.get(it.result)) {
                        is ClipPostHolder,
                        is PicturePostHolder,
                        is TextPostHolder -> {
                            adapter?.notifyItemChanged(
                                it.result,
                                MemberPostPagedAdapter.PAYLOAD_UPDATE_LIKE_AND_FOLLOW_UI
                            )
                        }
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.searchPostItemByTagListResult.observe(viewLifecycleOwner, Observer {
            adapter?.submitList(it)
        })

        viewModel.searchPostItemByKeywordListResult.observe(viewLifecycleOwner, Observer {
            adapter?.submitList(it)
        })

        viewModel.searchTotalCount.observe(viewLifecycleOwner, Observer { count ->
            txt_result.text = getSearchText(currentPostType, keyword, count, isPostFollow)
        })
    }

    override fun setupListeners() {
        ib_back.setOnClickListener {
            findNavController().navigateUp()
        }

        iv_clean.setOnClickListener {
            edit_search.setText("")
        }

        tv_search.setOnClickListener {
            updateData(true)
            GeneralUtils.hideKeyboard(requireActivity())
            viewModel.getSearchPostsByKeyword(
                currentPostType,
                edit_search.text.toString(),
                isPostFollow
            )
        }
    }

    private fun getSearchText(
        type: PostType,
        keyword: String,
        count: Long = 0,
        isPostFollow: Boolean
    ): SpannableStringBuilder {

        val word = SpannableStringBuilder()
            .append(getString(R.string.search_keyword_1))
            .append(keyword)
            .append(getString(R.string.search_keyword_2))
            .append(" ")
            .append(count.toString())
            .append(" ")
            .append(getString(R.string.search_keyword_3))

        if (!isPostFollow) {
            val typeText = when (type) {
                PostType.TEXT -> getString(R.string.search_type_text)
                PostType.IMAGE -> getString(R.string.search_type_picture)
                PostType.VIDEO -> getString(R.string.search_type_clip)
                else -> ""
            }
            word.append(typeText)
        }

        word.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_red_1
                )
            ), word.indexOf("\"") + 1,
            word.lastIndexOf("\""),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        word.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_red_1
                )
            ),
            word.indexOf(getString(R.string.search_text_1)) + 1,
            word.lastIndexOf(getString(R.string.search_text_2)),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return word
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

    private val attachmentListener = object : AttachmentListener {
        override fun onGetAttachment(id: String, position: Int, type: AttachmentType) {

        }

        override fun onGetAttachment(id: String, parentPosition: Int, position: Int) {
            viewModel.getAttachment(id, parentPosition, position)
        }
    }

    private val adultListener = object : AdultListener {
        override fun onFollowPostClick(item: MemberPostItem, position: Int, isFollow: Boolean) {
            viewModel.followPost(item, position, isFollow)
        }

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            viewModel.likePost(item, position, isLike)
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = PictureDetailFragment.createBundle(item, 1)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_searchPostFragment_to_pictureDetailFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 1)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_searchPostFragment_to_textDetailFragment,
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
                            R.id.action_searchPostFragment_to_pictureDetailFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 0)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_searchPostFragment_to_textDetailFragment,
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
                    R.id.action_searchPostFragment_to_clipFragment,
                    bundle
                )
            )
        }

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {
            // TODO: Sion Wang
            val bundle = ClipFragment.createBundle(ArrayList(item), position)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_searchPostFragment_to_clipFragment,
                    bundle
                )
            )
        }

        override fun onChipClick(type: PostType, tag: String) {
            updateData(false)
            viewModel.getSearchPostsByTag(type, tag, isPostFollow)
        }
    }

    private fun updateData(isSearchText: Boolean) {
        if (isSearchText) {
            searchText = edit_search.text.toString()
            mTag = ""
            keyword = edit_search.text.toString()
            adapter?.setupTag(mTag)
        } else {
            searchText = ""
            mTag = tag.toString()
            keyword = tag.toString()
            adapter?.setupTag(mTag)
        }
    }
}