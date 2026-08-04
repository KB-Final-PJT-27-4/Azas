package com.azas.domain.timecapsule.service;

import com.azas.domain.timecapsule.dto.CreateTimeCapsuleRequest;
import com.azas.domain.timecapsule.dto.TimeCapsuleListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleResponse;
import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleAccount;
import com.azas.domain.timecapsule.entity.TimeCapsuleStatus;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TimeCapsuleServiceTest {

    @Mock
    private TimeCapsuleMapper timeCapsuleMapper;

    @InjectMocks
    private TimeCapsuleService timeCapsuleService;

    @Test
    // [JMG] CAPSULE-1 활성 적금 계좌에 대한 보관함 생성 시 만기일을 공개 예정일로 저장한다.
    void createTimeCapsuleCreatesLockerForEligibleSavingsAccount() {
        TimeCapsuleAccount account = createAccount(
                1L,
                10L,
                "SAVINGS",
                "ACTIVE",
                "ACTIVE",
                LocalDate.of(2030, 7, 23)
        );
        CreateTimeCapsuleRequest request =
                createRequest("깨비의 첫 대학자금 저축");
        TimeCapsule persistedTimeCapsule =
                createTimeCapsule(
                        100L,
                        10L,
                        "깨비의 첫 대학자금 저축",
                        LocalDateTime.of(2030, 7, 23, 0, 0),
                        LocalDateTime.of(2026, 8, 4, 10, 0)
                );

        given(timeCapsuleMapper.findAccessibleAccountById(1L, 7L))
                .willReturn(account);
        given(timeCapsuleMapper.findByFinancialAccountId(1L))
                .willReturn(null);
        doAnswer(invocation -> {
            TimeCapsule created = invocation.getArgument(0);
            ReflectionTestUtils.setField(
                    created,
                    "timeCapsuleId",
                    100L
            );
            return 1;
        }).when(timeCapsuleMapper).insert(any(TimeCapsule.class));
        given(timeCapsuleMapper.findAccessibleById(100L, 7L))
                .willReturn(persistedTimeCapsule);

        TimeCapsuleResponse response =
                timeCapsuleService.createTimeCapsule(
                        7L,
                        1L,
                        request
                );

        ArgumentCaptor<TimeCapsule> captor =
                ArgumentCaptor.forClass(TimeCapsule.class);
        verify(timeCapsuleMapper).insert(captor.capture());
        assertEquals(
                LocalDateTime.of(2030, 7, 23, 0, 0),
                captor.getValue().getExpectedReleaseAt()
        );
        assertEquals(100L, response.getTimeCapsuleId());
        assertEquals(10L, response.getChildId());
        assertEquals("COLLECTING", response.getStatus());
    }

    @Test
    // [JMG] CAPSULE-1 적금이 아니거나 비활성 계좌에는 보관함 생성을 거부한다.
    void createTimeCapsuleRejectsIneligibleAccount() {
        TimeCapsuleAccount account = createAccount(
                1L,
                10L,
                "DEMAND_DEPOSIT",
                "ACTIVE",
                "ACTIVE",
                null
        );

        given(timeCapsuleMapper.findAccessibleAccountById(1L, 7L))
                .willReturn(account);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () ->
                timeCapsuleService.createTimeCapsule(
                        7L,
                        1L,
                        createRequest("제목")
                )
        );

        assertEquals(
                ErrorCode.INELIGIBLE_TIME_CAPSULE_ACCOUNT,
                exception.getErrorCode()
        );

        verify(timeCapsuleMapper, never())
                .insert(any(TimeCapsule.class));
    }

    @Test
    // [JMG] CAPSULE-1 서비스 연결이 해제된 적금 계좌에는 보관함 생성을 거부한다.
    void createTimeCapsuleRejectsUnlinkedAccount() {
        TimeCapsuleAccount account = createAccount(
                1L,
                10L,
                "SAVINGS",
                "ACTIVE",
                "UNLINKED",
                LocalDate.of(2030, 7, 23)
        );

        given(timeCapsuleMapper.findAccessibleAccountById(1L, 7L))
                .willReturn(account);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleService.createTimeCapsule(
                        7L,
                        1L,
                        createRequest("제목")
                )
        );

        assertEquals(
                ErrorCode.INELIGIBLE_TIME_CAPSULE_ACCOUNT,
                exception.getErrorCode()
        );
        verify(timeCapsuleMapper, never())
                .insert(any(TimeCapsule.class));
    }

    @Test
    // [JMG] CAPSULE-1 접근 권한이 없는 계좌는 존재 여부를 노출하지 않는다.
    void createTimeCapsuleHidesInaccessibleAccount() {
        given(timeCapsuleMapper.findAccessibleAccountById(1L, 7L))
                .willReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleService.createTimeCapsule(
                        7L,
                        1L,
                        createRequest("제목")
                )
        );

        assertEquals(
                ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );
        verify(timeCapsuleMapper, never())
                .findByFinancialAccountId(1L);
    }

    @Test
    // [JMG] CAPSULE-2 카드 목록은 다음 페이지가 있으면 keyset cursor를 반환한다.
    void getTimeCapsulesReturnsNextCursorForCardView() {
        TimeCapsule first = createTimeCapsule(
                3L,
                10L,
                "첫 번째",
                LocalDateTime.of(2030, 7, 23, 0, 0),
                LocalDateTime.of(2026, 8, 4, 12, 0)
        );
        TimeCapsule second = createTimeCapsule(
                2L,
                10L,
                "두 번째",
                LocalDateTime.of(2030, 8, 23, 0, 0),
                LocalDateTime.of(2026, 8, 3, 12, 0)
        );
        TimeCapsule third = createTimeCapsule(
                1L,
                10L,
                "세 번째",
                LocalDateTime.of(2030, 9, 23, 0, 0),
                LocalDateTime.of(2026, 8, 2, 12, 0)
        );

        given(timeCapsuleMapper.existsActiveParentRelation(7L, 10L))
                .willReturn(true);
        given(timeCapsuleMapper.findCardSummaries(any()))
                .willReturn(List.of(first, second, third));

        TimeCapsuleListResponse response =
                timeCapsuleService.getTimeCapsules(
                        7L,
                        10L,
                        "CARD",
                        "COLLECTING",
                        null,
                        2,
                        null,
                        null
                );

        assertTrue(response.isHasNext());
        assertNotNull(response.getNextCursor());
        assertEquals(
                List.of(3L, 2L),
                response.getItems().stream()
                        .map(item -> item.getTimeCapsuleId())
                        .collect(Collectors.toList())
        );
    }

    @Test
    // [JMG] CAPSULE-3 접근 권한이 없는 보관함은 존재 여부를 노출하지 않는다.
    void getTimeCapsuleHidesInaccessibleTimeCapsule() {
        given(timeCapsuleMapper.findAccessibleById(100L, 7L))
                .willReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleService.getTimeCapsule(7L, 100L)
        );

        assertEquals(
                ErrorCode.TIME_CAPSULE_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    // [JMG] CAPSULE-1 테스트용 보관함 생성 가능 계좌를 구성한다.
    private TimeCapsuleAccount createAccount(
            long financialAccountId,
            long childId,
            String accountProductType,
            String accountStatus,
            String linkStatus,
            LocalDate maturityDate
    ) {
        TimeCapsuleAccount account = new TimeCapsuleAccount();
        ReflectionTestUtils.setField(
                account,
                "financialAccountId",
                financialAccountId
        );
        ReflectionTestUtils.setField(account, "childId", childId);
        ReflectionTestUtils.setField(
                account,
                "accountProductType",
                accountProductType
        );
        ReflectionTestUtils.setField(
                account,
                "accountStatus",
                accountStatus
        );
        ReflectionTestUtils.setField(
                account,
                "linkStatus",
                linkStatus
        );
        ReflectionTestUtils.setField(
                account,
                "maturityDate",
                maturityDate
        );
        return account;
    }

    // [JMG] CAPSULE-1 테스트용 보관함 생성 요청을 구성한다.
    private CreateTimeCapsuleRequest createRequest(String title) {
        CreateTimeCapsuleRequest request =
                new CreateTimeCapsuleRequest();
        ReflectionTestUtils.setField(request, "title", title);
        return request;
    }

    // [JMG] CAPSULE-1~3 테스트용 ERD 타임캡슐 엔티티를 구성한다.
    private TimeCapsule createTimeCapsule(
            long timeCapsuleId,
            long childId,
            String title,
            LocalDateTime expectedReleaseAt,
            LocalDateTime createdAt
    ) {
        TimeCapsule timeCapsule = TimeCapsule.create(
                childId,
                1L,
                title,
                expectedReleaseAt.toLocalDate()
        );
        ReflectionTestUtils.setField(
                timeCapsule,
                "timeCapsuleId",
                timeCapsuleId
        );
        ReflectionTestUtils.setField(
                timeCapsule,
                "expectedReleaseAt",
                expectedReleaseAt
        );
        ReflectionTestUtils.setField(
                timeCapsule,
                "status",
                TimeCapsuleStatus.COLLECTING
        );
        ReflectionTestUtils.setField(
                timeCapsule,
                "createdAt",
                createdAt
        );
        return timeCapsule;
    }
}
