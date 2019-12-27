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

    public static ArrayList<ArrayList<LatLng>> listLatLngs = new ArrayList<ArrayList<LatLng>>(){{
                    add(new ArrayList<LatLng>());
                    add(new ArrayList<LatLng>());
                    add(new ArrayList<LatLng>());
                    add(new ArrayList<LatLng>());
                }};

    public static ArrayList<LatLng> rawLatLngs = new ArrayList<LatLng>();
    public static int displayMode = 1;
    public static int updatesPerMinute = 3;
    public static int[] precisionConstants = {0,300000,30000,10000};

    private static GeofencingClient geofencingClient;
    private static ArrayList<Geofence> geofences = new ArrayList<>();

    private static Context context;
/*

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
        if(displayMode>3){
            displayMode = 0;
        }
    }

    public static ArrayList<LatLng> getRawLocs(){
        return rawLatLngs;
    }

    public static void setLocs(ArrayList<LatLng> locs){
        for(int i=0;i<locs.size();i++) {
            addPoint(locs.get(i));
        }
    }

    public static void resetLocs(){
        rawLatLngs = new ArrayList<>();
        listLatLngs = new ArrayList<ArrayList<LatLng>>(){{
            add(new ArrayList<LatLng>());
            add(new ArrayList<LatLng>());
            add(new ArrayList<LatLng>());
            add(new ArrayList<LatLng>());
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
    public static void addPoint(LatLng newLatLng){

        rawLatLngs.add(newLatLng);

        for(int i=0;i<precisionConstants.length;i++) {
            int tempLat = (int) (newLatLng.latitude * precisionConstants[i]);
            int tempLng = (int) (newLatLng.longitude * precisionConstants[i]);
            LatLng tempLatLng = new LatLng((float) tempLat / precisionConstants[i],
                    (float) tempLng / precisionConstants[i]);

            if(!listLatLngs.get(i).contains(tempLatLng))
                listLatLngs.get(i).add(tempLatLng);
        }


    }

    public static ArrayList<LatLng> getLocs(){

        if(displayMode==0)
            return rawLatLngs;
        else
            return listLatLngs.get(displayMode);
    }

    /*
    public static ArrayList<LatLng> getLocs(){

        if(displayMode == 1){
            return clusterReduce(listLatLngs,300000);
        }
        else if(displayMode == 2){
            return clusterReduce(listLatLngs, 30000);
        }
        else if(displayMode == 3){
            return clusterReduce(listLatLngs, 3000);
        }
        else
            return listLatLngs;
    }
*/
    /*
    public static ArrayList<LatLng> clusterReduce(ArrayList<LatLng> allPoints, int precision){
        ArrayList<LatLng> reducedPoints = new ArrayList<LatLng>();
        HashSet<LatLng> hashedPoints = new HashSet<>();

        for(int i=0;i<allPoints.size();i++){
            int tempLat = (int)(allPoints.get(i).latitude*precision);
            int tempLng = (int)(allPoints.get(i).longitude*precision);
            LatLng tempLatLng = new LatLng((float)tempLat/precision,(float)tempLng/precision);
            //if(!reducedPoints.contains(tempLatLng))
            //    reducedPoints.add(tempLatLng);
            if(hashedPoints.add(tempLatLng))
                reducedPoints.add(tempLatLng);
        }

        return  reducedPoints;
    }

*/

}
