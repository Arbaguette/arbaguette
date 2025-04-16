package com.lucky.arbaguette.bonus.repository;

import com.lucky.arbaguette.bonus.domain.BonusCrew;
import com.lucky.arbaguette.bonus.domain.BonusCrewId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BonusCrewRepository extends JpaRepository<BonusCrew, BonusCrewId> {

    Optional<BonusCrew> findById_BonusIdAndId_CrewId(int bonusId, int crewId);
}
