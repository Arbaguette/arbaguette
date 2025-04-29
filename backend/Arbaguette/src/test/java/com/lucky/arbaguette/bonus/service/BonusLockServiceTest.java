package com.lucky.arbaguette.bonus.service;

import com.lucky.arbaguette.bonus.repository.BonusCrewRepository;
import com.lucky.arbaguette.common.domain.CustomUserDetails;
import com.lucky.arbaguette.common.domain.dto.CommonUserInfo;
import com.lucky.arbaguette.crew.domain.Crew;
import com.lucky.arbaguette.crew.repository.CrewRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
//@Transactional
class BonusLockServiceTest {

    private static final Logger log = LoggerFactory.getLogger(BonusLockServiceTest.class);

    @Autowired
    private BonusLockService bonusLockService;

    @Autowired
    private BonusCrewRepository bonusCrewRepository;

    @Autowired
    private CrewRepository crewRepository;

    @DisplayName("Crew1 : 300, Crew2 : 300, Crew3 : 300를 가져간다.")
    @Test
    void bonusLockServiceTest() throws InterruptedException {
        //given
        CustomUserDetails customUserDetails = new CustomUserDetails(
                new CommonUserInfo(
                        "arba2@naver.com",
                        "arbaguette",
                        "BOSS"
                )
        );
        int bonusId = bonusLockService.spreadBonus(customUserDetails, 900, 1);

        log.info("bonusId : {}", bonusId);

        List<CustomUserDetails> members = getCrews();

        //when
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failCount = new AtomicInteger(0);
        // 시간 측정 시작
        long startTime = System.nanoTime();
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        for (CustomUserDetails member : members) {
            executorService.submit(() -> {
                for (int j = 0; j < 3; j++) {
                    try {
                        bonusLockService.getBonus(member, bonusId);
//                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        log.error(e.getMessage());
//                        failCount.incrementAndGet();
                    }
                }
            });
        }
        // 모든 작업이 끝날 때까지 대기
        //새로운 작업을 더 이상 받지 않음
        executorService.shutdown();
        //shutdown() 이후, 스레드들이 모두 작업을 끝날 때까지 최대 10초 기다림
        boolean terminated = executorService.awaitTermination(10, TimeUnit.SECONDS);
        if (!terminated) {
            log.warn("스레드 작업이 시간 내에 끝나지 않았습니다.");
        }
        // 시간 측정 종료
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000; // 밀리초로 변환
        log.info("✅ 테스트 총 수행 시간: {}ms", durationMs);
        //then
        for (CustomUserDetails member : members) {
            Crew crew = crewRepository.findByEmail(member.getUsername()).get();
            log.info("crew : {}", crew.getCrewId());
            int money = bonusCrewRepository.findByIdBonusIdAndIdCrewId(bonusId, crew.getCrewId())
                        .get().getMoney();
            Assertions.assertThat(money).isEqualTo(300);

        }
    }

    private List<CustomUserDetails> getCrews() {
        List<CustomUserDetails> members = new ArrayList<>();

        members.add(new CustomUserDetails(
                new CommonUserInfo(
                        "crew1@naver.com",
                        "arbaguette",
                        "CREW"
                )
        ));

        members.add(new CustomUserDetails(
                new CommonUserInfo(
                        "crew2@naver.com",
                        "arbaguette",
                        "CREW"
                )
        ));

        members.add(new CustomUserDetails(
                new CommonUserInfo(
                        "crew3@naver.com",
                        "arbaguette",
                        "CREW"
                )
        ));

        return members;
    }

}