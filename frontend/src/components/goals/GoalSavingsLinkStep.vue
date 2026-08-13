<script setup lang="ts">
import { Check } from 'lucide-vue-next'

import goalEducationIcon from '@/assets/images/goals/goals_1.png'
import goalLumpSumIcon from '@/assets/images/goals/goal-lump-sum.png'
import goalHousingIcon from '@/assets/images/goals/goals_2.png'
import goalMarriageIcon from '@/assets/images/goals/goals_4.png'
import goalCustomIcon from '@/assets/images/goals/goals_6.png'

type GoalPlan = {
  id: string
  name: string
  amount: number
  targetDate: string
}

type SavingsAccount = {
  id: string
  name: string
  number: string
  balance: number
  rate: string
  maturity: string
}

defineProps<{
  plan: GoalPlan
  selectedSavingsIds: string[]
  goalNumber: number
  goalCount: number
}>()

const emit = defineEmits<{
  toggle: [savingsId: string]
}>()

const goalIcons: Record<string, string> = {
  education: goalEducationIcon,
  'lump-sum': goalLumpSumIcon,
  housing: goalHousingIcon,
  marriage: goalMarriageIcon,
  custom: goalCustomIcon,
}

const goalCardStyles: Record<string, string> = {
  education: 'border-[#cfe4ff] bg-[#f2f8ff]',
  'lump-sum': 'border-[#f7dfa1] bg-[#fff9e8]',
  housing: 'border-[#cdebd7] bg-[#f1fbf5]',
  marriage: 'border-[#f5d2df] bg-[#fff4f8]',
  custom: 'border-[#ddd8f5] bg-[#f7f5ff]',
}

const savingsAccounts: SavingsAccount[] = [
  {
    id: 'child-love-1',
    name: 'KB 아이사랑적금',
    number: '952-17362605-43',
    balance: 12_450_000,
    rate: '연 3.80%',
    maturity: '2027.08.12',
  },
  {
    id: 'young-youth',
    name: 'KB Young Youth 적금',
    number: '952-17362605-57',
    balance: 3_200_000,
    rate: '연 3.50%',
    maturity: '2028.03.20',
  },
  {
    id: 'child-love-2',
    name: 'KB 아이사랑적금 2',
    number: '952-17362605-68',
    balance: 1_520_000,
    rate: '연 3.40%',
    maturity: '2027.11.05',
  },
]
</script>

<template>
  <section class="pb-2">
    <div class="flex items-end justify-between gap-4">
      <div>
        <p class="text-xs font-semibold text-[var(--color-selected-text)]">
          목표 {{ goalNumber }} / {{ goalCount }}
        </p>
        <h1 class="mt-1 break-keep text-[26px] leading-[1.35] font-bold tracking-[-0.04em]">
          어떤 적금으로<br />목표를 준비할까요?
        </h1>
      </div>
    </div>
    <p class="mt-2 text-sm leading-6 text-[var(--color-text-secondary)]">
      목표에 연결할 적금을 선택해주세요. 여러 개를 함께 연결할 수 있어요.
    </p>

    <article
      class="mt-6 flex items-center gap-4 rounded-[20px] border p-4 transition-colors"
      :class="goalCardStyles[plan.id] ?? 'border-[#dce8ee] bg-[#f8fcfe]'"
      aria-label="현재 연결할 목표"
    >
      <span class="grid size-14 shrink-0 place-items-center">
        <img class="size-10 object-contain" :src="goalIcons[plan.id]" alt="" />
      </span>
      <span class="min-w-0 flex-1">
        <span class="block text-xs font-medium text-[var(--color-text-secondary)]">선택한 목표</span>
        <strong class="mt-0.5 block truncate text-lg">{{ plan.name }}</strong>
        <span class="mt-1 block text-sm text-[var(--color-text-secondary)]">
          목표 {{ plan.amount.toLocaleString('ko-KR') }}원
        </span>
      </span>
    </article>

    <div class="mt-7 flex items-center justify-between">
      <h2 class="text-base font-bold">연결 가능한 적금</h2>
      <span class="text-xs text-[var(--color-text-secondary)]">
        {{ selectedSavingsIds.length > 0 ? `${selectedSavingsIds.length}개 선택` : `총 ${savingsAccounts.length}개` }}
      </span>
    </div>

    <div class="mt-3 grid gap-3">
      <button
        v-for="saving in savingsAccounts"
        :key="saving.id"
        class="relative w-full rounded-[20px] border p-4 pr-14 text-left transition-[border-color,background-color,transform] active:scale-[0.99]"
        :class="
          selectedSavingsIds.includes(saving.id)
            ? 'border-[var(--color-brand-primary)] bg-[var(--color-selected-background)]'
            : 'border-[var(--color-border)] bg-white'
        "
        type="button"
        :aria-pressed="selectedSavingsIds.includes(saving.id)"
        @click="emit('toggle', saving.id)"
      >
        <strong class="block text-base">{{ saving.name }}</strong>
        <span class="mt-1 block text-xs text-[var(--color-text-secondary)]">
          {{ saving.number }}
        </span>
        <span class="mt-3 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs">
          <strong class="text-[var(--color-selected-text)]">
            {{ saving.balance.toLocaleString('ko-KR') }}원
          </strong>
          <span class="text-[var(--color-text-secondary)]">{{ saving.rate }}</span>
          <span class="text-[var(--color-text-secondary)]">만기 {{ saving.maturity }}</span>
        </span>
        <span
          class="absolute top-1/2 right-4 grid size-6 -translate-y-1/2 place-items-center rounded-full border transition-colors"
          :class="
            selectedSavingsIds.includes(saving.id)
              ? 'border-[var(--color-brand-primary)] bg-[var(--color-brand-primary)] text-white'
              : 'border-[var(--color-border)] text-transparent'
          "
          aria-hidden="true"
        >
          <Check :size="15" :stroke-width="3" />
        </span>
      </button>
    </div>
  </section>
</template>
