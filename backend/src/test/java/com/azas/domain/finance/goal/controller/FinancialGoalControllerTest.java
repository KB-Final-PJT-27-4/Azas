package com.azas.domain.finance.goal.controller;

import com.azas.domain.finance.goal.dto.FinancialGoalCreateCommand;
import com.azas.domain.finance.goal.dto.FinancialGoalCreateResult;
import com.azas.domain.finance.goal.dto.FinancialGoalCheckpointResult;
import com.azas.domain.finance.goal.dto.FinancialGoalDetailResult;
import com.azas.domain.finance.goal.dto.FinancialGoalLinkedAccountResult;
import com.azas.domain.finance.goal.dto.FinancialGoalListAccountResult;
import com.azas.domain.finance.goal.dto.FinancialGoalListItemResult;
import com.azas.domain.finance.goal.dto.FinancialGoalListResult;
import com.azas.domain.finance.goal.service.FinancialGoalCreateService;
import com.azas.domain.finance.goal.service.FinancialGoalDetailService;
import com.azas.domain.finance.goal.service.FinancialGoalListService;
import com.azas.domain.finance.goal.service.FinancialGoalUpdateService;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FinancialGoalControllerTest {

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    @Mock
    private FinancialGoalCreateService financialGoalCreateService;

    @Mock
    private FinancialGoalDetailService financialGoalDetailService;

    @Mock
    private FinancialGoalListService financialGoalListService;

    @Mock
    private FinancialGoalUpdateService financialGoalUpdateService;

    @InjectMocks
    private FinancialGoalController financialGoalController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders
                .standaloneSetup(financialGoalController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
    }

    @Test
    void updatesFinancialGoalAndReturnsDetail() throws Exception {
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(8L);
        given(financialGoalUpdateService.update(
                org.mockito.ArgumentMatchers.eq(8L),
                org.mockito.ArgumentMatchers.eq(31L),
                any()
        )).willReturn(detailResult());

        mockMvc.perform(patch("/api/v1/financial-goals/31")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "target_amount": 40000000,
                                  "target_date": "2046-03-31",
                                  "account_ids": [11, 12]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financial_goal_id").value(31))
                .andExpect(jsonPath("$.child_id").value(6))
                .andExpect(jsonPath("$.linked_account_count").value(1))
                .andExpect(jsonPath("$.checkpoints[0].percentage").value(10));

        org.mockito.ArgumentCaptor<com.azas.domain.finance.goal.dto.FinancialGoalUpdateRequest>
                captor = org.mockito.ArgumentCaptor.forClass(
                com.azas.domain.finance.goal.dto.FinancialGoalUpdateRequest.class);
        verify(financialGoalUpdateService).update(
                org.mockito.ArgumentMatchers.eq(8L),
                org.mockito.ArgumentMatchers.eq(31L), captor.capture());
        assertEquals(new BigDecimal("40000000"), captor.getValue().getTargetAmount());
        assertEquals(LocalDate.of(2046, 3, 31), captor.getValue().getTargetDate());
        assertEquals(List.of(11L, 12L), captor.getValue().getAccountIds());
    }

    @Test
    void createsTemplateGoalWithMultipleSavingsAccounts() throws Exception {
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(8L);
        given(financialGoalCreateService.create(anyLong(), anyLong(),
                any(FinancialGoalCreateCommand.class)))
                .willReturn(result());

        mockMvc.perform(post("/api/v1/children/6/financial-goals")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "financial_goal_template_id": 1,
                                  "title": null,
                                  "target_amount": 30000000,
                                  "target_date": "2045-03-31",
                                  "account_ids": [11, 12]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.financial_goal_id").value(31))
                .andExpect(jsonPath("$.child_id").value(6))
                .andExpect(jsonPath("$.financial_goal_template_id").value(1))
                .andExpect(jsonPath("$.title").value("대학자금"))
                .andExpect(jsonPath("$.target_amount").value(30000000))
                .andExpect(jsonPath("$.target_date").value("2045-03-31"))
                .andExpect(jsonPath("$.monthly_saving_amount").value(125000))
                .andExpect(jsonPath("$.current_amount").value(9600000))
                .andExpect(jsonPath("$.remaining_amount").value(20400000))
                .andExpect(jsonPath("$.achievement_rate").value(32.0))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.linked_account_count").value(2))
                .andExpect(jsonPath("$.linked_accounts[0].account_id").value(11))
                .andExpect(jsonPath("$.linked_accounts[0].account_name")
                        .value("KB 아이사랑적금1"))
                .andExpect(jsonPath("$.linked_accounts[0].bank_name")
                        .value("KB국민은행"))
                .andExpect(jsonPath("$.linked_accounts[0].balance")
                        .value(4800000))
                .andExpect(jsonPath("$.linked_accounts[0].account_number")
                        .doesNotExist())
                .andExpect(jsonPath("$.created_at")
                        .value("2026-08-18T03:00:00Z"));

        ArgumentCaptor<FinancialGoalCreateCommand> commandCaptor =
                ArgumentCaptor.forClass(FinancialGoalCreateCommand.class);
        verify(financialGoalCreateService).create(
                org.mockito.ArgumentMatchers.eq(8L),
                org.mockito.ArgumentMatchers.eq(6L),
                commandCaptor.capture()
        );
        FinancialGoalCreateCommand command = commandCaptor.getValue();
        assertEquals(1L, command.getFinancialGoalTemplateId());
        assertNull(command.getTitle());
        assertEquals(0, command.getTargetAmount()
                .compareTo(new BigDecimal("30000000")));
        assertEquals(LocalDate.of(2045, 3, 31), command.getTargetDate());
        assertEquals(List.of(11L, 12L), command.getAccountIds());
    }

    @Test
    void returnsFinancialGoalsWithDecryptedAccountNumbers() throws Exception {
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(8L);
        given(financialGoalListService.getGoals(8L, 6L))
                .willReturn(listResult());

        mockMvc.perform(get("/api/v1/children/6/financial-goals")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.child_id").value(6))
                .andExpect(jsonPath("$.total_count").value(1))
                .andExpect(jsonPath("$.financial_goals[0].financial_goal_id")
                        .value(31))
                .andExpect(jsonPath("$.financial_goals[0].financial_goal_template_id")
                        .value(1))
                .andExpect(jsonPath("$.financial_goals[0].icon_key")
                        .value("goal-university"))
                .andExpect(jsonPath("$.financial_goals[0].remaining_amount")
                        .value(20400000))
                .andExpect(jsonPath("$.financial_goals[0].linked_account_count")
                        .value(1))
                .andExpect(jsonPath("$.financial_goals[0].linked_accounts[0].account_number")
                        .value("952-17362605-43"))
                .andExpect(jsonPath("$..account_number_ciphertext")
                        .doesNotExist());

        verify(financialGoalListService).getGoals(8L, 6L);
    }

    @Test
    void returnsEmptyGoalList() throws Exception {
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(8L);
        given(financialGoalListService.getGoals(8L, 6L))
                .willReturn(new FinancialGoalListResult(6L, List.of()));

        mockMvc.perform(get("/api/v1/children/6/financial-goals")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.child_id").value(6))
                .andExpect(jsonPath("$.total_count").value(0))
                .andExpect(jsonPath("$.financial_goals").isEmpty());
    }

    @Test
    void returnsFinancialGoalDetailWithAccountsAndCheckpoints()
            throws Exception {
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(8L);
        given(financialGoalDetailService.getGoal(8L, 31L))
                .willReturn(detailResult());

        mockMvc.perform(get("/api/v1/financial-goals/31")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financial_goal_id").value(31))
                .andExpect(jsonPath("$.child_id").value(6))
                .andExpect(jsonPath("$.financial_goal_template_id").value(1))
                .andExpect(jsonPath("$.title").value("대학자금"))
                .andExpect(jsonPath("$.icon_key").value("goal-university"))
                .andExpect(jsonPath("$.target_amount").value(30000000))
                .andExpect(jsonPath("$.target_date").value("2045-03-31"))
                .andExpect(jsonPath("$.monthly_saving_amount").value(125000))
                .andExpect(jsonPath("$.current_amount").value(9600000))
                .andExpect(jsonPath("$.remaining_amount").value(20400000))
                .andExpect(jsonPath("$.achievement_rate").value(32.0))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.linked_account_count").value(1))
                .andExpect(jsonPath("$.linked_accounts[0].account_number")
                        .value("952-17362605-43"))
                .andExpect(jsonPath("$.checkpoints[0].financial_goal_checkpoint_id")
                        .value(101))
                .andExpect(jsonPath("$.checkpoints[0].percentage").value(10))
                .andExpect(jsonPath("$.checkpoints[0].reached").value(true))
                .andExpect(jsonPath("$.checkpoints[0].reached_at")
                        .value("2027-01-15T09:00:00Z"))
                .andExpect(jsonPath("$.recent_transfers").doesNotExist())
                .andExpect(jsonPath("$.account").doesNotExist())
                .andExpect(jsonPath("$.child").doesNotExist())
                .andExpect(jsonPath("$..account_number_ciphertext")
                        .doesNotExist());

        verify(financialGoalDetailService).getGoal(8L, 31L);
    }

    @Test
    void returnsNotFoundForHiddenFinancialGoal() throws Exception {
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(8L);
        given(financialGoalDetailService.getGoal(8L, 31L))
                .willThrow(new BusinessException(
                        ErrorCode.FINANCIAL_GOAL_NOT_FOUND
                ));

        mockMvc.perform(get("/api/v1/financial-goals/31")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code")
                        .value("FINANCIAL_GOAL_NOT_FOUND"));
    }

    @Test
    void returnsConflictWhenSavingsAccountAlreadyHasGoal() throws Exception {
        given(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).willReturn(8L);
        given(financialGoalCreateService.create(anyLong(), anyLong(),
                any(FinancialGoalCreateCommand.class)))
                .willThrow(new BusinessException(
                        ErrorCode.FINANCIAL_ACCOUNT_GOAL_ALREADY_ASSIGNED
                ));

        mockMvc.perform(post("/api/v1/children/6/financial-goals")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "financial_goal_template_id": null,
                                  "title": "유학자금",
                                  "target_amount": 50000000,
                                  "target_date": "2040-12-31",
                                  "account_ids": [11]
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code")
                        .value("FINANCIAL_ACCOUNT_GOAL_ALREADY_ASSIGNED"));
    }

    private FinancialGoalCreateResult result() {
        return new FinancialGoalCreateResult(
                31L,
                6L,
                1L,
                "대학자금",
                new BigDecimal("30000000"),
                LocalDate.of(2045, 3, 31),
                new BigDecimal("125000"),
                new BigDecimal("9600000"),
                new BigDecimal("20400000"),
                new BigDecimal("32.0"),
                "ACTIVE",
                List.of(
                        new FinancialGoalLinkedAccountResult(
                                11L,
                                "KB 아이사랑적금1",
                                "KB국민은행",
                                new BigDecimal("4800000")
                        ),
                        new FinancialGoalLinkedAccountResult(
                                12L,
                                "KB 아이사랑적금2",
                                "KB국민은행",
                                new BigDecimal("4800000")
                        )
                ),
                Instant.parse("2026-08-18T03:00:00Z")
        );
    }

    private FinancialGoalListResult listResult() {
        return new FinancialGoalListResult(
                6L,
                List.of(new FinancialGoalListItemResult(
                        31L,
                        1L,
                        "대학자금",
                        "goal-university",
                        new BigDecimal("30000000"),
                        new BigDecimal("9600000"),
                        new BigDecimal("20400000"),
                        new BigDecimal("32.0"),
                        LocalDate.of(2045, 3, 31),
                        "ACTIVE",
                        List.of(new FinancialGoalListAccountResult(
                                11L,
                                "KB 아이사랑적금1",
                                "KB국민은행",
                                "952-17362605-43",
                                new BigDecimal("4800000")
                        ))
                ))
        );
    }

    private FinancialGoalDetailResult detailResult() {
        return new FinancialGoalDetailResult(
                31L,
                6L,
                1L,
                "대학자금",
                "goal-university",
                new BigDecimal("30000000.00"),
                LocalDate.of(2045, 3, 31),
                new BigDecimal("125000.00"),
                new BigDecimal("9600000.00"),
                new BigDecimal("20400000.00"),
                new BigDecimal("32.0"),
                "ACTIVE",
                List.of(new FinancialGoalListAccountResult(
                        11L,
                        "아이사랑적금1",
                        "KB국민은행",
                        "952-17362605-43",
                        new BigDecimal("9600000.00")
                )),
                List.of(new FinancialGoalCheckpointResult(
                        101L,
                        10,
                        new BigDecimal("3000000.00"),
                        true,
                        Instant.parse("2027-01-15T09:00:00Z")
                ))
        );
    }
}
