<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import goalCloudBackground from '@/assets/images/home/home-hero-bg.png'
import AiRecommendationModal from '@/components/goals/AiRecommendationModal.vue'
import GoalAmountStep from '@/components/goals/GoalAmountStep.vue'
import { useToast } from '@/composables/useToast'
import { api, getApiErrorMessage } from '@/api'
import { toFinancialGoalApiDate, toFinancialGoalMonth } from '@/utils/financialGoalDate'

const props = defineProps<{
  goalsId: string
}>()

const router = useRouter()
const { showToast } = useToast()
const isRecommendationOpen = ref(false)
const selectedRecommendationAmount = ref<number>()
let previousHtmlBackground = ''
let previousBodyBackground = ''

const initialGoal = {
  name: '나의 목표',
  amount: 0,
  targetDate: '',
}

const form = reactive({ ...initialGoal })
const isSubmitDisabled = computed(() => !form.name.trim() || form.amount <= 0 || !form.targetDate)

const selectRecommendation = (amount: number) => {
  form.amount = amount
  selectedRecommendationAmount.value = amount
}

const saveGoal = async () => {
  if (isSubmitDisabled.value) return

  try {
    await api.updateGoalUsingPATCH(Number(props.goalsId), {
      target_amount: form.amount,
      target_date: toFinancialGoalApiDate(form.targetDate),
    })
    showToast('목표를 수정했어요.', 'success')
    await router.replace({ name: 'MypageGoals' })
  } catch (error) {
    showToast(getApiErrorMessage(error, '목표를 수정하지 못했습니다.'), 'error')
  }
}

const cancelEdit = () => {
  router.replace({ name: 'MypageGoals' })
}

onMounted(async () => {
  previousHtmlBackground = document.documentElement.style.backgroundColor
  previousBodyBackground = document.body.style.backgroundColor
  document.documentElement.style.backgroundColor = '#eef9fe'
  document.body.style.backgroundColor = '#eef9fe'
  try {
    const { data } = await api.getGoalUsingGET(Number(props.goalsId))
    form.name = data.title ?? '나의 목표'
    form.amount = data.target_amount ?? 0
    form.targetDate = toFinancialGoalMonth(data.target_date ?? '')
  } catch (error) {
    showToast(getApiErrorMessage(error, '목표 정보를 불러오지 못했습니다.'), 'error')
  }
})

onBeforeUnmount(() => {
  document.documentElement.style.backgroundColor = previousHtmlBackground
  document.body.style.backgroundColor = previousBodyBackground
})
</script>

<template>
  <main
    class="flex min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height)-env(safe-area-inset-top)-env(safe-area-inset-bottom))] flex-col bg-[#eef9fe] bg-cover bg-top bg-no-repeat px-[18px] pt-5 pb-6 text-[var(--color-text-primary)]"
    :style="{
      backgroundImage: `linear-gradient(rgba(247, 250, 252, 0.28), rgba(247, 250, 252, 0.42)), url(${goalCloudBackground})`,
    }"
  >
    <form class="flex flex-1 flex-col gap-4" @submit.prevent="saveGoal">
      <section
        class="rounded-[22px] border border-[var(--color-border)] bg-white p-5 shadow-[0_5px_18px_rgba(45,76,92,0.04)]"
        aria-labelledby="goal-info-title"
      >
        <div class="mb-5 flex items-center gap-2">
          <h2 id="goal-info-title" class="m-0 text-[16px] font-extrabold">목표 정보</h2>
        </div>

        <label class="grid gap-2">
          <span class="text-sm font-bold">목표명</span>
          <input
            v-model="form.name"
            class="h-14 cursor-not-allowed rounded-2xl border border-[#d9edf7] bg-[#f5f7f8] px-4 text-base font-semibold text-[var(--color-text-secondary)] outline-none"
            type="text"
            readonly
            aria-describedby="goal-name-help"
            placeholder="목표명을 입력해주세요"
          />
          <small id="goal-name-help" class="text-xs text-[var(--color-text-secondary)]">
            현재는 목표 금액과 목표 월만 변경할 수 있어요.
          </small>
        </label>

        <GoalAmountStep
          class="mt-6"
          :goal-name="form.name"
          :amount="form.amount"
          :target-date="form.targetDate"
          :show-intro="false"
          appearance="management"
          @update:amount="form.amount = $event"
          @update:target-date="form.targetDate = $event"
          @open-recommendation="isRecommendationOpen = true"
        />
      </section>

      <div class="mt-auto grid grid-cols-2 gap-3 pt-6">
        <button
          class="h-14 rounded-2xl border border-[var(--color-border)] bg-white text-[15px] font-semibold text-[var(--color-text-secondary)] transition-colors active:bg-[var(--color-surface-muted)]"
          type="button"
          @click="cancelEdit"
        >
          취소
        </button>
        <button
          class="h-14 rounded-2xl bg-[var(--color-brand-primary)] text-[15px] font-bold text-[var(--color-text-inverse)] shadow-[0_6px_16px_rgb(52_176_230_/_18%)] transition-colors active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[var(--color-disabled-background)] disabled:text-[var(--color-unselected-text)] disabled:shadow-none"
          type="submit"
          :disabled="isSubmitDisabled"
        >
          변경 내용 저장
        </button>
      </div>
    </form>

    <AiRecommendationModal
      v-if="isRecommendationOpen"
      :selected-amount="selectedRecommendationAmount"
      @close="isRecommendationOpen = false"
      @select="selectRecommendation"
    />
  </main>
</template>
