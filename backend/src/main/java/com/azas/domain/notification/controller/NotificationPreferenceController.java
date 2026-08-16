package com.azas.domain.notification.controller;

import com.azas.domain.notification.dto.NotificationPreferenceListResponse;
import com.azas.domain.notification.dto.UpdateNotificationPreferencesRequest;
import com.azas.domain.notification.service.NotificationPreferenceService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api(tags = "알림")
@RestController
@RequestMapping("/api/v1/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceService
            notificationPreferenceService;

    private final AccessTokenMemberResolver
            accessTokenMemberResolver;

    @ApiOperation(
            value = "알림 유형별 수신 설정 조회",
            notes = "현재 로그인한 회원의 알림 카테고리별 수신 설정을 조회합니다."
    )
    @GetMapping
    public ResponseEntity<NotificationPreferenceListResponse>
    getNotificationPreferences(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader
    ) {
        Long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        NotificationPreferenceListResponse response =
                notificationPreferenceService
                        .getNotificationPreferences(memberId);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response);
    }

    @ApiOperation(
            value = "알림 유형별 수신 설정 저장",
            notes = "현재 로그인한 회원의 알림 카테고리별 수신 여부를 일괄 저장합니다."
    )
    @PutMapping
    public ResponseEntity<NotificationPreferenceListResponse>
    updateNotificationPreferences(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader,

            @Valid
            @RequestBody
            UpdateNotificationPreferencesRequest request
    ) {
        Long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        NotificationPreferenceListResponse response =
                notificationPreferenceService
                        .updateNotificationPreferences(
                                memberId,
                                request
                        );

        return ResponseEntity.ok(response);
    }
}