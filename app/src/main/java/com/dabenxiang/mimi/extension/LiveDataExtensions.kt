package com.dabenxiang.mimi.extension

import androidx.lifecycle.MutableLiveData

fun MutableLiveData<Boolean>.setNot() {
    this.value = this.value?.not()
}