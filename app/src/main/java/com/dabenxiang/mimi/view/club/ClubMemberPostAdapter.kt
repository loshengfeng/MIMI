package com.dabenxiang.mimi.view.club

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.PostItem

class ClubMemberPostAdapter(
    val context: Context,
    private val postItemList: ArrayList<PostItem>,
    private val clubItem: MemberClubItem,
    private val clubFuncItem: ClubFuncItem
) : PagedListAdapter<PostItem, ClubMemberPostViewHolder>(diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<PostItem>() {
            override fun areItemsTheSame(
                oldItem: PostItem,
                newItem: PostItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: PostItem,
                newItem: PostItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun getItemCount(): Int {
        return postItemList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClubMemberPostViewHolder {
        return ClubMemberPostViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_club_member_post, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ClubMemberPostViewHolder, position: Int) {
        val item = postItemList[position]
        holder.onBind(item, clubItem, clubFuncItem)
    }
}