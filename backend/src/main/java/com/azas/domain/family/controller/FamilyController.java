package com.azas.domain.family.controller;

import com.azas.domain.family.dto.AllowanceRequestResponse;
import com.azas.domain.family.dto.ChildMemberLinkResponse;
import com.azas.domain.family.dto.FamilyGuardianListResponse;
import com.azas.domain.family.dto.FamilyInvitationAcceptRequest;
import com.azas.domain.family.dto.FamilyInvitationAcceptResponse;
import com.azas.domain.family.dto.FamilyInvitationCreateRequest;
import com.azas.domain.family.dto.FamilyInvitationCreateResponse;
import com.azas.domain.family.dto.FamilyInvitationInfoResponse;
import com.azas.domain.family.service.FamilyService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "자녀·가족")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation("함께 관리하는 보호자 목록 조회")
    @GetMapping("/children/{child_id}/family-members")
    public ResponseEntity<FamilyGuardianListResponse> getFamilyMembers(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("child_id") Long childId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(
                familyService.getFamilyMembers(memberId, childId)
        );
    }

    @ApiOperation("아이 계정 연결 상태 조회")
    @GetMapping("/children/{child_id}/member-link")
    public ResponseEntity<ChildMemberLinkResponse> getChildMemberLink(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("child_id") Long childId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(
                familyService.getChildMemberLink(memberId, childId)
        );
    }


    @ApiOperation("가족/아이 초대코드 발급")
    @PostMapping("/children/{child_id}/family-invitations")
    public ResponseEntity<FamilyInvitationCreateResponse> createFamilyInvitation(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("child_id") Long childId,
            @Valid @RequestBody FamilyInvitationCreateRequest request
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        FamilyInvitationCreateResponse response =
                familyService.createFamilyInvitation(
                        memberId,
                        childId,
                        request
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ApiOperation("초대코드 정보 조회")
    @GetMapping("/family-invitations/{invite_token}")
    public ResponseEntity<FamilyInvitationInfoResponse> getFamilyInvitationInfo(
            @ApiParam(value = "초대 토큰", required = true)
            @PathVariable("invite_token") String inviteToken
    ) {
        return ResponseEntity.ok(
                familyService.getFamilyInvitationInfo(inviteToken)
        );
    }

    @ApiOperation("초대코드 수락 및 계정 연결")
    @PostMapping("/family-invitations/{invite_token}/accept")
    public ResponseEntity<FamilyInvitationAcceptResponse> acceptFamilyInvitation(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @PathVariable("invite_token") String inviteToken,
            @RequestBody(required = false) FamilyInvitationAcceptRequest request
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(
                familyService.acceptFamilyInvitation(
                        memberId,
                        inviteToken,
                        request
                )
        );
    }
}