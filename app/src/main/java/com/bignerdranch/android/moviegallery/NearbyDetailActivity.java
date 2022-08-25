package com.bignerdranch.android.moviegallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bignerdranch.android.moviegallery.chat.ChatActivity;
import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.databinding.ActivityNearbyDetailBinding;
import com.bignerdranch.android.moviegallery.integration.AppClient;
import com.bignerdranch.android.moviegallery.integration.model.FriendsAddRequest;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class NearbyDetailActivity extends BaseActivity {
    private static final String TAG = "PersonDetailActivity";


    @Inject
    AppClient mAppClient;


    private ActivityNearbyDetailBinding mBinding;
    private int mOtherUid;

    public static Intent newIntent(Context context, Integer nearbyUid) {
        Intent intent = new Intent(context, NearbyDetailActivity.class);
        intent.putExtra(Constants.EXTRA_OTHER_UID, nearbyUid);
        return intent;

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityNearbyDetailBinding.inflate(getLayoutInflater());
        ConstraintLayout root = mBinding.getRoot();
        setContentView(root);


        if (savedInstanceState == null) {
            mOtherUid = getIntent().getIntExtra(Constants.EXTRA_OTHER_UID, -1);
            Bundle args = new Bundle();
            args.putInt(Constants.EXTRA_UID, mOtherUid);

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, UserDetailFragment.class, args)
                    .commit();
        }


        setupView();

    }

    private void setupView() {
        Call<Boolean> call = mAppClient.isFriend(mUid, mOtherUid);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                Boolean isFriend = response.body();
                mBinding.chatBtn.setVisibility(isFriend ? View.VISIBLE : View.GONE);
                mBinding.addFriendBtn.setVisibility(!isFriend ? View.VISIBLE : View.GONE);

            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e(TAG, "isFriend error", t);

            }
        });

        mBinding.addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendsAddRequest request = new FriendsAddRequest();
                request.setUid(mUid);
                request.setFriend_uid(mOtherUid);

                Call<Void> call = mAppClient.addFriend(request);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Log.i(TAG, "add_friend success");

                        mBinding.chatBtn.setVisibility(View.VISIBLE);
                        mBinding.addFriendBtn.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "add_friend fail", t);
                    }
                });

            }
        });

        mBinding.chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = ChatActivity.newIntent(NearbyDetailActivity.this, mOtherUid);
                startActivity(intent);
            }
        });
    }


}
