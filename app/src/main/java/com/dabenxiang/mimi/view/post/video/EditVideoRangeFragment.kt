package com.dabenxiang.mimi.view.post.video

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.EditVideoListener
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.FileUtil
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.video.trimmer.interfaces.OnTrimVideoListener
import com.video.trimmer.interfaces.OnVideoListener
import kotlinx.android.synthetic.main.fragment_edit_video_range.*
import timber.log.Timber


class EditVideoRangeFragment : BaseFragment(), OnTrimVideoListener, OnVideoListener {

    private var editVideoListener: EditVideoListener? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId(): Int {
        return R.layout.fragment_edit_video_range
    }

    companion object {
        private const val BUNDLE_URI = "bundle_uri"

        fun newInstance(uri: String): EditVideoRangeFragment {
            val fragment =
                EditVideoRangeFragment()
            val args = Bundle()
            args.putString(BUNDLE_URI, uri)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()

        val uri = arguments?.getString(BUNDLE_URI)

        FileUtil.getClipFile("")

        videoTrimmer.setTextTimeSelectionTypeface(null)
            .setOnTrimVideoListener(this)
            .setOnVideoListener(this)
            .setVideoURI(Uri.parse(uri))
            .setVideoInformationVisibility(true)
            .setMaxDuration(15)
            .setMinDuration(3)
            .setDestinationPath("${FileUtil.getAppPath(App.applicationContext())}/clip")
    }

    fun save() {
        videoTrimmer.onSaveClicked()
    }

    fun setEditVideoListener(editVideoListener: EditVideoListener) {
        this.editVideoListener = editVideoListener
    }

    override fun setupObservers() {
    }

    override fun setupListeners() {
    }

    override fun cancelAction() {
    }

    override fun getResult(uri: Uri) {
        editVideoListener?.onFinish(uri)
    }

    override fun onError(message: String) {
        val deviceNotSupportStr = getString(R.string.device_not_support)

        if (message == deviceNotSupportStr) {
            GeneralUtils.showToast(
                requireContext(),
                getString(R.string.device_not_support)
            )
        } else {
            editVideoListener?.onError(message)
        }
    }

    override fun onTrimStarted() {
    }

    override fun onVideoPrepared() {
        editVideoListener?.onStart()
    }
}