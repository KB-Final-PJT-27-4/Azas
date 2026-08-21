package com.azas.domain.notification.controller;

import com.azas.domain.notification.dto.PushDeviceRegistrationResult;
import com.azas.domain.notification.dto.PushDeviceResponse;
import com.azas.domain.notification.dto.RegisterPushDeviceRequest;
import com.azas.domain.notification.service.PushDeviceService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "푸시 알림")
@RestController
@RequestMapping("/api/v1/push-devices")
@RequiredArgsConstructor
public class PushDeviceController {

    private final PushDeviceService pushDeviceService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(
            value = "푸시 기기 등록 또는 토큰 갱신",
            notes = "동일한 device_key가 이미 있으면 FCM 토큰과 기기 정보를 갱신합니다."
    )
    @PostMapping
    public ResponseEntity<PushDeviceResponse> register(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader,
            @Valid @RequestBody RegisterPushDeviceRequest request
    ) {
        Long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        PushDeviceRegistrationResult result =
                pushDeviceService.register(memberId, request);

        HttpStatus status = result.isCreated()
                ? HttpStatus.CREATED
                : HttpStatus.OK;

        return ResponseEntity.status(status)
                .body(result.getResponse());
    }

    @ApiOperation(
            value = "푸시 기기 해제",
            notes = "로그아웃하거나 푸시 권한을 해제한 기기를 비활성화합니다."
    )
    @DeleteMapping("/{push_device_id}")
    public ResponseEntity<Void> unregister(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader,
            @PathVariable("push_device_id") Long pushDeviceId
    ) {
        Long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        pushDeviceService.unregister(memberId, pushDeviceId);
        return ResponseEntity.noContent().build();
    }
}
