package com.example.treinamento_huawei.directions;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnGetQueueValues {

    private GoogleMap mMap;

    private TextView tvCoordinate;
    private GoogleApiClient mGoogleApiClient;
    private Location oldLoc;
    private Date oldTime;
    private double logEmbdd;
    private double latEmbdd;
    Location location;
    private double latitude;
    private double longitude;
    private double speepServ;
    private double radius;
    private double beginQueueLat;
    private double beginQueueLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        tvCoordinate = (TextView) findViewById(R.id.tv_coordinate);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculaPrevisao();

            }
        });

        getQueueValues();

    }

    private void calculaPrevisao() {

    }

    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }



    private synchronized void callConnection() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        callConnection();
    }


    private synchronized void callConnectionOnce() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.i("LOG", "onConnectionFailed(" + connectionResult + ")");
                    }
                })
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.i("LOG", "onConnected(" + bundle + ")");

                        if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        Location l = LocationServices
                                .FusedLocationApi
                                .getLastLocation(mGoogleApiClient);

                        location = l;

                        if (l != null) {
                            Log.i("LOG", "latitude: " + l.getLatitude());
                            Log.i("LOG", "longitude: " + l.getLongitude());
                            tvCoordinate.setText(l.getLatitude() + " | " + l.getLongitude());
                            latitude = l.getLatitude();
                            longitude = l.getLongitude();

                            updateLocalizationOnRadius(l);
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.i("LOG", "onConnectionSuspended(" + i + ")");
                    }

                })
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void updateLocalizationOnRadius(Location l) {
        if (distanceDoubleLocation(latEmbdd, logEmbdd, l) < 60) {
            Log.d("test", "Estou no embdd");

            callConnectionUpdate();

        }
    }


    private synchronized void callConnectionUpdate() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.i("LOG", "onConnectionFailed(" + connectionResult + ")");
                    }
                })
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {

                        Log.i("LOG", "onConnected(" + bundle + ")");

                        LocationRequest mLocationRequest = new LocationRequest();
                        mLocationRequest.setInterval(3000);
                        mLocationRequest.setFastestInterval(5000);
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
                                            location = l;
                                            latitude = l.getLatitude();
                                            longitude = l.getLongitude();


                                            Log.i("LOG", "latitude: " + l.getLatitude());
                                            Log.i("LOG", "longitude: " + l.getLongitude());

                                            String distance;
                                            if (oldLoc != null) {
                                                distance = String.valueOf(distance(oldLoc, l));
                                            } else {
                                                distance = "0.0";
                                            }

                                            Date tempo = new Date();;

                                            Long tempoEntreupdates;
                                            if (oldTime != null) {
                                                tempoEntreupdates = tempo.getTime() - oldTime.getTime();
                                            } else {
                                                tempoEntreupdates = (long) -1;
                                            }


                                            sendLocalization(l);
                                            oldTime = tempo;
                                            oldLoc = l;


                                            Log.d("teste", String.valueOf(tempoEntreupdates));
                                            tvCoordinate.setText(l.getLatitude() + " | " + l.getLongitude() + " Rate: " + calculaVelocidadeMedia(Double.valueOf(distance), TimeUnit.MILLISECONDS.toSeconds(tempoEntreupdates)));

                                        }
                                    }
                                });

                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.i("LOG", "onConnectionSuspended(" + i + ")");

                    }
                })
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void sendLocalization(Location location){
        PostLocalization postLocalization = new PostLocalization(new OnPostLocalization() {
            @Override
            public void onPostlocalizationSucess() {
                Log.d("TEST", "Localização postada");

            }

            @Override
            public void onPostlocalizationError() {
                Log.d("TEST", "Localização não foi postada");

            }
        });
        GsonPostRequest request = postLocalization.serverRequestString(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        request.setRetryPolicy(new DefaultRetryPolicy(300, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueSingleton.getInstance(this).addToRequestQueue(request.setTag("dailyMenu"));

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("LOG", "onConnectionSuspended(" + i + ")");
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("LOG", "onConnectionFailed("+connectionResult+")");
    }

    public static double distance(Location StartP, Location EndP) {
        double lat1 = StartP.getLatitude();
        double lat2 = EndP.getLatitude();
        double lon1 = StartP.getLongitude();
        double lon2 = EndP.getLongitude();
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return 6366000 * c;
    }

    public double calculaVelocidadeMedia(double deslocamento, long tempoGasto) {
        return deslocamento/tempoGasto;
    }

    private void getQueueValues() {
        GetQueueValues getQueueValues = new GetQueueValues(this);
        GsonRequest<JsonObject> request = getQueueValues.getValues();
        request.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueSingleton.getInstance(this).addToRequestQueue(request.setTag("dailyMenu"));
    }

    @Override
    public void OnGetQueueValues(JsonObject media) {
        Log.d("Teste", media.toString());
        this.latEmbdd = media.getAsJsonObject("ru").get("latitude").getAsDouble();
        this.logEmbdd = media.getAsJsonObject("ru").get("longitude").getAsDouble();
        this.speepServ = media.getAsJsonObject("velocity").getAsDouble();
        this.radius = media.getAsJsonObject("radius").getAsDouble();
        this.beginQueueLat = media.getAsJsonObject("beginQueue").get("latitude").getAsDouble();
        this.beginQueueLog = media.getAsJsonObject("beginQueue").get("longitude").getAsDouble();
        Log.d("Teste", String.valueOf(latEmbdd));
        Log.d("Teste", String.valueOf(logEmbdd));
        Log.d("Teste", String.valueOf(speepServ));
        Log.d("Teste", String.valueOf(radius));
        Log.d("Teste", String.valueOf(beginQueueLat));
        Log.d("Teste", String.valueOf(beginQueueLog));
        callConnectionOnce();
    }


    public static double distanceDoubleLocation(double lat, double log, Location EndP) {
        double lat1 = lat;
        double lat2 = EndP.getLatitude();
        double lon1 = log;
        double lon2 = EndP.getLongitude();
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return 6366000 * c;
    }

}
