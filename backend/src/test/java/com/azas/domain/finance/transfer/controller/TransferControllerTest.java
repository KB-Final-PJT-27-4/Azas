package com.azas.domain.finance.transfer.controller;

import com.azas.domain.finance.transfer.dto.TransferAccountResponse;
import com.azas.domain.finance.transfer.dto.TransferCreateResponse;
import com.azas.domain.finance.transfer.entity.TransferStatus;
import com.azas.domain.finance.transfer.entity.TransferType;
import com.azas.domain.finance.transfer.service.TransferService;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransferControllerTest {

    private MockMvc mockMvc;
    private TransferService transferService;
    private AccessTokenMemberResolver accessTokenMemberResolver;

    @BeforeEach
    void setUp() {
        transferService = org.mockito.Mockito.mock(TransferService.class);
        accessTokenMemberResolver = org.mockito.Mockito.mock(AccessTokenMemberResolver.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new TransferController(transferService, accessTokenMemberResolver)
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void 수동이체_요청은_200과_SUCCEEDED를_반환한다() throws Exception {
        given(accessTokenMemberResolver.resolveMemberId("Bearer access-token"))
                .willReturn(1L);
        given(transferService.createTransfer(anyLong(), anyString(), any()))
                .willReturn(new TransferCreateResponse(
                        5001L,
                        3L,
                        new TransferAccountResponse(301L, "KB국민은행", "생활비 통장", "987-****-54321"),
                        new TransferAccountResponse(300L, "KB국민은행", "대학자금 적금", "123-****-56789"),
                        new BigDecimal("100000"),
                        "8월 대학자금",
                        TransferType.MANUAL,
                        TransferStatus.SUCCEEDED,
                        Instant.now()
                ));

        mockMvc.perform(post("/api/v1/transfers")
                        .header("Authorization", "Bearer access-token")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"source_account_id\":301,\"destination_account_id\":300,\"amount\":100000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financial_transfer_id").value(5001))
                .andExpect(jsonPath("$.financial_goal_id").value(3))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"));
    }

    @Test
    void 수동이체_요청에서_금액이_없으면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/api/v1/transfers")
                        .header("Authorization", "Bearer access-token")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"source_account_id\":301,\"destination_account_id\":300}"))
                .andExpect(status().isBadRequest());
    }
}