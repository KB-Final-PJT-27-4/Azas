<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { Pencil } from 'lucide-vue-next'
import { useRoute, useRouter } from 'vue-router'

import { api, getApiErrorMessage } from '@/api'

const route = useRoute()
const router = useRouter()
const transaction = reactive({
  amount: 0,
  type: 'expense' as 'income' | 'expense',
  memo: '',
  time: '',
  accountLabel: '',
  depositName: '',
  depositAccountNumber: '',
  withdrawalName: '',
  withdrawalAccountNumber: '',
  balanceAfterTransaction: 0,
})
const errorMessage = ref('')

const memo = ref('')
const memoDraft = ref(memo.value)
const isEditingMemo = ref(false)
const memoInput = ref<HTMLTextAreaElement | null>(null)

const signedAmount = computed(() => {
  const prefix = transaction.type === 'income' ? '+' : '-'
  return `${prefix}${Math.abs(transaction.amount).toLocaleString('ko-KR')} 원`
})

const accountLabel = computed(() => transaction.accountLabel || transaction.withdrawalName || '거래 계좌')
const depositName = computed(() => transaction.depositName || '입금처')
const depositAccountNumber = computed(() => transaction.depositAccountNumber || '-')
const withdrawalName = computed(() => transaction.withdrawalName || '출금처')
const withdrawalAccountNumber = computed(() => transaction.withdrawalAccountNumber || '-')
const balanceAfterTransaction = computed(() => transaction.balanceAfterTransaction)

onMounted(async () => {
  try {
    const transactionId = Number(route.params.transactionId)
    const { data } = await api.getTransactionDetailUsingGET(transactionId)
    transaction.amount = data.amount
    transaction.type = data.direction === 'CREDIT' ? 'income' : 'expense'
    transaction.memo = data.memo ?? ''
    transaction.time = new Date(data.occurred_at).toLocaleString('ko-KR')
    transaction.accountLabel = data.direction === 'CREDIT'
      ? (data.deposit_account.account_name ?? '')
      : (data.withdrawal_account.account_name ?? '')
    transaction.depositName = data.deposit_account.account_name ?? data.deposit_account.bank_name ?? ''
    transaction.depositAccountNumber = data.deposit_account.account_number ?? ''
    transaction.withdrawalName = data.withdrawal_account.account_name ?? data.withdrawal_account.bank_name ?? ''
    transaction.withdrawalAccountNumber = data.withdrawal_account.account_number ?? ''
    transaction.balanceAfterTransaction = data.balance_after ?? 0
    memo.value = transaction.memo
    memoDraft.value = transaction.memo
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, '거래 상세를 불러오지 못했습니다.')
  }
})

const startMemoEdit = async () => {
  memoDraft.value = memo.value
  isEditingMemo.value = true
  await nextTick()
  memoInput.value?.focus()
  memoInput.value?.select()
}

const saveMemo = () => {
  memo.value = memoDraft.value.trim()
  isEditingMemo.value = false
}

const cancelMemoEdit = () => {
  memoDraft.value = memo.value
  isEditingMemo.value = false
}

const updateMemoDraft = (event: Event) => {
  memoDraft.value = (event.target as HTMLTextAreaElement).value
}

const goToAssets = () => {
  router.push({ name: 'ChildAssets' })
}

