<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import AppSubHeader from '@/components/layout/AppSubHeader.vue'
import AiRecommendationModal from '@/components/goals/AiRecommendationModal.vue'
import GoalAmountStep from '@/components/goals/GoalAmountStep.vue'
import GoalPlanStep from '@/components/goals/GoalPlanStep.vue'
import GoalSelectionStep from '@/components/goals/GoalSelectionStep.vue'
import GoalSavingsLinkStep from '@/components/goals/GoalSavingsLinkStep.vue'
import GoalSetupSummaryStep from '@/components/goals/GoalSetupSummaryStep.vue'

type GoalSetting = { amount: number; targetDate: string }

const router = useRouter()
const currentStep = ref(1)
const currentGoalIndex = ref(0)
const selectedGoals = ref<string[]>([])
const customGoal = ref('')
const isRecommendationOpen = ref(false)
const goalSettings = reactive<Record<string, GoalSetting>>({})
const linkedSavings = reactive<Record<string, string[]>>({})
const currentLinkGoalIndex = ref(0)
const slideDirection = ref<'forward' | 'backward'>('forward')
const today = new Date()
const defaultTargetDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`
const goalDisplayOrder = ['education', 'housing', 'marriage', 'lump-sum', 'custom']

const goalNames: Record<string, string> = {
  education: '대학자금',
  'lump-sum': '목돈 마련',
  housing: '주거자금',
  marriage: '결혼자금',
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
  if (currentStep.value === 3) return '적금 연결하기'
  if (currentStep.value === 5) return '목표 관리로 이동'
  if (currentStep.value === 4) {
    return currentLinkGoalIndex.value < selectedGoals.value.length - 1 ? '다음 목표' : '연결 완료'
  }
  if (currentStep.value !== 2) return '다음'
  return remainingGoalCount.value > 0 ? '다음 목표' : '목표 확인하기'
})
const currentSetting = computed(() =>
  currentGoalId.value ? goalSettings[currentGoalId.value] : undefined,
)
const currentLinkPlan = computed(() => plans.value[currentLinkGoalIndex.value])
const unavailableSavingsIds = computed(() => {
  const currentGoalId = currentLinkPlan.value?.id
  return Object.entries(linkedSavings)
    .filter(([goalId]) => goalId !== currentGoalId)
    .flatMap(([, savingsIds]) => savingsIds)
})
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
    targetDate: goalSettings[id]?.targetDate ?? defaultTargetDate,
  })),
)

const ensureSetting = (goalId: string) => {
  if (!goalSettings[goalId]) {
    goalSettings[goalId] = { amount: 30_000_000, targetDate: defaultTargetDate }
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
  selectedGoals.value.sort(
    (a, b) => goalDisplayOrder.indexOf(a) - goalDisplayOrder.indexOf(b),
  )
  ensureSetting(goalId)
}

const goBack = () => {
  slideDirection.value = 'backward'
  if (currentStep.value === 5) {
    currentStep.value = 4
    currentLinkGoalIndex.value = selectedGoals.value.length - 1
  } else if (currentStep.value === 4 && currentLinkGoalIndex.value > 0) {
    currentLinkGoalIndex.value -= 1
  } else if (currentStep.value === 4) {
    currentStep.value = 3
  } else if (currentStep.value === 3) {
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
  } else if (currentStep.value === 2) {
    currentStep.value = 3
  } else if (currentStep.value === 3) {
    currentLinkGoalIndex.value = 0
    currentStep.value = 4
  } else if (currentStep.value === 4 && currentLinkGoalIndex.value < selectedGoals.value.length - 1) {
    currentLinkGoalIndex.value += 1
  } else if (currentStep.value === 4) {
    currentStep.value = 5
  } else if (currentStep.value === 5) {
    router.push({ name: 'MypageGoals' })
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

const toggleLinkedSaving = (goalId: string, savingsId: string) => {
  const selected = linkedSavings[goalId] ?? (linkedSavings[goalId] = [])
  const index = selected.indexOf(savingsId)
  if (index >= 0) selected.splice(index, 1)
  else selected.push(savingsId)
}
</script>

<template>
  <main
    class="flex h-dvh flex-col overflow-hidden bg-[var(--color-surface)] text-[var(--color-text-primary)]"
  >
    <AppSubHeader title="목표" :fixed="false" back-label="이전" @back="goBack" />

    <div v-if="currentStep < 3" class="flex shrink-0 gap-2 px-6 pt-7" aria-hidden="true">
      <span
        v-for="step in progressTotal"
        :key="step"
        class="h-1 flex-1 rounded-full transition-colors duration-200"
        :class="
          step <= progressStep ? 'bg-[var(--color-brand-primary)]' : 'bg-[var(--color-border)]'
        "
      ></span>
    </div>

    <div class="min-h-0 flex-1 overflow-x-hidden overflow-y-auto overscroll-contain px-6 pt-5 pb-8">
      <Transition :name="`goal-slide-${slideDirection}`" mode="out-in">
        <div
          :key="`${currentStep}-${currentStep === 4 ? currentLinkGoalIndex : currentGoalIndex}`"
          class="mx-auto w-full max-w-[520px]"
          :class="currentStep === 5 ? 'flex min-h-full items-center' : ''"
        >
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

          <GoalPlanStep v-else-if="currentStep === 3" :plans="plans" />
          <GoalSavingsLinkStep
            v-else-if="currentStep === 4 && currentLinkPlan"
            :plan="currentLinkPlan"
            :goal-number="currentLinkGoalIndex + 1"
            :goal-count="plans.length"
            :selected-savings-ids="linkedSavings[currentLinkPlan.id] ?? []"
            :unavailable-savings-ids="unavailableSavingsIds"
            @toggle="toggleLinkedSaving(currentLinkPlan.id, $event)"
          />
          <GoalSetupSummaryStep
            v-else-if="currentStep === 5"
            :plans="plans"
            :linked-savings="linkedSavings"
          />
        </div>
      </Transition>
    </div>

    <footer
      class="relative shrink-0 bg-[var(--color-surface)]/98 px-6 pt-3 pb-[max(24px,env(safe-area-inset-bottom))] backdrop-blur"
      :class="currentStep === 1 ? 'grid grid-cols-1' : 'grid grid-cols-2 gap-4'"
    >
      <button
        v-if="currentStep > 1"
        class="relative z-1 h-14 min-h-14 max-h-14 self-start rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] font-bold text-[var(--color-text-secondary)] transition-colors active:bg-[var(--color-surface-muted)]"
        type="button"
        @click="goBack"
      >
        이전
      </button>

      <button
        class="group relative z-1 flex h-14 min-h-14 max-h-14 items-center justify-center gap-1.5 self-start rounded-2xl bg-[var(--color-brand-primary)] font-bold text-[var(--color-text-inverse)] transition-[transform,background-color] active:scale-[0.985] active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[var(--color-disabled-background)] disabled:text-[var(--color-unselected-text)]"
        type="button"
        :disabled="!canContinue"
        @click="goNext"
      >
        <span>{{ nextButtonLabel }}</span>
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
  transition:
    transform 150ms cubic-bezier(0.25, 0.8, 0.25, 1),
    opacity 120ms ease-out;
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
