<script setup lang="ts">
import { computed, ref } from 'vue'

import {
  childcareReportSummary,
  formatReportWon,
  monthlyChildcareExpenses,
} from '@/data/childcareReportData'

type Period = '6months' | 'year' | 'all'
type ChartPoint = { x: number; y: number }

const period = ref<Period>('year')
const visibleExpenses = computed(() => {
  if (period.value === '6months') return monthlyChildcareExpenses.slice(-6)
  if (period.value === 'year') return monthlyChildcareExpenses.slice(-12)
  return monthlyChildcareExpenses
})
const chartMax = computed(
  () =>
    Math.ceil(Math.max(...visibleExpenses.value.map(({ amount }) => amount), 2_000_000) / 500_000) *
    500_000,
)
const pointsFor = (key: 'amount' | 'averageAmount'): ChartPoint[] =>
  visibleExpenses.value.map((item, index, items) => ({
    x: items.length === 1 ? 160 : 12 + (index / (items.length - 1)) * 296,
    y: 188 - (item[key] / chartMax.value) * 168,
  }))
const expensePoints = computed(() => pointsFor('amount'))
const averagePoints = computed(() => pointsFor('averageAmount'))
const linePoints = (points: ChartPoint[]) => points.map(({ x, y }) => `${x},${y}`).join(' ')
const areaPoints = computed(() => `12,188 ${linePoints(expensePoints.value)} 308,188`)
const displayedAnnualAmount = computed(() =>
  visibleExpenses.value.reduce((sum, { amount }) => sum + amount, 0),
)
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-white px-[18px] py-6 text-[var(--color-text-primary)]"
  >
    <div
      class="grid h-11 grid-cols-3 gap-2 rounded-full bg-[#f3f4f5] p-1"
      role="tablist"
      aria-label="조회 기간"
    >
      <button
        v-for="item in [
          { id: '6months', label: '6개월' },
          { id: 'year', label: '1년' },
          { id: 'all', label: '전체' },
        ]"
        :key="item.id"
        class="rounded-full text-[12px] font-bold transition-colors"
        :class="
          period === item.id
            ? 'bg-[var(--color-brand-secondary)] text-[var(--color-accent-yellow-text)] shadow-[0_4px_10px_rgba(176,142,43,0.12)] ring-1 ring-[var(--color-accent-yellow-border)]'
            : 'text-[var(--color-text-secondary)]'
        "
        type="button"
        role="tab"
        :aria-selected="period === item.id"
        @click="period = item.id as Period"
      >
        {{ item.label }}
      </button>
    </div>

    <section class="mt-7">
      <span class="text-[12px] font-bold">이번 달 지출</span>
      <strong class="mt-2 block text-[27px] tracking-[-0.035em]">{{
        formatReportWon(childcareReportSummary.currentMonthAmount)
      }}</strong>
      <div
        class="mt-4 flex items-center gap-5 text-[11px] font-bold text-[var(--color-text-secondary)]"
      >
        <span class="inline-flex items-center gap-1.5"
          ><i class="h-2.5 w-4 rounded-full bg-[var(--color-accent-yellow)]"></i>지출 금액</span
        >
        <span class="inline-flex items-center gap-1.5"
          ><i class="h-2.5 w-4 rounded-full bg-[#c9cfd4]"></i>평균 금액</span
        >
      </div>
    </section>

    <section class="mt-5" aria-label="월별 지출 추이 그래프">
      <div class="relative h-[248px] pl-7">
        <div
          class="absolute inset-y-5 left-0 flex flex-col justify-between text-[9px] text-[var(--color-text-secondary)]"
        >
          <span>{{ Math.round(chartMax / 10_000) }}</span
          ><span>{{ Math.round(chartMax / 20_000) }}</span
          ><span>0</span>
        </div>
        <svg
          class="h-[210px] w-full overflow-visible"
          viewBox="0 0 320 200"
          role="img"
          aria-label="월별 지출과 평균 금액 선 그래프"
        >
          <defs>
            <linearGradient id="childcare-area" x1="0" x2="0" y1="0" y2="1">
              <stop offset="0%" stop-color="var(--color-accent-yellow)" stop-opacity="0.2" />
              <stop offset="100%" stop-color="var(--color-accent-yellow)" stop-opacity="0" />
            </linearGradient>
          </defs>
          <g stroke="#edf0f2" stroke-width="1">
            <line x1="12" y1="20" x2="308" y2="20" />
            <line x1="12" y1="104" x2="308" y2="104" />
            <line x1="12" y1="188" x2="308" y2="188" />
          </g>
          <polygon :points="areaPoints" fill="url(#childcare-area)" />
          <polyline
            :points="linePoints(averagePoints)"
            fill="none"
            stroke="#c9cfd4"
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="3"
          />
          <polyline
            :points="linePoints(expensePoints)"
            fill="none"
            stroke="var(--color-accent-yellow)"
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="3"
          />
          <circle
            v-for="(point, index) in averagePoints"
            :key="`average-${index}`"
            :cx="point.x"
            :cy="point.y"
            r="3.5"
            fill="#c9cfd4"
          />
          <circle
            v-for="(point, index) in expensePoints"
            :key="`expense-${index}`"
            :cx="point.x"
            :cy="point.y"
            r="4"
            fill="var(--color-accent-yellow)"
          />
        </svg>
        <div
          class="ml-3 grid text-[9px] font-semibold text-[var(--color-text-secondary)]"
          :style="{ gridTemplateColumns: `repeat(${visibleExpenses.length}, minmax(0, 1fr))` }"
        >
          <span v-for="item in visibleExpenses" :key="item.month" class="text-center">{{
            item.month
          }}</span>
        </div>
      </div>
    </section>

    <section
      class="mt-6 flex items-center justify-between gap-4 rounded-[18px] border border-[var(--color-accent-yellow-border)] bg-[var(--color-accent-yellow-surface)] px-5 py-5"
    >
      <span class="text-[13px] font-bold"
        >{{ period === '6months' ? '6개월' : period === 'year' ? '연간' : '전체' }} 지출 합계</span
      >
      <strong class="text-[19px]">{{
        formatReportWon(
          period === 'year' ? childcareReportSummary.annualAmount : displayedAnnualAmount,
        )
      }}</strong>
    </section>
  </main>
</template>
