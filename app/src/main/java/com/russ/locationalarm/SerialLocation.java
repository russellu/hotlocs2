package com.russ.locationalarm;

import java.io.Serializable;

public class SerialLocation implements Serializable {

    final private double latitude;
    final private double longitude;
    final private double accuracy;
    final private long UTCTime;


    public SerialLocation(double latitude, double longitude, double accuracy, long UTCTime){
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.UTCTime = UTCTime;
    }

    public double getLatitude(){
        return  latitude;
    }
    public double getLongitude(){
        return longitude;
    }
    public double getAccuracy(){return accuracy;}
    public long getUTCTime(){return UTCTime;}

    @Override
    public boolean equals(Object other){
        SerialLocation otherLocation = (SerialLocation)other;
        if(otherLocation.getLatitude() == latitude && otherLocation.getLongitude()==longitude)
            return true;
        else
            return false;
    }

}