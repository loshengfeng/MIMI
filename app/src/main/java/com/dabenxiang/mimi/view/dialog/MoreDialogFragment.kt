package com.dabenxiang.mimi.view.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.MembersPostCommentItem
import com.dabenxiang.mimi.model.db.MiMiDB
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import kotlinx.android.synthetic.main.fragment_dialog_more.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import timber.log.Timber

class MoreDialogFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(
            item: BaseMemberPostItem,
            listener: OnMoreDialogListener,
            isComment: Boolean? = false,
            isLogin: Boolean = false,
            isFromPostPage: Boolean = false
        ): MoreDialogFragment {
            val fragment = MoreDialogFragment()
            fragment.baseMemberPostItem = item
            fragment.listener = listener
            fragment.isComment = isComment
            fragment.isLogin = isLogin
            fragment.isFromPostPage = isFromPostPage
            return fragment
        }
    }
    private val viewModel :MoreDialogViewModel by viewModels()
    private var baseMemberPostItem: BaseMemberPostItem? = null
    private var isComment: Boolean? = false
    private var listener: OnMoreDialogListener? = null
    private var isLogin: Boolean = false
    private var isFromPostPage: Boolean = false

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_more
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.memberPostItemResult.observe(viewLifecycleOwner){ dbItem->
            Timber.i("MoreDialogFragment dbItem: $dbItem")
            dbItem?.let {
                initUI(dbItem)
            } ?: run {
                baseMemberPostItem?.let { baseMemberPostItem ->
                    initUI(baseMemberPostItem)
                }
            }

        }

        when (baseMemberPostItem) {
            is MemberPostItem ->  viewModel.getDBItem((baseMemberPostItem as MemberPostItem).id)
            else -> {
                initUI(baseMemberPostItem as MembersPostCommentItem)
            }
        }

    }

    private fun initUI(item: BaseMemberPostItem){
        val isReport = when (item) {
            is MemberPostItem -> item.reported
            else -> (item as MembersPostCommentItem).reported
        } ?: false

        val deducted = if ((item is MemberPostItem)) {
            (item as MemberPostItem).deducted
        } else {
            true
        }
        Timber.i("MoreDialogFragment item: $item")
        Timber.i("MoreDialogFragment isReported: $isReport")
        Timber.i("MoreDialogFragment deducted: $deducted")

        if (isFromPostPage) {
            if (!isLogin || isReport || !deducted) {
                tv_problem_report.setTextColor(requireContext().getColor(R.color.color_black_1_50))
            } else {
                tv_problem_report.setTextColor(requireContext().getColor(R.color.color_black_1))
                tv_problem_report.setOnClickListener {
                    listener?.onProblemReport(item!!, isComment!!)
                }
            }
        } else {
            if (isReport || !deducted) {
                tv_problem_report.setTextColor(requireContext().getColor(R.color.color_black_1_50))
            } else {
                tv_problem_report.setTextColor(requireContext().getColor(R.color.color_black_1))
                tv_problem_report.setOnClickListener {
                    listener?.onProblemReport(item!!, isComment!!)
                }
            }
        }

        tv_cancel.setOnClickListener {
            listener?.onCancel()
        }

        background.setOnClickListener {
            listener?.onCancel()
        }
    }

    interface OnMoreDialogListener {
        fun onProblemReport(item: BaseMemberPostItem, isComment: Boolean)
        fun onCancel()
    }
}

class MoreDialogViewModel : BaseViewModel() {
    private val _memberPostItemResult = MutableLiveData<MemberPostItem?>()
    val memberPostItemResult: LiveData<MemberPostItem?> = _memberPostItemResult

    fun getDBItem(id:Long){
        viewModelScope.launch {
            mimiDB.withTransaction {
                mimiDB.postDBItemDao().getMemberPostItemById(id)?.let {
                    _memberPostItemResult.postValue(it)
                } ?: run {
                    _memberPostItemResult.postValue(null)
                }
            }
        }
    }
}