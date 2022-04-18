package com.reactnativedynamsoftbarcodescanner;

import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.DBRLicenseVerificationListener;
import com.dynamsoft.dbr.EnumConflictMode;
import com.dynamsoft.dbr.ImageData;
import com.dynamsoft.dbr.TextResult;
import com.dynamsoft.dbr.TextResultListener;
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
    private String dceLicense = "DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ==";
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
            try {
                mCameraEnhancer.open();
            } catch (CameraEnhancerException e) {
                e.printStackTrace();
            }
        }else{
            if (mCameraEnhancer!=null){
                try {
                    mCameraEnhancer.close();
                } catch (CameraEnhancerException e) {
                    e.printStackTrace();
                }
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
        String license;
        if (dbrLicense != null){
            license = dceLicense;
        }else{
            license = "DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ==";
        }
        BarcodeReader.initLicense(license, new DBRLicenseVerificationListener() {
            @Override
            public void DBRLicenseVerificationCallback(boolean isSuccess, Exception error) {
                if(!isSuccess){
                    error.printStackTrace();
                }
            }
        });
        try {
            reader = new BarcodeReader();
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
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
        TextResultListener mTextResultListener = new TextResultListener() {
            // Obtain the recognized barcode results and display.
            @Override
            public void textResultCallback(int id, ImageData imageData, TextResult[] textResults) {
                onScanned(textResults);
            }
        };
        reader.setTextResultListener(mTextResultListener);
        // Bind the Camera Enhancer instance to the Barcode Reader instance.
        // The mCameraEnhancer is the instance of the Dynamsoft Camera Enhancer.
        // The Barcode Reader will use this instance to take control of the camera and acquire frames from the camera to start the barcode decoding process.
        reader.setCameraEnhancer(mCameraEnhancer);
        // Start the barcode scanning thread.
        reader.startScanning();
    }

    public void onScanned(TextResult[] textResults) {
        WritableArray results = Arguments.createArray();
        for (TextResult tr:textResults){
            WritableMap map = Arguments.createMap();
            map.putString("barcodeText", tr.barcodeText);
            map.putString("barcodeFormat", tr.barcodeFormatString);
            map.putString("barcodeBytesBase64", Base64.encodeToString(tr.barcodeBytes, Base64.DEFAULT));
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
        Log.d("DBR","camera opened");
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onCameraOpened",map);
    }

    @Override
    public void onHostResume() {
        if (mCameraEnhancer!=null){
            if (this.isScanning){
                try {
                    mCameraEnhancer.open();
                    reader.startScanning();
                } catch (CameraEnhancerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onHostPause() {
        if (mCameraEnhancer!=null){
            try {
                reader.stopScanning();
                mCameraEnhancer.close();
            } catch (CameraEnhancerException e) {
                e.printStackTrace();
            }
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
