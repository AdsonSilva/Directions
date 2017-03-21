package com.example.treinamento_huawei.directions;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.example.treinamento_huawei.directions.api.GsonPostRequest;
import com.example.treinamento_huawei.directions.api.GsonRequest;
import com.example.treinamento_huawei.directions.api.RequestQueueSingleton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnGetQueueValues {

    private static final int PERMISSION_ALL = 2;
    private static final String PREFS_NAME = "MapsTest";
    private GoogleMap mMap;

    private TextView tvCoordinate;
    private GoogleApiClient mGoogleApiClient;
    private Location oldLoc;
    private Date oldTime;
    private double logEmbdd;
    private double latEmbdd;
    Location lastLocation;
    private double latitude;
    private double longitude;
    private double speepServ;
    private double radius;
    private double beginQueueLat;
    private double beginQueueLog;
    private TextView prevision;
    private double margin;
    private ArrayList<Double> insidePoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        MapsActivity.context = getApplicationContext();


        tvCoordinate = (TextView) findViewById(R.id.tv_coordinate);
        prevision = (TextView) findViewById(R.id.Previsao);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                prevision.setText(settings.getString("location", "oi"));


            }
        });

        getLocation();

    }

    private static Context context;

    public static Context getAppContext() {
        return MapsActivity.context;
    }

    public void getLocation() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (hasPermissions(MapsActivity.this, permissions)) {
            GetQueueValues getQueueValues = new GetQueueValues(this);
            GsonRequest<JsonObject> request = getQueueValues.getValues();
            request.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestQueueSingleton.getInstance(this).addToRequestQueue(request.setTag("dailyMenu"));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, PERMISSION_ALL);
            }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull final String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL:
                boolean permissionOk = true;
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            permissionOk = false;
                        }
                    }
                }
                if (permissionOk) {
                    getLocation();
                } else {
                    break;
                }
        }
    }


    @Override
    public void OnGetQueueValues(JsonObject media) {
        Log.d("Teste", media.toString());
        this.latEmbdd = media.getAsJsonObject("ru").get("latitude").getAsDouble();
        this.logEmbdd = media.getAsJsonObject("ru").get("longitude").getAsDouble();
        this.speepServ = media.get("velocity").getAsDouble();
        this.radius = media.get("radius").getAsDouble();
        this.beginQueueLat = media.getAsJsonArray("beginQueue").get(0).getAsJsonObject().get("latitude").getAsDouble();
        this.beginQueueLog = media.getAsJsonArray("beginQueue").get(0).getAsJsonObject().get("longitude").getAsDouble();
        this.margin = media.get("margin").getAsDouble();
        int interval = media.get("interval").getAsInt();

        double latI = media.getAsJsonArray("insidePoints").get(0).getAsJsonObject().get("latitudei").getAsDouble();
        double latF = media.getAsJsonArray("insidePoints").get(0).getAsJsonObject().get("latitudef").getAsDouble();
        double logI = media.getAsJsonArray("insidePoints").get(0).getAsJsonObject().get("longitudei").getAsDouble();
        double logF = media.getAsJsonArray("insidePoints").get(0).getAsJsonObject().get("longitudef").getAsDouble();

        this.insidePoints.add(latI);
        this.insidePoints.add(latF);
        this.insidePoints.add(logI);
        this.insidePoints.add(logF);


        double[] insidePoints = new double[4];

        insidePoints[0] = latI;
        insidePoints[1] = latF;
        insidePoints[2] = logI;
        insidePoints[3] = logF;


        Bundle bundle = new Bundle();
        bundle.putDouble("speedServ", speepServ);
        bundle.putDouble("radius", radius);
        bundle.putDouble("margin", margin);
        bundle.putDoubleArray("insidePoints", insidePoints);
        bundle.putDouble("latCent", latEmbdd);
        bundle.putDouble("logCent", logEmbdd);
        bundle.putInt("interval", interval);


        Log.d("Teste", String.valueOf(latEmbdd));
        Log.d("Teste", String.valueOf(logEmbdd));
        Log.d("Teste", String.valueOf(speepServ));
        Log.d("Teste", String.valueOf(radius));
        Log.d("Teste", String.valueOf(beginQueueLat));
        Log.d("Teste", String.valueOf(beginQueueLog));
        createAlarmToUpdateGPS(bundle);
    }


    public void createAlarmToUpdateGPS(Bundle bundle) {
        Log.d("Teste", "oi");

        int intervalServ = bundle.getInt("interval");
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(MapsActivity.this, WakefulReceiver.class);
        intent.putExtra("bundle", bundle);
        boolean flag = (PendingIntent.getBroadcast(MapsActivity.this, 0,
                intent, PendingIntent.FLAG_NO_CREATE) == null);

        if (flag) {
            PendingIntent alarmIntent = PendingIntent.getBroadcast(MapsActivity.this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            int intervalTimeMillis = 1000 * 60 * intervalServ;
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), intervalTimeMillis,
                    alarmIntent);
        } else {
            PendingIntent sender = PendingIntent.getBroadcast(MapsActivity.getAppContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(sender);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(MapsActivity.this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());


            int intervalTimeMillis = 1000 * 60 * intervalServ;
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), intervalTimeMillis,
                    alarmIntent);


        }
    }
    
}
