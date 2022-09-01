package com.bignerdranch.android.moviegallery.friends;

import static autodispose2.AutoDispose.autoDisposable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingDataAdapter;
import androidx.paging.PagingSource;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.PagingRx;
import androidx.paging.rxjava3.RxPagingSource;

import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.http.AppClient;
import com.bignerdranch.android.moviegallery.http.model.FriendsListRequest;
import com.bignerdranch.android.moviegallery.http.model.PageResponseWrapper;
import com.bignerdranch.android.moviegallery.http.model.User;

import javax.inject.Inject;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.jvm.functions.Function0;

@HiltViewModel
public class FriendsViewModel extends ViewModel {

    private Flowable<PagingData<User>> mFlowable;

    private final AppClient mAppClient;

    private Integer uid;

    @Inject
    public FriendsViewModel(AppClient mAppClient) {
        this.mAppClient = mAppClient;

        Pager<Integer, User> pager = new Pager<Integer, User>(
                new PagingConfig(Constants.PAGE_SIZE)
                , new Function0<PagingSource<Integer, User>>() {
            @Override
            public PagingSource<Integer, User> invoke() {
                return new FriendsPagingSource();
            }
        }
        );

        mFlowable = PagingRx.getFlowable(pager);
        mFlowable =
                PagingRx.cachedIn(mFlowable, ViewModelKt.getViewModelScope(this));

    }

    public void subscribe(Lifecycle lifecycle, PagingDataAdapter<User, FriendsFragment.FriendsViewHolder> adapter
            , Integer uid) {
        this.uid = uid;
        mFlowable
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle)))
                .subscribe(new Consumer<PagingData<User>>() {
                    @Override
                    public void accept(PagingData<User> userPagingData) throws Throwable {
                        adapter.submitData(lifecycle, userPagingData);
                    }
                });


    }

    public class FriendsPagingSource extends RxPagingSource<Integer, User> {
        @Nullable
        @Override
        public Integer getRefreshKey(@NonNull PagingState<Integer, User> pagingState) {
            return null;
        }

        @NonNull
        @Override
        public Single<LoadResult<Integer, User>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
            FriendsListRequest request = new FriendsListRequest();
            request.setUid(uid);
            int newKey = loadParams.getKey() == null ? Constants.DEFAULT_PAGE : loadParams.getKey() + 1;
            request.setPageNumber(newKey);
            request.setPageSize(loadParams.getLoadSize());

            Single<PageResponseWrapper<User>> single =
                    mAppClient.listFriends(request);

            return single.subscribeOn(Schedulers.io())
                    .map((Function<PageResponseWrapper<User>, LoadResult<Integer, User>>) wrapper -> {
                        return new LoadResult.Page<Integer, User>(wrapper.getList()
                                , newKey - 1 < 1 ? null : newKey - 1,
                                wrapper.getList().isEmpty() ? null : newKey + 1
                        );
                    })
                    .onErrorReturn(
                            LoadResult.Error::new
                    );


        }
    }

}
