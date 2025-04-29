package com.lucky.arbaguette.bonus.domain;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BonusCrewId implements Serializable {

    private int bonusId;
    private int crewId;

    public BonusCrewId(int bonusId, int crewId) {
        this.bonusId = bonusId;
        this.crewId = crewId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BonusCrewId that = (BonusCrewId) o;
        return getBonusId() == that.getBonusId() && getCrewId() == that.getCrewId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBonusId(), getCrewId());
    }

}
