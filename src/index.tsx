import { NativeModules, Platform } from 'react-native';
import { request, PERMISSIONS, RESULTS } from 'react-native-permissions';

const LINKING_ERROR =
  `The package 'react-native-librarysdk' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const Librarysdk = NativeModules.Librarysdk
  ? NativeModules.Librarysdk
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

async function requestPhonePermission(): Promise<boolean> {
  if (Platform.OS === 'android') {
    const permissionStatus = await request(PERMISSIONS.ANDROID.READ_PHONE_STATE);
    return permissionStatus === RESULTS.GRANTED;
  }
  return true;
}

export async function initializeTuracSDK(apiKey: string): Promise<string> {
  const permissionGranted = await requestPhonePermission();
  if (!permissionGranted) {
    return Promise.reject(new Error('Phone permission not granted.'));
  }
  
  return Librarysdk.initializeTuracSDK(apiKey);
}
export function multiply(a: number, b: number): Promise<number> {
  return Librarysdk.multiply(a, b);
}
export function addNumbers(a: number, b: number): Promise<number> {
  return Librarysdk.addNumbers(a, b);
}
