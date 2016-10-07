#import "CDVCardFlight.h"
#import <Cordova/CDV.h>

@interface CDVCardFlight ()
@property (nonatomic) CFTReader *reader;
@property (nonatomic) CFTCard *card;
@property (nonatomic) CFTPaymentView *paymentView;
@property (nonatomic) CDVPluginResult *readerPluginResult;
@property (nonatomic) CDVPluginResult *transactionPluginResult;
@property (nonatomic) NSString *onLowBatteryCallbackId;
@property (nonatomic) NSString *onReaderResponseCallbackId;
@property (nonatomic) NSString *onReaderAttachedCallbackId;
@property (nonatomic) NSString *onReaderConnectedCallbackId;
@property (nonatomic) NSString *onReaderDisconnectedCallbackId;
@property (nonatomic) NSString *onReaderNotDetectedCallbackId;
@property (nonatomic) NSString *onReaderConnectingCallbackId;
@property (nonatomic) NSString *onCardSwipedCallbackId;
@property (nonatomic) NSString *onEMVMessageCallbackId;
@property (nonatomic) NSString *onEMVCardDippedCallbackId;
@property (nonatomic) NSString *onEMVCardRemovedCallbackId;
@property (nonatomic) NSString *onTransactionResultCallbackId;
@property (nonatomic) NSString *onTokenizeCardCallbackId;
@end

@implementation CDVCardFlight

// Set API tokens to initalize CardFlight with apiKey and accountToken values
// Initializes reader itself, and a success returns emvReady as BOOL

- (void)setApiTokens:(CDVInvokedUrlCommand*)command {
    NSDictionary *options = [command.arguments objectAtIndex:0];
    NSString *apiKey = [options valueForKey:@"apiKey"];
    NSString *accountToken = [options valueForKey:@"accountToken"];
    
    __weak CDVCardFlight *weakSelf = self;
    [self.commandDelegate runInBackground:^{
        [[CFTSessionManager sharedInstance] setApiToken:apiKey accountToken:accountToken completed:^(BOOL emvReady) {
            NSLog(@"%@: %@ %@: %@", @"API TOKEN",apiKey,@" ACCOUNT TOKEN %@",accountToken);
            
            // If the user passes a readerType, specify type during reader init
            if ([options valueForKey:@"readerType"]) {
                weakSelf.reader = [[CFTReader alloc] initWithReader:[[options valueForKey:@"readerType"] longValue]];
            } else {
                weakSelf.reader = [[CFTReader alloc] init];
            }
            
            CDVPluginResult* pluginResult;
            
            if (weakSelf.reader) {
                [weakSelf.reader setDelegate:weakSelf];
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:emvReady];
            } else {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
            }
            
            [weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }];
    }];
}


// Initialize card reader. Optional param: readerType

