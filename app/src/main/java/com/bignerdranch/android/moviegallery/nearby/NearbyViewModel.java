package com.bignerdranch.android.moviegallery.nearby;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingDataAdapter;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.PagingRx;
import androidx.paging.rxjava3.RxPagingSource;

import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.integration.AppClient;
import com.bignerdranch.android.moviegallery.integration.model.PageResponseWrapper;
import com.bignerdranch.android.moviegallery.integration.model.User;
import com.bignerdranch.android.moviegallery.integration.model.UserGeoLocationSearchNearbyRequest;
import com.bignerdranch.android.moviegallery.integration.model.UserLocationProjection;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import autodispose2.AutoDispose;
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
@kotlinx.coroutines.ExperimentalCoroutinesApi
public class NearbyViewModel extends ViewModel {

    private Flowable<PagingData<UserLocationProjection>> mFlowable;
    private NearbyPagingSource mNearbyPagingSource;
    private AppClient mAppClient;

    private Location location;

    @Inject
    public NearbyViewModel(AppClient appClient) {
        mAppClient = appClient;
        Pager<Integer, UserLocationProjection> pager =
                new Pager<>(
                        new PagingConfig(Constants.PAGE_SIZE, Constants.PAGE_SIZE, false, Constants.PAGE_SIZE * 3, Constants.CACHE_MAX_SIZE)
                        , () -> {
                    mNearbyPagingSource = new NearbyPagingSource();
                    return mNearbyPagingSource;
                }
                );
        mFlowable = PagingRx.getFlowable(pager);
        mFlowable = PagingRx.cachedIn(mFlowable, ViewModelKt.getViewModelScope(this));

    }

    public void subscribe(PagingDataAdapter<UserLocationProjection, NearbyFragment.NearbyViewHolder> mAdapter,
                          LifecycleOwner lifecycleOwner) {
        mFlowable
                .to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
                .subscribe(pagingData -> {
                    mAdapter.submitData(lifecycleOwner.getLifecycle(), pagingData);
                });
    }


    public void upsertLocation(Location location) {
        this.location = location;

    }

    public Flowable<PagingData<UserLocationProjection>> getFlowable() {
        return mFlowable;
    }

    public NearbyPagingSource getNearbyPagingSource() {
        return mNearbyPagingSource;
    }

    public class NearbyPagingSource extends RxPagingSource<Integer, UserLocationProjection> {
        public static final String TAG = "NearbyPagingSource";

        @Nullable
        @Override
        public Integer getRefreshKey(@NonNull PagingState<Integer, UserLocationProjection> pagingState) {
            return null;
        }

        @NonNull
        @Override
        public Single<LoadResult<Integer, UserLocationProjection>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
            Log.w(TAG, "loadSingle,location:" + location);
            if (location == null) {
                return Single.just(new LoadResult.Page<>(Collections.emptyList(), null, null));
            }

            UserGeoLocationSearchNearbyRequest request = new UserGeoLocationSearchNearbyRequest();
            request.setLatitude((float) location.getLatitude());
            request.setLongitude((float) location.getLongitude());

            int newKey = loadParams.getKey() == null ? Constants.DEFAULT_PAGE : loadParams.getKey() + 1;

            request.setPageNumber(newKey);
            request.setPageSize(loadParams.getLoadSize());

            Single<PageResponseWrapper<UserLocationProjection>> single = mAppClient.searchNearby(request);

            return single.subscribeOn(Schedulers.io())
                    .map((Function<PageResponseWrapper<UserLocationProjection>, LoadResult<Integer, UserLocationProjection>>) wrapper -> {
                        return new LoadResult.Page<Integer, UserLocationProjection>(wrapper.getList()
                                , newKey - 1 < 1 ? null : newKey - 1,
                                wrapper.getList().isEmpty() ? null : newKey + 1
                        );
                    })
                    .onErrorReturn(e -> {
                        Log.e(TAG, "searchNearby error", e);
                        return new LoadResult.Error<Integer, UserLocationProjection>(e);
                    });


        }
    }
}
