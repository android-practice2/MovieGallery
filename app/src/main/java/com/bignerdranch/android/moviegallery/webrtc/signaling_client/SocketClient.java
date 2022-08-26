package com.bignerdranch.android.moviegallery.webrtc.signaling_client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bignerdranch.android.moviegallery.chat.VideoActivity;
import com.bignerdranch.android.moviegallery.util.JsonUtil;
import com.bignerdranch.android.moviegallery.webrtc.WebRTCClient;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.constants.EventConstants;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.ByeRequest;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.CallRequest;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.JoinRequest;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.JoinServerPush;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.SignalingMessage;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SocketClient {
    private static final String TAG = "SocketClient";

    private static final String SIGNALING_SERVER_URL = "https://socialme.hopto.org/";
    private Socket mSocket;
    private RoomCallback mRoomCallback;
    private SignalingCallback mSignalingCallback;
    private Context applicationContext;
    private CountDownLatch mCountDownLatch;

    private static final SocketClient SINGLETON = new SocketClient();

    public static SocketClient getInstance() {
        return SINGLETON;
    }

    public interface RoomCallback {

        void onCreated(Object... args);

        void onBusy(Object... args);

        void onOffline(Object... args);

        void onJoined(Object... args);

        void onReady(Object... args);

        void onPeer_leaved(Object... args);

        void onBye(Object... args);

    }

    public interface SignalingCallback {
        void onMessage(SignalingMessage args);

    }

    public void call(CallRequest request) {
        Log.i(getClass().getSimpleName(), "doCall " + request);
        mSocket.emit(EventConstants.CALL, JsonUtil.toJsonObject(request));
    }

    public void bye(ByeRequest request) {
        Log.i(getClass().getSimpleName(), "doBye " + request);
        mSocket.emit(EventConstants.BYE, JsonUtil.toJsonObject(request));

    }

    public void messaging(SignalingMessage message) {
        Log.i(getClass().getSimpleName(), "doMessaging " + message);
        mSocket.emit(EventConstants.MESSAGE, JsonUtil.toJsonObject(message));
    }

    public static void ensureSocket(Context applicationContext, Integer uid) {
        Log.i(TAG, "ensureSocket,uid:" + uid);
        SINGLETON.applicationContext = applicationContext;
        SINGLETON.doEnsureSocket(uid);
    }


    public static void setRoomCallback(RoomCallback roomCallback) {
        SINGLETON.mRoomCallback = roomCallback;
    }

    public static void setSignalingCallback(SignalingCallback signalingCallback) {
        SINGLETON.mSignalingCallback = signalingCallback;
    }


    private void doEnsureSocket(Integer uid) {
        if (mSocket == null) {
            try {
                OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                        .hostnameVerifier(hostnameVerifier)
//                        .sslSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault(), trustManager)
                        .connectTimeout(2, TimeUnit.SECONDS)
                        .writeTimeout(3, TimeUnit.SECONDS)//if HTTP long-polling, above the `pingInterval + pingTimeout` value from the server
                        .readTimeout(3, TimeUnit.SECONDS)//if HTTP long-polling, HTTP long-polling above the `pingInterval + pingTimeout` value from the server
                        .build();
                IO.setDefaultOkHttpCallFactory(new Call.Factory() {
                    @NonNull
                    @Override
                    public Call newCall(@NonNull Request request) {
                        return okHttpClient.newCall(request);
                    }
                });
                IO.setDefaultOkHttpWebSocketFactory(new WebSocket.Factory() {
                    @NonNull
                    @Override
                    public WebSocket newWebSocket(@NonNull Request request, @NonNull WebSocketListener webSocketListener) {
                        return okHttpClient.newWebSocket(request, webSocketListener);
                    }
                });

                IO.Options opts = new IO.Options();
                opts.reconnection = true;
                opts.reconnectionAttempts = 1;
                opts.reconnectionDelay = 2000;
                opts.reconnectionDelayMax = 10000;
                opts.timeout = 3000;//connection timeout
                opts.transports = new String[]{io.socket.engineio.client.transports.WebSocket.NAME};

                mSocket = IO.socket(SIGNALING_SERVER_URL + "?uid=" + uid, opts);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }


            setupSocketEventListener(uid);

        }

        if (!mSocket.connected()) {
            mSocket.connect();

        }
    }

    public CountDownLatch getCountDownLatch() {
        return mCountDownLatch;
    }

    private void setupSocketEventListener(Integer uid) {
        mSocket
                .on(EventConstants.JOIN, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onJoin" + " " + Arrays.toString(args));
                        JoinServerPush joinServerPush = JsonUtil.fromJsonObject((JSONObject) args[0], JoinServerPush.class);
                        Integer peerUid = joinServerPush.getPeerUid();
                        String room = joinServerPush.getRoom();
                        new WebRTCClient(applicationContext, room);

                        Intent intent = VideoActivity.newIntent(applicationContext, peerUid, room, false);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        applicationContext.startActivity(intent);

                        mCountDownLatch = new CountDownLatch(1);
                        try {
                            mCountDownLatch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        JoinRequest joinRequest = new JoinRequest();
                        joinRequest.setRoom(joinServerPush.getRoom());
                        joinRequest.setUid(joinServerPush.getUid());
                        mSocket.emit(EventConstants.JOIN, JsonUtil.toJsonObject(joinRequest));
                    }
                })
                .on(EventConstants.CREATED, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {//caller receive
                        Log.i(TAG, "onCreated" + " " + Arrays.toString(args));
                        mRoomCallback.onCreated(args);
                    }
                })
                .on(EventConstants.JOINED, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {//callee receive
                        Log.i(TAG, "onJoined" + " " + Arrays.toString(args));
//                        if (mRoomCallback != null) {//mRoomCallback may have not been set
//                            mRoomCallback.onJoined(args);
//                        }
                        mRoomCallback.onJoined(args);

                    }
                })
                .on(EventConstants.BUSY, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onBusy" + " " + Arrays.toString(args));
                        mRoomCallback.onBusy(args);
                    }
                })

                .on(EventConstants.READY, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onReady" + " " + Arrays.toString(args));
//                        if (mRoomCallback != null) {
//                            mRoomCallback.onReady(args);
//
//                        }
                        mRoomCallback.onReady(args);

                    }
                }).on(EventConstants.OFFLINE, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onOffline" + " " + Arrays.toString(args));
                        mRoomCallback.onOffline(args);

                    }
                }).on(EventConstants.PEER_LEAVED, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onPeer_leaved" + " " + Arrays.toString(args));
                        mRoomCallback.onPeer_leaved(args);

                    }
                }).on(EventConstants.BYE, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onBye" + " " + Arrays.toString(args));
                        mRoomCallback.onBye(args);

                    }
                }).on(EventConstants.MESSAGE, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onMessage" + " " + Arrays.toString(args));
                        JSONObject jsonObject = (JSONObject) args[0];
                        SignalingMessage signalingMessage = JsonUtil.fromJsonObject(jsonObject, SignalingMessage.class);
                        mSignalingCallback.onMessage(signalingMessage);
                    }
                })
                .on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onConnect" + " " + Arrays.toString(args));
                    }
                })
                .on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onDisconnect" + " " + Arrays.toString(args));
//                        mSocket.connect();  //already configed auto reconnect
                    }
                })
                .on(Socket.EVENT_PING, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onPing" + " " + Arrays.toString(args));

                    }
                })
                .on(Socket.EVENT_PONG, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onPong" + " " + Arrays.toString(args));

                    }
                })

        ;
    }


}