- (void)initReader:(CDVInvokedUrlCommand*)command {
    NSDictionary *options = [command.arguments objectAtIndex:0];
    CDVPluginResult* pluginResult;
    
    // If the user passes a readerType, specify type during reader init
    
    NSLog(@"init reader");
    if ([options valueForKey:@"readerType"]) {
        self.reader = [[CFTReader alloc] initWithReader:[[options valueForKey:@"readerType"] longValue]];
    } else {
        self.reader = [[CFTReader alloc] init];
    }
    
    
    if (self.reader) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


// Trigger keyed entry payment view. Will show a little more than 1/4 down on screen.
// Enable zip by passing optional {zip:true}. Default bool value = NO

- (void)beginKeyed:(CDVInvokedUrlCommand*)command {
    BOOL zip = NO;
    NSDictionary *options = [command.arguments objectAtIndex:0];
    if ([options valueForKey:@"zip"] != (id)[NSNull null]) {
        zip = [[options valueForKey:@"zip"] boolValue];
    }
    
    self.paymentView = [[CFTPaymentView alloc] initWithFrame:CGRectZero enableZip:zip];
    
    self.paymentView.delegate = self;
    self.paymentView.alpha = 0;
    self.paymentView.translatesAutoresizingMaskIntoConstraints = NO;
    [self.paymentView useKeyboardAppearance:UIKeyboardAppearanceDefault];
    [self.webView.superview addSubview:self.paymentView];
    
    UIView *subView=self.paymentView;
    UIView *parent=self.webView.superview;
    
    [UIView animateKeyframesWithDuration:.17 delay:.08 options:UIViewKeyframeAnimationOptionCalculationModeLinear animations:^{
        self.paymentView.alpha = 1.0f;
    } completion:^(BOOL finished) {
        [self.paymentView performSelector:@selector(becomeFirstResponder) withObject:self.webView.superview afterDelay:.1];
    }];
    
    NSLayoutConstraint *centerXConstraint = [NSLayoutConstraint constraintWithItem:subView attribute:NSLayoutAttributeCenterX
                                                                         relatedBy:NSLayoutRelationEqual
                                                                            toItem:parent
                                                                         attribute:NSLayoutAttributeCenterX
                                                                        multiplier:1.0 constant:0];
    
    NSLayoutConstraint *leftButtonYConstraint = [NSLayoutConstraint constraintWithItem:subView attribute:NSLayoutAttributeTop
                                                                             relatedBy:NSLayoutRelationEqual
                                                                                toItem:parent
                                                                             attribute:NSLayoutAttributeTop
                                                                            multiplier:1.0f constant:self.webView.frame.size.height/3.8];
    
    NSLayoutConstraint *widthConstraint = [NSLayoutConstraint constraintWithItem:subView
                                                                       attribute:NSLayoutAttributeWidth
                                                                       relatedBy:NSLayoutRelationEqual
                                                                          toItem:nil
                                                                       attribute:NSLayoutAttributeNotAnAttribute
                                                                      multiplier:1.0
                                                                        constant:289];
    
    NSLayoutConstraint *heightConstraint = [NSLayoutConstraint constraintWithItem:subView
                                                                        attribute:NSLayoutAttributeHeight
                                                                        relatedBy:NSLayoutRelationEqual
                                                                           toItem:nil
                                                                        attribute:NSLayoutAttributeNotAnAttribute
                                                                       multiplier:1.0
                                                                         constant:48];
    
    [self.webView.superview addConstraints:@[centerXConstraint, leftButtonYConstraint, widthConstraint, heightConstraint]];
}


// Keyed entry input has enough input to get valid card information,
// and sends card data (last4 and brand) to app as plugin result dictionary.
// This can be fired multiple times during keyed entry.

- (void)keyedCardResponse:(CFTCard *)card {
    NSLog(@"Valid card received %@", card);
    
    _card = card;
    NSDictionary *cardDictionary = [NSDictionary dictionaryWithObjectsAndKeys:
                                    [card valueForKey:@"last4"], @"last4",
                                    [card valueForKey:@"cardTypeString"], @"brand",
                                    @"keyed", @"type", nil];
    
    self.readerPluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                            messageAsDictionary:cardDictionary];
    
    if (self.onReaderResponseCallbackId) {
        [self.readerPluginResult setKeepCallbackAsBool:TRUE];
        [self.commandDelegate sendPluginResult:self.readerPluginResult callbackId:self.onReaderResponseCallbackId];
    }
}


// Keyed entry input does not have enough valid input to get card information.
// This will be fired multiple times throughout keyed entry.
// All reader responses send results via onReaderResponseCallbackId.

- (void)invalidKeyedResponse {
    _card = nil;
    
    if (self.onReaderResponseCallbackId) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [result setKeepCallbackAsBool:TRUE];
        [self.commandDelegate sendPluginResult:result callbackId:self.onReaderResponseCallbackId];
    }
}


// Removes the keyed-entry payment view and hides keyboard

- (void)endKeyed:(CDVInvokedUrlCommand*)command {
    [self.paymentView resignFirstResponder];
    [self.paymentView setHidden:YES];
    self.paymentView = nil;
}


// Prepare the reader for a swipe

