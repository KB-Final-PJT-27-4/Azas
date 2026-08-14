<script setup lang="ts">
import ChildBottomNavigation from '@/components/child/ChildBottomNavigation.vue'
import { childChecklistItems } from '@/mocks/childFinanceFlow'
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-5 pb-24">
    <section
      class="overflow-hidden rounded-[16px] border border-[var(--color-border)] bg-white shadow-[0_10px_24px_rgb(110_122_138_/_7%)]"
      aria-label="아이 체크리스트"
    >
      <button
        v-for="item in childChecklistItems"
        :key="item.id"
        class="grid w-full grid-cols-[24px_minmax(0,1fr)_auto] items-center gap-3 border-0 border-b border-[var(--color-border)] bg-white px-4 py-4 text-left last:border-b-0"
        type="button"
        @click="item.completed = !item.completed"
      >
        <span
          class="grid size-[22px] place-items-center rounded-full border text-[12px] font-bold"
          :class="
            item.completed
              ? 'border-[var(--color-brand-primary)] bg-[var(--color-brand-primary)] text-white'
              : 'border-[#cbd8e3] bg-white text-transparent'
          "
        >
          ✓
        </span>
        <strong class="text-[length:var(--font-size-sm)] text-[var(--color-text-primary)]">
          {{ item.title }}
        </strong>
        <span
          class="rounded-full border border-[var(--color-border)] px-3 py-1 text-[length:var(--font-size-xs)] font-bold text-[var(--color-text-secondary)]"
        >
          {{ item.completed ? '완료' : item.count }}
        </span>
      </button>
    </section>

    <section class="mt-5 rounded-[14px] bg-[#fffbe7] px-5 py-5 text-center">
      <p class="m-0 text-[length:var(--font-size-sm)] leading-[1.7] text-[var(--color-text-primary)]">
        미션과 상관없이 내가 스스로<br />
        실천하는 금융 행동이에요!
      </p>
    </section>

    <ChildBottomNavigation />
  </main>
</template>
