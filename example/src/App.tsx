import { useState, useEffect } from 'react';
import { StyleSheet, View, Text } from 'react-native';
import {
  multiply,
  addNumbers,
  initializeTuracSDK,
} from 'react-native-librarysdk';

export default function App() {
  const [result, setResult] = useState<number | undefined>();
  const [result1, setResult1] = useState<number | undefined>();
  useEffect(() => {
    multiply(3, 7).then(setResult);
    addNumbers(3, 7).then(setResult1);
    initializeTuracSDK(
      'atPWbf4XiQ1L78nrQ8qUM0vN7odIeMdCdN0Hjcr7GU6jJczhDsmJCiPT13DrWqlA|646B5185F1FA84CB448709A7484496C0'
    )
      .then((result) => {
        // API anahtarı başarıyla işlendi.
        console.log('SDK initialized successfully:', result);
      })
      .catch((error) => {
        // Bir hata meydana geldi
        console.error('Failed to initialize SDK:', error.message);
      });
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <Text>Result: {result1}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
