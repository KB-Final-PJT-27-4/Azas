<script setup lang="ts">
import {
  ArrowUpRight,
  BarChart3,
  BookOpenCheck,
  ChevronRight,
  LineChart,
  ReceiptText,
  Sparkles,
  TrendingUp,
} from 'lucide-vue-next'

import {
  childcareCategories,
  childcareReportSummary,
  formatReportWon,
} from '@/data/childcareReportData'

const highestCategory = childcareCategories[0]!
const comparisonRate = Math.round(
  (childcareReportSummary.currentMonthAmount / childcareReportSummary.peerAverageAmount) * 100,
)
</script>

<template>
  <div class="px-[18px] py-5 text-[var(--color-text-primary)]">
    <RouterLink
      class="block rounded-[22px] border border-[var(--color-accent-yellow-border)] bg-[var(--color-brand-secondary)] p-5 !text-[var(--color-text-primary)] shadow-[0_8px_22px_rgba(176,142,43,0.06)] active:scale-[0.995]"
      :to="{ name: 'ChildcareMonthlyTrend' }"
    >
      <div class="flex items-start justify-between gap-4">
        <div>
          <span
            class="inline-flex items-center gap-1.5 text-[12px] font-bold text-[var(--color-accent-yellow-text)]"
          >
            <ReceiptText :size="15" aria-hidden="true" /> 이번 달 아이 관련 지출
          </span>
          <strong class="mt-3 block text-[28px] leading-none tracking-[-0.04em]">
            {{ formatReportWon(childcareReportSummary.currentMonthAmount) }}
          </strong>
          <p class="mt-3 mb-0 text-[12px] text-[var(--color-text-secondary)]">
            지난달 대비
            <strong class="text-[var(--color-accent-yellow-text)]">
              +{{ formatReportWon(childcareReportSummary.previousMonthDifference) }}
            </strong>
            ({{ childcareReportSummary.previousMonthRate }}%)
          </p>
        </div>
        <span
          class="grid size-10 shrink-0 place-items-center rounded-2xl bg-white/80 text-[var(--color-accent-yellow-text)]"
        >
          <TrendingUp :size="21" :stroke-width="2.3" aria-hidden="true" />
        </span>
      </div>
      <span
        class="mt-4 flex items-center justify-end gap-1 text-[11px] font-bold text-[var(--color-accent-yellow-text)]"
      >
        월별 추이 보기 <ChevronRight :size="15" aria-hidden="true" />
      </span>
    </RouterLink>

    <section
      class="mt-4 rounded-[22px] border border-[#e5e8eb] bg-white p-5 shadow-[0_5px_18px_rgba(64,78,86,0.04)]"
    >
      <div class="flex items-start justify-between gap-3">
        <div>
          <h2 class="m-0 text-[16px] font-extrabold">동일 연령 평균과 비교</h2>
          <p class="mt-1.5 mb-0 text-[12px] leading-5 text-[var(--color-text-secondary)]">
            같은 연령대 가정의 월평균 양육비와 비교했어요.
          </p>
        </div>
        <RouterLink
          class="grid size-9 shrink-0 place-items-center rounded-full bg-[var(--color-brand-secondary)] !text-[var(--color-accent-yellow-text)]"
          :to="{ name: 'ChildcareAgeComparison' }"
          aria-label="연령별 평균 비교 자세히 보기"
        >
          <ArrowUpRight :size="17" aria-hidden="true" />
        </RouterLink>
      </div>

      <div class="mt-5 grid gap-4">
        <div>
          <div class="flex items-end justify-between gap-3 text-[13px]">
            <strong>우리 집</strong>
            <strong class="text-[var(--color-accent-yellow-text)]">
              {{ formatReportWon(childcareReportSummary.currentMonthAmount) }}
            </strong>
          </div>
          <div class="mt-2 h-2.5 overflow-hidden rounded-full bg-[#f1f2f3]">
            <div
              class="h-full rounded-full bg-[var(--color-accent-yellow)]"
              :style="{ width: `${Math.min(comparisonRate, 100)}%` }"
            ></div>
          </div>
        </div>
        <div>
          <div class="flex items-end justify-between gap-3 text-[13px]">
            <strong>동일 연령 평균</strong>
            <strong class="text-[var(--color-text-secondary)]">
              {{ formatReportWon(childcareReportSummary.peerAverageAmount) }}
            </strong>
          </div>
          <div class="mt-2 h-2.5 overflow-hidden rounded-full bg-[#f1f2f3]">
            <div class="h-full w-[82%] rounded-full bg-[#cfd4d8]"></div>
          </div>
        </div>
      </div>

      <div
        class="mt-4 rounded-[16px] bg-[var(--color-accent-yellow-surface)] px-4 py-3.5 text-[12px] leading-5 text-[var(--color-text-secondary)]"
      >
        이번 달은 동일 연령 평균보다
        <strong class="text-[var(--color-accent-yellow-text)]">약 34만원 높아요.</strong>
        항목별 차이를 확인해 다음 달 계획에 참고해보세요.
      </div>

      <div class="mt-4 grid grid-cols-2 gap-2.5">
        <RouterLink
          class="flex h-11 items-center justify-center gap-1 rounded-[13px] bg-[var(--color-brand-secondary)] text-[12px] font-bold !text-[var(--color-accent-yellow-text)]"
          :to="{ name: 'ChildcareAgeAverage' }"
        >
          평균 정보 <BookOpenCheck :size="15" aria-hidden="true" />
        </RouterLink>
        <RouterLink
          class="flex h-11 items-center justify-center gap-1 rounded-[13px] bg-[var(--color-accent-yellow)] text-[12px] font-bold !text-[var(--color-text-primary)] active:bg-[var(--color-accent-yellow-pressed)]"
          :to="{ name: 'ChildcareAgeComparison' }"
        >
          비교 분석 <BarChart3 :size="15" aria-hidden="true" />
        </RouterLink>
      </div>
    </section>

    <RouterLink
      class="mt-4 block rounded-[22px] border border-[#e5e8eb] bg-white p-5 !text-[var(--color-text-primary)] shadow-[0_5px_18px_rgba(64,78,86,0.04)]"
      :to="{ name: 'ChildcareCategoryAnalysis' }"
    >
      <div class="flex items-center justify-between gap-3">
        <h2 class="m-0 text-[16px] font-extrabold">이번 달 양육비 인사이트</h2>
        <ChevronRight
          :size="19"
          class="text-[var(--color-accent-yellow-text)]"
          aria-hidden="true"
        />
      </div>
      <div class="mt-4 grid gap-3">
        <div class="flex gap-3 border-b border-[#edf0f2] pb-3">
          <span
            class="grid size-9 shrink-0 place-items-center rounded-xl bg-[var(--color-brand-secondary)] text-[var(--color-accent-yellow-text)]"
          >
            <Sparkles :size="18" aria-hidden="true" />
          </span>
          <div class="min-w-0">
            <strong class="block text-[13px]"
              >{{ highestCategory.label }} 비중이 가장 높아요.</strong
            >
            <span class="mt-1 block text-[11px] text-[var(--color-text-secondary)]">
              전체 양육비의 약 40%를 차지하고 있어요.
            </span>
          </div>
        </div>
        <div class="flex gap-3">
          <span
            class="grid size-9 shrink-0 place-items-center rounded-xl bg-[var(--color-accent-yellow-surface)] text-[var(--color-accent-yellow-text)]"
          >
            <LineChart :size="18" aria-hidden="true" />
          </span>
          <div class="min-w-0">
            <strong class="block text-[13px]">지난달보다 지출이 6.9% 늘었어요.</strong>
            <span class="mt-1 block text-[11px] text-[var(--color-text-secondary)]">
              교육·의류 항목에서 증가 폭이 컸어요.
            </span>
          </div>
        </div>
      </div>
    </RouterLink>
  </div>
</template>
