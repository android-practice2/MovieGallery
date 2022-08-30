package com.bignerdranch.android.moviegallery.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Arrays;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

public class MqttConfig {


    public static final String MQTT_BROKER_URL_BASE = "tcp://socialme.hopto.org:1883";

    @Module
    @InstallIn(SingletonComponent.class)
    public static class BeanDef {

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


    }
}
