<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { Info } from 'lucide-vue-next'
import { useRouter } from 'vue-router'

import AiRecommendationModal from '@/components/goals/AiRecommendationModal.vue'
import { BaseDatePicker } from '@/components/common'

const props = defineProps<{
  goalsId: string
}>()

const router = useRouter()
const isRecommendationOpen = ref(false)

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
const savingsMonths = 240
const monthlySavingAmount = computed(() => Math.ceil(form.amount / savingsMonths))
const isSubmitDisabled = computed(() => !form.name.trim() || form.amount <= 0 || !form.targetDate)

const updateAmount = (event: Event) => {
  form.amount = Number((event.target as HTMLInputElement).value.replace(/[^0-9]/g, ''))
}

const selectRecommendation = (amount: number) => {
  form.amount = amount
  isRecommendationOpen.value = false
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

      <label class="grid gap-2">
        <span class="flex items-center justify-between text-sm font-bold">
          목표 금액
          <button
            class="flex h-8 items-center gap-1 rounded-full border border-[var(--color-border)] bg-[var(--color-surface)] px-3 text-xs font-semibold transition-colors hover:bg-[var(--color-surface-muted)]"
            type="button"
            @click="isRecommendationOpen = true"
          >
            AI 추천
            <Info :size="14" class="text-[var(--color-text-secondary)]" />
          </button>
        </span>
        <div
          class="flex h-16 items-center rounded-2xl bg-[var(--color-brand-secondary)] px-5 text-[27px] font-bold"
        >
          <input
            class="min-w-0 flex-1 bg-transparent outline-none"
            inputmode="numeric"
            :value="form.amount.toLocaleString('ko-KR')"
            aria-label="목표 금액"
            @input="updateAmount"
          />
          <span>원</span>
        </div>
      </label>

      <BaseDatePicker
        v-model="form.targetDate"
        label="목표 달성 시기"
        selection-mode="month"
        :min-year="new Date().getFullYear()"
        :max-year="new Date().getFullYear() + 100"
      />

      <div class="rounded-2xl bg-[var(--color-selected-background)] p-5">
        <strong class="text-xl font-bold text-[var(--color-selected-text)]">
          매월 약 {{ monthlySavingAmount.toLocaleString('ko-KR') }}원
        </strong>
        <p class="mt-2 text-sm text-[var(--color-text-secondary)]">
          현재부터 {{ savingsMonths }}개월 동안 균등 저축 기준
        </p>
      </div>

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
      @close="isRecommendationOpen = false"
      @select="selectRecommendation"
    />
  </main>
</template>
