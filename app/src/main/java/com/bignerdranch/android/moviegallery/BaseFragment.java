package com.bignerdranch.android.moviegallery;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bignerdranch.android.moviegallery.webrtc.signaling_client.SocketClient;

public class BaseFragment extends Fragment {
    public static final String TAG = "BaseFragment";
    protected int mUid = -1;
    protected UserModel mUserModel;

    public BaseFragment() {
    }

    public BaseFragment(@LayoutRes int contentLayoutId) {
        super(contentLayoutId);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserModel = new ViewModelProvider(requireActivity()).get(UserModel.class);
        Log.i(TAG, "mUserModel:" + mUserModel);

        mUid = mUserModel.getUid();
        if (mUid > 0) {
            SocketClient.ensureSocket(requireActivity().getApplicationContext(), mUid);

        } else {
            Log.e(TAG, "uid_error,uid:" + mUid + ",mUserModel:" + mUserModel);

        }

    }
}
