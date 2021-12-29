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



class DynamsoftBarcodeScannerView : UIView, DMDLSLicenseVerificationDelegate, DBRTextResultDelegate {
    @objc var onScanned: RCTDirectEventBlock?
    var dce:DynamsoftCameraEnhancer! = nil
    var barcodeReader:DynamsoftBarcodeReader! = nil
    var dceView:DCECameraView! = nil
    var bridge:RCTBridge! = nil

    func setBridge(bridge:RCTBridge){
        self.bridge = bridge
    }
    
    @objc var isScanning: Bool = false {
        didSet {
            print("isScanning: "+isScanning.description)
            if isScanning
            {
                print("start scan")
                if (barcodeReader == nil){
                    configurationDBR()
                    configurationDCE()
                }else{
                    dce.resume()
                }
            }else{
                print("stop scan")
                if dce != nil {
                    print("pausing")
                    dce.pause()
                    print("done")
                }
            }
        }
    }
    
    @objc var flashOn: Bool = false {
        didSet {
            print("flash")
            if dce != nil {
                if flashOn
                {
                    dce.turnOnTorch()
                }else{
                    dce.turnOffTorch()
                }
            }
        }
    }
    
    @objc var cameraID: String = "" {
        didSet {
            print("cameraID")
            if dce != nil {
                if cameraID != "" {
                    var error: NSError? = NSError()
                    dce.selectCamera(cameraID, error: &error)
                }
            }
        }
    }
    
    
    func configurationDBR() {
        let dls = iDMDLSConnectionParameters()
        let license = ""
        
        if (license != ""){
            barcodeReader = DynamsoftBarcodeReader(license: license)
        }else{
            dls.organizationID = "200001"
            barcodeReader = DynamsoftBarcodeReader(licenseFromDLS: dls, verificationDelegate: self)
        }
    }
        
    func configurationDCE() {
        // Initialize a camera view for previewing video.
        dceView = DCECameraView.init(frame: self.bounds)
        self.addSubview(dceView)
        dceView.overlayVisible = true
        dce = DynamsoftCameraEnhancer.init(view: dceView)
        dce.open()
        onCameraOpened()
        bindDCEtoDBR()
    }

    func bindDCEtoDBR(){
        // Create settings of video barcode reading.
        let para = iDCESettingParameters.init()
        // This cameraInstance is the instance of the Dynamsoft Camera Enhancer.
        // The Barcode Reader will use this instance to take control of the camera and acquire frames from the camera to start the barcode decoding process.
        para.cameraInstance = dce
        // Make this setting to get the result. The result will be an object that contains text result and other barcode information.
        para.textResultDelegate = self
        // Bind the Camera Enhancer instance to the Barcode Reader instance.
        barcodeReader.setCameraEnhancerPara(para)
    }
    
    public func dlsLicenseVerificationCallback(_ isSuccess: Bool, error: Error?) {
        if(error != nil)
        {
            print("dls error")
        }
    }
    
    // Obtain the recognized barcode results from the textResultCallback and display the results
    public func textResultCallback(_ frameId: Int, results: [iTextResult]?, userData: NSObject?) {
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
        bridge.eventDispatcher().sendDeviceEvent(withName: "onCameraUpdated", body: info)
    }
}
