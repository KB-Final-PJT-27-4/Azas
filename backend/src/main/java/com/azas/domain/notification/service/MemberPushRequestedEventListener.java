package com.azas.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MemberPushRequestedEventListener {

    private static final Logger log = LoggerFactory.getLogger(
            MemberPushRequestedEventListener.class
    );

    private final MemberPushService memberPushService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void handle(MemberPushRequestedEvent event) {
        try {
            memberPushService.sendToMember(
                    event.getMemberId(),
                    event.getPushMessage()
            );
        } catch (RuntimeException exception) {
            // 푸시 게이트웨이 또는 기기 조회 장애가 이미 완료된
            // 핵심 업무 트랜잭션에 영향을 주지 않게 격리한다.
            log.warn(
                    "Committed business event push delivery failed. memberId={}",
                    event.getMemberId(),
                    exception
            );
        }
    }
}
