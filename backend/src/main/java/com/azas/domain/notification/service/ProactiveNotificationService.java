package com.azas.domain.notification.service;

import com.azas.domain.notification.dto.NotificationInsertCommand;
import com.azas.domain.notification.dto.TimeCapsuleReleaseNotificationTarget;
import com.azas.domain.notification.dto.UsageLimitNotificationTarget;
import com.azas.domain.notification.mapper.ProactiveNotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ProactiveNotificationService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private final ProactiveNotificationMapper mapper;

    @Scheduled(fixedDelayString = "${notification.proactive-check-delay-millis:5000}")
    @Transactional
    public void checkAndNotify() {
        LocalDateTime now = LocalDateTime.now(SEOUL_ZONE);
        YearMonth month = YearMonth.from(now);
        notifyUsageLimit(month, now);
        notifyTimeCapsuleRelease(now);
    }

    void notifyUsageLimit(YearMonth month, LocalDateTime now) {
        List<UsageLimitNotificationTarget> targets = mapper.findUsageLimitTargets(
                month.atDay(1).atStartOfDay(),
                month.plusMonths(1).atDay(1).atStartOfDay()
        );
        for (UsageLimitNotificationTarget target : targets) {
            boolean exceeded = target.getSpentAmount().compareTo(target.getBudgetAmount()) > 0;
            String type = exceeded ? "USAGE_GUIDE_AMOUNT_EXCEEDED" : "USAGE_GUIDE_AMOUNT_REACHED";
            String title = exceeded ? "자녀 사용 한도를 초과했어요" : "자녀 사용 한도에 도달했어요";
            String content = String.format(Locale.KOREA,
                    "이번 달 사용금액 %,.0f원이 설정 한도 %,.0f원%s.",
                    target.getSpentAmount(), target.getBudgetAmount(), exceeded ? "을 넘었어요" : "에 도달했어요");
            notifyParents(target.getChildId(), "USAGE_LIMIT", type, title, content,
                    "FINANCIAL_ACCOUNT", target.getAccountId(),
                    type + ":" + target.getAccountId() + ":" + month, now);
        }
    }

    void notifyTimeCapsuleRelease(LocalDateTime now) {
        for (TimeCapsuleReleaseNotificationTarget target : mapper.findDueTimeCapsules(now)) {
            String title = "타임캡슐을 열 수 있어요";
            String content = target.getTitle() + " 타임캡슐의 공개일이 되었어요.";
            notifyParents(target.getChildId(), "TIME_CAPSULE", "TIME_CAPSULE_RELEASED",
                    title, content, "TIME_CAPSULE", target.getTimeCapsuleId(),
                    "TIME_CAPSULE_RELEASED:" + target.getTimeCapsuleId(), now);
        }
    }

    private void notifyParents(long childId, String category, String type,
            String title, String content, String referenceType, Long referenceId,
            String deduplicationPrefix, LocalDateTime now) {
        for (Long memberId : mapper.findEnabledParentRecipientIds(childId, category)) {
            NotificationInsertCommand command = new NotificationInsertCommand(memberId, childId,
                    category, type, title, content, referenceType, referenceId,
                    "{\"reference_id\":" + referenceId + "}",
                    deduplicationPrefix + ":" + memberId, now);
            mapper.insertNotification(command);
        }
    }
}
