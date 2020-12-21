package com.dabenxiang.mimi.view.post.video

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.EditVideoListener
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.FileUtil
import com.video.trimmer.interfaces.OnCropVideoListener
import kotlinx.android.synthetic.main.fragment_crop_video.*
import java.io.File


class CropVideoFragment : BaseFragment(), OnCropVideoListener {

    private var editVideoListener: EditVideoListener? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId(): Int {
        return R.layout.fragment_crop_video
    }

    companion object {
        private const val BUNDLE_URI = "bundle_uri"

        fun newInstance(uri: String): CropVideoFragment {
            val fragment =
                CropVideoFragment()
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

        FileUtil.getTakePhoto("")

        videoCropper.setVideoURI(Uri.parse(uri))
            .setOnCropVideoListener(this)
            .setMinMaxRatios(0.3f, 3f)
            .setDestinationPath("${FileUtil.getAppPath(App.applicationContext())}/pic")
    }

    fun save() {
        videoCropper.onSaveClicked()
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

    override fun onCropStarted() {
        editVideoListener?.onStart()
    }

    override fun onError(message: String) {
    }

    override fun onProgress(progress: Float) {
    }
}