package com.azas.domain.report.service;

import com.azas.domain.report.dto.AssetReportGoalAccountRow;
import com.azas.domain.report.dto.AssetReportUpsertCommand;
import com.azas.domain.report.mapper.AssetReportMapper;
import com.azas.global.security.AccountNumberProtector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AssetReportSnapshotService {

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private static final ZoneId SERVICE_ZONE =
            ZoneId.of("Asia/Seoul");

    private final AssetReportMapper assetReportMapper;
    private final ObjectMapper objectMapper;
    private final AccountNumberProtector accountNumberProtector;

    @Transactional
    public void generateForChild(
            Long childId,
            YearMonth targetMonth
    ) {
        LocalDate reportMonth =
                targetMonth.atDay(1);

        LocalDateTime startAt =
                toUtcStart(targetMonth);

        LocalDateTime endExclusive =
                toUtcStart(targetMonth.plusMonths(1));

        BigDecimal totalAssetAmount = zeroIfNull(
                assetReportMapper.findTotalAssetAmountAt(
                        childId,
                        endExclusive
                )
        );

        BigDecimal previousTotalAssetAmount =
                assetReportMapper.findPreviousTotalAssetAmount(
                        childId,
                        targetMonth.minusMonths(1).atDay(1)
                );

        if (previousTotalAssetAmount == null) {
            previousTotalAssetAmount = zeroIfNull(
                    assetReportMapper.findTotalAssetAmountAt(
                            childId,
                            startAt
                    )
            );
        }

        BigDecimal totalAssetChangeAmount =
                totalAssetAmount.subtract(
                        previousTotalAssetAmount
                );

        BigDecimal monthlySavedAmount = zeroIfNull(
                assetReportMapper.findMonthlySavedAmount(
                        childId,
                        startAt,
                        endExclusive
                )
        );

        BigDecimal previousMonthlySavedAmount =
                assetReportMapper.findPreviousMonthlySavedAmount(
                        childId,
                        targetMonth.minusMonths(1).atDay(1)
                );

        if (previousMonthlySavedAmount == null) {
            previousMonthlySavedAmount = zeroIfNull(
                    assetReportMapper.findMonthlySavedAmount(
                            childId,
                            toUtcStart(targetMonth.minusMonths(1)),
                            startAt
                    )
            );
        }

        BigDecimal monthlySavedChangeAmount =
                monthlySavedAmount.subtract(
                        previousMonthlySavedAmount
                );

        List<AssetReportGoalAccountRow> goalRows =
                assetReportMapper.findGoalAccountSnapshots(
                        childId,
                        startAt,
                        endExclusive
                );

        List<GoalSnapshot> goalSnapshots =
                createGoalSnapshots(goalRows);

        BigDecimal totalGoalTargetAmount = goalSnapshots.stream()
                .map(GoalSnapshot::getTargetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGoalSavedAmount = goalSnapshots.stream()
                .map(GoalSnapshot::getCurrentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal achievementRate =
                calculateRate(
                        totalGoalSavedAmount,
                        totalGoalTargetAmount
                );

        List<Map<String, Object>> insights =
                createInsights(
                        monthlySavedAmount,
                        monthlySavedChangeAmount,
                        achievementRate
                );

        AssetReportUpsertCommand command =
                AssetReportUpsertCommand.builder()
                        .childId(childId)
                        .reportMonth(reportMonth)
                        .totalAssetAmount(totalAssetAmount)
                        .totalAssetChangeAmount(
                                totalAssetChangeAmount
                        )
                        .monthlySavedAmount(
                                monthlySavedAmount
                        )
                        .totalGoalTargetAmount(
                                totalGoalTargetAmount
                        )
                        .totalGoalSavedAmount(
                                totalGoalSavedAmount
                        )
                        .goalAchievementRate(
                                achievementRate
                        )
                        .sixMonthFlowJson("[]")
                        .savingsGoalSummaryJson(
                                writeJson(goalSnapshots)
                        )
                        .insightItemsJson(
                                writeJson(insights)
                        )
                        .build();

        assetReportMapper.upsertAssetReport(command);
    }

    private LocalDateTime toUtcStart(YearMonth month) {
        return month.atDay(1)
                .atStartOfDay(SERVICE_ZONE)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
    }

    private List<GoalSnapshot> createGoalSnapshots(
            List<AssetReportGoalAccountRow> rows
    ) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        Map<Long, GoalSnapshot> snapshots =
                new LinkedHashMap<>();

        for (AssetReportGoalAccountRow row : rows) {
            GoalSnapshot snapshot = snapshots.computeIfAbsent(
                    row.getFinancialGoalId(),
                    ignored -> new GoalSnapshot(row)
            );

            snapshot.addAccount(
                    new AccountSnapshot(
                            row.getAccountId(),
                            row.getAccountName(),
                            row.getBankName(),
                            maskAccountNumber(
                                    row.getAccountNumberCiphertext()
                            ),
                            zeroIfNull(row.getBalance())
                    ),
                    zeroIfNull(row.getMonthlySavedAmount())
            );
        }

        return new ArrayList<>(snapshots.values());
    }

    private String maskAccountNumber(byte[] ciphertext) {
        String accountNumber =
                accountNumberProtector.decrypt(ciphertext);

        long digitCount = accountNumber.chars()
                .filter(Character::isDigit)
                .count();

        StringBuilder masked = new StringBuilder();
        int digitIndex = 0;

        for (char character : accountNumber.toCharArray()) {
            if (!Character.isDigit(character)) {
                masked.append(character);
                continue;
            }

            boolean visible = digitIndex < 3
                    || digitIndex >= digitCount - 2;

            masked.append(visible ? character : '*');
            digitIndex++;
        }

        return masked.toString();
    }

    private List<Map<String, Object>> createInsights(
            BigDecimal monthlySavedAmount,
            BigDecimal monthlySavedChangeAmount,
            BigDecimal achievementRate
    ) {
        List<Map<String, Object>> items =
                new ArrayList<>();

        if (monthlySavedChangeAmount.signum() > 0) {
            items.add(Map.of(
                    "type", "MONTHLY_SAVING_COMPARISON",
                    "title",
                    "지난달보다 "
                            + monthlySavedChangeAmount
                            .toPlainString()
                            + "원을 더 저축했어요.",
                    "description",
                    "꾸준한 저축 흐름이 아주 좋아요."
            ));
        } else if (monthlySavedChangeAmount.signum() < 0) {
            items.add(Map.of(
                    "type", "MONTHLY_SAVING_COMPARISON",
                    "title",
                    "지난달보다 "
                            + monthlySavedChangeAmount.abs()
                            .toPlainString()
                            + "원을 덜 저축했어요.",
                    "description",
                    "이번 달 저축 흐름을 함께 점검해보세요."
            ));
        } else {
            items.add(Map.of(
                    "type", "MONTHLY_SAVING_COMPARISON",
                    "title", "지난달과 같은 금액을 저축했어요.",
                    "description", "꾸준한 저축 흐름을 유지하고 있어요."
            ));
        }

        if (achievementRate.compareTo(
                new BigDecimal("50")
        ) >= 0) {
            items.add(Map.of(
                    "type", "GOAL_PROGRESS",
                    "title", "목표의 절반에 가까워졌어요.",
                    "description",
                    "현재 속도대로 저축을 이어가 보세요."
            ));
        }

        if (monthlySavedAmount.signum() > 0) {
            items.add(Map.of(
                    "type", "MONTHLY_SAVING",
                    "title",
                    "이번 달 "
                            + monthlySavedAmount
                            .toPlainString()
                            + "원을 저축했어요.",
                    "description",
                    "목표 계좌 저축액을 기준으로 계산했어요."
            ));
        }

        return items;
    }

    private BigDecimal calculateRate(
            BigDecimal currentAmount,
            BigDecimal targetAmount
    ) {
        if (targetAmount == null
                || targetAmount.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        return currentAmount
                .multiply(ONE_HUNDRED)
                .divide(
                        targetAmount,
                        4,
                        RoundingMode.HALF_UP
                )
                .min(ONE_HUNDRED);
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "자산 리포트 JSON 생성에 실패했습니다.",
                    exception
            );
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    private final class GoalSnapshot {

        private final Long financialGoalId;
        private final String title;
        private final BigDecimal targetAmount;
        private final BigDecimal monthlySavingTargetAmount;
        private final List<AccountSnapshot> linkedAccounts =
                new ArrayList<>();
        private BigDecimal currentAmount = BigDecimal.ZERO;
        private BigDecimal monthlySavedAmount = BigDecimal.ZERO;

        private GoalSnapshot(AssetReportGoalAccountRow row) {
            this.financialGoalId = row.getFinancialGoalId();
            this.title = row.getTitle();
            this.targetAmount = zeroIfNull(row.getTargetAmount());
            this.monthlySavingTargetAmount = zeroIfNull(
                    row.getMonthlySavingTargetAmount()
            );
        }

        private void addAccount(
                AccountSnapshot account,
                BigDecimal savedAmount
        ) {
            linkedAccounts.add(account);
            currentAmount = currentAmount.add(account.balance);
            monthlySavedAmount = monthlySavedAmount.add(savedAmount);
        }

        public Long getFinancialGoalId() {
            return financialGoalId;
        }

        public String getTitle() {
            return title;
        }

        public BigDecimal getCurrentAmount() {
            return currentAmount;
        }

        public BigDecimal getTargetAmount() {
            return targetAmount;
        }

        public BigDecimal getAchievementRate() {
            return calculateRate(currentAmount, targetAmount);
        }

        public BigDecimal getMonthlySavedAmount() {
            return monthlySavedAmount;
        }

        public BigDecimal getMonthlySavingTargetAmount() {
            return monthlySavingTargetAmount;
        }

        public List<AccountSnapshot> getLinkedAccounts() {
            return linkedAccounts;
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    private static final class AccountSnapshot {

        private final Long accountId;
        private final String accountName;
        private final String bankName;
        private final String accountNumberMasked;
        private final BigDecimal balance;

        private AccountSnapshot(
                Long accountId,
                String accountName,
                String bankName,
                String accountNumberMasked,
                BigDecimal balance
        ) {
            this.accountId = accountId;
            this.accountName = accountName;
            this.bankName = bankName;
            this.accountNumberMasked = accountNumberMasked;
            this.balance = balance;
        }

        public Long getAccountId() {
            return accountId;
        }

        public String getAccountName() {
            return accountName;
        }

        public String getBankName() {
            return bankName;
        }

        public String getAccountNumberMasked() {
            return accountNumberMasked;
        }

        public BigDecimal getBalance() {
            return balance;
        }
    }
}
