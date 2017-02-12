package com.odd.cardflight;

import android.Manifest;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import android.widget.FrameLayout;
import android.content.Context;
import android.app.AlertDialog;
import android.view.View;
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

    private static final String TAG = "FROM COLLECT FOR STRIPE: ";

    public CallbackContext onLowBatteryCallbackId;
    public CallbackContext onReaderDisconnectedCallbackId;
    public CallbackContext onReaderNotDetectedCallbackId;
    public CallbackContext onReaderAttachedCallbackId;
    public CallbackContext onReaderConnectedCallbackId;
    public CallbackContext onReaderConnectingCallbackId;
    public CallbackContext onReaderResponseCallbackId;
    public CallbackContext onCardSwipedCallbackId;
    public CallbackContext onEMVMessageCallbackId;
    public CallbackContext onEMVCardDippedCallbackId;
    public CallbackContext onEMVCardRemovedCallbackId;
    public CallbackContext onTransactionResultCallbackId;
    public CallbackContext onTokenizeCardCallbackId;

    private Card mCard = null;
    public String readerState = "DISCONNECTED";
    public ReaderType preferredReader = null;
    public CFDeviceHandler deviceHandler = new CFDeviceHandler();

    @Override
    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {
        JSONObject options = inputs.optJSONObject(0);

        if (action.equals("setApiTokens")) {
            setApiTokens(options, callbackContext);
            return true;
        } else if (action.equals("SDKVersion")) {
            SDKVersion(callbackContext);
            return true;
        } else if (action.equals("apiToken")) {
            apiToken(callbackContext);
            return true;
        } else if (action.equals("initReader")) {
            initReader(callbackContext);
            return true;
        } else if (action.equals("accountToken")) {
            accountToken(callbackContext);
            return true;
        } else if (action.equals("readerState")) {
            readerState(callbackContext);
            return true;
        } else if (action.equals("readerType")) {
            readerType(callbackContext);
            return true;
        } else if (action.equals("beginSwipe")) {
            beginSwipe(callbackContext);
            return true;
        } else if (action.equals("beginEMV")) {
            beginEMV(options, callbackContext);
            return true;
        } else if (action.equals("beginKeyed")) {
            beginKeyed(callbackContext);
            return true;
        } else if (action.equals("cancelTransaction")) {
            cancelTransaction(callbackContext);
            return true;
        } else if (action.equals("processCharge")) {
            processCharge(options);
            return true;
        } else if (action.equals("tokenizeCardWithSuccess")) {
            tokenizeCard();
            return true;
        } else if (action.equals("registerOnReaderResponse")) {
            registerOnReaderResponse(callbackContext);
            return true;
        } else if (action.equals("registerOnEMVMessage")) {
            registerOnEMVMessage(callbackContext);
            return true;
        } else if (action.equals("registerOnEMVCardDipped")) {
            registerOnEMVCardDipped(callbackContext);
            return true;
        } else if (action.equals("registerOnCardSwiped")) {
            registerOnCardSwiped(callbackContext);
            return true;
        } else if (action.equals("registerOnEMVCardRemoved")) {
            registerOnEMVCardRemoved(callbackContext);
            return true;
        } else if (action.equals("registerOnReaderAttached")) {
            registerOnReaderAttached(callbackContext);
            return true;
        } else if (action.equals("registerOnReaderDisconnected")) {
            registerOnReaderDisconnected(callbackContext);
            return true;
        } else if (action.equals("registerOnReaderNotDetected")) {
            // registerOnReaderNotDetected(callbackContext);
            return true;
        } else if (action.equals("registerOnReaderConnected")) {
            registerOnReaderConnected(callbackContext);
            return true;
        } else if (action.equals("registerOnReaderConnecting")) {
            registerOnReaderConnecting(callbackContext);
            return true;
        } else if (action.equals("registerOnTransactionResult")) {
            registerOnTransactionResult(callbackContext);
            return true;
        } else if (action.equals("registerOnTokenizeCard")) {
            registerOnTokenizeCard(callbackContext);
            return true;
        } else if (action.equals("registerOnLowBattery")) {
            registerOnLowBattery(callbackContext);
            return true;
        } else {
            return false;
        }
    }

    private void cancelTransaction(final CallbackContext callbackContext) {
        mCard = null;
        // mCharge = null;
        onEMVMessageCallbackId = null;
        onReaderResponseCallbackId = null;

        callbackContext.success();
    }

    private void setApiTokens(JSONObject options, final CallbackContext callbackContext) {
        Context context = this.cordova.getActivity().getApplicationContext();
        String apiKey = options.has("apiKey") ? options.optString("apiKey") : null;
        String accountToken = options.has("accountToken") ? options.optString("accountToken") : null;
        int givenType = options.optInt("readerType");

        CardFlight.getInstance()
            .setApiTokenAndAccountToken(apiKey, accountToken, null);

        if (givenType != 0) {
            switch (givenType) {
                case 2:
                    preferredReader = ReaderType.A100_READER;
                    break;
                case 3:
                    preferredReader = ReaderType.A200_READER;
                    break;
            }
        }

        Reader.getDefault(context)
            .setDeviceHandler(deviceHandler);

        if (preferredReader != null) {
            Reader.getDefault(context).setPreferredReader(preferredReader);   
        }

        callbackContext.success();
    }

    public void initReader(final CallbackContext callbackContext) {
        Log.d(TAG, "init reader");
        Context context = this.cordova.getActivity().getApplicationContext();
        Reader.getDefault(context)
            .setDeviceHandler(deviceHandler);

        if (preferredReader != null) {
            Reader.getDefault(context).setPreferredReader(preferredReader);   
        }

        if (callbackContext != null) {
            callbackContext.success();   
        }
    }

    // Return 'preferredReader', which is 
    // the current readerType
    public void readerType(final CallbackContext callbackContext) {
        Context context = this.cordova.getActivity().getApplicationContext();
        ReaderType currentType = ReaderType.values()[Reader.getDefault(context).getReaderType()];
        int typeInt = 0;

        if (preferredReader != currentType) {
            preferredReader = currentType;
        }
        
        // Set to int to match iOS
        switch (currentType) {
            case A100_READER:
                typeInt = 2;
                break;
            case A200_READER:
                typeInt = 3;
                break;
        }
        
        callbackContext.success(typeInt);
    }

    // Prepare the reader for a swipe
    private void beginSwipe(final CallbackContext callbackContext) {
        Log.d(TAG, "Begin swipe!");
        
        Context context = this.cordova.getActivity().getApplicationContext();
        UpdateReaderStatus(ReaderStatus.WAITING_FOR_SWIPE);

        Reader.getDefault(context).swipeHasTimeout(false).beginSwipe();

        callbackContext.success();
    }

    // Begin an EMV transaction with setup information, including
    // amount and optional metadata
    private void beginEMV(JSONObject options, final CallbackContext callbackContext) {
        Log.d(TAG, "BEGIN EMV");
        Context context = this.cordova.getActivity().getApplicationContext();

        // Get options and create metadata
        String description = options.optString("description");
        String currency = options.optString("currency");
        String stripeAccount = options.optString("stripeAccount");
        String amountString = options.optString("amount").replaceAll("[.]", "");
        String appFeeString = options.optString("applicationFee").replaceAll("[.]", "");
        JSONObject metadata = options.optJSONObject("metadata");
        int amount = Integer.valueOf(amountString);
        int appFee = Integer.valueOf(appFeeString);

        if (metadata == null) {
            metadata = new JSONObject();
        }
        if (stripeAccount != "") {
            try {
                metadata.put("application_fee", appFee);
                metadata.put("connected_stripe_account_id", stripeAccount);
            } catch (JSONException e) {
                e.printStackTrace();
            }   
        }

        HashMap <String, Object> chargeDetailsHash = new HashMap<String, Object>();
        chargeDetailsHash.put(Constants.REQUEST_KEY_AMOUNT, amount);
        chargeDetailsHash.put(Constants.REQUEST_KEY_META_DATA, metadata);
        chargeDetailsHash.put("description", description);
        chargeDetailsHash.put("currency", currency);

        Reader.getDefault(context).beginEMVTransaction(amount, chargeDetailsHash);
        UpdateReaderStatus(ReaderStatus.WAITING_FOR_DIP);

        callbackContext.success();
    }

    // Trigger keyed entry payment view. Will show a little more than 1/4 down on screen.
    // Enable zip by passing optional {zip:true}. Default bool value = NO
    private void beginKeyed(final CallbackContext callbackContext) {
        // Would create and display native PaymentView here
        callbackContext.success();
    }

    // Tokenize a card (Stripe only)
    private void tokenizeCard() {
        Context context = this.cordova.getActivity().getApplicationContext();

        if (mCard != null) {
            mCard.tokenize(
                context,
                new CardFlightTokenizationHandler() {
                    @Override
                    public void tokenizationSuccessful(String s) {
                        Log.d(TAG, "Tokenization Successful");
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, s);
                        pluginResult.setKeepCallback(true);
                        onTokenizeCardCallbackId.sendPluginResult(pluginResult);
                    }

                    @Override
                    public void tokenizationFailed(CardFlightError error) {
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, error.getMessage());
                        pluginResult.setKeepCallback(true);
                        onTokenizeCardCallbackId.sendPluginResult(pluginResult);
                    }
                });
        } else {
            onTokenizeCardCallbackId.error("No card found.");
        }
    }

    // Required arguments: amount, type ('emv', 'swipe' or 'keyed')
    // Optional argument: description
    public void processCharge(JSONObject options) {
        Log.d(TAG, "options " + options);
        Context context = this.cordova.getActivity().getApplicationContext();

        // Get options and create metadata
        String type = options.optString("type");
        String currency = options.optString("currency");
        String description = options.optString("description");
        String stripeAccount = options.optString("stripeAccount");
        String amountString = options.optString("amount").replaceAll("[.]", "");
        String appFeeString = options.optString("applicationFee").replaceAll("[.]", "");
        JSONObject metadata = options.optJSONObject("metadata");
        int amount = Integer.valueOf(amountString);
        int appFee = Integer.valueOf(appFeeString);

        if (metadata == null) {
            metadata = new JSONObject();
        }

        if (stripeAccount != "") {
            try {
                metadata.put("application_fee", appFee);
                metadata.put("connected_stripe_account_id", stripeAccount);
            } catch (JSONException e) {
                e.printStackTrace();
            }   
        }

        HashMap <String, Object> chargeDetailsHash = new HashMap<String, Object>();
        chargeDetailsHash.put(Constants.REQUEST_KEY_AMOUNT, amount);
        chargeDetailsHash.put(Constants.REQUEST_KEY_META_DATA, metadata);
        chargeDetailsHash.put("description", description);
        chargeDetailsHash.put("currency", currency);

        Log.d(TAG, currency);
        Log.d(TAG, type);

        if (type.equals("emv")) {
            Log.d(TAG, "Process emv");
            Reader.getDefault(context).emvProcessTransaction(true);
        } else if (mCard != null) {
            Log.d(TAG, "Process swipe");
            mCard.chargeCard(
                    context,
                    chargeDetailsHash,
                    new CardFlightPaymentHandler() {
                        @Override
                        public void transactionSuccessful(Charge charge) {
                            JSONObject resp = new JSONObject();
                            try {
                                resp.put("referenceID", charge.getReferenceId());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, resp);
                            pluginResult.setKeepCallback(true);
                            onTransactionResultCallbackId.sendPluginResult(pluginResult);
                        }

                        @Override
                        public void transactionFailed(CardFlightError error) {
                            Log.e(TAG, "error: " + error.getMessage());
                            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR);
                            pluginResult.setKeepCallback(true);
                            onTransactionResultCallbackId.sendPluginResult(pluginResult);
                        }
                    }
            );
        } else {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR);
            pluginResult.setKeepCallback(true);
            onTransactionResultCallbackId.sendPluginResult(pluginResult);
        }
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

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onReaderResponse will send results to onReaderResponseCallbackId
    public void registerOnReaderResponse(final CallbackContext callbackContext) {
        Log.d(TAG, "register onReaderResponse!");
        onReaderResponseCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onEMVMessage will send results to onEMVMessageCallbackId
    public void registerOnEMVMessage(final CallbackContext callbackContext) {
        onEMVMessageCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onEMVCardDipped will send results to onEMVCardDippedCallbackId
    public void registerOnEMVCardDipped(final CallbackContext callbackContext) {
        onEMVCardDippedCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onCardSwiped will send results to onCardSwipedCallbackId
    public void registerOnCardSwiped(final CallbackContext callbackContext) {
        onCardSwipedCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onEMVCardRemoved will send results to onEMVCardRemovedCallbackId
    public void registerOnEMVCardRemoved(final CallbackContext callbackContext) {
        Log.d(TAG, "register onEMVCardRemoved!");
        onEMVCardRemovedCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onTransactionResult will send results to onTransactionResultCallbackId
    public void registerOnTransactionResult(final CallbackContext callbackContext) {
        Log.d(TAG, "register onTransactionResult!");
        onTransactionResultCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onReaderDisconnected will send results to onReaderDisconnectedCallbackId
    public void registerOnReaderDisconnected(final CallbackContext callbackContext) {
        onReaderDisconnectedCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onReaderNotDetected will send results to onReaderNotDetectedCallbackId
    public void registerOnReaderNotDetected(final CallbackContext callbackContext) {
        onReaderNotDetectedCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onReaderConnecting will send results to onReaderConnectingCallbackId
    public void registerOnReaderConnecting(final CallbackContext callbackContext) {
        onReaderConnectingCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onTokenizeCard will send results to onTokenizeCardCallbackId
    public void registerOnTokenizeCard(final CallbackContext callbackContext) {
        onTokenizeCardCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onLowBattery will send results to onLowBatteryCallbackId
    public void registerOnLowBattery(final CallbackContext callbackContext) {
        onLowBatteryCallbackId = callbackContext;
    }


    // ************************************************************
    // Device handler, sends messages from reader to the app
    // ************************************************************
    public class CFDeviceHandler implements CardFlightDeviceHandler {

        public CFDeviceHandler() {
            // Log.d(TAG, "Init the thing");
            // requestMicrophoneAccess();
        }

        @Override
        public void emvTransactionResult(Charge charge, boolean requiresSignature, CFEMVMessage message) {
            PluginResult pluginResult;

            Log.d(TAG, "emv transaction result");
            Log.d(TAG, String.valueOf(charge));

            if (charge == null) {
                pluginResult = new PluginResult(PluginResult.Status.ERROR, message.getMessage());
            } else {
                JSONObject resp = new JSONObject();
                try {
                    resp.put("referenceID", charge.getReferenceId());
                    resp.put("signature", requiresSignature);
                } catch (JSONException e) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, String.valueOf(e));
                }
                pluginResult = new PluginResult(PluginResult.Status.OK, resp);
            }
            pluginResult.setKeepCallback(true);
            onTransactionResultCallbackId.sendPluginResult(pluginResult); 
        }

        @Override
        public void emvRequestApplicationSelection(ArrayList appList) {
            
            // Implement method to select which AID,
            // then call - Reader.getDefault(mContext).emvSelectApplication(aidIndex);
        }

        @Override
        public void emvMessage(CFEMVMessage message) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, message.getMessage());
            pluginResult.setKeepCallback(true);
            if (onEMVMessageCallbackId != null) {
                Log.d(TAG, "Sending emv message to web");
                onEMVMessageCallbackId.sendPluginResult(pluginResult);
            }
        }

        @Override
        public void emvCardResponse(HashMap<String, Object> hashMap) {
            Log.d(TAG, "emv card received");

            JSONObject cardDict = new JSONObject();
            String cardType = (String) hashMap.get(Constants.CARD_TYPE);
            String firstSix = (String) hashMap.get(Constants.FIRST_SIX);
            String lastFour = (String) hashMap.get(Constants.LAST_FOUR);
            try {
                cardDict.put("type", "emv");
                cardDict.put("first6", firstSix);
                cardDict.put("last4", lastFour);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, cardDict);
            pluginResult.setKeepCallback(true);
            onReaderResponseCallbackId.sendPluginResult(pluginResult);
        }

        @Override
        public void emvErrorResponse(CardFlightError error) {
            Log.d(TAG, "emv error response: " + error.getMessage());
            // PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            // pluginResult.setKeepCallback(true);
            // onEMVErrorRespones.sendPluginResult(pluginResult);
        }

        @Override
        public void emvAmountRequested() {
            // prompt for amount - doesn't happen if amount properly set
        }

        @Override
        public void readerBatteryLow() {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            pluginResult.setKeepCallback(true);
            onLowBatteryCallbackId.sendPluginResult(pluginResult);
        }

        @Override
        public void emvCardDipped() {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            pluginResult.setKeepCallback(true);
            onEMVCardDippedCallbackId.sendPluginResult(pluginResult);
        }

        @Override
        public void emvCardRemoved() {
            Log.d(TAG, "Card removed");
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            pluginResult.setKeepCallback(true);
            onEMVCardRemovedCallbackId.sendPluginResult(pluginResult);
        }

        @Override
        public void emvTransactionVaultID(String s) {
        
        }

        @Override
        public void readerCardResponse(Card card, CardFlightError error) {
            PluginResult pluginResult;

            if (error == null) {
                Log.d(TAG, "Got a card!");
                mCard = card;
                JSONObject cardObject = new JSONObject();
                try {
                    cardObject.put("type", "swipe");
                    cardObject.put("name", card.getName());
                    cardObject.put("last4", card.getLast4());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pluginResult = new PluginResult(PluginResult.Status.OK, cardObject);
            } else {
                mCard = null;
                pluginResult = new PluginResult(PluginResult.Status.ERROR, error.getMessage());
            }

            if (onReaderResponseCallbackId != null) {
                pluginResult.setKeepCallback(true);
                onReaderResponseCallbackId.sendPluginResult(pluginResult);   
            }
        }

        @Override
        public void readerIsAttached() {
            // Deprecated - This method is a duplicate of readerIsConnecting as of version 3.1 and will be
            // removed in a future release
            Log.d(TAG, "Reader attached!");
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
            UpdateReaderStatus(ReaderStatus.ATTACHED);

            PluginResult pluginResult;
            pluginResult = new PluginResult(PluginResult.Status.OK);
            pluginResult.setKeepCallback(true);
            onReaderAttachedCallbackId.sendPluginResult(pluginResult);
        }

        @Override
        public void readerIsUpdating() {
            Log.d(TAG, "Reader updating");   
        }

        @Override
        public void readerIsConnected(boolean isConnected, CardFlightError error) {
            Log.d(TAG, "Reader connected!");

            PluginResult pluginResult;
            UpdateReaderStatus(ReaderStatus.CONNECTED);

            if (error == null) {
                pluginResult = new PluginResult(PluginResult.Status.OK);
            } else {
                Log.d(TAG, "connect error");
                pluginResult = new PluginResult(PluginResult.Status.ERROR);
            }
            
            pluginResult.setKeepCallback(true);
            onReaderConnectedCallbackId.sendPluginResult(pluginResult);
        }

        @Override
        public void readerSwipeDetected() {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            pluginResult.setKeepCallback(true);
            onCardSwipedCallbackId.sendPluginResult(pluginResult);
        }

        @Override
        public void readerIsDisconnected() {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            UpdateReaderStatus(ReaderStatus.DISCONNECTED);
            pluginResult.setKeepCallback(true);
            onReaderDisconnectedCallbackId.sendPluginResult(pluginResult);
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
}