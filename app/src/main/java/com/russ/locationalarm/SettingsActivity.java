package com.russ.locationalarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.Map;

public class SettingsActivity extends Activity {

    private Button resetButton;
    private SeekBar updatesPerMinuteSeekBar;
    private TextView updatesPerMinuteTextView;

    private static final int[] updatesPerMinuteValues = {1,2,3,6,8,10,12,15,20,30,60};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        resetButton = findViewById(R.id.resetbutton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("RESET all locations")
                        .setMessage("permanently reset locations?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                MapUtils.resetLocs();
                               // SaveLocations.saveCurrentLocations(MapsActivity.this);
                               // buildAndDisplayHeatMap();
                               // tileOverlay.remove();
                                }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        updatesPerMinuteTextView = findViewById(R.id.updatesperminute);
        updatesPerMinuteTextView.setText("Updates per minute: " + MapUtils.updatesPerMinute);

        updatesPerMinuteSeekBar = findViewById(R.id.seekBar);
        updatesPerMinuteSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updatesPerMinuteTextView.setText("Updates per minute: " + updatesPerMinuteValues[progress]);
                MapUtils.updatesPerMinute = updatesPerMinuteValues[progress];
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        initializeSeekBarProgress();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }


    public void initializeSeekBarProgress(){
        ArrayList<Integer> temp = new ArrayList<Integer>();
        for(int i=0;i<updatesPerMinuteValues.length;i++)
            temp.add(updatesPerMinuteValues[i]);

        Log.d("service","updatesperminute = " + MapUtils.updatesPerMinute + " index = " + temp.indexOf(MapUtils.updatesPerMinute));
        updatesPerMinuteSeekBar.setProgress(temp.indexOf(MapUtils.updatesPerMinute));


    }

}