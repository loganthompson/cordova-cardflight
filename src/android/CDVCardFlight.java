package org.apache.cordova.cardflight;

import android.util.Log;

import org.apache.cordova.CDVCardFlight;

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

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CDVCardFlight extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {
        PluginResult result = null;

        if (action.equals("setApiTokens")) {
            JSONObject options = inputs.optJSONObject(0);
            result = setApiTokens(options, callbackContext);
        }

        if(result != null) callbackContext.sendPluginResult( result );

        return true;
    }

    private PluginResult setApiTokens(JSONObject options, final CallbackContext callbackContext) {

        String apiKey = options.has("apiKey") ? options.optString("apiKey") : null;
        String accountToken = options.has("accountToken") ? options.optString("accountToken") : null;
        String readerType = options.has("readerType") ? options.optString("readerType") : null;

        CardFlight.getInstance().setApiTokenAndAccountToken(apiKey, accountToken, new CardFlightApiKeyAccountTokenHandler() {
            @Override
            public void onSuccess() {
                //CardFlight.setPreferredReader(readerType, true);
            }

            @Override
            public void onFailed(CardFlightError cardFlightError) {
                
            }
        });

        callbackContext.success();
        return null;
    }

    private PluginResult pluginResultKeep () {
        PluginResult pluginResult = new PluginResult (PluginResult.Status.OK);
        pluginResult.setKeepCallback (true);
        return pluginResult;
    }
     
    private PluginResult pluginResultKeep (JSONObject message) {
        PluginResult pluginResult = new PluginResult (PluginResult.Status.OK, message);
        pluginResult.setKeepCallback (true);
        return pluginResult;
    }
}