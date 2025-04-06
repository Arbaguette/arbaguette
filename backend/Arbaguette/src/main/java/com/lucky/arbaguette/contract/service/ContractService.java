package com.lucky.arbaguette.contract.service;

import com.lucky.arbaguette.common.domain.CustomUserDetails;
import com.lucky.arbaguette.common.exception.BadRequestException;
import com.lucky.arbaguette.common.exception.DuplicateException;
import com.lucky.arbaguette.common.exception.NotFoundException;
import com.lucky.arbaguette.common.exception.UnAuthorizedException;
import com.lucky.arbaguette.common.service.NotificationService;
import com.lucky.arbaguette.common.util.EncryptUtil;
import com.lucky.arbaguette.common.util.S3Util;
import com.lucky.arbaguette.company.domain.Company;
import com.lucky.arbaguette.company.repository.CompanyRepository;
import com.lucky.arbaguette.contract.Repository.ContractRepository;
import com.lucky.arbaguette.contract.domain.Contract;
import com.lucky.arbaguette.contract.domain.dto.ContractInfo;
import com.lucky.arbaguette.contract.domain.dto.ContractSaveRequest;
import com.lucky.arbaguette.contractworkingday.domain.ContractWorkingDay;
import com.lucky.arbaguette.contractworkingday.domain.dto.WorkingDayInfo;
import com.lucky.arbaguette.contractworkingday.repository.ContractWorkingDayRepository;
import com.lucky.arbaguette.crew.domain.Crew;
import com.lucky.arbaguette.crew.repository.CrewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ContractService {

    private final CompanyRepository companyRepository;
    private final CrewRepository crewRepository;
    private final ContractRepository contractRepository;
    private final ContractWorkingDayRepository contractWorkingDayRepository;
    private final S3Util s3Util;
    private final NotificationService notificationService;
    private final PdfUtil pdfUtil;
    private final EncryptUtil encryptUtil;

    @Transactional
    public void saveContract(CustomUserDetails customUserDetails, ContractSaveRequest contractSaveRequest,
                             MultipartFile file) throws IOException {
        Company company = companyRepository.findByCompanyIdAndBoss_Email(contractSaveRequest.companyId(),
                customUserDetails.getUsername()).orElseThrow(() -> new UnAuthorizedException("사업장을 찾을 수 없습니다."));
        Crew crew = crewRepository.findById(contractSaveRequest.crewId())
                .orElseThrow(() -> new NotFoundException("알바생을 찾을 수 없습니다."));
        if (file.isEmpty()) {
            throw new BadRequestException("서명이 비었습니다.");
        }
        Contract contract = contractSaveRequest.toContract(company, crew, s3Util.upload(file));
        contractRepository.save(contract);

        //근로계약서에 따라 근무계약일 생성
        contractSaveRequest.workingDayInfoList().forEach(workingDayInfo -> {
            ContractWorkingDay contractWorkingDay = createContractWorkingDay(contract, workingDayInfo);
            contractWorkingDayRepository.save(contractWorkingDay);
        });

        //알림 전송
        notificationService.sendNotification(
                crew.getExpoPushToken(),
                "근로계약서",
                crew.getName() + "님, 근로계약서가 도착했어요 ! 확인 후 서명해주세요.",
                "arbaguette://crew/unauthorized/contract"
        );
    }

    private ContractWorkingDay createContractWorkingDay(Contract contract, WorkingDayInfo workingDayInfo) {
        if (contractWorkingDayRepository.existsByContractAndId_Weekday(contract, workingDayInfo.weekday())) {
            throw new DuplicateException("근무요일이 겹칩니다.");
        }
        return ContractWorkingDay.builder()
                .contract(contract)
                .startTime(workingDayInfo.startTime())
                .endTime(workingDayInfo.endTime())
                .weekday(workingDayInfo.weekday())
                .build();
    }

    @Transactional
    public void signCrewContract(CustomUserDetails customUserDetails, MultipartFile file) throws IOException {
        Crew crew = crewRepository.findByEmail(customUserDetails.getUsername())
                .orElseThrow(() -> new UnAuthorizedException("알바생을 찾을 수 없습니다."));
        Contract contract = contractRepository.findByCrew(crew)
                .orElseThrow(() -> new NotFoundException("근로계약서를 찾을 수 없습니다."));
        if (contract.alreadySigned()) {
            throw new DuplicateException("이미 서명했습니다.");
        }
        if (file.isEmpty()) {
            throw new BadRequestException("서명이 비었습니다.");
        }
        contract.signCrew(s3Util.upload(file));
        //계약서 정보를 바탕으로 pdf 생성
        String htmlContent = generateContractHtml(contract);
        byte[] pdfData = pdfUtil.generatePdfFromHtml(htmlContent);
        contract.makeContractPdf(s3Util.uploadPdf(pdfData, "contract_" + contract.getContractId() + ".pdf"));
        //알바생 상태 변경, UNSIGNED->SINGED
        crew.signComplete();
    }

    public ContractInfo getContract(CustomUserDetails customUserDetails, int crewId) {
        Crew crew = crewRepository.findById(crewId).orElseThrow(() -> new BadRequestException("알바생을 찾을 수 없습니다."));
        Contract contract = contractRepository.findByCrew(crew).orElseThrow(() -> new NotFoundException("근로계약서를 찾을 수 없습니다."));
        List<WorkingDayInfo> workingDayInfos = contractWorkingDayRepository.findAllByContract(contract).stream()
                .map(WorkingDayInfo::to)
                .toList();
        return ContractInfo.from(crew.getCompany(), crew, contract, workingDayInfos, encryptUtil);
    }

    public String generateContractHtml(Contract contract) {
        List<ContractWorkingDay> contractWorkingDays = contractWorkingDayRepository.findAllByContract(contract);
        StringBuilder workingDayInfoBuilder = new StringBuilder();
        for (ContractWorkingDay workingDay : contractWorkingDays) {
            //숫자를 한글로 변환
            String day = getDayOfWeek(workingDay.getId().getWeekday());
            workingDayInfoBuilder.append(String.format(
                    "<tr><td>근무 요일: %s</td><td>근무 시간: %s ~ %s</td></tr>",
                    day,  // 근무 요일 (0 = 월요일, 1 = 화요일 ...6 = 일요일)
                    workingDay.getStartTime(), // 근무 시작 시간
                    workingDay.getEndTime() // 근무 종료 시간
            ));
        }
        return """
                    <html>
                    <head>
                    <meta charset="UTF-8"/>
                        <style>
                            @import url('https://fonts.googleapis.com/css2?family=Nanum+Gothic&amp;display=swap');
                            body {
                                font-family: 'Nanum Gothic', Arial, sans-serif;
                                line-height: 1.6;
                            }
                            h1 {
                                text-align: center;
                                font-size: 24px;
                                font-weight: bold;
                                margin-bottom: 20px;
                            }
                            .contract {
                                width: 100%%;
                                border-collapse: collapse;
                            }
                            .contract td {
                                border: 1px solid black;
                                padding: 8px;
                            }
                            .signature {
                                margin-top: 30px;
                            }
                            .signature td {
                                border: none;
                            }
                            .signature img {
                                width: 150px;
                                height: auto;
                            }
                            .info {
                                margin-bottom: 20px;
                            }
                            .contract-date {
                                    text-align: center;
                                    font-size: 30px;
                                    font-weight: bold;
                                    position: fixed;
                                    left: 50%%;
                                    bottom: 20px;
                                    transform: translateX(-50%%)
                                }
                        </style>
                    </head>
                    <body>
                        <h1>표준근로계약서</h1>

                        <p><strong>%s</strong> (이하 "<strong>갑</strong>"이라 함)과 <strong>%s</strong> (이하 "<strong>을</strong>"이라 함)은 다음과 같이 근로계약을 체결한다.</p>
                        
                        <table class="contract">
                            <tr>
                                <td>1. 근로계약기간</td>
                                <td>%s부터</td>
                                <td>%s까지</td>
                            </tr>
                            <tr>
                                <td>2. 근무 장소</td>
                                <td colspan="2">%s</td>
                            </tr>
                            <tr>
                                <td>3. 근무시간</td>
                                <td colspan="2">
                                    <table>%s</table>
                                </td>
                            </tr>
                            <tr>
                                <td>4. 임금</td>
                                <td colspan="2">시급(일, 시간급): %d원</td>
                            </tr>
                            <tr>
                                <td>5. 연차유급휴가</td>
                                <td colspan="2">근로기준법에서 정하는 바에 따라 부여함</td>
                            </tr>
                            <tr>
                                <td>6. 근로계약서 교부</td>
                                <td colspan="2">서명과 함께 교부함</td>
                            </tr>
                            <tr>
                                <td>7. 기타</td>
                                <td colspan="2">근로기준법에 의함</td>
                            </tr>
                        </table>

                        <table class="signature">
                            <tr>
                                    <td>(갑) 사업자명: %s</td>
                                </tr>
                                <tr>
                                    <td>전화: %s</td>
                                </tr>
                                <tr>
                                    <td>대표자: %s</td>
                                </tr>
                                <tr>
                                    <td>서명: <img src="%s" alt="대표자 서명"/></td>
                                </tr>
                                <tr>
                                    <td>(을) 근로자명: %s</td>
                                </tr>
                                <tr>
                                    <td>전화: %s</td>
                                </tr>
                                <tr>
                                    <td>서명: <img src="%s" alt="근로자 서명"/></td>
                                </tr>
                        </table>

                        <p class="contract-date">%s</p>

                    </body>
                    </html>
                """.formatted(
                contract.getCompany().getRepresentative(),// 사업장 이름
                contract.getCrew().getName(),// 알바생 이름
                contract.getStartDate(),// 계약 시작일
                contract.getEndDate(),// 계약 종료일
                contract.getCompany().getAddress(),// 근무 장소
                workingDayInfoBuilder.toString(),//근무일
                contract.getSalary(),// 시급
                contract.getCompany().getName(),// 사업장명
                contract.getCompany().getBoss().getTel(),// 사업장 전화번호
                contract.getCompany().getRepresentative(),// 대표자 이름
                contract.getBossSign(),
                contract.getCrew().getName(),// 알바생 이름
                contract.getCrew().getTel(),// 알바생 전화번호
                contract.getCrewSign(),
                LocalDate.now() // 계약일 (현재 날짜)
        );
    }

    public String getDayOfWeek(int weekday) {
        return switch (weekday) {
            case 0 -> "월요일";
            case 1 -> "화요일";
            case 2 -> "수요일";
            case 3 -> "목요일";
            case 4 -> "금요일";
            case 5 -> "토요일";
            case 6 -> "일요일";
            default -> throw new IllegalArgumentException("Invalid weekday: " + weekday);
        };
    }


}
