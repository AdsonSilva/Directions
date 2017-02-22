package com.example.treinamento_huawei.directions;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.treinamento_huawei.directions.api.ApiManager;
import com.example.treinamento_huawei.directions.api.GsonPostRequest;

import java.util.Map;

/**
 * Created by huawei on 22/02/17.
 */

public class PostSpeed {
    private final OnPostSpeed onPostSpeed;

    private static final String TAG = "SendLocalization: ";

    public PostSpeed(OnPostSpeed onPostSpeed){
        this.onPostSpeed = onPostSpeed;
    }

    public GsonPostRequest serverRequestString(Map<String, String> params) {
        return new GsonPostRequest<>(
                ApiManager.getInstance().sendSpeed(),
                Object.class, params,
                new Response.Listener<Object>() {
                    @Override
                    public void onResponse(Object response) {
                        if (response != null) {
                            sendCallback(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "errorhttp" + String.valueOf(error.getMessage()));
                    }
                }
        );
    }

    private void sendCallback(Object response) {
        onPostSpeed.OnPostSpeed(response.toString());
    }
}