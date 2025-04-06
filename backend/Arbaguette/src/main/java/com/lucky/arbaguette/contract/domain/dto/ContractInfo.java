package com.lucky.arbaguette.contract.domain.dto;

import com.lucky.arbaguette.common.util.EncryptUtil;
import com.lucky.arbaguette.company.domain.Company;
import com.lucky.arbaguette.contract.domain.Contract;
import com.lucky.arbaguette.contract.domain.TaxType;
import com.lucky.arbaguette.contractworkingday.domain.dto.WorkingDayInfo;
import com.lucky.arbaguette.crew.domain.Crew;

import java.time.LocalDate;
import java.util.List;

public record ContractInfo(int contractId,
                           String companyName,
                           String address,
                           String representative,
                           String crewName,
                           String tel,
                           LocalDate startDate,
                           LocalDate endDate,
                           List<WorkingDayInfo> workingDayInfoList,
                           int salary,
                           int salaryDate,
                           TaxType tax,
                           String bossSign,
                           String crewSign,
                           String url) {

    public static ContractInfo from(Company company, Crew crew, Contract contract,
        List<WorkingDayInfo> workingDayInfoList, EncryptUtil encryptUtil) {
        return new ContractInfo(
                contract.getContractId(),
                encryptUtil.decryptToString(company.getName()),
                encryptUtil.decryptToString(company.getAddress()),
                encryptUtil.decryptToString(company.getRepresentative()),
                crew.getName(),
                crew.getTel(),
                contract.getStartDate(),
                contract.getEndDate(),
                workingDayInfoList,
                contract.getSalary(),
                contract.getSalaryDate(),
                contract.getTax(),
                contract.getBossSign(),
                contract.getCrewSign(),
                contract.getUrl()
        );
    }

}
