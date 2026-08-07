<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import AiRecommendationModal from '@/components/goals/AiRecommendationModal.vue'
import GoalAmountStep from '@/components/goals/GoalAmountStep.vue'

const props = defineProps<{
  goalsId: string
}>()

const router = useRouter()
const isRecommendationOpen = ref(false)
const selectedRecommendationAmount = ref<number>()

const goalPresets: Record<string, { name: string; amount: number; targetDate: string }> = {
  '1': { name: '대학자금', amount: 30_000_000, targetDate: '2045-03' },
  '2': { name: '독립자금', amount: 10_000_000, targetDate: '2045-03' },
}

const initialGoal = goalPresets[props.goalsId] ?? {
  name: '나의 목표',
  amount: 30_000_000,
  targetDate: '2045-03',
}

const form = reactive({ ...initialGoal })
const isSubmitDisabled = computed(() => !form.name.trim() || form.amount <= 0 || !form.targetDate)

const selectRecommendation = (amount: number) => {
  form.amount = amount
  selectedRecommendationAmount.value = amount
}

const saveGoal = () => {
  if (isSubmitDisabled.value) return

  // TODO: 목표 수정 API 연결
  router.push({ name: 'MypageGoals' })
}

const cancelEdit = () => {
  router.push({ name: 'MypageGoals' })
}
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-[var(--color-surface)] px-6 pt-8 pb-10 text-[var(--color-text-primary)]"
  >
    <section aria-labelledby="goal-edit-title">
      <h1 id="goal-edit-title" class="text-[25px] leading-tight font-bold tracking-[-0.04em]">
        목표 수정하기
      </h1>
      <p class="mt-2 text-sm text-[var(--color-text-secondary)]">
        목표 금액과 달성 시기를 수정할 수 있어요.
      </p>
    </section>

    <form class="mt-8 grid gap-6" @submit.prevent="saveGoal">
      <label class="grid gap-2">
        <span class="text-sm font-bold">목표명</span>
        <input
          v-model="form.name"
          class="h-14 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 text-base outline-none transition-colors placeholder:text-[var(--color-text-secondary)] focus:border-[var(--color-brand-primary-pressed)] focus:ring-2 focus:ring-[var(--color-selected-background)]"
          type="text"
          placeholder="목표명을 입력해주세요"
        />
      </label>

      <GoalAmountStep
        :goal-name="form.name"
        :amount="form.amount"
        :target-date="form.targetDate"
        :show-intro="false"
        @update:amount="form.amount = $event"
        @update:target-date="form.targetDate = $event"
        @open-recommendation="isRecommendationOpen = true"
      />

      <div class="mt-2 grid grid-cols-2 gap-3">
        <button
          class="h-14 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] text-base font-bold text-[var(--color-text-secondary)] transition-colors active:bg-[var(--color-surface-muted)]"
          type="button"
          @click="cancelEdit"
        >
          취소하기
        </button>
        <button
          class="h-14 rounded-xl bg-[var(--color-brand-primary)] text-base font-bold text-[var(--color-text-inverse)] transition-colors active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[var(--color-disabled-background)] disabled:text-[var(--color-unselected-text)]"
          type="submit"
          :disabled="isSubmitDisabled"
        >
          수정하기
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
