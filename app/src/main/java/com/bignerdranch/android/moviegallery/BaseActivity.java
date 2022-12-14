package com.bignerdranch.android.moviegallery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.SocketClient;

public class BaseActivity extends AppCompatActivity {
    public static final String TAG = "BaseActivity";
    public static final int REQUEST_CODE_LOGIN = 1;
    protected int mUid = -1;
    protected UserModel mUserModel;

    public BaseActivity() {
    }

    public BaseActivity(int contentLayoutId) {
        super(contentLayoutId);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserModel = new ViewModelProvider(this).get(UserModel.class);

        setupUid();
        if (mUid < 0) {
            Intent intent = LoginActivity.newIntent(this);
            startActivityForResult(intent, REQUEST_CODE_LOGIN);
        } else {
            afterLogin();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult");
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_LOGIN) {
            setupUid();
            afterLogin();
        }
    }

    private void setupUid() {
        mUid = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt(Constants.PF_UID, -1);
        mUserModel.setUid(mUid);
    }

    private void afterLogin() {
        if (mUid > 0) {
            SocketClient.ensureSocket(getApplicationContext(), mUid);
        } else {
            Log.e(TAG, "uid_error,uid:" + mUid);
        }

    }

}
