package com.bignerdranch.android.moviegallery.http;


import com.bignerdranch.android.moviegallery.http.model.ChatPostMsg;
import com.bignerdranch.android.moviegallery.http.model.FriendsAddRequest;
import com.bignerdranch.android.moviegallery.http.model.FriendsListRequest;
import com.bignerdranch.android.moviegallery.http.model.PageResponseWrapper;
import com.bignerdranch.android.moviegallery.http.model.RequestTokenResponse;
import com.bignerdranch.android.moviegallery.http.model.User;
import com.bignerdranch.android.moviegallery.http.model.UserGeoLocationAddLocationRequest;
import com.bignerdranch.android.moviegallery.http.model.UserGeoLocationSearchNearbyRequest;
import com.bignerdranch.android.moviegallery.http.model.UserGetDetailResponse;
import com.bignerdranch.android.moviegallery.http.model.UserLocationProjection;
import com.bignerdranch.android.moviegallery.http.model.UserRegisterRequest;
import com.bignerdranch.android.moviegallery.http.model.UserUpdateAvatarRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface AppClient {
    ObjectMapper objectMapper = new ObjectMapper();

    @PUT("User/register")
    Call<User> register(@Body UserRegisterRequest request);

    @GET("User/getDetail")
    Call<UserGetDetailResponse> getDetail(@Query("uid") Integer uid);


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

    @POST("chat/postMsg")
    Call<Void> postMsg(@Body ChatPostMsg request);


}
