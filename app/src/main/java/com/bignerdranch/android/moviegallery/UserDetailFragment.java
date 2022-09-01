package com.bignerdranch.android.moviegallery;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bignerdranch.android.moviegallery.chat.repository.PeerRepository;
import com.bignerdranch.android.moviegallery.chat.room.entity.Peer;
import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.databinding.FragmentUserDetailBinding;
import com.bignerdranch.android.moviegallery.integration.AppClient;
import com.bignerdranch.android.moviegallery.integration.model.RequestTokenResponse;
import com.bignerdranch.android.moviegallery.integration.model.UserGetDetailResponse;
import com.bignerdranch.android.moviegallery.integration.model.UserUpdateAvatarRequest;
import com.bignerdranch.android.moviegallery.util.OkHttpUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.zelory.compressor.Compressor;
import io.reactivex.rxjava3.functions.Consumer;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class UserDetailFragment extends BaseFragment {
    private static final String TAG = "PersonDetailActivity";

    @Inject
    AppClient mAppClient;
    private UserGetDetailResponse mUserDetail;
    private File mFile;
    private Uri mUriForFile;

    private FragmentUserDetailBinding mBinding;
    private CountDownLatch mCountDownLatch;
    private final Handler mHandler = new Handler();
    private int mSubjUid;
    @Inject
    private PeerRepository mPeerRepository;

    public UserDetailFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_user_detail, container, false);
        mBinding = FragmentUserDetailBinding.bind(inflate);
        mCountDownLatch = new CountDownLatch(1);
        mSubjUid = getArguments().getInt(Constants.EXTRA_UID, -1);

        queryUserDetail();

        setupView();

        return inflate;


    }

    private void setupView() {
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
                        requireActivity().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resolveInfos) {
                    requireActivity().grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                startActivityForResult(intent, Constants.RESULT_REQ_CODE_CAMERA);

            }
        });

        mBinding.avatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(UserDetailFragment.this.requireActivity(), android.R.style.Theme_NoTitleBar_Fullscreen);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_preview_image);
                ImageView image_view = dialog.findViewById(R.id.image_view);
                String avatar = mUserDetail.getAvatar();
                if (avatar != null) {
                    Glide.with(requireContext())
                            .load(avatar)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(image_view);
                } else {
                    Glide.with(requireContext())
                            .load(R.drawable.ic_person)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
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
    }

    private void queryUserDetail() {
        Call<UserGetDetailResponse> call = mAppClient.getDetail(mSubjUid);
        call.enqueue(new Callback<UserGetDetailResponse>() {
            @Override
            public void onResponse(Call<UserGetDetailResponse> call, Response<UserGetDetailResponse> response) {
                mUserDetail = response.body();
                mCountDownLatch.countDown();
                if (mUserDetail == null) {
                    Toast.makeText(UserDetailFragment.this.requireActivity(), "getDetailFromRemote exception", Toast.LENGTH_SHORT).show();
                    return;

                }
                Log.i(TAG, "user detail: " + mUserDetail);

                if (mUserDetail.getAvatar() != null) {
                    Glide.with(requireContext())
                            .load(mUserDetail.getAvatar())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(mBinding.avatarImage);
                } else {
                    Glide.with(requireContext())
                            .load(R.drawable.ic_person)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(mBinding.avatarImage);
                }
                mBinding.nicknameText.setText(mUserDetail.getNickname());
                mBinding.phoneNumberText.setText(mUserDetail.getPhone_number());
                mBinding.uidText.setText(mUserDetail.getUid().toString());

                Peer peer = new Peer(
                        mUserDetail.getUid(),
                        mUserDetail.getNickname(),
                        mUserDetail.getAvatar()
                );
                mPeerRepository.updateIfPresent(peer)
                        .doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Throwable {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e(TAG, "update_local_room_peer error", throwable);
                                        Toast.makeText(getContext(), "update_local_room_peer error", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .subscribe();

            }

            @Override
            public void onFailure(Call<UserGetDetailResponse> call, Throwable t) {
                mCountDownLatch.countDown();
                Log.e(TAG, "getDetailFromRemote fail", t);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "getDetailFromRemote fail", Toast.LENGTH_SHORT).show();

                    }
                });

//                requireActivity().runOnUiThread(new Runnable() { //requireActivity() may throw IllegalStateException if inner activity is null
//                    @Override
//                    public void run() {
//                        Toast.makeText(requireActivity(), "getDetailFromRemote fail", Toast.LENGTH_SHORT).show();
//                    }
//                });
            }
        });
    }


    private Uri buildUriForFile() {
        if (mFile == null) {
            mFile = new File(requireActivity().getFilesDir(), "IMG_" + mUid + ".jpg");
        }
        if (mUriForFile == null) {
            mUriForFile = FileProvider.getUriForFile(UserDetailFragment.this.requireActivity(),
                    "com.bignerdranch.android.moviegallery.fileprovider", mFile);
        }
        return mUriForFile;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == Constants.RESULT_REQ_CODE_CAMERA) {
            Uri uri = buildUriForFile();
            requireActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            //show and scale image
//            Bitmap scaledBitmap = PictureUtils.getScaledBitmap(mFile.getPath(), mAvatar_image.getMeasuredWidth(), mAvatar_image.getMeasuredHeight());
//            mAvatar_image.setImageBitmap(scaledBitmap);
            Glide.with(requireContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mBinding.avatarImage);

            //compress image
            File compressedFile = null;
            try {
                compressedFile = new Compressor(requireActivity()).compressToFile(mFile);
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

                    try {
                        mCountDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    OkHttpUtil.uploadAppFileByMethod(finalFile, body.getPutUrl(), OkHttpUtil.Method.PUT, new okhttp3.Callback() {
                        @Override
                        public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                            Log.e(TAG, "upload_avatar fail", e);

                        }

                        @Override
                        public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                            if (response.isSuccessful()) {
                                Log.i(TAG, "upload_avatar success, response:" + response);

                            } else {
                                Log.e(TAG, "upload_avatar fail " + response.code());
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "upload_avatar fail " + response.code(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
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
