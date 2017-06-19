# CardFlight Cordova Plugin v 2.0

This plugin allows direct interactions with the native CardFlight SDK through JavaScript functions in your Cordova app. This includes creating EMV, swipe and keyed credit card charges, among other features.

### CardFlight SDK Version 3.5.1
[SDK Documentation](https://developers.cardflight.com/docs/api/) includes tips for the order in which to create charges, and other information useful to implementing this plugin.

## LICENSE:

Please review the LICENSE file before proceeding. Copyright 2016 – 2017.

## New in 2.0
- Dynamic metadata! Just pass a 'metadata' object with other charge params
- Adds Android support
- _Note: Android version does not yet include keyed entry view._

## New in 1.1
- May now pass 'currency' variable upon charge creation
- Can get readerState through plugin (string value)
- Can initalize reader separately from setApiKeys function if needed
- Adds refund method
- Adds more callbacks (Reader Connecting state)
- Adds support for info callbacks (tokens, sdk version)

## Install

    cordova plugin add https://github.com/loganthompson/cordova-cardflight.git

## Global Object

Access the `cardflight` globally, available after the document's `deviceReady` event.

## Getting Started

The callbacks for readerAttached, readerConnected and readerDisconnected will set themselves immediately, when the plugin receives Cordova's onCordovaReady event. On the native side, the reader itself is not initialized by the SDK until you set the API tokens.

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

### cardflight.setApiTokens()

Set API tokens to initalize CardFlight with apiKey and accountToken values. Requires `apiKey` and `accountToken` arguments. `readerType` argument is optional.

This method will also nitialize reader itself, and a success returns `emvReady` as a BOOL
````javascript
cardflight.setApiTokens(successCallback, errorCallback, {
    apiKey: YOUR_CARDFLIGHT_API_KEY
    accountToken: YOUR_CARDFLIGHT_ACCOUNT_TOKEN,
    readerType: READER_TYPE // optional
});
````

-----

### cardflight.beginSwipe()

Prepare the reader for a credit card swipe
````javascript
cardflight.beginSwipe(successCallback, errorCallback);
````

-----

### cardflight.beginEMV()

Begin an EMV transaction with setup information, including amount and optional description
````javascript
cardflight.beginEMV(successCallback, errorCallback, {
    amount: CHARGE_AMOUNT, // String value with decimal e.g. '5.00' or '.50'
    description: CHARGE_DESCRIPTION // optional
});
````

-----

### cardflight.beginKeyed()

Trigger keyed entry and show input view. View will appear a little more than 1/4 down the screen, above cordova webview. Enable zip by passing optional {zip:true}. Default bool value = NO
````javascript
cardflight.beginKeyed(successCallback, errorCallback, {
    zip: BOOL // optional
});
````

-----

### cardflight.endKeyed()

Removes the keyed-entry payment view and hides keyboard
````javascript
cardflight.endKeyed(successCallback, errorCallback);
````

-----

### cardflight.cancelTransaction()

Cancel the current transaction
````javascript
cardflight.cancelTransaction(successCallback, errorCallback);
````

-----

### cardflight.destroy()

Run CardFlight's destroy method
````javascript
cardflight.destroy(successCallback, errorCallback);
````

-----

### cardflight.processCharge()

Process a payment of any type.
*Required arguments: amount, type ('emv', 'swipe' or 'keyed')
Optional argument: description

*For emv charges, you can leave off amount and description, as they won't be used, but it is good practice to process charges with all available arguments.

In the case of an emv Transaction results of any type will be sent through registerOnTransactionResult callbacks.
````javascript
cardflight.processCharge(successCallback, errorCallback, {
    type: CHARGE_TYPE,
    amount: CHARGE_AMOUNT,
    description: CHARGE_DESCRIPTION // optional
});
````

-----

### cardflight.uploadSignature()

Upload signature PNG data if/when required. Accepts a single argument `data` as base64 encoded string.
````javascript
cardflight.uploadSignature(successCallback, errorCallback, {
    data: DATA // base64 encoded string of png signature image
});
````

##Register Listener Callbacks

The above methods return a callback immediately, the `id` of which does not need to last. But for listeners that persist and need to be ready for an event to fire in the future, we have to register those callbacks. Use the following methods to register (or re-register) callbacks for a given event.

-----

### cardflight.registerOnCardSwiped()

Set listener on card swipe event (not the same as card or transaction responses; includes no data).
````javascript
cardflight.registerOnCardSwiped(successCallback, errorCallback);
````

-----

### cardflight.registerOnEMVCardDipped()

Set listener to detect when an EMV card is physically dipped.
````javascript
cardflight.registerOnEMVCardDipped(successCallback, errorCallback);
````

-----

### cardflight.registerOnEMVCardRemoved()

Set listener to detect when an EMV card is physically removed.
````javascript
cardflight.registerOnEMVCardRemoved(successCallback, errorCallback);
````

-----

### cardflight.registerOnReaderResponse()

Set listener to receive updates for valid keyed card responses (will include card JSON with last4 and brand), invalidKeyedResponse (no card), readerCardResponse (card JSON with name & last4), readerSwipeDidCancel, and emvCardResponse (card JSON).

To handle transaction results of different types in different ways, call `registerOnReaderResponse` and pass a new successCallback whenever you change transactions (from keyed to swipe, for example).

````javascript
cardflight.registerOnReaderResponse(successCallback(card), errorCallback);
````

-----

### cardflight.registerOnReaderAttached()

Set listener for the event fired when a card reader is physically attached via the headphone jack.
````javascript
cardflight.registerOnReaderAttached(successCallback, errorCallback);
````

-----

### cardflight.registerOnEMVMessage()

Set a listener to receive all EMV instruction/status updates during transaction, which is useful for showing "processing" status, "Approved," "Remove Card" etc.

Returns string containing the message.
````javascript
cardflight.registerOnEMVMessage(successCallback(message), errorCallback);
````

-----

### cardflight.registerOnReaderConnecting()

Set a listener for the physical reader's `connecting` state (physically attached but not yet connected).
````javascript
cardflight.registerOnReaderConnecting(successCallback, errorCallback);
````

-----

### cardflight.registerOnReaderConnected()

Set a listener for the physical reader's `connected` state (physically attached and connected).
````javascript
cardflight.registerOnReaderConnected(successCallback, errorCallback);
````

-----

### cardflight.registerOnReaderDisconnected()

Set a listener for the physical reader's `disconnected` state (fired when unplugged).
````javascript
cardflight.registerOnReaderDisconnected(successCallback, errorCallback);
````

-----

### cardflight.registerOnReaderNotDetected()

Set a listener for when a connection attempt has been made but no reader was detected.
````javascript
cardflight.registerOnReaderNotDetected(successCallback, errorCallback);
````

-----

### cardflight.registerOnTransactionResult()

Set a listener for transaction results of all types. If the charge was successful and was of type emv, the callback will receive a BOOL value for `signatureRequired`.
````javascript
cardflight.registerOnTransactionResult(successCallback(signatureRequired), errorCallback);
````

-----

### cardflight.registerOnLowBattery()
Set listener for reader's low battery event.
````javascript
cardflight.registerOnLowBattery(successCallback, errorCallback);
````

## Tips
- To begin an EMV charge you'll have to set the charge `amount` and `description` before the credit card is ever used. This differs from a swiped transaction, in which you'll get card information upfront and then send these arguments after the fact.

- Watch the logs in Xcode while debugging (not just the browser).


## Supported Platforms
- iOS

_Android in progress_
