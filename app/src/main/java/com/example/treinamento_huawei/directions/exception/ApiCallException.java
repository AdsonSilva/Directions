package com.example.treinamento_huawei.directions.exception;

/**
 * Created by huawei on 17/10/16.
 */

import com.android.volley.VolleyError;

public class ApiCallException extends VolleyError {

    public ApiCallException(String message) {
        super(message);
    }

}