package com.azas.domain.notification.service;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PushMessageTest {

    @Test
    void copiesAndFiltersDataPayload() {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("notification_id", "100");
        data.put("nullable", null);

        PushMessage pushMessage = new PushMessage(
                " 제목 ",
                " 내용 ",
                " /notifications/100 ",
                data
        );
        data.put("notification_id", "changed");

        assertEquals("제목", pushMessage.getTitle());
        assertEquals("내용", pushMessage.getBody());
        assertEquals(
                "/notifications/100",
                pushMessage.getActionUrl()
        );
        assertEquals(
                Map.of("notification_id", "100"),
                pushMessage.getData()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> pushMessage.getData()
                        .put("new", "value")
        );
    }
}
