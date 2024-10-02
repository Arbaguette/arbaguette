import { useQuery } from '@tanstack/react-query';

import { ExpectedSalaryCard } from '@/components/crew/Card/ExpectedSalaryCard';
import keys from '@/reactQuery/keys';
import arbaguette from '@/services/arbaguette';

/**
 * 이메일 중복 체크를 위한 쿼리 훅
 * @param email
 */
const useEmailCheck = (email: Email) => {
  const { data } = useQuery({ queryKey: keys.email(email), queryFn: () => arbaguette.checkEmail(email) });

  const isUnique = !!data?.data.code;

  return { isUnique };
};

/**
 * 사업장 리스트를 가져오는 쿼리 훅
 */
const useCompanyList = () => {
  const { data } = useQuery({ queryKey: keys.companyList(), queryFn: () => arbaguette.getCompanyList() });

  const companyList = data?.data.data.companies || [];

  return { companyList };
};

/**
 * 알바생 리스트를 가져오는 쿼리 훅
 */
const useCrewMemberList = () => {
  const { data } = useQuery({ queryKey: keys.crewList(), queryFn: () => arbaguette.getCrewMemberList() });

  const crewList = data?.data.data.crews || [];

  return { crewList };
};

/**
 * 알바생 상세 정보를 가져오는 쿼리 훅
 */
const useCrewMemeberDetail = (crewId: CrewId) => {
  const { data } = useQuery({
    queryKey: keys.crewDetail(crewId),
    queryFn: () => arbaguette.getCrewMemberDetail(crewId),
  });

  const crewDetail = data?.data.data;
  return { crewDetail };
};

/**
 * 당월 누적 급여를 가져오는 쿼리 훅
 */
const useMonthlyAccumulatedSalary = () => {
  const { data } = useQuery({
    queryKey: keys.accumulatedSalary(),
    queryFn: () => arbaguette.getMonthlyAccumulatedSalary(),
  });

  return data?.data.data || 0;
};

/**
 * 당월 예상 급여를 가져오는 쿼리 훅
 */
const useMonthlyEstimatedSalary = () => {
  const queryResult = useQuery({
    queryKey: keys.estimatedSalary(),
    queryFn: () => arbaguette.getMonthlyEstimatedSalary(),
  });

  const estimatedSalary = queryResult.data?.data.data || 0;

  return { estimatedSalary, ...queryResult };
};

/**
 * 계좌 잔액을 가져오는 쿼리 훅
 */
const useAccountBalance = () => {
  const queryResult = useQuery({
    queryKey: keys.balance(),
    queryFn: () => arbaguette.getAccountBalance(),
  });

  const accountBalance = queryResult.data?.data.data || 0;
  return { accountBalance, ...queryResult };
};

/**
 * 가까운 출퇴근 정보를 가져오는 쿼리 훅
 */
const useNearCommuteInfo = () => {
  const { data } = useQuery({
    queryKey: keys.nearCommuteInfo(),
    queryFn: () => arbaguette.getNearCommuteInfo(),
  });

  if (!data) return null;

  return data.data.data;
};

/**
 *  급여명세서를 가져오는 쿼리 훅
 * @param month 조회할 달
 */
const usePayStub = (month: Month) => {
  const queryResult = useQuery({
    queryKey: keys.payStub(month),
    queryFn: () => arbaguette.getPayStub(month),
  });
  const certification = queryResult.data?.data.data || null;
  return { certification, ...queryResult };
};

/**
 * 일별 스케줄을 가져오는 쿼리 훅
 * @param date 조회할 날짜
 * @param companyId 사업장 ID
 */
const useDailySchedule = (date: string, companyId?: CompanyId) => {
  const { data } = useQuery({
    queryKey: keys.dailySchedule(date, companyId),
    queryFn: () => arbaguette.getDailySchedule(date, companyId),
  });

  if (!data) return null;

  return data.data.data;
};

/**
 * 입출금 내역을 가져오는 쿼리 훅
 *
 */
const useGetBankHistory = () => {
  const queryResult = useQuery({
    queryKey: keys.bankHistory(),
    queryFn: () => arbaguette.getBankHistory(),
  });

  const bankHistory = queryResult.data?.data.data || null;
  return { bankHistory, ...queryResult };
};

/**
 * 사장님 이번달 예상지출 조회 쿼리 훅
 */
const useGetExpectedPayroll = () => {
  const queryResult = useQuery({
    queryKey: keys.bankHistory(),
    queryFn: () => arbaguette.getExpectedPayroll(),
  });

  const expectedPayroll = queryResult.data?.data || null;
  return { expectedPayroll, ...queryResult };
};

export {
  useAccountBalance,
  useCompanyList,
  useCrewMemberList,
  useCrewMemeberDetail,
  useDailySchedule,
  useEmailCheck,
  useGetBankHistory,
  useGetExpectedPayroll,
  useMonthlyAccumulatedSalary,
  useMonthlyEstimatedSalary,
  useNearCommuteInfo,
  usePayStub,
};