- (void)beginSwipe:(CDVInvokedUrlCommand *)command {
    [self.reader beginSwipe];
    [self.reader swipeHasTimeout:NO];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


// Run CardFlight destroy()

- (void)destroy:(CDVInvokedUrlCommand *)command {
    [self.reader destroy];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


// Cancel the current transaction

- (void)cancelTransaction:(CDVInvokedUrlCommand *)command {
    [self.reader cancelTransaction];
    
    self.onEMVMessageCallbackId = nil;
    self.onReaderResponseCallbackId = nil;
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


// Begin an EMV transaction with setup information, including
// amount and optional metadata

- (void)beginEMV:(CDVInvokedUrlCommand*)command {
    NSDictionary *options = [command.arguments objectAtIndex:0];
    NSDecimalNumber *amount = [NSDecimalNumber decimalNumberWithString:[options valueForKey:@"amount"]];
    NSNumber *appFee = [NSDecimalNumber decimalNumberWithString:[options valueForKey:@"applicationFee"]];
    NSString *currency = [options valueForKey:@"currency"];
    NSDictionary *metadata;
    
    if ([options valueForKey:@"stripeAccount"] != (id)[NSNull null]) {
        metadata = [NSDictionary dictionaryWithObjectsAndKeys:
                    appFee, @"application_fee",
                    [options valueForKey:@"stripeAccount"], @"connected_stripe_account_id", nil];
    }
    
    NSDictionary *chargeDict = [NSDictionary dictionaryWithObjectsAndKeys:
                                metadata, @"metadata",
                                amount, @"amount",
                                currency, @"currency",
                                [options valueForKey:@"description"], @"description", nil];
    
    [self.reader beginEMVTransactionWithAmount:amount andChargeDictionary:chargeDict];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


// A card was swiped. This is called right away,
// before any success or fail, just the swipe itself

- (void)readerSwipeDetected {
    NSLog(@"swipe");
    if (self.onCardSwipedCallbackId) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [result setKeepCallbackAsBool:TRUE];
        [self.commandDelegate sendPluginResult:result callbackId:self.onCardSwipedCallbackId];
    }
}


// A swipe has been cancelled
// Reuses onReaderResponseCallbackId

- (void)readerSwipeDidCancel {
    NSLog(@"Swipe canceled");
    if (self.onReaderResponseCallbackId) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [result setKeepCallbackAsBool:TRUE];
        [self.commandDelegate sendPluginResult:result callbackId:self.onReaderResponseCallbackId];
    }
}


// Card reader is not detected (connect attempted)

- (void)readerNotDetected {
    NSLog(@"Reader not detected");
    if (self.onReaderNotDetectedCallbackId) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [result setKeepCallbackAsBool:TRUE];
        [self.commandDelegate sendPluginResult:result callbackId:self.onReaderNotDetectedCallbackId];
    }
}


// EMV card has been physically dipped into the reader

- (void)emvCardDipped {
    NSLog(@"Card dipped'");
    if (self.onEMVCardDippedCallbackId) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [result setKeepCallbackAsBool:TRUE];
        [self.commandDelegate sendPluginResult:result callbackId:self.onEMVCardDippedCallbackId];
    }
}


// EMV card has been physically removed from the reader

- (void)emvCardRemoved {
    NSLog(@"Card removed");
    if (self.onEMVCardRemovedCallbackId && self.reader) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [result setKeepCallbackAsBool:TRUE];
        [self.commandDelegate sendPluginResult:result callbackId:self.onEMVCardRemovedCallbackId];
    }
}


// A successful swipe has returned card information.
// All reader responses send results via onReaderResponseCallbackId.

- (void)readerCardResponse:(CFTCard *)card withError:(NSError *)error {
    if (error) {
        [self.reader beginSwipe];
        [self.reader swipeHasTimeout:NO];
        self.readerPluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                    messageAsString: error.localizedDescription];
    } else {
        _card = card;
        NSLog(@"IN RESPONSE %@", _card.name);
        self.readerPluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsDictionary:@{@"name": _card.name, @"last4": _card.last4, @"type": @"swipe"}];
    }
    
    __weak CDVCardFlight *weakSelf = self;
    [self.commandDelegate runInBackground:^{
        [self.readerPluginResult setKeepCallbackAsBool:TRUE];
        [weakSelf.commandDelegate sendPluginResult:self.readerPluginResult callbackId:weakSelf.onReaderResponseCallbackId];
    }];
}


// A successful EMV dip has returned card information.
// All reader responses send results via onReaderResponseCallbackId.

- (void)emvCardResponse:(NSDictionary *)cardDictionary {
    NSLog(@"IN RESPONSE %@", [cardDictionary valueForKey:@"last4"]);
    self.readerPluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                            messageAsDictionary:cardDictionary];
    [self.readerPluginResult setKeepCallbackAsBool:TRUE];
    [self.commandDelegate sendPluginResult:self.readerPluginResult callbackId:self.onReaderResponseCallbackId];
}


// Process a payment of any type.
// Required arguments: amount, type ('emv', 'swipe' or 'keyed')
// Optional argument: description

