package com.bignerdranch.android.moviegallery.nearby;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bignerdranch.android.moviegallery.LoginActivity;
import com.bignerdranch.android.moviegallery.MyLoadStateAdapter;
import com.bignerdranch.android.moviegallery.PersonDetailActivity;
import com.bignerdranch.android.moviegallery.R;
import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.databinding.FragmentNearbyBinding;
import com.bignerdranch.android.moviegallery.databinding.ViewHolderNearbyBinding;
import com.bignerdranch.android.moviegallery.integration.AppClient;
import com.bignerdranch.android.moviegallery.integration.model.User;
import com.bignerdranch.android.moviegallery.integration.model.UserGeoLocationAddLocationRequest;
import com.bignerdranch.android.moviegallery.integration.model.UserLocationProjection;
import com.bumptech.glide.Glide;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
@AndroidEntryPoint
@kotlinx.coroutines.ExperimentalCoroutinesApi
public class NearbyFragment extends Fragment {
    private static final String TAG = "NearbyFragment";
    public static final int MIN_TIME_MS = 5 * 60 * 1000;
    public static final int MIN_DISTANCE_M = 50;
    public static final int REQUEST_CODE_LOGIN = 1;
    private com.bignerdranch.android.moviegallery.databinding.FragmentNearbyBinding mBinding;
    private NearbyViewModel mViewModel;
    private NearbyAdapter mAdapter;
    public static final int REQ_CODE_PERMISSION = 1;
    @Inject
    AppClient mAppClient;
    private Integer mUid = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        mUid = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getInt(Constants.PF_UID, -1);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_nearby, container, false);
        mViewModel = new ViewModelProvider(this).get(NearbyViewModel.class);
        mBinding = FragmentNearbyBinding.bind(inflate);
        //login
        if (mUid < 0) {
            Intent intent = LoginActivity.newIntent(getActivity());
            startActivityForResult(intent, REQUEST_CODE_LOGIN);
        }else{
            acquireLocation();

            bindPaging();
        }

        return inflate;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.i(TAG, "onActivityResult");
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_LOGIN) {
            User user = (User) data.getSerializableExtra(Constants.EXTRA_USER);
            mUid = user.getUid();
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .edit()
                    .putInt(Constants.PF_UID, mUid)
                    .apply();
            acquireLocation();
            bindPaging();

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "resumed");


    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "paused");
    }

    private void bindPaging() {
        Log.i(TAG, "bindPaging");
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new NearbyAdapter(new DiffUtil.ItemCallback<UserLocationProjection>() {
            @Override
            public boolean areItemsTheSame(@NonNull UserLocationProjection oldItem, @NonNull UserLocationProjection newItem) {
                return oldItem.getUid().equals(newItem.getUid());
            }

            @Override
            public boolean areContentsTheSame(@NonNull UserLocationProjection oldItem, @NonNull UserLocationProjection newItem) {
                return oldItem.equals(newItem);
            }
        });

        mBinding.recyclerView.setAdapter(
                mAdapter.withLoadStateFooter(new MyLoadStateAdapter(v -> mAdapter.retry()))
        );
        mViewModel.subscribe(mAdapter, this);


    }

    private void acquireLocation() {
        Log.i(TAG, "acquireLocation");
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_CODE_PERMISSION);
            return;
        } else {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = locationManager.getProviders(true);

            String targetProvider;
            if (providers.contains(LocationManager.GPS_PROVIDER)) {
                targetProvider = LocationManager.GPS_PROVIDER;
            } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                targetProvider = LocationManager.NETWORK_PROVIDER;
            } else {
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment)
                        .navigateUp();
                return;
            }


            Location location = locationManager.getLastKnownLocation(targetProvider);
            if (location != null) {
                mViewModel.upsertLocation(location);
                UserGeoLocationAddLocationRequest request = new UserGeoLocationAddLocationRequest(mUid, location);
                Call<Boolean> call = mAppClient.addLocation(request);
                call.enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        Log.i(TAG, "addLocation successful");

                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        Log.e(TAG, "addLocation exception", t);

                    }
                });
            }

            locationManager.requestLocationUpdates(targetProvider
                    , MIN_TIME_MS, MIN_DISTANCE_M, new LocationListener() {
                        @Override
                        public void onLocationChanged(@NonNull Location location) {
                            UserGeoLocationAddLocationRequest request = new UserGeoLocationAddLocationRequest(mUid, location);
                            Call<Boolean> call = mAppClient.addLocation(request);
                            call.enqueue(new Callback<Boolean>() {
                                @Override
                                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                    Log.i(TAG, "addLocation successful");

                                }

                                @Override
                                public void onFailure(Call<Boolean> call, Throwable t) {
                                    Log.e(TAG, "addLocation exception", t);

                                }
                            });
                        }
                    });

            if (location == null) {
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment)
                        .navigateUp();
            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                acquireLocation();
            } else {
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigateUp();
            }

        }

    }

    public class NearbyAdapter extends PagingDataAdapter<UserLocationProjection, NearbyViewHolder> {
        public NearbyAdapter(@NonNull DiffUtil.ItemCallback<UserLocationProjection> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public NearbyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new NearbyViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull NearbyViewHolder holder, int position) {
            holder.bind(getItem(position));
        }
    }

    public class NearbyViewHolder extends RecyclerView.ViewHolder {


        private final ViewHolderNearbyBinding mNearbyBinding;

        public NearbyViewHolder(@NonNull ViewGroup parent) {
            super(LayoutInflater.from(getActivity()).inflate(R.layout.view_holder_nearby, parent, false));
            mNearbyBinding = ViewHolderNearbyBinding.bind(super.itemView);

            super.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView uid_text=  v.findViewById(R.id.uid);
                    int uid = Integer.parseInt(uid_text.getText().toString());
                    Intent intent = PersonDetailActivity.newIntent(getActivity(), uid);
                    startActivity(intent);


                }
            });
        }

        public void bind(UserLocationProjection item) {
            mNearbyBinding.uid.setText(item.getUid() + "");
            mNearbyBinding.nickname.setText(item.getNickname() + "");
            Float distance = item.getDistance();
            String result;
            if (distance >= 1000) {
                result = String.format("%1$.1f km", distance / 1000);
            }else{
                result = String.format("%1$.0f m",distance);
            }

            mNearbyBinding.distance.setText(result);

            String avatar = item.getAvatar();
            if (avatar == null) {
                Glide.with(getActivity().getApplicationContext())
                        .load(R.drawable.ic_person)
                        .into(mNearbyBinding.avatarImage);
            }else{
                Glide.with(getActivity().getApplicationContext())
                        .load(avatar)
                        .into(mNearbyBinding.avatarImage);

            }

        }
    }
}