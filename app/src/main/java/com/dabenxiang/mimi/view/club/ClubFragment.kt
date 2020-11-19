package com.dabenxiang.mimi.view.club

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_club.*
import org.koin.android.ext.android.get

class ClubFragment : BaseFragment() {

    private val viewModel: ClubViewModel by viewModels()

    override fun getLayoutId() = R.layout.fragment_club
    override fun setupObservers() {
        TODO("Not yet implemented")
    }

    override fun setupListeners() {
        TODO("Not yet implemented")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title.text = getString(R.string.nav_club)
    }
}
