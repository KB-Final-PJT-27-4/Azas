<script setup lang="ts">
import { computed } from 'vue'
import { X } from 'lucide-vue-next'

import goalEducationIcon from '@/assets/images/goals/goals_1.png'
import goalLumpSumIcon from '@/assets/images/goals/goal-lump-sum.png'
import goalHousingIcon from '@/assets/images/goals/goals_2.png'
import goalMarriageIcon from '@/assets/images/goals/goals_4.png'
import goalCustomIcon from '@/assets/images/goals/goals_6.png'

type GoalOption = {
  id: string
  icon: string
  title: string
  description: string
}

const props = withDefaults(
  defineProps<{
    selectedGoals: string[]
    customGoal: string
    goalOrder?: string[]
    isLoading?: boolean
    singleSelection?: boolean
  }>(),
  { goalOrder: () => [], isLoading: false, singleSelection: false },
)

const emit = defineEmits<{
  toggle: [goalId: string]
  'update:customGoal': [value: string]
}>()

const customGoalSuggestions = [
  '생일 기념',
  '학교 입학',
  '가족 여행',
  '교육비 마련',
  '아이 미래 자금',
  '첫 독립 준비',
]

const goals: GoalOption[] = [
  {
    id: 'education',
    icon: goalEducationIcon,
    title: '대학자금',
    description: '대학 등록금과 교육비',
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
    id: 'lump-sum',
    icon: goalLumpSumIcon,
    title: '목돈 마련',
    description: '아이의 미래를 위한 든든한 목돈',
  },
  {
    id: 'custom',
    icon: goalCustomIcon,
    title: '직접 설정',
    description: '원하는 목표를 직접 입력해요',
  },
]

const orderedGoals = computed(() => {
  if (!props.goalOrder.length) return goals

  const orderByGoalId = new Map(props.goalOrder.map((goalId, index) => [goalId, index]))
  return [...goals].sort((left, right) => {
    if (left.id === 'custom') return 1
    if (right.id === 'custom') return -1

    const leftOrder = orderByGoalId.get(left.id) ?? Number.MAX_SAFE_INTEGER
    const rightOrder = orderByGoalId.get(right.id) ?? Number.MAX_SAFE_INTEGER
    return leftOrder - rightOrder
  })
})

const selectedGoal = computed(() => orderedGoals.value.find(({ id }) => id === props.selectedGoals[0]))

const selectedGoalCardStyle = computed(() => {
  const styles: Record<string, string> = {
    education: 'border-sky-300 bg-sky-50',
    housing: 'border-emerald-300 bg-emerald-50',
    marriage: 'border-pink-300 bg-pink-50',
    'lump-sum': 'border-amber-300 bg-amber-50',
    custom: 'border-violet-300 bg-violet-50',
  }

  return selectedGoal.value ? styles[selectedGoal.value.id] : ''
})
</script>

