<script setup lang="ts">
import { computed } from 'vue'

import {
  childcareCategories,
  childcareReportSummary,
  formatReportWon,
} from '@/data/childcareReportData'

const total = childcareReportSummary.currentMonthAmount
const percentage = (amount: number) => Math.round((amount / total) * 100)
const donutBackground = computed(() => {
  let start = 0
  const stops = childcareCategories.map((category) => {
    const end = start + (category.amount / total) * 100
    const stop = `${category.color} ${start}% ${end}%`
    start = end
    return stop
  })
  return `conic-gradient(${stops.join(', ')})`
})
</script>

<template>
  <div>
    <section aria-label="항목별 소비 구성">
      <div
        class="relative mx-auto size-[180px] rounded-full shadow-[0_12px_28px_rgba(98,78,15,0.1)]"
        :style="{ background: donutBackground }"
        role="img"
        aria-label="항목별 소비 비율 도넛 그래프"
      >
        <div
          class="absolute inset-[29px] grid place-items-center rounded-full bg-white text-center"
        >
          <div>
            <strong class="block text-[20px] tracking-[-0.03em]">{{
              formatReportWon(total)
            }}</strong>
            <span class="mt-1.5 block text-[11px] text-[var(--color-text-secondary)]">
              이번 달 총 지출
            </span>
          </div>
        </div>
      </div>

      <ul
        class="mt-7 mb-0 grid list-none divide-y divide-[#edf0f2] overflow-hidden rounded-[18px] border border-[#e3e7ea] bg-white px-4 py-1"
      >
        <li
          v-for="category in childcareCategories"
          :key="category.id"
          class="flex min-h-13 min-w-0 items-center gap-3 py-3"
        >
          <span
            class="size-3 shrink-0 rounded-full"
            :style="{ backgroundColor: category.color }"
          ></span>
          <strong class="min-w-0 flex-1 truncate text-[13px]">{{ category.label }}</strong>
          <span class="shrink-0 text-[12px] font-medium text-[var(--color-text-secondary)]">
            {{ formatReportWon(category.amount) }} ({{ percentage(category.amount) }}%)
          </span>
        </li>
      </ul>
    </section>

    <section
      class="mt-8 rounded-[20px] border border-[#e3e7ea] bg-[var(--color-accent-yellow-surface)] p-5"
      aria-labelledby="expense-summary-title"
    >
      <h3 id="expense-summary-title" class="m-0 text-[16px] font-extrabold">소비 요약</h3>
      <dl class="mt-4 mb-0 grid gap-3.5 text-[12px]">
        <div class="flex items-center justify-between gap-4 border-b border-[#eee9d9] pb-3.5">
          <dt class="text-[var(--color-text-secondary)]">가장 큰 지출</dt>
          <dd class="m-0 font-bold">교육/학습</dd>
        </div>
        <div class="flex items-center justify-between gap-4 border-b border-[#eee9d9] pb-3.5">
          <dt class="text-[var(--color-text-secondary)]">가장 적은 지출</dt>
          <dd class="m-0 font-bold">교통</dd>
        </div>
        <div class="flex items-center justify-between gap-4">
          <dt class="text-[var(--color-text-secondary)]">총 지출</dt>
          <dd class="m-0 font-extrabold">{{ formatReportWon(total) }}</dd>
        </div>
      </dl>
    </section>
  </div>
</template>
