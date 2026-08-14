package com.azas.domain.allowance.controller;

import com.azas.domain.allowance.dto.AllowanceRequestResponse;
import com.azas.domain.allowance.dto.CreateAllowanceRequest;
import com.azas.domain.allowance.service.AllowanceRequestService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "용돈 요청")
@RestController
@RequestMapping("/api/v1/children/me/allowance-requests")
@RequiredArgsConstructor
public class AllowanceRequestController {

    private final AllowanceRequestService allowanceRequestService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation("자녀 본인 용돈 요청")
    @PostMapping
    public ResponseEntity<AllowanceRequestResponse> createAllowanceRequest(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader,
            @Valid @RequestBody CreateAllowanceRequest request
    ) {
        long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        AllowanceRequestResponse response =
                allowanceRequestService.createAllowanceRequest(
                        memberId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}