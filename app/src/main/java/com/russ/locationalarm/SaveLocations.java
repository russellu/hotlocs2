package com.russ.locationalarm;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class SaveLocations {


    public static void saveCurrentLocations(MapsActivity mapsActivity){
        try {
            FileOutputStream listFos = mapsActivity.getActivity().openFileOutput("savedLocations", Context.MODE_PRIVATE);
            ObjectOutputStream listOs = new ObjectOutputStream(listFos);
            listOs.writeObject(MapUtils.getRawLocs());
            listOs.close();
            listFos.close();
        }catch(Exception e){e.printStackTrace();}

        Log.d("service","saved locations!");

    }

    public static ArrayList<SerialLocation> getSavedLocations(MapsActivity mapsActivity) {

        Log.e("service","getting locations!");

        ArrayList<SerialLocation> loadedLocations = null;

        try {
            FileInputStream listFis = mapsActivity.getActivity().openFileInput("savedLocations");
            ObjectInputStream listIs = new ObjectInputStream(listFis);
            loadedLocations = (ArrayList<SerialLocation>) listIs.readObject();
            listIs.close();
            listFis.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(loadedLocations != null)
            return loadedLocations;
        else
            return null;
    }

    /*
    public static ArrayList<LatLng> serialLatLngToLatLng(ArrayList<SerialLocation> serialLatLngs){
        ArrayList<LatLng> latLngs = new ArrayList<>();
        for(SerialLocation serialLatLng: serialLatLngs)
            latLngs.add(new LatLng(serialLatLng.getLatitude(),serialLatLng.getLongitude()));

        return latLngs;
    }

    public static ArrayList<SerialLocation> latLngToSerialLatLng(ArrayList<LatLng> latLngs){
        ArrayList<SerialLocation> serialLatLngs = new ArrayList<>();
        for(LatLng latLng: latLngs)
            serialLatLngs.add(new SerialLocation(latLng.latitude,latLng.longitude));

        return serialLatLngs;
    }
*/
}
