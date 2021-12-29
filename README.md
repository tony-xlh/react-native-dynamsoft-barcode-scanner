# react-native-dynamsoft-barcode-scanner

React Native Barcode Scanner library based on [Dynamsoft Barcode Reader](https://www.dynamsoft.com/barcode-reader/overview/) and [Dynamsoft Camera Enhancer](https://www.dynamsoft.com/camera-enhancer/overview/).

## Supported Platforms

* Android
* iOS

## Supported Barcode Symbologies

* Code 39
* Code 93
* Code 128
* Codabar
* EAN-8
* EAN-13
* UPC-A
* UPC-E
* Interleaved 2 of 5 (ITF)
* Industrial 2 of 5 (Code 2 of 5 Industry, Standard 2 of 5, Code 2 of 5)
* ITF-14 
* QRCode
* DataMatrix
* PDF417
* GS1 DataBar
* Maxicode
* Micro PDF417
* Micro QR
* PatchCode
* GS1 Composite
* Postal Code
* Dot Code

## Installation

```sh
npm install react-native-dynamsoft-barcode-scanner
```

Or from git:

```sh
npm install https://github.com/xulihang/react-native-dynamsoft-barcode-scanner
```

## Demo

```js
import * as React from 'react';
import {
  StyleSheet,
  View,
  Text,
  DeviceEventEmitter,
} from 'react-native';
import { Scanner } from 'react-native-dynamsoft-barcode-scanner'

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  scanner: {
    width: "100%",
    height: "100%",
  }
});

const App = () => {
  const [isScanning, setIsScanning] = React.useState(true);
  const [barcodesInfo, setBarcodesInfo] = React.useState('');
  const onScanned = (results) => {
      var info = "";
      for (var i=0;i<results.length;i++){
        let result = results[i];
        info = info + result.barcodeFormat + ": " + result.barcodeText + "\n";
      }
      setBarcodesInfo(info);
      console.log(info);
  }
  DeviceEventEmitter.addListener('onScanned',onScanned);

  return (
    <View style={styles.container}>
      <Scanner 
        isScanning={isScanning}
        onScanned={onScanned}
        style={styles.scanner}
      />
      <View style={{ position: 'absolute', top: "5%", left: 10, width: "80%" }}>
          <Text style={{ fontSize: 14, textShadowRadius: 12, textShadowColor: "black", color: "white" }}> {barcodesInfo} </Text>
      </View>
    </View>
  );
};

export default App;
```

### Props

```ts
type DynamsoftBarcodeScannerProps = {
  isScanning: boolean;
  style: ViewStyle;
  template?: string;
  organizationID?: string;
  dbrLicense?: string;
  dceLicense?: string;
  flashOn?: boolean;
  cameraID?: string;
  onScanned?: Event;
  onCameraOpened?: Event;
};
```

## License

MIT
