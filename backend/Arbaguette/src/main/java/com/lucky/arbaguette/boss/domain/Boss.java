package com.lucky.arbaguette.boss.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Boss {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bossId;

    private String email;

    private String password;

    private String name;

    private String tel;

    private String account;

    private String userKey;

    private int profileImage;

    @Builder
    private Boss(String email, String password, String name, String tel, String account, String userKey, int profileImage) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.tel = tel;
        this.account = account;
        this.userKey = userKey;
        this.profileImage = profileImage;
    }

}
