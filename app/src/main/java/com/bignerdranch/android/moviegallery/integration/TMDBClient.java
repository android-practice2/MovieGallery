package com.bignerdranch.android.moviegallery.integration;

import com.bignerdranch.android.moviegallery.integration.model.ListResponse;
import com.bignerdranch.android.moviegallery.integration.model.Movie;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TMDBClient {
    String BASE_URL = "https://api.themoviedb.org/3/";
    int PAGE_SIZE = 10;
    int DEFAULT_PAGE = 1;

    @GET("search/movie")
    Single<ListResponse<Movie>> getMovieSearchSingle(
            @Query("query") String query,
            @Query("page") Integer page
    );


    @GET("movie/popular")
    Single<ListResponse<Movie>> getMoviePopularSingle(@Query("page") Integer page);


}
