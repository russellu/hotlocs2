package com.russ.locationalarm;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static FusedLocationProviderClient fusedLocationClient;
    private static Timer timer;
    private static boolean updating = true;
    private static int timerCounter = 0;

    public TileOverlay tileOverlay;
    public boolean movedToLocation = false;

    private int UPDATE_MS = 5000;

    private Button settingsButton;
    private Button modeButton;
    private Button playPauseButton;
    private Button locateButton;
    private Button myLocsButton;

    private TextView latLngTextView;
    private TextView currentLocationTextView;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private Billing billing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initButtons();
        initTextViews();

   //     billing = new Billing(this);
   //     billing.checkPurchaseTokenAsync(this);
    //    boolean purchased = billing.checkPurchaseToken();

       // if(!purchased && !Billing.adsRemoved) {
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });
            AdView mAdView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
      /*
        }
        else{
            AdView mAdView = findViewById(R.id.adView);
            mAdView.setVisibility(View.GONE);
        }
        */

    }

    public void initTextViews(){
        latLngTextView = findViewById(R.id.latlngtextview);
        currentLocationTextView = findViewById(R.id.currentlocationtextview);
    }

    public void initButtons(){

        modeButton = findViewById(R.id.modebutton);
        modeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapUtils.incrementDisplayMode();
                buildAndDisplayHeatMap();
            }
        });

        locateButton = findViewById(R.id.locatebutton);
        locateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MapUtils.getLocs().size() > 0)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(MapUtils.getLocs().get(MapUtils.getLocs().size()-1).latitude,
                                MapUtils.getLocs().get(MapUtils.getLocs().size()-1).longitude), 15.0f));
                else
                     Toast.makeText(MapsActivity.this,"no locations yet",Toast.LENGTH_SHORT).show();
            }
        });

        settingsButton = findViewById(R.id.settingsbutton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
                MapsActivity.this.startActivity(intent);
            }
        });

        playPauseButton = findViewById(R.id.playpausebutton);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(updating) {
                    timer.cancel();
                    stopLocationUpdates();
                    updating = false;
                    playPauseButton.setText("Updates paused");
                }else{
                    startLocationUpdates();
                    updateDisplay();
                    updating = true;
                    playPauseButton.setText("Pause updates");
                }
            }
        });

        myLocsButton = findViewById(R.id.mylocsbutton);
        myLocsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, LocationHistory.class);
                MapsActivity.this.startActivity(intent);
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initializeSavedData();
        checkLocationPermission();

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                Log.d("service","onmaplongclick latlng = " + latLng);
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Title for new location");
                final EditText input = new EditText(MapsActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String locationName = input.getText().toString();
                        LocationHistory.addLocale(locationName, latLng);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

    }

    public void initializeSavedData(){

        ArrayList<SerialLocation> loaded = SaveLocations.getSavedLocations(this);

        if(loaded != null) {
            MapUtils.setLocs(loaded);
            updateLocation(MapUtils.getLocs().get(MapUtils.getLocs().size()-1).latitude,
                    MapUtils.getLocs().get(MapUtils.getLocs().size()-1).longitude);
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("request locations")
                        .setMessage("request locations")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            //Request location updates:
            if(fusedLocationClient == null) {
                startLocationUpdates();
            }
            startLocationService();
            updateDisplay();

            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        if(fusedLocationClient == null) {
                            startLocationUpdates();
                        }
                        startLocationService();
                        updateDisplay();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }
    public void startLocationService(){
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);
    }

    private LocationCallback locationCallback;

    public void startLocationUpdates(){

        UPDATE_MS = (60/MapUtils.updatesPerMinute)*1000;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.d("service","location was null");
                    return;
                }
                for (Location location : locationResult.getLocations()) {


                  Log.d("service","location = " + location +
                          " updates per minute = " + MapUtils.updatesPerMinute + " accuracy = " + location.getAccuracy());
                    MapUtils.addPoint(location.getLatitude(),
                                      location.getLongitude(),
                                      location.getAccuracy(),
                                      location.getTime());

                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(5);
                    latLngTextView.setText("Lat: " + df.format(lat)+", Lng: " + df.format(lng));

                //    MapUtils.checkForStationary();
                }
            };
        };

        fusedLocationClient.requestLocationUpdates(mLocationRequest,
                locationCallback,
                Looper.getMainLooper());

    }

    LocationRequest mLocationRequest;
    private void createLocationRequest() {

        Log.d("service","creating location request");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_MS);
        mLocationRequest.setFastestInterval(UPDATE_MS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setMaxWaitTime(UPDATE_MS);



    }

    public MapsActivity getActivity(){
        return this;
    }



    public void updateDisplay() {

        if(timer!=null)
            timer.cancel();

        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                // When you need to modify a UI element, do so on the UI thread.
                // 'getActivity()' is required as this is being ran from a Fragment.

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(MapUtils.getRawLocs().size()>0) {
                            double latitude = MapUtils.getRawLocs().get(MapUtils.getRawLocs().size()-1).getLatitude();
                            double longitude = MapUtils.getRawLocs().get(MapUtils.getRawLocs().size()-1).getLongitude();
                            updateLocation(latitude, longitude);
                            Log.d("service","updating map at frequency: " + (60/MapUtils.updatesPerMinute)*1000);
                            if(timerCounter%50 == 0)
                                SaveLocations.saveCurrentLocations(getActivity());
                        }
                        else{
                            //show a toast that says waiting for first location update.
                            Context context = getApplicationContext();
                            CharSequence text = "Waiting for first location update...";
                            int duration = Toast.LENGTH_LONG;
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                    }
                });
            }
        }, 1000, (60/MapUtils.updatesPerMinute)*1000); // End of your timer code.

    }

    public void updateLocation(double latitude, double longitude){
        if(!movedToLocation){
            moveToCurrentLocation(latitude,longitude);
            movedToLocation = true;
        }
        else
            buildAndDisplayHeatMap();
    }

    public void moveToCurrentLocation(double latitude, double longitude){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(latitude,longitude), 15.0f));

        ArrayList<LatLng> list = new ArrayList<LatLng>();
        list.add(new LatLng(latitude,longitude));

        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder().data(
                list).build();

        tileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    public void buildAndDisplayHeatMap(){

        HeatmapTileProvider mProvider = null;

        if(!MapUtils.getLocs().isEmpty()) {
            mProvider = new HeatmapTileProvider.Builder().data(MapUtils.getLocs()).build();
        }
        if(mProvider!=null){
            mProvider.setRadius(15);
            tileOverlay.remove();
            tileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putCharSequence("playPauseState",playPauseButton.getText());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        CharSequence playPauseState = savedInstanceState.getCharSequence("playPauseState");
        playPauseButton.setText(playPauseState);
    }


    @Override
    protected void onResume(){
        super.onResume();

        if(updating) {
            stopLocationUpdates();
            startLocationUpdates();
            startLocationService();
            updateDisplay();
        }
    }

    public void stopLocationUpdates(){
        if(fusedLocationClient!=null && locationCallback != null)
            fusedLocationClient.removeLocationUpdates(locationCallback);
    }

}
