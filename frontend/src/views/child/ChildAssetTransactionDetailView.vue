<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { Pencil } from 'lucide-vue-next'
import { useRoute, useRouter } from 'vue-router'

import { childAccountSummary, getChildTransaction } from '@/mocks/childHome'

const route = useRoute()
const router = useRouter()
const transaction = computed(() => getChildTransaction(String(route.params.transactionId ?? '')))

const memo = ref(transaction.value.memo ?? '')
const memoDraft = ref(memo.value)
const isEditingMemo = ref(false)
const memoInput = ref<HTMLTextAreaElement | null>(null)

const signedAmount = computed(() => {
  const prefix = transaction.value.type === 'income' ? '+' : '-'
  return `${prefix}${Math.abs(transaction.value.amount).toLocaleString('ko-KR')} 원`
})

const accountLabel = computed(() => transaction.value.accountLabel ?? childAccountSummary.accountName)
const depositName = computed(() => transaction.value.depositName ?? transaction.value.title)
const depositAccountNumber = computed(() => transaction.value.depositAccountNumber ?? '가맹점 결제')
const withdrawalName = computed(() => transaction.value.withdrawalName ?? childAccountSummary.accountName)
const withdrawalAccountNumber = computed(
  () => transaction.value.withdrawalAccountNumber ?? '952-17362605-47',
)
const balanceAfterTransaction = computed(
  () => transaction.value.balanceAfterTransaction ?? childAccountSummary.balance,
)

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
