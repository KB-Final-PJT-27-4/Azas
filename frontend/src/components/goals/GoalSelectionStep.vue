<script setup lang="ts">
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
    description: '목표를 직접 입력해주세요',
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

    <label v-if="selectedGoals.includes('custom')" class="mt-4 grid gap-2">
      <span class="text-sm font-bold">목표 이름</span>
      <input
        class="h-14 rounded-xl border border-[var(--color-border)] px-4 outline-none focus:border-[var(--color-brand-primary-pressed)] focus:ring-2 focus:ring-[var(--color-selected-background)]"
        type="text"
        maxlength="20"
        placeholder="예: 세계 여행 자금"
        :value="customGoal"
        @input="emit('update:customGoal', ($event.target as HTMLInputElement).value)"
      />
    </label>
  </section>
</template>
