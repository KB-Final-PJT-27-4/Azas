package com.azas.domain.notification.service;

import com.azas.domain.notification.dto.NotificationInsertCommand;
import com.azas.domain.notification.dto.TimeCapsuleReleaseNotificationTarget;
import com.azas.domain.notification.dto.UsageLimitNotificationTarget;
import com.azas.domain.notification.mapper.ProactiveNotificationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProactiveNotificationServiceTest {

    @Mock
    private ProactiveNotificationMapper mapper;

    @Test
    void 월사용한도를_초과하면_부모에게_인앱알림을_저장한다() {
        ProactiveNotificationService service = new ProactiveNotificationService(
                mapper
        );
        UsageLimitNotificationTarget target = usageTarget("10000", "12000");
        when(mapper.findUsageLimitTargets(any(), any())).thenReturn(List.of(target));
        when(mapper.findEnabledParentRecipientIds(10L, "USAGE_LIMIT"))
                .thenReturn(List.of(1L));
        when(mapper.insertNotification(any())).thenReturn(1);

        service.notifyUsageLimit(YearMonth.of(2026, 8), LocalDateTime.of(2026, 8, 10, 12, 0));

        ArgumentCaptor<NotificationInsertCommand> command = ArgumentCaptor.forClass(NotificationInsertCommand.class);
        verify(mapper).insertNotification(command.capture());
        assertEquals("USAGE_GUIDE_AMOUNT_EXCEEDED", command.getValue().getType());
        assertEquals("USAGE_LIMIT", command.getValue().getCategory());
    }

    @Test
    void 월사용한도에_도달하면_도달알림을_보낸다() {
        ProactiveNotificationService service = new ProactiveNotificationService(
                mapper
        );
        when(mapper.findUsageLimitTargets(any(), any()))
                .thenReturn(List.of(usageTarget("10000", "10000")));
        when(mapper.findEnabledParentRecipientIds(10L, "USAGE_LIMIT"))
                .thenReturn(List.of(1L));
        when(mapper.insertNotification(any())).thenReturn(1);

        service.notifyUsageLimit(
                YearMonth.of(2026, 8),
                LocalDateTime.of(2026, 8, 10, 12, 0)
        );

        ArgumentCaptor<NotificationInsertCommand> command =
                ArgumentCaptor.forClass(NotificationInsertCommand.class);
        verify(mapper).insertNotification(command.capture());
        assertEquals("USAGE_GUIDE_AMOUNT_REACHED", command.getValue().getType());
    }

    @Test
    void 공개일이되면_타임캡슐_인앱알림을_저장한다() {
        ProactiveNotificationService service = new ProactiveNotificationService(
                mapper
        );
        TimeCapsuleReleaseNotificationTarget target = new TimeCapsuleReleaseNotificationTarget();
        target.setTimeCapsuleId(50L);
        target.setChildId(10L);
        target.setTitle("첫 생일");
        when(mapper.findDueTimeCapsules(any())).thenReturn(List.of(target));
        when(mapper.findEnabledParentRecipientIds(10L, "TIME_CAPSULE"))
                .thenReturn(List.of(1L));
        when(mapper.insertNotification(any())).thenReturn(1);

        service.notifyTimeCapsuleRelease(LocalDateTime.of(2026, 8, 10, 12, 0));

        ArgumentCaptor<NotificationInsertCommand> command = ArgumentCaptor.forClass(NotificationInsertCommand.class);
        verify(mapper).insertNotification(command.capture());
        assertEquals("TIME_CAPSULE_RELEASED", command.getValue().getType());
        assertEquals("TIME_CAPSULE", command.getValue().getCategory());
    }

    private UsageLimitNotificationTarget usageTarget(String budget, String spent) {
        UsageLimitNotificationTarget target = new UsageLimitNotificationTarget();
        target.setChildId(10L);
        target.setAccountId(100L);
        target.setBudgetAmount(new BigDecimal(budget));
        target.setSpentAmount(new BigDecimal(spent));
        return target;
    }
}
