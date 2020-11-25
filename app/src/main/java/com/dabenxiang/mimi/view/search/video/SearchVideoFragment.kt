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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.FunctionType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.model.vo.SearchingVideoItem
import com.dabenxiang.mimi.view.adapter.SearchVideoAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.player.ui.PlayerFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_search_video.*
import timber.log.Timber
import java.util.*

class SearchVideoFragment : BaseFragment() {

    companion object {
        const val REQUEST_LOGIN = 1000
        const val KEY_DATA = "data"

        fun createBundle(
            title: String = "",
            tag: String = "",
            category: String = ""
        ): Bundle {
            val data = SearchingVideoItem()
            data.title = title
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


    override fun setupFirstTime() {
        super.setupFirstTime()

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()

        (arguments?.getSerializable(KEY_DATA) as SearchingVideoItem?)?.also { data ->
            Timber.d("key data from args is title: ${data.title}, tag: ${data.tag} ")
//            if (arguments?.getBoolean(KEY_IS_FROM_PLAYER) == true) {
//                viewModel.isAdult = data.isAdult
//            }

            if (data.tag.isNotBlank()) {
                viewModel.searchingTag = data.tag
                viewModel.getSearchList()
            }

            viewModel.category = data.category

            if (TextUtils.isEmpty(viewModel.searchingTag) && TextUtils.isEmpty(viewModel.searchingStr)) {
                layout_search_history.visibility = View.VISIBLE
                layout_search_text.visibility = View.GONE
                getSearchHistory()
            } else {
                layout_search_history.visibility = View.GONE
                layout_search_text.visibility = View.VISIBLE
            }

            recyclerview_content.layoutManager = LinearLayoutManager(requireContext())
            recyclerview_content.adapter = videoListAdapter

        }
    }

    override fun setupObservers() {
        viewModel.searchTextLiveData.bindingEditText = search_bar

        viewModel.searchTextLiveData.observe(viewLifecycleOwner, Observer {

        })

        viewModel.searchingListResult.observe(viewLifecycleOwner, Observer {
            videoListAdapter.submitList(it)
        })

        viewModel.searchingTotalCount.observe(viewLifecycleOwner, Observer { count ->
            tv_search_text.text = genResultText(count)
        })

        viewModel.likeResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> videoListAdapter.notifyDataSetChanged()
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.favoriteResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD.show()
                is Loaded -> progressHUD.dismiss()
                is Success -> videoListAdapter.notifyDataSetChanged()
                is Error -> onApiError(it.throwable)
            }
        })

    }

    override fun setupListeners() {
        ib_back.setOnClickListener {
            navigateTo(NavigateItem.Up)
        }

        iv_clean.setOnClickListener {
            viewModel.cleanSearchText()
        }

        tv_search.setOnClickListener {
            searchText()
        }

        iv_clear_search_text.setOnClickListener {
            chip_group_search_text.removeAllViews()
            viewModel.clearSearchHistory()
        }

        search_bar.addTextChangedListener {
            if (it.toString() == "" && !TextUtils.isEmpty(viewModel.searchingTag)) {
                layout_search_history.visibility = View.GONE
                layout_search_text.visibility = View.VISIBLE
            } else if (it.toString() == "") {
                layout_search_history.visibility = View.VISIBLE
                layout_search_text.visibility = View.GONE
                getSearchHistory()
                videoListAdapter.submitList(null)
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
            layout_search_history.visibility = View.GONE
            layout_search_text.visibility = View.VISIBLE
            viewModel.searchingTag = ""
            viewModel.searchingStr = search_bar.text.toString()
            viewModel.getSearchList()
            viewModel.updateSearchHistory(viewModel.searchingStr)
            GeneralUtils.hideKeyboard(requireActivity())
        } else {
            GeneralUtils.showToast(
                requireContext(),
                getString(R.string.search_video_input_empty_toast)
            )
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

        override fun onFunctionClick(type: FunctionType, view: View, item: VideoItem) {
            when (type) {
                FunctionType.LIKE -> {
                    // 點擊更改喜歡,
                    checkStatus {
                        viewModel.currentItem = item
                        item.id?.let {
                            viewModel.modifyLike(it)
                        }
                    }
                }

                FunctionType.FAVORITE -> {
                    // 點擊後加入收藏,
                    checkStatus {
                        viewModel.currentItem = item
                        item.id?.let {
                            viewModel.modifyFavorite(it)
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
                    val bundle = PlayerFragment.createBundle(playerData, true)
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
            viewModel.searchingTag = text
            viewModel.searchingStr = ""
            viewModel.getSearchList()
            GeneralUtils.hideKeyboard(requireActivity())
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
                layout_search_text.visibility = View.VISIBLE
                viewModel.searchingStr = text
                viewModel.searchingTag = ""
                viewModel.getSearchList()
                GeneralUtils.hideKeyboard(requireActivity())
            }
            chip_group_search_text.addView(chip)
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