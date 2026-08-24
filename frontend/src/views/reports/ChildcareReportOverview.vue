<script setup lang="ts">
import { CalendarDays, ChevronRight, CircleAlert, TrendingUp, X } from 'lucide-vue-next'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

import { formatReportWon, useChildcareReport } from '@/composables/useChildcareReport'

const { childcareReportSummary, load } = useChildcareReport()
const isAverageInfoOpen = ref(false)
const displayedCurrentMonthAmount = ref(0)
let currentMonthAmountAnimationFrame: number | null = null
const differenceAmount = computed(() => childcareReportSummary.comparisonDifferenceAmount)
const differenceRate = computed(() => childcareReportSummary.comparisonDifferenceRate)
const differenceText = computed(() => differenceAmount.value > 0 ? '더 높아요' : differenceAmount.value < 0 ? '더 낮아요' : '같아요')
const comparisonMaxAmount = computed(() => Math.max(childcareReportSummary.currentMonthAmount, childcareReportSummary.peerAverageAmount, 1))
const comparisonBarWidth = (amount: number) =>
  `${Math.max((amount / comparisonMaxAmount.value) * 100, 8)}%`

const animateCurrentMonthAmount = () => {
  if (currentMonthAmountAnimationFrame !== null) {
    cancelAnimationFrame(currentMonthAmountAnimationFrame)
  }

  const targetAmount = childcareReportSummary.currentMonthAmount
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    displayedCurrentMonthAmount.value = targetAmount
    currentMonthAmountAnimationFrame = null
    return
  }

  displayedCurrentMonthAmount.value = 0
  const startedAt = performance.now()
  const duration = 1100

  const updateAmount = (currentTime: number) => {
    const progress = Math.min((currentTime - startedAt) / duration, 1)
    const easedProgress = 1 - Math.pow(1 - progress, 3)
    displayedCurrentMonthAmount.value = Math.round(targetAmount * easedProgress)

    if (progress < 1) {
      currentMonthAmountAnimationFrame = requestAnimationFrame(updateAmount)
      return
    }

    displayedCurrentMonthAmount.value = targetAmount
    currentMonthAmountAnimationFrame = null
  }

  currentMonthAmountAnimationFrame = requestAnimationFrame(updateAmount)
}

onMounted(async () => {
  await load()
  animateCurrentMonthAmount()
})

onBeforeUnmount(() => {
  if (currentMonthAmountAnimationFrame !== null) {
    cancelAnimationFrame(currentMonthAmountAnimationFrame)
  }
})
</script>