const formatWon = (amount: number) => `${amount.toLocaleString('ko-KR')} 원`
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-white">
    <article class="px-5 pt-7 pb-10 text-[var(--color-text-primary)]">
      <h1 class="m-0 text-[22px] leading-tight font-bold tracking-[-0.02em]">
        {{ accountLabel }}
      </h1>

      <form v-if="isEditingMemo" class="mt-3" aria-label="메모 수정" @submit.prevent="saveMemo">
        <div class="relative">
          <textarea
            ref="memoInput"
            :value="memoDraft"
            class="block h-24 w-full resize-none rounded-lg border border-[var(--color-brand-primary)] px-3 pt-2.5 pb-7 text-[14px] leading-6 text-[var(--color-text-secondary)] outline-none focus:ring-2 focus:ring-[var(--color-selected-background)]"
            aria-label="메모"
            maxlength="50"
            @input="updateMemoDraft"
            @keydown.esc="cancelMemoEdit"
          ></textarea>
          <span
            class="pointer-events-none absolute right-3 bottom-2 text-[11px] tabular-nums text-[var(--color-text-secondary)]"
          >
            {{ memoDraft.length }}/50
          </span>
        </div>
        <div class="mt-1.5 flex justify-end">
          <button
            class="h-9 shrink-0 rounded-lg bg-[var(--color-brand-primary)] px-4 text-[13px] font-bold text-[var(--color-text-inverse)] active:bg-[var(--color-brand-primary-pressed)]"
            type="submit"
            aria-label="메모 저장"
          >
            완료
          </button>
        </div>
      </form>

      <div v-else-if="memo" class="mt-3 flex min-w-0 items-start gap-1.5">
        <p class="m-0 min-w-0 text-[14px] leading-7 text-[var(--color-text-secondary)]">
          {{ memo }}
        </p>
        <button
          class="grid size-8 shrink-0 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[var(--color-unselected-background)]"
          type="button"
          aria-label="메모 수정"
          @click="startMemoEdit"
        >
          <Pencil :size="19" :stroke-width="2.8" />
        </button>
      </div>

      <button
        v-else
        class="mt-3 inline-flex h-9 items-center gap-1.5 rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] px-3 text-[13px] font-semibold text-[var(--color-text-secondary)] active:bg-[var(--color-unselected-background)]"
        type="button"
        @click="startMemoEdit"
      >
        메모 추가
        <Pencil :size="16" :stroke-width="2.5" />
      </button>

      <div class="mt-5 border-t border-[var(--color-border)] pt-6">
        <dl class="m-0 grid grid-cols-[minmax(92px,1fr)_minmax(0,2fr)] gap-x-5 gap-y-6">
          <dt class="text-[15px] font-semibold text-[var(--color-text-secondary)]">거래 금액</dt>
          <dd
            class="m-0 text-right text-[18px] font-semibold"
            :class="
              transaction.type === 'income'
                ? 'text-[var(--color-selected-text)]'
                : 'text-[var(--color-text-primary)]'
            "
          >
            {{ signedAmount }}
          </dd>

          <dt class="text-[15px] font-semibold text-[var(--color-text-secondary)]">입금처</dt>
          <dd class="m-0 text-right text-[15px] leading-7">
            <strong class="block font-semibold">{{ depositName }}</strong>
            <span class="block text-[var(--color-text-secondary)]">
              {{ depositAccountNumber }}
            </span>
          </dd>

          <dt class="text-[15px] font-semibold text-[var(--color-text-secondary)]">출금처</dt>
          <dd class="m-0 text-right text-[15px] leading-7">
            <strong class="block font-semibold">{{ withdrawalName }}</strong>
            <span class="block text-[var(--color-text-secondary)]">
              {{ withdrawalAccountNumber }}
            </span>
          </dd>

          <dt class="text-[15px] font-semibold text-[var(--color-text-secondary)]">거래시각</dt>
          <dd class="m-0 text-right text-[15px] font-medium">{{ transaction.time }}</dd>

          <dt class="text-[15px] font-semibold text-[var(--color-text-secondary)]">거래 후 잔액</dt>
          <dd class="m-0 text-right text-[15px] font-medium">
            {{ formatWon(balanceAfterTransaction) }}
          </dd>
        </dl>
      </div>

      <button
        class="mt-16 h-14 w-full rounded-[14px] bg-[var(--color-brand-primary)] text-[16px] font-extrabold text-[var(--color-text-inverse)] transition-colors active:bg-[var(--color-brand-primary-pressed)]"
        type="button"
        @click="goToAssets"
      >
        확인
      </button>
    </article>
  </main>
</template>
