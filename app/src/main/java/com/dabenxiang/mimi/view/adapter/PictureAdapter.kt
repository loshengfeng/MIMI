package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.callback.OnItemClickListener
import com.dabenxiang.mimi.model.api.vo.ImageItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.LoadImageUtils
import kotlinx.android.synthetic.main.item_picture.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

class PictureAdapter(
    private val context: Context,
    private val imageItems: ArrayList<ImageItem>,
    private val onItemClickListener: OnItemClickListener,
    private val viewModelScope: CoroutineScope
) : RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val mView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_picture, parent, false)
        return PictureViewHolder(mView)
    }

    override fun getItemCount(): Int {
        return imageItems.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder as PictureViewHolder
        val item = imageItems[position]
        holder.onBind(item, onItemClickListener, viewModelScope)
    }

    class PictureViewHolder(itemView: View) : BaseViewHolder(itemView) {
        val picture: ImageView = itemView.iv_picture

        fun onBind(
            item: ImageItem,
            onItemClickListener: OnItemClickListener,
            viewModelScope: CoroutineScope
        ) {
            if (!TextUtils.isEmpty(item.url)) {
                Glide.with(picture.context)
                    .load(item.url).into(picture)
            } else {
                viewModelScope.launch {
                    LoadImageUtils.loadImage(item.id.toLongOrNull(), picture, LoadImageType.PICTURE_EMPTY)
                }
            }

            picture.setOnClickListener {
                onItemClickListener.onItemClick()
            }
        }
    }

}
