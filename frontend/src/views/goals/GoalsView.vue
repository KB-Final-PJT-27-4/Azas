<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import AppSubHeader from '@/components/layout/AppSubHeader.vue'
import AiRecommendationModal from '@/components/goals/AiRecommendationModal.vue'
import GoalAmountStep from '@/components/goals/GoalAmountStep.vue'
import GoalSelectionStep from '@/components/goals/GoalSelectionStep.vue'
import GoalSavingsLinkStep from '@/components/goals/GoalSavingsLinkStep.vue'
import GoalSetupSummaryStep from '@/components/goals/GoalSetupSummaryStep.vue'
import { api, getApiErrorMessage } from '@/api'
import { resolveCurrentChildId } from '@/api/context'
import { useToast } from '@/composables/useToast'
import { toFinancialGoalApiDate } from '@/utils/financialGoalDate'

type GoalSetting = { amount: number; targetDate: string }
type GoalSetupDraft = {
  childId: number | null
  selectedGoals: string[]
  customGoal: string
  amount: number
  targetDate: string
  linkedSavings: Record<string, string[]>
}

const GOAL_SETUP_DRAFT_KEY = 'azas_goal_setup_draft'

const router = useRouter()
const route = useRoute()
const { showToast } = useToast()
const childId = ref<number | null>(null)
const savingsAccounts = ref<Array<{ id: string; name: string; number: string; balance: number; rate: string; maturity: string }>>([])
const unavailableSavingsIds = ref<string[]>([])
const isSavingsLoading = ref(true)
const currentStep = ref<'setup' | 'summary'>('setup')
const selectedGoals = ref<string[]>([])
const customGoal = ref('')
const setting = reactive<GoalSetting>({ amount: 0, targetDate: '' })
const linkedSavings = reactive<Record<string, string[]>>({})
const isRecommendationOpen = ref(false)
const slideDirection = ref<'forward' | 'backward'>('forward')
const goalTemplateIds = ref<Record<string, number>>({})
const goalTemplateOrder = ref<string[]>([])
const isGoalTemplatesLoading = ref(true)

const readGoalSetupDraft = (): GoalSetupDraft | null => {
  try {
    const storedDraft = sessionStorage.getItem(GOAL_SETUP_DRAFT_KEY)
    if (!storedDraft) return null
    return JSON.parse(storedDraft) as GoalSetupDraft
  } catch {
    sessionStorage.removeItem(GOAL_SETUP_DRAFT_KEY)
    return null
  }
}

const restoreGoalSetupDraft = (draft: GoalSetupDraft) => {
  selectedGoals.value = Array.isArray(draft.selectedGoals) ? draft.selectedGoals : []
  customGoal.value = typeof draft.customGoal === 'string' ? draft.customGoal : ''
  setting.amount = Number.isFinite(draft.amount) ? draft.amount : 0
  setting.targetDate = typeof draft.targetDate === 'string' ? draft.targetDate : ''
  Object.entries(draft.linkedSavings ?? {}).forEach(([goalId, accountIds]) => {
    linkedSavings[goalId] = Array.isArray(accountIds) ? accountIds : []
  })
}

const clearGoalSetupDraft = () => sessionStorage.removeItem(GOAL_SETUP_DRAFT_KEY)

const goalTemplateNameByGoalId: Record<string, string> = {
  education: '대학자금',
  housing: '주거자금',
  marriage: '결혼자금',
  'lump-sum': '목돈 마련',
}

const goalIdByTemplateName = Object.fromEntries(
  Object.entries(goalTemplateNameByGoalId).map(([goalId, templateName]) => [templateName, goalId]),
) as Record<string, string>

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
const currentGoalTemplateId = computed(() => goalTemplateIds.value[currentGoalId.value] ?? null)
const hasGoalName = computed(
  () =>
    Boolean(currentGoalId.value) &&
    (currentGoalId.value !== 'custom' || Boolean(customGoal.value.trim())),
)
const canComplete = computed(
  () => hasGoalName.value && setting.amount > 0 && Boolean(setting.targetDate),
)
const hasLinkedSavings = computed(
  () => (linkedSavings[currentGoalId.value] ?? []).length > 0,
)
const canSubmitGoal = computed(() => canComplete.value && hasLinkedSavings.value)
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
  if (!currentGoalId.value || unavailableSavingsIds.value.includes(savingsId)) return
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

const goToSavingsRecommendation = () => {
  const draft: GoalSetupDraft = {
    childId: childId.value,
    selectedGoals: [...selectedGoals.value],
    customGoal: customGoal.value,
    amount: setting.amount,
    targetDate: setting.targetDate,
    linkedSavings: Object.fromEntries(
      Object.entries(linkedSavings).map(([goalId, accountIds]) => [goalId, [...accountIds]]),
    ),
  }
  sessionStorage.setItem(GOAL_SETUP_DRAFT_KEY, JSON.stringify(draft))
  router.push({ name: 'SavingsRecommendation', query: { from: 'goal-setup' } })
}

