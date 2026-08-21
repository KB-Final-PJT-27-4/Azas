<script setup lang="ts">
import { ArrowRight, Check } from 'lucide-vue-next'

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

withDefaults(
  defineProps<{
    plan: GoalPlan
    selectedSavingsIds: string[]
    unavailableSavingsIds: string[]
    savingsAccounts?: SavingsAccount[]
    goalNumber: number
    goalCount: number
    embedded?: boolean
    loading?: boolean
  }>(),
  { embedded: false, loading: false, savingsAccounts: () => [] },
)

const emit = defineEmits<{
  toggle: [savingsId: string]
  recommend: []
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

</script>

<template>
  <section :class="embedded ? '' : 'pb-2'">
    <div v-if="!embedded" class="flex items-end justify-between gap-4">
      <div>
        <p class="text-xs font-semibold text-[var(--color-selected-text)]">
          목표 {{ goalNumber }} / {{ goalCount }}
        </p>
        <h1 class="mt-1 break-keep text-[26px] leading-[1.35] font-bold tracking-[-0.04em]">
          어떤 적금으로<br />목표를 준비할까요?
        </h1>
      </div>
    </div>
    <p v-if="!embedded" class="mt-2 text-sm leading-6 text-[var(--color-text-secondary)]">
      연결할 적금을 선택해보세요. 지금 연결하지 않고 넘어가도 괜찮아요.
    </p>

    <article
      v-if="!embedded"
      class="mt-6 flex items-center gap-4 rounded-[20px] border p-4 transition-colors"
      :class="goalCardStyles[plan.id] ?? 'border-[#dce8ee] bg-[#f8fcfe]'"
      aria-label="현재 연결할 목표"
    >
      <span class="grid size-14 shrink-0 place-items-center">
        <img class="size-10 object-contain" :src="goalIcons[plan.id]" alt="" />
      </span>
      <span class="min-w-0 flex-1">
        <strong class="mt-0.5 block truncate text-lg">{{ plan.name }}</strong>
        <span class="mt-1 block text-sm text-[var(--color-text-secondary)]">
          목표 {{ plan.amount.toLocaleString('ko-KR') }}원
        </span>
      </span>
    </article>

    <div :class="embedded ? 'flex items-center justify-between' : 'mt-7 flex items-center justify-between'">
      <h2 :class="embedded ? 'text-lg font-extrabold tracking-[-0.02em]' : 'text-base font-bold'">
        연결할 적금
      </h2>
      <span v-if="!loading" class="text-xs text-[var(--color-text-secondary)]">
        {{ selectedSavingsIds.length > 0 ? `${selectedSavingsIds.length}개 선택` : '선택 안 함' }}
      </span>
    </div>

    <div
      v-if="loading"
      class="mt-4 grid gap-2.5"
      aria-label="연결 가능한 적금 불러오는 중"
      aria-busy="true"
    >
      <div
        v-for="index in 2"
        :key="index"
        class="h-[92px] animate-pulse rounded-2xl bg-[#edf1f3]"
        aria-hidden="true"
      ></div>
    </div>

    <div
      v-else-if="savingsAccounts.length === 0"
      class="mt-4 rounded-[20px] border border-[#d7e9f2] bg-[#f3faff] px-5 py-6 text-center"
    >
      <strong class="mt-4 block text-[17px] font-bold tracking-[-0.02em]">
        연결할 적금이 아직 없어요
      </strong>
      <p class="mt-2 break-keep text-[13px] leading-5 text-[var(--color-text-secondary)]">
        아이에게 맞는 적금을 추천받고<br />목표를 차근차근 준비해보세요.
      </p>
      <button
        class="mt-4 inline-flex items-center justify-center gap-1 border-0 bg-transparent p-1 text-[14px] font-bold text-[var(--color-selected-text)] underline decoration-1 underline-offset-4 transition-opacity active:opacity-60"
        type="button"
        @click="emit('recommend')"
      >
        추천 적금 보러가기
        <ArrowRight :size="17" :stroke-width="2.5" aria-hidden="true" />
      </button>
    </div>

    <div v-else :class="embedded ? 'mt-4 grid gap-2.5' : 'mt-3 grid gap-3'">
      <button
        v-for="saving in savingsAccounts"
        :key="saving.id"
        class="relative w-full border pr-14 text-left transition-[border-color,background-color,transform,box-shadow] active:scale-[0.99]"
        :class="[
          embedded ? 'rounded-2xl px-4 py-3.5' : 'rounded-[20px] p-4',
          selectedSavingsIds.includes(saving.id)
            ? 'border-[var(--color-brand-primary)] bg-[#eaf8ff] shadow-[0_0_0_1px_rgb(82_188_235_/_8%)]'
            : unavailableSavingsIds.includes(saving.id)
              ? 'cursor-not-allowed border-[#d9e0e6] bg-[#f5f6f7] opacity-55'
              : 'border-[#d7e0e5] bg-white',
        ]"
        type="button"
        :disabled="unavailableSavingsIds.includes(saving.id)"
        :aria-pressed="selectedSavingsIds.includes(saving.id)"
        :aria-label="
          unavailableSavingsIds.includes(saving.id)
            ? `${saving.name}, 다른 목표에 연결된 적금`
            : saving.name
        "
        @click="emit('toggle', saving.id)"
      >
        <strong class="block text-base">{{ saving.name }}</strong>
        <span class="mt-1 block text-xs text-[var(--color-text-secondary)]">
          {{ saving.number }}
        </span>
        <span :class="embedded ? 'mt-2 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs' : 'mt-3 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs'">
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
              : 'border-[#cbd5df] text-transparent'
          "
          aria-hidden="true"
        >
          <Check :size="15" :stroke-width="3" />
        </span>
        <span
          v-if="unavailableSavingsIds.includes(saving.id)"
          class="absolute top-3 right-4 text-[11px] font-semibold text-[var(--color-text-secondary)]"
        >
          연결됨
        </span>
      </button>
    </div>
  </section>
</template>
