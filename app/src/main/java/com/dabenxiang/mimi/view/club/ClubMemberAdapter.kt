package com.dabenxiang.mimi.view.club

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberClubItem

class ClubMemberAdapter(
    val context: Context,
    private val clubFuncItem: ClubFuncItem
) : PagedListAdapter<MemberClubItem, ClubMemberViewHolder>(diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<MemberClubItem>() {
            override fun areItemsTheSame(
                oldItem: MemberClubItem,
                newItem: MemberClubItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: MemberClubItem,
                newItem: MemberClubItem
            ): Boolean {
                return oldItem == newItem
            }
        }
        const val PAYLOAD_UPDATE_LIKE_AND_FOLLOW_UI = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClubMemberViewHolder {
        return ClubMemberViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_club_member, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ClubMemberViewHolder, position: Int) {
        val item = getItem(position)
        item?.also { holder.onBind(it, clubFuncItem, position) }
    }
}