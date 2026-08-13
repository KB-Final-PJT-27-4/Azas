<script setup lang="ts">
import { Check, X } from 'lucide-vue-next'
import { ref } from 'vue'

const props = defineProps<{
  selectedAmount?: number
}>()

const emit = defineEmits<{
  close: []
  select: [amount: number]
}>()

const pendingAmount = ref<number | null>(props.selectedAmount ?? null)
const isVisible = ref(true)

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

const applyRecommendation = () => {
  if (pendingAmount.value === null) return
  emit('select', pendingAmount.value)
  isVisible.value = false
}

const closeSheet = () => {
  isVisible.value = false
}

const finishClose = () => {
  emit('close')
}
</script>

<template>
  <Teleport to="body">
    <Transition name="ai-sheet" appear @after-leave="finishClose">
      <div
        v-if="isVisible"
        class="fixed inset-0 z-[var(--z-index-overlay)] flex items-end justify-center bg-black/40"
        role="presentation"
        @click.self="closeSheet"
      >
        <section
          class="flex max-h-[calc(100dvh-72px)] w-full max-w-[var(--app-max-width)] flex-col overflow-hidden rounded-t-[24px] bg-[var(--color-surface)] text-[var(--color-text-primary)] shadow-[0_-12px_40px_rgb(0_0_0_/_12%)]"
          role="dialog"
          aria-modal="true"
          aria-labelledby="ai-modal-title"
        >
          <div class="mx-auto mt-3 h-1 w-10 shrink-0 rounded-full bg-[var(--color-border)]" aria-hidden="true"></div>

          <header class="flex shrink-0 items-center justify-between px-6 pt-4 pb-1">
            <div>
              <h2 id="ai-modal-title" class="mt-1 text-xl font-bold">AI 추천 금액</h2>
            </div>
            <button
              class="grid size-9 place-items-center rounded-full text-[var(--color-text-secondary)] transition-colors active:bg-[var(--color-surface-muted)]"
              type="button"
              aria-label="닫기"
              @click="closeSheet"
            >
              <X :size="21" :stroke-width="2.4" aria-hidden="true" />
            </button>
          </header>

          <div class="min-h-0 flex-1 overflow-y-auto px-6 pb-4">

            <div class="mt-5 grid gap-3">
              <button
                v-for="option in recommendations"
                :key="option.label"
                class="relative w-full rounded-2xl border p-4 pr-16 text-left transition-all"
                :class="
                  pendingAmount === option.amount
                    ? 'border-[var(--color-brand-primary)] bg-[var(--color-selected-background)] shadow-[0_5px_18px_rgb(52_176_230_/_12%)]'
                    : 'border-[var(--color-border)] bg-[var(--color-surface)] active:bg-[var(--color-surface-muted)]'
                "
                type="button"
                :aria-pressed="pendingAmount === option.amount"
                @click="toggleRecommendation(option.amount)"
              >
                <span class="block">
                  <span>
                    <strong class="block text-sm">{{ option.label }}</strong>
                    <strong class="mt-1 block text-xl text-[var(--color-selected-text)]">
                      {{ (option.amount / 10_000).toLocaleString('ko-KR') }}만원
                    </strong>
                  </span>
                  <span
                    class="absolute top-1/2 right-4 grid size-6 -translate-y-1/2 place-items-center rounded-full border text-xs font-bold transition-colors"
                    :class="
                      pendingAmount === option.amount
                        ? 'border-[var(--color-brand-primary)] bg-[var(--color-brand-primary)] text-white'
                        : 'border-[var(--color-border)] text-transparent'
                    "
                    aria-hidden="true"
                  >
                    <Check :size="15" :stroke-width="3" aria-hidden="true" />
                  </span>
                </span>
                <span class="mt-3 flex flex-wrap gap-1.5">
                  <span
                    v-for="item in option.items"
                    :key="item"
                    class="rounded-full px-2.5 py-1 text-xs text-[var(--color-text-secondary)] transition-colors"
                    :class="
                      pendingAmount === option.amount
                        ? 'bg-white'
                        : 'bg-[var(--color-surface-muted)]'
                    "
                  >
                    {{ item }}
                  </span>
                </span>
              </button>
            </div>
          </div>

          <div class="shrink-0 px-6 pt-4 pb-[calc(16px+env(safe-area-inset-bottom))]">
            <button
              class="h-14 w-full rounded-2xl bg-[var(--color-brand-primary)] text-base font-bold text-[var(--color-text-inverse)] shadow-[0_6px_16px_rgb(52_176_230_/_22%)] transition-opacity disabled:cursor-not-allowed disabled:opacity-40"
              type="button"
              :disabled="pendingAmount === null"
              @click="applyRecommendation"
            >
              추천 금액 적용하기
            </button>
          </div>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.ai-sheet-enter-active,
.ai-sheet-leave-active {
  transition: background-color 180ms ease;
}

.ai-sheet-enter-active > section,
.ai-sheet-leave-active > section {
  transition: transform 240ms cubic-bezier(0.22, 1, 0.36, 1);
}

.ai-sheet-enter-from,
.ai-sheet-leave-to {
  background-color: transparent;
}

.ai-sheet-enter-from > section,
.ai-sheet-leave-to > section {
  transform: translateY(100%);
}
</style>
