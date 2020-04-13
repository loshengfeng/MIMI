package com.dabenxiang.mimi.widget.utility

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.view.OrientationEventListener
import java.lang.ref.WeakReference

class OrientationDetector : OrientationEventListener {
    interface OnChangeListener {
        fun onChanged(requestedOrientation: Int)
    }

    companion object {
        private const val MAX_CHECK_INTERVAL: Long = 3000
    }

    constructor(context: Context) : super(context) {
        mContextRef = WeakReference(context)
    }

    constructor(context: Context, rate: Int) : super(context, rate) {
        mContextRef = WeakReference(context)
    }

    private var mContextRef: WeakReference<Context>
    private var mIsSupportGravity = false
    private var mLastCheckTimestamp: Long = 0
    private var mChangeListener: OnChangeListener? = null

    var currOrientation = ORIENTATION_UNKNOWN
        private set

    fun setChangeListener(listener: OnChangeListener?) {
        mChangeListener = listener
    }

    private val isScreenAutoRotate: Boolean
        private get() {
            val context = mContextRef.get() ?: return false
            var gravity = 0
            try {
                gravity = Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.ACCELEROMETER_ROTATION
                )
            } catch (e: SettingNotFoundException) {
                e.printStackTrace()
            }
            return gravity == 1
        }

    @SuppressLint("WrongConstant")
    override fun onOrientationChanged(orientation: Int) {
        val context = mContextRef.get()
        if (context == null || context !is Activity) {
            return
        }
        val currTimestamp = System.currentTimeMillis()
        if (currTimestamp - mLastCheckTimestamp > MAX_CHECK_INTERVAL) {
            mIsSupportGravity = isScreenAutoRotate
            mLastCheckTimestamp = currTimestamp
        }
        if (!mIsSupportGravity) {
            return
        }
        if (orientation == ORIENTATION_UNKNOWN) {
            return
        }
        var requestOrientation = ORIENTATION_UNKNOWN
        requestOrientation = if (orientation > 350 || orientation < 10) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else if (orientation in 81..99) {
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        } else if (orientation in 261..279) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            return
        }
        if (requestOrientation == currOrientation) {
            return
        }
        val needNotify = currOrientation != ORIENTATION_UNKNOWN
        currOrientation = requestOrientation
        if (needNotify) {
            if (mChangeListener != null) {
                mChangeListener!!.onChanged(requestOrientation)
            } else {
                context.requestedOrientation = requestOrientation
            }
        }
    }
}