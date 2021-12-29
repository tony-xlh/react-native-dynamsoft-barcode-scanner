#import "React/RCTViewManager.h"

@interface RCT_EXTERN_MODULE(DynamsoftBarcodeScannerViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(isScanning, BOOL)
RCT_EXPORT_VIEW_PROPERTY(flashOn, BOOL)
RCT_EXPORT_VIEW_PROPERTY(cameraID, NSString)
RCT_EXPORT_VIEW_PROPERTY(organizationID, NSString)
RCT_EXPORT_VIEW_PROPERTY(dceLicense, NSString)
RCT_EXPORT_VIEW_PROPERTY(dbrLicense, NSString)
RCT_EXPORT_VIEW_PROPERTY(template, NSString)

@end
