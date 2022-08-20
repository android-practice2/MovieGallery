package com.bignerdranch.android.moviegallery;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import com.bignerdranch.android.moviegallery.chat.ChatActivity;
import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.databinding.ActivityPersonDetailBinding;
import com.bignerdranch.android.moviegallery.integration.AppClient;
import com.bignerdranch.android.moviegallery.integration.model.FriendsAddRequest;
import com.bignerdranch.android.moviegallery.integration.model.RequestTokenResponse;
import com.bignerdranch.android.moviegallery.integration.model.UserGetDetailV2Response;
import com.bignerdranch.android.moviegallery.integration.model.UserUpdateAvatarRequest;
import com.bignerdranch.android.moviegallery.util.OkHttpUtil;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.zelory.compressor.Compressor;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class PersonDetailActivity extends AppCompatActivity {
    private static final String TAG = "PersonDetailActivity";


    @Inject
    AppClient mAppClient;
    private UserGetDetailV2Response mUserDetail;
    private File mFile;
    private Uri mUriForFile;

    private com.bignerdranch.android.moviegallery.databinding.ActivityPersonDetailBinding mBinding;
    private int mLoginUid;
    private int mUid;

    public static Intent newIntent(Context context, Integer uid) {
        Intent intent = new Intent(context, PersonDetailActivity.class);
        intent.putExtra(Constants.EXTRA_UID, uid);
        return intent;

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityPersonDetailBinding.inflate(getLayoutInflater());
        ConstraintLayout root = mBinding.getRoot();
        setContentView(root);

        mUid = getIntent().getIntExtra(Constants.EXTRA_UID, -1);
        mLoginUid = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(Constants.PF_UID, -1);

        queryUserDetailV2();

        mBinding.replaceAvatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //camera
                //# create intent
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Uri uri = buildUriForFile();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                //# grant uri permission
                List<ResolveInfo> resolveInfos =
                        getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resolveInfos) {
                    grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                startActivityForResult(intent, Constants.RESULT_REQ_CODE_CAMERA);

            }
        });

        mBinding.avatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(PersonDetailActivity.this, android.R.style.Theme_NoTitleBar_Fullscreen);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_preview_image);
                ImageView image_view = dialog.findViewById(R.id.image_view);
                String avatar = mUserDetail.getAvatar();
                if (avatar != null) {
                    Glide.with(getApplicationContext())
                            .load(avatar)
                            .into(image_view);
                } else {
                    Glide.with(getApplicationContext())
                            .load(R.drawable.ic_person)
                            .into(image_view);
                }
                Button close_btn = dialog.findViewById(R.id.close_btn);
                close_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });


                dialog.show();
            }
        });

        mBinding.addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLoginUid < 0) {
                    Toast.makeText(PersonDetailActivity.this, "mSelfUid should not be null", Toast.LENGTH_SHORT).show();
                    return;
                }
                FriendsAddRequest request = new FriendsAddRequest();
                request.setUid(mLoginUid);
                request.setFriend_uid(mUid);

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
                Intent intent = ChatActivity.newIntent(PersonDetailActivity.this, mUid);
                startActivity(intent);
            }
        });

    }

    private void queryUserDetailV2() {
        Call<UserGetDetailV2Response> call = mAppClient.getDetailV2(mUid, mLoginUid);
        call.enqueue(new Callback<UserGetDetailV2Response>() {
            @Override
            public void onResponse(Call<UserGetDetailV2Response> call, Response<UserGetDetailV2Response> response) {
                mUserDetail = response.body();
                if (mUserDetail == null) {
                    Toast.makeText(PersonDetailActivity.this, "getDetailFromRemote exception", Toast.LENGTH_SHORT).show();
                    return;

                }
                Log.i(TAG, "user detail: " + mUserDetail);

                if (mUserDetail.getAvatar() != null) {
                    Glide.with(getApplicationContext())
                            .load(mUserDetail.getAvatar())
                            .into(mBinding.avatarImage);
                } else {
                    Glide.with(getApplicationContext())
                            .load(R.drawable.ic_person)
                            .into(mBinding.avatarImage);
                }
                mBinding.nicknameText.setText(mUserDetail.getNickname());
                mBinding.phoneNumberText.setText(mUserDetail.getPhone_number());
                mBinding.uidText.setText(mUserDetail.getUid().toString());

                boolean addFriendBtnVisible = !mUserDetail.getAreFriend();
                mBinding.addFriendBtn.setVisibility(addFriendBtnVisible ? View.VISIBLE : View.GONE);
                mBinding.chatBtn.setVisibility(!addFriendBtnVisible ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(Call<UserGetDetailV2Response> call, Throwable t) {
                Log.e(TAG, "getDetailFromRemote fail", t);
            }
        });
    }


    private Uri buildUriForFile() {
        if (mFile == null) {
            mFile = new File(getFilesDir(), "IMG_" + mUserDetail.getUid() + ".jpg");
        }
        if (mUriForFile == null) {
            mUriForFile = FileProvider.getUriForFile(PersonDetailActivity.this,
                    "com.bignerdranch.android.moviegallery.fileprovider", mFile);
        }
        return mUriForFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == Constants.RESULT_REQ_CODE_CAMERA) {
            Uri uri = buildUriForFile();
            revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            //show and scale image
//            Bitmap scaledBitmap = PictureUtils.getScaledBitmap(mFile.getPath(), mAvatar_image.getMeasuredWidth(), mAvatar_image.getMeasuredHeight());
//            mAvatar_image.setImageBitmap(scaledBitmap);
            Glide.with(getApplicationContext())
                    .load(uri)
                    .into(mBinding.avatarImage);

            //compress image
            File compressedFile = null;
            try {
                compressedFile = new Compressor(this).compressToFile(mFile);
            } catch (IOException e) {
                e.printStackTrace();
                compressedFile = mFile;
            }
            File finalFile = compressedFile;

            //upload image
            Call<RequestTokenResponse> call = mAppClient.requestToken();
            call.enqueue(new Callback<RequestTokenResponse>() {
                @Override
                public void onResponse(Call<RequestTokenResponse> call, Response<RequestTokenResponse> response) {
                    RequestTokenResponse body = response.body();
                    if (body == null) {
                        Log.e(TAG, "request_upload_token fail, body:" + body);
                        return;
                    }
                    Log.i(TAG, "RequestTokenResponse: " + body);

                    OkHttpUtil.uploadAppFileByMethod(finalFile, body.getPutUrl(), OkHttpUtil.Method.PUT, new okhttp3.Callback() {
                        @Override
                        public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                            Log.e(TAG, "upload_avatar fail", e);

                        }

                        @Override
                        public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                            Log.i(TAG, "upload_avatar success, response:" + response);
                        }
                    });

                    mUserDetail.setAvatar(body.getGetUrl());

                    UserUpdateAvatarRequest request = new UserUpdateAvatarRequest();
                    request.setAvatar(body.getGetUrl());
                    request.setUid(mUserDetail.getUid());
                    Call<ResponseBody> updateAvatar = mAppClient.updateAvatar(request);
                    updateAvatar.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Log.i(TAG, "updateAvatar success, response:" + response);

                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(TAG, "updateAvatar fail", t);

                        }
                    });
                }

                @Override
                public void onFailure(Call<RequestTokenResponse> call, Throwable t) {
                    Log.e(TAG, "request_upload_token fail", t);

                }
            });

        }


    }
}
