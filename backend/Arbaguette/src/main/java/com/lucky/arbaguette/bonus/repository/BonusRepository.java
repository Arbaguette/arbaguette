package com.lucky.arbaguette.bonus.repository;

import com.lucky.arbaguette.bonus.domain.Bonus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BonusRepository extends JpaRepository<Bonus, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Bonus b WHERE b.bonusId = :bonusId")
    Optional<Bonus> findByIdForUpdate(@Param("bonusId") Integer bonusId);

}
