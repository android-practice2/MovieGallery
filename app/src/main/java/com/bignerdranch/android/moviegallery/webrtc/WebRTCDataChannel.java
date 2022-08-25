package com.bignerdranch.android.moviegallery.webrtc;

import android.util.Log;

import com.bignerdranch.android.moviegallery.util.ByteBufferUti;

import org.webrtc.DataChannel;
import org.webrtc.PeerConnection;

import java.nio.ByteBuffer;

public class WebRTCDataChannel {

    private final PeerConnection mPeerConnection;
    private final Callback mMessagingCallback;

    private DataChannel mDataChannel;

    public WebRTCDataChannel(PeerConnection peerConnection, Callback messagingCallback) {
        mPeerConnection = peerConnection;
        mMessagingCallback = messagingCallback;
    }

    public void sendData(String message) {
        Log.i(getClass().getSimpleName(), "dataChannel_sendData:" + message);

        ByteBuffer byteBuffer = ByteBufferUti.str_to_bb(message);
        DataChannel.Buffer buffer = new DataChannel.Buffer(byteBuffer, false);
        mDataChannel.send(buffer);
    }


    public void setupDataChannel() {
        DataChannel.Init init = new DataChannel.Init();
        init.id = 1;
        mDataChannel = mPeerConnection.createDataChannel("dataChannel", init);
        mDataChannel.registerObserver(new DataChannelObserver());

    }

    /**
     * from peer data channel
     */
    public void setupDataChannel(DataChannel dataChannel) {
        mDataChannel = dataChannel;
        mDataChannel.registerObserver(new DataChannelObserver());

    }


    public class DataChannelObserver implements DataChannel.Observer {
        @Override
        public void onBufferedAmountChange(long l) {
            Log.i(getClass().getSimpleName(), "dataChannel_onBufferedAmountChange:" + l);

        }

        @Override
        public void onStateChange() {
            Log.i(getClass().getSimpleName(), "dataChannel_onStateChange");

        }

        @Override
        public void onMessage(DataChannel.Buffer buffer) {
            if (!buffer.binary) {
                ByteBuffer data = buffer.data;
                String message = ByteBufferUti.bb_to_str(data);
                Log.i(getClass().getSimpleName(), "dataChannel_onMessage:" + message);
                mMessagingCallback.onMessage(message);
            }

        }
    }

    public interface Callback {
        void onMessage(String message);
    }
}
