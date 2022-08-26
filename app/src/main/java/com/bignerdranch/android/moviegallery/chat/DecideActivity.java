package com.bignerdranch.android.moviegallery.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.bignerdranch.android.moviegallery.BaseActivity;
import com.bignerdranch.android.moviegallery.R;
import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.databinding.ActivityDecideBinding;
import com.bignerdranch.android.moviegallery.util.ThreadPool;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.SocketClient;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.ByeRequest;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.model.JoinRequest;

import java.util.concurrent.CountDownLatch;

public class DecideActivity extends BaseActivity {
    public static CountDownLatch mCountDownLatch;

    private int mPeerUid;
    private String mRoom;

    private com.bignerdranch.android.moviegallery.databinding.ActivityDecideBinding mBinding;


    public static Intent newIntent(Context context, Integer peerUid, String room) {
        Intent intent = new Intent(context, DecideActivity.class);
        intent.putExtra(Constants.EXTRA_PEER_UID, peerUid);
        intent.putExtra(Constants.EXTRA_ROOM, room);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decide);
        mBinding = ActivityDecideBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mPeerUid = getIntent().getIntExtra(Constants.EXTRA_PEER_UID, -1);
        mRoom = getIntent().getStringExtra(Constants.EXTRA_ROOM);


        mBinding.agreeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ThreadPool.getExecutorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        mCountDownLatch = new CountDownLatch(1);

                        startVideoActivity();

                        try {
                            mCountDownLatch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        join();
                    }
                });

            }
        });

        mBinding.rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bye();
                finish();
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCountDownLatch = null;
    }

    private void bye() {
        ByeRequest byeRequest = new ByeRequest();
        byeRequest.setRoom(mRoom);
        byeRequest.setUid(mUid);
        SocketClient.getInstance().bye(byeRequest);
    }

    private void join() {
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setRoom(mRoom);
        joinRequest.setUid(mUid);
        SocketClient.getInstance().join(joinRequest);
    }

    private void startVideoActivity() {
        Intent intent = VideoActivity.newIntent(this, mPeerUid, mRoom, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
