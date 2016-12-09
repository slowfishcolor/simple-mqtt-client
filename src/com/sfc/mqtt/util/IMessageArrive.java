package com.sfc.mqtt.util;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by Prophet on 2016/12/8.
 */
public interface IMessageArrive {
    public void doArrive(String topic, MqttMessage message);
}
