package com.reactnativedynamsoftbarcodescanner;

import android.graphics.Camera;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.DBRDLSLicenseVerificationListener;
import com.dynamsoft.dbr.DCESettingParameters;
import com.dynamsoft.dbr.DMDLSConnectionParameters;
import com.dynamsoft.dbr.TextResult;
import com.dynamsoft.dbr.TextResultCallback;
import com.dynamsoft.dce.CameraEnhancer;
import com.dynamsoft.dce.CameraEnhancerException;
import com.dynamsoft.dce.DCECameraView;
import com.dynamsoft.dce.DCELicenseVerificationListener;
import com.dynamsoft.dce.EnumCameraState;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.Map;

public class DynamsoftBarcodeScannerViewManager extends SimpleViewManager<DCECameraView> {
    public static final String REACT_CLASS = "DynamsoftBarcodeScannerView";
    private CameraEnhancer mCameraEnhancer;
    private DCECameraView mCameraView;
    private BarcodeReader reader = null;
    private ThemedReactContext context;
    @Override
    @NonNull
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    @NonNull
    public DCECameraView createViewInstance(ThemedReactContext reactContext) {
        context = reactContext;
        mCameraView = new DCECameraView(reactContext.getBaseContext());
        init();
        return mCameraView;
    }

    @ReactProp(name = "scanning")
    public void setScanning(View view, Boolean scanning) {
        if (scanning) {
            reader.StartCameraEnhancer();
        }else{
            reader.StopCameraEnhancer();
        }
    }

    private void init(){
        initDBR();
        initDCE(context);
        bindDBRandDCE();
    }

    private void initDBR()  {
        try {
            reader = new BarcodeReader();
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }
        DMDLSConnectionParameters dbrParameters = new DMDLSConnectionParameters();
        dbrParameters.organizationID = "200001";
        reader.initLicenseFromDLS(dbrParameters, new DBRDLSLicenseVerificationListener() {
            @Override
            public void DLSLicenseVerificationCallback(boolean isSuccessful, Exception e) {
                if (!isSuccessful) {
                    e.printStackTrace();
                }
            }
        });
    }

    private DCECameraView initDCE(ThemedReactContext reactContext){
        CameraEnhancer.initLicense("200001", new DCELicenseVerificationListener() {
          @Override
          public void DCELicenseVerificationCallback(boolean isSuccess, Exception error) {
            if(!isSuccess){
              error.printStackTrace();
            }
          }
        });

        mCameraEnhancer = new CameraEnhancer(reactContext.getBaseContext());
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
}
