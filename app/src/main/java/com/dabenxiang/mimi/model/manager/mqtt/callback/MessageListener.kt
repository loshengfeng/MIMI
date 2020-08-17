package com.dabenxiang.mimi.model.manager.mqtt.callback

import org.eclipse.paho.client.mqttv3.MqttMessage

interface MessageListener {
    fun onMsgReceive(message: MqttMessage)
}