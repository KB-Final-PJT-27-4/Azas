<script setup lang="ts">
import { Check, ChevronDown, ExternalLink, Info, X } from 'lucide-vue-next'
import { computed, onMounted, ref } from 'vue'

import { api, getApiErrorMessage } from '@/api'

type AmountRecommendation = {
  code: string
  label: string
  amount: number
  items: string[]
}

type RecommendationBasis = {
  organization: string
  datasetName: string
  referenceYear?: number
  metricName: string
  metricValue?: number
  metricUnit: string
  sourceUrl: string
  description: string
  disclaimer: string
}

const props = defineProps<{
  selectedAmount?: number
  financialGoalTemplateId: number
}>()

const emit = defineEmits<{
  close: []
  select: [amount: number]
}>()

const pendingAmount = ref<number | null>(props.selectedAmount ?? null)
const isVisible = ref(true)
const sheetRef = ref<HTMLElement | null>(null)
const dragOffset = ref(0)
const isDragging = ref(false)
const isSettling = ref(false)
let dragStartY = 0
let lastPointerY = 0
let lastPointerTime = 0
let dragVelocity = 0

const sheetStyle = computed(() => ({
  transform:
    isDragging.value || isSettling.value ? `translateY(${dragOffset.value}px)` : undefined,
  transition: isDragging.value ? 'none' : undefined,
}))

const recommendations = ref<AmountRecommendation[]>([])
const recommendationBasis = ref<RecommendationBasis | null>(null)
const isBasisOpen = ref(false)
const isLoading = ref(true)
const errorMessage = ref('')

const formatAmount = (amount: number) => {
  if (amount >= 100_000_000) {
    const eok = Math.floor(amount / 100_000_000)
    const remainder = amount % 100_000_000
    if (remainder === 0) return `${eok.toLocaleString('ko-KR')}억`
    if (remainder % 10_000 === 0) {
      return `${eok.toLocaleString('ko-KR')}억 ${(remainder / 10_000).toLocaleString('ko-KR')}만원`
    }
    return `${eok.toLocaleString('ko-KR')}억 ${remainder.toLocaleString('ko-KR')}원`
  }
  if (amount >= 10_000 && amount % 10_000 === 0) {
    return `${(amount / 10_000).toLocaleString('ko-KR')}만원`
  }
  return `${amount.toLocaleString('ko-KR')}원`
}

const formatMetric = (basis: RecommendationBasis) => {
  if (basis.metricValue == null) return basis.metricUnit
  return basis.metricUnit.startsWith('원')
    ? `${formatAmount(basis.metricValue)}${basis.metricUnit.slice(1)}`
    : `${basis.metricValue.toLocaleString('ko-KR')} ${basis.metricUnit}`
}

const normalizeSourceUrl = (value: string) =>
  value.match(/https?:\/\/[^\s\])]+/)?.[0] ?? ''

const loadRecommendations = async () => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const { data } = await api.getAmountRecommendationsUsingGET(props.financialGoalTemplateId)
    recommendations.value = (data.recommendations ?? [])
      .filter((recommendation) => Number.isFinite(recommendation.target_amount) && (recommendation.target_amount ?? 0) > 0)
      .sort((left, right) => (left.display_order ?? 0) - (right.display_order ?? 0))
      .map((recommendation) => ({
        code: recommendation.recommendation_code ?? String(recommendation.display_order ?? recommendation.target_amount),
        label: recommendation.title ?? '목표 금액 가이드',
        amount: recommendation.target_amount ?? 0,
        items: recommendation.coverage_items ?? [],
      }))
    const reference = data.reference_data
    recommendationBasis.value = reference
      ? {
          organization: reference.organization ?? '',
          datasetName: reference.dataset_name ?? '',
          referenceYear: reference.reference_year,
          metricName: reference.metric_name ?? '',
          metricValue: reference.metric_value,
          metricUnit: reference.metric_unit ?? '',
          sourceUrl: normalizeSourceUrl(reference.source_url ?? ''),
          description: data.description ?? '',
          disclaimer: data.disclaimer ?? '',
        }
      : null
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, '목표 금액 가이드를 불러오지 못했습니다.')
  } finally {
    isLoading.value = false
  }
}

