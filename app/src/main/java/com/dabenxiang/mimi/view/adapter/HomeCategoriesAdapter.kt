package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.home.HomeTemplate
import com.dabenxiang.mimi.view.home.VideoViewHolder
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope

class HomeCategoriesAdapter(private val nestedListener: HomeAdapter.EventListener, private val isAdult: Boolean) :
    RecyclerView.Adapter<BaseViewHolder>() {

    fun loadData(src: HomeTemplate.Categories) {
        reset()

        nestedListener.onLoadAdapter(this, src)
    }

    private var activeTask: Deferred<Any>? = null

    suspend fun activeTask(block: suspend () -> Any): Any {
        activeTask?.cancelAndJoin()

        return coroutineScope {
            val newTask = async {
                block()
            }

            newTask.invokeOnCompletion {
                activeTask = null
            }

            activeTask = newTask
            newTask.await()
        }
    }

    private val videoViewHolderListener by lazy {
        object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                if (index > -1) {
                    data?.get(index)?.also {
                        nestedListener.onVideoClick(view, PlayerData.parser(it).also { playerData ->
                            playerData.isAdult = isAdult
                        })
                    }
                }
            }
        }
    }

    private fun reset() {
        data = null

        notifyDataSetChanged()
    }

    private var data: List<BaseVideoItem.Video>? = null

    fun notifyUpdated(updated: List<BaseVideoItem.Video>?) {
        data = updated

        /*
        //Fake date:
        val list = mutableListOf<StatisticsItem>()
        repeat(12) {
            list.add(
                StatisticsItem(
                    title = "標題${it + 1}",
                    id = it.toLong(),
                    count = it.toLong(),
                    cover = "https://i2.kknews.cc/SIG=1nkii03/470400035pnr3n5r3s7n.jpg"
                )
            )
        }
        data = list
        */

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.nested_item_home_categories, parent, false)
        return VideoViewHolder(view, videoViewHolderListener)
    }

    override fun getItemCount(): Int {
        return data?.count() ?: 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder as VideoViewHolder

        var resetSuccess = false

        data?.also { data ->
            val item = data[position]
            holder.bind(item, position)
            resetSuccess = true
        }

        if (!resetSuccess) {
            holder.bind(null, -1)
        }
    }
}