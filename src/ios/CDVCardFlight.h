#import <Cordova/CDV.h>
#import "CardFlight.h"
#import "CFTReader.h"
#import "CFTCard.h"
#import "CFTCharge.h"
#import "CFTSessionManager.h"

@interface CDVCardFlight : CDVPlugin <CFTReaderDelegate, CFTPaymentViewDelegate>

@end