const goNext = async () => {
  if (currentStep.value === 'summary') {
    if (!childId.value || !canSubmitGoal.value) {
      showToast('목표에 연결할 적금을 하나 이상 선택해 주세요.', 'error')
      return
    }
    try {
      await api.createGoalUsingPOST(childId.value, {
        target_amount: setting.amount,
        target_date: toFinancialGoalApiDate(setting.targetDate),
        account_ids: (linkedSavings[currentGoalId.value] ?? []).map(Number).filter(Number.isFinite),
        ...(currentGoalTemplateId.value
          ? { financial_goal_template_id: currentGoalTemplateId.value }
          : { title: currentGoalName.value }),
      })
      clearGoalSetupDraft()
      showToast('목표를 만들었어요.', 'success')
      await router.push({ name: 'MypageGoals' })
    } catch (error) {
      showToast(getApiErrorMessage(error, '목표를 만들지 못했습니다.'), 'error')
    }
    return
  }
  if (!canSubmitGoal.value) {
    if (canComplete.value) {
      showToast('목표에 연결할 적금을 하나 이상 선택해 주세요.', 'error')
    }
    return
  }
  slideDirection.value = 'forward'
  currentStep.value = 'summary'
}

onMounted(async () => {
  try {
    childId.value = await resolveCurrentChildId()
    const savedDraft = readGoalSetupDraft()
    const shouldResumeGoalSetup = route.query.resumeGoal === 'true'
    if (shouldResumeGoalSetup && savedDraft?.childId === childId.value) {
      restoreGoalSetupDraft(savedDraft)
      clearGoalSetupDraft()
    } else if (savedDraft) {
      clearGoalSetupDraft()
    }
    const [{ data: childAccounts }, { data: parentAccounts }, { data: goals }] = await Promise.all([
      api.getChildAccountsUsingGET(childId.value),
      api.getMyAccountsUsingGET(),
      api.getGoalsUsingGET(childId.value),
    ])
    try {
      const { data: templates } = await api.getTemplatesUsingGET()
      const orderedTemplates = [...(templates.templates ?? [])].sort(
        (left, right) =>
          (left.display_order ?? Number.MAX_SAFE_INTEGER)
          - (right.display_order ?? Number.MAX_SAFE_INTEGER),
      )
      const templatesByName = new Map(
        orderedTemplates
          .filter((template) => template.name && template.financial_goal_template_id != null)
          .map((template) => [template.name!, template.financial_goal_template_id!] as const),
      )
      goalTemplateOrder.value = orderedTemplates
        .map((template) => goalIdByTemplateName[template.name ?? ''])
        .filter((goalId): goalId is string => Boolean(goalId))
      goalTemplateIds.value = Object.fromEntries(
        Object.entries(goalTemplateNameByGoalId)
          .map(([goalId, templateName]) => [goalId, templatesByName.get(templateName)] as const)
          .filter((entry): entry is [string, number] => entry[1] != null),
      )
    } catch {
      goalTemplateIds.value = {}
      goalTemplateOrder.value = []
    } finally {
      isGoalTemplatesLoading.value = false
    }
    unavailableSavingsIds.value = [
      ...new Set(
        goals.financial_goals.flatMap((goal) =>
          (goal.linked_accounts ?? [])
            .map(({ account_id }) => account_id)
            .filter((accountId): accountId is number => accountId != null)
            .map(String),
        ),
      ),
    ]
    const allSavingsAccounts = [...childAccounts.accounts, ...parentAccounts.accounts]
    const seenAccountIds = new Set<number>()
    savingsAccounts.value = allSavingsAccounts
      .filter(({ account_product_type }) => account_product_type === 'SAVINGS')
      .filter(({ account_id }) => {
        if (seenAccountIds.has(account_id)) return false
        seenAccountIds.add(account_id)
        return true
      })
      .map((account) => ({
        id: String(account.account_id),
        name: account.account_name,
        number: account.account_number,
        balance: account.balance,
        rate: '',
        maturity: '',
      }))
  } catch (error) {
    showToast(getApiErrorMessage(error, '계좌 정보를 불러오지 못했습니다.'), 'error')
  } finally {
    isGoalTemplatesLoading.value = false
    isSavingsLoading.value = false
  }
})
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
              :goal-order="goalTemplateOrder"
              :is-loading="isGoalTemplatesLoading"
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
                  :has-recommendation="currentGoalTemplateId !== null"
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
                  :unavailable-savings-ids="unavailableSavingsIds"
                  :savings-accounts="savingsAccounts"
                  :loading="isSavingsLoading"
                  embedded
                  @toggle="toggleLinkedSaving"
                  @recommend="goToSavingsRecommendation"
                />
              </section>
            </Transition>
          </template>

          <GoalSetupSummaryStep
            v-else
            :plans="plans"
            :linked-savings="linkedSavings"
            :savings-accounts="savingsAccounts"
          />
        </div>
      </Transition>
    </div>

    <footer class="shrink-0 bg-[var(--color-surface)]/98 px-6 pt-3 pb-[max(24px,env(safe-area-inset-bottom))] backdrop-blur">
      <button
        class="flex h-14 w-full items-center justify-center rounded-2xl bg-[var(--color-brand-primary)] font-bold text-[var(--color-text-inverse)] transition-[transform,background-color] active:scale-[0.985] active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[var(--color-disabled-background)] disabled:text-[var(--color-unselected-text)]"
        type="button"
        :disabled="currentStep === 'setup' && !canSubmitGoal"
        @click="goNext"
      >
        {{ currentStep === 'setup' ? '목표 설정 완료' : '목표 관리로 이동' }}
      </button>
    </footer>

    <AiRecommendationModal
      v-if="isRecommendationOpen && currentGoalTemplateId !== null"
      :selected-amount="setting.amount"
      :financial-goal-template-id="currentGoalTemplateId"
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
