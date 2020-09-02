package com.dabenxiang.mimi.view.adapter.viewHolder

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.PostVideoItemListener
import com.dabenxiang.mimi.model.vo.PostVideoAttachment
import com.dabenxiang.mimi.model.vo.ViewerItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_pic.view.*

class ScrollVideoAdapter(private val listener: PostVideoItemListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ADD = 0
        private const val TYPE_VIDEO = 1
        private const val VIDEO_LIMIT = 1
    }

    private val uriList = arrayListOf<PostVideoAttachment>()
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context

        when (viewType) {
            TYPE_ADD -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_video, parent, false)
                return AddViewHolder(view)
            }
            TYPE_VIDEO -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
                return PicViewHolder(view)
            }
        }

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_pic, parent, false)
        return AddViewHolder(view)
    }

    override fun getItemCount(): Int = VIDEO_LIMIT

    override fun getItemViewType(position: Int): Int {
        return if (uriList.isEmpty()) {
            TYPE_ADD
        } else {
            TYPE_VIDEO
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (uriList.isEmpty()) {
            holder as AddViewHolder
            holder.bind()
        } else {
            holder as PicViewHolder
            holder.bind(uriList[(position)], position)
        }
    }

    fun submitList(uriList: ArrayList<PostVideoAttachment>) {
        this.uriList.addAll(uriList)
        notifyDataSetChanged()
    }

    fun getData(): ArrayList<PostVideoAttachment> {
        return uriList
    }

    inner class AddViewHolder(itemView: View) : BaseViewHolder(itemView) {

        fun bind() {
            itemView.setOnClickListener {
                listener.onOpenRecorder()
            }
        }
    }

    inner class PicViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val imgPic = itemView.img_pic
        private val close = itemView.iv_close

        fun bind(postVideoAttachment: PostVideoAttachment, position: Int) {
            val viewItem = ViewerItem()

            if (postVideoAttachment.picAttachmentId.isEmpty()) {
                val uriP = Uri.parse(postVideoAttachment.picUrl)
                imgPic.setImageURI(uriP)
                viewItem.url = postVideoAttachment.picUrl
            } else {
                listener.getBitmap(postVideoAttachment.picAttachmentId.toLongOrNull(),imgPic)
                viewItem.attachmentId = postVideoAttachment.picAttachmentId
            }

            close.setOnClickListener {
                listener.onDelete(postVideoAttachment)
                uriList.removeAt(position)
                notifyDataSetChanged()
            }

            itemView.setOnClickListener {
                listener.onViewer(viewItem)
            }
        }
    }
}