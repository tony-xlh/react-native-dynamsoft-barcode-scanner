#import "React/RCTViewManager.h"

@interface RCT_EXTERN_MODULE(DynamsoftBarcodeScannerViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(isScanning, BOOL)
RCT_EXPORT_VIEW_PROPERTY(flashOn, BOOL)
RCT_EXPORT_VIEW_PROPERTY(cameraID, NSString)

@end
