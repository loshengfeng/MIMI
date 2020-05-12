package com.dabenxiang.mimi.model.serializable

import java.io.Serializable

class CategoriesData : Serializable {
    var id: String = ""
    var title: String = ""
    var isAdult: Boolean = false
}