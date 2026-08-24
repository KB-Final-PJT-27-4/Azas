<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { formatReportWon, useChildcareReport } from '@/composables/useChildcareReport'

const { childcareReportSummary, load } = useChildcareReport()
const comparisonText = computed(() => {
  const amount = childcareReportSummary.comparisonDifferenceAmount
  if (amount > 0) return `${formatReportWon(amount)} 높아요`
  if (amount < 0) return `${formatReportWon(Math.abs(amount))} 낮아요`
  return '같아요'
})
onMounted(load)
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-white px-[18px] py-6 text-[var(--color-text-primary)]"
  >
    <section>
      <span
        class="inline-flex rounded-full bg-[var(--color-accent-yellow-surface)] px-3 py-1.5 text-[10px] font-bold text-[var(--color-accent-yellow-text)]"
        >이번 달 비교</span
      >
      <h1 class="mt-3 mb-0 text-[19px] font-extrabold">{{ childcareReportSummary.comparisonLabel }}와 함께 살펴보세요</h1>
      <p class="mt-2 mb-0 text-[12px] leading-5 text-[var(--color-text-secondary)]">
        평균은 참고용 정보예요. 우리 집의 소비 흐름을 이해하는 데 활용해보세요.
      </p>
    </section>

    <section class="mt-5 grid grid-cols-2 gap-3" aria-label="총 지출 비교">
      <div
        class="rounded-[18px] border border-[var(--color-accent-yellow-border)] bg-[var(--color-accent-yellow-surface)] p-4"
      >
        <span class="text-[11px] font-bold text-[var(--color-text-secondary)]"
          >우리 집 이번 달</span
        >
        <strong class="mt-2 block text-[19px] text-[var(--color-accent-yellow-text)]">{{
          formatReportWon(childcareReportSummary.currentMonthAmount)
        }}</strong>
      </div>
      <div class="rounded-[18px] border border-[#e4e8eb] bg-[#fafbfb] p-4">
        <span class="text-[11px] font-bold text-[var(--color-text-secondary)]"
          >{{ childcareReportSummary.comparisonLabel }}</span
        >
        <strong class="mt-2 block text-[19px]">{{
          formatReportWon(childcareReportSummary.peerAverageAmount)
        }}</strong>
      </div>
    </section>

    <div
      class="mt-4 rounded-[16px] bg-[var(--color-accent-yellow-surface)] px-4 py-3.5 text-[12px] leading-5 text-[var(--color-text-secondary)]"
    >
      이번 달 우리 집 지출은 비교 기준보다
      <strong class="text-[var(--color-accent-yellow-text)]"
        >{{ comparisonText }}</strong
      >이에요.
    </div>

    <section
      class="mt-5 rounded-[22px] border border-[#e3e7ea] bg-white p-5 shadow-[0_5px_18px_rgba(64,78,86,0.04)]"
      aria-labelledby="benchmark-title"
    >
      <h2 id="benchmark-title" class="m-0 text-[16px] font-extrabold">비교 기준</h2>
      <dl class="mt-4 grid gap-3 text-[12px]">
        <div class="flex justify-between gap-3"><dt class="text-[var(--color-text-secondary)]">기준 금액</dt><dd class="m-0 font-bold">{{ formatReportWon(childcareReportSummary.peerAverageAmount) }}</dd></div>
        <div><dt class="text-[var(--color-text-secondary)]">산정 방식</dt><dd class="mt-1 m-0 leading-5">{{ childcareReportSummary.benchmarkCalculationBasis || '공공 통계 기준' }}</dd></div>
      </dl>
    </section>
  </main>
</template>
