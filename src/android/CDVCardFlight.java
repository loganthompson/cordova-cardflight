package com.odd.cardflight;

import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.bluetooth.BluetoothDevice;

import com.getcardflight.interfaces.CardFlightApiKeyAccountTokenHandler;
import com.getcardflight.interfaces.CardFlightAuthHandler;
import com.getcardflight.interfaces.CardFlightCaptureHandler;
import com.getcardflight.interfaces.CardFlightDecryptHandler;
import com.getcardflight.interfaces.CardFlightPaymentHandler;
import com.getcardflight.interfaces.CardFlightTokenizationHandler;
import com.getcardflight.interfaces.OnCardKeyedListener;
import com.getcardflight.interfaces.OnFieldResetListener;
import com.getcardflight.interfaces.CardFlightDeviceHandler;
import com.getcardflight.interfaces.CardFlightAutoConfigHandler;
import com.getcardflight.models.CFEMVMessage;
import com.getcardflight.models.Card;
import com.getcardflight.models.CardFlight;
import com.getcardflight.models.CardFlightError;
import com.getcardflight.models.Charge;
import com.getcardflight.models.Reader;
import com.getcardflight.models.ReaderType;
import com.getcardflight.util.Constants;
import com.getcardflight.util.PermissionUtils;
import com.getcardflight.views.PaymentView;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class CDVCardFlight extends CordovaPlugin {

    public enum ReaderStatus {
        ATTACHED,
        CONNECTING,
        UPDATING,
        CONNECTED,
        WAITING_FOR_DIP,
        WAITING_FOR_SWIPE,
        REMOVE_CARD,
        DISCONNECTED,
        UNKNOWN
    }

    public CallbackContext onLowBatteryCallbackId;
    public CallbackContext onReaderResponseCallbackId;
    public CallbackContext onReaderDisconnectedCallbackId;
    public CallbackContext onReaderNotDetectedCallbackId;
    public CallbackContext onReaderConnectingCallbackId;
    public CallbackContext onCardSwipedCallbackId;
    public CallbackContext onEMVMessageCallbackId;
    public CallbackContext onEMVCardDippedCallbackId;
    public CallbackContext onEMVCardRemovedCallbackId;
    public CallbackContext onTransactionResultCallbackId;
    public CallbackContext onTokenizeCardCallbackId;

    public String readerState = "DISCONNECTED";
    public CFDeviceHandler deviceHandler = new CFDeviceHandler();

    private static final String TAG = "FROM COLLECT FOR STRIPE: ";

    @Override
    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {
        if (action.equals("setApiTokens")) {
            JSONObject options = inputs.optJSONObject(0);
            setApiTokens(options, callbackContext);
            return true;
        } else if (action.equals("SDKVersion")) {
            SDKVersion(callbackContext);
            return true;
        } else if (action.equals("apiToken")) {
            apiToken(callbackContext);
            return true;
        } else if (action.equals("accountToken")) {
            accountToken(callbackContext);
            return true;
        } else if (action.equals("readerState")) {
            readerState(callbackContext);
            return true;
        } else if (action.equals("registerOnReaderResponse")) {
            // registerOnReaderResponse(callbackContext);
            return true;
        } else if (action.equals("registerOnEMVMessage")) {
            // registerOnEMVMessage(callbackContext);
            return true;
        } else if (action.equals("registerOnEMVCardDipped")) {
            // registerOnEMVCardDipped(callbackContext);
            return true;
        } else if (action.equals("registerOnCardSwiped")) {
            // registerOnCardSwiped(callbackContext);
            return true;
        } else if (action.equals("registerOnEMVCardRemoved")) {
            // registerOnEMVCardRemoved(callbackContext);
            return true;
        } else if (action.equals("registerOnReaderAttached")) {
            deviceHandler.registerOnReaderAttached(callbackContext);
            return true;
        } else if (action.equals("registerOnReaderDisconnected")) {
            // registerOnReaderDisconnected(callbackContext);
            return true;
        } else if (action.equals("registerOnReaderNotDetected")) {
            // registerOnReaderNotDetected(callbackContext);
            return true;
        } else if (action.equals("registerOnReaderConnected")) {
            deviceHandler.registerOnReaderConnected(callbackContext);
            return true;
        } else if (action.equals("registerOnReaderConnecting")) {
            // registerOnReaderConnecting(callbackContext);
            return true;
        } else if (action.equals("registerOnTransactionResult")) {
            // registerOnTransactionResult(callbackContext);
            return true;
        } else if (action.equals("registerOnTokenizeCard")) {
            // registerOnTokenizeCard(callbackContext);
            return true;
        } else if (action.equals("registerOnLowBattery")) {
            // registerOnLowBattery(callbackContext);
            return true;
        } else {
            return false;
        }
    }

    private void setApiTokens(JSONObject options, final CallbackContext callbackContext) {
        String apiKey = options.has("apiKey") ? options.optString("apiKey") : null;
        String accountToken = options.has("accountToken") ? options.optString("accountToken") : null;
        String readerType = options.has("readerType") ? options.optString("readerType") : null;

        Context context = this.cordova.getActivity().getApplicationContext();

        CardFlight.getInstance()
            .setApiTokenAndAccountToken(apiKey, accountToken, null);
        Reader.getDefault(context)
            .setPreferredReader(ReaderType.A100_READER)
            .setDeviceHandler(deviceHandler);

        callbackContext.success();
    }

    // Get the current SDK Version
    private void SDKVersion(final CallbackContext callbackContext) {
        String version = CardFlight.getInstance().getVersion();
        callbackContext.success(version);
    }
    // Get the current apiToken
    private void apiToken(final CallbackContext callbackContext) {
        String apiToken = CardFlight.getInstance().getApiToken();
        callbackContext.success(apiToken);
    }

    // Get the current accountToken
    private void accountToken(final CallbackContext callbackContext) {
        String accountToken = CardFlight.getInstance().getAccountToken();
        callbackContext.success(accountToken);
    }

    // Get the state of the card reader
    // Returns string version of state to JS app
    public void readerState(final CallbackContext callbackContext) {
        callbackContext.success(readerState);
    }

    // Update Reader State
    public void UpdateReaderStatus(ReaderStatus status) {
        switch (status) {
            case UNKNOWN:
                readerState = "UNKNOWN";
                break;
            case WAITING_FOR_DIP:
                readerState = "WAITING_FOR_DIP";
                break;
            case WAITING_FOR_SWIPE:
                readerState = "WAITING_FOR_SWIPE";
                break;
            case ATTACHED:
                readerState = "ATTACHED";
                break;
            case CONNECTED:
                readerState = "CONNECTED";
                break;
            case DISCONNECTED:
                readerState = "DISCONNECTED";
                break;
            case REMOVE_CARD:
                readerState = "REMOVE_CARD";
                break;
        }
    }


    // Device handler, sends messages from reader to the app

    public class CFDeviceHandler implements CardFlightDeviceHandler {

        private static final String TAG = "FROM COLLECT FOR STRIPE: ";

        public CallbackContext onReaderAttachedCallbackId;
        public CallbackContext onReaderConnectedCallbackId;

        public CFDeviceHandler() {
            Log.d(TAG, "Init the thing");
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
            Log.d(TAG, "Reader connecting!");
            UpdateReaderStatus(ReaderStatus.ATTACHED);

            PluginResult pluginResult;
            pluginResult = new PluginResult(PluginResult.Status.OK);
            pluginResult.setKeepCallback(true);
            onReaderAttachedCallbackId.sendPluginResult(pluginResult);
        }

        @Deprecated
        @Override
        public void readerIsConnecting() {
            Log.d(TAG, "Reader connecting!");
        }

        @Override
        public void readerIsUpdating() {
            
        }

        @Override
        public void readerIsConnected(boolean isConnected, CardFlightError error) {
            Log.d(TAG, "Reader connected!");

            PluginResult pluginResult;
            UpdateReaderStatus(ReaderStatus.CONNECTED);

            if (error == null) {
                pluginResult = new PluginResult(PluginResult.Status.OK);
            } else {
                pluginResult = new PluginResult(PluginResult.Status.ERROR);
            }
            
            pluginResult.setKeepCallback(true);
            onReaderConnectedCallbackId.sendPluginResult(pluginResult);
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

        // Set callback ID to be a listener, reusable by the plugin.
        // After this is set, onReaderAttached will send results to onReaderAttachedCallbackId
        public void registerOnReaderAttached(final CallbackContext callbackContext) {
            Log.d(TAG, "register onReaderAttached!");
            onReaderAttachedCallbackId = callbackContext;
        }

        // Set callback ID to be a listener, reusable by the plugin.
        // After this is set, onReaderConnected will send results to onReaderConnectedCallbackId
        public void registerOnReaderConnected(final CallbackContext callbackContext) {
            Log.d(TAG, "register onReaderConnected!");
            onReaderConnectedCallbackId = callbackContext;
        }
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onReaderResponse will send results to onReaderResponseCallbackId
    // public void registerOnReaderResponse(final CallbackContext callbackContext) {
    //     onReaderResponseCallbackId = callbackContext;
    // }

    // // Set callback ID to be a listener, reusable by the plugin.
    // // After this is set, onEMVMessage will send results to onEMVMessageCallbackId
    // public void registerOnEMVMessage(final CallbackContext callbackContext) {
    //     onEMVMessageCallbackId = callbackContext;
    // }

    // // Set callback ID to be a listener, reusable by the plugin.
    // // After this is set, onEMVCardDipped will send results to onEMVCardDippedCallbackId
    // public void registerOnEMVCardDipped(final CallbackContext callbackContext) {
    //     onEMVCardDippedCallbackId = callbackContext;
    // }

    // // Set callback ID to be a listener, reusable by the plugin.
    // // After this is set, onCardSwiped will send results to onCardSwipedCallbackId
    // public void registerOnCardSwiped(final CallbackContext callbackContext) {
    //     onCardSwipedCallbackId = callbackContext;
    // }

    // // Set callback ID to be a listener, reusable by the plugin.
    // // After this is set, onEMVCardRemoved will send results to onEMVCardRemovedCallbackId
    // public void registerOnEMVCardRemoved(final CallbackContext callbackContext) {
    //     onEMVCardRemovedCallbackId = callbackContext;
    // }

    // // Set callback ID to be a listener, reusable by the plugin.
    // // After this is set, onReaderDisconnected will send results to onReaderDisconnectedCallbackId
    // public void registerOnReaderDisconnected(final CallbackContext callbackContext) {
    //     onReaderDisconnectedCallbackId = callbackContext;
    // }

    // // Set callback ID to be a listener, reusable by the plugin.
    // // After this is set, onReaderNotDetected will send results to onReaderNotDetectedCallbackId
    // public void registerOnReaderNotDetected(final CallbackContext callbackContext) {
    //     onReaderNotDetectedCallbackId = callbackContext;
    // }

    // // Set callback ID to be a listener, reusable by the plugin.
    // // After this is set, onReaderConnecting will send results to onReaderConnectingCallbackId
    // public void registerOnReaderConnecting(final CallbackContext callbackContext) {
    //     onReaderConnectingCallbackId = callbackContext;
    // }

    // // Set callback ID to be a listener, reusable by the plugin.
    // // After this is set, onTransactionResult will send results to onTransactionResultCallbackId
    // public void registerOnTransactionResult(final CallbackContext callbackContext) {
    //     onTransactionResultCallbackId = callbackContext;
    // }

    // // Set callback ID to be a listener, reusable by the plugin.
    // // After this is set, onTokenizeCard will send results to onTokenizeCardCallbackId
    // public void registerOnTokenizeCard(final CallbackContext callbackContext) {
    //     onTokenizeCardCallbackId = callbackContext;
    // }

    // // Set callback ID to be a listener, reusable by the plugin.
    // // After this is set, onLowBattery will send results to onLowBatteryCallbackId
    // public void registerOnLowBattery(final CallbackContext callbackContext) {
    //     onLowBatteryCallbackId = callbackContext;
    // }

}