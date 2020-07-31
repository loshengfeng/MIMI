package com.dabenxiang.mimi.view.adapter.viewHolder

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.EditVideoAdapterListener
import com.dabenxiang.mimi.callback.PostVideoItemListener
import com.dabenxiang.mimi.model.vo.PostVideoAttachment
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
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
            if (postVideoAttachment.picAttachmentId.isEmpty()) {
                val uriP = Uri.parse(postVideoAttachment.picUrl)
                imgPic.setImageURI(uriP)
            } else {
                if (LruCacheUtils.getLruCache(postVideoAttachment.picAttachmentId) == null) {
                    listener.getBitmap(postVideoAttachment.picAttachmentId) { id ->
                        val bitmap = LruCacheUtils.getLruCache(id)
                        Glide.with(context).load(bitmap).into(imgPic)
                    }
                } else {
                    val bitmap = LruCacheUtils.getLruCache(postVideoAttachment.picAttachmentId)
                    Glide.with(context).load(bitmap).into(imgPic)
                }
            }

            close.setOnClickListener {
                listener.onDelete(postVideoAttachment)
                uriList.removeAt(position)
                notifyDataSetChanged()
            }
        }
    }
}