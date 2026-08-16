package com.azas.domain.child.service;

import com.azas.domain.child.dto.PregnancyStatusResponse;
import com.azas.domain.child.dto.PregnancyStatusRow;
import com.azas.domain.child.entity.BirthStatus;
import com.azas.domain.child.mapper.ChildMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PregnancyStatusServiceTest {

    private static final Long MEMBER_ID = 7L;
    private static final Long CHILD_ID = 3L;

    private static final LocalDate TODAY =
            LocalDate.of(2026, 8, 17);

    @Mock
    private ChildMapper childMapper;

    @Mock
    private PregnancyDateProvider pregnancyDateProvider;

    private PregnancyStatusService pregnancyStatusService;

    @BeforeEach
    void setUp() {
        pregnancyStatusService =
                new PregnancyStatusServiceImpl(
                        childMapper,
                        pregnancyDateProvider
                );
    }

    @Test
    void getsPregnancyWeekAndCharacter() {
        // 16주 2일 = 114일 경과
        // 출산까지 280 - 114 = 166일
        LocalDate expectedBirthDate =
                TODAY.plusDays(166);

        when(pregnancyDateProvider.today())
                .thenReturn(TODAY);

        when(childMapper.findPregnancyStatus(
                CHILD_ID,
                MEMBER_ID
        )).thenReturn(
                new PregnancyStatusRow(
                        CHILD_ID,
                        "깨비",
                        BirthStatus.EXPECTED,
                        expectedBirthDate
                )
        );

        PregnancyStatusResponse response =
                pregnancyStatusService.getPregnancyStatus(
                        MEMBER_ID,
                        CHILD_ID
                );

        assertEquals(16, response.getPregnancyWeek());
        assertEquals(2, response.getPregnancyDay());
        assertEquals(166L, response.getDaysUntilBirth());

        assertNotNull(response.getCharacter());
        assertEquals(
                "AVOCADO",
                response.getCharacter().getCode()
        );

        assertNotNull(response.getNextCharacter());
        assertEquals(
                "MANGO",
                response.getNextCharacter().getCode()
        );

        assertEquals(
                19L,
                response.getDaysUntilNextCharacter()
        );
    }

    @Test
    void throwsNotFoundWhenChildIsNotAccessible() {
        when(childMapper.findPregnancyStatus(
                CHILD_ID,
                MEMBER_ID
        )).thenReturn(null);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> pregnancyStatusService
                                .getPregnancyStatus(
                                        MEMBER_ID,
                                        CHILD_ID
                                )
                );

        assertEquals(
                ErrorCode.CHILD_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsBornChild() {
        when(childMapper.findPregnancyStatus(
                CHILD_ID,
                MEMBER_ID
        )).thenReturn(
                new PregnancyStatusRow(
                        CHILD_ID,
                        "깨비",
                        BirthStatus.BORN,
                        null
                )
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> pregnancyStatusService
                                .getPregnancyStatus(
                                        MEMBER_ID,
                                        CHILD_ID
                                )
                );

        assertEquals(
                ErrorCode.PREGNANCY_STATUS_NOT_AVAILABLE,
                exception.getErrorCode()
        );

        verifyNoInteractions(pregnancyDateProvider);
    }
}