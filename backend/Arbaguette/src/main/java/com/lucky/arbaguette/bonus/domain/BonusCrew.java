package com.lucky.arbaguette.bonus.domain;

import com.lucky.arbaguette.crew.domain.Crew;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BonusCrew {
    
    @EmbeddedId
    private BonusCrewId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bonus_id")
    @MapsId("bonusId")
    private Bonus bonus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id")
    @MapsId("crewId")
    private Crew crew;

    private int money;

    public void incrementMoney() {
        money += 100;
    }

    @Builder
    public BonusCrew(Bonus bonus, Crew crew) {
        this.id = new BonusCrewId(bonus.getBonusId(), crew.getCrewId());
        this.bonus = bonus;
        this.crew = crew;
        this.money = 100;
    }
}
