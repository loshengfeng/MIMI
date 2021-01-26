package com.dabenxiang.mimi.model.manager.mqtt

import android.content.Context
import com.dabenxiang.mimi.PROJECT_NAME
import com.dabenxiang.mimi.model.manager.mqtt.callback.ConnectCallback
import com.dabenxiang.mimi.model.manager.mqtt.callback.ExtendedCallback
import com.dabenxiang.mimi.model.manager.mqtt.callback.SubscribeCallback
import com.dabenxiang.mimi.model.pref.Pref
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.android.service.ParcelableMqttMessage
import org.eclipse.paho.client.mqttv3.*
import timber.log.Timber
import tw.gov.president.manager.submanager.logmoniter.di.SendLogManager
import java.util.logging.LogManager

class MQTTManager(val context: Context, private val pref: Pref) {

    companion object {
        const val PREFIX_CHAT = "/chat/"
        const val PREFIX_NOTIFICATION = "/notification/"
    }

    private var client: MqttAndroidClient? = null
    private var options: MqttConnectOptions? = null

    private val messageIdSet = hashSetOf<String>()

    private var extendedCallback: ExtendedCallback? = null

    fun init(serverUrl: String, clientId: String, callback: ExtendedCallback) {
        if(client!=null) {
            client?.disconnect()
            client?.setCallback(null)
            client =null
            options =null
            extendedCallback= null
        }
        extendedCallback = callback

        client = MqttAndroidClient(context, serverUrl, clientId, MqttAndroidClient.Ack.MANUAL_ACK)
        client?.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                Timber.i("MQTT - connectComplete")
                extendedCallback?.onConnectComplete(reconnect, serverURI)
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                val parcelableMqttMessage = message as ParcelableMqttMessage
                val messageId = parcelableMqttMessage.messageId
                client?.acknowledgeMessage(messageId)
                if (!messageIdSet.contains(messageId)) {
                    messageIdSet.add(messageId)
                    extendedCallback?.onMessageArrived(topic, message)
                }
            }

            override fun connectionLost(cause: Throwable?) {
                extendedCallback?.onConnectionLost(cause)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                extendedCallback?.onDeliveryComplete(token)
            }
        })

        options = MqttConnectOptions()
        options?.password = pref.memberToken.accessToken.toCharArray()
        options?.userName = pref.profileItem.userId.toString()
        options?.isAutomaticReconnect = false
        options?.isCleanSession = false

    }

    fun isMqttConnect(): Boolean {
        return try {
            client?.isConnected ?: false
        }catch (e:IllegalArgumentException){
            Timber.v("IllegalArgumentException:$e")
            extendedCallback?.onInvalidHandle(e.cause)
            false
        }

    }

    fun connect(connectCallback: ConnectCallback) {
        client?.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                val disconnectedBufferOptions = DisconnectedBufferOptions()
                disconnectedBufferOptions.isBufferEnabled = true
                disconnectedBufferOptions.bufferSize = 100
                disconnectedBufferOptions.isPersistBuffer = false
                disconnectedBufferOptions.isDeleteOldestMessages = false
                client?.setBufferOpts(disconnectedBufferOptions)
                Timber.i("MQTT - connect onSuccess")
                connectCallback.onSuccess(asyncActionToken)
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                connectCallback.onFailure(asyncActionToken, exception)
            }
        })
    }

    fun disconnect() {
        isMqttConnect().takeIf { it }?.let {
            client?.disconnect()
        }


    }

    fun subscribeToTopic(subscriptionTopic: String, subscribeCallback: SubscribeCallback) {
        client?.subscribe(subscriptionTopic, 1, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                subscribeCallback.onSuccess(asyncActionToken)
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                subscribeCallback.onFailure(asyncActionToken, exception)
            }
        })
    }


    private val defaultMqttCallback by lazy {
        object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {}
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {}
        }
    }

    fun publishMessage(
        publishTopic: String,
        publishMessage: String,
        callback: IMqttActionListener = defaultMqttCallback
    ) {
        val message = MqttMessage()
        message.payload = publishMessage.toByteArray()
        client?.publish(publishTopic, message, null, callback)
        Timber.d("Message Published")

        if (!isMqttConnect()) {
            Timber.d("${client?.bufferedMessageCount} messages in buffer.")
        }
    }
}