onMounted(loadRecommendations)

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

const startDrag = (event: PointerEvent) => {
  if (isSettling.value) return
  isDragging.value = true
  dragStartY = event.clientY - dragOffset.value
  lastPointerY = event.clientY
  lastPointerTime = performance.now()
  dragVelocity = 0
  ;(event.currentTarget as HTMLElement).setPointerCapture(event.pointerId)
}

const moveDrag = (event: PointerEvent) => {
  if (!isDragging.value) return

  const now = performance.now()
  const elapsed = Math.max(now - lastPointerTime, 1)
  dragVelocity = (event.clientY - lastPointerY) / elapsed
  lastPointerY = event.clientY
  lastPointerTime = now
  dragOffset.value = Math.max(0, event.clientY - dragStartY)
}

const finishDrag = () => {
  if (!isDragging.value) return
  isDragging.value = false

  const shouldClose = dragOffset.value >= 110 || (dragOffset.value >= 36 && dragVelocity > 0.55)
  if (!shouldClose) {
    isSettling.value = true
    dragOffset.value = 0
    window.setTimeout(() => { isSettling.value = false }, 240)
    return
  }

  isSettling.value = true
  dragOffset.value = sheetRef.value?.offsetHeight ?? window.innerHeight
  window.setTimeout(() => {
    isVisible.value = false
    dragOffset.value = 0
    isSettling.value = false
  }, 200)
}

