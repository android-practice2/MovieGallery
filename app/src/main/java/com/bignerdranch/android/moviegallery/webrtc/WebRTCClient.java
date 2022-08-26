package com.bignerdranch.android.moviegallery.webrtc;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.bignerdranch.android.moviegallery.util.JsonUtil;
import com.bignerdranch.android.moviegallery.webrtc.model.IceCandidateDTO;
import com.bignerdranch.android.moviegallery.webrtc.model.SessionDescriptionDTO;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.SocketClient;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.constants.SinglingConstants;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.SignalingMessage;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONObject;
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class WebRTCClient {
    public static final boolean FEATURE_DATA_CHANNEL_ENABLE = true;
    public static final int VIDEO_RESOLUTION_WIDTH = 320;
    public static final int VIDEO_RESOLUTION_HEIGHT = 240;
    public static final int FPS = 60;
    //    public static final String STUN_SERVER_URL = "stun:stun.l.google.com:19302";
    public static final String STUN_SERVER_URL = "stun:socialme.hopto.org:3478"; //not ssl
    private final SocketClient mSocketClient = SocketClient.getInstance();
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


    private String room;

    // dataChannel
    private WebRTCDataChannel mWebRTCDataChannel;

    private Context applicationContext;


    private static WebRTCClient instance;//need be set null while end call

    public static WebRTCClient getInstance() {
        return instance;
    }

    public WebRTCClient(Context applicationContext, String room) {
        this.applicationContext = applicationContext;
        this.room = room;
        setupConnection(applicationContext);
        //setup signaling listener
        SocketClient.setSignalingCallback(new SocketSignalingCallback());

        instance = this;

// create media component: mCameraVideoCapturer
        setupCameraCapturer(applicationContext);

    }

    public WebRTCClient(Application applicationContext,
                        String room, SurfaceViewRenderer localSurfaceViewRenderer,
                        SurfaceViewRenderer remoteSurfaceViewRenderer,
                        WebRTCDataChannel.Callback messagingCallback
    ) {
        this(applicationContext, room);


        bindView(localSurfaceViewRenderer, remoteSurfaceViewRenderer, messagingCallback);


    }

    public void bindView(SurfaceViewRenderer localSurfaceViewRenderer, SurfaceViewRenderer remoteSurfaceViewRenderer
            , WebRTCDataChannel.Callback messagingCallback) {
        // init SurfaceViewRenderer
        setupSurface(localSurfaceViewRenderer, remoteSurfaceViewRenderer);

        startStreamingLocal();

        setupDataChannel(messagingCallback);
    }

    private void setupDataChannel(WebRTCDataChannel.Callback messagingCallback) {
        if (FEATURE_DATA_CHANNEL_ENABLE) {
            mWebRTCDataChannel = new WebRTCDataChannel(mPeerConnection, messagingCallback);
        }
    }

    private void setupSurface(SurfaceViewRenderer localSurfaceViewRenderer, SurfaceViewRenderer remoteSurfaceViewRenderer) {
        this.localSurfaceViewRenderer = localSurfaceViewRenderer;
        this.remoteSurfaceViewRenderer = remoteSurfaceViewRenderer;
        initSurfaceViewRenderer(localSurfaceViewRenderer);
        initSurfaceViewRenderer(remoteSurfaceViewRenderer);
    }

    private void setupCameraCapturer(Context applicationContext) {
        Camera2Enumerator camera2Enumerator = new Camera2Enumerator(applicationContext);
        for (String deviceName : camera2Enumerator.getDeviceNames()) {
            boolean frontFacing = camera2Enumerator.isFrontFacing(deviceName);
            if (frontFacing) {
                Log.i(getClass().getSimpleName(), "camera_deviceName:" + deviceName);
                mCameraVideoCapturer = camera2Enumerator.createCapturer(deviceName, null);
                break;
            }
        }
        if (mCameraVideoCapturer == null) {
            throw new IllegalStateException();
        }
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

        mPeerConnection = mPeerConnectionFactory.createPeerConnection(
                iceServers,
                new PeerConnectionObserver()
        );
    }


    public void start() {

        startSignaling();

        if (FEATURE_DATA_CHANNEL_ENABLE) {
            mWebRTCDataChannel.setupDataChannel();

        }
    }


    private void startSignaling() {
        MediaConstraints constraints = new MediaConstraints();
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        mPeerConnection.createOffer(new BaseSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                SignalingMessage message = new SignalingMessage();
                message.setRoom(room);
                message.setType(SinglingConstants.OFFER);
                message.setContent(sessionDescription);
                mSocketClient.messaging(message);
            }
        }, constraints);
    }


    private void startStreamingLocal() {
        mVideoSource = mPeerConnectionFactory.createVideoSource(false);
        mAudioSource = mPeerConnectionFactory.createAudioSource(new MediaConstraints());


        SurfaceTextureHelper helper = SurfaceTextureHelper.create(Thread.currentThread().getName(), mEglBase.getEglBaseContext());
        mCameraVideoCapturer.initialize(helper, localSurfaceViewRenderer.getContext(), mVideoSource.getCapturerObserver());
        mCameraVideoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);

        mVideoTrack = mPeerConnectionFactory.createVideoTrack("local_video_track", mVideoSource);
