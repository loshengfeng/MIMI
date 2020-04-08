package com.dabenxiang.mimi.view.base

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController

abstract class BaseFragment2<out VM : BaseViewModel2> : BaseFragment() {

    abstract fun fetchViewModel(): VM?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fetchViewModel()?.navigateDestination?.observe(this, Observer { item ->
            findNavController().also { navController ->
                when (item) {
                    NavigateItem.Clean -> { }
                    NavigateItem.Up -> navController.navigateUp() //.popBackStack()
                    is NavigateItem.PopBackStack -> navController.popBackStack(item.fragmentId, item.inclusive)
                    is NavigateItem.Destination -> {
                        if (item.bundle == null) {
                            navController.navigate(item.action)
                        } else {
                            navController.navigate(item.action, item.bundle)
                        }
                    }
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchViewModel()?.also { viewModel ->
            viewModel.processing.observe(viewLifecycleOwner, Observer {
                if (it) {
                    progressHUD?.show()
                } else {
                    progressHUD?.dismiss()
                }
            })
        }
    }
}
