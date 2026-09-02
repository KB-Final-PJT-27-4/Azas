package com.azas.domain.finance.transfer.controller;

import com.azas.domain.finance.transfer.dto.ChildTransferListItemResponse;
import com.azas.domain.finance.transfer.dto.CreateTransferRequest;
import com.azas.domain.finance.transfer.dto.MemberTransferListItemResponse;
import com.azas.domain.finance.transfer.dto.TransferCreateResponse;
import com.azas.domain.finance.transfer.dto.TransferDetailResponse;
import com.azas.domain.finance.transfer.dto.TransferListResponse;
import com.azas.domain.finance.transfer.service.TransferService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;

@Api(tags = "이체")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation("TRANSFER-1 목표 계좌로 이체 요청(수동)")
    @PostMapping("/transfers")
    public ResponseEntity<TransferCreateResponse> createTransfer(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateTransferRequest request
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(authorizationHeader);
        TransferCreateResponse response = transferService.createTransfer(
                memberId, idempotencyKey, request
        );
        return ResponseEntity.ok(response);
    }

    @ApiOperation("TRANSFER-2 자녀 목표별 서비스 이체 내역 조회")
    @GetMapping("/children/{child_id}/transfers")
    public ResponseEntity<TransferListResponse<ChildTransferListItemResponse>> getChildTransfers(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable("child_id") Long childId,
            @RequestParam(value = "financial_goal_id", required = false) Long financialGoalId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(authorizationHeader);
        return ResponseEntity.ok(transferService.getChildTransfers(
                memberId, childId, financialGoalId, status,
                startDate, endDate, cursor, size
        ));
    }

    @ApiOperation("TRANSFER-3 이체 처리 결과 전체 조회")
    @GetMapping("/members/me/transfers")
    public ResponseEntity<TransferListResponse<MemberTransferListItemResponse>> getMemberTransfers(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "transfer_type", required = false) String transferType,
            @RequestParam(value = "child_id", required = false) Long childId,
            @RequestParam(value = "start_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(authorizationHeader);
        return ResponseEntity.ok(transferService.getMemberTransfers(
                memberId, status, transferType, childId,
                startDate, endDate, cursor, size
        ));
    }

    @ApiOperation("TRANSFER-4 이체 처리 결과 상세 조회")
    @GetMapping("/transfers/{transfer_id}")
    public ResponseEntity<TransferDetailResponse> getTransfer(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable("transfer_id") Long transferId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(authorizationHeader);
        return ResponseEntity.ok(transferService.getTransfer(memberId, transferId));
    }
}