package com.bignerdranch.android.moviegallery.config;

import com.bignerdranch.android.moviegallery.BuildConfig;
import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.integration.AppClient;
import com.bignerdranch.android.moviegallery.integration.TMDBClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitConfig {
    //    String BASE_URL = "http://10.0.2.2/api/";
    public static final String BASE_URL = "https://socialme.hopto.org/api/";

    @Module
    @InstallIn(SingletonComponent.class)
    public static class SingletonComponentModule {


        @Provides
        @Singleton
        public static TMDBClient sTMDBClient() {

            // Create OkHttp Client
            OkHttpClient.Builder client = new OkHttpClient.Builder();
            // Add interceptor to add API key as query string parameter to each request
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT);
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

            client.addInterceptor(chain -> {
                        Request original = chain.request();
                        HttpUrl originalHttpUrl = original.url();
                        HttpUrl url = originalHttpUrl.newBuilder()
                                // Add API Key as query string parameter
                                .addQueryParameter("api_key", BuildConfig.api_key)
                                .build();
                        Request.Builder requestBuilder = original.newBuilder()
                                .url(url);
                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    })
                    .addInterceptor(loggingInterceptor);

            // Create retrofit instance
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(TMDBClient.BASE_URL)
                    .client(client.build())
                    // Add Gson converter
                    .addConverterFactory(GsonConverterFactory.create())
//                .addConverterFactory(ScalarsConverterFactory.create())
                    // Add RxJava spport for Retrofit
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build();
            // Init APIInterface
            return retrofit.create(TMDBClient.class);
        }

        @Provides
        @Singleton
        public static AppClient sMyClient() {
            // Create OkHttp Client
            OkHttpClient.Builder client = new OkHttpClient.Builder();
            // Add interceptor to add API key as query string parameter to each request
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT);
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

            client
                    .addInterceptor(loggingInterceptor);

            // Create retrofit instance
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client.build())
                    // Add Gson converter
                    .addConverterFactory(GsonConverterFactory.create())
//                .addConverterFactory(ScalarsConverterFactory.create())
                    // Add RxJava spport for Retrofit
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build();
            // Init APIInterface
            return retrofit.create(AppClient.class);
        }


    }

}
