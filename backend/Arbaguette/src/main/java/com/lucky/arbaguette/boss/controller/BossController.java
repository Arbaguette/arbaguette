package com.lucky.arbaguette.boss.controller;

import com.lucky.arbaguette.boss.dto.request.CrewSaveRequest;
import com.lucky.arbaguette.boss.dto.request.ReceiptSendRequest;
import com.lucky.arbaguette.boss.dto.response.CrewSaveResponse;
import com.lucky.arbaguette.boss.dto.response.ExpectedCostResponse;
import com.lucky.arbaguette.boss.service.BossService;
import com.lucky.arbaguette.common.ApiResponse;
import com.lucky.arbaguette.common.domain.CustomUserDetails;
import com.lucky.arbaguette.crew.domain.dto.response.CrewDetailResponse;
import com.lucky.arbaguette.crew.domain.dto.response.CrewListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boss")
@RequiredArgsConstructor
@Slf4j
public class BossController {

    private final BossService bossService;

    @PostMapping("/crew")
    public ApiResponse<CrewSaveResponse> saveCrew(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                  @RequestBody CrewSaveRequest crewSaveRequest) {
        return ApiResponse.success(bossService.saveCrew(customUserDetails, crewSaveRequest));
    }

    @GetMapping("/expected")
    public ApiResponse<ExpectedCostResponse> getExpectedCost(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestParam int companyId) {
        return ApiResponse.success(bossService.getExpectedCost(customUserDetails, companyId));
    }

    @GetMapping("/crews")
    public ApiResponse<CrewListResponse> getCrews(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestParam int companyId) {
        return ApiResponse.success(bossService.getCrews(customUserDetails, companyId));
    }

    @GetMapping("/crew")
    public ApiResponse<CrewDetailResponse> getCrewDetails(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                          @RequestParam int crewId) {
        return ApiResponse.success(bossService.getCrewDetails(customUserDetails, crewId));
    }

    @PostMapping("/receipt")
    public ApiResponse<Void> sendReceipt(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                         @RequestBody ReceiptSendRequest receiptSendRequest) {
        bossService.sendReceipt(customUserDetails, receiptSendRequest);
        return ApiResponse.success();
    }

    @DeleteMapping("/crew")
    public ApiResponse<Void> deleteCrew(@RequestBody CrewSaveRequest crewSaveRequest) {
        bossService.deleteCrew(crewSaveRequest);
        return ApiResponse.success();
    }

}
