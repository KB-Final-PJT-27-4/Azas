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
    private TimeCapsuleEntryStatus status;
    private LocalDateTime sealedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int mediaCount;

    public static TimeCapsuleEntry createDraft(
            long timeCapsuleId,
            long authorMemberId,
            TimeCapsuleEntryTransaction transaction,
            String title,
            String message
    ) {
        TimeCapsuleEntry entry = new TimeCapsuleEntry();
        entry.timeCapsuleId = timeCapsuleId;
        entry.authorMemberId = authorMemberId;
        entry.accountTransactionId = transaction.getAccountTransactionId();
        entry.title = title;
        entry.message = message;
        entry.contributionAmount = transaction.getAmount();
        entry.contributedAt = transaction.getOccurredAt();
        entry.mediaMode = TimeCapsuleEntryMediaMode.NONE;
        entry.status = TimeCapsuleEntryStatus.DRAFT;
        return entry;
    }

    public boolean isDraft() {
        return status == TimeCapsuleEntryStatus.DRAFT;
    }
}
