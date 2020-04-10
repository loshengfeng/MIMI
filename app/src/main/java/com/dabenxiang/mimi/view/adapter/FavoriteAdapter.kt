package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.FavoriteItem
import com.dabenxiang.mimi.view.favorite.FavoriteViewHolder
import com.dabenxiang.mimi.view.listener.AdapterEventListener
import timber.log.Timber

class FavoriteAdapter(
    private val listener: AdapterEventListener<FavoriteItem>
) : RecyclerView.Adapter<FavoriteViewHolder>() {
    private var data: List<FavoriteItem>? = null

    fun setDataSrc(itemList: List<FavoriteItem>) {
        data = itemList
        updated()
    }

    private fun updated() {
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        Timber.d("${FavoriteAdapter::class.java.simpleName}_getItemCount")
        return 10
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        Timber.d("${FavoriteAdapter::class.java.simpleName}_onBindViewHolder")
        val item = data?.get(position)
        holder.bind(item)
    }
}