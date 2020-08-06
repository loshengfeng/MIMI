package com.dabenxiang.mimi.view.search.video

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
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
import com.dabenxiang.mimi.view.dialog.ReportDialogFragment
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_search_video.*
import timber.log.Timber
import java.util.*

class SearchVideoFragment : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"

        fun createBundle(title: String = "", tag: String = "", isAdult: Boolean? = null): Bundle {
            val data = SearchingVideoItem()
            data.title = title
            data.tag = tag
            data.isAdult = isAdult

            return Bundle().also {
                it.putSerializable(KEY_DATA, data)
            }
        }
    }

    private val viewModel: SearchVideoViewModel by viewModels()

    var moreDialog: MoreDialogFragment? = null
    var reportDialog: ReportDialogFragment? = null

    private val videoListAdapter by lazy {
        SearchVideoAdapter(requireContext(), adapterListener)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_search_video
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback { navigateTo(NavigateItem.Up) }

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (GeneralUtils.getScreenSize(requireActivity()).second * 0.0245).toInt()

        viewModel.isAdult = mainViewModel?.adultMode?.value ?: false

        (arguments?.getSerializable(KEY_DATA) as SearchingVideoItem?)?.also { data ->
            Timber.d("key data from args is title: ${data.title}, tag: ${data.tag} and isAdult: ${data.isAdult}")
            if (data.isAdult != null) {
                viewModel.isAdult = data.isAdult!!
                mainViewModel?.isFromPlayer = true
            } else
                mainViewModel?.isFromPlayer = false

            if (data.tag.isNotBlank()) {
                viewModel.searchingTag = data.tag
                tv_search_text.text = genResultText()
                viewModel.getSearchList()
            }

            if (TextUtils.isEmpty(viewModel.searchingTag) && TextUtils.isEmpty(viewModel.searchingStr)) {
                layout_search_history.visibility = View.VISIBLE
                layout_search_text.visibility = View.GONE
                getSearchHistory()
            } else {
                layout_search_history.visibility = View.GONE
                layout_search_text.visibility = View.VISIBLE
            }

            iv_clear_search_text.background =
                if (viewModel.isAdult) {
                    ContextCompat.getDrawable(requireContext(), R.drawable.btn_trash_white_n)
                } else {
                    ContextCompat.getDrawable(requireContext(), R.drawable.btn_trash_n)
                }

            txt_history_title.setTextColor(
                if (viewModel.isAdult) {
                    ContextCompat.getColor(requireContext(), android.R.color.white)
                } else {
                    ContextCompat.getColor(requireContext(), android.R.color.black)
                }
            )

            tv_search_text.setTextColor(
                if (viewModel.isAdult) {
                    ContextCompat.getColor(requireContext(), android.R.color.white)
                } else {
                    ContextCompat.getColor(requireContext(), android.R.color.black)
                }
            )

            layout_frame.background =
                if (viewModel.isAdult) {
                    R.color.adult_color_background
                } else {
                    R.color.normal_color_background
                }.let {
                    requireActivity().getDrawable(it)
                }

            recyclerview_content.layoutManager = LinearLayoutManager(requireContext())
            recyclerview_content.adapter = videoListAdapter

            recyclerview_content.background =
                if (viewModel.isAdult) {
                    R.color.adult_color_background
                } else {
                    R.color.normal_color_background
                }.let {
                    requireActivity().getDrawable(it)
                }

            layout_top.background =
                if (viewModel.isAdult) {
                    R.color.adult_color_status_bar
                } else {
                    R.color.normal_color_status_bar
                }.let {
                    requireActivity().getDrawable(it)
                }

            ib_back.setImageResource(
                if (viewModel.isAdult) {
                    R.drawable.adult_btn_back
                } else {
                    R.drawable.normal_btn_back
                }
            )

            iv_search_bar.setImageResource(
                if (viewModel.isAdult) {
                    R.drawable.bg_black_1_30_radius_18
                } else {
                    R.drawable.bg_white_1_65625_border_gray_11_radius_18
                }
            )

            iv_search.setImageResource(
                if (viewModel.isAdult) {
                    R.drawable.adult_btn_search
                } else {
                    R.drawable.normal_btn_search
                }
            )

            edit_search.setTextColor(
                if (viewModel.isAdult) {
                    R.color.adult_color_text
                } else {
                    R.color.normal_color_text
                }.let {
                    requireActivity().getColor(it)
                }
            )

            iv_clean.setImageResource(
                if (viewModel.isAdult) {
                    R.drawable.btn_close_white
                } else {
                    R.drawable.btn_close_gray
                }
            )
        }
    }

    override fun setupObservers() {
        viewModel.searchTextLiveData.bindingEditText = edit_search

        viewModel.searchTextLiveData.observe(viewLifecycleOwner, Observer {

        })

        viewModel.searchingListResult.observe(viewLifecycleOwner, Observer {
            tv_search_text.text = genResultText()
            videoListAdapter.submitList(it)
        })

        viewModel.searchingTotalCount.observe(viewLifecycleOwner, Observer { count ->
            tv_search_text.text = genResultText(count)
        })

        viewModel.likeResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Success -> {
                    videoListAdapter.notifyDataSetChanged()
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })

        viewModel.favoriteResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Success -> {
                    videoListAdapter.notifyDataSetChanged()
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })

        viewModel.postReportResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Empty -> {
                    GeneralUtils.showToast(requireContext(), getString(R.string.report_success))
                }
                is ApiResult.Error -> {
                    Timber.e(it.throwable)
                }
            }
        })
    }

    override fun setupListeners() {
        ib_back.setOnClickListener {
            if (mainViewModel?.isFromPlayer == true)
                activity?.onBackPressed()
            else navigateTo(NavigateItem.Up)
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

        edit_search.addTextChangedListener {
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

        edit_search.setOnEditorActionListener { v, actionId, event ->
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
        if (edit_search.text.isNotBlank()) {
            layout_search_history.visibility = View.GONE
            layout_search_text.visibility = View.VISIBLE
            viewModel.searchingTag = ""
            viewModel.searchingStr = edit_search.text.toString()
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
            val playerData = PlayerItem(
                item.id ?: 0,
                item.isAdult
            )
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtras(PlayerActivity.createBundle(playerData))
            startActivity(intent)
        }

        override fun onFunctionClick(type: FunctionType, view: View, item: VideoItem) {

            when (type) {
                FunctionType.LIKE -> {
                    // 點擊更改喜歡,
                    viewModel.currentItem = item
                    item.id?.let {
                        viewModel.modifyLike(it)
                    }
                }

                FunctionType.FAVORITE -> {
                    // 點擊後加入收藏,
                    viewModel.currentItem = item
                    item.id?.let {
                        viewModel.modifyFavorite(it)
                    }
                }

                FunctionType.SHARE -> {
                    /* 點擊後複製網址 */
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

                FunctionType.MSG -> {
                    // 點擊評論，進入播放頁面滾動到最下面
                    val playerData = PlayerItem(
                        item.id ?: 0,
                        item.isAdult
                    )
                    val intent = Intent(requireContext(), PlayerActivity::class.java)
                    intent.putExtras(PlayerActivity.createBundle(playerData, true))
                    startActivity(intent)
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

    private val onReportDialogListener = object : ReportDialogFragment.OnReportDialogListener {
        override fun onSend(item: BaseMemberPostItem, content: String) {
            if (TextUtils.isEmpty(content)) {
                GeneralUtils.showToast(requireContext(), getString(R.string.report_error))
            } else {
                reportDialog?.dismiss()
                viewModel.sendPostReport(item as MemberPostItem, content)
            }
        }

        override fun onCancel() {
            reportDialog?.dismiss()
        }
    }

    private fun getSearchHistory() {
        chip_group_search_text.removeAllViews()
        val searchHistories = viewModel.getSearchHistory().asReversed()
        searchHistories.forEach { text ->
            val chip = LayoutInflater.from(chip_group_search_text.context)
                .inflate(R.layout.chip_item, chip_group_search_text, false) as Chip
            chip.text = text

            if (viewModel.isAdult) {
                chip.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.color_black_6)
                )
                chip.setTextColor(requireContext().getColor(R.color.color_white_1_50))
            } else {
                chip.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.color_black_1_10)
                )
                chip.setTextColor(requireContext().getColor(R.color.color_black_1_50))
            }

            chip.setOnClickListener {
                edit_search.setText(text)
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
}