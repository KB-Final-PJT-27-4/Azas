package com.azas.domain.child.controller;

import com.azas.domain.child.dto.PregnancyStatusResponse;
import com.azas.domain.child.service.PregnancyStatusService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "자녀")
@RestController
@RequestMapping("/api/v1/children")
@RequiredArgsConstructor
public class PregnancyStatusController {

    private final PregnancyStatusService
            pregnancyStatusService;

    private final AccessTokenMemberResolver
            accessTokenMemberResolver;

    @ApiOperation(
            value = "임신 주차 및 캐릭터 조회",
            notes = "출산 예정일과 현재 날짜를 기준으로 임신 주차와 캐릭터를 조회합니다."
    )
    @GetMapping("/{child_id}/pregnancy-status")
    public ResponseEntity<PregnancyStatusResponse>
    getPregnancyStatus(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader,

            @PathVariable("child_id")
            Long childId
    ) {
        Long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        PregnancyStatusResponse response =
                pregnancyStatusService.getPregnancyStatus(
                        memberId,
                        childId
                );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response);
    }
}