package com.azas.domain.checklist.controller;

import com.azas.domain.checklist.dto.ChecklistItemListResponse;
import com.azas.domain.checklist.dto.ChecklistItemListResult;
import com.azas.domain.checklist.service.ChecklistItemListService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "체크리스트")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ChecklistController {

    private final AccessTokenMemberResolver memberResolver;
    private final ChecklistItemListService checklistItemListService;

    @ApiOperation(
            value = "생애주기 체크리스트 조회",
            notes = "연결된 보호자가 자녀의 생애주기별 체크리스트와 진행률을 조회합니다."
    )
    @GetMapping("/children/{child_id}/checklist-items")
    public ResponseEntity<ChecklistItemListResponse>
    getChecklistItems(
            @RequestHeader("Authorization")
            String authorization,

            @ApiParam(value = "자녀 프로필 ID", required = true)
            @PathVariable("child_id")
            Long childId,

            @ApiParam(
                    value = "PREGNANCY, AGE_0_TO_1, AGE_2_TO_4, AGE_5_TO_7"
            )
            @RequestParam(
                    value = "stage",
                    required = false
            )
            String stage
    ) {
        Long memberId =
                memberResolver.resolveMemberId(authorization);

        ChecklistItemListResult result =
                checklistItemListService.getChecklistItems(
                        memberId,
                        childId,
                        stage
                );

        return ResponseEntity.ok(
                ChecklistItemListResponse.from(result)
        );
    }
}