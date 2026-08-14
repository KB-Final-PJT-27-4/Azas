<script setup lang="ts">
import { ref } from 'vue'

import {
  childcareCategories,
  childcareReportSummary,
  formatReportManwon,
  formatReportWon,
} from '@/data/childcareReportData'

const maxAmount = Math.max(
  ...childcareCategories.flatMap(({ amount, averageAmount }) => [amount, averageAmount]),
)
const difference =
  childcareReportSummary.currentMonthAmount - childcareReportSummary.peerAverageAmount
const selectedCategoryId = ref(childcareCategories[0]?.id ?? '')
const barHeight = (amount: number) => `${Math.max((amount / maxAmount) * 74, 5)}%`
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-white px-[18px] py-6 text-[var(--color-text-primary)]"
  >
    <section>
      <span
        class="inline-flex rounded-full bg-[var(--color-accent-yellow-surface)] px-3 py-1.5 text-[10px] font-bold text-[var(--color-accent-yellow-text)]"
        >이번 달 비교</span
      >
      <h1 class="mt-3 mb-0 text-[19px] font-extrabold">동일 연령 평균과 함께 살펴보세요</h1>
      <p class="mt-2 mb-0 text-[12px] leading-5 text-[var(--color-text-secondary)]">
        평균은 참고용 정보예요. 우리 집의 소비 흐름을 이해하는 데 활용해보세요.
      </p>
    </section>

    <section class="mt-5 grid grid-cols-2 gap-3" aria-label="총 지출 비교">
      <div
        class="rounded-[18px] border border-[var(--color-accent-yellow-border)] bg-[var(--color-accent-yellow-surface)] p-4"
      >
        <span class="text-[11px] font-bold text-[var(--color-text-secondary)]"
          >우리 집 이번 달</span
        >
        <strong class="mt-2 block text-[19px] text-[var(--color-accent-yellow-text)]">{{
          formatReportWon(childcareReportSummary.currentMonthAmount)
        }}</strong>
      </div>
      <div class="rounded-[18px] border border-[#e4e8eb] bg-[#fafbfb] p-4">
        <span class="text-[11px] font-bold text-[var(--color-text-secondary)]"
          >동일 연령 월평균</span
        >
        <strong class="mt-2 block text-[19px]">{{
          formatReportWon(childcareReportSummary.peerAverageAmount)
        }}</strong>
      </div>
    </section>

    <div
      class="mt-4 rounded-[16px] bg-[var(--color-accent-yellow-surface)] px-4 py-3.5 text-[12px] leading-5 text-[var(--color-text-secondary)]"
    >
      이번 달 우리 집 지출은 동일 연령 평균보다
      <strong class="text-[var(--color-accent-yellow-text)]"
        >약 {{ formatReportManwon(difference) }} 높은 편</strong
      >이에요. 특정 항목의 지출이 일시적으로 늘었는지 함께 확인해보세요.
    </div>

    <section
      class="mt-5 rounded-[22px] border border-[#e3e7ea] bg-white p-5 shadow-[0_5px_18px_rgba(64,78,86,0.04)]"
      aria-labelledby="category-comparison-title"
    >
      <h2 id="category-comparison-title" class="m-0 text-[16px] font-extrabold">
        항목별 지출 흐름
      </h2>
      <p class="mt-1.5 mb-0 text-[11px] leading-5 text-[var(--color-text-secondary)]">
        우리 집 지출은 노란색, 평균은 회색으로 표시했어요. 막대를 눌러 금액을 확인해보세요.
      </p>

      <div
        class="mt-4 flex items-center gap-5 text-[11px] font-bold text-[var(--color-text-secondary)]"
      >
        <span class="inline-flex items-center gap-1.5"
          ><i class="h-2.5 w-4 rounded-full bg-[var(--color-accent-yellow)]"></i>우리 집</span
        >
        <span class="inline-flex items-center gap-1.5"
          ><i class="h-2.5 w-4 rounded-full bg-[#d8dde1]"></i>동일 연령 평균</span
        >
      </div>

      <div
        class="mt-5 grid h-[240px] grid-cols-6 gap-2 border-b border-[#dfe4e7] bg-[repeating-linear-gradient(to_bottom,transparent_0,transparent_59px,#edf0f2_60px)] px-1"
        role="group"
        aria-label="항목별 우리 집 지출과 동일 연령 평균 막대그래프"
      >
        <button
          v-for="category in childcareCategories"
          :key="category.id"
          class="relative flex min-w-0 flex-col justify-end border-0 bg-transparent p-0 text-inherit outline-none focus-visible:rounded-md focus-visible:ring-2 focus-visible:ring-[var(--color-accent-yellow-border)]"
          type="button"
          :aria-label="`${category.label}, 우리 집 ${formatReportWon(category.amount)}, 동일 연령 평균 ${formatReportWon(category.averageAmount)}`"
          :aria-pressed="selectedCategoryId === category.id"
          @click="selectedCategoryId = category.id"
        >
          <div class="relative flex h-[205px] items-end justify-center gap-1">
            <div
              v-if="selectedCategoryId === category.id"
              class="absolute left-1/2 z-10 flex -translate-x-1/2 items-center gap-1 whitespace-nowrap"
              :style="{
                bottom: `calc(${barHeight(Math.max(category.amount, category.averageAmount))} + 8px)`,
              }"
            >
              <span
                class="inline-flex items-center gap-1.5 rounded-full border border-[var(--color-accent-yellow-border)] bg-white px-2.5 py-1 text-[8px] font-extrabold shadow-[0_3px_8px_rgba(126,99,0,0.12)]"
              >
                <b class="text-[var(--color-accent-yellow-text)]">{{
                  formatReportManwon(category.amount)
                }}</b>
                <i class="not-italic text-[#c8ced3]" aria-hidden="true">|</i>
                <b class="text-[#596873]">{{ formatReportManwon(category.averageAmount) }}</b>
              </span>
            </div>
            <span
              class="w-[11px] rounded-t-[5px] transition-[height,background-color] duration-200"
              :class="
                selectedCategoryId === category.id
                  ? 'bg-[var(--color-accent-yellow)]'
                  : 'bg-[#ffe990]'
              "
              :style="{ height: barHeight(category.amount) }"
            ></span>
            <span
              class="w-[11px] rounded-t-[5px] transition-[height,background-color] duration-200"
              :class="selectedCategoryId === category.id ? 'bg-[#c8ced3]' : 'bg-[#d8dde1]'"
              :style="{ height: barHeight(category.averageAmount) }"
            ></span>
          </div>
          <span
            class="mt-2 h-7 break-keep text-center text-[9px] leading-3 font-semibold text-[var(--color-text-secondary)]"
            >{{ category.label.replace('/', '/\u200b') }}</span
          >
        </button>
      </div>
    </section>
  </main>
</template>
