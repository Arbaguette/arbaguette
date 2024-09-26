package com.lucky.arbaguette.substitute.service;

import com.lucky.arbaguette.common.domain.CustomUserDetails;
import com.lucky.arbaguette.common.exception.BadRequestException;
import com.lucky.arbaguette.common.exception.DuplicateException;
import com.lucky.arbaguette.common.exception.NotFoundException;
import com.lucky.arbaguette.crew.domain.Crew;
import com.lucky.arbaguette.crew.repository.CrewRepository;
import com.lucky.arbaguette.schedule.domain.Schedule;
import com.lucky.arbaguette.schedule.repository.ScheduleRepository;
import com.lucky.arbaguette.substitute.domain.Substitute;
import com.lucky.arbaguette.substitute.dto.request.SubstituteRequest;
import com.lucky.arbaguette.substitute.dto.response.SubstituteSaveResponse;
import com.lucky.arbaguette.substitute.dto.response.SubstitutesResponse;
import com.lucky.arbaguette.substitute.repository.SubstituteRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SubstituteService {

    private final SubstituteRepository substituteRepository;
    private final CrewRepository crewRepository;
    private final ScheduleRepository scheduleRepository;

    public SubstituteSaveResponse saveSubstitute(CustomUserDetails userDetails, SubstituteRequest request) {
        Crew crew = crewRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("알바생을 찾을 수 없습니다."));
        Schedule schedule = scheduleRepository.findByScheduleIdAndCrew(request.scheduleId(), crew)
                .orElseThrow(() -> new NotFoundException("스케줄을 찾을 수 없습니다."));

        if (schedule.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("이미 지난 날짜는 요청할 수 없습니다.");
        }

        if (substituteRepository.existsBySchedule(schedule)) {
            throw new DuplicateException("이미 대타 신청이 완료되었습니다.");
        }

        Substitute subStitute = substituteRepository.save(Substitute.builder()
                .schedule(schedule)
                .permit(false)
                .companyId(crew.getCompany().getCompanyId())
                .build());
        return SubstituteSaveResponse.of(subStitute);
    }

    public SubstitutesResponse getSubstitutes(CustomUserDetails userDetails, int companyId) {
        //companyId를 가지고 있는 알바생을 먼저 찾음
        //해당 알바생을 가지고 있는 스케줄을 찾음
        //해당 스케줄을 가지고 있는 대타를 찾음 (이 때, permit 이 false 인 것만 찾음)
        return null;
    }
}
