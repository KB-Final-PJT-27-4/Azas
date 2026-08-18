package com.azas.domain.child.controller;

import com.azas.domain.child.dto.ChildCreateRequest;
import com.azas.domain.child.dto.ChildListResponse;
import com.azas.domain.child.dto.ChildResponse;
import com.azas.domain.child.dto.ChildUpdateRequest;
import com.azas.domain.child.service.ChildService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "자녀")
@RestController
@RequestMapping("/api/v1/children")
@RequiredArgsConstructor
public class ChildController {
    private final ChildService childService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(value = "자녀 프로필 등록", notes = "부모가 자녀 프로필을 등록합니다.")
    @PostMapping
    public ResponseEntity<ChildResponse> createChild(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader,
            @RequestBody ChildCreateRequest request) {

        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        ChildResponse childResponse =
                childService.createChild(
                        memberId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(childResponse);
    }

    @ApiOperation(value = "자녀 목록 조회", notes = "현재 회원이 관리하는 자녀 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ChildListResponse> getChildren(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        ChildListResponse childListResponse= childService.getChildren(memberId);
        return ResponseEntity.ok(childListResponse);
    }

    @ApiOperation(value = "자녀 상세 조회")
    @GetMapping("/{childId}")
    public ResponseEntity<ChildResponse> getChild(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader,
            @ApiParam(value = "자녀 ID", required = true, example = "1")
            @PathVariable
            Long childId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        ChildResponse childResponse = childService.getChild(memberId, childId);
        return ResponseEntity.ok(childResponse);
    }


    @PatchMapping("/{childId}")
    public ResponseEntity<ChildResponse> updateChild(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader,
            @PathVariable
            Long childId,
            @RequestBody
            ChildUpdateRequest request
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );
        ChildResponse childResponse = childService.updateChild(memberId, childId, request);
        return ResponseEntity.ok(childResponse);
    }

    @DeleteMapping("/{childId}")
    public ResponseEntity<Void> deleteChild(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader,
            @PathVariable
            Long childId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        childService.deleteChild(memberId, childId);
        return ResponseEntity.noContent().build();
    }

}
