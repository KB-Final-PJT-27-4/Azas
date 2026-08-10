<script setup lang="ts">
import { X } from 'lucide-vue-next'

import goalEducationIcon from '@/assets/images/goals/goals_1.png'
import goalIndependenceIcon from '@/assets/images/goals/goals_2.png'
import goalHousingIcon from '@/assets/images/goals/goals_3.png'
import goalMarriageIcon from '@/assets/images/goals/goals_4.png'
import goalInvestmentIcon from '@/assets/images/goals/goals_5.png'
import goalCustomIcon from '@/assets/images/goals/goals_6.png'

type GoalOption = {
  id: string
  icon: string
  title: string
  description: string
}

defineProps<{
  selectedGoals: string[]
  customGoal: string
}>()

const emit = defineEmits<{
  toggle: [goalId: string]
  'update:customGoal': [value: string]
}>()

const customGoalSuggestions = [
  '첫 걸음마 기념',
  '첫 생일 기념',
  '유치원 입학 기념',
  '초등학교 입학 기념',
  '첫 가족 여행',
  '자전거 선물',
]

const goals: GoalOption[] = [
  {
    id: 'education',
    icon: goalEducationIcon,
    title: '대학자금',
    description: '대학 등록금과 교육비',
  },
  {
    id: 'independence',
    icon: goalIndependenceIcon,
    title: '독립자금',
    description: '사회초년생을 위한 자금',
  },
  {
    id: 'housing',
    icon: goalHousingIcon,
    title: '주거자금',
    description: '내 집 마련을 위한 자금',
  },
  {
    id: 'marriage',
    icon: goalMarriageIcon,
    title: '결혼자금',
    description: '미래 자녀의 결혼을 위한 자금',
  },
  {
    id: 'investment',
    icon: goalInvestmentIcon,
    title: '투자자금',
    description: '미래 재테크를 위한 자금',
  },
  {
    id: 'custom',
    icon: goalCustomIcon,
    title: '직접 설정',
    description: '추천받거나 원하는 목표를 입력해요',
  },
]
</script>

<template>
  <section>
    <h1 class="text-[26px] font-bold tracking-[-0.04em]">어떤 목표를 준비할까요?</h1>
    <p class="mt-2 text-sm text-[var(--color-text-secondary)]">
      여러 개를 선택하고 나중에 추가할 수도 있어요.
    </p>

    <div class="mt-7 grid gap-3">
      <button
        v-for="goal in goals"
        :key="goal.id"
        class="flex min-h-[74px] items-center gap-4 rounded-2xl border px-5 text-left transition-colors"
        :class="
          selectedGoals.includes(goal.id)
            ? 'border-[var(--color-brand-primary)] bg-[var(--color-selected-background)]'
            : 'border-[var(--color-border)] bg-[var(--color-surface)]'
        "
        type="button"
        :aria-pressed="selectedGoals.includes(goal.id)"
        @click="emit('toggle', goal.id)"
      >
        <img class="size-11 object-contain" :src="goal.icon" alt="" />
        <span class="min-w-0 flex-1">
          <strong class="block text-base">{{ goal.title }}</strong>
          <span class="mt-1 block text-sm text-[var(--color-text-secondary)]">
            {{ goal.description }}
          </span>
        </span>
        <span
          v-if="selectedGoals.includes(goal.id)"
          class="grid size-6 place-items-center rounded-full bg-[var(--color-brand-primary)] text-sm text-white"
          aria-hidden="true"
        >
          ✓
        </span>
      </button>
    </div>

    <div v-if="selectedGoals.includes('custom')" class="mt-5 grid gap-5">
      <div class="grid gap-3">
        <legend class="text-sm font-bold">이런 목표는 어때요?</legend>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="suggestion in customGoalSuggestions"
            :key="suggestion"
            class="min-h-10 rounded-full border px-4 py-2 text-sm font-medium transition-colors"
            :class="
              customGoal === suggestion
                ? 'border-[var(--color-brand-primary)] bg-[var(--color-selected-background)] text-[var(--color-selected-text)]'
                : 'border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text-secondary)]'
            "
            type="button"
            :aria-pressed="customGoal === suggestion"
            @click="emit('update:customGoal', suggestion)"
          >
            {{ suggestion }}
          </button>
        </div>
      </div>

      <div class="grid gap-2">
        <label class="text-sm font-bold" for="custom-goal-name">직접 입력</label>
        <span class="relative">
          <input
            id="custom-goal-name"
            class="h-14 w-full rounded-xl border border-[var(--color-border)] px-4 pr-12 outline-none focus:border-[var(--color-brand-primary-pressed)] focus:ring-2 focus:ring-[var(--color-selected-background)]"
            type="text"
            maxlength="20"
            placeholder="예: 첫 피아노 발표회"
            :value="customGoal"
            @input="emit('update:customGoal', ($event.target as HTMLInputElement).value)"
          />
          <button
            v-if="customGoal"
            class="absolute top-1/2 right-2 grid size-10 -translate-y-1/2 place-items-center rounded-full text-[var(--color-text-secondary)]"
            type="button"
            aria-label="입력한 목표 지우기"
            @click="emit('update:customGoal', '')"
          >
            <X :size="20" aria-hidden="true" />
          </button>
        </span>
      </div>
    </div>
  </section>
</template>
