package com.azas.domain.timecapsule.controller;

import com.azas.domain.timecapsule.dto.CreateTimeCapsuleRequest;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntrySealResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryUpdateResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleResponse;
import com.azas.domain.timecapsule.dto.UpdateTimeCapsuleEntryRequest;
import com.azas.domain.timecapsule.service.AccessTokenMemberResolver;
import com.azas.domain.timecapsule.service.TimeCapsuleEntryService;
import com.azas.domain.timecapsule.service.TimeCapsuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "타임캡슐")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TimeCapsuleController {

    private final AccessTokenMemberResolver accessTokenMemberResolver;
    private final TimeCapsuleService timeCapsuleService;
    private final TimeCapsuleEntryService timeCapsuleEntryService;

    @ApiOperation(
            value = "타임캡슐 보관함 생성",
            notes = "자녀 명의 활성 적금 계좌에 연결된 타임캡슐 보관함을 생성합니다."
    )
    @PostMapping("/accounts/{account_id}/time-capsule")
    // [JMG] CAPSULE-1 자녀 적금 계좌와 1:1로 연결되는 타임캡슐 보관함 생성 요청을 처리한다.
    public ResponseEntity<TimeCapsuleResponse> createTimeCapsule(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "금융 계좌 ID", required = true)
            @PathVariable("account_id")
            long accountId,
            @Valid @RequestBody CreateTimeCapsuleRequest request
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(timeCapsuleService.createTimeCapsule(
                        memberId,
                        accountId,
                        request
                ));
    }

    @ApiOperation(
            value = "타임캡슐 보관함 목록 조회",
            notes = "카드 또는 캘린더 화면용 보관함 목록을 커서 기반으로 조회합니다."
    )
    @GetMapping("/children/{child_id}/time-capsules")
    // [JMG] CAPSULE-2 부모에게 연결된 자녀의 타임캡슐 보관함 목록을 조회한다.
    public ResponseEntity<TimeCapsuleListResponse> getTimeCapsules(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "자녀 ID", required = true)
            @PathVariable("child_id")
            long childId,
            @RequestParam(value = "view", required = false) String view,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(timeCapsuleService.getTimeCapsules(
                memberId,
                childId,
                view,
                status,
                cursor,
                size,
                year,
                month
        ));
    }

    @ApiOperation(
            value = "타임캡슐 보관함 상세 조회",
            notes = "보관함 기본 정보와 적금 계좌에 설정된 목표를 조회합니다."
    )
    @GetMapping("/time-capsules/{time_capsule_id}")
    // [JMG] CAPSULE-3 부모 권한을 확인한 뒤 타임캡슐 보관함 기본 정보를 조회한다.
    public ResponseEntity<TimeCapsuleResponse> getTimeCapsule(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 보관함 ID", required = true)
            @PathVariable("time_capsule_id")
            long timeCapsuleId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(timeCapsuleService.getTimeCapsule(
                memberId,
                timeCapsuleId
        ));
    }

    @ApiOperation(
            value = "타임캡슐 엔트리 목록 조회",
            notes = "부모 또는 보호자가 삭제되지 않은 기록 목록을 조회합니다."
    )
    @GetMapping("/time-capsules/{time_capsule_id}/entries")
    // [JMG] CAPSULE-4 부모 권한을 확인한 뒤 타임캡슐 내부 엔트리 목록을 조회한다.
    public ResponseEntity<TimeCapsuleEntryListResponse>
    getTimeCapsuleEntries(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 보관함 ID", required = true)
            @PathVariable("time_capsule_id")
            long timeCapsuleId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(
                timeCapsuleEntryService.getTimeCapsuleEntries(
                        memberId,
                        timeCapsuleId
                )
        );
    }

    @ApiOperation(
            value = "타임캡슐 엔트리 수정",
            notes = "작성자 본인이 DRAFT 엔트리의 제목 또는 편지를 수정합니다."
    )
    @PatchMapping("/time-capsule-entries/{entry_id}")
    // [JMG] CAPSULE-12 작성자 본인의 DRAFT 엔트리 제목·편지 수정 요청을 처리한다.
    public ResponseEntity<TimeCapsuleEntryUpdateResponse>
    updateTimeCapsuleEntry(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 엔트리 ID", required = true)
            @PathVariable("entry_id")
            long timeCapsuleEntryId,
            @Valid @RequestBody UpdateTimeCapsuleEntryRequest request
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(
                timeCapsuleEntryService.updateTimeCapsuleEntry(
                        memberId,
                        timeCapsuleEntryId,
                        request
                )
        );
    }

    @ApiOperation(
            value = "타임캡슐 엔트리 봉인",
            notes = "작성자 본인이 미디어 조건을 충족한 DRAFT 엔트리를 봉인합니다."
    )
    @PatchMapping("/time-capsule-entries/{entry_id}/seal")
    // [JMG] CAPSULE-15 미디어 조건을 충족한 작성자 본인의 DRAFT 엔트리를 봉인한다.
    public ResponseEntity<TimeCapsuleEntrySealResponse>
    sealTimeCapsuleEntry(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 엔트리 ID", required = true)
            @PathVariable("entry_id")
            long timeCapsuleEntryId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(
                timeCapsuleEntryService.sealTimeCapsuleEntry(
                        memberId,
                        timeCapsuleEntryId
                )
        );
    }
}
