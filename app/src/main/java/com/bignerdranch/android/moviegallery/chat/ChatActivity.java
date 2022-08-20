package com.bignerdranch.android.moviegallery.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.databinding.ActivityChatBinding;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.SocketClient;

import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    private int mPeerUid;

    private com.bignerdranch.android.moviegallery.databinding.ActivityChatBinding mBinding;
    private String mRoom;

    public static Intent newIntent(Context context, Integer peerUid) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Constants.EXTRA_PEER_UID, peerUid);
        return intent;

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityChatBinding.inflate(getLayoutInflater());
        RelativeLayout root = mBinding.getRoot();
        setContentView(root);

        mPeerUid = getIntent().getIntExtra(Constants.EXTRA_PEER_UID, -1);

        mBinding.videoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRoom = UUID.randomUUID().toString().replace("-", "");
                Intent intent = VideoActivity.newIntent(ChatActivity.this, mPeerUid, mRoom, true);
                startActivity(intent);

            }
        });


    }
}
