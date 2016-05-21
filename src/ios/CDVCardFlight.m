#import "CDVCardFlight.h"
#import <Cordova/CDV.h>

@interface CDVCardFlight ()
@property (nonatomic) CFTReader *reader;
@property (nonatomic) CFTCard *card;
@property (nonatomic) CDVPluginResult *readerPluginResult;
@property (nonatomic) NSString *onReaderResponseCallbackId;
@property (nonatomic) NSString *onReaderAttachedCallbackId;
@property (nonatomic) NSString *onReaderConnectedCallbackId;
@property (nonatomic) NSString *onReaderDisconnectedCallbackId;
@property (nonatomic) NSString *onReaderConnectingCallbackId;
@end

@implementation CDVCardFlight

- (void)setApiTokens:(CDVInvokedUrlCommand*)command {
  NSString* apiToken = [command.arguments objectAtIndex:0];
  NSString* accountToken = [command.arguments objectAtIndex:1];
  CDVPluginResult* pluginResult = nil;

  [[CFTSessionManager sharedInstance] setApiToken:apiToken accountToken:accountToken completed:^(BOOL emvReady) {
      NSLog(@"%@: %@ %@: %@", @"API TOKEN",apiToken,@" ACCOUNT TOKEN %@",accountToken);
  }];
  
  _reader = [[CFTReader alloc] initWithReader:3];
  if (_reader) {
    [_reader setDelegate:self];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
  } else {
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
  }
  
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)swipeCard:(CDVInvokedUrlCommand*)command {
  [_reader beginSwipe];
  
  // Here wait for the cardResponse to complete via block
  
  __weak CDVCardFlight *weakSelf = self;
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
//  readerDone = ^{
//      [weakSelf.commandDelegate sendPluginResult:weakSelf.readerPluginResult
//                                      callbackId:command.callbackId];
//      NSLog(@"READER DONE");
//      NSLog(@"READER CALLBACKID %@\n", command.callbackId);
//      NSLog(@"READER RESULT %@\n", weakSelf.readerPluginResult);
//  };
  weakSelf = nil;
}

- (void)readerSwipeDetected:(CDVInvokedUrlCommand*)command {
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)readerCardResponse:(CFTCard *)card withError:(NSError *)error {
 if (error) {
     UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"CardFlight" message:error.localizedDescription delegate:self 
                                           cancelButtonTitle:@"Okay" otherButtonTitles:nil];
     [alert show];
 } else {
   _card = card;
   NSLog(@"IN RESPONSE %@", _card.name);
   NSLog(@"CallbackId %@", self.onReaderResponseCallbackId);
   _readerPluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                           messageAsDictionary:@{@"name": _card.name, @"last4": _card.last4}];
   [self.commandDelegate runInBackground:^{
       [self.commandDelegate sendPluginResult:_readerPluginResult callbackId:self.onReaderResponseCallbackId];
   }];
     
   // [_card tokenizeCardWithSuccess:^{
   //     NSLog(@"Card Token:  %@\n", _card.cardToken);
   //     _readerPluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
   //                             messageAsDictionary:@{@"cardToken": _card.cardToken}];
   //       // Callback to the block in the swipeCard method
   //       readerDone();
   //  }
   //  failure:^(NSError *error){
   //      NSLog(@"ERROR CODE: %i", error.code);
   //      _readerPluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
   //                                              messageAsString:error.localizedDescription];
   //     // Callback to the block in the swipeCard method
   //     readerDone();
   //  }];
 }
}

//Response after manual entry
-(void)manualEntryDictionary:(NSDictionary *)dictionary
{
}

//Server response after submitting data
-(void)serverResponse:(NSData *)response andError:(NSError *)error {
  //Manage the CardFlight API server response
  NSDictionary *jsonDict = [NSJSONSerialization JSONObjectWithData:response options:NSJSONReadingMutableContainers error:&error];
  NSLog(@"Server Response: %@", jsonDict);
}

- (void)readerIsAttached {
  NSLog(@"called readerIsAttached");
  NSLog(@"CallbackId %@", self.onReaderAttachedCallbackId);
  // fire corresponding callback id for onReaderAttached
  if (self.onReaderAttachedCallbackId) {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [result setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.onReaderAttachedCallbackId];
  }
}

- (void)readerIsDisconnected {
  NSLog(@"called readerIsDisconnected");
  NSLog(@"CallbackId %@", self.onReaderDisconnectedCallbackId);
  // fire corresponding callback id for onReaderAttached
  if (self.onReaderDisconnectedCallbackId) {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:self.onReaderDisconnectedCallbackId];
  }
}

- (void)readerIsConnecting {
  NSLog(@"called readerIsConnecting");
  NSLog(@"CallbackId %@", self.onReaderConnectingCallbackId);
  if (self.onReaderConnectingCallbackId) {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:self.onReaderDisconnectedCallbackId];
  }
} 


- (void)readerIsConnected:(BOOL)isConnected withError:(NSError *)error {
  NSLog(@"called readerIsConnected");
  NSLog(@"CallbackId %@", self.onReaderConnectedCallbackId);
  CDVPluginResult* result;

  if (self.onReaderConnectingCallbackId) {
    if (isConnected) {
      NSLog(@"READER IS CONNECTED");
      result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    } else {
        NSLog(@"%@ %ld",@"ERROR CODE: %i", (long)error.code);
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    [self.commandDelegate sendPluginResult:result callbackId:self.onReaderDisconnectedCallbackId];
  }
}


- (void)startOnReaderResponse:(CDVInvokedUrlCommand*)command {
    _onReaderResponseCallbackId = command.callbackId;
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    NSLog(@"called startOnReaderCardResponse");
}

- (void)startOnReaderAttached:(CDVInvokedUrlCommand*)command {
  _onReaderAttachedCallbackId = command.callbackId;
  CDVPluginResult* pluginResult = nil;
  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
  [pluginResult setKeepCallbackAsBool:YES];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  NSLog(@"called startOnReaderAttached");
}


- (void)startOnReaderDisconnected:(CDVInvokedUrlCommand*)command {
  _onReaderDisconnectedCallbackId = command.callbackId;
  CDVPluginResult* pluginResult = nil;
  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
  [pluginResult setKeepCallbackAsBool:YES];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  NSLog(@"called startOnReaderDisconnected");
}


- (void)startOnReaderConnected:(CDVInvokedUrlCommand*)command {
  _onReaderConnectedCallbackId = command.callbackId;
  CDVPluginResult* pluginResult = nil;
  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
  [pluginResult setKeepCallbackAsBool:YES];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  NSLog(@"called startOnReaderConnected");
}

- (void)startOnReaderConnecting:(CDVInvokedUrlCommand*)command {
  _onReaderConnectingCallbackId = command.callbackId;
  CDVPluginResult* pluginResult = nil;
  pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
  [pluginResult setKeepCallbackAsBool:YES];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  NSLog(@"called startOnReaderConnecting");
}

@end