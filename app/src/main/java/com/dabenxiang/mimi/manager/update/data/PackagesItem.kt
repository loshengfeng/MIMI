package com.dabenxiang.mimi.manager.update.data

import com.google.gson.annotations.SerializedName

data class PackagesItem(
    @SerializedName("packageUniqueId")
    val packageUniqueId: String = "",

    @SerializedName("packageName")
    val packageName: String = "",

    @SerializedName("publishTime")
    val publishTime: String = "",

    @SerializedName("state")
    val state: String = "",

    @SerializedName("version")
    val versionItem: VersionItem? = null,

    @SerializedName("packageUrl")
    val packageUrlItem: PackageUrlItem? = null,

    @SerializedName("requirement")
    val requirementItem: RequirementItem? = null,

    @SerializedName("compatibility")
    val compatibilityItem: CompatibilityItem? = null
)

//[
//    {
//        "packageUniqueId": "com.h5seen.video",
//        "packageName": "蜜蜜影視",
//        "publishTime": "2020-01-01T00:00:00+00",
//        "state":"",
//        "version": {
//        "code": "10020000",
//        "name": "10020001-prod",
//        "major": 10020000,
//        "stage": 0, // 0：Prod | 1：Dev | 2：Sit | 3：Alpha | 4：Beta
//        "releaseId": 3788307682528591872,
//        "state":""
//        },
//            "packageUrl": {
//            "main":  "https://dm.xyz.com/com.h5seen.video/com.h5seen.video.1.1.0-1234567890123456789.apk",
//            "mirror": [
//            "https://dm.aaa.com/com.h5seen.video/com.h5seen.video.1.1.0-alpha1.apk",
//            "https://dm.bbb.com/com.h5seen.video/com.h5seen.video.1.1.0-alpha1.apk",
//            "https://dm.ccc.com/com.h5seen.video/com.h5seen.video.1.1.0-alpha1.apk"
//            ]
//        },
//            "requirement": {
//            "os": "Android",
//            "osName": "Nougat",
//            "osVersion": "1.0.0",
//            "apiLevel": 23
//        },
//            "compatibility": {
//            "code": "10010000",
//            "name": "10010000-prod",
//            "major": 10010000,
//            "stage": 0, // 0：Prod | 1：Dev | 2：Sit | 3：Alpha | 4：Beta
//            "releaseId": 3788698880695074816
//        }
//    }
//]