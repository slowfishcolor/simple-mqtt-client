package com.sfc.mqtt.util;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * 通过该接口的实现来实现一个简单的 MQTT client，无加密
 * Created by Prophet on 2016/12/8.
 */
public interface IMqttClient extends MqttCallback{

    /**
     * 设立 will，在 client意外断开连接后，broker publish 该 will，必须在 openConnection 前调用该方法
     * @param topic will 的 topic
     * @param payloads  will 的 payloads
     * @param qos   will 的 qos
     * @param retained  是否要 retain 该条消息
     */
    public void setWill(String topic, byte[] payloads, int qos, boolean retained);
    /**
     * 建立一个MQTT连接，第一步操作
     * @param brokerUrl broker url
     * @param clientId  该client的唯一id，不得与其他client重复，否则在qos 0，2 情况下可能会收不到数据
     * @param cleanSession  clean session 为 false 时，client断开连接后，该 client 订阅的 topic 的 qos 为 1 或 2 的信息将
     *                      不会被删除，而是留在 broker 中，直到设备再次上线，再为该 client 推送，为 true 时，client 断开连
     *                      接后，其订阅的 topic 都会被删除
     * @throws MqttException
     */
    public void openConnection(String brokerUrl, String clientId, boolean cleanSession) throws MqttException;

    /**
     * 设置该 client 的回调对象，当订阅消息时，通过设置回调对象，在其 doArrive 方法中处理收到的消息
     * @param callBack  回调对象
     */
    public void setMessageArriveCallBack(IMessageArrive callBack);

    /**
     * 断开连接
     */
    public void disconnect();

    /**
     * 发布一条消息
     * @param topicName 该消息的 topic
     * @param qos   0：发送消息不加确认，至多一次；1：至少一次；2：正好一次
     * @param payload
     * @throws MqttException
     */
    public void publish(String topicName, int qos, byte[] payload) throws MqttException;

    /**
     * 订阅消息
     * @param topicName 订阅的 topic
     * @param qos   0：发送消息不加确认，至多一次；1：至少一次；2：正好一次
     * @throws MqttException
     */
    public void subscribe(String topicName, int qos) throws MqttException;
}
