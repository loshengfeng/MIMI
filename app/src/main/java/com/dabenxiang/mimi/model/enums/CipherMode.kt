package com.dabenxiang.mimi.model.enums

enum class CipherMode(val value: Int) {
    CBC(1),
    ECB(2),
    OFB(3),
    CFB(4),
    CTS(5),
}