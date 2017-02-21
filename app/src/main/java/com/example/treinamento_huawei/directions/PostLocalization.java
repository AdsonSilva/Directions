package com.example.treinamento_huawei.directions;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.treinamento_huawei.directions.api.ApiManager;
import com.example.treinamento_huawei.directions.api.GsonPostRequest;

import java.util.Map;


/**
 * Created by huawei on 16/01/17.
 */

public class PostLocalization {

    private final OnPostLocalization onPostLocalization;

    private static final String TAG = "SendLocalization: ";

    public PostLocalization(OnPostLocalization onPostLocalization){
        this.onPostLocalization = onPostLocalization;

    }

    public GsonPostRequest serverRequestString(String lat, String log) {
        Map<String, String> params = null;
        return new GsonPostRequest<>(
                ApiManager.getInstance().sendLocalization(lat, log),
                Object.class, params,
                new Response.Listener<Object>() {
                    @Override
                    public void onResponse(Object response) {
                        onPostLocalization.onPostlocalizationSucess();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "errorhttp" + String.valueOf(error.getMessage()));
                        onPostLocalization.onPostlocalizationError();
                    }
                }
        );
    }

}