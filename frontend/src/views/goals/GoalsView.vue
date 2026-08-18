<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import AppSubHeader from '@/components/layout/AppSubHeader.vue'
import AiRecommendationModal from '@/components/goals/AiRecommendationModal.vue'
import GoalAmountStep from '@/components/goals/GoalAmountStep.vue'
import GoalSelectionStep from '@/components/goals/GoalSelectionStep.vue'
import GoalSavingsLinkStep from '@/components/goals/GoalSavingsLinkStep.vue'
import GoalSetupSummaryStep from '@/components/goals/GoalSetupSummaryStep.vue'

type GoalSetting = { amount: number; targetDate: string }

const router = useRouter()
const currentStep = ref<'setup' | 'summary'>('setup')
const selectedGoals = ref<string[]>([])
const customGoal = ref('')
const setting = reactive<GoalSetting>({ amount: 0, targetDate: '' })
const linkedSavings = reactive<Record<string, string[]>>({})
const isRecommendationOpen = ref(false)
const slideDirection = ref<'forward' | 'backward'>('forward')

const goalNames: Record<string, string> = {
  education: '대학자금',
  'lump-sum': '목돈 마련',
  housing: '주거자금',
  marriage: '결혼자금',
}

const currentGoalId = computed(() => selectedGoals.value[0] ?? '')
const currentGoalName = computed(() =>
  currentGoalId.value === 'custom'
    ? customGoal.value.trim() || '직접 설정'
    : (goalNames[currentGoalId.value] ?? '목표'),
)
const hasGoalName = computed(
  () =>
    Boolean(currentGoalId.value) &&
    (currentGoalId.value !== 'custom' || Boolean(customGoal.value.trim())),
)
const canComplete = computed(
  () => hasGoalName.value && setting.amount > 0 && Boolean(setting.targetDate),
)
const currentPlan = computed(() =>
  currentGoalId.value
    ? {
        id: currentGoalId.value,
        name: currentGoalName.value,
        amount: setting.amount,
        targetDate: setting.targetDate,
      }
    : undefined,
)
const plans = computed(() => (currentPlan.value ? [currentPlan.value] : []))

const selectGoal = (goalId: string) => {
  if (currentGoalId.value === goalId) {
    selectedGoals.value = []
    customGoal.value = ''
  } else {
    selectedGoals.value = [goalId]
    if (goalId !== 'custom') customGoal.value = ''
  }

  setting.amount = 0
  setting.targetDate = ''
  Object.keys(linkedSavings).forEach((key) => delete linkedSavings[key])
}

const updateAmount = (value: number) => {
  setting.amount = value
  if (value <= 0) setting.targetDate = ''
}
const updateTargetDate = (value: string) => {
  setting.targetDate = value
}

const toggleLinkedSaving = (savingsId: string) => {
  if (!currentGoalId.value) return
  const selected = linkedSavings[currentGoalId.value] ?? (linkedSavings[currentGoalId.value] = [])
  const index = selected.indexOf(savingsId)
  if (index >= 0) selected.splice(index, 1)
  else selected.push(savingsId)
}

const goBack = () => {
  if (currentStep.value === 'summary') {
    slideDirection.value = 'backward'
    currentStep.value = 'setup'
    return
  }
  router.back()
}

const goNext = () => {
  if (currentStep.value === 'summary') {
    router.push({ name: 'MypageGoals' })
    return
  }
  if (!canComplete.value) return
  slideDirection.value = 'forward'
  currentStep.value = 'summary'
}
</script>