<template>
  <div class="px-[18px] py-5 text-[var(--color-text-primary)]">
    <RouterLink
      class="block rounded-[22px] border border-[var(--color-accent-yellow-border)] bg-[var(--color-accent-yellow-surface)] p-5 !text-[var(--color-text-primary)] shadow-[0_8px_22px_rgba(176,142,43,0.04)] active:scale-[0.995]"
      :to="{ name: 'ChildcareMonthlyTrend' }"
    >
      <div class="flex items-start justify-between gap-4">
        <div>
          <p class="m-0 text-sm font-semibold text-[var(--color-text-secondary)]">
            이번 달 아이 관련 지출
          </p>
          <strong
            class="mt-3 block text-[28px] leading-none tracking-[-0.04em] tabular-nums"
            :aria-label="`이번 달 아이 관련 지출 ${formatReportWon(childcareReportSummary.currentMonthAmount)}`"
          >
            {{ formatReportWon(displayedCurrentMonthAmount) }}
          </strong>
          <p class="mt-3 mb-0 text-[12px] text-[var(--color-text-secondary)]">
            지난달 대비
            <strong class="text-[var(--color-accent-yellow-text)]">
              {{ formatReportWon(childcareReportSummary.previousMonthDifference) }}
            </strong>
            늘었어요
          </p>
        </div>
        <span
          class="grid size-11 shrink-0 place-items-center rounded-2xl bg-white/80 text-[var(--color-accent-yellow-text)]"
        >
          <ChevronRight :size="23" :stroke-width="2.4" aria-hidden="true" />
        </span>
      </div>
    </RouterLink>

    <section
      class="mt-4 rounded-[22px] border border-[#e5e8eb] bg-white p-5 shadow-[0_5px_18px_rgba(64,78,86,0.04)]"
    >
      <div class="flex items-start justify-between gap-3">
        <div>
          <h2 class="m-0 text-[16px] font-extrabold">{{ childcareReportSummary.comparisonLabel }}과 비교</h2>
          <p class="mt-1.5 mb-0 text-[12px] leading-5 text-[var(--color-text-secondary)]">
            {{ childcareReportSummary.comparisonLabel }}와 비교했어요.
          </p>
        </div>
        <button
          class="grid size-8 shrink-0 place-items-center rounded-full bg-[var(--color-surface-muted)] text-[var(--color-text-secondary)] transition-colors active:bg-[#e9edef]"
          type="button"
          aria-label="동일 연령 평균 정보 보기"
          @click="isAverageInfoOpen = true"
        >
          <CircleAlert :size="18" :stroke-width="2.3" aria-hidden="true" />
        </button>
      </div>

      <div class="mt-6 grid gap-5">
        <div class="grid gap-5" aria-label="우리 집과 동일 연령 평균 양육비 막대 그래프">
          <div class="min-w-0">
            <div class="flex items-center justify-between gap-3 text-[12px]">
              <strong>우리 집</strong>
              <strong class="shrink-0 tabular-nums">
                {{ formatReportWon(childcareReportSummary.currentMonthAmount) }}
              </strong>
            </div>
            <div class="mt-2 h-4 overflow-hidden rounded-full bg-[#f0f2f4]">
              <div
                class="childcare-comparison-bar h-full rounded-full bg-[var(--color-accent-yellow)]"
                :style="{
                  width: comparisonBarWidth(childcareReportSummary.currentMonthAmount),
                }"
              ></div>
            </div>
          </div>

          <div class="min-w-0">
            <div class="flex items-center justify-between gap-3 text-[12px]">
              <strong class="text-[var(--color-text-secondary)]">{{ childcareReportSummary.comparisonLabel }}</strong>
              <strong class="shrink-0 tabular-nums text-[var(--color-text-secondary)]">
                {{ formatReportWon(childcareReportSummary.peerAverageAmount) }}
              </strong>
            </div>
            <div class="mt-2 h-4 overflow-hidden rounded-full bg-[#f0f2f4]">
              <div
                class="childcare-comparison-bar childcare-comparison-bar--average h-full rounded-full bg-[#cfd5da]"
                :style="{
                  width: comparisonBarWidth(childcareReportSummary.peerAverageAmount),
                }"
              ></div>
            </div>
          </div>
        </div>

        <div
          class="flex flex-wrap items-center justify-between gap-x-3 gap-y-1 border-t border-[#edf0f2] pt-4 text-[11px]"
        >
          <span class="text-[var(--color-text-secondary)]">비교 기준보다</span>
          <strong class="text-right">
            <span class="text-[var(--color-accent-yellow-text)]">
              {{ formatReportWon(Math.abs(differenceAmount)) }} {{ differenceText }}
            </span>
            <span class="ml-1 text-[var(--color-text-secondary)]">({{ differenceRate }}%)</span>
          </strong>
        </div>
      </div>

      <RouterLink
        class="mt-4 flex h-11 w-full items-center justify-center gap-1 rounded-[13px] border border-[var(--color-accent-yellow-border)] bg-[var(--color-accent-yellow)] text-[12px] font-bold !text-[var(--color-text-primary)] active:bg-[var(--color-accent-yellow-pressed)]"
        :to="{ name: 'ChildcareAgeComparison' }"
      >
        비교 분석
      </RouterLink>
    </section>

    <section class="mt-7 pb-2" aria-labelledby="childcare-insight-title">
      <h2 id="childcare-insight-title" class="m-0 text-[21px] font-extrabold tracking-[-0.03em]">
        이번 달 인사이트
      </h2>
      <div class="mt-4 grid gap-3">
        <article class="flex items-center gap-4 rounded-[18px] bg-[var(--color-surface-muted)] p-4">
          <TrendingUp
            class="shrink-0 text-[var(--color-accent-yellow-pressed)]"
            :size="27"
            :stroke-width="2.2"
          />
          <div>
            <strong class="text-sm">지난달보다 지출이 {{ Math.abs(childcareReportSummary.previousMonthRate) }}% {{ childcareReportSummary.previousMonthDifference >= 0 ? '늘었어요' : '줄었어요' }}.</strong>
            <p class="mt-1 mb-0 text-xs text-[var(--color-text-secondary)]">
              지난달과 비교한 실제 지출 흐름이에요.
            </p>
          </div>
        </article>
        <article class="flex items-center gap-4 rounded-[18px] bg-[var(--color-surface-muted)] p-4">
          <CalendarDays
            class="shrink-0 text-[var(--color-accent-yellow-pressed)]"
            :size="27"
            :stroke-width="2.2"
          />
          <div>
            <strong class="text-sm">다음 달 교육비를 미리 계획해보세요.</strong>
            <p class="mt-1 mb-0 text-xs text-[var(--color-text-secondary)]">
              정기 지출을 먼저 나누면 예산 관리가 쉬워져요.
            </p>
          </div>
        </article>
      </div>
    </section>

    <Teleport to="body">
      <Transition name="average-info-modal">
        <div
          v-if="isAverageInfoOpen"
          class="fixed inset-0 z-[var(--z-index-overlay)] grid place-items-center bg-black/40 p-5"
          role="presentation"
          @click.self="isAverageInfoOpen = false"
        >
          <section
            class="flex max-h-[calc(100dvh-40px)] w-full max-w-[360px] flex-col overflow-hidden rounded-[24px] bg-white shadow-[0_18px_52px_rgba(32,42,49,0.22)]"
            role="dialog"
            aria-modal="true"
            aria-labelledby="average-info-modal-title"
          >
            <header
              class="flex shrink-0 items-start justify-between gap-3 border-b border-[#edf0f2] px-5 pt-5 pb-4"
            >
              <div>
                <span class="text-[10px] font-bold text-[var(--color-accent-yellow-text)]">
                  공공 통계 기반
                </span>
                <h2 id="average-info-modal-title" class="mt-1 mb-0 text-[18px] font-extrabold">
                  동일 연령 평균 정보
                </h2>
                <p class="mt-1 mb-0 text-[11px] text-[var(--color-text-secondary)]">
                  {{ childcareReportSummary.ageGroup }} 기준
                </p>
              </div>
              <button
                class="grid size-9 shrink-0 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[var(--color-surface-muted)]"
                type="button"
                aria-label="평균 정보 닫기"
                @click="isAverageInfoOpen = false"
              >
                <X :size="21" :stroke-width="2.4" aria-hidden="true" />
              </button>
            </header>

            <div class="min-h-0 flex-1 overflow-y-auto">
              <div class="px-5 py-4">
                <p class="m-0 text-[12px] font-bold">{{ childcareReportSummary.comparisonLabel }}</p>
                <strong class="mt-2 block text-[22px]">{{ formatReportWon(childcareReportSummary.peerAverageAmount) }}</strong>
                <p class="mt-4 mb-0 text-[11px] leading-5 text-[var(--color-text-secondary)]">{{ childcareReportSummary.benchmarkCalculationBasis || '공공 통계 기준' }}</p>
              </div>

              <p class="m-0 px-5 py-4 text-[10px] leading-4 text-[var(--color-text-secondary)]">
                연령별 평균 금액은 가정 상황과 조사 시점에 따라 달라질 수 있으며, 양육 계획을 위한
                참고 자료로 활용해주세요.
              </p>
            </div>
          </section>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.childcare-comparison-bar {
  transform: scaleX(0);
  transform-origin: left center;
  animation: childcare-bar-grow 800ms cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

.childcare-comparison-bar--average {
  animation-delay: 100ms;
}

@keyframes childcare-bar-grow {
  to {
    transform: scaleX(1);
  }
}

.average-info-modal-enter-active,
.average-info-modal-leave-active {
  transition: opacity 180ms ease;
}

.average-info-modal-enter-active > section,
.average-info-modal-leave-active > section {
  transition: transform 220ms cubic-bezier(0.22, 1, 0.36, 1);
}

.average-info-modal-enter-from,
.average-info-modal-leave-to {
  opacity: 0;
}

.average-info-modal-enter-from > section,
.average-info-modal-leave-to > section {
  transform: translateY(12px) scale(0.98);
}

@media (prefers-reduced-motion: reduce) {
  .childcare-comparison-bar {
    transform: scaleX(1);
    animation: none;
  }

  .average-info-modal-enter-active,
  .average-info-modal-leave-active,
  .average-info-modal-enter-active > section,
  .average-info-modal-leave-active > section {
    transition-duration: 1ms;
  }
}
</style>
