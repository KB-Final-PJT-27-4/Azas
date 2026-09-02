<script setup lang="ts">
import { computed, useId } from 'vue'
import { BaseDatePicker } from '@/components/common'

const props = withDefaults(
  defineProps<{
    goalName: string
    amount: number
    targetDate: string
    goalNumber?: number
    showIntro?: boolean
    appearance?: 'default' | 'management' | 'unified'
    progressive?: boolean
    hasRecommendation?: boolean
  }>(),
  {
    showIntro: true,
    appearance: 'default',
    progressive: false,
    hasRecommendation: true,
  },
)

const emit = defineEmits<{
  'update:amount': [value: number]
  'update:targetDate': [value: string]
  openRecommendation: []
}>()

const amountInputId = `goal-amount-${useId()}`

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
  <div class="min-w-0 w-full max-w-full overflow-visible">
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

    <div
      class="grid min-w-0 w-full max-w-full"
      :class="[showIntro ? 'mt-9' : '', appearance === 'unified' ? 'gap-5' : 'gap-6']"
    >
      <div class="grid min-w-0 w-full max-w-full gap-2">
        <span class="flex min-w-0 items-center justify-between gap-3 text-sm font-bold">
          <label :for="amountInputId">목표 금액</label>
          <button
            v-if="hasRecommendation"
            class="shrink-0 rounded-full px-3 py-1.5 text-xs font-semibold transition-colors active:bg-[#e8edf0]"
            :class="appearance === 'unified' ? 'bg-[#e7f7ff] text-[#239fd6]' : 'border border-[var(--color-border)]'"
            type="button"
            @click.stop="emit('openRecommendation')"
          >
            목표 금액 가이드 ⓘ
          </button>
        </span>
        <div
          class="flex min-w-0 w-full max-w-full items-center rounded-2xl border px-5 font-bold transition-[border-color,box-shadow,background-color]"
          :class="
            appearance === 'management'
              ? 'h-16 border-[#d9edf7] bg-white text-[clamp(23px,7vw,27px)] focus-within:border-[var(--color-brand-primary)] focus-within:ring-3 focus-within:ring-[#dff5ff]/70'
              : appearance === 'unified'
                ? 'h-[62px] border-[#dce5ea] bg-white text-[24px] focus-within:border-[#79ccef] focus-within:ring-3 focus-within:ring-[#def4fd]/80'
                : 'h-16 border-transparent bg-[var(--color-brand-secondary)] text-[clamp(23px,7vw,27px)] focus-within:border-[var(--color-brand-primary)] focus-within:ring-3 focus-within:ring-[#dff5ff]/70'
          "
        >
          <input
            :id="amountInputId"
            class="w-0 min-w-0 flex-1 bg-transparent outline-none"
            inputmode="numeric"
            :value="amount > 0 ? amount.toLocaleString('ko-KR') : ''"
            placeholder="목표 금액 입력"
            aria-label="목표 금액"
            @input="updateAmount"
          />
          <span class="shrink-0">원</span>
        </div>
      </div>

      <Transition name="progressive-field">
        <BaseDatePicker
          v-if="!progressive || amount > 0"
          class="min-w-0 w-full max-w-full"
          :model-value="targetDate"
          label="목표 달성 시기"
          selection-mode="month"
          :min-date="todayValue"
          :inline-panel="false"
          panel-placement="inline-top"
          :min-year="currentYear"
          :max-year="currentYear + 100"
          @update:model-value="emit('update:targetDate', $event)"
        />
      </Transition>

      <Transition name="progressive-field">
        <div
          v-if="!progressive || Boolean(targetDate)"
          class="min-w-0 w-full max-w-full rounded-2xl"
          :class="
            appearance === 'management'
              ? 'border border-[#dceef7] bg-[#f5fbfe] p-5'
              : appearance === 'unified'
                ? 'flex items-center justify-between gap-4 border border-[#cceaf7] bg-[#eaf8ff] px-5 py-4'
                : 'bg-[var(--color-selected-background)] p-5'
          "
        >
          <div
            class="font-semibold"
            :class="appearance === 'unified' ? 'text-lg text-[var(--color-selected-text)]' : 'text-xl text-[var(--color-selected-text)]'"
          >
            매월 약 {{ monthlySavings.toLocaleString('ko-KR') }}원
          </div>
          <p
            class="text-[var(--color-text-secondary)]"
            :class="appearance === 'unified' ? 'shrink-0 text-right text-xs' : 'mt-2 text-sm'"
          >
            <template v-if="appearance === 'unified'">
              {{ remainingMonths.toLocaleString('ko-KR') }}개월 기준
            </template>
            <template v-else>
              현재부터 {{ remainingMonths.toLocaleString('ko-KR') }}개월 동안 균등 저축 기준
            </template>
          </p>
        </div>
      </Transition>
    </div>
  </div>
</template>

<style scoped>
.progressive-field-enter-active,
.progressive-field-leave-active {
  transition:
    transform 360ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 240ms ease,
    clip-path 360ms cubic-bezier(0.22, 1, 0.36, 1);
}
.progressive-field-enter-from {
  transform: translateY(14px);
  opacity: 0;
  clip-path: inset(0 0 100% 0 round 16px);
}
.progressive-field-leave-to {
  transform: translateY(-4px);
  opacity: 0;
}
</style>
