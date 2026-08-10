<script setup lang="ts">
import { X } from 'lucide-vue-next'
import { ref } from 'vue'

const props = defineProps<{
  selectedAmount?: number
}>()

const emit = defineEmits<{
  close: []
  select: [amount: number]
}>()

const pendingAmount = ref<number | null>(props.selectedAmount ?? null)

const recommendations = [
  { label: '기본 준비안', amount: 30_000_000, items: ['등록금', '교재 및 학습비'] },
  { label: '균형 준비안', amount: 50_000_000, items: ['등록금', '생활비', '취업 준비'] },
  {
    label: '장기 준비안',
    amount: 100_000_000,
    items: ['등록금', '생활비', '주거비', '사회초년 자금'],
  },
]

const toggleRecommendation = (amount: number) => {
  if (pendingAmount.value === amount) {
    pendingAmount.value = null
    return
  }

  pendingAmount.value = amount
}

const applyRecommendation = (amount: number) => {
  if (pendingAmount.value !== amount) return
  emit('select', amount)
  emit('close')
}
</script>

<template>
  <div
    class="fixed inset-0 z-[var(--z-index-overlay)] grid place-items-center bg-black/40 px-6"
    role="presentation"
    @click.self="$emit('close')"
  >
    <section
      class="w-full max-w-[380px] rounded-2xl bg-[var(--color-surface)] p-6"
      role="dialog"
      aria-modal="true"
      aria-labelledby="ai-modal-title"
    >
      <header class="flex items-center justify-between">
        <h2 id="ai-modal-title" class="text-xl font-bold">AI 추천 금액</h2>
        <button
          class="grid size-9 place-items-center rounded-full"
          type="button"
          aria-label="닫기"
          @click="$emit('close')"
        >
          <X :size="22" />
        </button>
      </header>
      <p class="mt-3 text-sm leading-6 text-[var(--color-text-secondary)]">
        아이의 현재 나이와 목표 시기를 바탕으로 평균 교육비와 물가 상승률을 고려해 추천했어요.
      </p>

      <div class="mt-5 grid gap-4">
        <div v-for="option in recommendations" :key="option.label" class="relative">
          <button
            class="w-full rounded-xl border p-4 pr-22 text-left transition-colors"
            :class="
              pendingAmount === option.amount
                ? 'border-[var(--color-brand-primary)] bg-[var(--color-selected-background)]'
                : 'border-transparent bg-[var(--color-surface-muted)]'
            "
            type="button"
            :aria-pressed="pendingAmount === option.amount"
            @click="toggleRecommendation(option.amount)"
          >
            <strong class="text-sm">{{ option.label }}</strong>
            <strong class="mt-1 block text-lg text-[var(--color-selected-text)]">
              {{ (option.amount / 10_000).toLocaleString('ko-KR') }}만원
            </strong>
            <ul class="mt-2 text-xs text-[var(--color-text-secondary)]">
              <li v-for="item in option.items" :key="item">• {{ item }}</li>
            </ul>
          </button>

          <button
            v-if="pendingAmount === option.amount"
            class="absolute top-1/2 right-4 h-9 -translate-y-1/2 rounded-full bg-[var(--color-brand-primary)] px-5 text-xs font-semibold text-[var(--color-text-inverse)]"
            type="button"
            :aria-label="`${option.label} 적용`"
            @click="applyRecommendation(option.amount)"
          >
            적용
          </button>
        </div>
      </div>
    </section>
  </div>
</template>
