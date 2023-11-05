package com.path.variable.medidoc.fileprocessor.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractSubscriber<T> implements MqttCallback {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSubscriber.class);

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected final IMqttClient mqttClient;

    public AbstractSubscriber(IMqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }


    @Override
    public void disconnected(MqttDisconnectResponse response) {
        LOG.info("Disconnected from {} because {}", response.getServerReference(), response.getReasonString());
    }

    @Override
    public void mqttErrorOccurred(MqttException exception) {
        LOG.error("An error occurred while processing an mqtt message",exception);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        readMessage(message);
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        LOG.info("Connection established with {}", serverURI);
    }

    @Override
    public void deliveryComplete(IMqttToken token) {

    }

    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {

    }

    public abstract String getTopic();

    protected void readMessage(MqttMessage message) {
        T mappedMessage = null;
        try {
            mappedMessage = OBJECT_MAPPER.readValue(message.getPayload(), getMessageClass());
        } catch (IOException e) {
            LOG.error("Error while reading message {}", message.getPayload(), e);
        }
        if (mappedMessage != null) {
            processMessage(mappedMessage);
        }
    }

    protected abstract void processMessage(T mappedMessage);

    protected abstract Class<T> getMessageClass();
}
