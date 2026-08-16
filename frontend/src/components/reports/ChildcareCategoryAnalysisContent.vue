<script setup lang="ts">
import { computed } from 'vue'

import {
  childcareCategories,
  childcareReportSummary,
  formatReportWon,
} from '@/data/childcareReportData'

const total = childcareReportSummary.currentMonthAmount
const percentage = (amount: number) => Math.round((amount / total) * 100)
const donutSegments = computed(() => {
  let offset = 0
  return childcareCategories.map((category) => {
    const length = (category.amount / total) * 100
    const segment = {
      ...category,
      dashArray: `${length} ${100 - length}`,
      dashOffset: -offset,
    }
    offset += length
    return segment
  })
})
</script>

<template>
  <div>
    <section aria-label="항목별 소비 구성">
      <div
        class="relative mx-auto size-[180px] rounded-full shadow-[0_12px_28px_rgba(98,78,15,0.1)]"
        role="img"
        aria-label="항목별 소비 비율 도넛 그래프"
      >
        <svg
          class="absolute inset-0 size-full overflow-visible"
          viewBox="0 0 180 180"
          aria-hidden="true"
        >
          <defs>
            <mask
              id="childcare-donut-reveal-mask"
              x="0"
              y="0"
              width="180"
              height="180"
              maskUnits="userSpaceOnUse"
            >
              <circle
                class="donut-reveal-path"
                cx="90"
                cy="90"
                r="75.5"
                pathLength="1"
                fill="none"
                stroke="white"
                stroke-width="30"
                transform="rotate(-90 90 90)"
              />
            </mask>
          </defs>
          <g mask="url(#childcare-donut-reveal-mask)">
            <circle
              v-for="segment in donutSegments"
              :key="segment.id"
              cx="90"
              cy="90"
              r="75.5"
              pathLength="100"
              fill="none"
              :stroke="segment.color"
              stroke-width="29"
              stroke-linecap="butt"
              :stroke-dasharray="segment.dashArray"
              :stroke-dashoffset="segment.dashOffset"
              transform="rotate(-90 90 90)"
            />
          </g>
        </svg>
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
          v-for="category in donutSegments"
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
      class="mt-8 rounded-[20px] border border-[#e3e7ea] bg-[var(--color-surface-muted)] p-5"
      aria-labelledby="expense-summary-title"
    >
      <h3 id="expense-summary-title" class="m-0 text-[16px] font-extrabold">소비 요약</h3>
      <dl class="mt-4 mb-0 grid gap-3.5 text-[12px]">
        <div class="flex items-center justify-between gap-4 border-b border-[#e3e7ea] pb-3.5">
          <dt class="text-[var(--color-text-secondary)]">가장 큰 지출</dt>
          <dd class="m-0 font-bold">교육/학습</dd>
        </div>
        <div class="flex items-center justify-between gap-4 border-b border-[#e3e7ea] pb-3.5">
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

<style scoped>
.donut-reveal-path {
  stroke-dasharray: 1;
  stroke-dashoffset: 1;
  animation: reveal-donut 1100ms cubic-bezier(0.22, 1, 0.36, 1) 120ms forwards;
}

@keyframes reveal-donut {
  to {
    stroke-dashoffset: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .donut-reveal-path {
    animation: none;
    stroke-dashoffset: 0;
  }
}
</style>
