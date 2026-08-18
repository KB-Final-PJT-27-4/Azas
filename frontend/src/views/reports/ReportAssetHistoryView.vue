<script setup lang="ts">
import { ArrowUpRight, ChevronDown } from 'lucide-vue-next'
import { computed, ref, watch } from 'vue'

type Period = '6개월' | '1년'
type MonthlyRecord = {
  key: string
  month: string
  saving: number
  increase: number
}

const periods: Period[] = ['6개월', '1년']
const selectedPeriod = ref<Period>('1년')
const selectedMonthKey = ref('2026-07')
const showAllHistory = ref(false)

const allMonthlyData: MonthlyRecord[] = [
  { key: '2025-08', month: '8월', saving: 620_000, increase: 280_000 },
  { key: '2025-09', month: '9월', saving: 830_000, increase: 310_000 },
  { key: '2025-10', month: '10월', saving: 900_000, increase: 350_000 },
  { key: '2025-11', month: '11월', saving: 780_000, increase: 290_000 },
  { key: '2025-12', month: '12월', saving: 1_050_000, increase: 420_000 },
  { key: '2026-01', month: '1월', saving: 980_000, increase: 360_000 },
  { key: '2026-02', month: '2월', saving: 750_000, increase: 400_000 },
  { key: '2026-03', month: '3월', saving: 1_100_000, increase: 400_000 },
  { key: '2026-04', month: '4월', saving: 1_450_000, increase: 150_000 },
  { key: '2026-05', month: '5월', saving: 1_280_000, increase: 320_000 },
  { key: '2026-06', month: '6월', saving: 1_200_000, increase: 480_000 },
  { key: '2026-07', month: '7월', saving: 1_750_000, increase: 350_000 },
]

const visibleData = computed(() => {
  if (selectedPeriod.value === '6개월') return allMonthlyData.slice(-6)
  return allMonthlyData.slice(-12)
})
const selectedMonth = computed(
  () =>
    visibleData.value.find(({ key }) => key === selectedMonthKey.value) ??
    visibleData.value.at(-1)!,
)
const maxSaving = computed(() => Math.max(...visibleData.value.map(({ saving }) => saving)))
const periodSaving = computed(() =>
  visibleData.value.reduce((total, { saving }) => total + saving, 0),
)
const periodIncrease = computed(() =>
  visibleData.value.reduce((total, { increase }) => total + increase, 0),
)
const averageSaving = computed(() => Math.round(periodSaving.value / visibleData.value.length))
const bestMonth = computed(() =>
  visibleData.value.reduce((best, item) => (item.saving > best.saving ? item : best)),
)
const historyItems = computed(() => {
  const reversed = [...visibleData.value].reverse()
  return showAllHistory.value ? reversed : reversed.slice(0, 4)
})
const periodLabel = computed(() => `최근 ${selectedPeriod.value}`)
const formatWon = (amount: number) => `${amount.toLocaleString('ko-KR')}원`
const currentAssets = 20_750_000
const goalAssets = [
  { name: '대학자금', amount: 14_600_000, color: '#55bcef' },
  { name: '목돈 마련', amount: 6_150_000, color: '#f2c94c' },
]

watch(selectedPeriod, () => {
  selectedMonthKey.value = visibleData.value.at(-1)!.key
  showAllHistory.value = false
})
</script>

