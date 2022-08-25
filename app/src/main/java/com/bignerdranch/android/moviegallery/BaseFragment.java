package com.bignerdranch.android.moviegallery;

import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bignerdranch.android.moviegallery.constants.Constants;

public class BaseFragment extends Fragment {
    protected int mUid = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUid = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getInt(Constants.PF_UID, -1);

    }
}
