package com.azas.domain.report.service;

import com.azas.domain.report.dto.AssetReportGoalAccountRow;
import com.azas.domain.report.dto.AssetReportUpsertCommand;
import com.azas.domain.report.mapper.AssetReportMapper;
import com.azas.global.security.AccountNumberProtector;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssetReportSnapshotServiceTest {

    private static final Long CHILD_ID = 6L;

    @Mock
    private AssetReportMapper assetReportMapper;

    @Mock
    private AccountNumberProtector accountNumberProtector;

    private ObjectMapper objectMapper;

    private AssetReportSnapshotService snapshotService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        snapshotService = new AssetReportSnapshotService(
                assetReportMapper,
                objectMapper,
                accountNumberProtector
        );
    }

    @Test
    void 실제_목표와_연결계좌를_스냅샷에_저장한다()
            throws Exception {
        mockAmounts();

        AssetReportGoalAccountRow row =
                new AssetReportGoalAccountRow();
        row.setFinancialGoalId(100L);
        row.setTitle("대학자금");
        row.setTargetAmount(new BigDecimal("30000000"));
        row.setMonthlySavingTargetAmount(
                new BigDecimal("300000")
        );
        row.setAccountId(3L);
        row.setAccountName("아이사랑적금");
        row.setBankName("KB국민은행");
        row.setAccountNumberCiphertext(new byte[]{1, 2, 3});
        row.setBalance(new BigDecimal("9600000"));
        row.setMonthlySavedAmount(new BigDecimal("120000"));

        when(assetReportMapper.findGoalAccountSnapshots(
                eq(CHILD_ID),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(row));

        when(accountNumberProtector.decrypt(
                row.getAccountNumberCiphertext()
        )).thenReturn("952-1234-5643");

        snapshotService.generateForChild(
                CHILD_ID,
                YearMonth.of(2026, 8)
        );

        ArgumentCaptor<AssetReportUpsertCommand> captor =
                ArgumentCaptor.forClass(
                        AssetReportUpsertCommand.class
                );

        verify(assetReportMapper).upsertAssetReport(
                captor.capture()
        );

        AssetReportUpsertCommand command = captor.getValue();
        JsonNode goal = objectMapper.readTree(
                command.getSavingsGoalSummaryJson()
        ).get(0);

        assertEquals(
                new BigDecimal("30000000"),
                command.getTotalGoalTargetAmount()
        );
        assertEquals(
                new BigDecimal("9600000"),
                command.getTotalGoalSavedAmount()
        );
        assertEquals("대학자금", goal.get("title").asText());
        assertEquals(
                "952-****-**43",
                goal.get("linked_accounts")
                        .get(0)
                        .get("account_number_masked")
                        .asText()
        );
    }

    private void mockAmounts() {
        when(assetReportMapper.findTotalAssetAmountAt(
                any(),
                any(LocalDateTime.class)
        )).thenReturn(new BigDecimal("500000"));

        when(assetReportMapper.findPreviousTotalAssetAmount(
                CHILD_ID,
                YearMonth.of(2026, 7).atDay(1)
        )).thenReturn(new BigDecimal("400000"));

        when(assetReportMapper.findMonthlySavedAmount(
                any(),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(new BigDecimal("120000"));

        when(assetReportMapper.findPreviousMonthlySavedAmount(
                CHILD_ID,
                YearMonth.of(2026, 7).atDay(1)
        )).thenReturn(new BigDecimal("30000"));
    }

    @Test
    void 전월_저축액과_비교한_인사이트를_저장한다() throws Exception {
        when(assetReportMapper.findTotalAssetAmountAt(
                any(),
                any(LocalDateTime.class)
        )).thenReturn(new BigDecimal("500000"));

        when(assetReportMapper.findPreviousTotalAssetAmount(
                CHILD_ID,
                YearMonth.of(2026, 7).atDay(1)
        )).thenReturn(new BigDecimal("400000"));

        when(assetReportMapper.findMonthlySavedAmount(
                any(),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(new BigDecimal("120000"));

        when(assetReportMapper.findPreviousMonthlySavedAmount(
                CHILD_ID,
                YearMonth.of(2026, 7).atDay(1)
        )).thenReturn(new BigDecimal("30000"));

        snapshotService.generateForChild(
                CHILD_ID,
                YearMonth.of(2026, 8)
        );

        ArgumentCaptor<AssetReportUpsertCommand> captor =
                ArgumentCaptor.forClass(
                        AssetReportUpsertCommand.class
                );

        verify(assetReportMapper).upsertAssetReport(
                captor.capture()
        );

        JsonNode insight = objectMapper.readTree(
                captor.getValue().getInsightItemsJson()
        ).get(0);

        assertEquals(
                "MONTHLY_SAVING_COMPARISON",
                insight.get("type").asText()
        );
        assertEquals(
                "지난달보다 90000원을 더 저축했어요.",
                insight.get("title").asText()
        );
    }
}
