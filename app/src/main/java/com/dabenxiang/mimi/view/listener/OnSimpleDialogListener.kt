package com.dabenxiang.mimi.view.listener

interface OnSimpleDialogListener {
    fun onConfirm()
    fun onCancel()
}

interface OnSimpleEditorDialogListener {
    fun onConfirm(text:String)
    fun onCancel()
}