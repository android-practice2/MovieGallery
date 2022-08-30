package com.bignerdranch.android.moviegallery.mqtt;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bignerdranch.android.moviegallery.constants.Constants;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class MqttWork extends Worker {
    private int uid;
    private MqttClient client;
    private Context context;

    @AssistedInject
    public MqttWork(@Assisted @NonNull Context context,
                    @Assisted @NonNull WorkerParameters workerParams
            , MqttClient client
    ) {
        super(context, workerParams);
        this.client = client;
        this.context = context;
        this.uid = workerParams.getInputData().getInt(Constants.EXTRA_UID, -1);

    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(getClass().getSimpleName(), "doWork");
        if (uid < 0) {
            Log.e(getClass().getSimpleName(), "uid_is_null before doWork");
            return Result.failure();
        }
        if (client.isConnected()) {
            return Result.success();
        }
        client.setCallback(new MyMqttCallback());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
//                options.setUserName("username");
//                options.setPassword("password".toCharArray());
        options.setAutomaticReconnect(true);
        options.setMaxReconnectDelay(128000);

        try {
            client.connect(options);
            client.subscribe("u/" + uid);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return Result.success();


    }

    public class MyMqttCallback implements MqttCallbackExtended {
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
            // TODO: 2022/8/30 message  write and update to Room
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.i(getClass().getSimpleName(), "deliveryComplete " + token);

        }
    }
}
