package com.lucky.arbaguette.bonus.service;

import com.lucky.arbaguette.bonus.domain.Bonus;
import com.lucky.arbaguette.bonus.domain.BonusCrew;
import com.lucky.arbaguette.bonus.repository.BonusCrewRepository;
import com.lucky.arbaguette.bonus.repository.BonusRepository;
import com.lucky.arbaguette.boss.domain.Boss;
import com.lucky.arbaguette.boss.repository.BossRepository;
import com.lucky.arbaguette.common.domain.CustomUserDetails;
import com.lucky.arbaguette.common.exception.BadRequestException;
import com.lucky.arbaguette.common.exception.NotFoundException;
import com.lucky.arbaguette.common.service.BankService;
import com.lucky.arbaguette.crew.domain.Crew;
import com.lucky.arbaguette.crew.repository.CrewRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BonusLockService {

    private final BossRepository bossRepository;
    private final BankService bankService;
    private final BonusRepository bonusRepository;
    private final CrewRepository crewRepository;
    private final BonusCrewRepository bonusCrewRepository;

    @Transactional
    public int spreadBonus(CustomUserDetails customUserDetails, int money, int companyId) {
        Boss boss = bossRepository.findByEmail(customUserDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("사장님을 찾을 수 없습니다."));

        bankService.depositAccountWithdraw(boss, money);

        Bonus bonus = bonusRepository.save(Bonus.builder()
                .boss(boss)
                .money(money)
                .build());

        return bonus.getBonusId();
    }

    @Transactional
    public void getBonus(CustomUserDetails customUserDetails, int bonusId) {
        Crew crew = crewRepository.findByEmail(customUserDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("알바생을 찾을 수 없습니다."));

        Bonus bonus = bonusRepository.findById(bonusId)
                .orElseThrow(() -> new NotFoundException("보너스를 찾을 수 없습니다."));

        if (bonus.isRemain()) {
            bonus.decrementMoney();
            Optional<BonusCrew> bonusCrew = bonusCrewRepository.findById_BonusIdAndId_CrewId(bonus.getBonusId(),
                    crew.getCrewId());

            if (bonusCrew.isPresent()) {
                bonusCrew.get().incrementMoney();
            } else {
                bonusCrewRepository.save(
                        BonusCrew.builder()
                                .bonus(bonus)
                                .crew(crew)
                                .build()
                );
            }
        } else {
            throw new BadRequestException("종료된 이벤트입니다.");
        }
    }

}
