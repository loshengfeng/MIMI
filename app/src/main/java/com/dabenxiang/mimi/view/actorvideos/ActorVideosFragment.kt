package com.dabenxiang.mimi.view.actorvideos

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ActorVideosItem
import com.dabenxiang.mimi.view.base.BaseFragment

class ActorVideosFragment : BaseFragment() {
    companion object {
        const val KEY_DATA = "data"

        fun createBundle(
            item: ActorVideosItem? = null
        ): Bundle {

            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    private val viewModel: ActorVideosViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_actor_videos
    }

    override fun setupFirstTime() {
        super.setupFirstTime()
    }

    override fun initSettings() {
        super.initSettings()
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {

    }

}