package com.russ.locationalarm;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Billing implements PurchasesUpdatedListener {

    private BillingClient billingClient;
    private Activity context;
    private SkuDetails skuDetails;

    public Billing(Activity context){
        this.context = context;


        billingClient = BillingClient.newBuilder(context).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });



        List<String> skuList = new ArrayList<>();
        skuList.add("removeads");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        // Process the result.
                        Log.d("service","sku = " + billingResult);
                        if(skuDetailsList!= null && skuDetailsList.size() > 0) {
                            Log.d("service","setting sku details");
                            skuDetails = skuDetailsList.get(0);
                        }
                    }
                });

    }

    public void initPurchase(){
        // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();

        BillingResult responseCode = billingClient.launchBillingFlow(context, flowParams);

    }

    public void handlePurchase(Purchase purchase){
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {

            // Grant entitlement to the user.
            writePurchaseToken();

            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener =
                        new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {

                    }
                };
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        }
    }

    public void writePurchaseToken(){
        try {
            FileOutputStream fos = context.openFileOutput("purchaseToken", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject("purchased");
            os.close();
            fos.close();
        }catch(Exception e){e.printStackTrace();}
    }

    public static boolean checkPurchaseToken(Context context){

        boolean tokenPurchased = false;

        try {
            FileInputStream listFis = context.openFileInput("purchaseToken");
            if(listFis != null)
                tokenPurchased = true;
            ObjectInputStream listIs = new ObjectInputStream(listFis);
            listIs.close();
            listFis.close();
        }catch(Exception e){}

        return tokenPurchased;
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && list != null) {
            for (Purchase purchase : list) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }
}
