package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.CarouselHolderItem
import com.dabenxiang.mimi.view.home.CarouselViewHolder
import com.dabenxiang.mimi.view.home.HomeTemplate

class CarouselAdapter(private val nestedListener: HomeAdapter.EventListener) : RecyclerView.Adapter<CarouselViewHolder>() {

    private var data: List<CarouselHolderItem>? = null

    fun setDataSrc(src: HomeTemplate.Carousel) {
        data = src.carouselList

        updated()
    }

    private fun updated() {

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CarouselViewHolder(layoutInflater.inflate(R.layout.nested_item_carousel, parent, false), nestedListener)
    }

    override fun getItemCount(): Int {
        return data?.count() ?: 0
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        data?.also { it ->
            holder.bind(it[position])
        }
    }
}