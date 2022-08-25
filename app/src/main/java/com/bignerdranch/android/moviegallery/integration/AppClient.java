package com.bignerdranch.android.moviegallery.integration;


import com.bignerdranch.android.moviegallery.integration.model.FriendsAddRequest;
import com.bignerdranch.android.moviegallery.integration.model.FriendsListRequest;
import com.bignerdranch.android.moviegallery.integration.model.PageResponseWrapper;
import com.bignerdranch.android.moviegallery.integration.model.RequestTokenResponse;
import com.bignerdranch.android.moviegallery.integration.model.User;
import com.bignerdranch.android.moviegallery.integration.model.UserGeoLocationAddLocationRequest;
import com.bignerdranch.android.moviegallery.integration.model.UserGeoLocationSearchNearbyRequest;
import com.bignerdranch.android.moviegallery.integration.model.UserGetDetailResponse;
import com.bignerdranch.android.moviegallery.integration.model.UserGetDetailV2Response;
import com.bignerdranch.android.moviegallery.integration.model.UserLocationProjection;
import com.bignerdranch.android.moviegallery.integration.model.UserRegisterRequest;
import com.bignerdranch.android.moviegallery.integration.model.UserUpdateAvatarRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface AppClient {
    ObjectMapper objectMapper = new ObjectMapper();

    @PUT("User/register")
    Call<User> register(@Body UserRegisterRequest request);

    @GET("User/getDetail")
    Call<UserGetDetailResponse> getDetail(@Query("uid") Integer uid);

    @GET("User/getDetailV2")
    Call<UserGetDetailV2Response> getDetailV2(@Query("selfUid") Integer selfUid, @Query("uid") Integer uid);

    @PUT("UserGeoLocation/addLocation")
    Call<Boolean> addLocation(@Body UserGeoLocationAddLocationRequest request);

    default Single<PageResponseWrapper<UserLocationProjection>> searchNearby(UserGeoLocationSearchNearbyRequest request) {
        Map<String, String> map = objectMapper.convertValue(request, new TypeReference<Map<String, String>>() {
        });
        return doSearchNearby(map);
    }

    @GET("UserGeoLocation/searchNearby")
    Single<PageResponseWrapper<UserLocationProjection>> doSearchNearby(@QueryMap Map<String, String> map);


    @GET("OssAcl/requestToken")
    Call<RequestTokenResponse> requestToken();

    @PATCH("User/updateAvatar")
    Call<ResponseBody> updateAvatar(@Body UserUpdateAvatarRequest request);


    @PUT("Friends/add")
    Call<Void> addFriend(@Body FriendsAddRequest request);

    default Single<PageResponseWrapper<User>> listFriends(FriendsListRequest request) {
        Map<String, String> map = objectMapper.convertValue(request, new TypeReference<Map<String, String>>() {
        });
        return doListFriends(map);
    }

    @GET("Friends/list")
    Single<PageResponseWrapper<User>> doListFriends(@QueryMap Map<String, String> request);

    @GET("Friends/isFriend")
    Call<Boolean> isFriend(@Query("selfUid") Integer selfUid, @Query("uid") Integer uid);
}
