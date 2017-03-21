package com.example.treinamento_huawei.directions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class WakefulReceiver extends WakefulBroadcastReceiver {

    private static final String TAGLOC = "Teste";

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras().getBundle("bundle");
        Intent service = new Intent(context, GPSService.class);
        service.putExtra("bundle", bundle);
        Log.d(TAGLOC, "WakeFullService start");
        startWakefulService(context, service);
    }

}