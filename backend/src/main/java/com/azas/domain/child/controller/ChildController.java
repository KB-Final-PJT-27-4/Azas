package com.azas.domain.child.controller;

import com.azas.domain.child.dto.ChildCreateRequest;
import com.azas.domain.child.dto.ChildListResponse;
import com.azas.domain.child.dto.ChildResponse;
import com.azas.domain.child.dto.ChildUpdateRequest;
import com.azas.domain.child.service.ChildService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "자녀")
@RestController
@RequestMapping("/api/v1/children")
@RequiredArgsConstructor
public class ChildController {
    private final ChildService childService;

    @ApiOperation(value = "자녀 프로필 등록", notes = "부모가 자녀 프로필을 등록합니다.")
    @PostMapping
    public ResponseEntity<ChildResponse> createChild(@RequestBody ChildCreateRequest request) {
        Long memberId = 1L;

        ChildResponse childResponse = childService.createChild(memberId, request);
        return ResponseEntity.ok(childResponse);
    }

    @ApiOperation(value = "자녀 목록 조회", notes = "현재 회원이 관리하는 자녀 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ChildListResponse> getChildren() {
        Long memberId = 1L;

        ChildListResponse childListResponse= childService.getChildren(memberId);
        return ResponseEntity.ok(childListResponse);
    }

    @ApiOperation(value = "자녀 상세 조회")
    @GetMapping("/{childId}")
    public ResponseEntity<ChildResponse> getChild(
            @ApiParam(value = "자녀 ID", required = true, example = "1")
            @PathVariable Long childId) {
        Long memberId = 1L;

        ChildResponse childResponse = childService.getChild(memberId, childId);
        return ResponseEntity.ok(childResponse);
    }


    @PatchMapping("/{childId}")
    public ResponseEntity<ChildResponse> updateChild(
            @PathVariable Long childId,
            @RequestBody ChildUpdateRequest request
    ) {
        Long memberId = 1L;
        ChildResponse childResponse = childService.updateChild(memberId, childId, request);
        return ResponseEntity.ok(childResponse);
    }

    @DeleteMapping("/{childId}")
    public ResponseEntity<Void> deleteChild(@PathVariable Long childId) {
        Long memberId = 1L;

        childService.deleteChild(memberId, childId);
        return ResponseEntity.noContent().build();
    }

}