const cancelDrag = () => {
  if (!isDragging.value) return
  isDragging.value = false
  isSettling.value = true
  dragOffset.value = 0
  window.setTimeout(() => { isSettling.value = false }, 240)
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
          ref="sheetRef"
          class="flex max-h-[calc(100dvh-72px)] w-full max-w-[var(--app-max-width)] flex-col overflow-hidden rounded-t-[24px] bg-[var(--color-surface)] text-[var(--color-text-primary)] shadow-[0_-12px_40px_rgb(0_0_0_/_12%)]"
          :class="{ 'sheet-settling': isSettling }"
          :style="sheetStyle"
          role="dialog"
          aria-modal="true"
          aria-labelledby="ai-modal-title"
        >
          <button
            class="sheet-handle mx-auto grid h-8 w-20 shrink-0 touch-none place-items-center border-0 bg-transparent p-0"
            type="button"
            aria-label="아래로 밀어 닫기"
            @pointerdown="startDrag"
            @pointermove="moveDrag"
            @pointerup="finishDrag"
            @pointercancel="cancelDrag"
          >
            <span class="block h-1 w-10 rounded-full bg-[var(--color-border)]"></span>
          </button>

          <header class="flex shrink-0 items-center justify-between px-6 pt-1 pb-1">
            <div>
              <h2 id="ai-modal-title" class="mt-1 text-xl font-bold">목표 금액 가이드</h2>
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

            <div
              v-if="!isLoading && !errorMessage && recommendations.length > 0"
              class="mt-5 grid gap-3"
            >
              <button
                v-for="option in recommendations"
                :key="option.code"
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
                      {{ formatAmount(option.amount) }}
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

              <section
                v-if="recommendationBasis"
                class="order-first overflow-hidden rounded-2xl border border-[#dce8ee] bg-white"
                aria-labelledby="recommendation-basis-title"
              >
                <button
                  class="flex w-full items-center gap-3 px-4 py-3.5 text-left"
                  type="button"
                  :aria-expanded="isBasisOpen"
                  @click="isBasisOpen = !isBasisOpen"
                >
                  <span class="grid size-8 shrink-0 place-items-center rounded-full bg-[#eaf8ff] text-[var(--color-selected-text)]">
                    <Info :size="17" :stroke-width="2.3" aria-hidden="true" />
                  </span>
                  <span class="min-w-0 flex-1">
                    <strong id="recommendation-basis-title" class="block text-[13px]">추천 기준과 출처</strong>
                    <span class="mt-0.5 block truncate text-[11px] text-[var(--color-text-secondary)]">
                      {{ recommendationBasis.organization }} · {{ recommendationBasis.referenceYear }}년 자료
                    </span>
                  </span>
                  <ChevronDown
                    class="shrink-0 text-[var(--color-text-secondary)] transition-transform"
                    :class="isBasisOpen ? 'rotate-180' : ''"
                    :size="19"
                    aria-hidden="true"
                  />
                </button>

                <Transition name="basis-expand">
                  <div v-if="isBasisOpen" class="border-t border-[#edf1f3] px-4 pt-4 pb-5">
                    <span class="inline-flex rounded-full bg-[#f1f7fa] px-2.5 py-1 text-[10px] font-bold text-[var(--color-text-secondary)]">
                      {{ recommendationBasis.datasetName }}
                    </span>
                    <dl class="mt-3 grid gap-2 rounded-xl bg-[#f7fafb] px-3.5 py-3 text-[11px]">
                      <div class="flex items-start justify-between gap-3">
                        <dt class="shrink-0 text-[var(--color-text-secondary)]">참고 지표</dt>
                        <dd class="m-0 text-right font-bold">{{ recommendationBasis.metricName }}</dd>
                      </div>
                      <div class="flex items-center justify-between gap-3">
                        <dt class="text-[var(--color-text-secondary)]">기준값</dt>
                        <dd class="m-0 font-extrabold text-[var(--color-selected-text)]">
                          {{ formatMetric(recommendationBasis) }}
                        </dd>
                      </div>
                    </dl>
                    <p class="mt-3 mb-0 text-[11px] leading-[1.65] text-[var(--color-text-secondary)]">
                      {{ recommendationBasis.description }}
                    </p>
                    <a
                      v-if="recommendationBasis.sourceUrl"
                      class="mt-3 inline-flex items-center gap-1 text-[11px] font-bold !text-[var(--color-selected-text)] underline underline-offset-2"
                      :href="recommendationBasis.sourceUrl"
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      원문 자료 확인하기
                      <ExternalLink :size="13" :stroke-width="2.3" aria-hidden="true" />
                    </a>
                    <p class="mt-4 mb-0 rounded-xl bg-[#fff9e8] px-3 py-2.5 text-[10px] leading-[1.55] text-[#8b6b20]">
                      {{ recommendationBasis.disclaimer }}
                    </p>
                  </div>
                </Transition>
              </section>
            </div>

            <p
              v-if="isLoading"
              class="mt-5 rounded-2xl bg-[var(--color-surface-muted)] px-4 py-6 text-center text-sm text-[var(--color-text-secondary)]"
            >
              목표 금액 가이드를 불러오는 중이에요.
            </p>
            <p
              v-else-if="errorMessage"
              class="mt-5 rounded-2xl bg-[#fff4f4] px-4 py-6 text-center text-sm text-[#d85a5a]"
            >
              {{ errorMessage }}
            </p>
            <p
              v-else-if="recommendations.length === 0"
              class="mt-5 rounded-2xl bg-[var(--color-surface-muted)] px-4 py-6 text-center text-sm text-[var(--color-text-secondary)]"
            >
              등록된 목표 금액 가이드가 없어요.
            </p>
          </div>

          <div class="shrink-0 px-6 pt-4 pb-[calc(16px+env(safe-area-inset-bottom))]">
            <button
              class="h-14 w-full rounded-2xl bg-[var(--color-brand-primary)] text-base font-bold text-[var(--color-text-inverse)] shadow-[0_6px_16px_rgb(52_176_230_/_22%)] transition-opacity disabled:cursor-not-allowed disabled:opacity-40"
              type="button"
              :disabled="pendingAmount === null || isLoading || Boolean(errorMessage)"
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

.sheet-settling {
  transition: transform 220ms cubic-bezier(0.22, 1, 0.36, 1) !important;
}

.sheet-handle {
  cursor: grab;
  user-select: none;
  -webkit-user-select: none;
}

.sheet-handle:active {
  cursor: grabbing;
}

.basis-expand-enter-active,
.basis-expand-leave-active {
  transition: opacity 160ms ease, transform 180ms ease;
}

.basis-expand-enter-from,
.basis-expand-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}
</style>
