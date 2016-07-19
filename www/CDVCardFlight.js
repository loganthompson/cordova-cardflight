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

CardFlight.prototype.setApiTokens = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "setApiTokens", [options]);
};

CardFlight.prototype.beginSwipe = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "beginSwipe", []);
};

CardFlight.prototype.beginEMV = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "beginEMV", [options]);
};

CardFlight.prototype.cancelTransaction = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "cancelTransaction", []);
};

CardFlight.prototype.destroy = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "destroy", []);
};

CardFlight.prototype.processCharge = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "processCharge", [options]);
};

CardFlight.prototype.uploadSignature = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "uploadSignature", [options]);
};

CardFlight.prototype.beginKeyed = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "beginKeyed", [options]);
};

CardFlight.prototype.endKeyed = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "endKeyed", []);
};

CardFlight.prototype.registerOnCardSwiped = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnCardSwiped", []);
};

CardFlight.prototype.registerOnEMVCardDipped = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnEMVCardDipped", []);
};

CardFlight.prototype.registerOnEMVCardRemoved = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnEMVCardRemoved", []);
};

CardFlight.prototype.registerOnReaderResponse = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderResponse", []);
};

CardFlight.prototype.registerOnReaderAttached = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderAttached", []);
};

CardFlight.prototype.registerOnEMVMessage = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnEMVMessage", []);
};

CardFlight.prototype.registerOnReaderConnected = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderConnected", []);
};

CardFlight.prototype.registerOnReaderDisconnected = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderDisconnected", []);
};

CardFlight.prototype.registerOnReaderNotDetected = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderNotDetected", []);
};

CardFlight.prototype.registerOnReaderConnecting = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderConnecting", []);
};

CardFlight.prototype.registerOnTransactionResult = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnTransactionResult", []);
};

CardFlight.prototype.registerOnLowBattery = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnLowBattery", []);
};

module.exports = new CardFlight();