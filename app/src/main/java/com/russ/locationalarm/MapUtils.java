package com.russ.locationalarm;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;

public class MapUtils {

    public static ArrayList<LatLng> listLatLngs = new ArrayList<>();
    public static int displayMode = 1;
    public static int updatesPerMinute = 6;


    public static void incrementDisplayMode(){
        displayMode++;
        if(displayMode>3){
            displayMode = 0;
        }
    }

    public static ArrayList<LatLng> getRawLocs(){
        return listLatLngs;
    }

    public static void setLocs(ArrayList<LatLng> locs){
        listLatLngs = locs;
    }

    public static void resetLocs(){
        listLatLngs = new ArrayList<>();

    }

    public static void addPoint(LatLng newLatLng){
        listLatLngs.add(newLatLng);
    }

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

    public static ArrayList<LatLng> clusterReduce(ArrayList<LatLng> allPoints, int precision){
        ArrayList<LatLng> reducedPoints = new ArrayList<LatLng>();

        for(int i=0;i<allPoints.size();i++){

            int tempLat = (int)(allPoints.get(i).latitude*precision);
            int tempLng = (int)(allPoints.get(i).longitude*precision);
            LatLng tempLatLng = new LatLng((float)tempLat/precision,(float)tempLng/precision);
            if(!reducedPoints.contains(tempLatLng))
                reducedPoints.add(tempLatLng);
        }

        return  reducedPoints;
    }

}
