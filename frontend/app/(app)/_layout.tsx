import { Stack } from 'expo-router';

const AppLayout = () => {
  return (
    <Stack screenOptions={{ headerShown: false }} initialRouteName="(public)">
      <Stack.Screen name="(public)" />
      <Stack.Screen name="boss" />
      <Stack.Screen name="crew" />
      <Stack.Screen name="notification" />
    </Stack>
  );
};

export default AppLayout;
