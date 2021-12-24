import * as React from 'react';
import { useState } from 'react';
import { DeviceEventEmitter, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { Scanner } from 'react-native-dynamsoft-barcode-scanner';
import type { ScanResult } from './Definitions';

export default function App() {
  const [scanning, setScanning] = useState(false);
  const [btnText, setBtnText] = useState('Start Scan');
  const [barcodesInfo, setBarcodesInfo] = useState('');
  const onScanned = (results:Array<ScanResult>) => {
    var info = "";
    for (var i=0;i<results.length;i++){
      let result = results[i];
      info = info + result.barcodeFormat + ": " + result.barcodeText + "\n";
    }
    setBarcodesInfo(info);
  }
  DeviceEventEmitter.addListener('onScanned',onScanned);
  const toggleScan = () =>  {
    if (scanning == true){
      setScanning(false);
      setBtnText("Start Scan");
    }else{
      setScanning(true);
      setBtnText("Stop Scan");
    }
  }

  return (
    <View style={styles.container}>
      <Scanner scanning={scanning} style={styles.scanner} onScanned={onScanned}/>
      <View style={{ position: 'absolute', top: 10,left: 10 }}>
          <Text style={{ fontSize: 14, textShadowRadius: 12, textShadowColor: "black", color: "white" }}> {barcodesInfo} </Text>
      </View>
      <View style={{ position: 'absolute', justifyContent: 'center', bottom: "10%" }}>
          <TouchableOpacity onPress={toggleScan} >
            <Text style={{ fontSize: 14, textShadowRadius: 30, textShadowColor: "white" }}> {btnText} </Text>
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
});
