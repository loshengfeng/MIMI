package com.dabenxiang.mimi.view.topuphistory

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.TopupHistoryAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_personal.tv_title
import kotlinx.android.synthetic.main.fragment_topup_history.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import kotlinx.android.synthetic.main.item_topup_history_no_data.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class TopupHistoryFragment : BaseFragment<TopupHistoryViewModel>() {
    private val viewModel by viewModel<TopupHistoryViewModel>()
    private var hasData = true

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_topup_history
    }

    override fun fetchViewModel(): TopupHistoryViewModel? {
        return viewModel
    }

    override fun setupObservers() {
        Timber.d("${TopupHistoryFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${TopupHistoryFragment::class.java.simpleName}_setupListeners")

        tl_type.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        GeneralUtils.showToast(context!!, "0")
                        rv_content.visibility = View.VISIBLE
                        item_no_data.visibility = View.GONE
                    }
                    1 -> {
                        GeneralUtils.showToast(context!!, "1")
                        rv_content.visibility = View.GONE
                        item_no_data.visibility = View.VISIBLE
                    }
                    2 -> {
                        GeneralUtils.showToast(context!!, "2")
                        rv_content.visibility = View.VISIBLE
                        item_no_data.visibility = View.GONE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_back -> Navigation.findNavController(view!!).navigateUp()
//                R.id.xxx -> Navigation.findNavController(view!!).navigate(R.id.action_settingFragment_to_resendMailFragment)
            }
        }.also {
            tv_back.setOnClickListener(it)
        }
    }

    override fun initSettings() {
        tv_title.text = getString(R.string.personal_topoup_history)
        when(hasData) {
            true -> {
                item_no_data.visibility = View.VISIBLE
                item_no_data.visibility = View.GONE
            }
            false -> {
                item_no_data.visibility = View.GONE
                item_no_data.visibility = View.VISIBLE
            }
        }
        tv_text.text = "提示字提示字"

        activity?.also { activity ->
            LinearLayoutManager(activity).also { layoutManager ->
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                rv_content.layoutManager = layoutManager
            }
        }
        rv_content.adapter = TopupHistoryAdapter()
    }
}