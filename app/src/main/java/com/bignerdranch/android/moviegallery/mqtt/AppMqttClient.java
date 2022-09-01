package com.bignerdranch.android.moviegallery.mqtt;

import android.util.Log;

import com.bignerdranch.android.moviegallery.chat.room.entity.Message;
import com.bignerdranch.android.moviegallery.util.JsonUtil;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.MaybeSource;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AppMqttClient {
    public static final String TAG = "AppMqttClient";

    private final MqttClient mMqttClient;
    private final MyMqttCallback mMyMqttCallback;

    @Inject
    public AppMqttClient(MqttClient mqttClient, MyMqttCallback myMqttCallback) {
        mMqttClient = mqttClient;
        mMyMqttCallback = myMqttCallback;
    }

    public Maybe<Void> publish(int uid, Message message) {

        return Maybe.defer(new Supplier<MaybeSource<Void>>() {
                    @Override
                    public MaybeSource<Void> get() throws Throwable {
                        try {
                            mMqttClient.publish("u/" + uid, JsonUtil.toJsonStr(message).getBytes(StandardCharsets.UTF_8), 2, false);
                        } catch (Exception e) {
                            Log.e(TAG, "pub_msg error", e);
                            return Maybe.error(e);
                        }
                        Log.i(TAG, "pub_msg success");
                        return Maybe.empty();
                    }
                })
                .subscribeOn(Schedulers.io())
                ;


    }

    public void start(int uid) {
        if (uid < 0) {
            Log.e(getClass().getSimpleName(), "uid_is_null before doWork");
            return;
        }
        if (mMqttClient.isConnected()) {
            return;
        }
        mMqttClient.setCallback(mMyMqttCallback);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
//                options.setUserName("username");
//                options.setPassword("password".toCharArray());
        options.setAutomaticReconnect(true);
        options.setMaxReconnectDelay(5000);
        options.setKeepAliveInterval(5);
        options.setConnectionTimeout(3);
        options.setCleanSession(false);


        try {
            mMqttClient.connect(options);
            mMqttClient.subscribe("u/" + uid);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


}
