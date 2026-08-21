package com.azas.domain.notification.controller;

import com.azas.domain.notification.dto.PushDeviceRegistrationResult;
import com.azas.domain.notification.dto.PushDeviceResponse;
import com.azas.domain.notification.entity.PushPlatform;
import com.azas.domain.notification.entity.PushProvider;
import com.azas.domain.notification.service.PushDeviceService;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class PushDeviceControllerTest {

    private static final Long MEMBER_ID = 7L;
    private static final String AUTHORIZATION = "Bearer access-token";

    @Mock
    private PushDeviceService pushDeviceService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PushDeviceController controller = new PushDeviceController(
                pushDeviceService,
                accessTokenMemberResolver
        );

        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returns201WhenPushDeviceIsCreated() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(AUTHORIZATION))
                .thenReturn(MEMBER_ID);
        when(pushDeviceService.register(eq(MEMBER_ID), any()))
                .thenReturn(new PushDeviceRegistrationResult(
                        response(),
                        true
                ));

        mockMvc.perform(
                        post("/api/v1/push-devices")
                                .header("Authorization", AUTHORIZATION)
                                .contentType("application/json")
                                .content(validBody())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.push_device_id").value(31))
                .andExpect(jsonPath("$.device_key")
                        .value("web-installation-uuid"))
                .andExpect(jsonPath("$.platform").value("WEB"))
                .andExpect(jsonPath("$.provider").value("FCM"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void returns200WhenExistingPushDeviceIsUpdated()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(AUTHORIZATION))
                .thenReturn(MEMBER_ID);
        when(pushDeviceService.register(eq(MEMBER_ID), any()))
                .thenReturn(new PushDeviceRegistrationResult(
                        response(),
                        false
                ));

        mockMvc.perform(
                        post("/api/v1/push-devices")
                                .header("Authorization", AUTHORIZATION)
                                .contentType("application/json")
                                .content(validBody())
                )
                .andExpect(status().isOk());
    }

    @Test
    void rejectsRequestWithoutPushToken() throws Exception {
        mockMvc.perform(
                        post("/api/v1/push-devices")
                                .header("Authorization", AUTHORIZATION)
                                .contentType("application/json")
                                .content("""
                                        {
                                          "device_key": "web-installation-uuid",
                                          "platform": "WEB",
                                          "provider": "FCM"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code")
                        .value("BADREQUEST"));
    }

    @Test
    void deactivatesOwnedPushDevice() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(AUTHORIZATION))
                .thenReturn(MEMBER_ID);

        mockMvc.perform(
                        delete("/api/v1/push-devices/{id}", 31L)
                                .header("Authorization", AUTHORIZATION)
                )
                .andExpect(status().isNoContent());

        verify(pushDeviceService).unregister(MEMBER_ID, 31L);
    }

    private String validBody() {
        return """
                {
                  "device_key": "web-installation-uuid",
                  "platform": "WEB",
                  "provider": "FCM",
                  "push_token": "fcm-registration-token",
                  "device_name": "MacBook Air"
                }
                """;
    }

    private PushDeviceResponse response() {
        LocalDateTime now = LocalDateTime.of(
                2026, 8, 20, 20, 0
        );
        return new PushDeviceResponse(
                31L,
                "web-installation-uuid",
                PushPlatform.WEB,
                PushProvider.FCM,
                "MacBook Air",
                true,
                now,
                now,
                now
        );
    }
}
