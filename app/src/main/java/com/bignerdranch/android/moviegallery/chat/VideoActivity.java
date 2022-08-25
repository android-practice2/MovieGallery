package com.bignerdranch.android.moviegallery.chat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;

import com.bignerdranch.android.moviegallery.BaseActivity;
import com.bignerdranch.android.moviegallery.R;
import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.databinding.ActivityVideoBinding;
import com.bignerdranch.android.moviegallery.webrtc.WebRTCClient;
import com.bignerdranch.android.moviegallery.webrtc.WebRTCDataChannel;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.SocketClient;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.ByeRequest;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.CallRequest;

public class VideoActivity extends BaseActivity {

    private int mPeerUid;

    private com.bignerdranch.android.moviegallery.databinding.ActivityVideoBinding mBinding;
    private String mRoom;
    private WebRTCClient mWebRTCClient;
    private boolean mIsInitiator;
    private ControlState mControlState = new ControlState();
    private SocketClient mSocketClient = SocketClient.getInstance();

    public static Intent newIntent(Context context, Integer peerUid, String room
            , boolean isInitiator) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra(Constants.EXTRA_PEER_UID, peerUid);
        intent.putExtra(Constants.EXTRA_ROOM, room);
        intent.putExtra(Constants.EXTRA_IS_INITIATOR, isInitiator);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityVideoBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SocketClient.setRoomCallback(new SocketRoomCallback());

        mPeerUid = getIntent().getIntExtra(Constants.EXTRA_PEER_UID, -1);
        mRoom = getIntent().getStringExtra(Constants.EXTRA_ROOM);
        mIsInitiator = getIntent().getBooleanExtra(Constants.EXTRA_IS_INITIATOR, false);

        requestPermissionAndSetupWebRTC();
        setupViewBehavior();
        if (mIsInitiator) {
            roomCall();
        }


    }

    @Override
    protected void onDestroy() {
        mWebRTCClient.endCall();

        super.onDestroy();

    }

    private void roomCall() {
        CallRequest callRequest = new CallRequest();
        callRequest.setRoom(mRoom);
        callRequest.setUid(mUid);
        callRequest.setPeerUid(mPeerUid);

        mSocketClient.call(callRequest);
    }

    private void setupViewBehavior() {
        mBinding.micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebRTCClient.setMic(mControlState.micEnable = !mControlState.micEnable);
                mBinding.micButton.setImageDrawable(
                        mControlState.micEnable ?
                                AppCompatResources.getDrawable(VideoActivity.this, R.drawable.ic_baseline_mic_24)
                                : AppCompatResources.getDrawable(VideoActivity.this, R.drawable.ic_baseline_mic_off_24)
                );
            }
        });
        mBinding.videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebRTCClient.setVideo(mControlState.videoEnable = !mControlState.videoEnable);
                mBinding.videoButton.setImageDrawable(
                        mControlState.videoEnable ?
                                AppCompatResources.getDrawable(VideoActivity.this,
                                        R.drawable.ic_baseline_videocam_24
                                )
                                : AppCompatResources.getDrawable(VideoActivity.this,
                                R.drawable.ic_baseline_videocam_off_24)
                );
            }
        });
        mBinding.volumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebRTCClient.setVolume(mControlState.volumeEnable = !mControlState.volumeEnable);
                mBinding.volumeButton.setImageDrawable(
                        mControlState.volumeEnable ?
                                AppCompatResources.getDrawable(VideoActivity.this,
                                        R.drawable.ic_baseline_volume_up_24
                                )
                                : AppCompatResources.getDrawable(VideoActivity.this,
                                R.drawable.ic_baseline_volume_off_24)

                );
            }
        });

        mBinding.endCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ByeRequest byeRequest = new ByeRequest();
                byeRequest.setRoom(mRoom);
                byeRequest.setUid(mUid);
                mSocketClient.bye(byeRequest);

                mWebRTCClient.endCall();
                finish();
            }
        });
        mBinding.flipCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebRTCClient.flipCamera();
            }
        });

        setupMessagingViewBehavior();


    }

    private void setupMessagingViewBehavior() {
        if (WebRTCClient.FEATURE_DATA_CHANNEL_ENABLE) {
            mBinding.messageReceived.setVisibility(View.GONE);
            mBinding.messageLayout.setVisibility(View.VISIBLE);
            mBinding.sendBtn.setVisibility(View.GONE);

            mBinding.messageText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    mBinding.sendBtn.setVisibility(View.VISIBLE);
                }
            });


            mBinding.sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String message = mBinding.messageText.getText().toString();
                    mWebRTCClient.sendMessage(message);
                    mBinding.messageText.setText("");
                }
            });

        } else {
            mBinding.messageReceived.setVisibility(View.GONE);
            mBinding.messageLayout.setVisibility(View.GONE);

        }
    }

    public static class ControlState {
        public boolean micEnable = true;
        public boolean videoEnable = true;
        public boolean volumeEnable = true;

    }

    private void requestPermissionAndSetupWebRTC() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO},
                    Constants.PERM_REQ_CODE_CAMERA_AUDIO);

        } else {
            instantiateWebRTC();

        }
    }

    private void instantiateWebRTC() {
        mWebRTCClient = new WebRTCClient(getApplication(),
                mBinding.localSurface,
                mBinding.remoteSurface,
                mRoom,
                new WebRTCMessagingCallback()
        );
    }

    private void startWebRTC() {
        mWebRTCClient.start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.PERM_REQ_CODE_CAMERA_AUDIO
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionAndSetupWebRTC();
        }

    }

    public class SocketRoomCallback implements SocketClient.RoomCallback {

        @Override
        public void onCreated(Object... args) {//delegator have done logging
        }

        @Override
        public void onJoined(Object... args) {

        }

        @Override
        public void onReady(Object... args) {
            if (mIsInitiator) {
                startWebRTC();
            }
        }

        @Override
        public void onBusy(Object... args) {
            if (mWebRTCClient != null) {
                mWebRTCClient.endCall();
            }
        }

        @Override
        public void onOffline(Object... args) {
            if (mWebRTCClient != null) {
                mWebRTCClient.endCall();

            }

        }


        @Override
        public void onPeer_leaved(Object... args) {
            if (mWebRTCClient != null) {
                mWebRTCClient.endCall();

            }
        }

        @Override
        public void onBye(Object... args) {
            if (mWebRTCClient != null) {
                mWebRTCClient.endCall();
            }
        }
    }

    public class WebRTCMessagingCallback implements WebRTCDataChannel.MessagingCallback {
        @Override
        public void onMessage(String message) {
            mBinding.messageReceived.setVisibility(View.VISIBLE);
            mBinding.messageReceived.setText(message);
        }
    }

}
