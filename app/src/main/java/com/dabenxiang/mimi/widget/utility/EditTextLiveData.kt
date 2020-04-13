package com.dabenxiang.mimi.widget.utility

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.lifecycle.LiveData
import java.lang.ref.WeakReference

abstract class EditTextLiveData(value: String? = null) : LiveData<String>(value) {
    private var isFirst = false
    private var thisRef: WeakReference<EditTextLiveData>? = null
    private var editTextRef: WeakReference<EditText>? = null

    private val textWatcher: TextWatcher by lazy {
        object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isFirst) {
                    isFirst = false
                } else {
                    thisRef?.get()?.setLiveDataValue(s.toString())
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        }
    }

    private fun setLiveDataValue(value: String?) {
        super.setValue(value)
    }

    var bindingEditText: EditText?
        get() {
            return editTextRef?.get()
        }
        set(view) {
            if (thisRef?.get() == null) {
                thisRef = WeakReference(this)
            }

            editTextRef?.get()?.also { lastEdit ->
                lastEdit.removeTextChangedListener(textWatcher)
                editTextRef = null
            }

            view?.also {
                isFirst = true
                editTextRef = WeakReference(view)

                view.addTextChangedListener(textWatcher)
                value.also {
                    view.setText(it ?: "")
                    view.setSelection(it?.length ?: 0)
                }
            }
        }

    override fun setValue(value: String?) {
        when (val editText = editTextRef?.get()) {
            null -> super.setValue(value)
            else -> editText.setText(value ?: "")
        }
    }

    override fun postValue(value: String?) {
        when (val editText = editTextRef?.get()) {
            null -> super.postValue(value)
            else -> editText.setText(value ?: "")
        }
    }
}