package com.dabenxiang.mimi.view.chatcontent

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_chat_content.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class ChatContentFragment : BaseFragment() {

    private val viewModel by viewModel<ChatContentViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_chat_content
    }

    override fun setupObservers() {
        Timber.d("${ChatContentFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${ChatContentFragment::class.java.simpleName}_setupListeners")

        btnClose.setOnClickListener {
            Navigation.findNavController(requireView()).navigateUp()
        }

        recyclerContent.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }
}