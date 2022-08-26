package com.bignerdranch.android.moviegallery.chat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.bignerdranch.android.moviegallery.BaseActivity;
import com.bignerdranch.android.moviegallery.R;
import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.databinding.ActivityVideoBinding;
import com.bignerdranch.android.moviegallery.webrtc.WebRTCClient;
import com.bignerdranch.android.moviegallery.webrtc.WebRTCDataChannel;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.SocketClient;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.ByeRequest;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.CallRequest;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoActivity extends BaseActivity {

    private int mPeerUid;

    private com.bignerdranch.android.moviegallery.databinding.ActivityVideoBinding mBinding;
    private String mRoom;
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
        if (mSocketClient.getCountDownLatch() != null) {//callee
            mSocketClient.getCountDownLatch().countDown();
        }

        mPeerUid = getIntent().getIntExtra(Constants.EXTRA_PEER_UID, -1);
        mRoom = getIntent().getStringExtra(Constants.EXTRA_ROOM);
        mIsInitiator = getIntent().getBooleanExtra(Constants.EXTRA_IS_INITIATOR, false);

        setupWebRTC();
        setupViewBehavior();
        if (mIsInitiator) {
            roomCall();
        }


    }

    @Override
    protected void onDestroy() {
        endCall(mRoom);
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
                WebRTCClient.getInstance().setMic(mControlState.micEnable = !mControlState.micEnable);
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
                WebRTCClient.getInstance().setVideo(mControlState.videoEnable = !mControlState.videoEnable);
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
                WebRTCClient.getInstance().setVolume(mControlState.volumeEnable = !mControlState.volumeEnable);
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

                endCall(mRoom);

            }
        });
        mBinding.flipCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebRTCClient.getInstance().flipCamera();
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
                    WebRTCClient.getInstance().sendMessage(message);
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

    @AfterPermissionGranted(Constants.PERM_REQ_CODE_CAMERA_AUDIO)
    private void setupWebRTC() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            instantiateWebRTC();
        } else {
            EasyPermissions.requestPermissions(this, "", Constants.PERM_REQ_CODE_CAMERA_AUDIO, perms);
        }
    }

//    private void setupWebRTC() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED
//                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
//                != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
//                            Manifest.permission.RECORD_AUDIO},
//                    Constants.PERM_REQ_CODE_CAMERA_AUDIO);
//
//        } else {
//            instantiateWebRTC();
//
//        }
//    }

    private void instantiateWebRTC() {
        if (mIsInitiator) {
            new WebRTCClient(getApplication(),
                    mRoom,
                    mBinding.localSurface,
                    mBinding.remoteSurface,
                    new WebRTCDataChannelCallback()
            );
        } else {//callee, webRTCClient have been instantiated
            WebRTCClient.getInstance().bindView(mBinding.localSurface,
                    mBinding.remoteSurface,
                    new WebRTCDataChannelCallback());

        }

    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == Constants.PERM_REQ_CODE_CAMERA_AUDIO
//                && grantResults[0] == PackageManager.PERMISSION_GRANTED
//                && grantResults[1] == PackageManager.PERMISSION_GRANTED
//        ) {
//            setupWebRTC();
//        }
//
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public class SocketRoomCallback implements SocketClient.RoomCallback {

        @Override
        public void onCreated(Object... args) {//delegator have done logging
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VideoActivity.this, "onCreated", Toast.LENGTH_SHORT).show();

                }
            });
        }

        @Override
        public void onJoined(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VideoActivity.this, "onJoined", Toast.LENGTH_SHORT).show();
                }
            });

        }

        @Override
        public void onReady(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VideoActivity.this, "onReady", Toast.LENGTH_SHORT).show();

                }
            });
            if (mIsInitiator) {
                WebRTCClient.getInstance().start();
            } else {

                // TODO: 2022/8/26
            }
        }


        @Override
        public void onOffline(String room) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VideoActivity.this, "onOffline", Toast.LENGTH_SHORT).show();

                }
            });
            endCall(room);

        }


        @Override
        public void onBye(String room) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VideoActivity.this, "onBye", Toast.LENGTH_SHORT).show();
                }
            });
            Log.i(TAG, "onBye_endCAll");
            endCall(room);
        }
    }

    private void endCall(String room) {
        if (WebRTCClient.getInstance() != null) {
            WebRTCClient.getInstance().endCall(room);
            finish();
        } else {
            Log.e(TAG, "mWebRTCClient_is_null");
        }
    }

    public class WebRTCDataChannelCallback implements WebRTCDataChannel.Callback {
        @Override
        public void onMessage(String message) {
            mBinding.messageReceived.setVisibility(View.VISIBLE);
            mBinding.messageReceived.setText(message);
        }
    }

}
