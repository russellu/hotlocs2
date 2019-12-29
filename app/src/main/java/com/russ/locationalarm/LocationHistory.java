package com.russ.locationalarm;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.maps.model.LatLng;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class LocationHistory  extends FragmentActivity {

    private static ArrayList<MyLocale> myLocales = new ArrayList<>();

    private Billing billing;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_viewer);

        loadLocales(this);

     //   billing = new Billing(this);
     //   billing.checkPurchaseTokenAsync(this);
     //   boolean purchased = billing.checkPurchaseToken();

     //   if(!purchased && !Billing.adsRemoved) {
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

    public static List<MyLocale> getMyLocales(){
        return myLocales;
    }

    public static void addLocale(String name, LatLng latLng){
        MyLocale newLocale = new MyLocale(name, latLng);
        myLocales.add(newLocale);

    }

    public static void saveLocales(Context context){
        try {
            FileOutputStream listFos = context.openFileOutput("savedLocales", Context.MODE_PRIVATE);
            ObjectOutputStream listOs = new ObjectOutputStream(listFos);
            listOs.writeObject(myLocales);
            listOs.close();
            listFos.close();
        }catch(Exception e){e.printStackTrace();}

        Log.d("service","saved locales to internal storage");

    }

    public static void loadLocales(Context context){
        try {
            FileInputStream listFis = context.openFileInput("savedLocations");
            ObjectInputStream listIs = new ObjectInputStream(listFis);
            myLocales = (ArrayList<MyLocale>) listIs.readObject();
            listIs.close();
            listFis.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("service","loaded saved locales from internal storage");
    }

    @Override
    protected void onStop(){
        super.onStop();
        saveLocales(this);
    }
}
