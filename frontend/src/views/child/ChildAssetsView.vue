<script setup lang="ts">
import { computed } from 'vue'
import { ChevronRight, WalletCards } from 'lucide-vue-next'

import ChildBottomNavigation from '@/components/child/ChildBottomNavigation.vue'
import { childAccountSummary, childTransactions, type ChildTransaction } from '@/mocks/childHome'

const fallbackTransactions: ChildTransaction[] = [
  {
    id: 'asset-stationery-1',
    title: '문구점',
    time: '2026.07.21 11:01',
    amount: -10_000,
    type: 'expense',
    icon: '₩',
  },
  {
    id: 'asset-icecream',
    title: '아이스크림 가게',
    time: '2026.07.21 11:01',
    amount: -1_000,
    type: 'expense',
    icon: '₩',
  },
  {
    id: 'asset-convenience',
    title: '편의점',
    time: '2026.07.21 11:01',
    amount: -3_000,
    type: 'expense',
    icon: '₩',
  },
  {
    id: 'asset-stationery-2',
    title: '문구점',
    time: '2026.07.21 11:01',
    amount: -5_000,
    type: 'expense',
    icon: '₩',
  },
  {
    id: 'asset-allowance',
    title: '엄마 용돈',
    time: '2026.07.21 11:01',
    amount: 100_000,
    type: 'income',
    icon: '₩',
  },
]

const usageLeft = computed(() => childAccountSummary.monthlyLimit - childAccountSummary.monthlySpent)
const displayTransactions = computed(() => {
  const merged = [...childTransactions, ...fallbackTransactions]
  const unique = new Map<string, ChildTransaction>()

  merged.forEach((transaction) => {
    if (!unique.has(transaction.id)) {
      unique.set(transaction.id, transaction)
    }
  })

  return Array.from(unique.values()).slice(0, 5)
})

const formatCurrency = (amount: number) => `${Math.abs(amount).toLocaleString('ko-KR')}원`
const formatSignedCurrency = (transaction: ChildTransaction) => {
  const prefix = transaction.type === 'income' ? '+' : '-'

  return `${prefix}${formatCurrency(transaction.amount)}`
}
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-5 pb-[112px]">
    <section
      class="relative overflow-hidden rounded-[26px] border border-[#dceef6] bg-[#eaf8ff] px-5 pt-5 pb-5 shadow-[0_12px_30px_rgba(54,112,139,0.08)]"
      aria-label="이번 달 사용 현황"
    >
      <div class="relative z-[1] max-w-[62%]">
        <p class="m-0 text-[14px] font-bold text-[#628096]">
          이번 달 사용 현황
        </p>
        <div class="mt-3 flex items-end gap-1">
          <strong class="text-[32px] leading-none font-extrabold tracking-[-0.035em] text-[var(--color-text-primary)]">
            {{ formatCurrency(childAccountSummary.monthlySpent).replace('원', '') }}
          </strong>
          <span class="pb-0.5 text-[18px] leading-none font-bold text-[var(--color-text-primary)]">원</span>
        </div>
        <span class="mt-3 inline-flex rounded-full bg-white/85 px-3 py-1.5 text-[12px] font-bold text-[var(--color-selected-text)]">
          {{ childAccountSummary.usageProgress }}프로 사용
        </span>
      </div>

      <div class="relative z-[1] mt-6 rounded-[18px] bg-white/92 px-4 py-4 backdrop-blur-sm">
        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="m-0 text-[12px] text-[var(--color-text-secondary)]">이번 달 한도</p>
            <strong class="mt-1 block text-[15px] text-[var(--color-text-primary)]">
              {{ formatCurrency(childAccountSummary.monthlySpent) }}
              <span class="font-medium text-[var(--color-text-secondary)]">/ {{ formatCurrency(childAccountSummary.monthlyLimit) }}</span>
            </strong>
          </div>
          <span class="shrink-0 text-[12px] font-bold text-[var(--color-selected-text)]">
            {{ formatCurrency(usageLeft) }} 남음
          </span>
        </div>
        <div class="mt-3 h-2 overflow-hidden rounded-full bg-[#e3edf2]">
          <div
            class="h-full rounded-full bg-[var(--color-brand-primary)] transition-[width] duration-500"
            :style="{ width: `${childAccountSummary.usageProgress}%` }"
          />
        </div>
      </div>
    </section>

    <section class="mt-8" aria-labelledby="child-assets-history-title">
      <div class="mb-4 flex items-center justify-between">
        <h1
          id="child-assets-history-title"
          class="m-0 text-[22px] leading-none font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]"
        >
          최근 거래 내역
        </h1>
        <button
          class="flex items-center gap-1 border-0 bg-transparent p-0 text-[16px] font-semibold text-[var(--color-text-secondary)]"
          type="button"
        >
          전체 보기
          <ChevronRight :size="17" :stroke-width="2.7" aria-hidden="true" />
        </button>
      </div>

      <div class="overflow-hidden rounded-[22px] border border-[#e1eaee] bg-white shadow-[0_10px_26px_rgba(54,112,139,0.06)]">
        <article
          v-for="transaction in displayTransactions"
          :key="transaction.id"
          class="grid min-h-[76px] grid-cols-[44px_minmax(0,1fr)_auto_18px] items-center gap-3 border-b border-[#edf1f3] bg-white px-4 py-3 last:border-b-0 active:bg-[#f8fbfc]"
        >
          <div
            class="grid size-11 place-items-center rounded-[14px]"
            :class="transaction.type === 'income' ? 'bg-[#eef8ff] text-[var(--color-brand-primary)]' : 'bg-[#fff7d7] text-[#bd8a00]'"
          >
            <WalletCards :size="22" :stroke-width="2.4" aria-hidden="true" />
          </div>
          <div class="min-w-0">
            <span class="block truncate text-[12px] text-[var(--color-text-secondary)]">
              {{ transaction.time }}
            </span>
            <strong class="mt-1 block truncate text-[14px] font-bold text-[var(--color-text-primary)]">
              {{ transaction.title }}
            </strong>
          </div>
          <strong
            class="text-[14px] font-extrabold"
            :class="
              transaction.type === 'income'
                ? 'text-[var(--color-brand-primary)]'
                : 'text-[var(--color-text-primary)]'
            "
          >
            {{ formatSignedCurrency(transaction) }}
          </strong>
          <ChevronRight class="text-[var(--color-text-secondary)]" :size="20" :stroke-width="2.8" />
        </article>
      </div>
    </section>

    <ChildBottomNavigation />
  </main>
</template>
