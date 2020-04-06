package com.dabenxiang.mimi.model.manager.mqtt

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttMessage

interface ExtendedCallback {
    fun onConnectComplete(reconnect: Boolean, serverURI: String)
    fun onMessageArrived(topic: String, message: MqttMessage)
    fun onConnectionLost(cause: Throwable)
    fun onDeliveryComplete(token: IMqttDeliveryToken)
}