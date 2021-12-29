package com.reactnativedynamsoftbarcodescanner;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.DBRDLSLicenseVerificationListener;
import com.dynamsoft.dbr.DCESettingParameters;
import com.dynamsoft.dbr.DMDLSConnectionParameters;
import com.dynamsoft.dbr.EnumConflictMode;
import com.dynamsoft.dbr.TextResult;
import com.dynamsoft.dbr.TextResultCallback;
import com.dynamsoft.dce.CameraEnhancer;
import com.dynamsoft.dce.CameraEnhancerException;
import com.dynamsoft.dce.DCECameraView;
import com.dynamsoft.dce.DCELicenseVerificationListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;


public class DynamsoftBarcodeScannerViewManager extends SimpleViewManager<DCECameraView> implements LifecycleEventListener {
    public static final String REACT_CLASS = "DynamsoftBarcodeScannerView";
    private CameraEnhancer mCameraEnhancer;
    private DCECameraView mCameraView;
    private BarcodeReader reader = null;
    private ThemedReactContext context;
    private String dbrLicense = null;
    private String template = null;
    private String organizationID = "200001";
    private String dceLicense = "DLS2eyJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSJ9";
    private Boolean flashOn = false;
    private Boolean isScanning = false;

    @Override
    @NonNull
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    @NonNull
    public DCECameraView createViewInstance(ThemedReactContext reactContext) {
        context = reactContext;
        reactContext.addLifecycleEventListener(this);
        mCameraView = new DCECameraView(reactContext.getBaseContext());
        return mCameraView;
    }

    @ReactProp(name = "organizationID")
    public void setOrganizationID(View view, String ID) {
        organizationID = ID;
    }

    @ReactProp(name = "dceLicense")
    public void setDceLicense(View view, String license) {
        dceLicense = license;
    }

    @ReactProp(name = "template")
    public void setTemplate(View view, String template) {
        this.template = template;
        updateTemplate();
    }

    @ReactProp(name = "flashOn")
    public void setFlashOn(View view, Boolean on) {
        flashOn = on;
        toggleFlash();
    }

    private void updateSettings(){
        toggleFlash();
        updateTemplate();
    }

    private void toggleFlash(){
        if (mCameraEnhancer!=null){
            try {
                if (flashOn == true){
                    mCameraEnhancer.turnOnTorch();
                }else{
                    mCameraEnhancer.turnOffTorch();
                }
            } catch (CameraEnhancerException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateTemplate(){
        if (reader!=null && template!=null){
            try {
                reader.initRuntimeSettingsWithString(template, EnumConflictMode.CM_OVERWRITE);
            } catch (BarcodeReaderException e) {
                e.printStackTrace();
            }
        }
    }

    @ReactProp(name = "dbrLicense")
    public void setDbrLicense(View view, String license) {
        dbrLicense = license;
    }

    @ReactProp(name = "cameraID")
    public void setCameraID(View view, String id) {
        if (mCameraEnhancer!=null){
            try {
                mCameraEnhancer.selectCamera(id);
            } catch (CameraEnhancerException  e) {
                e.printStackTrace();
            }
        }
    }

    @ReactProp(name = "isScanning")
    public void setIsScanning(View view, Boolean isScanning) {
        this.isScanning = isScanning;
        if (isScanning) {
            initIfNeeded();
            updateSettings();
            reader.StartCameraEnhancer();
        }else{
            if (reader!=null){
                reader.StopCameraEnhancer();
            }
        }
    }

    private void initIfNeeded(){
        if (reader == null){
            initDBR();
            initDCE(context);
            bindDBRandDCE();
        }
    }

    private void initDBR()  {
        try {
            reader = new BarcodeReader();
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }
        if (dbrLicense!=null){
            try {
                reader.initLicense(dbrLicense);
            } catch (BarcodeReaderException e) {
                e.printStackTrace();
            }
        }else{
            DMDLSConnectionParameters dbrParameters = new DMDLSConnectionParameters();
            dbrParameters.organizationID = organizationID;
            reader.initLicenseFromDLS(dbrParameters, new DBRDLSLicenseVerificationListener() {
                @Override
                public void DLSLicenseVerificationCallback(boolean isSuccessful, Exception e) {
                    if (!isSuccessful) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private DCECameraView initDCE(ThemedReactContext reactContext){
        CameraEnhancer.initLicense(dceLicense, new DCELicenseVerificationListener() {
            @Override
            public void DCELicenseVerificationCallback(boolean isSuccess, Exception error) {
                if(!isSuccess){
                    error.printStackTrace();
                }
            }
        });

        mCameraEnhancer = new CameraEnhancer(reactContext.getBaseContext());
        onCameraOpened();
        mCameraEnhancer.setCameraView(mCameraView);
        mCameraView.setOverlayVisible(true);
        return mCameraView;
    }

    private void bindDBRandDCE(){
        TextResultCallback mTextResultCallback = new TextResultCallback() {
            // Obtain the recognized barcode results and display.
            @Override
            public void textResultCallback(int i, TextResult[] textResults, Object userData) {
                onScanned(textResults);
            }
        };
        DCESettingParameters dceSettingParameters = new DCESettingParameters();
        dceSettingParameters.cameraInstance = mCameraEnhancer;
        dceSettingParameters.textResultCallback = mTextResultCallback;
        reader.SetCameraEnhancerParam(dceSettingParameters);
    }

    public void onScanned(TextResult[] textResults) {
        WritableArray results = Arguments.createArray();
        for (TextResult result:textResults){
            WritableMap map = Arguments.createMap();
            map.putString("barcodeText",result.barcodeText);
            map.putString("barcodeFormat",result.barcodeFormatString);
            results.pushMap(map);
        }
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onScanned",results);
    }

    public void onCameraOpened() {
        WritableMap map = Arguments.createMap();
        WritableArray cameras = Arguments.createArray();
        for (String cameraID : mCameraEnhancer.getAllCameras()){
            cameras.pushString(cameraID);
        }
        map.putArray("cameras",cameras);
        map.putString("selectedCamera",mCameraEnhancer.getSelectedCamera());
        Log.d("DBR","camera updated");
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onCameraOpened",map);
    }

    @Override
    public void onHostResume() {
        if (reader!=null){
            if (this.isScanning){
                reader.StartCameraEnhancer();
            }
        }
    }

    @Override
    public void onHostPause() {
        if (reader!=null){
            reader.StopCameraEnhancer();
        }
    }

    @Override
    public void onHostDestroy() {
        if (reader!=null){
            reader.destroy();
            reader=null;
            mCameraEnhancer=null;
        }
        context.removeLifecycleEventListener(this);
    }
}
