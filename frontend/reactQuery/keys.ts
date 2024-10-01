const keys = {
  all: ['arbaguette'] as const,
  boss: () => [...keys.all, 'boss'],
  company: () => [...keys.boss(), 'company'],
  daySchedule: (date: string, companyId: CompanyId) => [...keys.company(), 'schedule', date, companyId],
  companyList: () => [...keys.company(), 'list'],
  crew: () => [...keys.all, 'crew'],
  crewList: () => [...keys.boss(), 'crews'],
  crewDetail: (crewId: CrewId) => [...keys.crew(), 'detail', crewId],
  nearCommuteInfo: () => [...keys.crew(), 'nearCommuteInfo'],
  salary: () => [...keys.crew(), 'salary'],
  payStub: (month: Month) => [...keys.salary(), 'payStub', month],
  accumulatedSalary: () => [...keys.salary(), 'accumulatedSalary'],
  estimatedSalary: () => [...keys.salary(), 'estimatedSalary'],
  common: () => [...keys.all, 'common'],
  email: (email: Email) => [...keys.common(), 'email', email],
  bank: () => [...keys.common(), 'bank'],
  balance: () => [...keys.bank(), 'balance'],
  dailySchedule: (date: string, companyId?: CompanyId) => [...keys.common(), 'dailySchedule', date, companyId],
  bankHistory: () => [...keys.bank(), 'bankHistory'],
};

export default keys;
