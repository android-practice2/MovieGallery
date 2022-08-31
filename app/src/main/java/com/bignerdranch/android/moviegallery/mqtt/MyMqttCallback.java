package com.bignerdranch.android.moviegallery.mqtt;

import android.util.Log;

import com.bignerdranch.android.moviegallery.chat.repository.MessageRepository;
import com.bignerdranch.android.moviegallery.chat.room.entity.Message;
import com.bignerdranch.android.moviegallery.util.JsonUtil;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

public class MyMqttCallback implements MqttCallbackExtended {
    private final MessageRepository mMessageRepository;

    @Inject
    public MyMqttCallback(MessageRepository messageRepository) {
        mMessageRepository = messageRepository;
    }


    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.i(getClass().getSimpleName(), "connectComplete,reconnect:" + reconnect + ",serverURI:" + serverURI);
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.e(getClass().getSimpleName(), "connectionLost", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String text = new String(message.getPayload(), StandardCharsets.UTF_8);
        Message msg = JsonUtil.fromJsonStr(text, Message.class);
        mMessageRepository.insertAll(msg)
                .subscribe()
        ;
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i(getClass().getSimpleName(), "deliveryComplete " + token);

    }
}