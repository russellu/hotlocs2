package com.russ.locationalarm;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class MapUtils {

    public static ArrayList<SerialLocation> rawLocations = new ArrayList<>();

    public static ArrayList<HashSet<SerialLocation>> listLocations = new ArrayList<HashSet<SerialLocation>>(){{
                    add(new HashSet<SerialLocation>());
                    add(new HashSet<SerialLocation>());
                    add(new HashSet<SerialLocation>());
                    add(new HashSet<SerialLocation>());
                }};

    public static int displayMode = 0;
    public static int updatesPerMinute = 6;
    public static int[] precisionConstants = {200000,80000,10000};
    public static int minAccuracy = 20;

    /*
        private static GeofencingClient geofencingClient;
        private static ArrayList<Geofence> geofences = new ArrayList<>();

        private static Ctext context;

        public static void setupGeofencingClient(Context context){
            geofencingClient = LocationServices.getGeofencingClient(context);
            MapUtils.context = context;
        }

        private static PendingIntent geofencePendingIntent;
        private static PendingIntent getGeofencePendingIntent() {
            // Reuse the PendingIntent if we already have it.

            Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
            // calling addGeofences() and removeGeofences().
            geofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            return geofencePendingIntent;
        }

        private static GeofencingRequest getGeofencingRequest() {
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            builder.addGeofences(geofences);
            return builder.build();
        }

        public static void setupGeofenceOnCurrentLocation(LatLng currentLocation){
            String key = "geofencekey";
            geofences.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(key)
                    .setCircularRegion(
                            currentLocation.latitude,
                            currentLocation.longitude,
                            1
                    )
                    .setExpirationDuration(600*1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_ENTER)
                    .setNotificationResponsiveness(1000)
                    .build());


            geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("service","SUCCESSFULLY added geofence!");
                            // Geofences added
                            // ...
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to add geofences
                            // ...
                        }
                    });
        }
    */

    public static void incrementDisplayMode(){
        displayMode++;
        if(displayMode>2){
            displayMode = 0;
        }
    }

    public static ArrayList<SerialLocation> getRawLocs(){
        return rawLocations;
    }

    public static void setLocs(ArrayList<SerialLocation> serialLocations){

        resetLocs();
        Log.d("service","serialLocationSize = " + serialLocations.size());
        for(SerialLocation loc: serialLocations) {
            addPoint(loc.getLatitude(), loc.getLongitude(), loc.getAccuracy(), loc.getUTCTime());
        }
    }

    public static void resetLocs(){
        rawLocations = new ArrayList<>();
        listLocations = new ArrayList<HashSet<SerialLocation>>(){{
            add(new HashSet<SerialLocation>());
            add(new HashSet<SerialLocation>());
            add(new HashSet<SerialLocation>());
        }};
    }

/*
    public static void checkForStationary(){

        float updateTimeSeconds = 60/updatesPerMinute;
        double maxDiff = 0;

        if(rawLatLngs.size() > 10){
            double sumLat = 0;
            double sumLng = 0;
            for(int i=rawLatLngs.size()-10;i<rawLatLngs.size();i++) {
                for (int j = rawLatLngs.size() - 10; j < rawLatLngs.size(); j++) {
                    double latDiff = rawLatLngs.get(i).latitude - rawLatLngs.get(j).latitude;
                    double lngDiff = rawLatLngs.get(i).longitude - rawLatLngs.get(j).longitude;
                    double diff = Math.abs(latDiff + lngDiff);
                    if (diff > maxDiff)
                        maxDiff = diff;
                }
                sumLat += rawLatLngs.get(i).latitude;
                sumLng += rawLatLngs.get(i).longitude;
            }
            LatLng avgLatLng = new LatLng(sumLat/10,sumLng/10);
            Log.d("service","checking for stationary, time = "+updateTimeSeconds*10 + " maxdiff = " + maxDiff);
            Log.d("service","average latlng = " + avgLatLng);
            if(maxDiff < 0.005){
                Log.d("service","setting up geofence!");
                setupGeofenceOnCurrentLocation(avgLatLng);
            }
        }
    }
*/
    public static void addPoint(double latitude, double longitude, double accuracy, long UTCTime){


        rawLocations.add(new SerialLocation(latitude,longitude,accuracy,UTCTime));
        for(int i=0;i<precisionConstants.length;i++) {

            int tempLat = (int) (latitude * precisionConstants[i]);
            int tempLng = (int) (longitude * precisionConstants[i]);

            LatLng tempLatLng = new LatLng((float) tempLat / precisionConstants[i],
                    (float) tempLng / precisionConstants[i]);

            if(!listLocations.get(i).contains(tempLatLng))
                listLocations.get(i).add(new SerialLocation(tempLatLng.latitude,
                                                            tempLatLng.longitude,
                                                            accuracy,
                                                            UTCTime));
        }
    }

    public static ArrayList<LatLng> getLocs(){
        ArrayList<LatLng> locs = new ArrayList<>();
        for(SerialLocation loc: listLocations.get(displayMode))
            if(loc.getAccuracy() < minAccuracy)
                locs.add(new LatLng(loc.getLatitude(),
                                    loc.getLongitude()));
        return locs;
    }

}
