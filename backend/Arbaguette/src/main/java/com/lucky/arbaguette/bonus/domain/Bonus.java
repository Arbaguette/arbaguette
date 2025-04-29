package com.lucky.arbaguette.bonus.domain;

import com.lucky.arbaguette.boss.domain.Boss;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Bonus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bonusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boss_id")
    private Boss boss;

    private int money;

    @Builder
    private Bonus(Boss boss, int money) {
        this.boss = boss;
        this.money = money;
    }

    public boolean isRemain() {
        return money >= 100;
    }

    public void decrementMoney() {
        money -= 100;
    }

}
