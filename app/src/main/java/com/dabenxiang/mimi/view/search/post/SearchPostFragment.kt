package com.dabenxiang.mimi.view.search.post

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
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
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.pic.ClubPicFragment
import com.dabenxiang.mimi.view.club.text.ClubTextFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment.Companion.MEMBER_DATA
import com.dabenxiang.mimi.view.pagingfooter.withMimiLoadStateFooter
import com.dabenxiang.mimi.view.player.ui.ClipPlayerFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.search.post.SearchPostAdapter.Companion.UPDATE_FAVORITE
import com.dabenxiang.mimi.view.search.post.SearchPostAdapter.Companion.UPDATE_LIKE
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_search_post.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
        SearchPostAdapter(
            requireActivity(),
            postListener,
            memberPostFuncItem,
            { searchText ?: "" },
            { searchTag ?: "" })
    }

    private val postListener = object : MyPostListener {

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            checkStatus { viewModel.likePost(item, position, isLike) }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
                when (item.type) {
                    PostType.IMAGE -> {
                        val bundle = ClubPicFragment.createBundle(item, 1)
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_to_clubPicFragment,
                                bundle
                            )
                        )
                    }
                    PostType.TEXT -> {
                        val bundle = ClubTextFragment.createBundle(item, 1)
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_to_clubTextFragment,
                                bundle
                            )
                        )
                    }
                    PostType.VIDEO -> {
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

        override fun onFavoriteClick(
            item: MemberPostItem,
            position: Int,
            isFavorite: Boolean,
            type: AttachmentType
        ) {
            checkStatus { viewModel.favoritePost(item, position, isFavorite) }
        }

        override fun onFollowClick(
            items: List<MemberPostItem>,
            position: Int,
            isFollow: Boolean
        ) {
        }

        override fun onAvatarClick(userId: Long, name: String) {
            val bundle = MyPostFragment.createBundle(
                userId, name,
                isAdult = true,
                isAdultTheme = true
            )
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_searchPostFragment_to_myPostFragment,
                    bundle
                )
            )
        }

        override fun onMoreClick(item: MemberPostItem, position: Int) {
            onMoreClick(item, position) {
                it as MemberPostItem

                val searchPostItem = SearchPostItem(searchType, searchOrderBy, searchTag, searchKeyword)
                val bundle = Bundle()
                item.id
                bundle.putBoolean(MyPostFragment.EDIT, true)
                bundle.putString(BasePostFragment.PAGE, BasePostFragment.SEARCH)
                bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
                bundle.putSerializable(KEY_DATA, searchPostItem)

                when(it.type) {
                    PostType.TEXT -> {
                        findNavController().navigate(
                            R.id.action_searchPostFragment_to_postArticleFragment,
                            bundle
                        )
                    }
                    PostType.IMAGE -> {
                        findNavController().navigate(
                            R.id.action_searchPostFragment_to_postPicFragment,
                            bundle
                        )
                    }
                    PostType.VIDEO -> {
                        findNavController().navigate(
                            R.id.action_searchPostFragment_to_postVideoFragment,
                            bundle
                        )
                    }
                }
            }
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (item.type) {
                PostType.IMAGE -> {
                    val bundle = ClubPicFragment.createBundle(item)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_to_clubPicFragment,
                            bundle
                        )
                    )
                }
                PostType.TEXT -> {
                    val bundle = ClubTextFragment.createBundle(item)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_to_clubTextFragment,
                            bundle
                        )
                    )
                }
                PostType.VIDEO -> {
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

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {}

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {}

        override fun onChipClick(type: PostType, tag: String) {
            layout_search_text.visibility = View.GONE
            search_bar.setText(tag)
            search(tag = tag)
            GeneralUtils.hideKeyboard(requireActivity())
//            search_bar.clearFocus()
        }
    }

    override fun setupFirstTime() {
        (arguments?.getSerializable(KEY_DATA) as SearchPostItem).also {
            searchType = it.type
            searchTag = it.tag
            searchText = it.keyword
            searchOrderBy = it.orderBy ?: StatisticsOrderType.LATEST
        }

        viewModel.adWidth =
            ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()

        if (!TextUtils.isEmpty(searchTag)) {
            layout_search_history.visibility = View.GONE
            search_bar.setText(searchTag)
            search(tag = searchTag)
//            search_bar.clearFocus()
        } else {
            getSearchHistory()
            GeneralUtils.showKeyboard(requireContext())
//            search_bar.requestFocus()
        }
        layout_search_text.visibility = View.GONE

        adapter.addLoadStateListener(loadStateListener)
        recycler_search_result.layoutManager = LinearLayoutManager(requireContext())
        recycler_search_result.adapter = adapter.withMimiLoadStateFooter { adapter.retry() }
    }

    override fun setupObservers() {
        viewModel.searchTotalCount.observe(viewLifecycleOwner, Observer { count ->
            if(search_bar.text.isNotBlank()) setSearchResultText(count)
        })

        viewModel.likePostResult.observe(this, {
            when (it) {
                is ApiResult.Success -> {
                    it.result.let { position ->
                        adapter.notifyItemChanged(position, UPDATE_LIKE)
                    }
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.favoriteResult.observe(this, {
            when (it) {
                is ApiResult.Success -> {
                    it.result.let { position ->
                        adapter.notifyItemChanged(position, UPDATE_FAVORITE)
                    }
                }
                is ApiResult.Error -> onApiError(it.throwable)
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

        iv_clear_search_bar.setOnClickListener {
            search_bar.setText("")
            GeneralUtils.showKeyboard(requireContext())
//            search_bar.requestFocus()
        }

        iv_clear_history.setOnClickListener {
            chip_group_search_text.removeAllViews()
            viewModel.clearSearchHistory()
        }

        tv_search.setOnClickListener {
            search(text = search_bar.text.toString())
        }

        search_bar.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    search(text = search_bar.text.toString())
                    true
                }
                else -> false
            }
        }

        search_bar.addTextChangedListener {
            if (it.toString() == "") {
                iv_clear_search_bar.visibility = View.GONE
                layout_search_text.visibility = View.GONE
                getSearchHistory()
                lifecycleScope.launch { adapter.submitData(PagingData.empty()) }
            } else {
                iv_clear_search_bar.visibility = View.VISIBLE
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

    private fun search(text: String? = null, tag: String? = null) {
        if (TextUtils.isEmpty(text) && TextUtils.isEmpty(tag)) {
            GeneralUtils.showToast(
                requireContext(),
                getString(R.string.search_video_input_empty_toast)
            )
//            search_bar.requestFocus()
            return
        }
        layout_search_history.visibility = View.GONE
        text?.let {
            viewModel.updateSearchHistory(text)
            searchKeyword = text
            searchText = text
            searchTag = ""
        }
        tag?.let {
            searchKeyword = tag
            searchText = ""
            searchTag = tag
        }

        when (searchType) {
            PostType.FOLLOWED -> searchPostFollow(text, tag)
            PostType.TEXT_IMAGE_VIDEO,
            PostType.TEXT,
            PostType.IMAGE,
            PostType.VIDEO -> searchPostAll(text, tag)
            else -> {
            }
        }
        GeneralUtils.hideKeyboard(requireActivity())
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
            { id, view, type -> viewModel.loadImage(id, view, type)},
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
                layout_search_history.visibility = View.GONE
                search(text = text)
                GeneralUtils.hideKeyboard(requireActivity())
//                search_bar.clearFocus()
            }
            chip_group_search_text.addView(chip)
        }

        layout_search_history.visibility = View.VISIBLE
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
        val item = arguments?.get(MEMBER_DATA) as MemberPostItem
        val bundle = ClipPlayerFragment.createBundle(item.id)

        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_clipPlayerFragment,
                bundle
            )
        )
    }

}