<template>
  <section>
    <div
      class="goal-intro overflow-hidden"
      :class="singleSelection && selectedGoal ? 'goal-intro--hidden' : ''"
      :aria-hidden="singleSelection && Boolean(selectedGoal)"
    >
      <div>
        <h1 class="text-[26px] font-bold tracking-[-0.04em]">어떤 목표를 준비할까요?</h1>
        <p class="mt-2 text-sm text-[var(--color-text-secondary)]">
          {{ singleSelection ? '가장 먼저 준비하고 싶은 목표 하나를 골라주세요.' : '여러 개를 선택하고 나중에 추가할 수도 있어요.' }}
        </p>
      </div>
    </div>

    <div
      class="goal-list"
      :class="singleSelection && selectedGoal ? 'goal-list--selected' : 'mt-7'"
    >
      <template v-if="isLoading">
        <div
          v-for="index in 5"
          :key="`goal-skeleton-${index}`"
          class="mb-3 flex min-h-[74px] animate-pulse items-center gap-3 rounded-2xl border border-[var(--color-border)] bg-white px-4 last:mb-0"
          aria-hidden="true"
        >
          <span class="size-11 shrink-0 rounded-xl bg-[#edf2f5]"></span>
          <span class="grid flex-1 gap-2">
            <span class="h-4 w-24 rounded-full bg-[#e8eef2]"></span>
            <span class="h-3 w-40 max-w-[70%] rounded-full bg-[#f0f3f5]"></span>
          </span>
        </div>
      </template>
      <button
        v-for="goal in isLoading ? [] : orderedGoals"
        :key="goal.id"
        class="goal-option mb-3 flex min-h-[74px] w-full items-center gap-3 overflow-hidden rounded-2xl border px-4 text-left last:mb-0"
        :class="[
          selectedGoals.includes(goal.id)
            ? selectedGoalCardStyle
            : 'border-[var(--color-border)] bg-[var(--color-surface)]',
          singleSelection && selectedGoal && !selectedGoals.includes(goal.id)
            ? 'goal-option--hidden'
            : '',
        ]"
        type="button"
        :aria-pressed="selectedGoals.includes(goal.id)"
        :tabindex="singleSelection && selectedGoal && !selectedGoals.includes(goal.id) ? -1 : 0"
        @click="emit('toggle', goal.id)"
      >
        <img class="size-11 object-contain" :src="goal.icon" alt="" />
        <span class="min-w-0 flex-1">
          <span
            v-if="singleSelection && selectedGoals.includes(goal.id)"
            class="block text-[11px] font-bold text-[var(--color-selected-text)]"
          >선택한 목표</span>
          <strong
            class="block text-base"
            :class="singleSelection && selectedGoals.includes(goal.id) ? 'mt-0.5' : ''"
          >{{ goal.title }}</strong>
          <span
            v-if="!singleSelection || !selectedGoals.includes(goal.id)"
            class="mt-1 block break-keep text-sm leading-5 text-[var(--color-text-secondary)]"
          >
            {{ goal.description }}
          </span>
        </span>
        <span
          v-if="selectedGoals.includes(goal.id) && !singleSelection"
          class="grid size-6 place-items-center rounded-full bg-[var(--color-brand-primary)] text-sm text-white"
          aria-hidden="true"
        >
          ✓
        </span>
        <span
          v-else-if="selectedGoals.includes(goal.id)"
          class="rounded-full bg-white px-3 py-1.5 text-xs font-bold text-[var(--color-text-secondary)]"
        >변경</span>
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

<style scoped>
.goal-intro {
  max-height: 88px;
  transition:
    max-height 360ms cubic-bezier(0.22, 1, 0.36, 1),
    transform 300ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 180ms ease;
}

.goal-intro--hidden {
  max-height: 0;
  transform: translateY(-8px);
  opacity: 0;
}

.goal-list {
  transition: margin-top 360ms cubic-bezier(0.22, 1, 0.36, 1);
}

.goal-list--selected {
  margin-top: 0;
}

.goal-option {
  max-height: 96px;
  transition:
    max-height 400ms cubic-bezier(0.22, 1, 0.36, 1),
    min-height 400ms cubic-bezier(0.22, 1, 0.36, 1),
    margin 400ms cubic-bezier(0.22, 1, 0.36, 1),
    padding 320ms cubic-bezier(0.22, 1, 0.36, 1),
    border-color 220ms ease,
    background-color 220ms ease,
    transform 360ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 170ms ease;
}

.goal-option--hidden {
  min-height: 0;
  max-height: 0;
  margin: 0;
  padding-top: 0;
  padding-bottom: 0;
  border-width: 0;
  transform: translateY(-8px) scale(0.985);
  opacity: 0;
  pointer-events: none;
}

@media (prefers-reduced-motion: reduce) {
  .goal-intro,
  .goal-list,
  .goal-option {
    transition-duration: 1ms;
  }
}
</style>
