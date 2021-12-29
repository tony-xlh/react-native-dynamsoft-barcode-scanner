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

## Usage

```js
import { DynamsoftBarcodeScannerView } from "react-native-dynamsoft-barcode-scanner";

export default function App() {
    const [isScanning, setIsScanning] = useState(false);
    const onScanned = (results:Array<ScanResult>) => {
        var info = "";
        for (var i=0;i<results.length;i++){
          let result = results[i];
          info = info + result.barcodeFormat + ": " + result.barcodeText + "\n";
        }
        console.log(info);
    }
    DeviceEventEmitter.addListener('onScanned',onScanned);
    return (
        <Scanner 
            isScanning={isScanning}
            onScanned={onScanned}
        />
    );
}
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
