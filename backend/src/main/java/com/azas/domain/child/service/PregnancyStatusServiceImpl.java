package com.azas.domain.child.service;

import com.azas.domain.child.dto.PregnancyCharacterResponse;
import com.azas.domain.child.dto.PregnancyStatusResponse;
import com.azas.domain.child.dto.PregnancyStatusRow;
import com.azas.domain.child.entity.BirthStatus;
import com.azas.domain.child.entity.PregnancyCharacter;
import com.azas.domain.child.mapper.ChildMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PregnancyStatusServiceImpl
        implements PregnancyStatusService {

    private static final long FULL_PREGNANCY_DAYS = 280L;

    private final ChildMapper childMapper;

    private final PregnancyDateProvider pregnancyDateProvider;

    @Override
    @Transactional(readOnly = true)
    public PregnancyStatusResponse getPregnancyStatus(
            Long memberId,
            Long childId
    ) {
        if (childId == null || childId <= 0) {
            throw new BusinessException(
                    ErrorCode.BADREQUEST
            );
        }

        PregnancyStatusRow row =
                childMapper.findPregnancyStatus(
                        childId,
                        memberId
                );

        if (row == null) {
            throw new BusinessException(
                    ErrorCode.CHILD_NOT_FOUND
            );
        }

        if (
                row.getBirthStatus() != BirthStatus.EXPECTED
                        || row.getExpectedBirthDate() == null
        ) {
            throw new BusinessException(
                    ErrorCode.PREGNANCY_STATUS_NOT_AVAILABLE
            );
        }

        LocalDate today = pregnancyDateProvider.today();

        long rawDaysUntilBirth =
                ChronoUnit.DAYS.between(
                        today,
                        row.getExpectedBirthDate()
                );

        long daysUntilBirth =
                Math.max(0L, rawDaysUntilBirth);

        long elapsedDays =
                Math.max(
                        0L,
                        FULL_PREGNANCY_DAYS
                                - rawDaysUntilBirth
                );

        int pregnancyWeek =
                (int) (elapsedDays / 7L);

        int pregnancyDay =
                (int) (elapsedDays % 7L);

        PregnancyCharacter character =
                PregnancyCharacter.currentAt(
                        pregnancyWeek
                );

        PregnancyCharacter nextCharacter =
                PregnancyCharacter.nextAfter(
                        pregnancyWeek
                );

        Long daysUntilNextCharacter =
                calculateDaysUntilNextCharacter(
                        elapsedDays,
                        nextCharacter
                );

        return new PregnancyStatusResponse(
                row.getChildId(),
                row.getChildName(),
                row.getExpectedBirthDate(),
                today,
                pregnancyWeek,
                pregnancyDay,
                daysUntilBirth,
                PregnancyCharacterResponse.from(character),
                PregnancyCharacterResponse.from(nextCharacter),
                daysUntilNextCharacter
        );
    }

    private Long calculateDaysUntilNextCharacter(
            long elapsedDays,
            PregnancyCharacter nextCharacter
    ) {
        if (nextCharacter == null) {
            return null;
        }

        long nextStartDay =
                nextCharacter.getStartWeek() * 7L;

        return Math.max(
                0L,
                nextStartDay - elapsedDays
        );
    }
}