//        mVideoTrack.setEnabled(true);
        mVideoTrack.addSink(localSurfaceViewRenderer);
        mAudioTrack = mPeerConnectionFactory.createAudioTrack("local_audio_track", mAudioSource);

        MediaStream localStream = mPeerConnectionFactory.createLocalMediaStream("local_stream");
        localStream.addTrack(mVideoTrack);
        localStream.addTrack(mAudioTrack);
        mPeerConnection.addStream(localStream);
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

    public void endCall() {
        if (instance == null) {
            Log.e(getClass().getSimpleName(), "mPeerConnection_is_null");
            return;
        }
        instance = null;

        Log.i(getClass().getSimpleName(), "endCall");
        mRemoteMediaStream.videoTracks.get(0).removeSink(remoteSurfaceViewRenderer);
        mRemoteMediaStream.dispose();

        mVideoTrack.removeSink(localSurfaceViewRenderer);

        localSurfaceViewRenderer.release();
        remoteSurfaceViewRenderer.release();

        mVideoSource.dispose();
        mAudioSource.dispose();
        mCameraVideoCapturer.dispose();//must after mVideoSource\mAudioSource  dispose

        mPeerConnection.dispose();//local MediaStream is managed by PeerConnection
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
        mWebRTCDataChannel.sendData(message);

    }

    public class SocketSignalingCallback implements SocketClient.SignalingCallback {
        @Override
        public void onMessage(SignalingMessage message) {
            String type = message.getType();
            switch (type) {
                case SinglingConstants.ICE_CANDIDATE: {
                    IceCandidateDTO dto = JsonUtil.fromObj(message.getContent(), IceCandidateDTO.class);
                    IceCandidate iceCandidate = new IceCandidate(dto.sdpMid, dto.sdpMLineIndex, dto.sdp);

                    mPeerConnection.addIceCandidate(iceCandidate);
                    break;
                }
                case SinglingConstants.OFFER: {
                    SessionDescriptionDTO dto = JsonUtil.fromObj(message.getContent(), SessionDescriptionDTO.class);
                    SessionDescription sessionDescription = new SessionDescription(dto.type, dto.description);

                    mPeerConnection.setRemoteDescription(new BaseSdpObserver(), sessionDescription);

                    MediaConstraints constraints = new MediaConstraints();
                    constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
                    mPeerConnection.createAnswer(new BaseSdpObserver() {
                        @Override
                        public void onCreateSuccess(SessionDescription sessionDescription) {
                            super.onCreateSuccess(sessionDescription);

                            SignalingMessage msg = new SignalingMessage();
                            msg.setRoom(room);
                            msg.setType(SinglingConstants.ANSWER);
                            msg.setContent(sessionDescription);
                            mSocketClient.messaging(msg);
                        }
                    }, constraints);

                    break;
                }
                case SinglingConstants.ANSWER: {
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

            mSocketClient.messaging(message);
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
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
            mWebRTCDataChannel.setupDataChannel(dataChannel);

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


}
