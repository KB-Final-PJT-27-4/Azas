package com.azas.domain.member.controller;

import com.azas.domain.member.dto.PhoneVerificationConfirmRequest;
import com.azas.domain.member.dto.PhoneVerificationConfirmResponse;
import com.azas.domain.member.dto.PhoneVerificationConfirmResult;
import com.azas.domain.member.dto.PhoneVerificationSendRequest;
import com.azas.domain.member.dto.PhoneVerificationSendResponse;
import com.azas.domain.member.dto.PhoneVerificationSendResult;
import com.azas.domain.member.service.PhoneVerificationConfirmService;
import com.azas.domain.member.service.PhoneVerificationSendService;
import com.azas.global.response.ApiErrorResponse;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "회원")
@RestController
@RequestMapping("/api/v1/members/me/phone-verifications")
@RequiredArgsConstructor
public class PhoneVerificationController {

    private final PhoneVerificationSendService
            phoneVerificationSendService;
    private final PhoneVerificationConfirmService
            phoneVerificationConfirmService;
    private final AccessTokenMemberResolver
            accessTokenMemberResolver;

    @ApiOperation(
            value = "휴대폰 SMS 인증번호 발송",
            notes = "현재 로그인 회원의 휴대폰으로 6자리 인증번호를 발송합니다. "
                    + "인증번호는 3분 동안 유효하며 재발송은 60초 후 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 202,
                    message = "인증번호 발송 요청 성공",
                    response = PhoneVerificationSendResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "휴대폰 번호 누락 또는 형식 오류",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락·만료 또는 유효하지 않음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 429,
                    message = "인증번호 재발송 대기시간이 지나지 않음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 502,
                    message = "SMS 제공자 발송 실패",
                    response = ApiErrorResponse.class
            )
    })
    @PostMapping
    public ResponseEntity<PhoneVerificationSendResponse>
    sendVerificationCode(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader,
            @Valid
            @RequestBody
            PhoneVerificationSendRequest request
    ) {
        long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        PhoneVerificationSendResult result =
                phoneVerificationSendService.send(
                        memberId,
                        request.getPhoneNumber()
                );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                        PhoneVerificationSendResponse.from(
                                result
                        )
                );
    }

    @ApiOperation(
            value = "휴대폰 SMS 인증번호 확인",
            notes = "발급된 인증 요청 ID와 인증번호를 확인합니다. "
                    + "성공하면 회원정보 수정에 사용할 수 있는 일회용 인증 토큰을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "휴대폰 인증번호 확인 성공",
                    response = PhoneVerificationConfirmResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "인증 요청을 사용할 수 없거나 인증번호가 일치하지 않음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = "Access Token 누락·만료 또는 유효하지 않음",
                    response = ApiErrorResponse.class
            ),
            @ApiResponse(
                    code = 429,
                    message = "인증번호 입력 가능 횟수 초과",
                    response = ApiErrorResponse.class
            )
    })
    @PostMapping("/{verification_id}/confirm")
    public ResponseEntity<PhoneVerificationConfirmResponse>
    confirmVerificationCode(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader,
            @PathVariable("verification_id")
            long verificationId,
            @Valid
            @RequestBody
            PhoneVerificationConfirmRequest request
    ) {
        long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        PhoneVerificationConfirmResult result =
                phoneVerificationConfirmService.confirm(
                        memberId,
                        verificationId,
                        request.getVerificationCode()
                );

        return ResponseEntity.ok(
                PhoneVerificationConfirmResponse.from(
                        result
                )
        );
    }
}