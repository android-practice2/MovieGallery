package com.bignerdranch.android.moviegallery.util;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;

public class JsonUtil {
    private static final Gson gson = new Gson();

    public static <T> T fromJsonObject(JSONObject jsonObject, Class<T> clzz) {
        return gson.fromJson(jsonObject.toString(), clzz);
    }

    public static JSONObject toJsonObject(Object obj) {
        try {
            JSONObject jsonObject = new JSONObject(gson.toJson(obj));
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


}
