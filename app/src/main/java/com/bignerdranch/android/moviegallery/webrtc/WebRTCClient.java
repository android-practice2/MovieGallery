package com.bignerdranch.android.moviegallery.webrtc;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.bignerdranch.android.moviegallery.util.ByteBufferUti;
import com.bignerdranch.android.moviegallery.util.JsonUtil;
import com.bignerdranch.android.moviegallery.webrtc.model.IceCandidateDTO;
import com.bignerdranch.android.moviegallery.webrtc.model.SessionDescriptionDTO;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.SocketClient;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.constants.SinglingConstants;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.SignalingMessage;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class WebRTCClient {
    public static final String TAG = "WebRTCClient";
    public static final boolean FEATURE_DATA_CHANNEL_ENABLE = true;
    public static final int VIDEO_RESOLUTION_WIDTH = 320;
    public static final int VIDEO_RESOLUTION_HEIGHT = 240;
    public static final int FPS = 60;
    //    public static final String STUN_SERVER_URL = "stun:stun.l.google.com:19302";
    public static final String STUN_SERVER_URL = "stun:socialme.hopto.org:3478"; //not ssl
    private final SocketClient mSocketClient = SocketClient.getInstance();
    private Context applicationContext;
    // webrtc component
    private EglBase mEglBase;
    private PeerConnectionFactory mPeerConnectionFactory;
    private PeerConnection mPeerConnection;

    // media component
    private CameraVideoCapturer mCameraVideoCapturer;
    private VideoSource mVideoSource;
    private AudioSource mAudioSource;
    private VideoTrack mVideoTrack;
    private AudioTrack mAudioTrack;
    private MediaStream mRemoteMediaStream;

    //media output component
    private SurfaceViewRenderer localSurfaceViewRenderer;
    private SurfaceViewRenderer remoteSurfaceViewRenderer;


    //     data channel
    private DataChannelCallback mDataChannelCallback;
    private DataChannel mDataChannel;

    //biz args
    private String room;


    private static WebRTCClient instance;//need be set null while end call

    public static WebRTCClient getInstance() {
        return instance;
    }


    public WebRTCClient(Application applicationContext,
                        String room, SurfaceViewRenderer localSurfaceViewRenderer,
                        SurfaceViewRenderer remoteSurfaceViewRenderer,
                        DataChannelCallback dataChannelCallback
    ) {
        this.applicationContext = applicationContext;
        this.room = room;
        setupConnection(applicationContext);

        SocketClient.setSignalingCallback(new SocketSignalingCallback());

        setupDataChannel(dataChannelCallback);

        setupSurface(localSurfaceViewRenderer, remoteSurfaceViewRenderer);

        startStreamingLocal();

        instance = this;


    }

    private void setupDataChannel(DataChannelCallback messagingCallback) {
        if (FEATURE_DATA_CHANNEL_ENABLE) {
            mDataChannel = mPeerConnection.createDataChannel("dataChannel", new DataChannel.Init());
            mDataChannel.registerObserver(new DataChannelObserver());
            mDataChannelCallback = messagingCallback;
        }
    }


    private void setupSurface(SurfaceViewRenderer localSurfaceViewRenderer, SurfaceViewRenderer remoteSurfaceViewRenderer) {
        this.localSurfaceViewRenderer = localSurfaceViewRenderer;
        this.remoteSurfaceViewRenderer = remoteSurfaceViewRenderer;
        initSurfaceViewRenderer(localSurfaceViewRenderer);
        initSurfaceViewRenderer(remoteSurfaceViewRenderer);
    }

    private CameraVideoCapturer getCameraCapturer(Context applicationContext) {
        CameraVideoCapturer cameraVideoCapturer = null;
        Camera2Enumerator camera2Enumerator = new Camera2Enumerator(applicationContext);
        for (String deviceName : camera2Enumerator.getDeviceNames()) {
            boolean frontFacing = camera2Enumerator.isFrontFacing(deviceName);
            if (frontFacing) {
                Log.i(getClass().getSimpleName(), "camera_deviceName:" + deviceName);
                cameraVideoCapturer = camera2Enumerator.createCapturer(deviceName, null);
                break;
            }
        }
        if (cameraVideoCapturer == null) {
            throw new IllegalStateException();
        }

        mCameraVideoCapturer = cameraVideoCapturer;
        return cameraVideoCapturer;
    }

    private void setupConnection(Context applicationContext) {
        mEglBase = EglBase.create();

//        create PeerConnectionFactory
        PeerConnectionFactory.InitializationOptions initializationOptions = PeerConnectionFactory.InitializationOptions.builder(applicationContext)
                .setEnableInternalTracer(true)
                .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);
        PeerConnectionFactory.Options factoryOptions = new PeerConnectionFactory.Options();
//        factoryOptions.disableEncryption = true;  // should not set true for dataChannel
//        factoryOptions.disableNetworkMonitor = true;
        mPeerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(mEglBase.getEglBaseContext(), true, true))
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(mEglBase.getEglBaseContext()))
                .setOptions(factoryOptions)
                .createPeerConnectionFactory();
