package com.dabenxiang.mimi.model.manager.mqtt

import org.eclipse.paho.client.mqttv3.IMqttToken

interface ConnectCallback {
    fun onSuccess(asyncActionToken: IMqttToken)
    fun onFailure(asyncActionToken: IMqttToken, exception: Throwable)
}