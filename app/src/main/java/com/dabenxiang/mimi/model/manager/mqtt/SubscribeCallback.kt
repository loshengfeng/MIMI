package com.dabenxiang.mimi.model.manager.mqtt

import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage

interface SubscribeCallback {
    fun onSuccess(asyncActionToken: IMqttToken)
    fun onFailure(asyncActionToken: IMqttToken, exception: Throwable)
    fun onSubscribe(topic: String, message: MqttMessage)
}