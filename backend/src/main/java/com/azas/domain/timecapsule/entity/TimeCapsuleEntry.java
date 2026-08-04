package com.azas.domain.timecapsule.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    // [JMG] CAPSULE-5 적금 입금 거래의 금액과 시각을 스냅샷으로 보관하는 기록을 생성한다.
    public static TimeCapsuleEntry create(
            long timeCapsuleId,
            long authorMemberId,
            TimeCapsuleEntryTransaction transaction,
            String title,
            String message,
            TimeCapsuleEntryMediaMode mediaMode
    ) {
        TimeCapsuleEntry entry = new TimeCapsuleEntry();
        entry.timeCapsuleId = timeCapsuleId;
        entry.authorMemberId = authorMemberId;
        entry.accountTransactionId = transaction.getAccountTransactionId();
        entry.title = title.trim();
        entry.message = message.trim();
        entry.contributionAmount = transaction.getAmount();
        entry.contributedAt = transaction.getOccurredAt();
        entry.mediaMode = mediaMode;
        entry.status = TimeCapsuleEntryStatus.DRAFT;
        return entry;
    }
}
