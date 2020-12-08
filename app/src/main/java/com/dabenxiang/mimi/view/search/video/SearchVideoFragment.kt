package com.dabenxiang.mimi.view.search.video

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
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
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.FunctionType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.model.vo.SearchingVideoItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.pagingfooter.withMimiLoadStateFooter
import com.dabenxiang.mimi.view.player.ui.PlayerFragment
import com.dabenxiang.mimi.view.player.ui.PlayerV2Fragment
import com.dabenxiang.mimi.view.search.video.SearchVideoAdapter.Companion.UPDATE_FAVORITE
import com.dabenxiang.mimi.view.search.video.SearchVideoAdapter.Companion.UPDATE_LIKE
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_search_video.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class SearchVideoFragment : BaseFragment() {

    companion object {
        const val REQUEST_LOGIN = 1000
        const val KEY_DATA = "data"

        fun createBundle(
            tag: String = "",
            category: String = ""
        ): Bundle {
            val data = SearchingVideoItem()
            data.tag = tag
            data.category = category

            return Bundle().also {
                it.putSerializable(KEY_DATA, data)
            }
        }
    }

    private val viewModel: SearchVideoViewModel by viewModels()

    var moreDialog: MoreDialogFragment? = null

    private val videoListAdapter by lazy {
        SearchVideoAdapter(requireContext(), adapterListener)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_search_video
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

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

    override fun setupFirstTime() {
        viewModel.adWidth = GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second

        (arguments?.getSerializable(KEY_DATA) as SearchingVideoItem?)?.also { data ->

            viewModel.searchingTag = data.tag
            viewModel.category = data.category
            if (data.tag.isNotBlank()) {
                layout_search_history.visibility = View.GONE
                search_bar.setText(data.tag)
                searchVideo(tag = data.tag)
                search_bar.post {
                    search_bar.clearFocus()
                }
            } else {
                iv_clear_search_bar.visibility = View.GONE
                getSearchHistory()
                search_bar.post {
                    GeneralUtils.showKeyboard(search_bar.context)
                    search_bar.requestFocus()
                }
            }
            layout_search_text.visibility = View.GONE

            videoListAdapter.addLoadStateListener(loadStateListener)
            recyclerview_content.layoutManager = LinearLayoutManager(requireContext())
            recyclerview_content.adapter =
                videoListAdapter.withMimiLoadStateFooter { videoListAdapter.retry() }
        }
    }

    override fun setupObservers() {
        viewModel.showProgress.observe(this, Observer { showProgress ->
            if (showProgress) progressHUD.show()
            else progressHUD.dismiss()
        })

        viewModel.searchingTotalCount.observe(viewLifecycleOwner, Observer { count ->
            if (search_bar.text.isNotBlank()) {
                tv_search_text.text = genResultText(count)
                layout_search_text.visibility = View.VISIBLE
            }
        })

        viewModel.likeResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    it.result.let { position ->
                        videoListAdapter.notifyItemChanged(position, UPDATE_LIKE)
                    }
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.favoriteResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    it.result.let { position ->
                        videoListAdapter.notifyItemChanged(position, UPDATE_FAVORITE)
                    }
                }
                is Error -> onApiError(it.throwable)
            }
        })

    }

    override fun setupListeners() {
        ib_back.setOnClickListener {
            GeneralUtils.hideKeyboard(requireActivity())
            navigateTo(NavigateItem.Up)
        }

        iv_clear_search_bar.setOnClickListener {
            search_bar.setText("")
            GeneralUtils.showKeyboard(requireContext())
            search_bar.requestFocus()
        }

        tv_search.setOnClickListener {
            searchText()
        }

        iv_clear_history.setOnClickListener {
            chip_group_search_text.removeAllViews()
            viewModel.clearSearchHistory()
        }

        search_bar.addTextChangedListener {
            if (it.toString() == "") {
                iv_clear_search_bar.visibility = View.GONE
                layout_search_text.visibility = View.GONE
                getSearchHistory()
                lifecycleScope.launch { videoListAdapter.submitData(PagingData.empty()) }
            } else {
                iv_clear_search_bar.visibility = View.VISIBLE
            }
        }

        search_bar.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    searchText()
                    true
                }
                else -> false
            }
        }
    }

    private fun searchText() {
        if (search_bar.text.isNotBlank()) {
            layout_search_text.visibility = View.GONE
            layout_search_history.visibility = View.GONE
            viewModel.searchingTag = ""
            viewModel.searchingStr = search_bar.text.toString()
            searchVideo(keyword = search_bar.text.toString())
            viewModel.updateSearchHistory(viewModel.searchingStr)
            GeneralUtils.hideKeyboard(requireActivity())
            search_bar.clearFocus()
        } else {
            GeneralUtils.showToast(
                requireContext(),
                getString(R.string.search_video_input_empty_toast)
            )
            search_bar.requestFocus()
        }
    }

    private val adapterListener = object : SearchVideoAdapter.EventListener {
        override fun onVideoClick(item: VideoItem) {
            val playerData = PlayerItem(item.id ?: 0)
            val bundle = PlayerFragment.createBundle(playerData)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_searchVideoFragment_to_navigation_player,
                    bundle
                )
            )
        }

        override fun onFunctionClick(
            type: FunctionType,
            view: View,
            item: VideoItem,
            position: Int
        ) {
            when (type) {
                FunctionType.LIKE -> {
                    // 點擊更改喜歡,
                    checkStatus {
                        viewModel.currentItem = item
                        item.id.let {
                            viewModel.modifyLike(it, position)
                        }
                    }
                }

                FunctionType.FAVORITE -> {
                    // 點擊後加入收藏,
                    checkStatus {
                        viewModel.currentItem = item
                        item.id?.let {
                            viewModel.modifyFavorite(it, position)
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
                    val playerData = PlayerItem(item.id)
                    val bundle = PlayerV2Fragment.createBundle(playerData, true)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_searchVideoFragment_to_navigation_player,
                            bundle
                        )
                    )
                }

                FunctionType.MORE -> {
                    if (item.id != null) {
                        val data = MemberPostItem(
                            id = item.id,
                            creationDate = Date(),
                            type = PostType.VIDEO
                        )
                        moreDialog =
                            MoreDialogFragment.newInstance(data, onMoreDialogListener).also {
                                it.show(
                                    requireActivity().supportFragmentManager,
                                    MoreDialogFragment::class.java.simpleName
                                )
                            }
                    } else {
                        GeneralUtils.showToast(
                            requireContext(),
                            getString(R.string.unexpected_error)
                        )
                    }
                }
                else -> {
                }
            }
        }

        override fun onChipClick(text: String) {
            layout_search_text.visibility = View.GONE
            viewModel.searchingTag = text
            viewModel.searchingStr = ""
            search_bar.setText(text)
            searchVideo(tag = text)
            GeneralUtils.hideKeyboard(requireActivity())
            search_bar.clearFocus()
        }

        override fun onAvatarDownload(view: ImageView, id: String) {
        }
    }

    /**
     * 產生搜尋結果的效果文字
     */
    private fun genResultText(count: Long = 0): SpannableString {
        val word = SpannableString(
            getString(
                R.string.search_video_result,
                if (viewModel.searchingTag.isNotBlank()) viewModel.searchingTag else viewModel.searchingStr,
                count
            )
        )
        word.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_red_1
                )
            ), word.indexOf("\"") + 1, word.lastIndexOf("\""), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        word.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_red_1
                )
            ), word.indexOf("到") + 1, word.lastIndexOf("个"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return word
    }

    private val onMoreDialogListener = object : MoreDialogFragment.OnMoreDialogListener {
        override fun onProblemReport(item: BaseMemberPostItem, isComment: Boolean) {
            moreDialog?.dismiss()
            checkStatus {
                (requireActivity() as MainActivity).showReportDialog(
                    item,
                    isComment = isComment
                )
            }
        }

        override fun onCancel() {
            moreDialog?.dismiss()
        }
    }

    private fun getSearchHistory() {
        chip_group_search_text.removeAllViews()
        val searchHistories = viewModel.getSearchHistory().asReversed()
        searchHistories.forEach { text ->
            val chip = LayoutInflater.from(chip_group_search_text.context)
                .inflate(R.layout.chip_item, chip_group_search_text, false) as Chip
            chip.text = text
            chip.ellipsize = TextUtils.TruncateAt.END
            chip.setOnClickListener {
                search_bar.setText(text)
                layout_search_history.visibility = View.GONE
                viewModel.searchingStr = text
                viewModel.searchingTag = ""
                searchVideo(keyword = text)
                GeneralUtils.hideKeyboard(requireActivity())
                search_bar.clearFocus()
            }
            chip_group_search_text.addView(chip)
        }

        layout_search_history.visibility = View.VISIBLE
    }

    private fun searchVideo(
        keyword: String? = null,
        tag: String? = null
    ) {
        lifecycleScope.launch {
            videoListAdapter.submitData(PagingData.empty())
            viewModel.getSearchVideoResult(keyword, tag)
                .collectLatest { videoListAdapter.submitData(it) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_LOGIN -> {
                    findNavController().navigate(R.id.action_to_loginFragment, data?.extras)
                }
            }
        }
    }

}