<template>
  <main class="min-h-full bg-white px-[18px] py-5 text-[var(--color-text-primary)]">
    <div
      class="relative grid grid-cols-2 rounded-[14px] bg-[#f3f6f8] p-1"
      role="tablist"
      aria-label="조회 기간"
    >
      <span
        class="pointer-events-none absolute top-1 bottom-1 left-1 w-[calc(50%-4px)] rounded-[11px] bg-white shadow-[0_2px_8px_rgb(29_68_89_/_10%)] transition-transform duration-250 ease-out"
        :class="selectedPeriod === '1년' ? 'translate-x-full' : 'translate-x-0'"
        aria-hidden="true"
      ></span>
      <button
        v-for="period in periods"
        :key="period"
        class="relative z-1 flex h-10 items-center justify-center rounded-[11px] text-sm font-semibold transition-colors active:opacity-70"
        :class="
          selectedPeriod === period
            ? 'text-[var(--color-text-primary)]'
            : 'text-[var(--color-text-secondary)]'
        "
        type="button"
        role="tab"
        :aria-selected="selectedPeriod === period"
        @click="selectedPeriod = period"
      >
        {{ period }}
      </button>
    </div>

    <section class="mt-5 rounded-[22px] border border-[var(--color-border)] bg-white p-5">
      <p class="text-sm font-bold">월별 저축 금액</p>
      <strong class="mt-3 block text-[27px] tracking-[-0.04em]">{{
        formatWon(selectedMonth.saving)
      }}</strong>
      <p class="mt-1 text-xs text-[var(--color-text-secondary)]">
        {{ selectedMonth.month }} 저축 금액 · 막대를 눌러 확인해보세요
      </p>

      <div
        :key="selectedPeriod"
        class="relative mt-7 flex h-48 items-end justify-between gap-0.5 px-0.5"
      >
        <span
          class="pointer-events-none absolute right-0 bottom-0 left-0 h-px bg-[#e5ebef]"
          aria-hidden="true"
        ></span>
        <button
          v-for="(item, index) in visibleData"
          :key="item.key"
          class="relative z-1 flex h-full min-w-0 flex-1 flex-col items-center justify-end rounded-t-lg focus-visible:outline-2 focus-visible:outline-[var(--color-brand-primary)]"
          type="button"
          :aria-label="`${item.month} ${formatWon(item.saving)}`"
          :aria-pressed="selectedMonth.key === item.key"
          @click="selectedMonthKey = item.key"
        >
          <span
            v-if="selectedMonth.key === item.key"
            class="asset-history-tooltip mb-2 whitespace-nowrap rounded-lg bg-[var(--color-brand-primary)] px-1.5 py-1 text-[9px] font-bold text-white"
            :style="{
              animationDelay:
                item.key === visibleData.at(-1)?.key ? `${760 + visibleData.length * 55}ms` : '0ms',
            }"
          >
            {{ Math.round(item.saving / 10_000) }}만원
          </span>
          <span
            class="asset-history-bar w-[clamp(10px,4vw,20px)] rounded-t-md transition-[height,background-color] duration-200"
            :class="selectedMonth.key === item.key ? 'bg-[#218ced]' : 'bg-[#9bc7f4]'"
            :style="{
              height: `${Math.max(18, (item.saving / maxSaving) * 128)}px`,
              animationDelay: `${80 + index * 55}ms`,
            }"
          ></span>
          <span
            class="absolute top-full mt-2 whitespace-nowrap text-[8px] font-medium tracking-[-0.05em] text-[var(--color-text-secondary)]"
          >
            {{ item.month }}
          </span>
        </button>
      </div>

      <div class="mt-11 flex items-center justify-between rounded-2xl bg-[#f5f8fa] px-4 py-4">
        <span class="text-sm font-semibold">{{ periodLabel }} 저축 합계</span>
        <strong class="text-lg">{{ formatWon(periodSaving) }}</strong>
      </div>
    </section>

    <section class="mt-4 rounded-[22px] border border-[var(--color-border)] bg-white p-5">
      <div>
        <p class="text-sm font-bold">자산 변화 요약</p>
        <strong class="mt-2 block text-[25px] text-[var(--color-selected-text)]"
          >+{{ formatWon(periodIncrease) }}</strong
        >
        <p class="mt-1 text-xs text-[var(--color-text-secondary)]">{{ periodLabel }} 기준</p>
      </div>

      <div class="mt-5 grid grid-cols-2 gap-3">
        <article class="rounded-2xl bg-[#eef8fd] p-4">
          <span class="text-xs text-[var(--color-text-secondary)]">월평균 저축</span>
          <strong class="mt-2 block text-lg text-[var(--color-selected-text)]">{{
            formatWon(averageSaving)
          }}</strong>
        </article>
        <article class="rounded-2xl bg-[#fff8dc] p-4">
          <span class="text-xs text-[var(--color-text-secondary)]">가장 많이 저축한 달</span>
          <strong class="mt-2 block text-lg">{{ bestMonth.month }}</strong>
          <span class="mt-1 block text-xs text-[var(--color-text-secondary)]">{{
            formatWon(bestMonth.saving)
          }}</span>
        </article>
      </div>
    </section>

    <section class="mt-4 rounded-[22px] border border-[var(--color-border)] bg-white p-5">
      <div class="flex items-center gap-2">
        <h2 class="text-base font-bold">목표별 자산 구성</h2>
      </div>
      <p class="mt-1 text-xs text-[var(--color-text-secondary)]">
        현재 자산이 어떤 목표에 모여 있는지 확인해보세요.
      </p>

      <div
        class="mt-5 flex h-3 overflow-hidden rounded-full bg-[#edf2f5]"
        aria-label="목표별 자산 비중"
      >
        <span
          v-for="goal in goalAssets"
          :key="goal.name"
          class="h-full first:rounded-l-full last:rounded-r-full"
          :style="{ width: `${(goal.amount / currentAssets) * 100}%`, backgroundColor: goal.color }"
        ></span>
      </div>

      <ul class="mt-4 grid gap-3">
        <li
          v-for="goal in goalAssets"
          :key="goal.name"
          class="flex items-center justify-between gap-3"
        >
          <span class="flex min-w-0 items-center gap-2.5">
            <span
              class="size-2.5 shrink-0 rounded-full"
              :style="{ backgroundColor: goal.color }"
            ></span>
            <strong class="truncate text-sm">{{ goal.name }}</strong>
          </span>
          <span class="shrink-0 text-right">
            <strong class="block text-sm">{{ formatWon(goal.amount) }}</strong>
            <span class="text-xs text-[var(--color-text-secondary)]">
              {{ ((goal.amount / currentAssets) * 100).toFixed(1) }}%
            </span>
          </span>
        </li>
      </ul>
    </section>

    <section
      class="mt-4 overflow-hidden rounded-[22px] border border-[var(--color-border)] bg-white"
    >
      <header class="flex items-center justify-between px-5 py-4">
        <h2 class="text-base font-bold">자산 증감 내역</h2>
        <span class="text-xs text-[var(--color-text-secondary)]">{{ periodLabel }}</span>
      </header>
      <ul class="divide-y divide-[#edf1f3] border-t border-[#edf1f3]">
        <li
          v-for="item in historyItems"
          :key="item.key"
          class="flex items-center justify-between px-5 py-4"
        >
          <div class="flex items-center gap-3">
            <span
              class="grid size-9 place-items-center rounded-full bg-[#eaf8ff] text-[var(--color-selected-text)]"
            >
              <ArrowUpRight :size="18" :stroke-width="2.4" />
            </span>
            <span
              ><strong class="block text-sm">{{ item.month }}</strong
              ><span class="text-xs text-[var(--color-text-secondary)]"
                >저축 및 이자 반영</span
              ></span
            >
          </div>
          <strong class="text-sm text-[var(--color-selected-text)]"
            >+{{ formatWon(item.increase) }}</strong
          >
        </li>
      </ul>
      <button
        v-if="visibleData.length > 4"
        class="flex h-12 w-full items-center justify-center gap-1 border-t border-[#edf1f3] text-sm font-semibold text-[var(--color-text-secondary)] active:bg-[#f7f9fa]"
        type="button"
        :aria-expanded="showAllHistory"
        @click="showAllHistory = !showAllHistory"
      >
        {{ showAllHistory ? '접기' : `${visibleData.length - 4}개월 더보기` }}
        <ChevronDown
          :size="17"
          class="transition-transform"
          :class="showAllHistory ? 'rotate-180' : ''"
        />
      </button>
    </section>
  </main>
</template>

<style scoped>
.asset-history-bar {
  transform: scaleY(0);
  transform-origin: bottom center;
  animation: grow-asset-history-bar 720ms cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

.asset-history-tooltip {
  animation: reveal-asset-history-tooltip 180ms cubic-bezier(0.22, 1, 0.36, 1) both;
  transform-origin: bottom center;
}

@keyframes grow-asset-history-bar {
  to {
    transform: scaleY(1);
  }
}

@keyframes reveal-asset-history-tooltip {
  from {
    opacity: 0;
    transform: translateY(4px) scale(0.92);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@media (prefers-reduced-motion: reduce) {
  .asset-history-bar,
  .asset-history-tooltip {
    animation: none;
    opacity: 1;
    transform: none;
  }
}
</style>
