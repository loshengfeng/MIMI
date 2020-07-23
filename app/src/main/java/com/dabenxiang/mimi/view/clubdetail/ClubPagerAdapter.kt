package com.dabenxiang.mimi.view.clubdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.AttachmentListener

class ClubPagerAdapter(
    private val clubDetailFuncItem: ClubDetailFuncItem,
    private val attachmentListener: AttachmentListener,
    private val adultListener: AdultListener
) : RecyclerView.Adapter<ClubPagerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClubPagerViewHolder {
        return ClubPagerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_club_pager, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: ClubPagerViewHolder, position: Int) {
        holder.onBind(position, clubDetailFuncItem, attachmentListener, adultListener)
    }
}