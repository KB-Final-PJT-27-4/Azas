package com.azas.domain.mission.controller;

import com.azas.domain.mission.dto.*;
import com.azas.domain.mission.service.MissionService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "용돈 미션")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;
    private final AccessTokenMemberResolver memberResolver;

    @ApiOperation(
            value = "미션 생성",
            notes = "연결된 부모가 자녀에게 보상형 용돈 미션을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = "미션 생성 성공",
                    response = MissionCreateResponse.class
            ),
            @ApiResponse(code = 400, message = "미션 입력값 오류"),
            @ApiResponse(code = 401, message = "인증 오류"),
            @ApiResponse(code = 403, message = "부모 권한 없음"),
            @ApiResponse(code = 404, message = "자녀 없음")
    })
    @PostMapping("/children/{child_id}/missions")
    public ResponseEntity<MissionCreateResponse> createMission(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,
            @PathVariable("child_id") Long childId,
            @Valid @RequestBody CreateMissionRequest request
    ) {
        Long memberId =
                memberResolver.resolveMemberId(authorization);

        MissionCreateResponse response =
                missionService.createMission(
                        memberId,
                        childId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @ApiOperation(
            value = "미션 목록 조회",
            notes = "연결된 부모와 해당 자녀 본인이 미션 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "미션 목록 조회 성공",
                    response = MissionListResponse.class
            ),
            @ApiResponse(code = 400, message = "조회 조건 오류"),
            @ApiResponse(code = 401, message = "인증 오류"),
            @ApiResponse(code = 403, message = "자녀 접근 권한 없음"),
            @ApiResponse(code = 404, message = "자녀 없음")
    })
    @GetMapping("/children/{child_id}/missions")
    public ResponseEntity<MissionListResponse> getMissions(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,
            @PathVariable("child_id") Long childId,
            @RequestParam(
                    value = "filter",
                    required = false
            ) String filter,
            @RequestParam(
                    value = "cursor",
                    required = false
            ) String cursor,
            @RequestParam(
                    value = "size",
                    required = false
            ) Integer size
    ) {
        Long memberId =
                memberResolver.resolveMemberId(
                        authorization
                );

        return ResponseEntity.ok(
                missionService.getMissions(
                        memberId,
                        childId,
                        filter,
                        cursor,
                        size
                )
        );
    }

    @ApiOperation(
            value = "미션 상세 조회",
            notes = "연결된 부모 또는 해당 자녀 본인이 미션 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "미션 상세 조회 성공",
                    response = MissionDetailResponse.class
            ),
            @ApiResponse(code = 400, message = "미션 ID 오류"),
            @ApiResponse(code = 401, message = "인증 오류"),
            @ApiResponse(code = 403, message = "미션 접근 권한 없음"),
            @ApiResponse(code = 404, message = "미션 없음")
    })
    @GetMapping("/missions/{mission_id}")
    public ResponseEntity<MissionDetailResponse>
    getMissionDetail(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,
            @PathVariable("mission_id") Long missionId
    ) {
        Long memberId =
                memberResolver.resolveMemberId(
                        authorization
                );

        return ResponseEntity.ok(
                missionService.getMissionDetail(
                        memberId,
                        missionId
                )
        );
    }

    @ApiOperation(
            value = "미션 상태 변경",
            notes = "자녀 완료 요청 및 부모의 승인·거절·취소를 처리합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "미션 상태 변경 성공",
                    response = MissionDetailResponse.class
            ),
            @ApiResponse(code = 400, message = "상태 변경 요청 오류"),
            @ApiResponse(code = 401, message = "인증 오류"),
            @ApiResponse(code = 403, message = "미션 처리 권한 없음"),
            @ApiResponse(code = 404, message = "미션 또는 계좌 없음"),
            @ApiResponse(code = 409, message = "상태 전이 또는 이체 오류"),
            @ApiResponse(code = 422, message = "보상 계좌 조합 오류")
    })
    @PatchMapping("/missions/{mission_id}")
    public ResponseEntity<MissionDetailResponse>
    updateMissionStatus(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,
            @PathVariable("mission_id") Long missionId,
            @Valid @RequestBody
            UpdateMissionStatusRequest request
    ) {
        Long memberId =
                memberResolver.resolveMemberId(
                        authorization
                );

        return ResponseEntity.ok(
                missionService.updateMissionStatus(
                        memberId,
                        missionId,
                        request
                )
        );
    }
}