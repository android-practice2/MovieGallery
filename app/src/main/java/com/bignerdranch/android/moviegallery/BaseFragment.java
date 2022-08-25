package com.bignerdranch.android.moviegallery;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class BaseFragment extends Fragment {
    protected int mUid = -1;
    protected UserModel mUserModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserModel = new ViewModelProvider(requireActivity()).get(UserModel.class);
        mUid = mUserModel.getUid();

    }
}
