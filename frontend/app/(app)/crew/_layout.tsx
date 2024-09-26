import { router, Tabs, useGlobalSearchParams } from 'expo-router';

import BellButton from '@/components/crew/BellButton';
import CalendarButton from '@/components/crew/CalendarButton';
import Theme from '@/styles/Theme';

const CrewMainLayout = () => {
  const { year, month } = useGlobalSearchParams<Partial<{ year: string; month: string }>>();

  return (
    <Tabs
      initialRouteName="main"
      screenOptions={{
        headerStyle: {
          backgroundColor: Theme.color.WHITE,
          elevation: 0,
        },
        headerTitleAlign: 'center',
        headerLeftContainerStyle: { paddingHorizontal: Theme.layout.PADDING.HORIZONTAL },
        headerRightContainerStyle: { paddingHorizontal: Theme.layout.PADDING.HORIZONTAL },
      }}>
      <Tabs.Screen name="main" options={{ title: '홈' }} />
      <Tabs.Screen name="schedule" options={{ title: '스케줄' }} />
      <Tabs.Screen
        name="management"
        options={{
          title: '근무 내역',
          headerTitle: '근무 내역 조회',
          headerRight: () => <BellButton onPress={() => router.navigate('/crew/management/contract')} />,
          headerLeft: () => (
            <CalendarButton
              onPress={() => router.navigate({ pathname: '/crew/management/calendar', params: { year, month } })}
            />
          ),
        }}
      />
      <Tabs.Screen name="banking" options={{ title: '뱅킹' }} />
    </Tabs>
  );
};

export default CrewMainLayout;
