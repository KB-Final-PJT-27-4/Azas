<script setup lang="ts">
import { CalendarDays, ChevronRight, Sparkles, TrendingUp } from 'lucide-vue-next'

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
      class="block rounded-[22px] border border-[var(--color-accent-yellow-border)] bg-[var(--color-accent-yellow-surface)] p-5 !text-[var(--color-text-primary)] shadow-[0_8px_22px_rgba(176,142,43,0.04)] active:scale-[0.995]"
      :to="{ name: 'ChildcareMonthlyTrend' }"
    >
      <div class="flex items-start justify-between gap-4">
        <div>
          <p class="m-0 text-sm font-semibold text-[var(--color-text-secondary)]">
            이번 달 아이 관련 지출
          </p>
          <strong class="mt-3 block text-[28px] leading-none tracking-[-0.04em]">
            {{ formatReportWon(childcareReportSummary.currentMonthAmount) }}
          </strong>
          <p class="mt-3 mb-0 text-[12px] text-[var(--color-text-secondary)]">
            지난달 대비
            <strong class="text-[var(--color-accent-yellow-text)]">
              {{ formatReportWon(childcareReportSummary.previousMonthDifference) }}
            </strong>
            늘었어요
          </p>
        </div>
        <span
          class="grid size-11 shrink-0 place-items-center rounded-2xl bg-white/80 text-[var(--color-accent-yellow-text)]"
        >
          <ChevronRight :size="23" :stroke-width="2.4" aria-hidden="true" />
        </span>
      </div>
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
      </div>

      <div class="mt-5 grid gap-4">
        <div>
          <div class="flex items-end justify-between gap-3 text-[13px]">
            <strong>우리 집</strong>
            <strong>
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
        class="mt-4 rounded-[16px] bg-[#f6f8fa] px-4 py-3.5 text-[12px] leading-5 text-[var(--color-text-secondary)]"
      >
        이번 달은 동일 연령 평균보다
        <strong class="text-[var(--color-text-primary)]">약 34만원 높아요.</strong>
        항목별 차이를 확인해 다음 달 계획에 참고해보세요.
      </div>

      <div class="mt-4 grid grid-cols-2 gap-2.5">
        <RouterLink
          class="flex h-11 items-center justify-center gap-1 rounded-[13px] border border-[#e1e6e9] bg-white text-[12px] font-bold !text-[var(--color-text-secondary)] active:bg-[#f7f9fa]"
          :to="{ name: 'ChildcareAgeAverage' }"
        >
          평균 정보
        </RouterLink>
        <RouterLink
          class="flex h-11 items-center justify-center gap-1 rounded-[13px] bg-[#eef2f4] text-[12px] font-bold !text-[var(--color-text-primary)] active:bg-[#e3e9ec]"
          :to="{ name: 'ChildcareAgeComparison' }"
        >
          비교 분석
        </RouterLink>
      </div>
    </section>

    <section class="mt-7 pb-2" aria-labelledby="childcare-insight-title">
      <h2 id="childcare-insight-title" class="m-0 text-[21px] font-extrabold tracking-[-0.03em]">
        이번 달 인사이트
      </h2>
      <div class="mt-4 grid gap-3">
        <article
          class="flex items-center gap-4 rounded-[18px] bg-[var(--color-accent-yellow-surface)] p-4"
        >
          <Sparkles
            class="shrink-0 text-[var(--color-accent-yellow-text)]"
            :size="27"
            :stroke-width="2.2"
          />
          <div>
            <strong class="text-sm">{{ highestCategory.label }} 비중이 가장 높아요.</strong>
            <p class="mt-1 mb-0 text-xs text-[var(--color-text-secondary)]">
              전체 양육비의 약 40%를 차지하고 있어요.
            </p>
          </div>
        </article>
        <article
          class="flex items-center gap-4 rounded-[18px] bg-[var(--color-accent-yellow-surface)] p-4"
        >
          <TrendingUp class="shrink-0 text-[#ef6c8f]" :size="27" :stroke-width="2.2" />
          <div>
            <strong class="text-sm">지난달보다 지출이 6.9% 늘었어요.</strong>
            <p class="mt-1 mb-0 text-xs text-[var(--color-text-secondary)]">
              교육·의류 항목에서 증가 폭이 컸어요.
            </p>
          </div>
        </article>
        <article
          class="flex items-center gap-4 rounded-[18px] bg-[var(--color-accent-yellow-surface)] p-4"
        >
          <CalendarDays class="shrink-0 text-[#65bd73]" :size="27" :stroke-width="2.2" />
          <div>
            <strong class="text-sm">다음 달 교육비를 미리 계획해보세요.</strong>
            <p class="mt-1 mb-0 text-xs text-[var(--color-text-secondary)]">
              정기 지출을 먼저 나누면 예산 관리가 쉬워져요.
            </p>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>
