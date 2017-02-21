package com.example.treinamento_huawei.directions;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.treinamento_huawei.directions.api.ApiManager;
import com.example.treinamento_huawei.directions.api.GsonRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by huawei on 21/02/17.
 */

public class GetQueueValues {
    private final OnGetQueueValues callback;
    private static final String TAG = "GetQueueValues: ";

    public GetQueueValues(OnGetQueueValues callback){
        this.callback = callback;
    }

    public GsonRequest<JsonObject> getValues() {
        String url = ApiManager.getInstance().getQueueValues();
        return serverRequest(url);
    }

    private GsonRequest<JsonObject> serverRequest(String url) {
        Type type = new TypeToken<JsonObject>() {
        }.getType();
        return new GsonRequest<>(
                url,
                type,
                null,
                new Response.Listener<JsonObject>() {
                    @Override
                    public void onResponse(JsonObject response) {
                        sendCallbackMedia(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Erro apresentado" + error.getLocalizedMessage());
                    }
                }
        );
    }

    private void sendCallbackMedia(JsonObject media) {

        this.callback.OnGetQueueValues(media);
    }
}
