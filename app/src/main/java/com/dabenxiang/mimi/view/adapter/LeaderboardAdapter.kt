package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import kotlinx.android.synthetic.main.item_leaderboard.view.*

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 10
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.itemView.tv_leaderboard.text = "TOP${position + 1}"
    }
}

class LeaderboardViewHolder(view: View) : RecyclerView.ViewHolder(view)