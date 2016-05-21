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
  this.available = false;
  this.platform = null;
  this.cordova = null;
  this.config = null;

  var _this = this;

  channel.onCordovaReady.subscribe(function() {
    _this.initialize();
  });
}

CardFlight.prototype.configure = function(options) {
  var successCallback = function() {
    console.log("SUCCESSFULLY SET TOKENS");
  };
  var errorCallback = function() {
    console.log("ERROR SETTING TOKENS");
}
  this.setApiTokens(successCallback, errorCallback, options);
}

CardFlight.prototype.isAttached = function() {
  return CFATTACHED;
}

CardFlight.prototype.isConnected = function() {
  return CFCONNECTED;
}

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
  this.registerOnEMVMessage(function(msg){
    window.alert(msg, 'Instruction');
  }, errorCallback);
  this.registerOnReaderResponse(function(last4){
    window.alert(last4, 'Card Success');
  }, errorCallback);

  channel.onCordovaCardFlightReady.fire();

}


CardFlight.prototype.setApiTokens = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "setApiTokens", [options.apiToken, options.accountToken]);
};

CardFlight.prototype.beginEMV = function(successCallback, errorCallback, options) {
  exec(successCallback, errorCallback, "CDVCardFlight", "beginEMV", [options.amount, options.details]);
};

CardFlight.prototype.beginKeyed = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "beginKeyed", []);
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

CardFlight.prototype.registerOnReaderConnecting = function(successCallback, errorCallback) {
  exec(successCallback, errorCallback, "CDVCardFlight", "registerOnReaderConnecting", []);
};

module.exports = new CardFlight();