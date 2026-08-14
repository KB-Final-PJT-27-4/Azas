<script setup lang="ts">
import { computed, ref } from 'vue'

import {
  childcareCategories,
  childcareReportSummary,
  formatReportWon,
} from '@/data/childcareReportData'

type AnalysisMode = 'amount' | 'ratio'

const analysisMode = ref<AnalysisMode>('amount')
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
  <main
    class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-white px-[18px] py-6 text-[var(--color-text-primary)]"
  >
    <div
      class="mx-auto grid h-11 max-w-[230px] grid-cols-2 rounded-full bg-[#f2f3f4] p-1"
      role="tablist"
      aria-label="분석 표시 방식"
    >
      <button
        class="rounded-full text-[12px] font-bold transition-colors"
        :class="
          analysisMode === 'amount'
            ? 'bg-[var(--color-brand-secondary)] text-[var(--color-accent-yellow-text)] shadow-[0_4px_10px_rgba(176,142,43,0.12)] ring-1 ring-[var(--color-accent-yellow-border)]'
            : 'text-[var(--color-text-secondary)]'
        "
        type="button"
        role="tab"
        :aria-selected="analysisMode === 'amount'"
        @click="analysisMode = 'amount'"
      >
        금액
      </button>
      <button
        class="rounded-full text-[12px] font-bold transition-colors"
        :class="
          analysisMode === 'ratio'
            ? 'bg-[var(--color-brand-secondary)] text-[var(--color-accent-yellow-text)] shadow-[0_4px_10px_rgba(176,142,43,0.12)] ring-1 ring-[var(--color-accent-yellow-border)]'
            : 'text-[var(--color-text-secondary)]'
        "
        type="button"
        role="tab"
        :aria-selected="analysisMode === 'ratio'"
        @click="analysisMode = 'ratio'"
      >
        비율
      </button>
    </div>

    <section
      class="mt-7 grid grid-cols-[minmax(130px,0.9fr)_minmax(145px,1.1fr)] items-center gap-5"
      aria-label="항목별 소비 구성"
    >
      <div
        class="relative mx-auto size-[150px] rounded-full shadow-[0_10px_25px_rgba(98,78,15,0.08)]"
        :style="{ background: donutBackground }"
        role="img"
        aria-label="항목별 소비 비율 도넛 그래프"
      >
        <div
          class="absolute inset-[24px] grid place-items-center rounded-full bg-white text-center"
        >
          <div>
            <strong class="block text-[17px] tracking-[-0.03em]">{{
              formatReportWon(total)
            }}</strong>
            <span class="mt-1 block text-[10px] text-[var(--color-text-secondary)]"
              >이번 달 총 지출</span
            >
          </div>
        </div>
      </div>

      <ul class="m-0 grid list-none gap-3 p-0">
        <li
          v-for="category in childcareCategories"
          :key="category.id"
          class="flex min-w-0 items-start gap-2.5"
        >
          <span
            class="mt-1 size-2.5 shrink-0 rounded-full"
            :style="{ backgroundColor: category.color }"
          ></span>
          <span class="min-w-0">
            <strong class="block truncate text-[12px]">{{ category.label }}</strong>
            <span class="mt-0.5 block text-[10px] text-[var(--color-text-secondary)]">
              {{
                analysisMode === 'amount'
                  ? formatReportWon(category.amount)
                  : `${percentage(category.amount)}%`
              }}
              <template v-if="analysisMode === 'amount'">
                ({{ percentage(category.amount) }}%)</template
              >
            </span>
          </span>
        </li>
      </ul>
    </section>

    <section
      class="mt-8 rounded-[20px] border border-[#e3e7ea] bg-[var(--color-accent-yellow-surface)] p-5"
      aria-labelledby="expense-summary-title"
    >
      <h1 id="expense-summary-title" class="m-0 text-[16px] font-extrabold">소비 요약</h1>
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
  </main>
</template>
