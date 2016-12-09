package com.sfc.mqtt.util;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * 阻塞式发布消息
 * Created by Prophet on 2016/12/8.
 */
public class SimpleMqttClient implements IMqttClient {

    MqttClient client = null;
    IMessageArrive callBack = null;

    String willTopic = null;
    byte[] willPayloads = null;
    int willQos = 0;
    boolean willRetained = false;


    /**
     * 设立 will，在 client意外断开连接后，broker publish 该 will，必须在 openConnection 前调用该方法
     *
     * @param topic    will 的 topic
     * @param payloads will 的 payloads
     * @param qos      will 的 qos
     * @param retained 是否要 retain 该条消息
     */
    @Override
    public void setWill(String topic, byte[] payloads, int qos, boolean retained) {
        willTopic = topic;
        willPayloads = payloads;
        willQos = qos;
        willRetained = retained;
    }

    /**
     * 建立一个MQTT连接，第一步操作
     *
     * @param brokerUrl    broker url
     * @param clientId     该client的唯一id，不得与其他client重复，否则在qos 0，2 情况下可能会收不到数据
     * @param cleanSession clean session 为 false 时，client断开连接后，该 client 订阅的 topic 的 qos 为 1 或 2 的信息将
     *                     不会被删除，而是留在 broker 中，直到设备再次上线，再为该 client 推送，为 true 时，client 断开连
     *                     接后，其订阅的 topic 都会被删除
     * @throws MqttException
     */
    @Override
    public void openConnection(String brokerUrl, String clientId, boolean cleanSession) throws MqttException {
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttConnectOptions conOpt = new MqttConnectOptions();
            conOpt.setCleanSession(cleanSession);
            // Set will for this connection
            if (willTopic != null) conOpt.setWill(willTopic, willPayloads, willQos, willRetained);
            // Construct an MQTT blocking mode client
            client = new MqttClient(brokerUrl, clientId, persistence);
            // Set this wrapper as the callback handler
            client.setCallback(this);
            // Connect to the MQTT server
            client.connect(conOpt);

            System.out.println("connect to " + brokerUrl);

        } catch (MqttException e) {
            e.printStackTrace();
            System.out.println("unable connect to " + brokerUrl);
        }
    }

    /**
     * 设置该 client 的回调对象，当订阅消息时，通过设置回调对象，在其 doArrive 方法中处理收到的消息
     *
     * @param callBack 回调对象
     */
    @Override
    public void setMessageArriveCallBack(IMessageArrive callBack) {
        this.callBack = callBack;
    }

    /**
     * 断开连接
     */
    @Override
    public void disconnect() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
            System.out.println("unable to disconnect");
        }
    }

    /**
     * 发布一条消息
     *
     * @param topicName 该消息的 topic
     * @param qos       0：发送消息不加确认，至多一次；1：至少一次；2：正好一次
     * @param payload
     * @throws MqttException
     */
    @Override
    public void publish(String topicName, int qos, byte[] payload) throws MqttException {

        if (client == null || client.isConnected() == false ) {
            System.out.println("no connection");
            return;
        }

        // Create and configure a message
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);

        // Send the message to the server, control is not returned until
        // it has been delivered to the server meeting the specified
        // quality of service.
        client.publish(topicName, message);
        System.out.println("publish to topic [" + topicName + "] at qos " + qos + " with payload: " + new String(payload));
    }

    /**
     * 订阅消息
     *
     * @param topicName 订阅的 topic
     * @param qos       0：发送消息不加确认，至多一次；1：至少一次；2：正好一次
     * @throws MqttException
     */
    @Override
    public void subscribe(String topicName, int qos) throws MqttException {

        if (client == null || client.isConnected() == false ) {
            System.out.println("no connection");
            return;
        }

        // Subscribe to the requested topic
        // The QoS specified is the maximum level that messages will be sent to the client at.
        // For instance if QoS 1 is specified, any messages originally published at QoS 2 will
        // be downgraded to 1 when delivering to the client but messages published at 1 and 0
        // will be received at the same level they were published at.
        client.subscribe(topicName, qos);
        System.out.println("subscribe to topic [" + topicName + "] at qos " + qos);
    }

    /**
     * This method is called when the connection to the server is lost.
     *
     * @param cause the reason behind the loss of connection.
     */
    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("connection lost");

        try {
            client.reconnect();
            System.out.println("reconnect success");
        } catch (MqttException e) {
            e.printStackTrace();
            System.out.println("reconnect fail");
        }
    }

    /**
     * This method is called when a message arrives from the server.
     * <p>
     * <p>
     * This method is invoked synchronously by the MQTT client. An
     * acknowledgment is not sent back to the server until this
     * method returns cleanly.</p>
     * <p>
     * If an implementation of this method throws an <code>Exception</code>, then the
     * client will be shut down.  When the client is next re-connected, any QoS
     * 1 or 2 messages will be redelivered by the server.</p>
     * <p>
     * Any additional messages which arrive while an
     * implementation of this method is running, will build up in memory, and
     * will then back up on the network.</p>
     * <p>
     * If an application needs to persist data, then it
     * should ensure the data is persisted prior to returning from this method, as
     * after returning from this method, the message is considered to have been
     * delivered, and will not be reproducible.</p>
     * <p>
     * It is possible to send a new message within an implementation of this callback
     * (for example, a response to this message), but the implementation must not
     * disconnect the client, as it will be impossible to send an acknowledgment for
     * the message being processed, and a deadlock will occur.</p>
     *
     * @param topic   name of the topic on the message was published to
     * @param message the actual message.
     * @throws Exception if a terminal error has occurred, and the client should be
     *                   shut down.
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (callBack == null) {
            System.out.println("no callback object");
            return;
        }
        callBack.doArrive(topic, message);
    }

    /**
     * Called when delivery for a message has been completed, and all
     * acknowledgments have been received. For QoS 0 messages it is
     * called once the message has been handed to the network for
     * delivery. For QoS 1 it is called when PUBACK is received and
     * for QoS 2 when PUBCOMP is received. The token will be the same
     * token as that returned when the message was published.
     *
     * @param token the delivery token associated with the message.
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("delivery complete");
    }
}
