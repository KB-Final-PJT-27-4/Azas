<script setup lang="ts">
import { computed } from 'vue'
import { BaseDatePicker } from '@/components/common'

const props = withDefaults(
  defineProps<{
    goalName: string
    amount: number
    targetDate: string
    goalNumber?: number
    showIntro?: boolean
    appearance?: 'default' | 'management'
  }>(),
  {
    showIntro: true,
    appearance: 'default',
  },
)

const emit = defineEmits<{
  'update:amount': [value: number]
  'update:targetDate': [value: string]
  openRecommendation: []
}>()

const today = new Date()
const currentYear = today.getFullYear()
const currentMonth = today.getMonth() + 1
const todayValue = `${currentYear}-${String(currentMonth).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

const remainingMonths = computed(() => {
  const match = /^(\d{4})-(\d{2})$/.exec(props.targetDate)
  if (!match) return 1

  const targetYear = Number(match[1])
  const targetMonth = Number(match[2])
  const monthDifference = (targetYear - currentYear) * 12 + (targetMonth - currentMonth)

  return Math.max(monthDifference, 1)
})

const monthlySavings = computed(() =>
  props.amount > 0 ? Math.ceil(props.amount / remainingMonths.value) : 0,
)

const updateAmount = (event: Event) => {
  const value = Number((event.target as HTMLInputElement).value.replace(/[^0-9]/g, ''))
  emit('update:amount', value)
}
</script>

<template>
  <div
    class="min-w-0 w-full max-w-full"
    :class="appearance === 'management' ? 'overflow-visible' : 'overflow-x-clip'"
  >
    <h1
      v-if="showIntro"
      class="max-w-full break-keep text-[25px] leading-[1.35] font-bold tracking-[-0.04em] [overflow-wrap:anywhere]"
    >
      <span
        v-if="goalNumber"
        class="mr-1.5 inline-grid size-6 translate-y-[-2px] place-items-center rounded-full bg-[var(--color-brand-primary)] text-sm tracking-normal text-[var(--color-text-inverse)] align-middle"
      >
        {{ goalNumber }}
      </span>
      <span class="text-[var(--color-selected-text)]">{{ goalName }}</span> 목표 금액을 정해주세요
    </h1>
    <p v-if="showIntro" class="mt-2 text-sm text-[var(--color-text-secondary)]">
      금액과 시기를 입력하면 월 저축액을 계산해드려요.
    </p>

    <div class="grid min-w-0 w-full max-w-full gap-6" :class="showIntro ? 'mt-9' : ''">
      <label class="grid min-w-0 w-full max-w-full gap-2">
        <span class="flex min-w-0 items-center justify-between gap-3 text-sm font-bold">
          목표 금액
          <button
            class="shrink-0 rounded-full border border-[var(--color-border)] px-3 py-1 text-xs"
            type="button"
            @click="emit('openRecommendation')"
          >
            AI 추천 ⓘ
          </button>
        </span>
        <div
          class="flex h-16 min-w-0 w-full max-w-full items-center rounded-2xl border px-5 text-[clamp(23px,7vw,27px)] font-bold transition-colors focus-within:border-[var(--color-brand-primary)] focus-within:ring-2 focus-within:ring-[#dff5ff]"
          :class="
            appearance === 'management'
              ? 'border-[#d9edf7] bg-white'
              : 'border-transparent bg-[var(--color-brand-secondary)]'
          "
        >
          <input
            class="w-0 min-w-0 flex-1 bg-transparent outline-none"
            inputmode="numeric"
            :value="amount.toLocaleString('ko-KR')"
            aria-label="목표 금액"
            @input="updateAmount"
          />
          <span class="shrink-0">원</span>
        </div>
      </label>

      <BaseDatePicker
        class="min-w-0 w-full max-w-full"
        :model-value="targetDate"
        label="목표 달성 시기"
        selection-mode="month"
        :min-date="todayValue"
        :inline-panel="appearance !== 'management'"
        :min-year="currentYear"
        :max-year="currentYear + 100"
        @update:model-value="emit('update:targetDate', $event)"
      />

      <div
        class="min-w-0 w-full max-w-full rounded-2xl p-5"
        :class="
          appearance === 'management'
            ? 'border border-[#dceef7] bg-[#f5fbfe]'
            : 'bg-[var(--color-selected-background)]'
        "
      >
        <div class="text-xl font-semibold text-[var(--color-selected-text)]">
          매월 약 {{ monthlySavings.toLocaleString('ko-KR') }}원
        </div>
        <p class="mt-2 text-sm text-[var(--color-text-secondary)]">
          현재부터 {{ remainingMonths.toLocaleString('ko-KR') }}개월 동안 균등 저축 기준
        </p>
      </div>
    </div>
  </div>
</template>
