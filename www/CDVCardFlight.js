var argscheck = require('cordova/argscheck'),
  channel = require('cordova/channel'),
  utils = require('cordova/utils'),
  exec = require('cordova/exec'),
  cordova = require('cordova');

channel.createSticky('onCordovaCardFlightReady');
channel.waitForInitialization('onCordovaCardFlightReady');

var CFATTACHED = false;
var CFCONNECTED = false;

function CardFlight() {
  this.cordova = null;
  this.config = null;

  var self = this;

  channel.onCordovaReady.subscribe(function() {
    self.initialize();
  });
}

// Call cardflight.isConnected or cardflight.isAttached
// anytime to check the physical card reader status

CardFlight.prototype.isAttached = function() {
  return CFATTACHED;
}

CardFlight.prototype.isConnected = function() {
  return CFCONNECTED;
}

// Sets onReaderAttached & Connected callbacks to update whenever
// the reader is plugged in / connected

CardFlight.prototype.initialize = function() {
  var errorCallback = function() {
    console.log("callback failure");
  }
  this.registerOnReaderAttached(function(){
    CFATTACHED = true;
  }, errorCallback);
  this.registerOnReaderDisconnected(function(){
    CFCONNECTED = false;
    cordova.fireDocumentEvent('CFReaderPlugged');
  }, errorCallback);
  this.registerOnReaderConnected(function(){
    CFCONNECTED = true;
    cordova.fireDocumentEvent('CFReaderPlugged');
  }, errorCallback);

  channel.onCordovaCardFlightReady.fire();
}

// Set API tokens to initalize CardFlight with apiKey and accountToken values
// Initializes reader itself, and a success returns emvReady as BOOL
CardFlight.prototype.setApiTokens = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "setApiTokens", [options]);
};

// Initialize the reader itself separately from setApiTokens
CardFlight.prototype.initReader = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "initReader", [options]);
};

// Prepare the reader for a swipe
CardFlight.prototype.beginSwipe = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "beginSwipe", []);
};

// Begin an EMV transaction with setup information,
// including amount and optional description
CardFlight.prototype.beginEMV = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "beginEMV", [options]);
};

// Trigger keyed entry payment view. Will show a little more than 1/4 down
// the screen, above cordova webview.
// Enable zip by passing optional {zip:true}. Default bool value = NO
CardFlight.prototype.beginKeyed = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "beginKeyed", [options]);
};

// Removes the keyed-entry payment view and hides keyboard
CardFlight.prototype.endKeyed = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "endKeyed", []);
};

// Cancel the current transaction
CardFlight.prototype.cancelTransaction = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "cancelTransaction", []);
};

// Run CardFlight destroy()
CardFlight.prototype.destroy = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "destroy", []);
};

// Process a payment of any type.
// Required arguments: amount, type ('emv', 'swipe' or 'keyed')
// Optional argument: description
// Transaction results of any type will be sent through registerOnTransactionResult callbacks.
CardFlight.prototype.processCharge = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "processCharge", [options]);
};

// Upload signature PNG data if/when required.
// Accepts a single argument 'data' as base64 encoded string.
CardFlight.prototype.uploadSignature = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "uploadSignature", [options]);
};

// Refund charge using the charge ID
CardFlight.prototype.refundCharge = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "refundCharge", [options]);
};

// Returns readerState as a string
// e.g. "WAITING_FOR_CONNECT", "REMOVE_CARD"
CardFlight.prototype.readerState = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "readerState", []);
};

CardFlight.prototype.SDKVersion = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "SDKVersion", []);
};

CardFlight.prototype.apiToken = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "apiToken", []);
};

CardFlight.prototype.accountToken = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "accountToken", []);
};

// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onCardSwiped will send results to the callbacks passed here.
CardFlight.prototype.registerOnCardSwiped = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnCardSwiped", []);
};

// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onEMVCardDipped will send results to the callbacks passed here.
CardFlight.prototype.registerOnEMVCardDipped = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnEMVCardDipped", []);
};

// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onEMVCardRemoved will send results to the callbacks passed here.
CardFlight.prototype.registerOnEMVCardRemoved = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnEMVCardRemoved", []);
};

// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onReaderResponse will send results to the callbacks passed here.
// Receives updates for valid keyed card responses (will include card JSON with last4 and brand),
// invalidKeyedResponse (no card), readerCardResponse (card JSON with name & last4),
// readerSwipeDidCancel, and emvCardResponse (card JSON).
CardFlight.prototype.registerOnReaderResponse = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderResponse", []);
};

// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onReaderAttached will send results to the callbacks passed here.
CardFlight.prototype.registerOnReaderAttached = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderAttached", []);
};

// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onEMVMessage will send results to the callbacks passed here.
// Success callback receives all EMV instruction/status updates during transaction,
// which is useful for showing "processing" status, "Approved," "Remove Card" etc.
CardFlight.prototype.registerOnEMVMessage = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnEMVMessage", []);
};

// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onReaderConnecting will send results to the callbacks passed here.
CardFlight.prototype.registerOnReaderConnecting = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderConnecting", []);
};

// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onReaderConnected will send results to the callbacks passed here.
CardFlight.prototype.registerOnReaderConnected = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderConnected", []);
};

// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onReaderDisconnected will send results to the callbacks passed here.
CardFlight.prototype.registerOnReaderDisconnected = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderDisconnected", []);
};

// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onReaderNotDetected will send results to the callbacks passed here.
CardFlight.prototype.registerOnReaderNotDetected = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderNotDetected", []);
};

// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onReaderConnecting will send results to the callbacks passed here.
CardFlight.prototype.registerOnReaderConnecting = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderConnecting", []);
};

// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onTransactionResult will send results to the callbacks passed here.
// Receives updates for all types of transactions.
CardFlight.prototype.registerOnTransactionResult = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnTransactionResult", []);
};

// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onLowBattery will send results to the callbacks passed here.
CardFlight.prototype.registerOnLowBattery = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnLowBattery", []);
};

module.exports = new CardFlight();