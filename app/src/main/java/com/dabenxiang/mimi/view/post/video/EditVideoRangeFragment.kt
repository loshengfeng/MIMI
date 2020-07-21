package com.dabenxiang.mimi.view.post.video

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.EditVideoListener
import com.dabenxiang.mimi.view.base.BaseFragment
import com.video.trimmer.interfaces.OnTrimVideoListener
import com.video.trimmer.interfaces.OnVideoListener
import kotlinx.android.synthetic.main.fragment_edit_video_range.*
import java.io.File


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

        videoTrimmer.setTextTimeSelectionTypeface(null)
            .setOnTrimVideoListener(this)
            .setOnVideoListener(this)
            .setVideoURI(Uri.parse(uri))
            .setVideoInformationVisibility(true)
            .setMaxDuration(10)
            .setMinDuration(2)
            .setDestinationPath(Environment.getExternalStorageDirectory().toString() + File.separator + "temp" + File.separator + "Videos" + File.separator)
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
    }

    override fun onTrimStarted() {
    }

    override fun onVideoPrepared() {
        editVideoListener?.onStart()
    }
}