- (void)processCharge:(CDVInvokedUrlCommand*)command; {
    NSDictionary *options = [command.arguments objectAtIndex:0];
    NSString *type = [options valueForKey:@"type"];
    NSString *currency = [options valueForKey:@"currency"];
    NSDecimalNumber *amount = [NSDecimalNumber decimalNumberWithString:[options valueForKey:@"amount"]];
    
    if ([type isEqualToString: @"emv"]) {
        [self.reader emvProcessTransaction:TRUE];
    } else if (amount) {
        NSDecimalNumber *appFee = [NSDecimalNumber decimalNumberWithString:[options valueForKey:@"applicationFee"]];
        NSDictionary *metadata;
        
        if ([options valueForKey:@"stripeAccount"] != (id)[NSNull null]) {
            metadata =  [NSDictionary dictionaryWithObjectsAndKeys:
                         appFee, @"application_fee",
                         [options valueForKey:@"stripeAccount"], @"connected_stripe_account_id", nil];
        } else {
            metadata = [[NSDictionary alloc] init];
        }
        
        NSDictionary *chargeDict = [NSDictionary dictionaryWithObjectsAndKeys:
                                    metadata, @"metadata",
                                    amount, @"amount",
                                    currency, @"currency",
                                    [options valueForKey:@"description"], @"description", nil];
        
        __weak CDVCardFlight *weakSelf = self;
        [_card chargeCardWithParameters:chargeDict success:^(CFTCharge *charge) {
            NSLog(@"success");
            CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [result setKeepCallbackAsBool:TRUE];
            [weakSelf.commandDelegate sendPluginResult:result callbackId:weakSelf.onTransactionResultCallbackId];
        } failure:^(NSError *error) {
            NSLog(@"%@", error.localizedDescription);
            CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
            [result setKeepCallbackAsBool:TRUE];
            [weakSelf.commandDelegate sendPluginResult:result callbackId:weakSelf.onTransactionResultCallbackId];
        }];
    }
}


// EMV Transaction processed, receives transactionResult

- (void)emvTransactionResult:(CFTCharge *)charge requiresSignature:(BOOL)signature withError:(NSError *)error {
    CDVPluginResult* result;
    if (error) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
        NSLog(@"%@", error.localizedDescription);
    } else if (charge) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:signature];
        NSLog(@"Charge success");
    }
    
    [result setKeepCallbackAsBool:TRUE];
    [self.commandDelegate sendPluginResult:result callbackId:self.onTransactionResultCallbackId];
}


// Upload signature PNG data if/when required
// Accepts a single argument 'data' as base64 encoded string

- (void)uploadSignature:(CDVInvokedUrlCommand*)command; {
    NSDictionary *options = [command.arguments objectAtIndex:0];
    NSURL *url = [NSURL URLWithString:[options valueForKey:@"data"]];
    NSData *imageData = [NSData dataWithContentsOfURL:url];
    
    [self.reader emvTransactionSignature:imageData success:^{
        nil;
    } failure:^(NSError *error) {
        nil;
    }];
}


// Tokenize a card
// Sends plugin data via onTokenizeCardCallbackId

- (void)tokenizeCardWithSuccess:(CDVInvokedUrlCommand *)command {
    __weak CDVCardFlight *weakSelf = self;

    [_card tokenizeCardWithSuccess:^{
        NSLog(@"Token received %@", _card.cardToken);
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:_card.cardToken];
        [result setKeepCallbackAsBool:TRUE];
        [weakSelf.commandDelegate sendPluginResult:result callbackId:weakSelf.onTokenizeCardCallbackId];
    } failure:^(NSError *error) {
        NSLog(@"%@", error.localizedDescription);
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
        [result setKeepCallbackAsBool:TRUE];
        [weakSelf.commandDelegate sendPluginResult:result callbackId:weakSelf.onTokenizeCardCallbackId];
    }];
}


// Server response after submitting data

-(void)serverResponse:(NSData *)response andError:(NSError *)error {
    NSDictionary *jsonDict = [NSJSONSerialization JSONObjectWithData:response options:NSJSONReadingMutableContainers error:&error];
    NSLog(@"Server Response: %@", jsonDict);
}


// Callback fired when reader is physically attached
// Sends plugin data via onReaderAttachedCallbackId

- (void)readerIsAttached {
    NSLog(@"CallbackId %@", self.onReaderAttachedCallbackId);
    // fire corresponding callback id for onReaderAttached
    if (self.onReaderAttachedCallbackId) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:self.onReaderAttachedCallbackId];
    }
}


// Callback fired when reader is physically disconnected
// Sends plugin data via onReaderDisconnectedCallbackID

