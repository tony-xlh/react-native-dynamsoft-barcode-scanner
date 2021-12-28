import * as React from 'react';
import { useEffect, useState } from 'react';
import { Alert, BackHandler, DeviceEventEmitter, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { CameraInfo, Scanner, ScanResult } from 'react-native-dynamsoft-barcode-scanner';

let currentCameraInfo: CameraInfo | undefined;

export default function App() {

  useEffect(() => {
    const backAction = () => {
      Alert.alert("Hold on!", "Are you sure you want to close the app?", [
        {
          text: "Cancel",
          onPress: () => null,
          style: "cancel"
        },
        { text: "YES", onPress: () => BackHandler.exitApp() }
      ]);
      return true;
    };

    const backHandler = BackHandler.addEventListener(
      "hardwareBackPress",
      backAction
    );

    return () => backHandler.remove();
  }, []);

  const [isScanning, setIsScanning] = useState(false);
  const [flashOn, setFlashOn] = useState(false);
  const [btnText, setBtnText] = useState('Start Scan');
  const [barcodesInfo, setBarcodesInfo] = useState('');
  const [cameraID, setCameraID] = useState('');
  const onScanned = (results:Array<ScanResult>) => {
    var info = "";
    for (var i=0;i<results.length;i++){
      let result = results[i];
      info = info + result.barcodeFormat + ": " + result.barcodeText + "\n";
    }
    setBarcodesInfo(info);
  }
  
  const onCameraUpdated = (cameraInfo:CameraInfo) => {
    if (cameraInfo != undefined){
      currentCameraInfo = cameraInfo;
    }
  }

  const switchCamera = () => {
    if (currentCameraInfo != undefined){
      for (var i=0;i<currentCameraInfo.cameras.length;i++){
        console.log(currentCameraInfo.cameras[i]);
        if (currentCameraInfo.selectedCamera!=currentCameraInfo.cameras[i]){
          let newCamera = currentCameraInfo.cameras[i];
          currentCameraInfo.selectedCamera = newCamera;
          setCameraID(newCamera);
          return;
        }
      }
    }
  }
  
  DeviceEventEmitter.addListener('onScanned',onScanned);
  DeviceEventEmitter.addListener('onCameraUpdated',onCameraUpdated);

  const toggleScan = () =>  {
    if (isScanning == true){
      setIsScanning(false);
      setBtnText("Start Scan");
    }else{
      setIsScanning(true);
      setBtnText("Stop Scan");
    }
  }

  const toggleFlash = () =>  {
    if (flashOn == true){
      setFlashOn(false);
    }else{
      setFlashOn(true);
    }
  }
  const template = "{\"ImageParameter\":{\"BarcodeFormatIds\":[\"BF_QR_CODE\"],\"Description\":\"\",\"Name\":\"Settings\"},\"Version\":\"3.0\"}";
  
  return (
    <View style={styles.container}>
      <Scanner 
        isScanning={isScanning} 
        style={styles.scanner} 
        flashOn={flashOn}
        cameraID={cameraID}
        onScanned={onScanned}
        template={template}
        onCameraUpdated={onCameraUpdated}
      />
      
      <View style={{ position: 'absolute', top: 10,left: 10 }}>
          <Text style={{ fontSize: 14, textShadowRadius: 12, textShadowColor: "black", color: "white" }}> {barcodesInfo} </Text>
      </View>
      <View style={{ position: 'absolute', top: 10, right: 10, height: 20 }}>
          <TouchableOpacity onPress={toggleFlash} >
            <Text style={{ fontSize: 14, textShadowRadius: 30, textShadowColor: "white" }}> Toggle Flash </Text>
          </TouchableOpacity>
      </View>
      <View style={{ position: 'absolute', top: 40, right: 10, height: 20 }}>
          <TouchableOpacity onPress={switchCamera} >
            <Text style={{ fontSize: 14,textAlignVertical:"center", textShadowRadius: 30, textShadowColor: "white" }}> Switch Camera </Text>
          </TouchableOpacity>
      </View>
      <View style={{ ...styles.bordered, ...{ position: 'absolute', justifyContent: 'center', bottom: "10%" } }}>
          <TouchableOpacity onPress={toggleScan} >
            <Text style={{ fontSize: 16 }}> {btnText} </Text>
          </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  scanner: {
    width: "100%",
    height: "100%",
  },
  bordered:{
    borderColor:"black", 
    borderWidth:2, 
    borderRadius:5,
    backgroundColor: "rgba(255,255,255,0.2)"
  }
});
