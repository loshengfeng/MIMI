package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.picturepost.PicturePostHolder

class CommonPagedAdapter(
    val context: Context
) : PagedListAdapter<MemberPostItem, BaseViewHolder>(diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<MemberPostItem>() {
            override fun areItemsTheSame(
                oldItem: MemberPostItem,
                newItem: MemberPostItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: MemberPostItem,
                newItem: MemberPostItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    private var adultTabType: AdultTabType = AdultTabType.FOLLOW

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (adultTabType) {
            AdultTabType.PICTURE -> {
                PicturePostHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_picture_post, parent, false)
                )
            }
            else -> {
                //TODO:
                PicturePostHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_picture_post, parent, false)
                )
            }
        }

    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val memberPostItem = getItem(position)

    }

    fun setupAdultTabType(type: AdultTabType) {
        adultTabType = type
    }
}

//class MemberPostHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//    val textOrderNo: TextView = itemView.text_order_no
//    val textTransferAccount: TextView = itemView.text_transfer_account
//    val textTargetAccount: TextView = itemView.text_target_account
//    val textAmount: TextView = itemView.text_amount
//    val textFee: TextView = itemView.text_fee
//    val textDate: TextView = itemView.text_date
//}