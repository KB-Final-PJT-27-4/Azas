package com.azas.domain.allowance.controller;

import com.azas.domain.allowance.dto.*;
import com.azas.domain.allowance.service.AllowanceRequestService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "용돈 요청")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AllowanceRequestController {

    private final AllowanceRequestService allowanceRequestService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation("자녀 본인 용돈 요청")
    @PostMapping("/children/me/allowance-requests")
    public ResponseEntity<AllowanceRequestResponse>
    createAllowanceRequest(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader,
            @Valid @RequestBody
            CreateAllowanceRequest request
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


    @ApiOperation("자녀 용돈 요청 목록 조회")
    @GetMapping("/children/{child_id}/allowance-requests")
    public ResponseEntity<AllowanceRequestListResponse>
    getAllowanceRequests(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader,
            @PathVariable("child_id") Long childId,
            @RequestParam(
                    value = "status",
                    required = false
            ) String status,
            @RequestParam(
                    value = "cursor",
                    required = false
            ) String cursor,
            @RequestParam(
                    value = "size",
                    required = false
            ) String size
    ) {
        long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        return ResponseEntity.ok(
                allowanceRequestService.getAllowanceRequests(
                        memberId,
                        childId,
                        status,
                        cursor,
                        size
                )
        );
    }

    @ApiOperation("용돈 요청 상세 조회")
    @GetMapping("/allowance-requests/{allowance_request_id}")
    public ResponseEntity<AllowanceRequestDetailResponse>
    getAllowanceRequestDetail(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader,
            @PathVariable("allowance_request_id")
            Long allowanceRequestId
    ) {
        long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        return ResponseEntity.ok(
                allowanceRequestService.getAllowanceRequestDetail(
                        memberId,
                        allowanceRequestId
                )
        );
    }

    @ApiOperation("용돈 요청 상태 변경")
    @PatchMapping("/allowance-requests/{allowance_request_id}")
    public ResponseEntity<AllowanceRequestDetailResponse>
    updateAllowanceRequestStatus(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader,
            @PathVariable("allowance_request_id")
            Long allowanceRequestId,
            @RequestBody
            UpdateAllowanceRequestStatus request
    ) {
        long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        return ResponseEntity.ok(
                allowanceRequestService.updateAllowanceRequestStatus(
                        memberId,
                        allowanceRequestId,
                        request
                )
        );
    }
}