package com.lucky.arbaguette.common.service;

import com.lucky.arbaguette.boss.domain.Boss;
import com.lucky.arbaguette.boss.repository.BossRepository;
import com.lucky.arbaguette.common.domain.CustomUserDetails;
import com.lucky.arbaguette.common.domain.TokenRedis;
import com.lucky.arbaguette.common.domain.dto.request.UserJoinRequest;
import com.lucky.arbaguette.common.domain.dto.response.LoginTokenResponse;
import com.lucky.arbaguette.common.domain.dto.response.UserInfoResponse;
import com.lucky.arbaguette.common.exception.BadRequestException;
import com.lucky.arbaguette.common.exception.DuplicateException;
import com.lucky.arbaguette.common.exception.NotFoundException;
import com.lucky.arbaguette.common.jwt.JWTUtil;
import com.lucky.arbaguette.common.repository.TokenRedisRepository;
import com.lucky.arbaguette.common.util.S3Util;
import com.lucky.arbaguette.crew.domain.Crew;
import com.lucky.arbaguette.crew.repository.CrewRepository;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.lucky.arbaguette.common.domain.enums.UserRole.BOSS;
import static com.lucky.arbaguette.common.domain.enums.UserRole.CREW;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final BossRepository bossRepository;
    private final CrewRepository crewRepository;
    private final TokenRedisRepository tokenRedisRepository;
    private final WebClient webClient; // WebClient 주입git
    private final JWTUtil jwtUtil;
    private final S3Util s3Util;

    @Value("${finopenapi.url}")
    private String financialApiUrl;

    @Value("${finopenapi.key}")
    private String financialApiKey;

    @Value("${token.access.expired.time}")
    private long accessTokenExpiredTime;

    @Value("${token.refresh.expired.time}")
    private long refreshTokenExpiredTime;

    public void checkEmail(String email) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("userId", email);
        requestBody.put("apiKey", financialApiKey);
        try {
            webClient.post()
                    .uri(financialApiUrl + "/v1/member/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            return;
        }
        throw new DuplicateException("이메일이 중복되었습니다");
    }

    public void checkTel(String tel) {
        if (bossRepository.existsByTel(tel) || crewRepository.existsByTel(tel)) {
            throw new DuplicateException("전화번호가 중복되었습니다.");
        }
    }

    public void joinProcess(UserJoinRequest joinRequest, MultipartFile file) throws IOException {

        // 아이디 중복 확인
        checkEmail(joinRequest.getEmail());

        // 전화번호 중복 확인
        checkTel(joinRequest.getTel());

        // userKey 발급 요청
        Map<String, String> userKeyBody = new HashMap<>();
        userKeyBody.put("userId", joinRequest.getEmail());
        userKeyBody.put("apiKey", financialApiKey);

        Map<String, Object> responseBody = webClient.post()
                .uri(financialApiUrl + "/v1/member/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(userKeyBody))
                .retrieve()
                .bodyToMono(Map.class)
                .block();// 결과를 기다림

        log.info("Response Body: {}", responseBody);
        String userKey = responseBody.get("userKey").toString();

        // 계좌 생성 요청
        Map<String, Object> accountRequestBody = new HashMap<>();
        Map<String, String> headerMap = new HashMap<>();

        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String formattedDate = formatter.format(today);

        SimpleDateFormat timeFormatter = new SimpleDateFormat("HHmmss");
        String formattedTime = timeFormatter.format(today);

        headerMap.put("apiName", "createDemandDepositAccount");
        headerMap.put("transmissionDate", formattedDate); // 실제 날짜와 시간을 넣어야 함
        headerMap.put("transmissionTime", formattedTime);
        headerMap.put("institutionCode", "00100");
        headerMap.put("fintechAppNo", "001");
        headerMap.put("apiServiceCode", "createDemandDepositAccount");
        headerMap.put("institutionTransactionUniqueNo", formattedDate + formattedTime + "000000"); // 유일한 값 필요
        headerMap.put("apiKey", financialApiKey);
        headerMap.put("userKey", userKey);

        accountRequestBody.put("Header", headerMap);
        accountRequestBody.put("accountTypeUniqueNo", "001-1-dde34ab0a5c249");

        // WebClient를 사용한 계좌 생성 요청
        Map<String, Map<String, Object>> accountResponseBody = webClient.post()
                .uri(financialApiUrl + "/v1/edu/demandDeposit/createDemandDepositAccount")
                .body(BodyInserters.fromValue(accountRequestBody))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Map<String, Object> rec = accountResponseBody.get("REC");
        String account = rec.get("accountNo").toString();

        // 사장님 회원가입
        if (BOSS.equals(joinRequest.getRole())) {
            Boss boss = joinRequest.toBoss(bCryptPasswordEncoder, account, userKey, s3Util.upload(file));
            bossRepository.save(boss);
        }

        // 알바생 회원가입
        if (CREW.equals(joinRequest.getRole())) {
            Crew crew = joinRequest.toCrew(bCryptPasswordEncoder, account, userKey, s3Util.upload(file));
            crewRepository.save(crew);
        }
    }


    public LoginTokenResponse reissue(String refreshToken) {
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new BadRequestException("refresh token expired");
        }

        if (!"refresh".equals(jwtUtil.getCategory(refreshToken))) {
            throw new BadRequestException("invalid refresh token");
        }

        String email = jwtUtil.getEmail(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        if (!tokenRedisRepository.existsBy(email)) {
            throw new BadRequestException("invalid refresh token");
        }

        String access = jwtUtil.createJwt("access", email, role, getCrewStatus(email, role),
                getCrewId(email, role));
        String refresh = jwtUtil.createJwt("refresh", email, role, getCrewStatus(email, role),
                getCrewId(email, role));

        tokenRedisRepository.deleteBy(email);
        saveRefreshEntity(email, refresh);

        return LoginTokenResponse.of(access, refresh);
    }

    public UserInfoResponse info(CustomUserDetails customUserDetails) {
        return new UserInfoResponse(customUserDetails.getUsername(), customUserDetails.getRole());
    }

    private String getCrewStatus(String email, String role) {
        if ("BOSS".equals(role)) {
            return null;
        }
        return crewRepository.findByEmail(email)
                .orElseThrow(null)
                .getCrewStatus().name();
    }

    private int getCrewId(String email, String role) {
        if ("BOSS".equals(role)) {
            return -1;
        }
        return crewRepository.findByEmail(email)
                .orElseThrow(null)
                .getCrewId();
    }

    private void saveRefreshEntity(String email, String refreshToken) {
        tokenRedisRepository.save(
                TokenRedis.builder()
                        .email(email)
                        .refreshToken(refreshToken)
                        .build()
        );
    }

    public boolean checkAccountPassword(CustomUserDetails customUserDetails, String accountPassword) {
        if (customUserDetails.isBoss()) {
            Boss boss = bossRepository.findByEmail(customUserDetails.getUsername()).orElseThrow(() -> new NotFoundException("회원정보를 찾을 수 없습니다."));
            return bCryptPasswordEncoder.matches(accountPassword, boss.getAccountPassword());
        }
        Crew crew = crewRepository.findByEmail(customUserDetails.getUsername()).orElseThrow(() -> new NotFoundException("회원정보를 찾을 수 없습니다."));
        return bCryptPasswordEncoder.matches(accountPassword, crew.getAccountPassword());
    }

    @Transactional
    public void saveExpoToken(CustomUserDetails userDetails, String expoPushToken) {
        if (userDetails.isBoss()) {
            bossRepository.findByEmail(userDetails.getUsername()).get().saveExpoPushToken(expoPushToken);
        } else {
            crewRepository.findByEmail(userDetails.getUsername()).get().saveExpoPushToken(expoPushToken);
        }
    }
}
