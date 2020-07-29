package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.PostPicItemListener
import com.dabenxiang.mimi.model.vo.PostAttachmentItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.item_add_pic.view.*
import kotlinx.android.synthetic.main.item_pic.view.*

class ScrollPicAdapter(private val postPicItemListener: PostPicItemListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ADD = 0
        private const val TYPE_IMG = 1
        private const val PIC_LIMIT = 10
    }

    private val attachmentList = arrayListOf<PostAttachmentItem>()
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context

        when (viewType) {
            TYPE_ADD -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_pic, parent, false)
                return AddViewHolder(view)
            }
            TYPE_IMG -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pic, parent, false)
                return PicViewHolder(view)
            }
        }

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_pic, parent, false)
        return AddViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (attachmentList.size >= PIC_LIMIT) {
            PIC_LIMIT
        } else {
            attachmentList.size + 1
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (attachmentList.size >= PIC_LIMIT) {
            TYPE_IMG
        } else {
            return when(position) {
                0 ->  TYPE_ADD
                else ->  TYPE_IMG
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (attachmentList.size >= PIC_LIMIT) {
            holder as PicViewHolder
            holder.bind(attachmentList[(position)], position)
        } else {
            when(position) {
                0 -> {
                    holder as AddViewHolder
                    holder.bind()
                }
                else -> {
                    holder as PicViewHolder
                    holder.bind(attachmentList[(position - 1)], position)
                }
            }
        }
    }

    fun submitList(uriList: ArrayList<PostAttachmentItem>) {
        this.attachmentList.addAll(uriList)
        notifyDataSetChanged()
    }

    fun getData(): ArrayList<PostAttachmentItem> {
        return attachmentList
    }

    inner class AddViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val pic = itemView.iv_pic
        private val addStr = itemView.txt_add
        private val imgAdd = itemView.iv_add

        fun bind() {
            if (attachmentList.isEmpty()) {
                pic.visibility = View.VISIBLE
                addStr.visibility = View.VISIBLE
                imgAdd.visibility = View.GONE
            } else {
                pic.visibility = View.GONE
                addStr.visibility = View.GONE
                imgAdd.visibility = View.VISIBLE
            }

            itemView.setOnClickListener {
                postPicItemListener.onAddPic()
            }
        }
    }

    inner class PicViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val imgPic = itemView.img_pic
        private val close = itemView.iv_close

        fun bind(item: PostAttachmentItem, position: Int) {
            if (item.attachmentId.isEmpty()) {
                val uriP = Uri.parse(item.uri)
                imgPic.setImageURI(uriP)
            } else {
                if (LruCacheUtils.getLruCache(item.attachmentId) == null) {
                    postPicItemListener.getBitmap(item.attachmentId) { id ->
                        val bitmap = LruCacheUtils.getLruCache(id)
                        Glide.with(context).load(bitmap).into(imgPic)
                    }
                } else {
                    val bitmap = LruCacheUtils.getLruCache(item.attachmentId)
                    Glide.with(context).load(bitmap).into(imgPic)
                }
            }

            close.setOnClickListener {
                postPicItemListener.onDelete(item)
                if (attachmentList.size >= PIC_LIMIT) {
                    attachmentList.removeAt(position)
                } else {
                    attachmentList.removeAt(position - 1)
                }
                postPicItemListener.onUpdate()
                notifyDataSetChanged()
            }
        }
    }
}