<script setup lang="ts">
import { onMounted } from 'vue'

import { formatReportWon, useChildcareReport } from '@/composables/useChildcareReport'

const { childcareReportSummary, load } = useChildcareReport()
onMounted(load)
</script>

<template>
  <section class="rounded-[20px] border border-[#e3e7ea] bg-white p-5" aria-labelledby="comparison-benchmark-title">
    <h2 id="comparison-benchmark-title" class="m-0 text-[16px] font-extrabold">양육비 비교 기준</h2>
    <p class="mt-2 mb-0 text-[12px] text-[var(--color-text-secondary)]">항목별 비교 통계는 제공하지 않으며, 월 총지출을 고정 기준값과 비교합니다.</p>
    <div class="mt-5 flex items-center justify-between gap-4 rounded-[14px] bg-[var(--color-surface-muted)] p-4">
      <span class="text-[12px] font-bold">{{ childcareReportSummary.comparisonLabel }}</span>
      <strong class="text-[18px]">{{ formatReportWon(childcareReportSummary.peerAverageAmount) }}</strong>
    </div>
    <p class="mt-4 mb-0 text-[11px] leading-5 text-[var(--color-text-secondary)]">{{ childcareReportSummary.benchmarkCalculationBasis || '공공 통계 기준' }}</p>
  </section>
</template>
