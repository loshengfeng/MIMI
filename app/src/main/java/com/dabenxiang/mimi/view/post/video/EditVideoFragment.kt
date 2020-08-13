package com.dabenxiang.mimi.view.post.video

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.EditVideoListener
import com.dabenxiang.mimi.model.api.vo.MediaItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PicParameter
import com.dabenxiang.mimi.model.api.vo.VideoParameter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.post.video.PostVideoFragment.Companion.BUNDLE_COVER_URI
import com.dabenxiang.mimi.view.post.video.PostVideoFragment.Companion.BUNDLE_TRIMMER_URI
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
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

        useAdultTheme(false)
    }

    override fun setupObservers() {
    }

    override fun setupListeners() {
        tv_back.setOnClickListener {
            handleBackEvent()
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

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            handleBackEvent()
        }
    }

    private fun handleBackEvent() {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.whether_to_discard_content,
                messageIcon = R.drawable.ico_default_photo,
                firstBtn = getString(R.string.btn_cancel),
                secondBtn = getString(R.string.btn_confirm),
                isMessageIcon = false,
                secondBlock = {
                    findNavController().navigateUp()
                }
            )
        ).show(requireActivity().supportFragmentManager)
    }

    override fun initSettings() {
        super.initSettings()

        tv_title.text = getString(R.string.post_title)
        tv_clean.visibility = View.VISIBLE
        tv_clean.text = getString(R.string.btn_send)

        val img = requireContext().getDrawable(R.drawable.btn_close_n)
        tv_back.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null)
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

            val isEdit = arguments?.getBoolean(MyPostFragment.EDIT)

            if (isEdit != null && isEdit) {
                val item = arguments?.getSerializable(MyPostFragment.MEMBER_DATA) as MemberPostItem
                val mediaItem = Gson().fromJson(item.content, MediaItem::class.java)
                mediaItem.videoParameter = VideoParameter()
                mediaItem.picParameter = ArrayList(mutableListOf(PicParameter()))
                item.content = Gson().toJson(mediaItem)

                bundle.putBoolean(MyPostFragment.EDIT, true)
                bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
                findNavController().navigate(R.id.action_editVideoFragment2_to_postVideoFragment, bundle)
            } else {
                findNavController().navigate(R.id.action_editVideoFragment_to_postVideoFragment, bundle)
            }
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