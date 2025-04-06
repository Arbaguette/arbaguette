package com.lucky.arbaguette.company.dto;

import com.lucky.arbaguette.boss.domain.Boss;
import com.lucky.arbaguette.common.util.EncryptUtil;
import com.lucky.arbaguette.company.domain.Company;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;

public record CompanyInfo(String name,
                          String address,
                          String representative) {

    public Company toCompany(Boss boss, EncryptUtil encryptUtil){
        return Company.builder()
                .boss(boss)
                .name(encryptUtil.encryptToString(this.name))
                .address(encryptUtil.encryptToString(this.address))
                .representative(encryptUtil.encryptToString(this.representative))
                .build();
    }


}
