package com.sfc.mqtt.util;

import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Created by Prophet on 2016/12/8.
 */
public class SimpleMqttClientTest {
    public static void main(String args[]) {
        // new client
        IMqttClient client = new SimpleMqttClient();
        // set up call back object
        IMessageArrive callback = new MessageArrive();
        client.setMessageArriveCallBack(callback);
        try {
            client.setWill("prophet-test", "connection lost".getBytes(), 2, false);
            client.openConnection("tcp://m2m.eclipse.org:1883", "sfctest", true);
            client.subscribe("prophet-test", 2);
            client.publish("prophet-test", 2, "test".getBytes());

            while (true);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        //while (true);
    }
}
