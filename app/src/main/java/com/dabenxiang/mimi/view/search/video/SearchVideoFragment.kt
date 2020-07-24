package com.dabenxiang.mimi.view.search.video

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
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
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.model.serializable.SearchingVideoData
import com.dabenxiang.mimi.view.adapter.SearchVideoAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.MoreDialogFragment
import com.dabenxiang.mimi.view.dialog.ReportDialogFragment
import com.dabenxiang.mimi.view.player.PlayerActivity
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_search_video.*
import timber.log.Timber
import java.util.*

class SearchVideoFragment : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"

        fun createBundle(title: String = "", tag: String = ""): Bundle {
            val data = SearchingVideoData()
            data.title = title
            data.tag = tag

            return Bundle().also {
                it.putSerializable(KEY_DATA, data)
            }
        }
    }

    private val viewModel: SearchVideoViewModel by viewModels()

    var moreDialog: MoreDialogFragment? = null
    var reportDialog: ReportDialogFragment? = null

    private val videoListAdapter by lazy {
        SearchVideoAdapter(adapterListener)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_search_video
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback { navigateTo(NavigateItem.Up) }

        viewModel.isAdult = mainViewModel?.adultMode?.value ?: false

        (arguments?.getSerializable(KEY_DATA) as SearchingVideoData?)?.also { data ->
            if (data.tag.isNotBlank()) {
                viewModel.searchingTag = data.tag
                txt_result.text = genResultText()
                viewModel.getSearchList()
            }

            txt_result.setTextColor(
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
            txt_result.text = genResultText()
            videoListAdapter.submitList(it)
        })

        viewModel.searchingTotalCount.observe(viewLifecycleOwner, Observer { count ->
            txt_result.text = genResultText(count)
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
            navigateTo(NavigateItem.Up)
        }

        iv_clean.setOnClickListener {
            viewModel.cleanSearchText()
        }

        tv_search.setOnClickListener {
            if (edit_search.text.isNotBlank()) {
                viewModel.searchingTag = ""
                viewModel.searchingStr = edit_search.text.toString()
                viewModel.getSearchList()
            } else {
                GeneralUtils.showToast(
                    requireContext(),
                    getString(R.string.search_video_input_empty_toast)
                )
            }
        }
    }

    private val adapterListener = object : SearchVideoAdapter.EventListener {
        override fun onVideoClick(item: VideoItem) {
            val playerData = PlayerData(item.id ?: 0, item.isAdult)
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
                        GeneralUtils.showToast(requireContext(), "already copy url")
                    }
                }

                FunctionType.MSG -> {
                    // 點擊評論，進入播放頁面滾動到最下面
                    val playerData = PlayerData(item.id ?: 0, item.isAdult)
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
}