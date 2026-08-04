package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsuleView;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeCapsuleCursorTest {

    @Test
    // [JMG] CAPSULE-2 커서 안의 날짜 형식이 잘못되면 400 오류로 변환한다.
    void decodeRejectsInvalidDateTime() {
        String invalidCursor = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        "v1|CARD|invalid-date-time|1"
                                .getBytes(StandardCharsets.UTF_8)
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> TimeCapsuleCursor.decode(
                        invalidCursor,
                        TimeCapsuleView.CARD
                )
        );

        assertEquals(
                ErrorCode.BADREQUEST,
                exception.getErrorCode()
        );
    }
}
