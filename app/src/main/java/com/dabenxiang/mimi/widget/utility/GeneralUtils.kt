package com.dabenxiang.mimi.widget.utility

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.dabenxiang.mimi.*
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.vo.error.ErrorItem
import com.dabenxiang.mimi.model.api.vo.error.HttpExceptionItem
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.view.main.MainActivity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import tw.gov.president.manager.submanager.logmoniter.di.SendLogManager
import java.io.*
import java.lang.reflect.Method
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.ceil
import kotlin.math.roundToInt


object GeneralUtils {

    private val decimalFormat = DecimalFormat("###,##0.00")

    fun showToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("HardwareIds")
    fun getAndroidID(): String {
        return Settings.Secure.getString(
            App.applicationContext().contentResolver,
            Settings.Secure.ANDROID_ID
        )
//        return "1234567890"
    }

    fun getHttpExceptionData(httpException: HttpException): HttpExceptionItem {
        val oriResponse = httpException.response()

        val url = oriResponse?.raw()?.request?.url.toString()

        val errorBody = oriResponse?.errorBody()
        val errorCode = oriResponse?.code()
        val jsonStr = errorBody?.string()
        val type = object : TypeToken<ErrorItem>() {}.type

        val errorItem: ErrorItem = try {
            Gson().fromJson(jsonStr, type)
        } catch (e: Exception) {
            e.printStackTrace()
            ErrorItem(errorCode.toString(), "伺服器错误", null)
        }

        val responseBody = Gson().toJson(
            ErrorItem(
                errorItem.code,
                errorItem.message,
                null
            )
        )
            .toResponseBody(ApiRepository.MEDIA_TYPE_JSON.toMediaTypeOrNull())

        val rawResponse = okhttp3.Response.Builder()
            .code(httpException.code())
            .message(httpException.message())
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url(url).build())
            .build()

        val response = Response.error<ErrorItem>(responseBody, rawResponse)

