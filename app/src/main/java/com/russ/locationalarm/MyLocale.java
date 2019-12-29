package com.russ.locationalarm;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class MyLocale implements Serializable {

    private String label;
    private LatLng location;

    public MyLocale(String label, LatLng location){
        this.label = label;
        this.location = location;
    }


}
