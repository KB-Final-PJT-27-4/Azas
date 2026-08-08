package com.azas.domain.family.service;

import com.azas.domain.auth.mapper.ChildInviteMapper;
import com.azas.domain.auth.mapper.ParentInviteMapper;
import com.azas.domain.auth.service.FamilyInvitationStore;
import com.azas.domain.auth.service.TokenHashEncoder;

import com.azas.domain.family.dto.AllowanceRequestResponse;
import com.azas.domain.family.dto.ChildMemberLinkResponse;
import com.azas.domain.family.dto.FamilyGuardianListResponse;
import com.azas.domain.family.dto.FamilyGuardianResponse;

import com.azas.domain.family.mapper.FamilyMapper;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class FamilyServiceImplTest {

    @Mock
    private FamilyMapper familyMapper;

    @Mock
    private TokenHashEncoder tokenHashEncoder;

    @Mock
    private FamilyInvitationStore familyInvitationStore;

    @Mock
    private ParentInviteMapper parentInviteMapper;

    @Mock
    private ChildInviteMapper childInviteMapper;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private FamilyServiceImpl familyService;

    @Test
    void getFamilyMembersReturnsMembersForAccessibleChild() {
        FamilyGuardianResponse guardian = new FamilyGuardianResponse();
        ReflectionTestUtils.setField(guardian, "memberId", 8L);
        ReflectionTestUtils.setField(guardian, "name", "김엄마");

        given(familyMapper.countChildAccess(10L, 7L)).willReturn(1);
        given(familyMapper.findFamilyMembers(10L, 7L))
                .willReturn(List.of(guardian));

        FamilyGuardianListResponse response =
                familyService.getFamilyMembers(7L, 10L);

        assertEquals(1, response.getItems().size());
        assertEquals("김엄마", response.getItems().get(0).getName());
    }

    @Test
    void getFamilyMembersRejectsInaccessibleChild() {
        given(familyMapper.countChildAccess(10L, 7L)).willReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.getFamilyMembers(7L, 10L)
        );

        assertEquals(ErrorCode.CHILD_NOT_FOUND, exception.getErrorCode());
        verify(familyMapper, never()).findFamilyMembers(anyLong(), anyLong());
    }

    @Test
    void getChildMemberLinkReturnsStoredLinkInformation() {
        ChildMemberLinkResponse linkResponse = childMemberLink(10L, true, 20L);

        given(familyMapper.countChildAccess(10L, 7L)).willReturn(1);
        given(familyMapper.findChildMemberLinkByChildId(10L))
                .willReturn(linkResponse);

        ChildMemberLinkResponse response =
                familyService.getChildMemberLink(7L, 10L);

        assertEquals(10L, response.getChildId());
        assertTrue(response.getLinked());
        assertEquals(20L, response.getChildMemberId());
    }

    @Test
    void requestAllowanceCreatesFirstRequestForChildMember() {
        given(familyMapper.countChildMemberAccess(10L, 20L)).willReturn(1);
        given(familyMapper.findLastAllowanceRequestMonth(10L))
                .willReturn(null);
        given(familyMapper.updateAllowanceRequest(eq(10L), any(LocalDate.class)))
                .willReturn(1);
        given(familyMapper.findChildAvailableAmount(10L))
                .willReturn(new BigDecimal("50000"));

        AllowanceRequestResponse response =
                familyService.requestAllowance(20L, 10L);

        assertEquals(10L, response.getChildId());
        assertTrue(response.getRequested());
        assertEquals(
                LocalDate.now().withDayOfMonth(1),
                response.getRequestMonth()
        );
        assertEquals(new BigDecimal("50000"),
                response.getChildAvailableAmount());
        verify(familyMapper).updateAllowanceRequest(
                eq(10L),
                eq(LocalDate.now().withDayOfMonth(1))
        );
    }

    @Test
    void requestAllowanceRejectsDuplicateRequestInSameMonth() {
        LocalDate thisMonth = LocalDate.now().withDayOfMonth(1);

        given(familyMapper.countChildMemberAccess(10L, 20L)).willReturn(1);
        given(familyMapper.findLastAllowanceRequestMonth(10L))
                .willReturn(thisMonth);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.requestAllowance(20L, 10L)
        );

        assertEquals(
                ErrorCode.ALLOWANCE_REQUEST_ALREADY_EXISTS,
                exception.getErrorCode()
        );
        verify(familyMapper, never()).updateAllowanceRequest(
                anyLong(),
                any(LocalDate.class)
        );
    }

    @Test
    void requestAllowanceRejectsParentAccount() {
        given(familyMapper.countChildMemberAccess(10L, 7L)).willReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.requestAllowance(7L, 10L)
        );

        assertEquals(ErrorCode.CHILD_ACCESS_DENIED, exception.getErrorCode());
    }

    private ChildMemberLinkResponse childMemberLink(
            Long childId,
            boolean linked,
            Long memberId
    ) {
        ChildMemberLinkResponse response = new ChildMemberLinkResponse();
        ReflectionTestUtils.setField(response, "childId", childId);
        ReflectionTestUtils.setField(response, "linked", linked);
        ReflectionTestUtils.setField(response, "childMemberId", memberId);
        return response;
    }
}