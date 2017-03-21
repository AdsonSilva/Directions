package com.example.treinamento_huawei.directions;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.example.treinamento_huawei.directions.MapsActivity;
import com.example.treinamento_huawei.directions.api.GsonPostRequest;
import com.example.treinamento_huawei.directions.api.RequestQueueSingleton;
import com.example.treinamento_huawei.directions.OnPostSpeed;
import com.example.treinamento_huawei.directions.PostSpeed;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by huawei on 02/03/17.
 */

public class GPSService extends IntentService implements OnPostSpeed {
    private GoogleApiClient mGoogleApiClient;
    private static final String TAGLOC = "Teste";

    private double logCenter;
    private double latCenter;
    private double speepServ;
    private double radius;
    private double margin;
    private double[] insidePoints;
    public static final String PREFS_NAME = "MyPrefsFileMaps";

    public GPSService()
    {
        super("GPSService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAGLOC, "GPS Service Handle Intent");
        Bundle bundle = intent.getExtras().getBundle("bundle");
        speepServ = (double) bundle.get("speedServ");
        radius = (double) bundle.get("radius");
        margin = (double) bundle.get("margin");
        insidePoints = (double[]) bundle.get("insidePoints");
        latCenter = (double) bundle.get("latCent");
        logCenter = (double) bundle.get("logCent");
        callConnectionUpdate();
    }

    private synchronized void callConnectionUpdate() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        if(connectionResult!= null){
                            Log.i(TAGLOC, "onConnectionFailed(" + connectionResult + ")");
                        }
                    }
                })
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {

                        Log.i(TAGLOC, "onConnected(" + bundle + ")");

                        LocationRequest mLocationRequest = new LocationRequest();
                        mLocationRequest.setInterval(5000);
                        mLocationRequest.setFastestInterval(1000);
                        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


                        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        LocationServices
                                .FusedLocationApi
                                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
                                    @Override
                                    public void onLocationChanged(Location l) {
                                        if (l != null) {


                                            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                                            double oldLocLat = Double.valueOf(settings.getString("oldLocLat", "0.0"));
                                            double oldLocLog = Double.valueOf(settings.getString("oldLocLog", "0.0"));
                                            long oldTime = settings.getLong("oldTime", Long.parseLong(String.valueOf(0)));//TERMINEI AQUI ONTEM

                                            Date tempo = new Date();
                                            SharedPreferences.Editor editor = settings.edit();
                                            editor.putLong("oldTime", tempo.getTime());
                                            editor.putString("oldLocLat", String.valueOf(l.getLatitude()));
                                            editor.putString("oldLocLog", String.valueOf(l.getLongitude()));
                                            editor.commit();

                                            String distance;
                                            if (oldLocLat != 0.0 || oldLocLog != 0.0) {
                                                distance = String.valueOf(distanceDoubleLocation(oldLocLat, oldLocLog, l));
                                            } else {
                                                distance = "0.0";
                                            }

                                            long tempoEntreupdates;
                                            if (oldTime != 0.0) {
                                                tempoEntreupdates = tempo.getTime() - oldTime;
                                            } else {
                                                tempoEntreupdates = (long) -1;
                                            }


                                            Log.d(TAGLOC, String.valueOf(tempoEntreupdates));
                                            double speedMy = calculaVelocidadeMedia(Double.valueOf(distance), TimeUnit.MILLISECONDS.toSeconds(tempoEntreupdates));

                                            boolean inRadius = Math.abs(distanceDoubleLocation(oldLocLat, oldLocLog, l)) < radius;
                                            boolean queueSpeed = (speedMy > speepServ - (speepServ * margin) && speedMy < speepServ + (speepServ * margin));
                                            boolean inLat = (Math.abs(l.getLatitude()) < Math.abs(insidePoints[0])) && (Math.abs(l.getLatitude()) > Math.abs(insidePoints[1]));
                                            boolean inLog = (Math.abs(l.getLongitude()) > Math.abs(insidePoints[2])) && (Math.abs(l.getLongitude()) < Math.abs(insidePoints[3]));
                                            boolean inside = inLat && inLog;
                                            Log.d(TAGLOC, "radius: " + String.valueOf(inRadius));
                                            Log.d(TAGLOC, "speed"+ String.valueOf(queueSpeed));
                                            Log.d(TAGLOC, "inside"+ String.valueOf(inside));
                                            Log.d(TAGLOC, "bool"+ String.valueOf(l.getLatitude()) + insidePoints[0]);
                                            Log.d(TAGLOC, "bool"+ String.valueOf(l.getLatitude()) + insidePoints[1]);
                                            Log.d(TAGLOC, "bool"+ String.valueOf(l.getLongitude()) + insidePoints[2]);
                                            Log.d(TAGLOC, "bool"+ String.valueOf(l.getLongitude()) + insidePoints[3]);

                                            StringBuilder stringBuilder = new StringBuilder();
                                            stringBuilder.append("Start" + tempo);
                                            stringBuilder.append("inRadius" + String.valueOf(inRadius));
                                            stringBuilder.append("speed" + String.valueOf(inRadius));
                                            stringBuilder.append("location" + l);

                                            new AlertDialog.Builder(MapsActivity.getAppContext())
                                                    .setTitle("TESTE")
                                                    .setMessage(stringBuilder.toString())
                                                    .setCancelable(false)
                                                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                        }
                                                    }).show();




                                        }
                                    }
                                });

                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }

                })
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    public static double distanceDoubleLocation(double lat, double log, Location EndP) {
        double lat1 = lat;
        double lat2 = EndP.getLatitude();
        double lon1 = log;
        double lon2 = EndP.getLongitude();
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return 6366000 * c;
    }

    public double calculaVelocidadeMedia(double deslocamento, long tempoGasto) {
        return deslocamento / tempoGasto;
    }

    private void postSpeed(double speedMy) {
        Map<String, String> params = new HashMap<>();
        params.put("speed", String.valueOf(speedMy));
        PostSpeed postSpeed = new PostSpeed(this);
        GsonPostRequest request = postSpeed.serverRequestString(params);
        request.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueSingleton.getInstance(this).addToRequestQueue(request.setTag("dailyMenu"));


    }



    @Override
    public void OnPostSpeed(double media) {

    }
}
