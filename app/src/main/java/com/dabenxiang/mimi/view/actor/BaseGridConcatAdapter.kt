package com.dabenxiang.mimi.view.actor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import kotlinx.android.synthetic.main.item_actors_concat_row.view.*

class BaseGridConcatAdapter(private val context: Context, private val actorListAdapter: ActorListAdapter, private val spanCount:Int) :
    RecyclerView.Adapter<BaseConcatHolder<*>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseConcatHolder<*> {
        val view = LayoutInflater.from(context).inflate(R.layout.item_actors_concat_row,parent,false)
        view.rv_actor_concat.layoutManager = GridLayoutManager(context, spanCount)
        return ConcatViewHolder(view)
    }

    override fun getItemCount(): Int  = 1

    override fun onBindViewHolder(holder: BaseConcatHolder<*>, position: Int) {
        when(holder){
            is ConcatViewHolder -> holder.bind(actorListAdapter)
            else -> throw IllegalArgumentException("No viewholder to show this data, did you forgot to add it to the onBindViewHolder?")
        }
    }

    inner class ConcatViewHolder(itemView: View): BaseConcatHolder<ActorListAdapter>(itemView){
        override fun bind(adapter: ActorListAdapter) {
            itemView.rv_actor_concat.adapter = adapter
        }
    }
}