<template>
  <main class="flex h-dvh flex-col overflow-hidden bg-[var(--color-surface)] text-[var(--color-text-primary)]">
    <AppSubHeader title="목표" :fixed="false" back-label="이전" @back="goBack" />

    <div class="min-h-0 flex-1 overflow-x-hidden overflow-y-auto overscroll-contain px-6 pt-5 pb-8">
      <Transition :name="`goal-slide-${slideDirection}`" mode="out-in">
        <div
          :key="currentStep"
          class="mx-auto w-full max-w-[520px]"
          :class="currentStep === 'summary' ? 'flex min-h-full items-center' : ''"
        >
          <template v-if="currentStep === 'setup'">
            <GoalSelectionStep
              :selected-goals="selectedGoals"
              :custom-goal="customGoal"
              single-selection
              @toggle="selectGoal"
              @update:custom-goal="customGoal = $event"
            />

            <Transition name="progressive-section">
              <section v-if="hasGoalName" class="mt-4">
                <h2 class="text-xl font-extrabold tracking-[-0.025em]">얼마를 언제까지 모을까요?</h2>
                <GoalAmountStep
                  class="mt-4 rounded-[24px] border border-[#e9eef1] bg-[#fafbfc] p-5"
                  :goal-name="currentGoalName"
                  :amount="setting.amount"
                  :target-date="setting.targetDate"
                  :show-intro="false"
                  appearance="unified"
                  progressive
                  @update:amount="updateAmount"
                  @update:target-date="updateTargetDate"
                  @open-recommendation="isRecommendationOpen = true"
                />
              </section>
            </Transition>

            <Transition name="progressive-section">
              <section
                v-if="currentPlan && setting.targetDate"
                class="mt-5 rounded-[24px] border border-[#e9eef1] bg-[#fafbfc] p-5"
              >
                <GoalSavingsLinkStep
                  :plan="currentPlan"
                  :goal-number="1"
                  :goal-count="1"
                  :selected-savings-ids="linkedSavings[currentGoalId] ?? []"
                  :unavailable-savings-ids="[]"
                  embedded
                  @toggle="toggleLinkedSaving"
                />
              </section>
            </Transition>
          </template>

          <GoalSetupSummaryStep v-else :plans="plans" :linked-savings="linkedSavings" />
        </div>
      </Transition>
    </div>

    <footer class="shrink-0 bg-[var(--color-surface)]/98 px-6 pt-3 pb-[max(24px,env(safe-area-inset-bottom))] backdrop-blur">
      <button
        class="flex h-14 w-full items-center justify-center rounded-2xl bg-[var(--color-brand-primary)] font-bold text-[var(--color-text-inverse)] transition-[transform,background-color] active:scale-[0.985] active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[var(--color-disabled-background)] disabled:text-[var(--color-unselected-text)]"
        type="button"
        :disabled="currentStep === 'setup' && !canComplete"
        @click="goNext"
      >
        {{ currentStep === 'setup' ? '목표 설정 완료' : '목표 관리로 이동' }}
      </button>
    </footer>

    <AiRecommendationModal
      v-if="isRecommendationOpen"
      :selected-amount="setting.amount"
      @close="isRecommendationOpen = false"
      @select="updateAmount"
    />
  </main>
</template>

<style scoped>
.progressive-section-enter-active,
.progressive-section-leave-active {
  transition:
    transform 400ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 260ms ease,
    clip-path 400ms cubic-bezier(0.22, 1, 0.36, 1);
}
.progressive-section-enter-from {
  transform: translateY(16px);
  opacity: 0;
  clip-path: inset(0 0 100% 0 round 24px);
}
.progressive-section-leave-to { transform: translateY(-4px); opacity: 0; }

.goal-slide-forward-enter-active,
.goal-slide-forward-leave-active,
.goal-slide-backward-enter-active,
.goal-slide-backward-leave-active {
  transition: transform 150ms cubic-bezier(0.25, 0.8, 0.25, 1), opacity 120ms ease-out;
}
.goal-slide-forward-enter-from,
.goal-slide-backward-leave-to { transform: translateX(18px); opacity: 0; }
.goal-slide-forward-leave-to,
.goal-slide-backward-enter-from { transform: translateX(-18px); opacity: 0; }

@media (prefers-reduced-motion: reduce) {
  .progressive-section-enter-active,
  .progressive-section-leave-active,
  .goal-slide-forward-enter-active,
  .goal-slide-forward-leave-active,
  .goal-slide-backward-enter-active,
  .goal-slide-backward-leave-active { transition-duration: 1ms; }
}
</style>
