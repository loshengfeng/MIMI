package com.dabenxiang.mimi.view.search.post

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.*
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.club.post.ClubPicDetailFragment
import com.dabenxiang.mimi.view.club.post.ClubPicFragment
import com.dabenxiang.mimi.view.club.post.ClubPostPicFragment
import com.dabenxiang.mimi.view.club.post.ClubTextFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_general_video.*
import kotlinx.android.synthetic.main.fragment_search_post.*
import kotlinx.android.synthetic.main.fragment_search_post.tv_search
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class SearchPostFragment : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"
        fun createBundle(item: SearchPostItem): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    private val viewModel: SearchPostViewModel by viewModels()

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId() = R.layout.fragment_search_post

    private var searchType: PostType = PostType.TEXT_IMAGE_VIDEO
    private var searchTag: String? = null
    private var searchText: String? = null
    private var searchOrderBy: StatisticsOrderType = StatisticsOrderType.LATEST
    private var searchKeyword: String = ""

    private val adapter: SearchPostAdapter by lazy {
        SearchPostAdapter(requireActivity(), postListener, memberPostFuncItem, attachmentListener)
    }

    private val postListener = object : MyPostListener {

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            checkStatus { viewModel.likePost(item, position, isLike) }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            //todo
        }

        override fun onFavoriteClick(
            item: MemberPostItem,
            position: Int,
            isFavorite: Boolean,
            type: AttachmentType
        ) {
            checkStatus { viewModel.favoritePost(item, position, isFavorite) }
        }

        override fun onFollowClick(items: List<MemberPostItem>, position: Int, isFollow: Boolean) {
        }

        override fun onMoreClick(item: MemberPostItem, position: Int) {
            onMoreClick(item, position) {
                //todo
                it as MemberPostItem
            }
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (item.type) {
                PostType.IMAGE -> {
                    val bundle = ClubPicFragment.createBundle(item)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_searchPostFragment_to_clubPicFragment,
                            bundle
                        )
                    )
                }
                PostType.TEXT -> {
                    val bundle = ClubTextFragment.createBundle(item)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_searchPostFragment_to_clubTextFragment,
                            bundle
                        )
                    )
                }
                else -> {
                }
            }
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {}

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {}

        override fun onChipClick(type: PostType, tag: String) {
            search(tag = tag)
        }
    }

    private val attachmentListener = object : AttachmentListener {
        override fun onGetAttachment(id: Long?, view: ImageView, type: LoadImageType) {
            viewModel.loadImage(id, view, type)
        }

        override fun onGetAttachment(id: String, parentPosition: Int, position: Int) {
        }
    }

    override fun setupFirstTime() {
        (arguments?.getSerializable(KEY_DATA) as SearchPostItem).also {
            searchType = it.type
            searchTag = it.tag
            searchText = it.keyword
            searchOrderBy = it.orderBy ?: StatisticsOrderType.LATEST
        }

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()

        if (TextUtils.isEmpty(searchTag) && TextUtils.isEmpty(searchText)) {
            layout_search_history.visibility = View.VISIBLE
            layout_search_text.visibility = View.GONE
            getSearchHistory()
        } else {
            layout_search_history.visibility = View.GONE
            layout_search_text.visibility = View.VISIBLE
        }

        adapter.addLoadStateListener(loadStateListener)
        recycler_search_result.layoutManager = LinearLayoutManager(requireContext())
        recycler_search_result.adapter = adapter

        searchTag?.also { search(tag = searchTag) }
        searchText?.also { search(keyword = searchText) }
    }

    override fun setupObservers() {
        viewModel.searchTotalCount.observe(viewLifecycleOwner, Observer { count ->
            setSearchResultText(count)
        })

        viewModel.likePostResult.observe(this, {
            when (it) {
                is ApiResult.Success -> {
                    it.result?.let { position ->
                        adapter.notifyItemChanged(position)
                    }
                }

                else -> {
                    onApiError(Exception("Unknown Error!"))
                }
            }
        })

        viewModel.favoriteResult.observe(this, {
            when (it) {
                is ApiResult.Success -> {
                    it.result?.let { position ->
                        adapter.notifyItemChanged(position)
                    }
                }
                else -> {
                    onApiError(Exception("Unknown Error!"))
                }
            }
        })

        mainViewModel?.deletePostResult?.observe(this, {
            when (it) {
                is ApiResult.Success -> {
                    adapter.removedPosList.add(it.result)
                    adapter.notifyItemChanged(it.result)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

    }

    override fun setupListeners() {
        ib_back.setOnClickListener {
            findNavController().navigateUp()
        }

        iv_clean.setOnClickListener {
            search_bar.setText("")
        }

        iv_clear_search_text.setOnClickListener {
            chip_group_search_text.removeAllViews()
            viewModel.clearSearchHistory()
        }

        tv_search.setOnClickListener {
            search(keyword = search_bar.text.toString())
        }

        search_bar.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    search(keyword = search_bar.text.toString())
                    true
                }
                else -> false
            }
        }

        search_bar.addTextChangedListener {
            if (it.toString() == "" && !TextUtils.isEmpty(searchTag)) {
                layout_search_history.visibility = View.GONE
                layout_search_text.visibility = View.VISIBLE
            } else if (it.toString() == "") {
                layout_search_history.visibility = View.VISIBLE
                layout_search_text.visibility = View.GONE
                getSearchHistory()
            }
        }
    }

    private val loadStateListener = { loadStatus: CombinedLoadStates ->
        when (loadStatus.refresh) {
            is LoadState.Error -> {
                Timber.e("Refresh Error: ${(loadStatus.refresh as LoadState.Error).error.localizedMessage}")
                progressHUD.dismiss()
                onApiError((loadStatus.refresh as LoadState.Error).error)
            }
            is LoadState.Loading -> {
                progressHUD.show()
            }
            is LoadState.NotLoading -> {
                progressHUD.dismiss()
            }
        }

        when (loadStatus.append) {
            is LoadState.Error -> {
                Timber.e("Append Error:${(loadStatus.append as LoadState.Error).error.localizedMessage}")
                onApiError((loadStatus.refresh as LoadState.Error).error)
            }
            is LoadState.Loading -> {
                Timber.d("Append Loading endOfPaginationReached:${(loadStatus.append as LoadState.Loading).endOfPaginationReached}")
            }
            is LoadState.NotLoading -> {
                Timber.d("Append NotLoading endOfPaginationReached:${(loadStatus.append as LoadState.NotLoading).endOfPaginationReached}")
            }
        }
    }

    private fun search(keyword: String? = null, tag: String? = null) {
        GeneralUtils.hideKeyboard(requireActivity())
        if (TextUtils.isEmpty(keyword) && TextUtils.isEmpty(tag)) {
            Timber.d("no search rule!")
            return
        }
        layout_search_history.visibility = View.GONE
        layout_search_text.visibility = View.INVISIBLE
        keyword?.let {
            viewModel.updateSearchHistory(keyword)
            searchKeyword = keyword
        }
        tag?.let {
            searchKeyword = tag
        }

        when (searchType) {
            PostType.FOLLOWED -> searchPostFollow(keyword, tag)
            PostType.TEXT_IMAGE_VIDEO,
            PostType.TEXT,
            PostType.IMAGE,
            PostType.VIDEO -> searchPostAll(keyword, tag)
            else -> {
            }
        }
    }

    private fun setSearchResultText(
        count: Long = 0
    ) {
        val word = SpannableStringBuilder()
            .append(getString(R.string.search_keyword_1))
            .append(searchKeyword)
            .append(getString(R.string.search_keyword_2))
            .append(" ")
            .append(count.toString())
            .append(" ")
            .append(getString(R.string.search_keyword_3))

        val typeText = when (searchType) {
            PostType.TEXT -> getString(R.string.club_tab_novel)
            PostType.IMAGE -> getString(R.string.club_tab_picture)
            PostType.VIDEO -> getString(R.string.club_tab_clip)
            else -> ""
        }
        word.append(typeText)

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

        tv_search_text.text = word
        layout_search_text.visibility = View.VISIBLE
    }

    private val memberPostFuncItem by lazy {
        MemberPostFuncItem(
            {},
            { id, view, type -> },
            { item, items, isFollow, func -> },
            { item, isLike, func -> },
            { item, isFavorite, func -> }
        )
    }

    private fun getSearchHistory() {
        chip_group_search_text.removeAllViews()
        val searchHistories = viewModel.getSearchHistory().asReversed()
        searchHistories.forEach { text ->
            val chip = LayoutInflater.from(chip_group_search_text.context)
                .inflate(R.layout.chip_item, chip_group_search_text, false) as Chip
            chip.text = text
            chip.ellipsize = TextUtils.TruncateAt.END
            chip.setTextColor(requireContext().getColor(R.color.color_black_1_50))
            chip.setOnClickListener {
                search_bar.setText(text)
                search(text)
            }
            chip_group_search_text.addView(chip)
        }
    }

    private fun searchPostFollow(
        keyword: String? = null,
        tag: String? = null
    ) {
        lifecycleScope.launch {
            adapter.submitData(PagingData.empty())
            viewModel.getSearchPostFollowResult(keyword, tag)
                .collectLatest { adapter.submitData(it) }
        }
    }

    private fun searchPostAll(
        keyword: String? = null,
        tag: String? = null
    ) {
        lifecycleScope.launch {
            adapter.submitData(PagingData.empty())
            viewModel.getSearchPostAllResult(searchType, keyword, tag, searchOrderBy)
                .collectLatest { adapter.submitData(it) }
        }
    }

}