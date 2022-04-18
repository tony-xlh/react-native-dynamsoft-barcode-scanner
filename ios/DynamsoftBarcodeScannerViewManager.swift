import DynamsoftCameraEnhancer
import DynamsoftBarcodeReader
import React

@objc(DynamsoftBarcodeScannerViewManager)
class DynamsoftBarcodeScannerViewManager: RCTViewManager  {

  override func view() -> (DynamsoftBarcodeScannerView) {
      let view = DynamsoftBarcodeScannerView()
      view.setBridge(bridge: self.bridge)
      return view
  }
}



class DynamsoftBarcodeScannerView : UIView, DBRLicenseVerificationListener, DCELicenseVerificationListener, DBRTextResultListener {

    @objc var dceLicense: String = "DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ=="
    @objc var dbrLicense: String = "DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ=="
    var dce:DynamsoftCameraEnhancer! = nil
    var barcodeReader:DynamsoftBarcodeReader! = nil
    var dceView:DCECameraView! = nil
    var bridge:RCTBridge! = nil

    func setBridge(bridge:RCTBridge){
        self.bridge = bridge
    }
    
    @objc var isScanning: Bool = false {
        didSet {
            if isScanning
            {
                if (barcodeReader == nil){
                    configurationDBR()
                    configurationDCE()
                    updateSettings()
                }else{
                    barcodeReader.startScanning()
                    dce.resume()
                }
            }else{
                if dce != nil {
                    barcodeReader.stopScanning()
                    dce.pause()
                }
            }
        }
    }
    
    @objc var flashOn: Bool = false {
        didSet {
            toggleFlash()
        }
    }
    
    @objc var cameraID: String = "" {
        didSet {
            if dce != nil {
                if cameraID != "" {
                    var error: NSError? = NSError()
                    dce.selectCamera(cameraID, error: &error)
                }
            }
        }
    }
    
    @objc var template: String = "" {
        didSet {
            updateTemplate()
        }
    }
    
    func updateSettings(){
        toggleFlash()
        updateTemplate()
    }
    
    func toggleFlash(){
        if dce != nil {
            if flashOn
            {
                dce.turnOnTorch()
            }else{
                dce.turnOffTorch()
            }
        }
    }
    
    func updateTemplate(){
        if barcodeReader != nil {
            if (template != ""){
                try? barcodeReader.initRuntimeSettingsWithString(template, conflictMode: EnumConflictMode.overwrite)
            }else{
                try? barcodeReader.resetRuntimeSettings()
            }
        }
    }
    
    func configurationDBR() {
        DynamsoftBarcodeReader.initLicense(dbrLicense, verificationDelegate: self)
        barcodeReader = DynamsoftBarcodeReader.init()
    }
    
    func dbrLicenseVerificationCallback(_ isSuccess: Bool, error: Error?) {
        let err = error as NSError?
        if(err != nil){
            print("Server DBR license verify failed")
        }
    }
    
        
    func configurationDCE() {
        // Initialize a camera view for previewing video.
        dceView = DCECameraView.init(frame: self.bounds)
       
        self.addSubview(dceView)
        dceView.overlayVisible = true
        DynamsoftCameraEnhancer.initLicense(dceLicense, verificationDelegate: self)
        dce = DynamsoftCameraEnhancer.init(view: dceView)
        dce.open()
        onCameraOpened()
        bindDCEtoDBR()
    }

    func bindDCEtoDBR(){
        // Bind Camera Enhancer to the Barcode Reader.
        // The Barcode Reader will get video frame from the Camera Enhancer
        barcodeReader.setCameraEnhancer(dce)

        // Set text result call back to get barcode results.
        barcodeReader.setDBRTextResultListener(self)

        // Start the barcode decoding thread.
        barcodeReader.startScanning()
    }
    
    
    func dceLicenseVerificationCallback(_ isSuccess: Bool, error: Error?) {
        let err = error as NSError?
        if(err != nil){
            print("Server DCE license verify failed")
        }
    }
    
    // Obtain the recognized barcode results from the textResultCallback and display the results
    func textResultCallback(_ frameId: Int, imageData: iImageData, results: [iTextResult]?) {
        let count = results?.count ?? 0
        let array = NSMutableArray()
        for index in 0..<count {
            let tr = results![index]
            let result = NSMutableDictionary()
            result["barcodeText"] = tr.barcodeText
            result["barcodeFormat"] = tr.barcodeFormatString
            result["barcodeBytesBase64"] = tr.barcodeBytes?.base64EncodedString()
            array.add(result)
        }
        bridge.eventDispatcher().sendDeviceEvent(withName: "onScanned", body: array)
    }
    
    public func onCameraOpened(){
        let info = NSMutableDictionary()
        info["selectedCamera"] = dce.getSelectedCamera()
        print(dce.getSelectedCamera())
        let array = NSMutableArray()
        for cameraID in dce.getAllCameras(){
            array.add(cameraID)
        }
        info["cameras"] = array
        bridge.eventDispatcher().sendDeviceEvent(withName: "onCameraOpened", body: info)
    }
}
