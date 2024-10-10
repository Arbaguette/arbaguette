const keys = {
  all: ['arbaguette'] as const,
  notification: () => [keys.all, 'notification'],
  boss: () => [...keys.all, 'boss'],
  company: () => [...keys.boss(), 'company'],
  daySchedule: (date: string, companyId: CompanyId) => [...keys.company(), 'schedule', date, companyId],
  companyList: () => [...keys.company(), 'list'],
  crewList: (companyId: CompanyId) => [...keys.boss(), 'crews', companyId],
  crew: () => [...keys.all, 'crew'],
  crewDetail: (crewId: CrewId) => [...keys.crew(), 'detail', crewId],
  nearCommuteInfo: () => [...keys.crew(), 'nearCommuteInfo'],
  workHistory: (date: string) => [...keys.crew(), 'workHistory', date],
  salary: () => [...keys.crew(), 'salary'],
  payStub: (month: Month) => [...keys.salary(), 'payStub', month],
  accumulatedSalary: () => [...keys.salary(), 'accumulatedSalary'],
  estimatedSalary: () => [...keys.salary(), 'estimatedSalary'],
  common: () => [...keys.all, 'common'],
  email: (email: Email) => [...keys.common(), 'email', email],
  dailySchedule: (date: string, companyId?: CompanyId) => [...keys.common(), 'dailySchedule', date, companyId],
  bankHistory: () => [...keys.bank(), 'bankHistory'],
  monthlySchedule: (month: Month, companyId?: CompanyId) => [...keys.common(), 'monthlySchedule', month, companyId],
  employmentContract: (crewId: CrewId) => [...keys.common(), 'employmentContract', crewId],
  bank: () => [...keys.common(), 'bank'],
  balance: () => [...keys.bank(), 'balance'],
  expectedExpenses: (companyId: CompanyId) => [...keys.common(), 'expectedExpenses', companyId],
  takeSubstitute: (scheduleId: ScheduleId) => [...keys.common(), 'takeSubstitute', scheduleId],
  checkAccountUser: (account: BankAccount) => [...keys.bank(), 'checkAccountUser', account],
  remittance: (account: BankAccount, money: string, password: Password) => [
    ...keys.bank(),
    'remittance',
    account,
    money,
    password,
  ],
  sendSalary: (crewId: CrewId, money: string) => [...keys.bank(), 'sendSalasy', crewId, money],
};

export default keys;
