package com.azas.domain.timecapsule.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeCapsuleEntry {

    private Long timeCapsuleEntryId;
    private Long timeCapsuleId;
    private Long authorMemberId;
    private Long accountTransactionId;
    private String title;
    private String message;
    private BigDecimal contributionAmount;
    private LocalDateTime contributedAt;
    private TimeCapsuleEntryMediaMode mediaMode;
    private String thumbnailObjectKey;
    private String blurredThumbnailObjectKey;
    private TimeCapsuleEntryStatus status;
    private int editCount;
    private LocalDateTime sealedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int mediaCount;

    private static final DateTimeFormatter AUTO_TITLE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일");

    // [JMG] CAPSULE-5 성공한 적금 입금 거래를 기준으로 수정 가능한 엔트리 초안을 자동 생성한다.
    public static TimeCapsuleEntry createDraftForSuccessfulTransfer(
            long timeCapsuleId,
            long authorMemberId,
            TimeCapsuleEntryTransaction transaction
    ) {
        TimeCapsuleEntry entry = new TimeCapsuleEntry();
        entry.timeCapsuleId = timeCapsuleId;
        entry.authorMemberId = authorMemberId;
        entry.accountTransactionId = transaction.getAccountTransactionId();
        entry.title = transaction.getOccurredAt()
                .toLocalDate()
                .format(AUTO_TITLE_DATE_FORMATTER) + " 저축 기록";
        entry.message = null;
        entry.contributionAmount = transaction.getAmount();
        entry.contributedAt = transaction.getOccurredAt();
        entry.mediaMode = TimeCapsuleEntryMediaMode.NONE;
        entry.status = TimeCapsuleEntryStatus.DRAFT;
        return entry;
    }

    // [JMG] CAPSULE-12 엔트리가 수정과 봉인이 가능한 작성 중 상태인지 반환한다.
    public boolean isDraft() {
        return status == TimeCapsuleEntryStatus.DRAFT;
    }
}
