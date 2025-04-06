package com.lucky.arbaguette.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EncryptUtil {

    private final AesBytesEncryptor aesBytesEncryptor;

    public String encryptToString(String target){
        byte[] encryptedBytes = aesBytesEncryptor.encrypt(target.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decryptToString(String encryptedBase64){
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
        byte[] decryptedBytes = aesBytesEncryptor.decrypt(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
