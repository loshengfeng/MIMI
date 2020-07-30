package com.dabenxiang.mimi.manager.update

import com.dabenxiang.mimi.model.api.vo.error.TOKEN_NOT_FOUND
import com.dabenxiang.mimi.model.vo.domain.DomainInputItem
import com.dabenxiang.mimi.model.vo.domain.DomainOutputItem
import com.dabenxiang.mimi.model.vo.domain.DomainOutputListItem
import com.google.gson.GsonBuilder

import org.koin.core.KoinComponent
import timber.log.Timber
import tw.gov.president.manager.BaseManagerData
import tw.gov.president.manager.BaseManagerData.App
import tw.gov.president.utils.gson.factory.EnumTypeAdapterFactory

abstract class BaseDomainManager : KoinComponent {

    companion object {
        const val LOG_SERVER_PROJECT_ID = "c3gz3mrn" // Log server projectName: oolong
        const val DOWNLOAD_SERVER_PROJECT_ID = "WaumJF6y" // download server projectName: puer
        const val NBK_PROJECT_ID = "c3gz3mrn" //TODO
        const val FLAVOR_DEV = "dev"
        const val FLAVOR_SIT = "sit"
        const val MEDIA_TYPE_TEXT = "text/plain"
        const val MEDIA_TYPE_JSON = "application/json"
        const val AES_KEY = "56102858kefu9527" //FIXME !!!
        const val TOKEN_PREFIX = "Basic "
        const val BEARER_PREFIX = "Bearer "
        const val AUTHORIZATION = "Authorization"
        const val APP_ID = "app_id"
        const val DEVICE_ID = "device_id"
        const val APP_VERSION_CODE = "device_version_code"
        const val TIMESTAMP = "timestamp"
        const val REQUEST_ID = "request_id"
        const val SIGN = "sign"
        fun isRefreshTokenFailed(code: String?): Boolean {
            return code == TOKEN_NOT_FOUND
        }
    }

    enum class DomainType(val domain: String) {
        WWW("wwww"),
        API("api"),
        BWR("bwr")
    }

    val gson = GsonBuilder().registerTypeAdapterFactory(EnumTypeAdapterFactory()).create()
    abstract val projectId: String
    abstract val defaultDomain: String
    private var currentDomainIndex = 0
    private val domainList: ArrayList<String> = arrayListOf()
    private var domainOutputs: List<DomainOutputItem> = arrayListOf()
//    private var _goLibVersion = Libs.getVersion()
//    val goLibVersion get() = _goLibVersion

    fun changeApiDomainIndex() {
        if (domainList.isNotEmpty()) {
            Timber.i("changeApiDomainIndex ++ ")
            currentDomainIndex++
        }
    }

    fun getApiDomain(domainType: DomainType): String {
        val result = StringBuilder()
        result.append("https://")
            .append(domainType.domain)
            .append(".")
            .append(getDomain())
            .toString()
        return result.toString()
    }

    fun getDomain(): String {
        return when {
            currentDomainIndex < domainList.size -> {
                domainList[currentDomainIndex]
            }
            else -> {
                val domains = getDomains()
                if (domains.size > currentDomainIndex) {
                    domains[currentDomainIndex]
                } else {
                    defaultDomain
                }

            }
        }
    }

    private fun getDomains(): List<String> {
        clearData()
        domainOutputs = fetchDomains().domainOutputs
        domainOutputs.forEach {
            if (it.domain.isNotEmpty()) domainList.add(it.domain)
        }
        return domainList
    }

    open fun fetchDomains(): DomainOutputListItem {
        val domainInputItem =
            DomainInputItem(
                projectId,
                App?.applicationContext?.filesDir?.path!!,
                getLibEnv()
            )
        val input = gson.toJson(domainInputItem)
        Timber.d("input: $input")
        val output = libs.Libs.getDomains(input)
        Timber.d("output: $output")

        return gson.fromJson(output, DomainOutputListItem::class.java)
    }

    private fun clearData() {
        currentDomainIndex = 0
        domainList.clear()
    }

    open fun getLibEnv(): String {
        Timber.i("FLAVOR= ${checkFlavor()}")
        return when (checkFlavor()) {
            FLAVOR_DEV -> "d"
            FLAVOR_SIT -> "s"
            else -> "p"
        }
    }

    fun checkFlavor(): String {
        return BaseManagerData.configData?.flavor?.let {
            when {
                it.contains("_") -> {
                    BaseManagerData.configData?.flavor?.substringBefore("_")
                }
                else -> it
            }
        } ?: ""
    }
}