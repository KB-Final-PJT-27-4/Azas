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

    // [JMG] CAPSULE-5 부모가 선택한 입금 거래와 작성 내용을 기준으로 엔트리 초안을 생성한다.
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

    // [JMG] CAPSULE-12 엔트리가 수정과 봉인이 가능한 작성 중 상태인지 반환한다.
    public boolean isDraft() {
        return status == TimeCapsuleEntryStatus.DRAFT;
    }
}
