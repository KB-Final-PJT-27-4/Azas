<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import ChildcareCategoryAnalysisContent from '@/components/reports/ChildcareCategoryAnalysisContent.vue'
import { formatReportWon, useChildcareReport } from '@/composables/useChildcareReport'

const { childcareReportSummary, monthlyChildcareExpenses, load } = useChildcareReport()

type Period = '6months' | 'year'
type ChartPoint = { x: number; y: number }

const period = ref<Period>('year')
const activePointIndex = ref<number | null>(null)
const visibleExpenses = computed(() => {
  if (period.value === '6months') return monthlyChildcareExpenses.slice(-6)
  return monthlyChildcareExpenses.slice(-12)
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
const activeExpense = computed(() =>
  activePointIndex.value === null ? null : (visibleExpenses.value[activePointIndex.value] ?? null),
)
const activeExpensePoint = computed(() =>
  activePointIndex.value === null ? null : (expensePoints.value[activePointIndex.value] ?? null),
)
const activeAveragePoint = computed(() =>
  activePointIndex.value === null ? null : (averagePoints.value[activePointIndex.value] ?? null),
)
const tooltipPosition = computed(() => {
  const expensePoint = activeExpensePoint.value
  const averagePoint = activeAveragePoint.value
  if (!expensePoint || !averagePoint) return null

  return {
    x: expensePoint.x > 165 ? expensePoint.x - 148 : expensePoint.x + 10,
    y: Math.min(Math.max(Math.min(expensePoint.y, averagePoint.y) - 35, 4), 126),
  }
})
const linePoints = (points: ChartPoint[]) => points.map(({ x, y }) => `${x},${y}`).join(' ')
const areaPoints = computed(() => `12,188 ${linePoints(expensePoints.value)} 308,188`)
const displayedAnnualAmount = computed(() =>
  visibleExpenses.value.reduce((sum, { amount }) => sum + amount, 0),
)
const formatChartAmount = (amount: number) => `${Math.round(amount / 10_000)}만원`
const selectPeriod = (nextPeriod: Period) => {
  period.value = nextPeriod
  activePointIndex.value = null
}
onMounted(load)
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-white px-[18px] py-6 text-[var(--color-text-primary)]"
  >
    <div
      class="relative grid grid-cols-2 rounded-[14px] bg-[#f3f6f8] p-1"
      role="tablist"
      aria-label="조회 기간"
    >
      <span
        class="pointer-events-none absolute top-1 bottom-1 left-1 w-[calc(50%-4px)] rounded-[11px] bg-white shadow-[0_2px_8px_rgb(29_68_89_/_10%)] transition-transform duration-250 ease-out"
        :class="period === 'year' ? 'translate-x-full' : 'translate-x-0'"
        aria-hidden="true"
      ></span>
      <button
        v-for="item in [
          { id: '6months', label: '6개월' },
          { id: 'year', label: '1년' },
        ]"
        :key="item.id"
        class="relative z-1 flex h-10 items-center justify-center rounded-[11px] text-sm font-semibold transition-colors active:opacity-70"
        :class="
          period === item.id
            ? 'text-[var(--color-text-primary)]'
            : 'text-[var(--color-text-secondary)]'
        "
        type="button"
        role="tab"
        :aria-selected="period === item.id"
        @click="selectPeriod(item.id as Period)"
      >
        {{ item.label }}
      </button>
    </div>

    <section
      class="mt-5 rounded-[22px] border border-[var(--color-border)] bg-white p-5 shadow-[0_4px_14px_rgb(45_76_92_/_3%)]"
      aria-labelledby="monthly-expense-title"
    >
      <div>
        <h1 id="monthly-expense-title" class="m-0 text-[14px] font-bold">월별 지출 금액</h1>
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
      </div>

      <div class="mt-5" aria-label="월별 지출 추이 그래프">
        <div class="relative h-[248px] pl-7">
          <div
            class="absolute inset-y-5 left-0 flex flex-col justify-between text-[9px] text-[var(--color-text-secondary)]"
          >
            <span>{{ Math.round(chartMax / 10_000) }}</span
            ><span>{{ Math.round(chartMax / 20_000) }}</span
            ><span>0</span>
          </div>
          <svg
            :key="period"
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
            <polygon :points="areaPoints" fill="url(#childcare-area)" class="trend-chart-area" />
            <polyline
              :points="linePoints(averagePoints)"
              pathLength="1"
              fill="none"
              stroke="#c9cfd4"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="3"
              class="trend-chart-line trend-chart-line--average"
            />
            <polyline
              :points="linePoints(expensePoints)"
              pathLength="1"
              fill="none"
              stroke="var(--color-accent-yellow)"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="3"
              class="trend-chart-line trend-chart-line--expense"
            />
            <circle
              v-for="(point, index) in averagePoints"
              :key="`average-${index}`"
              :cx="point.x"
              :cy="point.y"
              r="3.5"
              fill="#c9cfd4"
              class="trend-chart-point"
              :style="{ animationDelay: `${380 + index * 45}ms` }"
            />
            <circle
              v-for="(point, index) in expensePoints"
              :key="`expense-${index}`"
              :cx="point.x"
              :cy="point.y"
              r="4"
              fill="var(--color-accent-yellow)"
              class="trend-chart-point"
              :style="{ animationDelay: `${520 + index * 45}ms` }"
            />
            <circle
              v-for="(point, index) in expensePoints"
              :key="`hit-${index}`"
              :cx="point.x"
              :cy="point.y"
              r="14"
              fill="transparent"
              class="cursor-pointer outline-none"
              tabindex="0"
              role="button"
              :aria-label="`${visibleExpenses[index]?.month} 지출 ${formatChartAmount(visibleExpenses[index]?.amount ?? 0)}, 평균 ${formatChartAmount(visibleExpenses[index]?.averageAmount ?? 0)}`"
              @mouseenter="activePointIndex = index"
              @mouseleave="activePointIndex = null"
              @focus="activePointIndex = index"
              @blur="activePointIndex = null"
              @click="activePointIndex = index"
              @touchstart="activePointIndex = index"
              @keydown.enter.prevent="activePointIndex = index"
              @keydown.space.prevent="activePointIndex = index"
            />
            <g
              v-if="activeExpense && activeExpensePoint && activeAveragePoint && tooltipPosition"
              class="pointer-events-none"
            >
              <line
                :x1="activeExpensePoint.x"
                :x2="activeExpensePoint.x"
                y1="20"
                y2="188"
                stroke="#aab4bc"
                stroke-dasharray="3 4"
                stroke-width="1"
              />
              <circle
                :cx="activeExpensePoint.x"
                :cy="activeExpensePoint.y"
                r="6"
                fill="var(--color-accent-yellow)"
                stroke="white"
                stroke-width="2"
              />
              <circle
                :cx="activeAveragePoint.x"
                :cy="activeAveragePoint.y"
                r="5.5"
                fill="#c9cfd4"
                stroke="white"
                stroke-width="2"
              />
              <g :transform="`translate(${tooltipPosition.x} ${tooltipPosition.y})`">
                <rect width="138" height="70" rx="11" fill="#333333" fill-opacity="1" />
                <text x="12" y="19" fill="white" font-size="11" font-weight="700">
                  {{ activeExpense.month }}
                </text>
                <text x="12" y="40" fill="#ffe36e" font-size="11" font-weight="700">
                  지출 {{ formatChartAmount(activeExpense.amount) }}
                </text>
                <text x="12" y="58" fill="#d9dee2" font-size="10">
                  평균 {{ formatChartAmount(activeExpense.averageAmount) }}
                </text>
              </g>
            </g>
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
      </div>

      <div
        class="mt-6 flex items-center justify-between gap-4 rounded-[18px] bg-[#f6f8fa] px-5 py-5"
      >
        <span class="text-[13px] font-bold"
          >{{ period === '6months' ? '최근 6개월' : '최근 1년' }} 지출 합계</span
        >
        <strong class="text-[19px]">{{
          formatReportWon(
            period === 'year' ? childcareReportSummary.annualAmount : displayedAnnualAmount,
          )
        }}</strong>
      </div>
    </section>
  </main>
</template>

<style scoped>
.trend-chart-line {
  stroke-dasharray: 1;
  stroke-dashoffset: 1;
  animation: draw-trend-line 900ms cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

.trend-chart-line--average {
  animation-delay: 80ms;
}

.trend-chart-line--expense {
  animation-delay: 180ms;
}

.trend-chart-area {
  opacity: 0;
  animation: reveal-trend-area 600ms ease-out 680ms forwards;
}

.trend-chart-point {
  opacity: 0;
  transform-box: fill-box;
  transform-origin: center;
  animation: reveal-trend-point 320ms cubic-bezier(0.2, 1.5, 0.4, 1) forwards;
}

@keyframes draw-trend-line {
  to {
    stroke-dashoffset: 0;
  }
}

@keyframes reveal-trend-area {
  to {
    opacity: 1;
  }
}

@keyframes reveal-trend-point {
  from {
    opacity: 0;
    transform: scale(0.25);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

@media (prefers-reduced-motion: reduce) {
  .trend-chart-line,
  .trend-chart-area,
  .trend-chart-point {
    animation: none;
    opacity: 1;
    stroke-dashoffset: 0;
    transform: none;
  }
}
</style>
