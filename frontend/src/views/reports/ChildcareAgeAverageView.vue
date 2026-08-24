<script setup lang="ts">
import { onMounted } from 'vue'
import { formatReportWon, useChildcareReport } from '@/composables/useChildcareReport'

const { childcareReportSummary, load } = useChildcareReport()
onMounted(load)
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-white px-[18px] py-6 text-[var(--color-text-primary)]"
  >
    <section
      class="overflow-hidden rounded-[22px] border border-[#e3e7ea] bg-white shadow-[0_6px_20px_rgba(71,77,82,0.04)]"
      aria-labelledby="age-average-title"
    >
      <div class="bg-[var(--color-accent-yellow-surface)] px-5 py-5">
        <span
          class="inline-flex rounded-full bg-white/75 px-2.5 py-1 text-[10px] font-bold text-[var(--color-accent-yellow-text)]"
          >공공 통계 기반</span
        >
        <h1 id="age-average-title" class="mt-2.5 mb-0 text-[18px] font-extrabold">
          {{ childcareReportSummary.comparisonLabel }} 정보
        </h1>
        <p class="mt-1.5 mb-0 text-[12px] font-semibold text-[var(--color-text-secondary)]">
          {{ childcareReportSummary.ageGroup }} 부모 가구 기준
        </p>
      </div>

      <div
        class="grid grid-cols-[minmax(0,1fr)_auto] bg-[#fafbfb] px-5 py-3 text-[11px] font-bold text-[var(--color-text-secondary)]"
      >
        <span>비교 기준</span>
        <span>월 평균 금액</span>
      </div>

      <dl class="m-0">
        <div class="grid min-h-14 grid-cols-[minmax(0,1fr)_auto] items-center gap-3 border-t border-[#edf0f2] px-5 py-3.5">
          <dt class="text-[13px] font-bold">{{ childcareReportSummary.comparisonLabel }}</dt>
          <dd class="m-0 text-right text-[13px] font-semibold text-[var(--color-text-secondary)]">{{ formatReportWon(childcareReportSummary.peerAverageAmount) }}</dd>
        </div>
      </dl>

      <div
        class="grid grid-cols-[minmax(0,1fr)_auto] items-center gap-3 border-t border-[#e2e7ea] bg-[var(--color-accent-yellow-surface)] px-5 py-5"
      >
        <strong class="text-[14px]">월평균 양육비</strong>
        <strong class="text-[20px] tracking-[-0.02em]">
          {{ formatReportWon(childcareReportSummary.peerAverageAmount) }}
        </strong>
      </div>
    </section>

    <section class="mt-4 rounded-[18px] bg-[var(--color-surface-muted)] p-4 text-[11px] leading-5 text-[var(--color-text-secondary)]">
      <p class="m-0"><strong class="text-[var(--color-text-primary)]">산정 방식</strong><br>{{ childcareReportSummary.benchmarkCalculationBasis || '공공 통계 기준' }}</p>
      <p class="mt-3 mb-0"><strong class="text-[var(--color-text-primary)]">출처</strong><br>{{ childcareReportSummary.benchmarkSourceName }} {{ childcareReportSummary.benchmarkSourceYear ? `(${childcareReportSummary.benchmarkSourceYear})` : '' }}</p>
      <a v-if="childcareReportSummary.benchmarkSourceUrl" :href="childcareReportSummary.benchmarkSourceUrl" target="_blank" rel="noopener noreferrer" class="mt-2 inline-block font-bold text-[var(--color-accent-yellow-text)] underline">출처 보기</a>
    </section>

    <p class="mt-4 mb-0 px-1 text-[10px] leading-4 text-[var(--color-text-secondary)]">
      연령별 평균 금액은 가정 상황과 조사 시점에 따라 달라질 수 있으며, 양육 계획을 위한 참고 자료로
      활용해주세요.
    </p>
  </main>
</template>
