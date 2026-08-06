package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleView;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.Base64;

@Getter
public class TimeCapsuleCursor {

    private static final String VERSION = "v1";
    private static final String SEPARATOR = "|";

    private final TimeCapsuleView view;
    private final LocalDateTime sortAt;
    private final long timeCapsuleId;

    private TimeCapsuleCursor(
            TimeCapsuleView view,
            LocalDateTime sortAt,
            long timeCapsuleId
    ) {
        this.view = view;
        this.sortAt = sortAt;
        this.timeCapsuleId = timeCapsuleId;
    }

    // [JMG] CAPSULE-2 프론트에 노출할 목록 조회 keyset cursor를 생성한다.
    public static String encode(
            TimeCapsuleView view,
            TimeCapsule timeCapsule
    ) {
        LocalDateTime sortAt = view == TimeCapsuleView.CARD
                ? timeCapsule.getCreatedAt()
                : timeCapsule.getExpectedReleaseAt();

        if (sortAt == null) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        String payload = String.join(
                SEPARATOR,
                VERSION,
                view.name(),
                sortAt.toString(),
                String.valueOf(timeCapsule.getTimeCapsuleId())
        );

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        payload.getBytes(StandardCharsets.UTF_8)
                );
    }

    // [JMG] CAPSULE-2 프론트가 전달한 목록 조회 keyset cursor를 검증·복원한다.
    public static TimeCapsuleCursor decode(
            String encodedCursor,
            TimeCapsuleView requestedView
    ) {
        if (encodedCursor == null || encodedCursor.isBlank()) {
            return null;
        }

        try {
            String payload = new String(
                    Base64.getUrlDecoder().decode(encodedCursor),
                    StandardCharsets.UTF_8
            );
            String[] values = payload.split("\\|", -1);

            if (values.length != 4 || !VERSION.equals(values[0])) {
                throw new IllegalArgumentException();
            }

            TimeCapsuleView cursorView = TimeCapsuleView.from(values[1]);

            if (cursorView != requestedView) {
                throw new IllegalArgumentException();
            }

            LocalDateTime sortAt = LocalDateTime.parse(values[2]);
            long timeCapsuleId = Long.parseLong(values[3]);

            if (timeCapsuleId <= 0) {
                throw new IllegalArgumentException();
            }

            return new TimeCapsuleCursor(
                    cursorView,
                    sortAt,
                    timeCapsuleId
            );
        } catch (IllegalArgumentException | DateTimeException exception) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }
    }
}