- (void)readerIsDisconnected {
    NSLog(@"CallbackId %@", self.onReaderDisconnectedCallbackId);
    // fire corresponding callback id for onReaderAttached
    if (self.onReaderDisconnectedCallbackId) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:self.onReaderDisconnectedCallbackId];
    }
}


// Callback fired when reader is attached and connecting
// Sends plugin data via onReaderConnectingCallbackId

- (void)readerIsConnecting {
    NSLog(@"CallbackId %@", self.onReaderConnectingCallbackId);
    if (self.onReaderConnectingCallbackId) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:result callbackId:self.onReaderConnectingCallbackId];
    }
}


// Callback fired when reader is attached and connected
// Sends plugin data via onReaderConnectedCallbackId

- (void)readerIsConnected:(BOOL)isConnected withError:(NSError *)error {
    NSLog(@"CallbackId %@", self.onReaderConnectedCallbackId);
    if (self.onReaderConnectedCallbackId) {
        CDVPluginResult* result;
        if (isConnected) {
            NSLog(@"READER IS CONNECTED");
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        } else {
            NSLog(@"%@ %ld",@"ERROR CODE: %i", (long)error.code);
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        }
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:self.onReaderConnectedCallbackId];
    }
}


// Callback fired when EMV instruction or status text is updated
// Sends plugin data with message text via onEMVMessageCallbackId

- (void)emvMessage:(CFTEMVMessage)message {
    NSString *msgText = [self.reader defaultMessageForCFTEMVMessage:message];
    if (self.onEMVMessageCallbackId) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:msgText];
        [result setKeepCallbackAsBool:TRUE];
        [self.commandDelegate sendPluginResult:result callbackId:self.onEMVMessageCallbackId];
    }
}


// Callback fired when card reader has a low battery.
// Set callback ID using registerOnLowBattery.

- (void)readerBatteryLow {
    NSLog(@"BATTERY LOW");
    if (self.onLowBatteryCallbackId) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [result setKeepCallbackAsBool:TRUE];
        [self.commandDelegate sendPluginResult:result callbackId:self.onLowBatteryCallbackId];
    }
}


// Select application if multiples are available
// Currently sets to first option (default in U.S. – must alter this for international use)

- (void)emvApplicationSelection:(NSArray *)applicationArray {
    [self.reader emvSelectApplication:0];
}


// Refund a charge using the chard ID / token
// Required params: token, amount

- (void)refundCharge:(CDVInvokedUrlCommand *)command {
    NSDictionary *options = [command.arguments objectAtIndex:0];
    NSString *chargeToken = [options valueForKey:@"chargeToken"];
    NSDecimalNumber *amount = [NSDecimalNumber decimalNumberWithString:[options valueForKey:@"amount"]];
    
    __block CDVPluginResult* result;
    
    [CFTCharge refundChargeWithToken:chargeToken andAmount:amount success:^(CFTCharge *charge) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        NSLog(@"Refunded!");
    } failure:^(NSError *error) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        NSLog(@"Fail");
    }];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}


// Get the state of the card reader
// Returns string version of state to JS app

- (void)readerState:(CDVInvokedUrlCommand *)command {
    CFTReaderState state = self.reader.readerState;
    NSString *stateString = [[NSString alloc]init];
    
    switch(state) {
        case ReaderState_UNKNOWN:
            stateString = @"UNKNOWN";
            break;
        case ReaderState_WAITING_FOR_CONNECT:
            stateString = @"WAITING_FOR_CONNECT";
            break;
        case ReaderState_ATTACHED:
            stateString = @"ATTACHED";
            break;
        case ReaderState_CONNECTED:
            stateString = @"CONNECTED";
            break;
        case ReaderState_WAITING_FOR_DIP:
            stateString = @"WAITING_FOR_DIP";
            break;
        case ReaderState_WAITING_FOR_SWIPE:
            stateString = @"WAITING_FOR_SWIPE";
            break;
        case ReaderState_SWIPE_DETECTED:
            stateString = @"SWIPE_DETECTED";
            break;
        case ReaderState_CARD_INSERTED:
            stateString = @"CARD_INSERTED";
            break;
        case ReaderState_REMOVE_CARD:
            stateString = @"REMOVE_CARD";
            break;
        case ReaderState_NOT_FOUND:
            stateString = @"NOT_FOUND";
            break;
        case ReaderState_DISCONNECTED:
            stateString = @"DISCONNECTED";
            break;
        default:
            break;
    }
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:stateString];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}


// Get the current SDK Version

