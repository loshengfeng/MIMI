package com.dabenxiang.mimi.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.text.*
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.Gravity
import com.dabenxiang.mimi.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class BirthdayEditText : TextInputEditText {

    enum class DateFormat(val value: String) { YYYYMMDD("yyyyMMdd") }
    enum class DividerCharacter(val value: String) { Minus("-") }

    private var dividerCharacter = DividerCharacter.Minus
    private var dateFormat = DateFormat.YYYYMMDD

    private var maxDate: Date? = null
        set(value) {
            validateMinMaxDate(minDate, value)
            field = value
        }

    private var minDate: Date? = null
        set(value) {
            validateMinMaxDate(value, maxDate)
            field = value
        }

    var autoCorrect: Boolean = false
    var helperTextEnabled = false
    var helperTextHighlightedColor = Color.BLUE

    private val dateLength: Int
        get() {
            return if (dateFormat == DateFormat.YYYYMMDD) {
                10
            } else {
                5
            }
        }

    private val firstDividerPosition = 4
    private val nextDividerPosition = 7
    private var edited = false
    private var valueWithError: String? = null

    constructor(context: Context?) : super(context!!) {
        initDateEditText()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        initDateEditText(attrs = attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!,
        attrs,
        defStyle
    ) {
        initDateEditText(attrs = attrs)
    }

    fun listen() {
        addTextChangedListener(dateTextWatcher)
    }

    @SuppressLint("RtlHardcoded", "CustomViewStyleable")
    private fun initDateEditText(attrs: AttributeSet? = null) {
        gravity = Gravity.LEFT
        isCursorVisible = false
        setOnClickListener { setSelection(text?.length ?: 0) }
        inputType = InputType.TYPE_CLASS_NUMBER

        if (attrs == null) {
            return
        }

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DateEditText, 0, 0)

        getDividerCharacter(typedArray)
        getDateFormat(typedArray)
        getMaxDate(typedArray)
        getMinDate(typedArray)

        autoCorrect = typedArray.getBoolean(R.styleable.DateEditText_autoCorrect, true)
        helperTextEnabled = typedArray.getBoolean(R.styleable.DateEditText_helperTextEnabled, false)
        helperTextHighlightedColor =
            typedArray.getColor(R.styleable.DateEditText_helperTextHighlightedColor, Color.BLUE)

        validateMinMaxDate(minDate, maxDate)

        typedArray.recycle()
    }

    private fun getDateFormat(typedArray: TypedArray) {
        val dateFormat = typedArray.getInt(R.styleable.DateEditText_dateFormat, 0)

        if (dateFormat == 0) {
            this.dateFormat = DateFormat.YYYYMMDD
        }

        if (hint.isNullOrEmpty()) {
            hint = getDateFormatFromDivider()
        }
    }

    private fun getDividerCharacter(typedArray: TypedArray) {
        val dividerCharacter = typedArray.getInt(R.styleable.DateEditText_dividerCharacter, 0)
        if (dividerCharacter == 0) {
            this.dividerCharacter = DividerCharacter.Minus
        }
    }

    private fun getMaxDate(typedArray: TypedArray) {
        val maxDateString = typedArray.getString(R.styleable.DateEditText_maxDate) ?: return
        val format = getDateFormatFromDivider()
        validateMinMaxDate(date = maxDateString)
        try {
            maxDate = SimpleDateFormat(format, Locale.getDefault()).parse(maxDateString)
        } catch (e: ParseException) {
            throw IllegalArgumentException("max date must be entered as a format and divider character")
        }
    }

    private fun getMinDate(typedArray: TypedArray) {
        val minDateString = typedArray.getString(R.styleable.DateEditText_minDate) ?: return
        val format = getDateFormatFromDivider()
        validateMinMaxDate(date = minDateString)
        try {
            minDate = SimpleDateFormat(format, Locale.getDefault()).parse(minDateString)
        } catch (e: ParseException) {
            throw IllegalArgumentException("min date must be entered as a format and divider character")
        }
    }

    private fun validateMinMaxDate(date: String) {
        if (dateFormat == DateFormat.YYYYMMDD) {
            if (date.length != 10) {
                throw IllegalArgumentException("Invalid date")
            }

            val day = date.substring(0, 4).toInt()
            val month = date.substring(5, 7).toInt()
            val year = date.substring(8, 10).toInt()

            val isLeapYear = (year % 100 != 0 || year % 400 != 0)

            if (month > 12 || month <= 0) {
                throw IllegalArgumentException("Invalid date")
            }

            if (day > 31 || day == 0) {
                throw IllegalArgumentException("Invalid date")
            } else if (day == 31 && (month == 4 || month == 6 || month == 9 || month == 11)) {
                throw IllegalArgumentException("Invalid date")
            } else if (month == 2 && day == 31) {
                throw IllegalArgumentException("Invalid date")
            } else if (month == 2 && day == 29 && !isLeapYear) {
                throw IllegalArgumentException("Invalid date")
            }
        }
    }

    private fun validateMinMaxDate(minDate: Date?, maxDate: Date?) {
        val mMinDate = minDate ?: return
        val mMaxDate = maxDate ?: return
        if (mMinDate >= mMaxDate) {
            throw IllegalArgumentException("min date must be smaller than max date")
        }
    }

    private val dateTextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (edited) {
                edited = false
                return
            }

            var value = getEditText()
            value = validate(value)

            if (valueWithError != null && before < count) {
                edited = true
                setText(valueWithError)
                setSelection(text?.length ?: 0)
                valueWithError = null
                return
            }

            value = manageDateDivider(value, firstDividerPosition, start, before)

            if (dateFormat == DateFormat.YYYYMMDD) {
                value = manageDateDivider(value, nextDividerPosition, start, before)
            }

            edited = true
            setText(value)
            setSelection(text?.length ?: 0)
            renderHelperText(value = value)
        }

        override fun afterTextChanged(s: Editable) {}

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    }

    private fun validate(value: String): String {
        if (dateFormat == DateFormat.YYYYMMDD) {
            return validatedDateFormat(value)
        }
        return value
    }

    private fun validatedDateFormat(value: String): String {
        var mValue = value

        if (mValue.length >= 8) {
            val month = mValue.substring(5, 7).toInt()
            if (month > 12 || month == 0) {
                if (autoCorrect) {
                    mValue = mValue.replace(month.toString(), "12", false)
                }
            }

            if (mValue.length >= 10) {
                val day = mValue.substring(8, 10).toInt()
                if (day == 31 && (month == 4 || month == 6 || month == 9 || month == 11)) {
                    if (autoCorrect) {
                        mValue = mValue.replace(day.toString(), "30", false)
                    }
                } else if (month == 2 && day == 31) {
                    if (autoCorrect) {
                        mValue = mValue.replace(day.toString(), "29", false)
                    }
                } else if (day > 31 || day == 0) {
                    if (autoCorrect) {
                        mValue = mValue.replace(
                            day.toString(),
                            when (month % 2 == 0) {
                                true -> "30"
                                else -> "31"
                            },
                            false
                        )
                    }
                }
            }
        }

        if (mValue.length >= 10) {
            val day = mValue.substring(8, 10).toInt()
            if (day > 31 || day == 0) {
                if (autoCorrect) {
                    mValue = mValue.replace(day.toString(), "31", false)
                }
            }
        }

        if (mValue.length == 10) {
            val year = mValue.substring(0, 4).toInt()
            if (maxDate != null) {
                val maxDate = maxDate!!
                val format = getDateFormatFromDivider()
                val inputDate = value.toDate(format = format)
                if (inputDate != null && inputDate > maxDate) {
                    if (autoCorrect) {
                        mValue = maxDate.toString(format = format)
                    }
                }
            }

            if (minDate != null) {
                val minDate = minDate!!
                val format = getDateFormatFromDivider()
                val inputDate = value.toDate(format = format)
                if (inputDate != null && inputDate < minDate) {
                    if (autoCorrect) {
                        mValue = minDate.toString(format = format)
                    }
                }
            }

            if (isLeapYear(year).not()) {
                val month = mValue.substring(5, 7).toInt()
                val day = mValue.substring(8, 10).toInt()
                if (month == 2 && day >= 28) {
                    if (autoCorrect) {
                        mValue = mValue.replace(day.toString(), "28", false)
                    }
                }
            }
        }

        return mValue
    }

    private fun isLeapYear(year: Int) = when {
        year % 4 == 0 -> {
            when {
                year % 100 == 0 -> year % 400 == 0
                else -> true
            }
        }
        else -> false
    }

    private fun getEditText(): String {
        return if ((text?.length ?: 0) >= dateLength)
            text.toString().substring(0, dateLength)
        else
            text.toString()
    }

    private fun manageDateDivider(working: String, position: Int, start: Int, before: Int): String {
        if (working.length == position) {
            return if (before <= position && start < position)
                working + dividerCharacter.value
            else
                working.dropLast(1)
        }
        return working
    }

    private fun getDateFormatFromDivider(): String {
        return when (dateFormat) {
            DateFormat.YYYYMMDD -> dateFormat.value.substring(
                0,
                4
            ) + dividerCharacter.value + dateFormat.value.substring(
                4,
                6
            ) + dividerCharacter.value + dateFormat.value.substring(6, 8)
        }
    }

    private fun renderHelperText(value: String) {

        if (parent.parent is TextInputLayout && helperTextEnabled) {
            val textInputLayout = parent.parent as TextInputLayout
            if (value.isEmpty()) {
                textInputLayout.helperText = null
                return
            }
            textInputLayout.isHelperTextEnabled = true
            val hint = getDateFormatFromDivider()
            val spannableString = SpannableString(hint)
            val foregroundSpan = ForegroundColorSpan(helperTextHighlightedColor)
            spannableString.setSpan(
                foregroundSpan,
                0,
                value.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textInputLayout.helperText = spannableString
        }
    }

    private fun String.toDate(format: String): Date? {
        val sdf = SimpleDateFormat(format, Locale.US)
        return sdf.parse(this)
    }

    private fun Date.toString(format: String): String {
        val sdf = SimpleDateFormat(format, Locale.US)
        return sdf.format(this)
    }
}