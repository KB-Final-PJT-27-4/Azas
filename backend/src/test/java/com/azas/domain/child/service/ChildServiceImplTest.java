package com.azas.domain.child.service;

import com.azas.domain.child.dto.ChildCreateRequest;
import com.azas.domain.child.dto.ChildListResponse;
import com.azas.domain.child.dto.ChildResponse;
import com.azas.domain.child.dto.ChildSummaryResponse;
import com.azas.domain.child.dto.ChildUpdateRequest;
import com.azas.domain.child.entity.BirthStatus;
import com.azas.domain.child.entity.Child;
import com.azas.domain.child.entity.Gender;
import com.azas.domain.child.entity.RelationType;
import com.azas.domain.child.mapper.ChildMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChildServiceImplTest {

    private static final Long MEMBER_ID = 7L;
    private static final Long CHILD_ID = 3L;

    @Mock
    private ChildMapper childMapper;

    private ChildService childService;

    @BeforeEach
    void setUp() {
        childService = new ChildServiceImpl(
                childMapper
        );
    }

    @Test
    void createsExpectedChildAndUsesDefaultGuardianRelation() {
        ChildCreateRequest request =
                expectedChildRequest(null);

        ChildResponse expectedResponse =
                childResponse(
                        CHILD_ID,
                        "깨비",
                        BirthStatus.EXPECTED
                );

        doAnswer(invocation -> {
            Child child = invocation.getArgument(0);
            child.setChildId(CHILD_ID);
            return null;
        }).when(childMapper).insertChild(any(Child.class));

        when(childMapper.findChildByIdForMember(
                CHILD_ID,
                MEMBER_ID
        )).thenReturn(expectedResponse);

        ChildResponse response =
                childService.createChild(
                        MEMBER_ID,
                        request
                );

        ArgumentCaptor<Child> childCaptor =
                ArgumentCaptor.forClass(Child.class);

        verify(childMapper).insertChild(
                childCaptor.capture()
        );

        Child insertedChild = childCaptor.getValue();

        assertEquals("깨비", insertedChild.getName());
        assertEquals(
                BirthStatus.EXPECTED,
                insertedChild.getBirthStatus()
        );
        assertEquals(
                LocalDate.of(2027, 1, 30),
                insertedChild.getExpectedBirthDate()
        );

        verify(childMapper).insertChildParent(
                CHILD_ID,
                MEMBER_ID,
                RelationType.GUARDIAN
        );

        assertSame(expectedResponse, response);
    }

    @Test
    void createsChildUsingRequestedRelationType() {
        ChildCreateRequest request =
                expectedChildRequest(RelationType.MOTHER);

        doAnswer(invocation -> {
            Child child = invocation.getArgument(0);
            child.setChildId(CHILD_ID);
            return null;
        }).when(childMapper).insertChild(any(Child.class));

        when(childMapper.findChildByIdForMember(
                CHILD_ID,
                MEMBER_ID
        )).thenReturn(
                childResponse(
                        CHILD_ID,
                        "깨비",
                        BirthStatus.EXPECTED
                )
        );

        childService.createChild(
                MEMBER_ID,
                request
        );

        verify(childMapper).insertChildParent(
                CHILD_ID,
                MEMBER_ID,
                RelationType.MOTHER
        );
    }

    @Test
    void rejectsChildCreationWhenNameIsBlank() {
        ChildCreateRequest request =
                expectedChildRequest(RelationType.MOTHER);

        ReflectionTestUtils.setField(
                request,
                "name",
                " "
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> childService.createChild(
                                MEMBER_ID,
                                request
                        )
                );

        assertEquals(
                ErrorCode.CHILD_INVALID_NAME,
                exception.getErrorCode()
        );

        verifyNoInteractions(childMapper);
    }

    @Test
    void rejectsExpectedChildWithoutExpectedBirthDate() {
        ChildCreateRequest request =
                expectedChildRequest(RelationType.MOTHER);

        ReflectionTestUtils.setField(
                request,
                "expectedBirthDate",
                null
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> childService.createChild(
                                MEMBER_ID,
                                request
                        )
                );

        assertEquals(
                ErrorCode.CHILD_EXPECTED_BIRTH_DATE_REQUIRED,
                exception.getErrorCode()
        );

        verifyNoInteractions(childMapper);
    }

    @Test
    void rejectsBornChildWithoutBirthDate() {
        ChildCreateRequest request =
                new ChildCreateRequest();

        ReflectionTestUtils.setField(
                request,
                "name",
                "하늘"
        );
        ReflectionTestUtils.setField(
                request,
                "birthStatus",
                BirthStatus.BORN
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> childService.createChild(
                                MEMBER_ID,
                                request
                        )
                );

        assertEquals(
                ErrorCode.CHILD_BIRTH_DATE_REQUIRED,
                exception.getErrorCode()
        );

        verifyNoInteractions(childMapper);
    }

    @Test
    void getsAccessibleChildren() {
        ChildSummaryResponse item =
                new ChildSummaryResponse();

        ReflectionTestUtils.setField(
                item,
                "childId",
                CHILD_ID
        );
        ReflectionTestUtils.setField(
                item,
                "name",
                "깨비"
        );
        ReflectionTestUtils.setField(
                item,
                "birthStatus",
                BirthStatus.EXPECTED
        );

        when(childMapper.findChildrenByMemberId(
                MEMBER_ID
        )).thenReturn(List.of(item));

        ChildListResponse response =
                childService.getChildren(MEMBER_ID);

        assertEquals(1, response.getItems().size());
        assertEquals(
                CHILD_ID,
                response.getItems().get(0).getChildId()
        );

        verify(childMapper)
                .findChildrenByMemberId(MEMBER_ID);
    }

    @Test
    void getsChildWhenMemberHasAccess() {
        ChildResponse expected =
                childResponse(
                        CHILD_ID,
                        "깨비",
                        BirthStatus.EXPECTED
                );

        when(childMapper.countChildAccess(
                CHILD_ID,
                MEMBER_ID
        )).thenReturn(1);

        when(childMapper.findChildByIdForMember(
                CHILD_ID,
                MEMBER_ID
        )).thenReturn(expected);

        ChildResponse response =
                childService.getChild(
                        MEMBER_ID,
                        CHILD_ID
                );

        assertSame(expected, response);

        verify(childMapper).countChildAccess(
                CHILD_ID,
                MEMBER_ID
        );

        verify(childMapper).findChildByIdForMember(
                CHILD_ID,
                MEMBER_ID
        );
    }

    @Test
    void rejectsChildLookupWhenMemberHasNoAccess() {
        when(childMapper.countChildAccess(
                CHILD_ID,
                MEMBER_ID
        )).thenReturn(0);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> childService.getChild(
                                MEMBER_ID,
                                CHILD_ID
                        )
                );

        assertEquals(
                ErrorCode.CHILD_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(childMapper, never())
                .findChildByIdForMember(
                        anyLong(),
                        anyLong()
                );
    }

    @Test
    void updatesAccessibleChild() {
        ChildUpdateRequest request =
                new ChildUpdateRequest();

        ReflectionTestUtils.setField(
                request,
                "name",
                "새로운 태명"
        );
        ReflectionTestUtils.setField(
                request,
                "birthStatus",
                BirthStatus.EXPECTED
        );
        ReflectionTestUtils.setField(
                request,
                "expectedBirthDate",
                LocalDate.of(2027, 2, 5)
        );

        when(childMapper.countChildAccess(
                CHILD_ID,
                MEMBER_ID
        )).thenReturn(1);

        ChildResponse expected =
                childResponse(
                        CHILD_ID,
                        "새로운 태명",
                        BirthStatus.EXPECTED
                );

        when(childMapper.findChildByIdForMember(
                CHILD_ID,
                MEMBER_ID
        )).thenReturn(expected);

        ChildResponse response =
                childService.updateChild(
                        MEMBER_ID,
                        CHILD_ID,
                        request
                );

        ArgumentCaptor<Child> childCaptor =
                ArgumentCaptor.forClass(Child.class);

        verify(childMapper).updateChild(
                childCaptor.capture()
        );

        Child updatedChild = childCaptor.getValue();

        assertEquals(CHILD_ID, updatedChild.getChildId());
        assertEquals(
                "새로운 태명",
                updatedChild.getName()
        );
        assertEquals(
                LocalDate.of(2027, 2, 5),
                updatedChild.getExpectedBirthDate()
        );
        assertSame(expected, response);
    }

    @Test
    void softDeletesChildWithoutFinancialHistory() {
        when(childMapper.countChildAccess(
                CHILD_ID,
                MEMBER_ID
        )).thenReturn(1);

        when(childMapper.countFinancialHistory(
                CHILD_ID
        )).thenReturn(0);

        childService.deleteChild(
                MEMBER_ID,
                CHILD_ID
        );

        verify(childMapper).softDeleteChild(
                CHILD_ID
        );
    }

    @Test
    void rejectsDeletionWhenFinancialHistoryExists() {
        when(childMapper.countChildAccess(
                CHILD_ID,
                MEMBER_ID
        )).thenReturn(1);

        when(childMapper.countFinancialHistory(
                CHILD_ID
        )).thenReturn(2);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> childService.deleteChild(
                                MEMBER_ID,
                                CHILD_ID
                        )
                );

        assertEquals(
                ErrorCode.CHILD_HAS_FINANCIAL_HISTORY,
                exception.getErrorCode()
        );

        verify(childMapper, never())
                .softDeleteChild(anyLong());
    }

    private ChildCreateRequest expectedChildRequest(
            RelationType relationType
    ) {
        ChildCreateRequest request =
                new ChildCreateRequest();

        ReflectionTestUtils.setField(
                request,
                "name",
                "깨비"
        );
        ReflectionTestUtils.setField(
                request,
                "birthStatus",
                BirthStatus.EXPECTED
        );
        ReflectionTestUtils.setField(
                request,
                "expectedBirthDate",
                LocalDate.of(2027, 1, 30)
        );
        ReflectionTestUtils.setField(
                request,
                "gender",
                Gender.UNKNOWN
        );
        ReflectionTestUtils.setField(
                request,
                "relationType",
                relationType
        );

        return request;
    }

    private ChildResponse childResponse(
            Long childId,
            String name,
            BirthStatus birthStatus
    ) {
        ChildResponse response =
                new ChildResponse();

        ReflectionTestUtils.setField(
                response,
                "childId",
                childId
        );
        ReflectionTestUtils.setField(
                response,
                "name",
                name
        );
        ReflectionTestUtils.setField(
                response,
                "birthStatus",
                birthStatus
        );
        ReflectionTestUtils.setField(
                response,
                "expectedBirthDate",
                LocalDate.of(2027, 1, 30)
        );
        ReflectionTestUtils.setField(
                response,
                "gender",
                Gender.UNKNOWN
        );
        ReflectionTestUtils.setField(
                response,
                "relationType",
                RelationType.MOTHER
        );

        return response;
    }
}