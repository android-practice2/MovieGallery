package com.bignerdranch.android.moviegallery.webrtc;

import android.app.Application;
import android.util.Log;

import com.bignerdranch.android.moviegallery.util.JsonUtil;
import com.bignerdranch.android.moviegallery.webrtc.model.IceCandidateDTO;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.SocketClient;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.constants.SinglingConstants;
import com.bignerdranch.android.moviegallery.webrtc.model.SessionDescriptionDTO;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.SignalingMessage;

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
    public static final int VIDEO_RESOLUTION_WIDTH = 320;
    public static final int VIDEO_RESOLUTION_HEIGHT = 240;
    public static final int FPS = 60;
    public static final String STUN_SERVER_URL = "stun:stun.l.google.com:19302";
    private SocketClient mSocketClient = SocketClient.getInstance();
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

    //media output component
    private SurfaceViewRenderer localSurfaceViewRenderer;
    private SurfaceViewRenderer remoteSurfaceViewRenderer;


    private String room;
    private MediaStream mRemoteMediaStream;

    public WebRTCClient(Application applicationContext,
                        SurfaceViewRenderer localSurfaceViewRenderer,
                        SurfaceViewRenderer remoteSurfaceViewRenderer,
                        String room
    ) {
        this.localSurfaceViewRenderer = localSurfaceViewRenderer;
        this.remoteSurfaceViewRenderer = remoteSurfaceViewRenderer;
        this.room = room;

        mEglBase = EglBase.create();

//        create PeerConnectionFactory
        PeerConnectionFactory.InitializationOptions initializationOptions = PeerConnectionFactory.InitializationOptions.builder(applicationContext)
                .setEnableInternalTracer(true)
                .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);
        PeerConnectionFactory.Options factoryOptions = new PeerConnectionFactory.Options();
        factoryOptions.disableEncryption = true;
        factoryOptions.disableNetworkMonitor = true;
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

// create media component: mCameraVideoCapturer
        Camera2Enumerator camera2Enumerator = new Camera2Enumerator(applicationContext);
        for (String deviceName : camera2Enumerator.getDeviceNames()) {
            boolean frontFacing = camera2Enumerator.isFrontFacing(deviceName);
            if (frontFacing) {
                mCameraVideoCapturer = camera2Enumerator.createCapturer(deviceName, null);
                break;
            }
        }
        if (mCameraVideoCapturer == null) {
            throw new IllegalStateException();
        }


// init SurfaceViewRenderer
        initSurfaceViewRenderer(localSurfaceViewRenderer);
        initSurfaceViewRenderer(remoteSurfaceViewRenderer);

        //setup signaling listener
        SocketClient.setSignalingCallback(new SocketSignalingCallback());


        startStreamingLocal();


    }

    public void start() {

        startSignaling();

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
        if (mPeerConnection == null) {
            return;
        }
        mPeerConnection.close();
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

    public class SocketSignalingCallback implements SocketClient.SignalingCallback {
        @Override
        public void onMessage(SignalingMessage message) {
            String type = message.getType();
            switch (type) {
                case SinglingConstants.ICE_CANDIDATE: {
                    JSONObject jsonObject = (JSONObject) message.getContent();

                    IceCandidateDTO dto = JsonUtil.fromJsonObject(jsonObject, IceCandidateDTO.class);
                    IceCandidate iceCandidate = new IceCandidate(dto.sdpMid, dto.sdpMLineIndex, dto.sdp);

                    mPeerConnection.addIceCandidate(iceCandidate);
                    break;
                }
                case SinglingConstants.OFFER: {
                    JSONObject jsonObject = (JSONObject) message.getContent();
                    SessionDescriptionDTO dto = JsonUtil.fromJsonObject(jsonObject, SessionDescriptionDTO.class);
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
                    JSONObject jsonObject = (JSONObject) message.getContent();
                    SessionDescriptionDTO dto = JsonUtil.fromJsonObject(jsonObject, SessionDescriptionDTO.class);
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
            mediaStream.videoTracks.get(0).addSink(remoteSurfaceViewRenderer);

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
