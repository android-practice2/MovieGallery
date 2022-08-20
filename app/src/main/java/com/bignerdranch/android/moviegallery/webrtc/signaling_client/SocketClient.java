package com.bignerdranch.android.moviegallery.webrtc.signaling_client;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bignerdranch.android.moviegallery.chat.VideoActivity;
import com.bignerdranch.android.moviegallery.util.JsonUtil;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.constants.EventConstants;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.ByeRequest;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.CallRequest;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.JoinRequest;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.JoinServerPush;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.SignalingMessage;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Arrays;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketClient {
    private static final String TAG = "SocketClient";

    private static final String SIGNALING_SERVER_URL = "http://192.168.0.100:8181/";
    private Socket mSocket;
    private RoomCallback mRoomCallback;
    private SignalingCallback mSignalingCallback;

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

    public static void ensureSocket(Context context, Integer uid) {
        SINGLETON.doEnsureSocket(context, uid);
    }


    public static void setRoomCallback(RoomCallback roomCallback) {
        SINGLETON.mRoomCallback = roomCallback;
    }

    public static void setSignalingCallback(SignalingCallback signalingCallback) {
        SINGLETON.mSignalingCallback = signalingCallback;
    }


    private void doEnsureSocket(Context context, Integer uid) {
        if (mSocket == null) {
            try {
                mSocket = IO.socket(SIGNALING_SERVER_URL + "?uid=" + uid);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }


            setupSocketEventListener(context, uid);

        }

        if (!mSocket.connected()) {
            mSocket.connect();

        }
    }

    private void setupSocketEventListener(Context context, Integer uid) {
        mSocket
                .on(EventConstants.JOIN, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onJoin" + " " + args[0]);
                        JoinServerPush joinServerPush = (JoinServerPush) args[0];
                        Integer peerUid = joinServerPush.getPeerUid();
                        String room = joinServerPush.getRoom();

                        Intent intent = VideoActivity.newIntent(context, peerUid, room, false);
                        context.startActivity(intent);

                        JoinRequest joinRequest = new JoinRequest();
                        joinRequest.setRoom(joinServerPush.getRoom());
                        joinRequest.setUid(joinServerPush.getUid());
                        mSocket.emit(EventConstants.JOIN, joinRequest);
                    }
                })
                .on(EventConstants.CREATED, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onCreated" + " " + args[0]);
                        mRoomCallback.onCreated(args);
                    }
                })
                .on(EventConstants.BUSY, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onBusy" + " " + args[0]);
                        mRoomCallback.onBusy(args);
                    }
                })
                .on(EventConstants.JOINED, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onJoined" + " " + args[0]);

                        mRoomCallback.onJoined(args);

                    }
                })

                .on(EventConstants.READY, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onReady" + " " + args[0]);
                        mRoomCallback.onReady(args);

                    }
                }).on(EventConstants.OFFLINE, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onOffline" + " " + args[0]);
                        mRoomCallback.onOffline(args);

                    }
                }).on(EventConstants.PEER_LEAVED, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onPeer_leaved" + " " + args[0]);
                        mRoomCallback.onPeer_leaved(args);

                    }
                }).on(EventConstants.BYE, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onBye" + " " + args[0]);
                        String room = (String) args[0];

                        ByeRequest request = new ByeRequest();
                        request.setRoom(room);
                        request.setUid(uid);
                        bye(request);

                        mRoomCallback.onBye(args);

                    }
                }).on(EventConstants.MESSAGE, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "onMessage" + " " + args[0]);
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
                        mSocket.connect();
                    }
                })

        ;
    }


}
