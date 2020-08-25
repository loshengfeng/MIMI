package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.vo.CarouselHolderItem
import com.dabenxiang.mimi.view.home.viewholder.CarouselViewHolder
import timber.log.Timber

class CarouselAdapter(
    private val nestedListener: HomeAdapter.EventListener,
    private val isAdult: Boolean,
    private val memberPostFuncItem: MemberPostFuncItem
) : ListAdapter<CarouselHolderItem, CarouselViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<CarouselHolderItem>() {
                override fun areItemsTheSame(
                    oldItem: CarouselHolderItem,
                    newItem: CarouselHolderItem
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: CarouselHolderItem,
                    newItem: CarouselHolderItem
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CarouselViewHolder(
            layoutInflater.inflate(
                R.layout.nested_item_carousel,
                parent,
                false
            ), nestedListener, isAdult, memberPostFuncItem
        )
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}