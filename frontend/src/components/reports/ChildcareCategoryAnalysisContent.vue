<script setup lang="ts">
import { onMounted } from 'vue'

import { formatReportWon, useChildcareReport } from '@/composables/useChildcareReport'

const { childcareCategories, childcareReportSummary, isUsingDemoData, load } = useChildcareReport()
onMounted(load)
</script>

<template>
  <section
    class="rounded-[20px] border border-[#e3e7ea] bg-white p-5"
    aria-labelledby="comparison-benchmark-title"
  >
    <h2 id="comparison-benchmark-title" class="m-0 text-[16px] font-extrabold">양육비 비교 기준</h2>
    <p class="mt-2 mb-0 text-[12px] text-[var(--color-text-secondary)]">
      월 총지출을 비교 기준과 비교합니다.
    </p>
    <div
      class="mt-5 flex items-center justify-between gap-4 rounded-[14px] bg-[var(--color-surface-muted)] p-4"
    >
      <span class="text-[12px] font-bold">{{ childcareReportSummary.comparisonLabel }}</span>
      <strong class="text-[18px]">{{
        formatReportWon(childcareReportSummary.peerAverageAmount)
      }}</strong>
    </div>
    <ul v-if="isUsingDemoData && childcareCategories.length > 0" class="mt-4 grid list-none gap-2 p-0 m-0">
      <li
        v-for="category in childcareCategories"
        :key="category.id"
        class="flex items-center justify-between gap-3 rounded-[12px] bg-[var(--color-surface-muted)] px-3 py-2.5 text-[12px]"
      >
        <span class="font-bold">{{ category.label }}</span>
        <strong>{{ formatReportWon(category.amount) }}</strong>
      </li>
    </ul>
    <p class="mt-4 mb-0 text-[11px] leading-5 text-[var(--color-text-secondary)]">
      {{ childcareReportSummary.benchmarkCalculationBasis || '공공 통계 기준' }}
    </p>
  </section>
</template>
