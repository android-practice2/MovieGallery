package com.bignerdranch.android.moviegallery.movie;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingSource;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.PagingRx;
import androidx.paging.rxjava3.RxPagingSource;

import com.bignerdranch.android.moviegallery.integration.TMDBClient;
import com.bignerdranch.android.moviegallery.integration.model.ListResponse;
import com.bignerdranch.android.moviegallery.integration.model.Movie;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlinx.coroutines.CoroutineScope;

@HiltViewModel
@kotlinx.coroutines.ExperimentalCoroutinesApi
public class MovieViewModel extends ViewModel {

    private final TMDBClient mTMDBClient;
    private Flowable<PagingData<Movie>> mFlowable;
    private MoviePagingSource mMoviePagingSource;

    private String query;


    @Inject
    public MovieViewModel(TMDBClient tmdbClient) {
        mTMDBClient = tmdbClient;
        Pager<Integer, Movie> pager =
                new Pager<Integer, Movie>(
                        new PagingConfig(
                                TMDBClient.PAGE_SIZE,
                                TMDBClient.PAGE_SIZE,
                                false,
                                TMDBClient.PAGE_SIZE * 2
                        ),
                        () -> {
                            mMoviePagingSource = new MoviePagingSource();
                            return mMoviePagingSource;

                        }
                );


        mFlowable = PagingRx.getFlowable(pager);
        CoroutineScope coroutineScope = ViewModelKt.getViewModelScope(this);

        mFlowable = PagingRx.cachedIn(mFlowable, coroutineScope);
    }

    public Flowable<PagingData<Movie>> getFlowable() {
        return mFlowable;
    }

    public MoviePagingSource getMoviePagingSource() {
        return mMoviePagingSource;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public class MoviePagingSource extends RxPagingSource<Integer, Movie> {
        private static final String TAG = "MoviePagingSource";

        @NonNull
        @Override
        public Single<LoadResult<Integer, Movie>> loadSingle(@NonNull LoadParams<Integer> loadParams) {

            return doLoadSingleV2(loadParams);


        }


        private Single<LoadResult<Integer, Movie>> doLoadSingleV2(@NonNull LoadParams<Integer> loadParams) {
            Single<ListResponse<Movie>> moviesSingle;
            int pageKey = TMDBClient.DEFAULT_PAGE;
            Integer key = loadParams.getKey();
            if (key != null) {
                pageKey = key + 1;
            }

            if (query == null) {
                moviesSingle = mTMDBClient.getMoviePopularSingle(pageKey);
            } else {
                moviesSingle = mTMDBClient.getMovieSearchSingle(query, pageKey);

            }


            return moviesSingle
                    .subscribeOn(Schedulers.io())

                    .map((Function<ListResponse<Movie>, LoadResult<Integer, Movie>>) movieListResponse -> {
                        List<Movie> movies = movieListResponse.getResults();
                        Integer page = movieListResponse.getPage();

                        return new LoadResult.Page<>(movies, page == TMDBClient.DEFAULT_PAGE ? null : page - 1, page + 1);
                    })
//
                    .onErrorReturn(e -> {
                        Log.e(TAG, "fetch movie error", e);
                        return new LoadResult.Error<Integer, Movie>(e);
                    })
                    ;
        }

        @Nullable
        @Override
        public Integer getRefreshKey(@NonNull PagingState<Integer, Movie> pagingState) {
            return null;
        }


    }
}
