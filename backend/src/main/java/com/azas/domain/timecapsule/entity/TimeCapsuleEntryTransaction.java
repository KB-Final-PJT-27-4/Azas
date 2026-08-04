package com.azas.domain.timecapsule.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TimeCapsuleEntryTransaction {

    private Long accountTransactionId;
    private AccountTransactionDirection direction;
    private BigDecimal amount;
    private LocalDateTime occurredAt;

    // [JMG] CAPSULE-5 타임캡슐 기록에 연결할 수 있는 입금 거래인지 판별한다.
    public boolean isCredit() {
        return direction == AccountTransactionDirection.CREDIT;
    }
}
