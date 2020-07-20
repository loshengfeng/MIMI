package com.dabenxiang.mimi.view.post

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.video.trimmer.interfaces.OnCropVideoListener
import kotlinx.android.synthetic.main.fragment_crop_video.*
import java.io.File


class CropVideoFragment : BaseFragment(), OnCropVideoListener {

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId(): Int {
        return R.layout.fragment_crop_video
    }

    companion object {
        private const val BUNDLE_URI = "bundle_uri"

        fun newInstance(uri: String): CropVideoFragment {
            val fragment = CropVideoFragment()
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

        videoCropper.setVideoURI(Uri.parse(uri))
            .setOnCropVideoListener(this)
            .setMinMaxRatios(0.3f, 3f)
            .setDestinationPath(Environment.getExternalStorageDirectory().toString() + File.separator + "temp" + File.separator + "Videos" + File.separator)
    }

    fun save() {
        videoCropper.onSaveClicked()
    }

    override fun setupObservers() {
    }

    override fun setupListeners() {

    }

    override fun cancelAction() {
    }

    override fun getResult(uri: Uri) {
    }

    override fun onCropStarted() {
    }

    override fun onError(message: String) {
    }

    override fun onProgress(progress: Float) {
    }
}