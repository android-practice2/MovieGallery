package com.bignerdranch.android.moviegallery.mqtt;

import com.bignerdranch.android.moviegallery.chat.repository.MessageRepository;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class MqttBeanDef {


    public static final String MQTT_BROKER_URL_BASE = "tcp://socialme.hopto.org:1883";


    @Provides
    @Singleton
    public static MqttClient sMqttClient() {
        MqttClient client = null; //Persistence
        try {
            client = new MqttClient(
                    MQTT_BROKER_URL_BASE, //URI
                    MqttClient.generateClientId(), //ClientId
                    new MemoryPersistence());

        } catch (MqttException e) {
            e.printStackTrace();
        }

        return client;
    }

    @Provides
    @Singleton
    public static MyMqttCallback sMyMqttCallback(MessageRepository messageRepository) {
        return new MyMqttCallback(messageRepository);
    }

    @Provides
    @Singleton
    public static AppMqttClient sAppMqttClient(MqttClient sMqttClient, MyMqttCallback sMyMqttCallback) {
        return new AppMqttClient(sMqttClient, sMyMqttCallback);
    }

}
