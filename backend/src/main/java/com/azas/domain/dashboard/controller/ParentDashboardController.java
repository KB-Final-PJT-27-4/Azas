package com.azas.domain.dashboard.controller;

import com.azas.domain.dashboard.dto.ParentDashboardResponse;
import com.azas.domain.dashboard.service.ParentDashboardService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "대시보드")
@RestController
@RequestMapping("/api/v1/children")
@RequiredArgsConstructor
public class ParentDashboardController {

    private final AccessTokenMemberResolver accessTokenMemberResolver;
    private final ParentDashboardService parentDashboardService;

    @ApiOperation("부모용 자녀 홈 대시보드 조회")
    @GetMapping("/{childId}/dashboard")
    public ResponseEntity<ParentDashboardResponse> getDashboard(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long childId
    ) {
        Long memberId =
                accessTokenMemberResolver.resolveMemberId(authorization);

        ParentDashboardResponse response =
                parentDashboardService.getDashboard(memberId, childId);

        return ResponseEntity.ok(response);
    }
}