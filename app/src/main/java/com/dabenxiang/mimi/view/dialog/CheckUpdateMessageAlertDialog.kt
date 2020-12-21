package com.dabenxiang.mimi.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.listener.OnSimpleDialogListener
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.dialog_update_message_alert.*
import kotlinx.android.synthetic.main.fragment_splash.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import tw.gov.president.manager.submanager.update.callback.DownloadProgressCallback


class CheckUpdateMessageAlertDialog(
    context: Context,
    private val updateApp: ((DownloadProgressCallback) -> Unit) = { _ -> },
    private val dialogListener: OnSimpleDialogListener
) : Dialog(context, R.style.dialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_update_message_alert_with_progress)
        tv_msg.setText(R.string.updated_version_title)
        btn_confirm.setText(R.string.update_immediately )
        btn_confirm.setOnClickListener {
            pb_update.visibility = View.VISIBLE
            btn_confirm.visibility = View.GONE
            btn_cancel.visibility = View.GONE
            updateApp(progressCallback)
        }

        setCancelable(false)
        btn_cancel.setText(R.string.btn_close)

        btn_cancel.setOnClickListener {
            dismiss()
            dialogListener.onCancel()
        }
    }

    private val progressCallback = object :
        DownloadProgressCallback {
        override fun schedule(longId: Long, totalSize: Int, currentSize: Int, status: Int) {
            Timber.d("progress: ${((currentSize.toFloat() / totalSize.toFloat()) * 100).toInt()}")
            pb_update.progress = ((currentSize.toFloat() / totalSize.toFloat()) * 100).toInt()
            CoroutineScope(Dispatchers.Main).launch {
                tv_msg.text =
                    String.format(
                        context.getString(R.string.update_version),
                        ((currentSize.toFloat() / totalSize.toFloat()) * 100).toInt()
                    )
            }
        }

        override fun complete(longId: Long, path: String, mimeType: String) {
            Timber.d("complete path: $path, mimeType: $mimeType")
            tv_msg.text = String.format(context.getString(R.string.update_version), 100)
            pb_update.progress = 100

            CoroutineScope(Dispatchers.Main).launch {
                tv_msg.text =
                    String.format(context.getString(R.string.update_version), 100)
            }
            GeneralUtils.installApk(App.applicationContext(), path)
            dismiss()
        }
    }
}