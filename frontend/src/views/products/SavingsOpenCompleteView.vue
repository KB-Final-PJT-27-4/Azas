<script setup lang="ts">
import { Landmark } from 'lucide-vue-next'
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SuccessCheckAnimation from '@/components/feedback/SuccessCheckAnimation.vue'

const router = useRouter()
const route = useRoute()
const availableProductNames = [
  'KB Young Youth 적금',
  'KB아이사랑적금',
  '내 아이를 위한 280일 적금',
] as const
const selectedProductName = computed(() => {
  const productName = String(route.query.product ?? '')
  return availableProductNames.find((name) => name === productName) ?? 'KB아이사랑적금'
})
</script>

<template>
  <main
    class="flex min-h-[calc(100dvh-var(--app-header-height))] flex-col bg-white px-6 pt-8 pb-[calc(28px+env(safe-area-inset-bottom))] text-[var(--color-text-primary)]"
  >
    <section class="flex flex-1 flex-col text-center" aria-labelledby="savings-complete-title">
      <SuccessCheckAnimation class="mx-auto mt-1" />

      <h1
        id="savings-complete-title"
        class="mt-8 text-[28px] leading-tight font-extrabold tracking-[-0.035em]"
      >
        자녀 적금이 개설되었어요!
      </h1>
      <p class="mt-4 text-sm leading-6 text-[var(--color-text-secondary)]">
        이제 깨비의 첫 적금을<br />서비스에 연결해볼까요?
      </p>

      <article
        class="mt-8 flex items-center rounded-2xl border border-[var(--color-border)] bg-white px-5 py-5 text-left shadow-[0_5px_18px_rgba(43,83,105,0.04)]"
        aria-label="개설된 적금 정보"
      >
        <span class="grid size-14 shrink-0 place-items-center rounded-full bg-[#e1e3e5] text-[#8a9298]">
          <Landmark :size="25" :stroke-width="2" aria-hidden="true" />
        </span>
        <div class="ml-4 min-w-0">
          <strong class="block truncate text-lg font-extrabold">{{ selectedProductName }}</strong>
          <span class="mt-1 block text-sm text-[var(--color-text-secondary)]">
            123-456-789012
          </span>
          <span class="mt-1 block text-sm font-semibold text-[var(--color-text-secondary)]">
            잔액 0원
          </span>
        </div>
      </article>

      <button
        class="mt-7 min-h-14 w-full rounded-xl bg-[var(--color-brand-primary)] text-base font-bold text-white shadow-[0_7px_18px_rgba(39,169,235,0.22)] transition-colors active:bg-[var(--color-brand-primary-pressed)]"
        type="button"
        @click="router.push({ name: 'Goals' })"
      >
        목표 설정하기
      </button>
    </section>
  </main>
</template>
