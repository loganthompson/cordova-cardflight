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
      readerType: 3
    });
  }
});
````

In the SDK, this will run `- (void)setApiTokens` using given credentials. It also run's the reader's `init` method, or `initWithReader` if you choose to pass in the optional readerType argument. If you know the reader you'll be using, passing this value will speed up the process during initialization, but you can safely leave it out.

After success, the plugin returns the bool value 'emvReady' to your successCallback.
