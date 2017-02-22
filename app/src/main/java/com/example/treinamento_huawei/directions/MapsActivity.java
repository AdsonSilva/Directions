package com.example.treinamento_huawei.directions;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnGetQueueValues {

    private GoogleMap mMap;

    private TextView tvCoordinate;
    private GoogleApiClient mGoogleApiClient;
    private Location oldLoc;

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
                callConnection();
            }
        });

        getQueueValues();

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("LOG", "onConnected(" + bundle + ")");

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices
                .FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {


                            Log.i("LOG", "latitude: " + location.getLatitude());
                            Log.i("LOG", "longitude: " + location.getLongitude());

                            LatLng ruUFCG = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(ruUFCG).title("Marker in Sydney"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(ruUFCG));
                            mMap.moveCamera(CameraUpdateFactory.zoomIn());

                            String distance;
                            if (oldLoc != null) {
                                distance = String.valueOf(distance(oldLoc, location));
                            } else {
                                distance = "Impossivel pegar distancia";
                            }

                            tvCoordinate.setText(location.getLatitude() + " | " + location.getLongitude() + " Rate: " + distance);
                            sendLocalization(location);
                            oldLoc = location;

                        }
                    }
                });
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
    }

    public void sendLocalization(Location location){
        PostLocalization postLocalization = new PostLocalization(new OnPostLocalization() {
            @Override
            public void onPostlocalizationSucess() {
                Log.d("TEST", "testt");

            }

            @Override
            public void onPostlocalizationError() {

            }
        });
        GsonPostRequest request = postLocalization.serverRequestString(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        request.setRetryPolicy(new DefaultRetryPolicy(300, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueSingleton.getInstance(this).addToRequestQueue(request.setTag("dailyMenu"));

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

    public double calculaVelocidadeMedia(double deslocamento, double tempoGasto) {
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
        Log.d("GET_QUEUE_VALUE", media.toString());
    }
}
