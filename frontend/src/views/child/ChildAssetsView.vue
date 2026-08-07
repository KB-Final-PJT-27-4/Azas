<script setup lang="ts">
import { childTransactions, type ChildTransaction } from '@/mocks/childHome'

const formatCurrency = (amount: number) => `${Math.abs(amount).toLocaleString('ko-KR')}원`
const formatSignedCurrency = (transaction: ChildTransaction) => {
  const prefix = transaction.type === 'income' ? '+' : '-'

  return `${prefix}${formatCurrency(transaction.amount)}`
}
const getTransactionEmoji = (transaction: ChildTransaction) =>
  transaction.type === 'income' ? '💰' : '🛍️'
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-[#E6F1FB] px-4 pt-6 pb-8">
    <h1
      class="m-0 mb-5 text-center text-[length:var(--font-size-lg)] font-extrabold text-[var(--color-text-primary)]"
    >
      최근 돈 기록
    </h1>

    <section
      class="overflow-hidden rounded-[24px] border border-[var(--color-border)] bg-white shadow-[0_14px_32px_rgb(110_122_138_/_10%)]"
      aria-label="최근 돈 기록 목록"
    >
      <article
        v-for="transaction in childTransactions"
        :key="transaction.id"
        class="grid grid-cols-[44px_minmax(0,1fr)_auto] items-center gap-4 border-b border-[var(--color-border)] px-4 py-4 last:border-b-0"
      >
        <div
          class="grid size-11 place-items-center rounded-[14px] text-[20px]"
          :class="transaction.type === 'income' ? 'bg-[#ebfff7]' : 'bg-[#fff2f2]'"
          aria-hidden="true"
        >
          {{ getTransactionEmoji(transaction) }}
        </div>

        <div class="min-w-0">
          <strong
            class="block truncate text-[length:var(--font-size-sm)] text-[var(--color-text-primary)]"
          >
            {{ transaction.title }}
          </strong>
          <span class="text-[length:var(--font-size-xs)] text-[var(--color-text-secondary)]">
            {{ transaction.time }}
          </span>
        </div>

        <strong
          class="text-[length:var(--font-size-sm)]"
          :class="transaction.type === 'income' ? 'text-[#41b883]' : 'text-[#f05d5d]'"
        >
          {{ formatSignedCurrency(transaction) }}
        </strong>
      </article>
    </section>
  </main>
</template>
