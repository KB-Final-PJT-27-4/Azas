<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ChevronRight } from 'lucide-vue-next'

import ChildBottomNavigation from '@/components/child/ChildBottomNavigation.vue'
import childCloudBackgroundUrl from '@/assets/images/home/home-hero-bg.png'
import { api, getApiErrorMessage } from '@/api'

type ChildTransaction = { id: string; title: string; time: string; amount: number; type: 'income' | 'expense' }

const childAccountSummary = reactive({ monthlySpent: 0, monthlyLimit: 0, usageProgress: 0 })
const childAssetTransactions = ref<ChildTransaction[]>([])
const errorMessage = ref('')

const showAllTransactions = ref(false)
const transactions = computed(() => {
  const unique = new Map<string, ChildTransaction>()

  childAssetTransactions.value.forEach((transaction) => {
    if (!unique.has(transaction.id)) {
      unique.set(transaction.id, transaction)
    }
  })

  return Array.from(unique.values())
})

onMounted(async () => {
  try {
    const { data: dashboard } = await api.getDashboardUsingGET()
    const spending = dashboard.spending_summary
    childAccountSummary.monthlySpent = spending?.current_month_spent_amount ?? 0
    childAccountSummary.monthlyLimit = spending?.monthly_budget_amount ?? 0
    childAccountSummary.usageProgress = spending?.usage_rate ?? 0
    if (!spending?.account_id) return
    const { data } = await api.getTransactionsUsingGET(spending.account_id, undefined, undefined, 50)
    childAssetTransactions.value = data.transactions.map((item) => ({
      id: String(item.account_transaction_id),
      title: item.counterparty_name ?? '계좌 거래',
      time: new Date(item.occurred_at).toLocaleString('ko-KR'),
      amount: item.amount,
      type: item.direction === 'CREDIT' ? 'income' : 'expense',
    }))
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, '거래 내역을 불러오지 못했습니다.')
  }
})
const displayTransactions = computed(() => {
  if (showAllTransactions.value) return transactions.value
  return transactions.value.slice(0, 5)
})

const formatCurrency = (amount: number) => `${Math.abs(amount).toLocaleString('ko-KR')}원`
const formatSignedCurrency = (transaction: ChildTransaction) => {
  const prefix = transaction.type === 'income' ? '+' : '-'

  return `${prefix}${formatCurrency(transaction.amount)}`
}
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-[#eef9ff] bg-cover bg-top bg-no-repeat px-[18px] pt-4 pb-[112px]"
    :style="{
      backgroundImage: `linear-gradient(rgba(238, 249, 255, 0.76), rgba(255, 255, 255, 0.9)), url(${childCloudBackgroundUrl})`,
    }"
  >
    <section
      class="overflow-hidden rounded-[22px] border border-[#e2edf2] bg-white shadow-[0_8px_24px_rgba(54,112,139,0.07)]"
      aria-label="이번 달 사용 현황"
    >
      <div class="bg-white px-5 pt-[18px] pb-4">
        <p class="m-0 text-[14px] font-bold text-[#628096]">이번 달 사용 현황</p>
        <div class="mt-3 flex items-end gap-1">
          <strong
            class="text-[30px] leading-none font-extrabold tracking-[-0.03em] text-[var(--color-text-primary)]"
          >
            {{ formatCurrency(childAccountSummary.monthlySpent).replace('원', '') }}
          </strong>
          <span class="pb-0.5 text-[17px] leading-none font-bold text-[var(--color-text-primary)]"
            >원</span
          >
        </div>
      </div>

      <div class="bg-white px-5 py-4">
        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="m-0 text-[12px] text-[var(--color-text-secondary)]">
              {{ childAccountSummary.usageProgress }}프로 사용했어요
            </p>
            <strong class="mt-1 block text-[15px] text-[var(--color-text-primary)]">
              {{ formatCurrency(childAccountSummary.monthlySpent) }}
              <span class="font-medium text-[var(--color-text-secondary)]"
                >/ {{ formatCurrency(childAccountSummary.monthlyLimit) }}</span
              >
            </strong>
          </div>
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
          class="flex items-center gap-0.5 border-0 bg-transparent p-0 text-[13px] font-semibold text-[var(--color-text-secondary)]"
          type="button"
          @click="showAllTransactions = !showAllTransactions"
        >
          {{ showAllTransactions ? '접기' : '전체 보기' }}
          <ChevronRight
            :size="15"
            :stroke-width="2.7"
            aria-hidden="true"
            :class="showAllTransactions ? '-rotate-90' : ''"
          />
        </button>
      </div>

      <div
        class="overflow-hidden rounded-[22px] border border-[#e1eaee] bg-white shadow-[0_10px_26px_rgba(54,112,139,0.06)]"
      >
        <RouterLink
          v-for="transaction in displayTransactions"
          :key="transaction.id"
          class="grid min-h-[72px] grid-cols-[minmax(0,1fr)_auto_18px] items-center gap-3 border-b border-[#edf1f3] bg-white px-5 py-3 !text-[var(--color-text-primary)] no-underline last:border-b-0 active:bg-[#f8fbfc]"
          :to="{ name: 'ChildAssetTransactionDetail', params: { transactionId: transaction.id } }"
          :aria-label="`${transaction.title} ${formatSignedCurrency(transaction)} 거래 상세 보기`"
        >
          <div class="min-w-0">
            <span class="block truncate text-[12px] text-[var(--color-text-secondary)]">
              {{ transaction.time }}
            </span>
            <strong
              class="mt-1 block truncate text-[14px] font-bold text-[var(--color-text-primary)]"
            >
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
        </RouterLink>
      </div>
    </section>

    <ChildBottomNavigation />
  </main>
</template>
