<script setup lang="ts">
import { Landmark } from 'lucide-vue-next'
import SuccessCheckAnimation from '@/components/feedback/SuccessCheckAnimation.vue'

defineProps<{
  bank: string
  accountNumber: string
  accountName: string
  balance: number
}>()

const emit = defineEmits<{
  home: []
  createGoal: []
}>()
</script>

<template>
  <section
    class="flex min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] flex-col bg-white"
  >
    <div class="flex flex-1 flex-col px-6 pt-8 pb-6 text-center">
      <SuccessCheckAnimation class="mx-auto mt-1" />

      <h1
        class="mt-8 text-[28px] leading-tight font-extrabold tracking-[-0.035em] text-[var(--color-text-primary)]"
      >
        계좌 등록이 완료되었어요!
      </h1>
      <p class="mt-4 text-sm leading-6 text-[var(--color-text-secondary)]">
        이제 아이의 저축 목표를 설정하고<br />
        함께 성장해봐요
      </p>

      <article class="mt-8 flex items-center rounded-2xl border border-[var(--color-border)] bg-white px-5 py-5 text-left shadow-[0_5px_18px_rgba(43,83,105,0.04)]" aria-label="연결된 계좌 정보">
        <span class="grid size-14 shrink-0 place-items-center rounded-full bg-[#e1e3e5] text-[#8a9298]">
          <Landmark :size="25" :stroke-width="2" aria-hidden="true" />
        </span>
        <div class="ml-4 min-w-0">
          <strong class="block truncate text-lg font-extrabold text-[var(--color-text-primary)]">
            {{ accountName }}
          </strong>
          <span class="mt-1 block text-sm text-[var(--color-text-secondary)]">
            {{ accountNumber }}
          </span>
          <span class="mt-1 block text-sm font-semibold text-[var(--color-text-secondary)]">
            잔액 {{ balance.toLocaleString('ko-KR') }}원
          </span>
        </div>
      </article>

      <div class="mt-auto grid grid-cols-2 gap-4">
        <button
          class="min-h-14 rounded-xl border border-[var(--color-border)] bg-white text-base font-bold text-[var(--color-selected-text)] transition-colors hover:bg-[var(--color-surface-muted)] active:bg-[var(--color-unselected-background)]"
          type="button"
          @click="emit('home')"
        >
          홈으로 가기
        </button>
        <button
          class="min-h-14 rounded-xl bg-[var(--color-brand-primary)] text-base font-bold text-white transition-colors hover:bg-[var(--color-brand-primary-pressed)] active:bg-[var(--color-brand-primary-pressed)]"
          type="button"
          @click="emit('createGoal')"
        >
          목표 설정하기
        </button>
      </div>
    </div>
  </section>
</template>
