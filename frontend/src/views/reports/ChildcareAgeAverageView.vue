<script setup lang="ts">
import { onMounted } from 'vue'
import { formatReportWon, useChildcareReport } from '@/composables/useChildcareReport'

const { childcareCategories, childcareReportSummary, load } = useChildcareReport()
onMounted(load)
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-white px-[18px] py-6 text-[var(--color-text-primary)]"
  >
    <section
      class="overflow-hidden rounded-[22px] border border-[#e3e7ea] bg-white shadow-[0_6px_20px_rgba(71,77,82,0.04)]"
      aria-labelledby="age-average-title"
    >
      <div class="bg-[var(--color-accent-yellow-surface)] px-5 py-5">
        <span
          class="inline-flex rounded-full bg-white/75 px-2.5 py-1 text-[10px] font-bold text-[var(--color-accent-yellow-text)]"
          >공공 통계 기반</span
        >
        <h1 id="age-average-title" class="mt-2.5 mb-0 text-[18px] font-extrabold">
          동일 연령 평균 정보
        </h1>
        <p class="mt-1.5 mb-0 text-[12px] font-semibold text-[var(--color-text-secondary)]">
          {{ childcareReportSummary.ageGroup }} 기준
        </p>
      </div>

      <div
        class="grid grid-cols-[minmax(0,1fr)_auto] bg-[#fafbfb] px-5 py-3 text-[11px] font-bold text-[var(--color-text-secondary)]"
      >
        <span>항목</span>
        <span>월 평균 금액</span>
      </div>

      <dl class="m-0">
        <div
          v-for="category in childcareCategories"
          :key="category.id"
          class="grid min-h-14 grid-cols-[minmax(0,1fr)_auto] items-center gap-3 border-t border-[#edf0f2] px-5 py-3.5"
        >
          <dt class="flex min-w-0 items-center gap-2.5 text-[13px] font-bold">
            <span
              class="size-2.5 shrink-0 rounded-full"
              :style="{ backgroundColor: category.color }"
            ></span>
            {{ category.label }}
          </dt>
          <dd class="m-0 text-right text-[13px] font-semibold text-[var(--color-text-secondary)]">
            {{ formatReportWon(category.averageAmount) }}
          </dd>
        </div>
      </dl>

      <div
        class="grid grid-cols-[minmax(0,1fr)_auto] items-center gap-3 border-t border-[#e2e7ea] bg-[var(--color-accent-yellow-surface)] px-5 py-5"
      >
        <strong class="text-[14px]">합계</strong>
        <strong class="text-[20px] tracking-[-0.02em]">
          {{ formatReportWon(childcareReportSummary.peerAverageAmount) }}
        </strong>
      </div>
    </section>

    <p class="mt-4 mb-0 px-1 text-[10px] leading-4 text-[var(--color-text-secondary)]">
      연령별 평균 금액은 가정 상황과 조사 시점에 따라 달라질 수 있으며, 양육 계획을 위한 참고 자료로
      활용해주세요.
    </p>
  </main>
</template>
