package com.bignerdranch.android.moviegallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.databinding.FragmentMyDetailBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MyDetailFragment extends BaseFragment {
    public static final String TAG = "MyDetailFragment";

    private FragmentMyDetailBinding mBinding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_my_detail, container, false);
        mBinding = FragmentMyDetailBinding.bind(inflate);
        Bundle args = new Bundle();
        args.putInt(Constants.EXTRA_UID, mUid);
        getFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_container_view, UserDetailFragment.class, args)
                .commit();

        return inflate;
    }
}
