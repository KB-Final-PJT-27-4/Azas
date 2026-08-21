package com.azas.domain.timecapsule.service;

import com.azas.domain.timecapsule.dto.CreateTimeCapsuleRequest;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleListResponse;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleAccount;
import com.azas.domain.timecapsule.entity.TimeCapsuleStatus;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleEntryMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMediaMapper;
import com.azas.domain.timecapsule.storage.TimeCapsuleObjectStorage;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TimeCapsuleServiceTest {

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private TimeCapsuleMapper timeCapsuleMapper;

    @Mock
    private TimeCapsuleEntryMapper timeCapsuleEntryMapper;

    @Mock
    private TimeCapsuleMediaMapper timeCapsuleMediaMapper;


    @Mock
    private TimeCapsuleObjectStorage timeCapsuleObjectStorage;

    @InjectMocks
    private TimeCapsuleService timeCapsuleService;

    @Test
    void createTimeCapsuleUsesChildSavingsAccountAndRequestedReleaseDate() {
        TimeCapsuleAccount account = createAccount(
                1L, "CHILD", null, 10L,
                "아이사랑적금", "SAVINGS", "ACTIVE", "ACTIVE"
        );
        LocalDate releaseDate = LocalDate.now().plusYears(1);
        CreateTimeCapsuleRequest request = createRequest(1L, releaseDate);
        stubSuccessfulCreation(account, releaseDate);

        CreateTimeCapsuleResponse response =
                timeCapsuleService.createTimeCapsule(7L, 10L, request);

        ArgumentCaptor<TimeCapsule> captor =
                ArgumentCaptor.forClass(TimeCapsule.class);
        verify(timeCapsuleMapper).insert(captor.capture());
        assertEquals("아이사랑적금", captor.getValue().getTitle());
        assertEquals(releaseDate.atStartOfDay(),
                captor.getValue().getExpectedReleaseAt());
        assertEquals(100L, response.getTimeCapsuleId());
        assertEquals("CHILD", response.getAccount().getOwnerType());
        assertEquals("아이사랑적금", response.getTitle());
        assertEquals(releaseDate, response.getReleaseDate());
    }

    @Test
    void createTimeCapsuleSupportsParentDemandDepositWithoutReleaseDate() {
        TimeCapsuleAccount account = createAccount(
                1L, "PARENT", 7L, null,
                "KB국민 입출금통장", "DEMAND_DEPOSIT",
                "ACTIVE", "ACTIVE"
        );
        CreateTimeCapsuleRequest request = createRequest(1L, null);
        stubSuccessfulCreation(account, null);

        CreateTimeCapsuleResponse response =
                timeCapsuleService.createTimeCapsule(7L, 10L, request);

        assertEquals("PARENT", response.getAccount().getOwnerType());
        assertEquals("DEMAND_DEPOSIT",
                response.getAccount().getAccountProductType());
        assertEquals(null, response.getReleaseDate());
    }

    @Test
    void createTimeCapsuleRejectsUnlinkedAccount() {
        TimeCapsuleAccount account = createAccount(
                1L, "CHILD", null, 10L,
                "아이사랑적금", "SAVINGS", "ACTIVE", "UNLINKED"
        );
        stubParentAndChildAccess();
        given(timeCapsuleMapper.findAccountById(1L)).willReturn(account);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleService.createTimeCapsule(
                        7L, 10L, createRequest(1L, null)
                )
        );

        assertEquals(ErrorCode.INELIGIBLE_TIME_CAPSULE_ACCOUNT,
                exception.getErrorCode());
        verify(timeCapsuleMapper, never()).insert(any(TimeCapsule.class));
    }

    @Test
    void createTimeCapsuleRejectsAccountOwnedByAnotherMember() {
        TimeCapsuleAccount account = createAccount(
                1L, "PARENT", 8L, null,
                "다른 부모 계좌", "DEMAND_DEPOSIT", "ACTIVE", "ACTIVE"
        );
        stubParentAndChildAccess();
        given(timeCapsuleMapper.findAccountById(1L)).willReturn(account);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleService.createTimeCapsule(
                        7L, 10L, createRequest(1L, null)
                )
        );

        assertEquals(ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                exception.getErrorCode());
    }

    @Test
    void createTimeCapsuleRejectsDuplicateChildAccountCombination() {
        TimeCapsuleAccount account = createAccount(
                1L, "CHILD", null, 10L,
                "아이사랑적금", "SAVINGS", "ACTIVE", "ACTIVE"
        );
        stubParentAndChildAccess();
        given(timeCapsuleMapper.findAccountById(1L)).willReturn(account);
        given(timeCapsuleMapper.findByChildIdAndFinancialAccountId(10L, 1L))
                .willReturn(createTimeCapsule(
                        99L, 10L, "아이사랑적금",
                        LocalDateTime.now().plusYears(1),
                        LocalDateTime.now()
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleService.createTimeCapsule(
                        7L, 10L, createRequest(1L, null)
                )
        );

        assertEquals(ErrorCode.DUPLICATE_TIME_CAPSULE,
                exception.getErrorCode());
    }

    @Test
    void createTimeCapsuleRejectsTodayAsReleaseDate() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleService.createTimeCapsule(
                        7L, 10L,
                        createRequest(1L, LocalDate.now())
                )
        );

        assertEquals(ErrorCode.BADREQUEST, exception.getErrorCode());
    }

    @Test
    void createTimeCapsuleRequiresParentMember() {
        given(memberMapper.findById(7L)).willReturn(
                Member.createChild("child@example.com", "자녀", null)
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleService.createTimeCapsule(
                        7L, 10L, createRequest(1L, null)
                )
        );

        assertEquals(ErrorCode.PARENT_ACCESS_REQUIRED,
                exception.getErrorCode());
        verify(timeCapsuleMapper, never()).findAccountById(1L);
    }

    @Test
    void createTimeCapsuleRejectsInaccessibleChild() {
        given(memberMapper.findById(7L)).willReturn(
                Member.createParent("parent@example.com", "부모", null)
        );
        given(timeCapsuleMapper.existsChildById(10L)).willReturn(true);
        given(timeCapsuleMapper.existsActiveParentRelation(7L, 10L))
                .willReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleService.createTimeCapsule(
                        7L, 10L, createRequest(1L, null)
                )
        );

        assertEquals(ErrorCode.CHILD_ACCESS_DENIED,
                exception.getErrorCode());
        verify(timeCapsuleMapper, never()).findAccountById(1L);
    }

    @Test
    void createTimeCapsuleRejectsChildAccountForDifferentChild() {
        TimeCapsuleAccount account = createAccount(
                1L, "CHILD", null, 11L,
                "다른 자녀 적금", "SAVINGS", "ACTIVE", "ACTIVE"
        );
        stubParentAndChildAccess();
        given(timeCapsuleMapper.findAccountById(1L)).willReturn(account);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleService.createTimeCapsule(
                        7L, 10L, createRequest(1L, null)
                )
        );

        assertEquals(ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                exception.getErrorCode());
    }

    @Test
    // [JMG] CAPSULE-2 카드 목록은 다음 페이지가 있으면 keyset cursor를 반환한다.
    void getTimeCapsulesReturnsScreenSummaries() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        TimeCapsule first = createTimeCapsule(
                3L,
                10L,
                "첫 번째",
                today.plusDays(23).atStartOfDay(),
                LocalDateTime.of(2026, 8, 4, 12, 0)
        );
        TimeCapsule second = createTimeCapsule(
                2L,
                10L,
                "두 번째",
                today.minusDays(1).atStartOfDay(),
                LocalDateTime.of(2026, 8, 3, 12, 0)
        );
        TimeCapsule third = createTimeCapsule(
                1L,
                10L,
                "세 번째",
                today.plusMonths(2).atStartOfDay(),
                LocalDateTime.of(2026, 8, 2, 12, 0)
        );

        ReflectionTestUtils.setField(first, "financialAccountId", 31L);
        ReflectionTestUtils.setField(second, "financialAccountId", 32L);
        ReflectionTestUtils.setField(third, "financialAccountId", 33L);
        ReflectionTestUtils.setField(third, "expectedReleaseAt", null);
        ReflectionTestUtils.setField(first, "totalContributionAmount",
                new BigDecimal("200000.00"));
        ReflectionTestUtils.setField(second, "totalContributionAmount",
                new BigDecimal("50000.00"));
        ReflectionTestUtils.setField(third, "totalContributionAmount",
                BigDecimal.ZERO);

        given(memberMapper.findById(7L)).willReturn(
                Member.createParent("parent@example.com", "parent", null)
        );
        given(timeCapsuleMapper.existsChildById(10L)).willReturn(true);
        given(timeCapsuleMapper.existsActiveParentRelation(7L, 10L))
                .willReturn(true);
        given(timeCapsuleMapper.findSummariesByChildId(10L))
                .willReturn(List.of(first, second, third));

        TimeCapsuleListResponse response =
                timeCapsuleService.getTimeCapsules(7L, 10L);

        assertEquals(3, response.getTotalCount());
        assertEquals(3L,
                response.getTimeCapsules().get(0).getTimeCapsuleId());
        assertEquals(31L,
                response.getTimeCapsules().get(0).getAccountId());
        assertEquals(23, response.getTimeCapsules().get(0).getDDay());
        assertEquals(new BigDecimal("200000.00"),
                response.getTimeCapsules().get(0).getTotalSavedAmount());
        assertEquals(0, response.getTimeCapsules().get(1).getDDay());
        assertNull(response.getTimeCapsules().get(2).getReleaseDate());
        assertNull(response.getTimeCapsules().get(2).getDDay());
    }

    @Test
    void getTimeCapsulesReturnsEmptyList() {
        stubParentAndChildAccess();
        given(timeCapsuleMapper.findSummariesByChildId(10L))
                .willReturn(List.of());

        TimeCapsuleListResponse response =
                timeCapsuleService.getTimeCapsules(7L, 10L);

        assertTrue(response.getTimeCapsules().isEmpty());
        assertEquals(0, response.getTotalCount());
    }

    @Test
    void getTimeCapsulesDistinguishesMissingChild() {
        given(memberMapper.findById(7L)).willReturn(
                Member.createParent("parent@example.com", "parent", null)
        );
        given(timeCapsuleMapper.existsChildById(10L)).willReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleService.getTimeCapsules(7L, 10L)
        );

        assertEquals(ErrorCode.CHILD_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getTimeCapsulesRejectsInaccessibleChild() {
        given(memberMapper.findById(7L)).willReturn(
                Member.createParent("parent@example.com", "parent", null)
        );
        given(timeCapsuleMapper.existsChildById(10L)).willReturn(true);
        given(timeCapsuleMapper.existsActiveParentRelation(7L, 10L))
                .willReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleService.getTimeCapsules(7L, 10L)
        );

        assertEquals(ErrorCode.CHILD_ACCESS_DENIED,
                exception.getErrorCode());
    }

    @Test
    // [JMG] CAPSULE-6 보관함 삭제는 엔트리·미디어·결과물 객체를 정리한 뒤 DB 하위 행부터 영구 삭제한다.
    void deleteTimeCapsuleDeletesStorageObjectsAndDatabaseRows() {
        TimeCapsule timeCapsule = createTimeCapsule(
                100L,
                10L,
                "깨비의 대학자금 타임캡슐",
                LocalDateTime.of(2030, 7, 23, 0, 0),
                LocalDateTime.of(2026, 8, 5, 10, 0)
        );
        given(timeCapsuleMapper.findAccessibleByIdForUpdate(100L, 7L))
                .willReturn(timeCapsule);
        given(timeCapsuleEntryMapper.lockByTimeCapsuleId(100L))
                .willReturn(List.of(1000L));
        given(timeCapsuleMediaMapper
                .findObjectKeysByTimeCapsuleIdForUpdate(100L))
                .willReturn(List.of(
                        "time-capsules/100/entries/1000/slot-1.jpg"
                ));
        given(timeCapsuleMapper.deleteById(100L)).willReturn(1);

        timeCapsuleService.deleteTimeCapsule(7L, 100L);

        verify(timeCapsuleObjectStorage).deleteObject(
                "time-capsules/100/entries/1000/slot-1.jpg"
        );
        verify(timeCapsuleMediaMapper).deleteByTimeCapsuleId(100L);
        verify(timeCapsuleEntryMapper).deleteByTimeCapsuleId(100L);
        verify(timeCapsuleMapper).deleteById(100L);
    }

    @Test
    // [JMG] CAPSULE-6 S3 객체 삭제 실패 시 DB 하위 행과 보관함은 삭제하지 않는다.
    void deleteTimeCapsuleLeavesDatabaseUntouchedWhenStorageDeletionFails() {
        TimeCapsule timeCapsule = createTimeCapsule(
                100L,
                10L,
                "깨비의 대학자금 타임캡슐",
                LocalDateTime.of(2030, 7, 23, 0, 0),
                LocalDateTime.of(2026, 8, 5, 10, 0)
        );
        String mediaObjectKey =
                "time-capsules/100/entries/1000/slot-1.jpg";
        given(timeCapsuleMapper.findAccessibleByIdForUpdate(100L, 7L))
                .willReturn(timeCapsule);
        given(timeCapsuleEntryMapper.lockByTimeCapsuleId(100L))
                .willReturn(List.of(1000L));
        given(timeCapsuleMediaMapper
                .findObjectKeysByTimeCapsuleIdForUpdate(100L))
                .willReturn(List.of(mediaObjectKey));
        doThrow(new BusinessException(ErrorCode.TIME_CAPSULE_STORAGE_UNAVAILABLE))
                .when(timeCapsuleObjectStorage)
                .deleteObject(mediaObjectKey);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleService.deleteTimeCapsule(7L, 100L)
        );

        assertEquals(ErrorCode.TIME_CAPSULE_STORAGE_UNAVAILABLE,
                exception.getErrorCode());
        verify(timeCapsuleMediaMapper, never()).deleteByTimeCapsuleId(100L);
        verify(timeCapsuleEntryMapper, never()).deleteByTimeCapsuleId(100L);
        verify(timeCapsuleMapper, never()).deleteById(100L);
    }

    @Test
    void deleteTimeCapsuleRejectsInvalidIdentifier() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleService.deleteTimeCapsule(7L, 0L)
        );

        assertEquals(ErrorCode.BADREQUEST, exception.getErrorCode());
        verify(timeCapsuleMapper, never())
                .findAccessibleByIdForUpdate(anyLong(), anyLong());
    }

    @Test
    void deleteTimeCapsuleHidesMissingOrInaccessibleCapsule() {
        given(timeCapsuleMapper.findAccessibleByIdForUpdate(100L, 7L))
                .willReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleService.deleteTimeCapsule(7L, 100L)
        );

        assertEquals(
                ErrorCode.TIME_CAPSULE_NOT_FOUND,
                exception.getErrorCode()
        );
        verify(timeCapsuleEntryMapper, never())
                .lockByTimeCapsuleId(100L);
    }


    // [JMG] CAPSULE-1 테스트용 보관함 생성 가능 계좌를 구성한다.
    private TimeCapsuleAccount createAccount(
            long financialAccountId,
            String ownerType,
            Long ownerMemberId,
            Long childId,
            String accountName,
            String accountProductType,
            String accountStatus,
            String linkStatus
    ) {
        TimeCapsuleAccount account = new TimeCapsuleAccount();
        ReflectionTestUtils.setField(
                account,
                "financialAccountId",
                financialAccountId
        );
        ReflectionTestUtils.setField(account, "ownerType", ownerType);
        ReflectionTestUtils.setField(
                account,
                "ownerMemberId",
                ownerMemberId
        );
        ReflectionTestUtils.setField(account, "childId", childId);
        ReflectionTestUtils.setField(account, "accountName", accountName);
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
        return account;
    }

    private CreateTimeCapsuleRequest createRequest(
            long financialAccountId,
            LocalDate releaseDate
    ) {
        CreateTimeCapsuleRequest request =
                new CreateTimeCapsuleRequest();
        ReflectionTestUtils.setField(
                request,
                "financialAccountId",
                financialAccountId
        );
        ReflectionTestUtils.setField(request, "releaseDate", releaseDate);
        return request;
    }

    private void stubParentAndChildAccess() {
        given(memberMapper.findById(7L)).willReturn(
                Member.createParent("parent@example.com", "부모", null)
        );
        given(timeCapsuleMapper.existsChildById(10L)).willReturn(true);
        given(timeCapsuleMapper.existsActiveParentRelation(7L, 10L))
                .willReturn(true);
    }

    private void stubSuccessfulCreation(
            TimeCapsuleAccount account,
            LocalDate releaseDate
    ) {
        stubParentAndChildAccess();
        given(timeCapsuleMapper.findAccountById(1L)).willReturn(account);
        given(timeCapsuleMapper.findByChildIdAndFinancialAccountId(10L, 1L))
                .willReturn(null);
        doAnswer(invocation -> {
            TimeCapsule created = invocation.getArgument(0);
            ReflectionTestUtils.setField(created, "timeCapsuleId", 100L);
            return 1;
        }).when(timeCapsuleMapper).insert(any(TimeCapsule.class));

        TimeCapsule persisted = TimeCapsule.create(
                10L,
                1L,
                account.getAccountName(),
                releaseDate
        );
        ReflectionTestUtils.setField(persisted, "timeCapsuleId", 100L);
        ReflectionTestUtils.setField(
                persisted,
                "createdAt",
                LocalDateTime.of(2026, 8, 16, 10, 0)
        );
        given(timeCapsuleMapper.findAccessibleById(100L, 7L))
                .willReturn(persisted);
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
