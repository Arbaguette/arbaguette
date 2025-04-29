package com.lucky.arbaguette.bonus.controller;

import com.lucky.arbaguette.bonus.dto.request.BonusGetRequest;
import com.lucky.arbaguette.bonus.dto.request.BonusSaveRequest;
import com.lucky.arbaguette.bonus.service.BonusLockService;
import com.lucky.arbaguette.bonus.service.BonusService;
import com.lucky.arbaguette.common.ApiResponse;
import com.lucky.arbaguette.common.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bonus")
public class BonusController {

    private final BonusService bonusService;
    private final BonusLockService bonusLockService;

    @PostMapping
    public ApiResponse<Void> spreadBonus(@AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody BonusSaveRequest request) {
        //bonusLockService.spreadBonus(customUserDetails, request.money(), request.companyId());
        bonusService.spreadBonus(customUserDetails, request.money(), request.companyId());
        return ApiResponse.success();
    }

    @PostMapping("/crew")
    public ApiResponse<Void> getBonus(@AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody BonusGetRequest request) {
        //bonusLockService.getBonus(customUserDetails, request.bonusId());
        bonusService.getBonus(customUserDetails, request.bonusId());
        return ApiResponse.success();
    }
}
