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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.FunctionType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.adapter.MemberPostPagedAdapter
import com.dabenxiang.mimi.view.adapter.SearchVideoAdapter
import com.dabenxiang.mimi.view.adapter.viewHolder.ClipPostHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.PicturePostHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.TextPostHolder
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.club.adapter.ClubFuncItem
import com.dabenxiang.mimi.view.club.member.ClubMemberAdapter
import com.dabenxiang.mimi.view.club.member.MiMiLinearLayoutManager
import com.dabenxiang.mimi.view.club.topic.TopicDetailFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.player.ui.PlayerFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_search_post.*

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

    private var currentPostType: PostType = PostType.TEXT
    private var mTag: String = ""
    private var searchText: String = ""
    private var searchKeyword: String = ""

    private var isPostFollow: Boolean = false
    private var isClub: Boolean = false

    private var concatAdapter: ConcatAdapter? = null
    private var memberPostAdapter: MemberPostPagedAdapter? = null
    private var videoListAdapter: SearchVideoAdapter? = null
    private var hybridItemsCount: Long = -1L

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (arguments?.getSerializable(KEY_DATA) as SearchPostItem).also {
            currentPostType = it.type
            isPostFollow = it.isPostFollow
            mTag = it.tag
            searchText = it.searchText
            isClub = it.isClub
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        useAdultTheme(false)

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()

        if (TextUtils.isEmpty(mTag) && TextUtils.isEmpty(searchText)) {
            layout_search_history.visibility = View.VISIBLE
            layout_search_text.visibility = View.GONE
            getSearchHistory()
        } else {
            layout_search_history.visibility = View.GONE
            layout_search_text.visibility = View.VISIBLE
        }

        memberPostAdapter = MemberPostPagedAdapter(
            requireContext(), adultListener, mTag, memberPostFuncItem, true
        )

        videoListAdapter = SearchVideoAdapter(requireContext(), videoAdapterListener)
        recycler_search_result.layoutManager = LinearLayoutManager(requireContext())

        concatAdapter = ConcatAdapter(memberPostAdapter, videoListAdapter)

        takeIf { isClub }?.also {
            recycler_search_result.layoutManager = MiMiLinearLayoutManager(requireContext())
            recycler_search_result.adapter = clubMemberAdapter
        }
            ?: run { recycler_search_result.adapter = concatAdapter }

        if (!TextUtils.isEmpty(mTag)) {
            updateTag(mTag)
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
        viewModel.showProgress.observe(viewLifecycleOwner, Observer { showProgress ->
            showProgress?.takeUnless { it }?.also { progressHUD?.dismiss() }
        })

        viewModel.followPostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    when ((concatAdapter?.adapters?.get(0) as MemberPostPagedAdapter).viewHolderMap?.get(it.result)) {
                        is ClipPostHolder,
                        is PicturePostHolder,
                        is TextPostHolder -> {
                            (concatAdapter?.adapters?.get(0) as MemberPostPagedAdapter).notifyItemChanged(
                                it.result,
                                MemberPostPagedAdapter.PAYLOAD_UPDATE_LIKE
                            )
                        }
                    }
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.likePostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    when ((concatAdapter?.adapters?.get(0) as MemberPostPagedAdapter).viewHolderMap?.get(it.result)) {
                        is ClipPostHolder,
                        is PicturePostHolder,
                        is TextPostHolder -> {
                            (concatAdapter?.adapters?.get(0) as MemberPostPagedAdapter).notifyItemChanged(
                                it.result,
                                MemberPostPagedAdapter.PAYLOAD_UPDATE_LIKE
                            )
                        }
                    }
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.searchPostItemByTagListResult.observe(viewLifecycleOwner, Observer {
            (concatAdapter?.adapters?.get(0) as MemberPostPagedAdapter).submitList(it)
        })

        viewModel.searchPostItemByKeywordListResult.observe(viewLifecycleOwner, Observer {
            (concatAdapter?.adapters?.get(0) as MemberPostPagedAdapter).submitList(it)
        })

        viewModel.searchTotalCount.observe(viewLifecycleOwner, Observer { count ->
            if(currentPostType == PostType.HYBRID) {
                if(hybridItemsCount == -1L)
                    hybridItemsCount = count
                else {
                    tv_search_text.text = getSearchText(currentPostType, searchKeyword, hybridItemsCount + count, isPostFollow)
                    hybridItemsCount = -1L
                }
            } else
                tv_search_text.text = getSearchText(currentPostType, searchKeyword, count, isPostFollow)
        })

        viewModel.clubItemListResult.observe(viewLifecycleOwner, Observer {
            clubMemberAdapter.submitList(it)
        })

        viewModel.followResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Empty -> {
                    recycler_search_result.adapter?.notifyItemRangeChanged(
                        0,
                        viewModel.totalCount,
                        MemberPostPagedAdapter.PAYLOAD_UPDATE_FOLLOW
                    )
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.searchingListResult.observe(viewLifecycleOwner, Observer {
            (concatAdapter?.adapters?.get(1) as SearchVideoAdapter).submitList(it)
        })

        viewModel.likeVideoResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> (concatAdapter?.adapters?.get(1) as SearchVideoAdapter).notifyDataSetChanged()
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.favoriteVideoResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> (concatAdapter?.adapters?.get(1) as SearchVideoAdapter).notifyDataSetChanged()
                is Error -> onApiError(it.throwable)
            }
        })

        mainViewModel?.deletePostResult?.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    memberPostAdapter?.also { adapter ->
                        adapter.removedPosList.add(it.result)
                        adapter.notifyItemChanged(it.result)
                    }
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.cleanRemovedPosList.observe(viewLifecycleOwner, Observer{
            memberPostAdapter?.removedPosList?.clear()
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
            search()
        }

        search_bar.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    search()
                    true
                }
                else -> false
            }
        }

        search_bar.addTextChangedListener {
            if (it.toString() == "" && !TextUtils.isEmpty(mTag)) {
                layout_search_history.visibility = View.GONE
                layout_search_text.visibility = View.VISIBLE
            } else if (it.toString() == "") {
                layout_search_history.visibility = View.VISIBLE
                layout_search_text.visibility = View.GONE
                getSearchHistory()
                (concatAdapter?.adapters?.get(0) as MemberPostPagedAdapter).submitList(null)
                clubMemberAdapter.submitList(null)
                (concatAdapter?.adapters?.get(1) as SearchVideoAdapter).submitList(null)
            }
        }
    }

    private fun search() {
        GeneralUtils.hideKeyboard(requireActivity())
        if (viewModel.isSearchTextEmpty(search_bar.text.toString())) {
            GeneralUtils.showToast(
                requireContext(),
                getString(R.string.search_input_empty_toast)
            )
            return
        }
        layout_search_history.visibility = View.GONE
        layout_search_text.visibility = View.VISIBLE
        updateTag("")

        viewModel.updateSearchHistory(search_bar.text.toString())

        if (isClub) {
            viewModel.getClubs(search_bar.text.toString())
            progressHUD?.show()
        } else {
            when (currentPostType) {
                PostType.VIDEO_ON_DEMAND -> viewModel.getSearchVideoList("", search_bar.text.toString())
                PostType.HYBRID -> {
                    viewModel.getSearchVideoList("", search_bar.text.toString())
                    viewModel.getSearchPostsByKeyword(
                            currentPostType,
                            search_bar.text.toString(),
                            isPostFollow
                    )
                }
                else -> {
                    viewModel.getSearchPostsByKeyword(
                            currentPostType,
                            search_bar.text.toString(),
                            isPostFollow
                    )
                }
            }
        }
    }

    private val videoAdapterListener = object : SearchVideoAdapter.EventListener {
        override fun onVideoClick(item: VideoItem) {
            val playerData = PlayerItem(item.id ?: 0)
            val bundle = PlayerFragment.createBundle(playerData)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_searchPostFragment_to_navigation_player,
                    bundle
                )
            )
        }

        override fun onFunctionClick(type: FunctionType, view: View, item: VideoItem) {
            when (type) {
                FunctionType.LIKE -> {
                    // 點擊更改喜歡,
                    checkStatus {
                        viewModel.currentVideoItem = item
                        item.id?.let {
                            viewModel.modifyVideoLike(it)
                        }
                    }
                }

                FunctionType.FAVORITE -> {
                    // 點擊後加入收藏,
                    checkStatus {
                        viewModel.currentVideoItem = item
                        item.id?.let {
                            viewModel.modifyVideoFavorite(it)
                        }
                    }
                }

                FunctionType.SHARE -> {
                    /* 點擊後複製網址 */
                    checkStatus {
                        if (item.tags == null || (item.tags as String).isEmpty() || item.id == null) {
                            GeneralUtils.showToast(requireContext(), "copy url error")
                        } else {
                            GeneralUtils.copyToClipboard(
                                    requireContext(),
                                    viewModel.getShareUrl(item.tags, item.id)
                            )
                            GeneralUtils.showToast(
                                    requireContext(),
                                    requireContext().getString(R.string.copy_url)
                            )
                        }
                    }
                }

                FunctionType.MSG -> {
                    // 點擊評論，進入播放頁面滾動到最下面
                    val playerData = PlayerItem(item.id ?: 0)
                    val bundle = PlayerFragment.createBundle(playerData)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_searchPostFragment_to_navigation_player,
                            bundle
                        )
                    )
                }

                FunctionType.MORE -> {
                }
                else -> {
                }
            }
        }
        override fun onChipClick(text: String){
            viewModel.getSearchVideoList(text, "")
            GeneralUtils.hideKeyboard(requireActivity())
        }
        override fun onAvatarDownload(view: ImageView, id: String){}
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

        if (isClub) word.append(getString(R.string.search_type_club))

        if (!isPostFollow && !isClub) {
            val typeText = when (type) {
                PostType.TEXT -> getString(R.string.search_type_text)
                PostType.IMAGE -> getString(R.string.search_type_picture)
                PostType.VIDEO, PostType.VIDEO_ON_DEMAND -> getString(R.string.search_type_clip)
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

    private val clubMemberAdapter by lazy {
        ClubMemberAdapter(
            requireContext(),
            clubFuncItem
        )
    }

    private val clubFuncItem by lazy {
        ClubFuncItem(
            { item -> onItemClick(item) },
            { id, view, type -> viewModel.loadImage(id, view, type) },
            { item, isFollow, function -> clubFollow(item, isFollow, function) })
    }

    private val memberPostFuncItem by lazy {
        MemberPostFuncItem(
            {},
            { id, view, type -> viewModel.loadImage(id, view, type) },
            { item, items, isFollow, func -> followMember(item, items, isFollow, func) },
            { item, isLike, func -> likePost(item, isLike, func) },
            { item, isFavorite, func -> favoritePost(item, isFavorite, func) }
        )
    }

    private val adultListener = object : AdultListener {
        override fun onFollowPostClick(item: MemberPostItem, position: Int, isFollow: Boolean) {
            checkStatus { viewModel.followPost(item, position, isFollow) }
        }

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            checkStatus { viewModel.likePost(item, position, isLike) }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            checkStatus {
                when (adultTabType) {
                    AdultTabType.PICTURE -> {
                        val bundle = PictureDetailFragment.createBundle(item, 1)
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_searchPostFragment_to_clubPicFragment,
                                bundle
                            )
                        )
                    }
                    AdultTabType.TEXT -> {
                        val bundle = TextDetailFragment.createBundle(item, 1)
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_searchPostFragment_to_clubTextFragment,
                                bundle
                            )
                        )
                    }
                    AdultTabType.CLIP -> {
                        val bundle = ClipFragment.createBundle(arrayListOf(item), 0, true)
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_searchPostFragment_to_clipFragment,
                                bundle
                            )
                        )
                    }
                    else -> {
                    }
                }
            }
        }

        override fun onMoreClick(item: MemberPostItem, items:List<MemberPostItem>) {
            val searchPostItem = arguments?.getSerializable(KEY_DATA) as SearchPostItem

            memberPostAdapter?.also {
                onMoreClick(
                    item,
                    ArrayList(items),
                    onEdit = {
                        val bundle = Bundle()
                        bundle.putBoolean(MyPostFragment.EDIT, true)
                        bundle.putString(BasePostFragment.PAGE, BasePostFragment.SEARCH)
                        bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
                        bundle.putSerializable(KEY_DATA, searchPostItem)

                        it as MemberPostItem
                        when (item.type) {
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
                )
            }
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = PictureDetailFragment.createBundle(item, 0)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_searchPostFragment_to_clubPicFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 0)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_searchPostFragment_to_clubTextFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.CLIP -> {
                    // todo 更換新的 clip detail page
                    val bundle = ClipFragment.createBundle(arrayListOf(item), 0)
//                    navigateTo(
//                        NavigateItem.Destination(
//                            R.id.action_searchPostFragment_to_clipFragment,
//                            bundle
//                        )
//                    )
                }
                else -> {
                }
            }
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {
            val iterator = item.iterator()
            val memberPostItemList = arrayListOf<MemberPostItem>()
            while (iterator.hasNext()) {
                val data = iterator.next()
                if (data.type != PostType.AD) memberPostItemList.add(data)
            }
            val mappingPosition = position - (position / 3)
            val bundle = ClipFragment.createBundle(ArrayList(memberPostItemList), mappingPosition)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_searchPostFragment_to_clipFragment,
                    bundle
                )
            )
        }

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {
            checkStatus {
                val bundle = ClipFragment.createBundle(ArrayList(item), position)
                navigateTo(
                    NavigateItem.Destination(
                        R.id.action_searchPostFragment_to_clipFragment,
                        bundle
                    )
                )
            }
        }

        override fun onChipClick(type: PostType, tag: String) {
            updateTag(tag)
            viewModel.getSearchPostsByTag(type, tag, isPostFollow)
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
    }

    private fun updateTag(tag: String) {
        if (TextUtils.isEmpty(tag)) {
            searchText = search_bar.text.toString()
            mTag = tag
            searchKeyword = search_bar.text.toString()
            (concatAdapter?.adapters?.get(0) as MemberPostPagedAdapter).setupTag(tag)
        } else {
            searchText = ""
            mTag = tag
            searchKeyword = tag
            (concatAdapter?.adapters?.get(0) as MemberPostPagedAdapter).setupTag(tag)
            search_bar.setText("")
        }
    }

    private fun clubFollow(
        memberClubItem: MemberClubItem,
        isFollow: Boolean,
        update: (Boolean) -> Unit
    ) {
        checkStatus { viewModel.clubFollow(memberClubItem, isFollow, update) }
    }

    private fun onItemClick(item: MemberClubItem) {
        val bundle = TopicDetailFragment.createBundle(item)
        findNavController().navigate(R.id.action_searchPostFragment_to_topicDetailFragment, bundle)
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
                layout_search_text.visibility = View.VISIBLE
                updateTag("")
                if (isClub) {
                    viewModel.getClubs(search_bar.text.toString())
                    progressHUD?.show()
                } else {
                    when (currentPostType) {
                        PostType.VIDEO_ON_DEMAND -> {
                            viewModel.getSearchVideoList("", search_bar.text.toString())
                        }
                        PostType.HYBRID -> {
                            viewModel.getSearchVideoList("", search_bar.text.toString())
                            viewModel.getSearchPostsByKeyword(
                                    currentPostType,
                                    search_bar.text.toString(),
                                    isPostFollow
                            )
                        }
                        else -> {
                            viewModel.getSearchPostsByKeyword(
                                    currentPostType,
                                    search_bar.text.toString(),
                                    isPostFollow
                            )
                        }
                    }
                }
            }
            chip_group_search_text.addView(chip)
        }
    }

    private fun followMember(
        memberPostItem: MemberPostItem,
        items: List<MemberPostItem>,
        isFollow: Boolean,
        update: (Boolean) -> Unit
    ) {
        checkStatus { viewModel.followMember(memberPostItem, ArrayList(items), isFollow, update) }
    }

    private fun likePost(
        memberPostItem: MemberPostItem,
        isLike: Boolean,
        update: (Boolean, Int) -> Unit
    ) {
        checkStatus { viewModel.likePost(memberPostItem, isLike, update) }
    }

    private fun favoritePost(
        memberPostItem: MemberPostItem,
        isFavorite: Boolean,
        update: (Boolean, Int) -> Unit
    ) {
        checkStatus { viewModel.favoritePost(memberPostItem, isFavorite, update) }
    }
}