package com.dabenxiang.mimi.view.club.member

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.viewHolder.AdHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.club.adapter.ClubFuncItem
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.view.home.club.ClubDataSource
import kotlinx.android.synthetic.main.item_list_loading.view.*

class ClubMemberAdapter(
    val context: Context,
    private val clubFuncItem: ClubFuncItem
) : PagedListAdapter<MemberClubItem, BaseViewHolder>(diffCallback) {

    companion object {
        const val VIEW_TYPE_AD = 0
        const val VIEW_TYPE_CLUB = 1
        const val VIEW_TYPE_FOOTER = 2
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

    var totalCount = 0

    override fun getItemCount(): Int {
        return if(currentList?.size ?: 0 >= ClubDataSource.PER_LIMIT) {
            super.getItemCount() + 1
        } else {
            super.getItemCount()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < currentList?.size ?: 0) {
            val item = getItem(position)
            if (item?.type == PostType.AD) {
                VIEW_TYPE_AD
            } else {
                VIEW_TYPE_CLUB
            }
        } else {
            VIEW_TYPE_FOOTER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_AD -> {
                AdHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_ad, parent, false)
                )
            }
            VIEW_TYPE_CLUB -> {
                ClubMemberViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_club_member, parent, false)
                )
            }
            else -> {
                ListLoadingViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_list_loading, parent, false))
            }
        }
    }

    class ListLoadingViewHolder(view: View) : BaseViewHolder(view) {
        val clLoading: ConstraintLayout = view.cl_loading
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is AdHolder -> {
                val item = getItem(position)
                Glide.with(context).load(item?.adItem?.href).into(holder.adImg)
                holder.adImg.setOnClickListener { view ->
                    GeneralUtils.openWebView(view.context, item?.adItem?.target ?: "")
                }
            }
            is ClubMemberViewHolder -> {
                val item = getItem(position)
                item?.also { holder.onBind(it, clubFuncItem, position) }
            }
            is ListLoadingViewHolder -> {
                if (position >= totalCount) holder.clLoading.visibility = View.GONE
            }
        }
    }
}