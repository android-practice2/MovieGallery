package com.bignerdranch.android.moviegallery;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bignerdranch.android.moviegallery.webrtc.signaling_client.SocketClient;

public class BaseFragment extends Fragment {
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
        mUid = mUserModel.getUid();
        SocketClient.ensureSocket(requireActivity().getApplicationContext(), mUid);

    }
}