        val httpExceptionClone = HttpException(response)
        return HttpExceptionItem(
            errorItem,
            httpExceptionClone,
            url
        )
    }

    fun getLibEnv(): String {
        if (BuildConfig.BUILD_TYPE.contains(DomainManager.BUILDTYPE_DEV)) {
            return "d"
        } else if (BuildConfig.BUILD_TYPE.contains(DomainManager.BUILDTYPE_SIT)) {
            return "s"
        } else {
            return "p"
        }
    }

    fun isFriendlyNameValid(name: String): Boolean {
        return Pattern.matches(
            "^[a-zA-Z0-9-\\u4e00-\\u9fa5-`\\[\\]~!@#\$%^&*()_+{}|:”<>?`\\[\\];’,./\\\\]{1,20}+$",
            name
        )
    }

    fun isEmailValid(email: String): Boolean {
        return Pattern.matches(
            "^[A-Za-z0-9_\\-\\.\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*$",
            email
        )
    }

    fun isAccountValid(account: String): Boolean {
        return Pattern.matches("^[a-zA-Z0-9]{5,20}$", account)
    }

    fun isPasswordValid(pwd: String): Boolean {
        return Pattern.matches(
            "^[a-zA-Z0-9-`\\[\\]~!@#\$%^&*()_+\\-=;',./?<>{}|:\"\\\\]{8,20}+$",
            pwd
        )
    }

    fun isMobileValid(callPrefix: String, mobile: String, validMobile: Boolean = false): Boolean {
        return if (callPrefix == "+86") {
            if (validMobile) {
                !checkPhoneNum(mobile)
            } else {
                mobile.length < 11
            }
        } else {
            mobile.length < 9
        }
    }

    fun getScreenSize(activity: Activity): Pair<Int, Int> {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        return Pair(width, height)
    }

    fun getStatusBarHeight(context: Context): Int {
        var statusBarHeight = 0
        val resourceId: Int =
            context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }

    /**
     * 複製到剪貼板
     */
    fun copyToClipboard(context: Context, content: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("share", content)
        clipboard.setPrimaryClip(clip)
    }

    fun getTimeDiff(startDate: Date, endDate: Date): String {
        val time = (endDate.time - startDate.time) / 1000
        return when {
            (time / (60 * 60 * 24 * 30)) > 0 -> {
                (time / (60 * 60 * 24 * 30)).toString().plus("个月前")
            }
            (time / (60 * 60 * 24)) > 0 -> {
                (time / (60 * 60 * 24)).toString().plus("天前")
            }
            (time / (60 * 60)) > 0 -> {
                (time / (60 * 60)).toString().plus("小时前")
            }
            (time / 60) > 0 -> {
                (time / 60).toString().plus("分钟前")
            }
            else -> {
                time.toString().plus("秒钟前")
            }
        }
    }

    fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    fun pxToDp(context: Context, px: Int): Int {
        val density = context.resources.displayMetrics.density
        return (px.toFloat() / density).roundToInt()
    }

    fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus
        if (view != null) {
            val inputManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun hideKeyboard(activity: FragmentActivity) {
        val view = activity.currentFocus
        if (view != null) {
            val inputManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun hideKeyboard(fragment: Fragment) {
        fragment.view?.run {
            val inputManager =
                fragment.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(this.windowToken, 0)
        }
    }

    fun showKeyboard(context: Context) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun getExceptionDetail(t: Throwable): String {
        return when (t) {
            is HttpException -> {
                val data = getHttpExceptionData(t)
                "$data, ${t.localizedMessage}"
            }
            else -> getStackTrace(t)
        }
    }

    fun openWebView(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun getStackTrace(t: Throwable): String {
        val sw = StringWriter(256)
        val pw = PrintWriter(sw, false)
        t.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    fun installApk(context: Context, path: String) {
        val session: PackageInstaller.Session?
        try {
            Timber.d("installApk: $path")
            val packageInstaller = context.packageManager.packageInstaller
            val sessionParams =
                PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            val sessionId = packageInstaller.createSession(sessionParams)
            session = packageInstaller.openSession(sessionId)

            addApkToInstallSession(path, session)
            Timber.d("session: $session")
            val intent = Intent(context, MainActivity::class.java)
            intent.action = PACKAGE_INSTALLED_ACTION
            Timber.d("intent: $intent")
            val pendingIntent = PendingIntent.getActivity(context, 1010, intent, 0)
            session.commit(pendingIntent.intentSender)
        } catch (e: java.lang.Exception) {
            Timber.d("installApk: $e")
            e.printStackTrace()
        }
    }

    private fun addApkToInstallSession(apkName: String, session: PackageInstaller.Session) {
        val packageInSession = session.openWrite("package", 0, -1)
        val buffer = ByteArray(65535)
        File(apkName).inputStream().let {
            Timber.d("input stream: $it")
            it.buffered().let { input ->
                while (true) {
                    val sz = input.read(buffer)
                    if (sz <= 0) break
                    Timber.d("input sz: $sz")
                    packageInSession.write(buffer, 0, sz)
                }
                input.close()
            }
            it.close()
            packageInSession.close()
        }
    }

    /**
     * 開啟app play video
     */
    fun openPlayerIntent(context: Context, path: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
        intent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, false)
        intent.setDataAndType(Uri.parse(path), "video/*")
        context.startActivity(intent)
    }

    fun getDensity(): Float {
        return App.applicationContext().resources.displayMetrics.density
    }

    fun getWindowsWidth(): Int {
        return App.applicationContext().resources.displayMetrics.widthPixels
    }

    fun getWindowsHeight(): Int {
        return App.applicationContext().resources.displayMetrics.heightPixels
    }

    fun getAmountFormat(amount: Float): String {
        return decimalFormat.format(amount)
    }

    private fun checkPhoneNum(num: String): Boolean {
        val regExp = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$"
        val p = Pattern.compile(regExp)
        val m = p.matcher(num)
        return m.matches()
    }

    fun getCopyText(context: Context): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        val clipDataItem = clipData?.getItemAt(0)
        return clipDataItem?.text.toString()
    }

    fun getSpanString(context: Context, text: String, keyword: String): SpannableString {
        if (keyword.isBlank()) return SpannableString(text)
        val result = SpannableString(text)
        var index = text.toLowerCase().indexOf(keyword.toLowerCase())
        while (index != -1) {
            result.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        context,
                        R.color.color_red_1
                    )
                ),
                index,
                index + keyword.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            index = text.toLowerCase().indexOf(keyword.toLowerCase(), index + keyword.length)
        }
        return result
    }

    fun getMediaSource(
        uriString: String,
        sourceFactory: DefaultDataSourceFactory
    ): MediaSource? {
        val uri = Uri.parse(uriString)

        val sourceType = Util.inferContentType(uri)
        Timber.d("#sourceType: $sourceType")

        return when (sourceType) {
            C.TYPE_DASH ->
                DashMediaSource.Factory(sourceFactory)
                    .createMediaSource(uri)
            C.TYPE_HLS ->
                HlsMediaSource.Factory(sourceFactory)
                    .createMediaSource(uri)
            C.TYPE_SS ->
                SsMediaSource.Factory(sourceFactory)
                    .createMediaSource(uri)
            C.TYPE_OTHER -> {
                when {
                    uriString.startsWith("rtmp://") ->
                        ProgressiveMediaSource.Factory(RtmpDataSourceFactory())
                            .createMediaSource(uri)
                    uriString.contains("m3u8") -> HlsMediaSource.Factory(sourceFactory)
                        .createMediaSource(uri)
                    else ->
                        ProgressiveMediaSource.Factory(sourceFactory)
                            .createMediaSource(uri)
                }
            }
            else -> null
        }
    }

    fun getStringLength(str: String): Int {
        Timber.i("getStringLength str =$str 1.0")
        var valueLength = 0.0
        val pattern = Pattern.compile("""^[_A-z0-9]*((\s)*[_A-z0-9])*${'$'}""")
        for (i in str.indices) {

            val temp: String = str.substring(i, i + 1)
            valueLength += if (pattern.matcher(temp).matches()) {
                Timber.i("getStringLength temp =$temp 1.0")
                1.0
            } else {
                Timber.i("getStringLength temp =$temp 2.0")
                2.0
            }
        }
        return ceil(valueLength).toInt()
    }

    fun parseTimeToUTC(date: Date): String {
        var time: String
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            time = sdf.format(date)
        } catch (e: Exception) {
            Timber.d("parse time error: $e")
            time = "0000-00-00T00:00:00Z"
        }
        Timber.d("parse time to UTC: $time")
        return time
    }

    fun getAdSize(activity: Activity): Pair<Int, Int> {
        return Pair(getScreenSize(activity).first / 3, getScreenSize(activity).first / 3 / 3)
    }

    fun getMaxCount(count: Int): Int {
        return if (count > 1000) 999 else count
    }

    @SuppressLint("PackageManagerGetSignatures")
    fun getApplicationSignature(packageName: String = App.self.packageName): List<Signature> {

        val signatureList: List<Signature>
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val sig = App.self.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
                signatureList = if (sig.hasMultipleSigners()) {
                    sig.apkContentsSigners.map { it }
                } else {
                    sig.signingCertificateHistory.map {
                       it
                    }
                }
            } else {
                val sig = App.self.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
                signatureList = sig.map {it}
            }

            return signatureList
        } catch (e: Exception) {
           Timber.i("getApplicationSignature failed :$e")
        }
        return emptyList()
    }

    /**
     * Check this device is emulator
     */
    fun isProbablyRunningOnEmulator(): Boolean {
        // Android SDK emulator
        return ((Build.FINGERPRINT.startsWith("google/sdk_gphone_")
                && Build.FINGERPRINT.endsWith(":user/release-keys")
                && Build.MANUFACTURER == "Google" && Build.PRODUCT.startsWith("sdk_gphone_") && Build.BRAND == "google"
                && Build.MODEL.startsWith("sdk_gphone_"))
                //
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                //bluestacks
                || "QC_Reference_Phone" == Build.BOARD && !"Xiaomi".equals(Build.MANUFACTURER, ignoreCase = true) //bluestacks
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HOST.startsWith("Build") //MSI App Player
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.PRODUCT == "google_sdk"
                // another Android SDK emulator check
                || SystemProperties.getProp("ro.kernel.qemu") == "1")
    }

    object SystemProperties {
        private var failedUsingReflection = false
        private var getPropMethod: Method? = null

        @SuppressLint("PrivateApi")
        fun getProp(propName: String, defaultResult: String = ""): String {
            if (!failedUsingReflection) try {
                if (getPropMethod == null) {
                    val clazz = Class.forName("android.os.SystemProperties")
                    getPropMethod = clazz.getMethod("get", String::class.java, String::class.java)
                }
                return getPropMethod!!.invoke(null, propName, defaultResult) as String? ?: defaultResult
            } catch (e: Exception) {
                getPropMethod = null
                failedUsingReflection = true
            }
            var process: Process? = null
            try {
                process = Runtime.getRuntime().exec("getprop \"$propName\" \"$defaultResult\"")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                return reader.readLine()
            } catch (e: IOException) {
            } finally {
                process?.destroy()
            }
            return defaultResult
        }
    }

    fun parserReferrerCode(context: Context): String {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboard.primaryClip
            val clipDataItem = clipData?.getItemAt(0)
            val copyText = clipDataItem?.text.toString() ?: ""

            return if (copyText.contains(MIMI_INVITE_CODE)) {
                val startIndex = copyText.lastIndexOf(MIMI_INVITE_CODE) + MIMI_INVITE_CODE.length
                val inviteCode = copyText.substring(startIndex, copyText.length)
                inviteCode
            } else ""

        } catch (e:Exception){
            Timber.e("parserReferrerCode Exception:$e")
            SendLogManager.v(
                PROJECT_NAME,
                "parserReferrerCode Exception:$e"
            )
            return ""
        }

    }
}