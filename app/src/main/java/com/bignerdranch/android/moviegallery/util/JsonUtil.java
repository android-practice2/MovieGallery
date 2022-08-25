package com.bignerdranch.android.moviegallery.util;

import com.bignerdranch.android.moviegallery.webrtc.model.SessionDescriptionDTO;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;

import java.util.Map;

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

    public static <T> T fromObj(Object obj, Class<T> clzz) {
        return gson.fromJson(gson.toJsonTree(obj), clzz);
    }


}
