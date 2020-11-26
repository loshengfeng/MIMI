package com.dabenxiang.mimi.view.club.member

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


/**
 * For fixing java.lang.IndexOutOfBoundsException: Inconsistency detected.
 */
class MiMiLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {
    override fun onLayoutChildren(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }
}