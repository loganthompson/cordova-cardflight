#CardFlight Cordova Plugin v1.0

This plugin allows direct interactions with the native CardFlight SDK through JavaScript functions in your Cordova app. This includes creating EMV, swipe and keyed credit card charges, among other features.

###CardFlight SDK Version 3.2

Install

    cordova plugin add https://github.com/loganthompson/cordova-cardflight.git

##Global Object

Access the `cardflight` globally, available after the document's `deviceReady` event.

##Getting Started

The callbacks for readerAttached, readerConnected and readerDisconnected will set themselves immediately, when the plugin receives Cordova's onCordovaReady event. On the iOS side, the reader itself is not initialized by the SDK until you set the API tokens.

Example:
```javascript
document.addEventListener('deviceready', function() {
  if (window.cordova) {
    // Initialize cardflight SDK
    cardflight.setApiTokens(successCallback, errorCallback, {
      apiKey: YOUR_CARDFLIGHT_API_KEY,
      accountToken: YOUR_CARDFLIGHT_ACCOUNT_TOKEN,
      readerType: TYPE_NUM
    });
  }
});
````

This will run the SDK's `setApiTokens` method using given credentials. It also run's the reader's `init` method, or `initWithReader` if you choose to pass in the optional readerType argument. If you know the reader you'll be using, passing this (number) value will speed up the process during initialization, but you can safely leave it out.

After success, the plugin returns the bool value 'emvReady' to your successCallback. The CardFlight SDK is now initialized and ready to continue.

## Methods

###setApiToken
Set API tokens to initalize CardFlight with apiKey and accountToken values
Initializes reader itself, and a success returns emvReady as BOOL
````javascript
CardFlight.prototype.setApiTokens = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "setApiTokens", [options]);
};
````

Prepare the reader for a swipe
````javascript
CardFlight.prototype.beginSwipe = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "beginSwipe", []);
};
````

Begin an EMV transaction with setup information,
including amount and optional description
````javascript
CardFlight.prototype.beginEMV = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "beginEMV", [options]);
};

Trigger keyed entry payment view. Will show a little more than 1/4 down
the screen, above cordova webview.
Enable zip by passing optional {zip:true}. Default bool value = NO
CardFlight.prototype.beginKeyed = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "beginKeyed", [options]);
};

Removes the keyed-entry payment view and hides keyboard
CardFlight.prototype.endKeyed = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "endKeyed", []);
};

Cancel the current transaction
CardFlight.prototype.cancelTransaction = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "cancelTransaction", []);
};

Run CardFlight destroy()
CardFlight.prototype.destroy = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "destroy", []);
};

Process a payment of any type.
Required arguments: amount, type ('emv', 'swipe' or 'keyed')
Optional argument: description
Transaction results of any type will be sent through registerOnTransactionResult callbacks.
CardFlight.prototype.processCharge = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "processCharge", [options]);
};

Upload signature PNG data if/when required.
Accepts a single argument 'data' as base64 encoded string.
CardFlight.prototype.uploadSignature = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "uploadSignature", [options]);
};

Set callback ID to be a listener, reusable by the plugin.
After this is set, onCardSwiped will send results to the callbacks passed here.
CardFlight.prototype.registerOnCardSwiped = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnCardSwiped", []);
};

Set callback ID to be a listener, reusable by the plugin.
After this is set, onEMVCardDipped will send results to the callbacks passed here.
CardFlight.prototype.registerOnEMVCardDipped = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnEMVCardDipped", []);
};

Set callback ID to be a listener, reusable by the plugin.
After this is set, onEMVCardRemoved will send results to the callbacks passed here.
CardFlight.prototype.registerOnEMVCardRemoved = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnEMVCardRemoved", []);
};

Set callback ID to be a listener, reusable by the plugin.
After this is set, onReaderResponse will send results to the callbacks passed here.
Receives updates for valid keyed card responses (will include card JSON with last4 and brand),
invalidKeyedResponse (no card), readerCardResponse (card JSON with name & last4),
readerSwipeDidCancel, and emvCardResponse (card JSON).
CardFlight.prototype.registerOnReaderResponse = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderResponse", []);
};

Set callback ID to be a listener, reusable by the plugin.
After this is set, onReaderAttached will send results to the callbacks passed here.
CardFlight.prototype.registerOnReaderAttached = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderAttached", []);
};

Set callback ID to be a listener, reusable by the plugin.
After this is set, onEMVMessage will send results to the callbacks passed here.
Success callback receives all EMV instruction/status updates during transaction,
which is useful for showing "processing" status, "Approved," "Remove Card" etc.
CardFlight.prototype.registerOnEMVMessage = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnEMVMessage", []);
};

Set callback ID to be a listener, reusable by the plugin.
After this is set, onReaderConnecting will send results to the callbacks passed here.
CardFlight.prototype.registerOnReaderConnecting = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderConnecting", []);
};

Set callback ID to be a listener, reusable by the plugin.
After this is set, onReaderConnected will send results to the callbacks passed here.
CardFlight.prototype.registerOnReaderConnected = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderConnected", []);
};

Set callback ID to be a listener, reusable by the plugin.
After this is set, onReaderDisconnected will send results to the callbacks passed here.
CardFlight.prototype.registerOnReaderDisconnected = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderDisconnected", []);
};

Set callback ID to be a listener, reusable by the plugin.
After this is set, onReaderNotDetected will send results to the callbacks passed here.
CardFlight.prototype.registerOnReaderNotDetected = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderNotDetected", []);
};

Set callback ID to be a listener, reusable by the plugin.
After this is set, onTransactionResult will send results to the callbacks passed here.
Receives updates for all types of transactions.
CardFlight.prototype.registerOnTransactionResult = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnTransactionResult", []);
};

Set callback ID to be a listener, reusable by the plugin.
After this is set, onLowBattery will send results to the callbacks passed here.
CardFlight.prototype.registerOnLowBattery = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnLowBattery", []);
};

