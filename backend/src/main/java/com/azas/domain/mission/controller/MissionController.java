package com.azas.domain.mission.controller;

import com.azas.domain.mission.dto.CreateMissionRequest;
import com.azas.domain.mission.dto.MissionCreateResponse;
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
}