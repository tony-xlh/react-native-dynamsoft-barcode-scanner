import {
  requireNativeComponent,
  UIManager,
  Platform,
  ViewStyle,
} from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-dynamsoft-barcode-scanner' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

type DynamsoftBarcodeScannerProps = {
  scanning: boolean;
  style: ViewStyle;
  onScanned: Event;
};

const ComponentName = 'DynamsoftBarcodeScannerView';

export const Scanner =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<DynamsoftBarcodeScannerProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };
