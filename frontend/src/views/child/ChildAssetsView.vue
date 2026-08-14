<script setup lang="ts">
import { computed } from 'vue'
import { ChevronRight } from 'lucide-vue-next'

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
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-6 pb-[104px]">
    <section
      class="rounded-[18px] border border-[#dceef8] bg-[#F0FAFE] px-5 py-6"
      aria-label="이번 달 사용 현황"
    >
      <p class="m-0 text-[18px] font-bold text-[var(--color-text-primary)]">
        이번 달 사용 현황
      </p>

      <div class="mt-5 flex items-end gap-2">
        <strong class="text-[34px] leading-none font-bold text-black">
          {{ formatCurrency(childAccountSummary.monthlySpent).replace('원', '') }}
        </strong>
        <span class="pb-[2px] text-[20px] leading-none font-bold text-black">원</span>
        <span class="pb-[2px] text-[20px] leading-none text-[var(--color-text-primary)]">
          / {{ formatCurrency(childAccountSummary.monthlyLimit) }}
        </span>
      </div>

      <div class="mt-5 h-[7px] overflow-hidden rounded-full bg-[#d7edf9]">
        <div
          class="h-full rounded-full bg-[var(--color-brand-primary)]"
          :style="{ width: `${childAccountSummary.usageProgress}%` }"
        />
      </div>

      <div class="mt-4 flex items-center justify-between text-[16px] text-[var(--color-text-secondary)]">
        <span>{{ childAccountSummary.usageProgress }}프로 사용했어요</span>
        <span>{{ formatCurrency(usageLeft) }} 남음</span>
      </div>
    </section>

    <section class="mt-8" aria-labelledby="child-assets-history-title">
      <div class="mb-4 flex items-center justify-between">
        <h1
          id="child-assets-history-title"
          class="m-0 text-[24px] leading-none font-bold text-[var(--color-text-primary)]"
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

      <div class="grid gap-4">
        <article
          v-for="transaction in displayTransactions"
          :key="transaction.id"
          class="grid min-h-[78px] grid-cols-[46px_minmax(0,1fr)_auto_18px] items-center gap-4 rounded-[14px] border border-[var(--color-border)] bg-white px-4 py-3"
        >
          <div class="grid size-11 place-items-center rounded-full bg-[#fff7d7] text-[22px] font-semibold text-[#bd8a00]">
            ₩
          </div>
          <div class="min-w-0">
            <span class="block truncate text-[12px] text-[var(--color-text-secondary)]">
              {{ transaction.time }}
            </span>
            <strong class="mt-1 block truncate text-[12px] font-bold text-[var(--color-text-primary)]">
              {{ transaction.title }}
            </strong>
          </div>
          <strong
            class="text-[14px] font-bold"
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
