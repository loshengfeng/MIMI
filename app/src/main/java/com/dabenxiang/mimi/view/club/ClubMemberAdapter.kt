package com.dabenxiang.mimi.view.club

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.viewHolder.AdHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils

class ClubMemberAdapter(
    val context: Context,
    private val clubFuncItem: ClubFuncItem
) : PagedListAdapter<MemberClubItem, BaseViewHolder>(diffCallback) {

    companion object {
        const val VIEW_TYPE_AD = 0
        const val VIEW_TYPE_CLUB = 1
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
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item?.type == PostType.AD) {
            VIEW_TYPE_AD
        } else {
            VIEW_TYPE_CLUB
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_AD -> {
                AdHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_ad, parent, false)
                )
            }
            else -> {
                ClubMemberViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_club_member, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is AdHolder -> {
                Glide.with(context).load(item?.adItem?.href).into(holder.adImg)
                holder.adImg.setOnClickListener { view ->
                    GeneralUtils.openWebView(view.context, item?.adItem?.target ?: "")
                }
            }
            is ClubMemberViewHolder -> {
                item?.also { holder.onBind(it, clubFuncItem, position) }
            }
        }
    }
}