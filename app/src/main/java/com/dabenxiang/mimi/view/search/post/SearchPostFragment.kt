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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
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
import com.dabenxiang.mimi.view.club.base.AdHeaderAdapter
import com.dabenxiang.mimi.view.club.pic.ClubPicFragment
import com.dabenxiang.mimi.view.club.text.ClubTextFragment
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment.Companion.MEMBER_DATA
import com.dabenxiang.mimi.view.player.ui.ClipPlayerFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.search.post.SearchPostAdapter.Companion.UPDATE_FAVORITE
import com.dabenxiang.mimi.view.search.post.SearchPostAdapter.Companion.UPDATE_LIKE
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_search_post.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

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

    private val adTop: AdHeaderAdapter by lazy {
        AdHeaderAdapter(requireContext())
    }

    private var searchType: PostType = PostType.TEXT_IMAGE_VIDEO
    private var searchTag: String? = null
    private var searchText: String? = null
    private var searchOrderBy: StatisticsOrderType = StatisticsOrderType.LATEST
    private var searchKeyword: String = ""

    private val adapter: SearchPostAdapter by lazy {
        SearchPostAdapter(
            requireActivity(),
            postListener,
            viewModel.viewModelScope,
            { searchText ?: "" },
            { searchTag ?: "" })
    }

    private val postListener = object : MyPostListener {

        override fun onLoginClick() {
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_loginFragment,
                    LoginFragment.createBundle(LoginFragment.TYPE_LOGIN)
                )
            )
        }

        override fun onRegisterClick() {
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_loginFragment,
                    LoginFragment.createBundle(LoginFragment.TYPE_REGISTER)
                )
            )
        }

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            checkStatus { viewModel.likePost(item, position, isLike) }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            checkStatus {
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
                    R.id.action_to_myPostFragment,
                    bundle
                )
            )
        }

        override fun onMoreClick(item: MemberPostItem, position: Int) {
            onMoreClick(item, position) {
                it as MemberPostItem

                val searchPostItem =
                    SearchPostItem(searchType, searchOrderBy, searchTag, searchKeyword)
                val bundle = Bundle()
                item.id
                bundle.putBoolean(MyPostFragment.EDIT, true)
                bundle.putString(BasePostFragment.PAGE, BasePostFragment.SEARCH)
                bundle.putSerializable(MEMBER_DATA, item)
                bundle.putSerializable(KEY_DATA, searchPostItem)

                when (it.type) {
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
            checkStatus {
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
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {}

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {}

        override fun onChipClick(type: PostType, tag: String) {
            search_bar.setText(tag)
            search(tag = tag)
            GeneralUtils.hideKeyboard(requireActivity())
            search_bar.clearFocus()

            val searchPostItem = SearchPostItem(
                searchType,
                searchOrderBy,
                tag,
                searchText,
            )

            arguments?.putSerializable(KEY_DATA, searchPostItem)
        }
    }

    override fun setupFirstTime() {
        viewModel.adWidth =
            GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second

        (arguments?.getSerializable(KEY_DATA) as SearchPostItem).also {
            searchType = it.type
            searchTag = it.tag
            searchText = it.keyword
            searchOrderBy = it.orderBy ?: StatisticsOrderType.LATEST
        }

        if (!TextUtils.isEmpty(searchText)) {
            recycler_search_result.visibility = View.VISIBLE
            search_bar.setText(searchText)
            search(text = searchText)
            search_bar.post {
                search_bar.clearFocus()
            }
        } else if (!TextUtils.isEmpty(searchTag)) {
            recycler_search_result.visibility = View.VISIBLE
            search_bar.setText(searchTag)
            search(tag = searchTag)
            search_bar.post {
                search_bar.clearFocus()
            }
        } else {
            recycler_search_result.visibility = View.INVISIBLE
            layout_search_text.visibility = View.GONE
            iv_clear_search_bar.visibility = View.GONE
            getSearchHistory()
            search_bar.post {
                GeneralUtils.showKeyboard(search_bar.context)
                search_bar.requestFocus()
            }
        }

        recycler_search_result.layoutManager = LinearLayoutManager(requireContext())
        recycler_search_result.adapter = ConcatAdapter(adTop, adapter/*.withMimiLoadStateFooter { adapter.retry() }*/)

        @OptIn(ExperimentalCoroutinesApi::class)
        viewModel.viewModelScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh) {
                    is LoadState.NotLoading -> progressHUD.dismiss()
                    is LoadState.Error -> {
                        progressHUD.dismiss()
                        onApiError((loadStates.refresh as LoadState.Error).error)
                    }
                    else -> progressHUD.show()
                }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        viewModel.viewModelScope.launch {
            @OptIn(FlowPreview::class)
            adapter.loadStateFlow
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .onEach { delay(1000) }
                .collect {
                    if (adapter.snapshot().items.isEmpty() && timeout > 0) {
                        timeout--
                        adapter.refresh()
                    }
                }
        }

    }

    override fun setupObservers() {
        viewModel.searchTotalCount.observe(viewLifecycleOwner, { count ->
            if (search_bar.text.isNotBlank()) setSearchResultText(count)
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

        viewModel.topAdResult.observe(this) {
            adTop.adItem = it
            adTop.notifyDataSetChanged()
        }

        viewModel.getTopAd("search_top")
    }

    override fun setupListeners() {
        ib_back.setOnClickListener {
            GeneralUtils.hideKeyboard(requireActivity())
            findNavController().navigateUp()
        }

        iv_clear_search_bar.setOnClickListener {
            search_bar.setText("")
            GeneralUtils.hideKeyboard(requireActivity())
            GeneralUtils.showKeyboard(requireContext())
            search_bar.requestFocus()
        }

        iv_clear_history.setOnClickListener {
            chip_group_search_text.removeAllViews()
            viewModel.clearSearchHistory()
        }

        tv_search.setOnClickListener {
            search(text = search_bar.text.toString())

            val searchPostItem = SearchPostItem(
                searchType,
                searchOrderBy,
                searchTag,
                search_bar.text.toString(),
            )

            arguments?.putSerializable(KEY_DATA, searchPostItem)
        }

        search_bar.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    search(text = search_bar.text.toString())

                    val searchPostItem = SearchPostItem(
                        searchType,
                        searchOrderBy,
                        searchTag,
                        search_bar.text.toString(),
                    )

                    arguments?.putSerializable(KEY_DATA, searchPostItem)

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
                recycler_search_result?.visibility = View.INVISIBLE
            } else {
                iv_clear_search_bar.visibility = View.VISIBLE
            }
        }
    }

    private fun search(text: String? = null, tag: String? = null) {
        if (TextUtils.isEmpty(text) && TextUtils.isEmpty(tag)) {
            GeneralUtils.showToast(
                requireContext(),
                getString(R.string.search_video_input_empty_toast)
            )
            search_bar.requestFocus()
            return
        }
        layout_search_text.visibility = View.GONE
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
            PostType.FOLLOWED -> lifecycleScope.launch {
                viewModel.posts(
                    pageCode = SearchPostFragment::class.simpleName + "FOLLOW",
                    keyword = text,
                    tag = tag
                )
                    .flowOn(Dispatchers.IO)
                    .collectLatest {
                        adapter.submitData(it)
                        recycler_search_result?.visibility = View.VISIBLE
                    }
            }
            PostType.TEXT_IMAGE_VIDEO,
            PostType.TEXT,
            PostType.IMAGE,
            PostType.VIDEO -> lifecycleScope.launch {
                viewModel.posts(
                    SearchPostFragment::class.simpleName + "ALL",
                    searchType,
                    text,
                    tag,
                    searchOrderBy
                )
                    .flowOn(Dispatchers.IO)
                    .collectLatest {
                        adapter.submitData(it)
                        recycler_search_result?.visibility = View.VISIBLE
                    }
            }
            else -> {
            }
        }
        recycler_search_result.visibility = View.INVISIBLE
        GeneralUtils.hideKeyboard(requireActivity())
        search_bar.clearFocus()
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
                search(text = text)
                GeneralUtils.hideKeyboard(requireActivity())
                search_bar.clearFocus()

                val searchPostItem = SearchPostItem(
                    searchType,
                    searchOrderBy,
                    searchTag,
                    text,
                )

                arguments?.putSerializable(KEY_DATA, searchPostItem)
            }
            chip_group_search_text.addView(chip)
        }

        layout_search_history.visibility = View.VISIBLE
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