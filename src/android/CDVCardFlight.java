package com.odd.cardflight;

import android.util.Log;

import com.getcardflight.interfaces.CardFlightApiKeyAccountTokenHandler;
import com.getcardflight.interfaces.CardFlightAuthHandler;
import com.getcardflight.interfaces.CardFlightCaptureHandler;
import com.getcardflight.interfaces.CardFlightDecryptHandler;
import com.getcardflight.interfaces.CardFlightPaymentHandler;
import com.getcardflight.interfaces.CardFlightTokenizationHandler;
import com.getcardflight.interfaces.OnCardKeyedListener;
import com.getcardflight.interfaces.OnFieldResetListener;
import com.getcardflight.models.Card;
import com.getcardflight.models.CardFlight;
import com.getcardflight.models.CardFlightError;
import com.getcardflight.models.Charge;
import com.getcardflight.models.Reader;
import com.getcardflight.util.Constants;
import com.getcardflight.util.PermissionUtils;
import com.getcardflight.views.PaymentView;

import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class CDVCardFlight extends CordovaPlugin {

    private static CallbackContext onLowBatteryCallbackId;
    private static CallbackContext onReaderResponseCallbackId;
    private static CallbackContext onReaderAttachedCallbackId;
    private static CallbackContext onReaderConnectedCallbackId;
    private static CallbackContext onReaderDisconnectedCallbackId;
    private static CallbackContext onReaderNotDetectedCallbackId;
    private static CallbackContext onReaderConnectingCallbackId;
    private static CallbackContext onCardSwipedCallbackId;
    private static CallbackContext onEMVMessageCallbackId;
    private static CallbackContext onEMVCardDippedCallbackId;
    private static CallbackContext onEMVCardRemovedCallbackId;
    private static CallbackContext onTransactionResultCallbackId;
    private static CallbackContext onTokenizeCardCallbackId;

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
        } else {
            return false;
        }
    }

    private void setApiTokens(JSONObject options, final CallbackContext callbackContext) {

        String apiKey = options.has("apiKey") ? options.optString("apiKey") : null;
        String accountToken = options.has("accountToken") ? options.optString("accountToken") : null;
        String readerType = options.has("readerType") ? options.optString("readerType") : null;

        CardFlight.getInstance().setApiTokenAndAccountToken(apiKey, accountToken, null);

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

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onReaderResponse will send results to onReaderResponseCallbackId
    private void registerOnReaderResponse(final CallbackContext callbackContext) {
        onReaderResponseCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onEMVMessage will send results to onEMVMessageCallbackId
    private void registerOnEMVMessage(final CallbackContext callbackContext) {
        onEMVMessageCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onEMVCardDipped will send results to onEMVCardDippedCallbackId
    private void registerOnEMVCardDipped(final CallbackContext callbackContext) {
        onEMVCardDippedCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onCardSwiped will send results to onCardSwipedCallbackId
    private void registerOnCardSwiped(final CallbackContext callbackContext) {
        onCardSwipedCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onEMVCardRemoved will send results to onEMVCardRemovedCallbackId
    private void registerOnEMVCardRemoved(final CallbackContext callbackContext) {
        onEMVCardRemovedCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onReaderAttached will send results to onReaderAttachedCallbackId
    private void registerOnReaderAttached(final CallbackContext callbackContext) {
        onReaderAttachedCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onReaderDisconnected will send results to onReaderDisconnectedCallbackId
    private void registerOnReaderDisconnected(final CallbackContext callbackContext) {
        onReaderDisconnectedCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onReaderNotDetected will send results to onReaderNotDetectedCallbackId
    private void registerOnReaderNotDetected(final CallbackContext callbackContext) {
        onReaderNotDetectedCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onReaderConnected will send results to onReaderConnectedCallbackId
    private void registerOnReaderConnected(final CallbackContext callbackContext) {
        onReaderConnectedCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onReaderConnecting will send results to onReaderConnectingCallbackId
    private void registerOnReaderConnecting(final CallbackContext callbackContext) {
        onReaderConnectingCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onTransactionResult will send results to onTransactionResultCallbackId
    private void registerOnTransactionResult(final CallbackContext callbackContext) {
        onTransactionResultCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onTokenizeCard will send results to onTokenizeCardCallbackId
    private void registerOnTokenizeCard(final CallbackContext callbackContext) {
        onTokenizeCardCallbackId = callbackContext;
    }

    // Set callback ID to be a listener, reusable by the plugin.
    // After this is set, onLowBattery will send results to onLowBatteryCallbackId
    private void registerOnLowBattery(final CallbackContext callbackContext) {
        onLowBatteryCallbackId = callbackContext;
    }

}