- (void)SDKVersion:(CDVInvokedUrlCommand *)command {
    NSString *version = [[CFTSessionManager sharedInstance] SDKVersion];
    
    CDVPluginResult* result;
    if (version) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:version];
    } else {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}


// Get the current apiToken

- (void)apiToken:(CDVInvokedUrlCommand *)command {
    NSString *apiToken = [[CFTSessionManager sharedInstance] apiToken];
    
    CDVPluginResult* result;
    if (apiToken) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:apiToken];
    } else {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}


// Get the current account token

- (void)accountToken:(CDVInvokedUrlCommand *)command {
    NSString *accountToken = [[CFTSessionManager sharedInstance] accountToken];
    
    CDVPluginResult* result;
    if (accountToken) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:accountToken];
    } else {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}


// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onReaderResponse will send results to onReaderResponseCallbackId

- (void)registerOnReaderResponse:(CDVInvokedUrlCommand*)command {
    self.onReaderResponseCallbackId = command.callbackId;
    NSLog(@"set registerOnReaderCardResponse");
}


// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onEMVMessage will send results to onEMVMessageCallbackId

- (void)registerOnEMVMessage:(CDVInvokedUrlCommand*)command {
    self.onEMVMessageCallbackId = command.callbackId;
    NSLog(@"set registerEMVMessage");
}


// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onEMVCardDipped will send results to onEMVCardDippedCallbackId

- (void)registerOnEMVCardDipped:(CDVInvokedUrlCommand*)command {
    self.onEMVCardDippedCallbackId = command.callbackId;
    NSLog(@"set registerOnEMVCardDipped");
}


// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onCardSwiped will send results to onCardSwipedCallbackId

- (void)registerOnCardSwiped:(CDVInvokedUrlCommand*)command {
    self.onCardSwipedCallbackId = command.callbackId;
    NSLog(@"set registerOnCardSwiped");
}


// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onEMVCardRemoved will send results to onEMVCardRemovedCallbackId

- (void)registerOnEMVCardRemoved:(CDVInvokedUrlCommand*)command {
    self.onEMVCardRemovedCallbackId = command.callbackId;
    NSLog(@"set registerOnEMVCardRemoved");
}


// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onReaderAttached will send results to onReaderAttachedCallbackId

- (void)registerOnReaderAttached:(CDVInvokedUrlCommand*)command {
    self.onReaderAttachedCallbackId = command.callbackId;
    NSLog(@"set registerOnReaderAttached");
}


// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onReaderDisconnected will send results to onReaderDisconnectedCallbackId

- (void)registerOnReaderDisconnected:(CDVInvokedUrlCommand*)command {
    self.onReaderDisconnectedCallbackId = command.callbackId;
    NSLog(@"set registerOnReaderDisconnected");
}


// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onReaderNotDetected will send results to onReaderNotDetectedCallbackId

- (void)registerOnReaderNotDetected:(CDVInvokedUrlCommand*)command {
    self.onReaderNotDetectedCallbackId = command.callbackId;
    NSLog(@"set registerOnReaderNotDetectedCallbackId");
}


// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onReaderConnected will send results to onReaderConnectedCallbackId

- (void)registerOnReaderConnected:(CDVInvokedUrlCommand*)command {
    self.onReaderConnectedCallbackId = command.callbackId;
    NSLog(@"set registerOnReaderConnected");
}


// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onReaderConnecting will send results to onReaderConnectingCallbackId

- (void)registerOnReaderConnecting:(CDVInvokedUrlCommand*)command {
    self.onReaderConnectingCallbackId = command.callbackId;
    NSLog(@"set registerOnReaderConnecting");
}


// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onTransactionResult will send results to onTransactionResultCallbackId

- (void)registerOnTransactionResult:(CDVInvokedUrlCommand*)command {
    self.onTransactionResultCallbackId = command.callbackId;
    NSLog(@"set onTransactionResultCallbackId");
}


// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onTokenizeCard will send results to onTokenizeCardCallbackId

- (void)registerOnTokenizeCard:(CDVInvokedUrlCommand*)command {
    self.onTokenizeCardCallbackId = command.callbackId;
    NSLog(@"set onTokenizeCardCallbackId");
}


// Set callback ID to be a listener, reusable by the plugin.
// After this is set, onLowBattery will send results to onLowBatteryCallbackId

- (void)registerOnLowBattery:(CDVInvokedUrlCommand*)command {
    self.onLowBatteryCallbackId = command.callbackId;
    NSLog(@"set onLowBatteryCallbackId");
}

@end