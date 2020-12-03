package com.dabenxiang.mimi.view.actor

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.actor.ActorFragment.Companion.VIEW_TYPE_ACTOR_HEADER

class ActorHeaderAdapter(
    val title: String?,
    val context: Context
) : RecyclerView.Adapter<ActorHeaderViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorHeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actors_header, parent, false)
        return ActorHeaderViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onBindViewHolder(holder: ActorHeaderViewHolder, position: Int) {
        holder.title.text = title
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_ACTOR_HEADER
    }
}