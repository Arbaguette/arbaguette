import styled from '@emotion/native';
import Entypo from '@expo/vector-icons/Entypo';
import { router } from 'expo-router';
import React from 'react';

import Button from '@/components/common/Button';
import useRootStore from '@/zustand';

import ScheduleAddCard from './ScheduleAddCard';

const ContractContainer = styled.View(({ theme }) => ({
  backgroundColor: theme.color.WHITE,
  gap: 20,
  paddingHorizontal: 10,
  width: '100%',
}));

const InfoHeader = styled.View(({ theme }) => ({
  backgroundColor: theme.color.WHITE,
  width: '100%',
  alignItems: 'center',
  justifyContent: 'space-between',
  flexDirection: 'row',
}));

const InfoTitle = styled.Text(({ theme }) => ({
  fontSize: 20,
  fontWeight: 'bold',
  color: theme.color.BLACK,
}));

const InfoContent = styled.View(({ theme }) => ({
  backgroundColor: theme.color.WHITE,
  width: '100%',
  justifyContent: 'flex-start',
  alignItems: 'center',
  gap: 15,
}));

const NoneSchedule = styled.View(({ theme }) => ({
  width: '100%',
  height: 100,
  justifyContent: 'center',
  alignItems: 'center',
}));

const NoneScheduleText = styled.Text(({ theme }) => ({
  fontSize: 16,
  fontWeight: 'bold',
  color: theme.color.GRAY[3],
}));

const ContractScheduleBox = () => {
  const { registWorkingDayInfoList } = useRootStore();
  return (
    <ContractContainer>
      <InfoHeader>
        <InfoTitle>근무 일정</InfoTitle>
        <Button
          onPress={() => router.push('/(app)/boss/contract/DaySetModal')}
          type="primary"
          buttonStyle={{ width: 25, height: 25, paddingHorizontal: 0, borderRadius: 20 }}
          textStyle={{ fontSize: 12 }}>
          <Entypo name="plus" size={16} color="white" />
        </Button>
      </InfoHeader>
      <InfoContent>
        {registWorkingDayInfoList.length === 0 && (
          <NoneSchedule style={{ height: 100 }}>
            <NoneScheduleText>일정이 없습니다.</NoneScheduleText>
          </NoneSchedule>
        )}
        {registWorkingDayInfoList.map((info) => (
          <ScheduleAddCard key={info.id} data={info} />
        ))}
      </InfoContent>
    </ContractContainer>
  );
};

export default ContractScheduleBox;
