package com.sfc.mqtt.util;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by Prophet on 2016/12/8.
 */
public class MessageArrive implements IMessageArrive {
    @Override
    public void doArrive(String topic, MqttMessage message) {
        System.out.println("New Message: [" + topic + "]"+ new String(message.getPayload()));
    }
}
