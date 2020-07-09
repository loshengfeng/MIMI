package com.dabenxiang.mimi.view.clip

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.adapter.ClipAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_clip.*
import timber.log.Timber
import java.io.File


class ClipFragment : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"
        const val KEY_POSITION = "position"

        fun createBundle(
            items: ArrayList<MemberPostItem>,
            position: Int
        ): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, items)
                it.putInt(KEY_POSITION, position)
            }
        }
    }

    private val viewModel: ClipViewModel by viewModels()

    private val clipMap: HashMap<Long, File> = hashMapOf()
    private val coverMap: HashMap<Long, Bitmap> = hashMapOf()

    override val bottomNavigationVisibility = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_clip
    }

    override fun setupObservers() {
        viewModel.clipResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Success -> {
                    val result = it.result
                    clipMap[result.first] = result.third
                    rv_clip.adapter?.notifyItemChanged(result.second)
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })

        viewModel.coverResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Success -> {
                    rv_clip.adapter?.notifyItemChanged(it.result)
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })
    }

    override fun setupListeners() {

    }

    override fun initSettings() {
        (arguments?.getSerializable(KEY_DATA) as ArrayList<MemberPostItem>).also { data ->
            rv_clip.adapter = ClipAdapter(
                requireContext(),
                data,
                clipMap,
                0,
                { id, pos -> getClip(id, pos) },
                { id, pos -> getCover(id, pos) })
            (rv_clip.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            PagerSnapHelper().attachToRecyclerView(rv_clip)
            rv_clip.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            val position = (rv_clip.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            Timber.d("SCROLL_STATE_IDLE position: $position")
                            val clipAdapter = rv_clip.adapter as ClipAdapter
                            val lastPosition = clipAdapter.getCurrentPos()
                            takeIf { position != lastPosition }?.also {
                                clipAdapter.updateCurrentPosition(position)
                                clipAdapter.notifyItemChanged(lastPosition)
                                clipAdapter.notifyItemChanged(position)
                            }
                        }
                    }
                }
            })
        }
    }

    private fun getClip(id: Long, pos: Int) {
        Timber.d("getClip, id: $id, position: $pos")
        viewModel.getClip(id, pos)
    }

    private fun getCover(id: Long, pos: Int) {
        Timber.d("getCover, id: $id, position: $pos")
        viewModel.getCover(id, pos)
    }
}