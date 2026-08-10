<script setup lang="ts">
import { ChevronLeft } from 'lucide-vue-next'
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import AiRecommendationModal from '@/components/goals/AiRecommendationModal.vue'
import GoalAmountStep from '@/components/goals/GoalAmountStep.vue'
import GoalPlanStep from '@/components/goals/GoalPlanStep.vue'
import GoalSelectionStep from '@/components/goals/GoalSelectionStep.vue'

type GoalSetting = { amount: number; targetDate: string }

const router = useRouter()
const currentStep = ref(1)
const currentGoalIndex = ref(0)
const selectedGoals = ref<string[]>([])
const customGoal = ref('')
const isRecommendationOpen = ref(false)
const goalSettings = reactive<Record<string, GoalSetting>>({})
const slideDirection = ref<'forward' | 'backward'>('forward')

const goalNames: Record<string, string> = {
  education: '대학자금',
  independence: '독립자금',
  housing: '주거자금',
  marriage: '결혼자금',
  investment: '투자자금',
}

const getGoalName = (goalId: string) =>
  goalId === 'custom' ? customGoal.value.trim() || '직접 설정' : (goalNames[goalId] ?? '목표')

const currentGoalId = computed(() => selectedGoals.value[currentGoalIndex.value] ?? '')
const currentGoalName = computed(() => getGoalName(currentGoalId.value))
const currentGoalNumber = computed(() => currentGoalIndex.value + 1)
const remainingGoalCount = computed(() =>
  Math.max(selectedGoals.value.length - currentGoalNumber.value, 0),
)
const nextButtonLabel = computed(() => {
  if (currentStep.value === 3) return '시작하기'
  if (currentStep.value !== 2) return '다음'
  return remainingGoalCount.value > 0 ? '다음 목표' : '목표 확인하기'
})
const currentSetting = computed(() =>
  currentGoalId.value ? goalSettings[currentGoalId.value] : undefined,
)
const canContinue = computed(
  () =>
    selectedGoals.value.length > 0 &&
    (!selectedGoals.value.includes('custom') || customGoal.value.trim().length > 0),
)
const progressStep = computed(() =>
  currentStep.value === 1
    ? 1
    : currentStep.value === 2
      ? currentGoalIndex.value + 2
      : selectedGoals.value.length + 1,
)
const progressTotal = computed(() => selectedGoals.value.length + 1)
const plans = computed(() =>
  selectedGoals.value.map((id) => ({
    id,
    name: getGoalName(id),
    amount: goalSettings[id]?.amount ?? 30_000_000,
    targetDate: goalSettings[id]?.targetDate ?? '2045-03',
  })),
)

const ensureSetting = (goalId: string) => {
  if (!goalSettings[goalId]) {
    goalSettings[goalId] = { amount: 30_000_000, targetDate: '2045-03' }
  }
}

const toggleGoal = (goalId: string) => {
  const index = selectedGoals.value.indexOf(goalId)
  if (index >= 0) {
    selectedGoals.value.splice(index, 1)
    if (goalId === 'custom') customGoal.value = ''
    return
  }
  selectedGoals.value.push(goalId)
  ensureSetting(goalId)
}

const goBack = () => {
  slideDirection.value = 'backward'
  if (currentStep.value === 3) {
    currentStep.value = 2
    currentGoalIndex.value = selectedGoals.value.length - 1
  } else if (currentStep.value === 2 && currentGoalIndex.value > 0) {
    currentGoalIndex.value -= 1
  } else if (currentStep.value === 2) {
    currentStep.value = 1
  } else {
    router.back()
  }
}

const goNext = () => {
  if (!canContinue.value) return
  slideDirection.value = 'forward'
  if (currentStep.value === 1) {
    selectedGoals.value.forEach(ensureSetting)
    currentGoalIndex.value = 0
    currentStep.value = 2
  } else if (currentGoalIndex.value < selectedGoals.value.length - 1) {
    currentGoalIndex.value += 1
  } else {
    currentStep.value = 3
  }
}

