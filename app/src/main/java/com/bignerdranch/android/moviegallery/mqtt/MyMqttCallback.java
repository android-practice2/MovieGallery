package com.bignerdranch.android.moviegallery.mqtt;

import android.util.Log;

import com.bignerdranch.android.moviegallery.chat.repository.MessageRepository;
import com.bignerdranch.android.moviegallery.chat.room.entity.Message;
import com.bignerdranch.android.moviegallery.http.model.ChatPostMsg;
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
    public void messageArrived(String topic, MqttMessage mqttMsg) throws Exception {
        String text = new String(mqttMsg.getPayload(), StandardCharsets.UTF_8);
        Log.i(getClass().getSimpleName(), "messageArrived,topic:" + topic + ",message:" + text);
        ChatPostMsg msg = JsonUtil.fromJsonStr(text, ChatPostMsg.class);
        Message entity = new Message(msg.getPeerUid(), Message.TYPE_PEER, msg.getContent());
        mMessageRepository.insertAll(entity)
                .subscribe()
        ;
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i(getClass().getSimpleName(), "deliveryComplete " + token);

    }
}