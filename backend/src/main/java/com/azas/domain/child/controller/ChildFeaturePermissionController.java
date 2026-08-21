package com.azas.domain.child.controller;

import com.azas.domain.child.dto.ChildFeaturePermissionRequest;
import com.azas.domain.child.dto.ChildFeaturePermissionResponse;
import com.azas.domain.child.service.ChildFeaturePermissionService;
import com.azas.global.response.ApiErrorResponse;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "자녀")
@RestController
@RequestMapping("/api/v1/children")
@RequiredArgsConstructor
public class ChildFeaturePermissionController {

    private final ChildFeaturePermissionService childFeaturePermissionService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation(value = "자녀 이용 권한 조회")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "자녀 이용 권한 조회 성공",
                    response = ChildFeaturePermissionResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token이 없거나 유효하지 않음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "해당 자녀 정보에 접근할 부모 권한이 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "자녀 정보를 찾을 수 없음",
                    response = ApiErrorResponse.class
            )
    })
    @GetMapping("/{child_id}/feature-permissions")
    public ResponseEntity<ChildFeaturePermissionResponse> getPermission(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("child_id") long childId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(ChildFeaturePermissionResponse.from(
                childFeaturePermissionService.getPermission(memberId, childId)
        ));
    }

    @ApiOperation(value = "자녀 이용 권한 수정")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "자녀 이용 권한 수정 성공",
                    response = ChildFeaturePermissionResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "필수 권한 값이 누락되었거나 요청 값이 올바르지 않음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token이 없거나 유효하지 않음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 403,
                    message = "해당 자녀 정보에 접근할 부모 권한이 없음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "자녀 정보를 찾을 수 없음",
                    response = ApiErrorResponse.class
            )
    })
    @PatchMapping("/{child_id}/feature-permissions")
    public ResponseEntity<ChildFeaturePermissionResponse> updatePermission(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("child_id") long childId,
            @Valid @RequestBody ChildFeaturePermissionRequest request
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(ChildFeaturePermissionResponse.from(
                childFeaturePermissionService.updatePermission(
                        memberId,
                        childId,
                        request
                )
        ));
    }
}
