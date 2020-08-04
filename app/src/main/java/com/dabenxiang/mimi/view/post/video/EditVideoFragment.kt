package com.dabenxiang.mimi.view.post.video

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.EditVideoListener
import com.dabenxiang.mimi.model.api.vo.PostMemberRequest
import com.dabenxiang.mimi.model.vo.PostVideoAttachment
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.post.video.PostVideoFragment.Companion.BUNDLE_COVER_URI
import com.dabenxiang.mimi.view.post.video.PostVideoFragment.Companion.BUNDLE_TRIMMER_URI
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_edit_video.*
import kotlinx.android.synthetic.main.item_setting_bar.*


class EditVideoFragment : BaseFragment() {

    private lateinit var editVideoFragmentPagerAdapter: EditVideoFragmentPagerAdapter
    private var videoUri = Uri.EMPTY
    private var isVideoRangeFinish = false
    private var isCropPicFinish = false

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId(): Int {
        return R.layout.fragment_edit_video
    }

    companion object {
        const val BUNDLE_VIDEO_URI = "bundle_video_uri"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
        val isNeedVideoUpload = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(PostVideoFragment.UPLOAD_VIDEO)

        if (isNeedVideoUpload?.value != null) {
            val memberRequest = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<PostMemberRequest>(PostVideoFragment.MEMBER_REQUEST)
            val videoAttachment = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<ArrayList<PostVideoAttachment>>(PostVideoFragment.VIDEO_DATA)

            findNavController().previousBackStackEntry?.savedStateHandle?.set(PostVideoFragment.UPLOAD_VIDEO, true)
            findNavController().previousBackStackEntry?.savedStateHandle?.set(PostVideoFragment.MEMBER_REQUEST, memberRequest?.value)
            findNavController().previousBackStackEntry?.savedStateHandle?.set(PostVideoFragment.VIDEO_DATA, videoAttachment?.value)
            findNavController().navigateUp()
        }

        val uri = arguments?.getString(BUNDLE_VIDEO_URI)
        editVideoFragmentPagerAdapter =
            EditVideoFragmentPagerAdapter(
                childFragmentManager,
                tabLayout!!.tabCount,
                uri!!
            )
        viewPager.adapter = editVideoFragmentPagerAdapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tv_clean.isEnabled = true
    }

    override fun setupObservers() {
    }

    override fun setupListeners() {
        tv_back.setOnClickListener {
            findNavController().popBackStack()
        }

        tv_clean.setOnClickListener {
            val editVideoRangeFragment = editVideoFragmentPagerAdapter.getFragment(0) as EditVideoRangeFragment
            editVideoRangeFragment.setEditVideoListener(editTrimmerVideoListener)
            editVideoRangeFragment.save()
        }

        tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager!!.currentItem = tab!!.position
            }
        })
    }

    override fun initSettings() {
        super.initSettings()

        tv_title.text = getString(R.string.post_title)
        tv_clean.visibility = View.VISIBLE
        tv_clean.text = getString(R.string.btn_send)
    }

    private val editTrimmerVideoListener = object : EditVideoListener {
        override fun onStart() {
            progressHUD?.show()
        }

        override fun onFinish(resourceUri: Uri) {
            isVideoRangeFinish = true

            dismissDialog()

            videoUri = resourceUri
            val cropVideoFragment = editVideoFragmentPagerAdapter.getFragment(1) as CropVideoFragment
            cropVideoFragment.setEditVideoListener(editCropVideoListener)
            cropVideoFragment.save()
        }
    }

    private val editCropVideoListener = object : EditVideoListener {
        override fun onStart() {
            progressHUD?.show()
        }

        override fun onFinish(resourceUri: Uri) {
            isCropPicFinish = true

            dismissDialog()

            val bundle = Bundle()
            bundle.putString(BUNDLE_TRIMMER_URI, videoUri.toString())
            bundle.putString(BUNDLE_COVER_URI, resourceUri.toString())
            findNavController().navigate(R.id.action_editVideoFragment_to_postVideoFragment, bundle)
        }
    }

    private fun dismissDialog() {
        if (isCropPicFinish && isVideoRangeFinish) {
            progressHUD?.dismiss()
            isCropPicFinish = false
            isVideoRangeFinish = false
        }
    }
}