const updateAmount = (value: number) => {
  if (currentSetting.value) currentSetting.value.amount = value
}
const updateTargetDate = (value: string) => {
  if (currentSetting.value) currentSetting.value.targetDate = value
}
const selectRecommendation = (value: number) => {
  updateAmount(value)
}
</script>

<template>
  <main class="flex min-h-dvh flex-col bg-[var(--color-surface)] text-[var(--color-text-primary)]">
    <header
      class="relative flex h-16 items-center justify-center border-b border-[var(--color-border)]"
    >
      <button
        class="absolute left-4 grid size-10 place-items-center text-[var(--color-text-secondary)]"
        type="button"
        aria-label="이전"
        @click="goBack"
      >
        <ChevronLeft :size="28" />
      </button>
      <strong>목표</strong>
    </header>

    <div v-if="currentStep < 3" class="flex gap-2 px-6 pt-7" aria-hidden="true">
      <span
        v-for="step in progressTotal"
        :key="step"
        class="h-1 flex-1 rounded-full"
        :class="
          step <= progressStep ? 'bg-[var(--color-brand-primary)]' : 'bg-[var(--color-border)]'
        "
      ></span>
    </div>

    <div class="flex-1 overflow-x-hidden px-6 pt-7 pb-6">
      <Transition :name="`goal-slide-${slideDirection}`" mode="out-in">
        <div :key="`${currentStep}-${currentGoalIndex}`">
          <GoalSelectionStep
            v-if="currentStep === 1"
            :selected-goals="selectedGoals"
            :custom-goal="customGoal"
            @toggle="toggleGoal"
            @update:custom-goal="customGoal = $event"
          />

          <GoalAmountStep
            v-else-if="currentStep === 2 && currentSetting"
            :goal-name="currentGoalName"
            :goal-number="currentGoalNumber"
            :amount="currentSetting.amount"
            :target-date="currentSetting.targetDate"
            @update:amount="updateAmount"
            @update:target-date="updateTargetDate"
            @open-recommendation="isRecommendationOpen = true"
          />

          <GoalPlanStep v-else :plans="plans" />
        </div>
      </Transition>
    </div>

    <footer
      class="bg-[var(--color-surface)] px-6 pt-3 pb-8"
      :class="currentStep === 1 ? 'grid grid-cols-1' : 'grid grid-cols-2 gap-4'"
    >
      <button
        v-if="currentStep > 1"
        class="relative z-1 h-14 min-h-14 max-h-14 self-start rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] font-bold text-[var(--color-selected-text)]"
        type="button"
        @click="goBack"
      >
        이전
      </button>

      <button
        class="relative z-1 h-14 min-h-14 max-h-14 self-start rounded-xl bg-[var(--color-brand-primary)] font-bold text-[var(--color-text-inverse)] disabled:cursor-not-allowed disabled:bg-[var(--color-disabled-background)] disabled:text-[var(--color-unselected-text)]"
        type="button"
        :disabled="!canContinue"
        @click="currentStep === 3 ? router.push({ name: 'Home' }) : goNext()"
      >
        {{ nextButtonLabel }}
      </button>
    </footer>

    <AiRecommendationModal
      v-if="isRecommendationOpen"
      :selected-amount="currentSetting?.amount"
      @close="isRecommendationOpen = false"
      @select="selectRecommendation"
    />
  </main>
</template>

<style scoped>
.goal-slide-forward-enter-active,
.goal-slide-forward-leave-active,
.goal-slide-backward-enter-active,
.goal-slide-backward-leave-active {
  transition: transform 150ms cubic-bezier(0.25, 0.8, 0.25, 1), opacity 120ms ease-out;
}

.goal-slide-forward-enter-from,
.goal-slide-backward-leave-to {
  transform: translateX(18px);
  opacity: 0;
}

.goal-slide-forward-leave-to,
.goal-slide-backward-enter-from {
  transform: translateX(-18px);
  opacity: 0;
}

@media (prefers-reduced-motion: reduce) {
  .goal-slide-forward-enter-active,
  .goal-slide-forward-leave-active,
  .goal-slide-backward-enter-active,
  .goal-slide-backward-leave-active {
    transition-duration: 1ms;
  }
}
</style>
