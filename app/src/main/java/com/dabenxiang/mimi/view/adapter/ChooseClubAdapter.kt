package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.ClubListener
import com.dabenxiang.mimi.callback.PostAttachmentListener
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_choose_club.view.*

class ChooseClubAdapter(
    private val attachmentListener: PostAttachmentListener,
    private val clubListener: ClubListener
) : RecyclerView.Adapter<ChooseClubAdapter.ChooseClubViewHolder>() {

    private lateinit var context: Context

    private val data: ArrayList<MemberClubItem> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseClubViewHolder {
        context = parent.context

        return ChooseClubViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_choose_club, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ChooseClubViewHolder, position: Int) {
        val item = data[position]

        holder.clubName.text = item.title
        holder.hashTag.text = item.tag

        holder.rootLayout.setOnClickListener {
            clubListener.onClick(item)
        }

        attachmentListener.getAttachment(item.avatarAttachmentId, holder.avatar)
    }

    fun updateData(data: ArrayList<MemberClubItem>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    class ChooseClubViewHolder(itemView: View) : BaseViewHolder(itemView) {
        val rootLayout: ConstraintLayout = itemView.rootLayout
        val avatar: ImageView = itemView.iv_avatar
        val clubName: TextView = itemView.txt_clubName
        val hashTag: TextView = itemView.txt_hashtagName
    }

    override fun getItemCount(): Int {
        return data.size
    }
}