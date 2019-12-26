package com.russ.locationalarm;

import java.io.Serializable;

public class SerialLatLng implements Serializable {

    private double latitude;
    private double longitude;

    public SerialLatLng(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude(){
        return  latitude;
    }
    public double getLongitude(){
        return longitude;
    }
}
