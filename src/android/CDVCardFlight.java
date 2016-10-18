package com.odd.cardflight;

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

    @Override
    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {

        if (action.equals("setApiTokens")) {

            JSONObject options = inputs.optJSONObject(0);
            String message = "Still working";
            setApiTokens(options, callbackContext);

            return true;

        } else {
            
            return false;

        }
    }

    private void setApiTokens(JSONObject options, final CallbackContext callbackContext) {

        String apiKey = options.has("apiKey") ? options.optString("apiKey") : null;
        String accountToken = options.has("accountToken") ? options.optString("accountToken") : null;
        String readerType = options.has("readerType") ? options.optString("readerType") : null;

        CardFlight.getInstance();

        callbackContext.success();
    }
}