// create PeerConnection and observer
        List<PeerConnection.IceServer> iceServers = new LinkedList<>();
        iceServers.add(PeerConnection.IceServer.builder(STUN_SERVER_URL).createIceServer());

        MediaConstraints constraints = new MediaConstraints();
        constraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        mPeerConnection = mPeerConnectionFactory.createPeerConnection(
                iceServers,
                constraints,
                new PeerConnectionObserver()
        );
    }


    public void startSignaling() {
        MediaConstraints constraints = new MediaConstraints();
//        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        mPeerConnection.createOffer(new BaseSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                mPeerConnection.setLocalDescription(new BaseSdpObserver(), sessionDescription);

                SignalingMessage message = new SignalingMessage();
                message.setRoom(room);
                message.setType(SinglingConstants.OFFER);
                message.setContent(sessionDescription);
                Log.i(TAG, "doOffer," + message);
                mSocketClient.messaging(message);
            }
        }, constraints);
    }


    private void startStreamingLocal() {
        MediaStream localStream = getLocalMediaStream();
        mPeerConnection.addStream(localStream);
    }

    private MediaStream getLocalMediaStream() {
        MediaStream localStream = mPeerConnectionFactory.createLocalMediaStream("local_stream");
        localStream.addTrack(getLocalVideoTrack());
        localStream.addTrack(getLocalAudioTrack());

        return localStream;
    }

    private AudioTrack getLocalAudioTrack() {
        mAudioSource = mPeerConnectionFactory.createAudioSource(new MediaConstraints());
        mAudioTrack = mPeerConnectionFactory.createAudioTrack("local_audio_track", mAudioSource);

        return mAudioTrack;
    }

    private VideoTrack getLocalVideoTrack() {
        // create media component: mCameraVideoCapturer
        CameraVideoCapturer cameraVideoCapturer = getCameraCapturer(applicationContext);

        mVideoSource = mPeerConnectionFactory.createVideoSource(false);
        SurfaceTextureHelper helper = SurfaceTextureHelper.create(Thread.currentThread().getName(), mEglBase.getEglBaseContext());
        cameraVideoCapturer.initialize(helper, localSurfaceViewRenderer.getContext(), mVideoSource.getCapturerObserver());
        cameraVideoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);

        mVideoTrack = mPeerConnectionFactory.createVideoTrack("local_video_track", mVideoSource);
        mVideoTrack.addSink(localSurfaceViewRenderer);

        return mVideoTrack;
    }

    private void initSurfaceViewRenderer(SurfaceViewRenderer surfaceViewRenderer) {
        surfaceViewRenderer.setMirror(true);
        surfaceViewRenderer.setEnableHardwareScaler(true);
        surfaceViewRenderer.init(mEglBase.getEglBaseContext(), null);
    }


    public void setMic(boolean enable) {
        if (mAudioTrack == null) {
            return;
        }
        mAudioTrack.setEnabled(enable);
    }

    public void setVideo(boolean enable) {
        if (mVideoTrack == null) {
            return;
        }
        mVideoTrack.setEnabled(enable);
    }

    public void endCall(String room) {
        if (!Objects.equals(this.room, room)) {//late async event
            Log.i(getClass().getSimpleName(), "endCall_room_diff,this room:" + this.room + ",peer room:" + room);
            return;
        }
        if (instance == null) {
            Log.e(getClass().getSimpleName(), "mPeerConnection_is_null");
            return;
        }
        instance = null;

        Log.i(getClass().getSimpleName(), "endCall");
        if (mRemoteMediaStream != null && mRemoteMediaStream.videoTracks != null) {
            mRemoteMediaStream.videoTracks.get(0).removeSink(remoteSurfaceViewRenderer);
        }

        mVideoTrack.removeSink(localSurfaceViewRenderer);

        localSurfaceViewRenderer.release();
        remoteSurfaceViewRenderer.release();

        mVideoSource.dispose();
        mAudioSource.dispose();
        mCameraVideoCapturer.dispose();//must after mVideoSource\mAudioSource  dispose

        mPeerConnection.dispose();// MediaStream is managed by PeerConnection
        mPeerConnectionFactory.dispose();

    }

    public void setVolume(boolean enable) {
        if (mRemoteMediaStream == null) {
            return;
        }
        mRemoteMediaStream.audioTracks.get(0).setEnabled(enable);

    }

    public void flipCamera() {
        mCameraVideoCapturer.switchCamera(null);
    }

    public void sendMessage(String message) {
        Log.i(getClass().getSimpleName(), "dataChannel_sendData:" + message);

        ByteBuffer byteBuffer = ByteBufferUti.str_to_bb(message);
        DataChannel.Buffer buffer = new DataChannel.Buffer(byteBuffer, false);
        mDataChannel.send(buffer);

    }

    public class SocketSignalingCallback implements SocketClient.SignalingCallback {
        @Override
        public void onMessage(SignalingMessage message) {
            String type = message.getType();
            switch (type) {
                case SinglingConstants.ICE_CANDIDATE: {
                    Log.i(TAG, "onCandidate," + message);
                    IceCandidateDTO dto = JsonUtil.fromObj(message.getContent(), IceCandidateDTO.class);
                    IceCandidate iceCandidate = new IceCandidate(dto.sdpMid, dto.sdpMLineIndex, dto.sdp);

                    mPeerConnection.addIceCandidate(iceCandidate);
                    break;
                }
                case SinglingConstants.OFFER: {
                    Log.i(TAG, "onOffer," + message);
                    SessionDescriptionDTO dto = JsonUtil.fromObj(message.getContent(), SessionDescriptionDTO.class);
                    SessionDescription sessionDescription = new SessionDescription(dto.type, dto.description);

                    mPeerConnection.setRemoteDescription(new BaseSdpObserver(), sessionDescription);

                    MediaConstraints constraints = new MediaConstraints();
                    constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
                    mPeerConnection.createAnswer(new BaseSdpObserver() {
                        @Override
                        public void onCreateSuccess(SessionDescription sessionDescription) {
                            super.onCreateSuccess(sessionDescription);
                            mPeerConnection.setLocalDescription(new BaseSdpObserver(), sessionDescription);

                            SignalingMessage msg = new SignalingMessage();
                            msg.setRoom(room);
                            msg.setType(SinglingConstants.ANSWER);
                            msg.setContent(sessionDescription);
                            Log.i(TAG, "doAnswer," + msg);
                            mSocketClient.messaging(msg);
                        }
                    }, constraints);

                    break;
                }
                case SinglingConstants.ANSWER: {
                    Log.i(TAG, "onAnswer," + message);
                    SessionDescriptionDTO dto = JsonUtil.fromObj(message.getContent(), SessionDescriptionDTO.class);
                    SessionDescription sessionDescription = new SessionDescription(dto.type, dto.description);
                    mPeerConnection.setRemoteDescription(new BaseSdpObserver(), sessionDescription);
                    break;
                }
            }

        }
    }


    public class PeerConnectionObserver implements PeerConnection.Observer {
        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            Log.i(getClass().getSimpleName(), "onIceCandidate" + " " + iceCandidate);
            mPeerConnection.addIceCandidate(iceCandidate);
            SignalingMessage message = new SignalingMessage();
            message.setType(SinglingConstants.ICE_CANDIDATE);
            message.setRoom(room);
            message.setContent(iceCandidate);
            Log.i(getClass().getSimpleName(), "doCandidate," + message);
            mSocketClient.messaging(message);
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {//To get the sound of the interlocutor we don’t need to do anything extra
            Log.i(getClass().getSimpleName(), "onAddStream" + " " + mediaStream);
            mRemoteMediaStream = mediaStream;
            mRemoteMediaStream.videoTracks.get(0).addSink(remoteSurfaceViewRenderer);

        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.i(getClass().getSimpleName(), "onSignalingChange" + " " + signalingState);

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Log.i(getClass().getSimpleName(), "onIceConnectionChange" + " " + iceConnectionState);

        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            Log.i(getClass().getSimpleName(), "onIceConnectionReceivingChange" + " " + b);

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Log.i(getClass().getSimpleName(), "onIceGatheringChange" + " " + iceGatheringState);

        }


        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
            Log.i(getClass().getSimpleName(), "onIceCandidatesRemoved" + " " + iceCandidates);

        }


        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Log.i(getClass().getSimpleName(), "onRemoveStream" + " " + mediaStream);

        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Log.i(getClass().getSimpleName(), "onDataChannel" + " " + dataChannel);
            mDataChannel = dataChannel;
            mDataChannel.registerObserver(new DataChannelObserver());

        }

        @Override
        public void onRenegotiationNeeded() {
            Log.i(getClass().getSimpleName(), "onRenegotiationNeeded");

        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
            Log.i(getClass().getSimpleName(), "onAddTrack" + " " + rtpReceiver + "\n" + Arrays.toString(mediaStreams));

        }
    }

    public class DataChannelObserver implements DataChannel.Observer {
        @Override
        public void onBufferedAmountChange(long l) {
            Log.i(getClass().getSimpleName(), "dataChannel_onBufferedAmountChange:" + l);

        }

        @Override
        public void onStateChange() {
            Log.i(getClass().getSimpleName(), "dataChannel_onStateChange");

            if (mDataChannel.state() == DataChannel.State.OPEN) {
                mDataChannelCallback.onOpen();

            } else {
                mDataChannelCallback.onClose();
            }
        }

        @Override
        public void onMessage(DataChannel.Buffer buffer) {
            ByteBuffer data = buffer.data;
            String message = ByteBufferUti.bb_to_str(data);
            Log.i(getClass().getSimpleName(), "dataChannel_onMessage:" + message);
            mDataChannelCallback.onMessage(message);
        }
    }

    public interface DataChannelCallback {
        void onMessage(String message);

        void onOpen();

        void onClose();

    }

}
