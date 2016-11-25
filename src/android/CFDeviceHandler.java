package com.odd.cardflight;

import android.Manifest;
import android.util.Log;
import android.bluetooth.BluetoothDevice;
import android.app.AlertDialog;
import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.getcardflight.interfaces.CardFlightDeviceHandler;
import com.getcardflight.models.CFEMVMessage;
import com.getcardflight.models.Card;
import com.getcardflight.models.CardFlightError;
import com.getcardflight.models.Charge;
import com.getcardflight.models.Reader;
import com.getcardflight.util.Constants;
import com.getcardflight.util.PermissionUtils;

import org.apache.cordova.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class CFDeviceHandler implements CardFlightDeviceHandler {

    private static final String TAG = "FROM COLLECT FOR STRIPE: ";

    public CallbackContext onReaderConnectedCallbackId;
    private Context mContext;

    public CFDeviceHandler() {
        Log.d(TAG, "Init the thing");
    }

    public enum ReaderStatus {
        CONNECTING,
        UPDATING,
        CONNECTED,
        DISCONNECTED,
        UNKNOWN,
        NOT_COMPATIBLE
    }

    @Override
    public void emvTransactionResult(Charge charge, boolean requiresSignature, CFEMVMessage message) {
        
    }

    @Override
    public void emvRequestApplicationSelection(ArrayList appList) {
        

        // Implement method to select which AID,
        // then call - Reader.getDefault(mContext).emvSelectApplication(aidIndex);
    }

    @Override
    public void emvMessage(CFEMVMessage message) {
        
    }

    @Override
    public void emvCardResponse(HashMap<String, Object> hashMap) {
        String cardType = (String) hashMap.get(Constants.CARD_TYPE);
        String firstSix = (String) hashMap.get(Constants.FIRST_SIX);
        String lastFour = (String) hashMap.get(Constants.LAST_FOUR);

        
    }

    @Override
    public void emvErrorResponse(CardFlightError error) {
        
    }

    @Override
    public void emvAmountRequested() {
        // prompt for amount - doesn't happen if amount properly set
    }

    @Override
    public void readerBatteryLow() {
        
    }

    @Override
    public void emvCardDipped() {
        
    }

    @Override
    public void emvCardRemoved() {
        
    }

    @Override
    public void emvTransactionVaultID(String s) {
    
    }

    @Override
    public void readerCardResponse(Card card, CardFlightError error) {
        if (error == null) {
         
        } else {
         
        }
    }

    @Override
    public void readerIsAttached() {
        // Deprecated - This method is a duplicate of readerIsConnecting as of version 3.1 and will be
        // removed in a future release
        Log.d(TAG, "Reader attached!");
    }

    @Deprecated
    @Override
    public void readerIsConnecting() {
        Log.d(TAG, "Reader connecting!");
    }

    @Override
    public void readerIsUpdating() {
        
    }

    public void showAlert() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);

        dialogBuilder.setTitle("Confirm Transaction");
        dialogBuilder.setMessage("prompt");

        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                

                dialog.dismiss();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                

                dialog.dismiss();
            }
        });

        dialogBuilder.create().show();
    }

    @Override
    public void readerIsConnected(boolean isConnected, CardFlightError error) {
        Log.d(TAG, "Reader connected!");
        // PluginResult pluginResult;
        // if (error == null) {
        //     pluginResult = new PluginResult(PluginResult.Status.OK);
        // } else {
        //     pluginResult = new PluginResult(PluginResult.Status.ERROR);
        // }
        // pluginResult.setKeepCallback(true);
        // onReaderConnectedCallbackId.sendPluginResult(pluginResult);
    }

    @Override
    public void readerSwipeDetected() {
        Log.d(TAG, "Swipe!");
    }

    @Override
    public void readerIsDisconnected() {
    
    }

    @Override
    public void readerSwipeDidCancel() {
        
    }

    @Override
    public void readerNotDetected() {
       
    }

    @Override
    public void selectBluetoothDevice(ArrayList<BluetoothDevice> arrayList) {
        
    }
}