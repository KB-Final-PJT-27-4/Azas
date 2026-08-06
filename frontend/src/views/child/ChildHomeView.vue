<script setup lang="ts">
import { computed, ref } from 'vue'
import { ChevronDown, ChevronRight, X } from 'lucide-vue-next'

import allowanceCardPigUrl from '@/assets/images/child/child-allowance-card-pig.png'
import allowanceIconUrl from '@/assets/images/child/child-allowance-icon.png'
import childHomeBgUrl from '@/assets/images/child/child-home-bg.png'
import childHomePigUrl from '@/assets/images/child/child-home-pig.png'
import {
  childAccountSummary,
  childTransactions,
  recordChildTransfer,
  transferDefaults,
  type ChildTransaction,
} from '@/mocks/childHome'

type TransferSheetMode = 'form' | 'success'

const isTransferSheetOpen = ref(false)
const transferSheetMode = ref<TransferSheetMode>('form')
const transferAccountNumber = ref('')
const transferAmount = ref(10_000)
const completedTransferAmount = ref(0)
const transferErrorMessage = ref('')
const recentTransactions = computed(() => childTransactions.slice(0, 3))
const transferAmountValue = computed(() => Number(transferAmount.value) || 0)
const formattedTransferAmount = computed(() =>
  transferAmountValue.value > 0 ? transferAmountValue.value.toLocaleString('ko-KR') : '',
)
const transferValidationMessage = computed(() => {
  if (!transferAccountNumber.value.trim()) {
    return '계좌번호를 입력해주세요.'
  }

  if (transferAmountValue.value <= 0) {
    return '보낼 금액을 입력해주세요.'
  }

  if (transferAmountValue.value > transferDefaults.balance) {
    return '잔액보다 큰 금액은 보낼 수 없어요.'
  }

  return ''
})
const canSubmitTransfer = computed(() => transferValidationMessage.value === '')

const formatCurrency = (amount: number) => `${Math.abs(amount).toLocaleString('ko-KR')}원`
const formatSignedCurrency = (transaction: ChildTransaction) => {
  const prefix = transaction.type === 'income' ? '+' : '-'

  return `${prefix}${formatCurrency(transaction.amount)}`
}
const getTransactionEmoji = (transaction: ChildTransaction) =>
  transaction.type === 'income' ? '💰' : '🛍️'

const resetTransferForm = () => {
  transferAccountNumber.value = ''
  transferAmount.value = 10_000
  transferErrorMessage.value = ''
}

const updateTransferAmount = (event: Event) => {
  const input = event.target as HTMLInputElement
  const numericValue = input.value.replace(/\D/g, '')

  transferAmount.value = numericValue ? Number(numericValue) : 0
}

const openTransferSheet = () => {
  transferSheetMode.value = 'form'
  resetTransferForm()
  isTransferSheetOpen.value = true
}

const closeTransferSheet = () => {
  isTransferSheetOpen.value = false
  transferSheetMode.value = 'form'
  resetTransferForm()
}

const submitTransfer = () => {
  if (!canSubmitTransfer.value) {
    transferErrorMessage.value = transferValidationMessage.value
    return
  }

  completedTransferAmount.value = transferAmountValue.value
  recordChildTransfer({
    amount: completedTransferAmount.value,
    bankName: transferDefaults.bankName,
  })
  transferSheetMode.value = 'success'
  resetTransferForm()
}
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-[#eef8ff] px-5 pt-6 pb-[24px]">
    <section
      class="relative min-h-[362px] overflow-hidden rounded-[28px] border border-[#d8ebff] bg-[#dff1ff] px-6 py-7 shadow-[0_16px_36px_rgb(85_192_244_/_16%)]"
      :style="{
        backgroundImage: `url(${childHomeBgUrl})`,
        backgroundPosition: 'center',
        backgroundSize: 'cover',
      }"
      aria-label="아이 잔액 요약"
    >
      <div class="relative z-[1] max-w-[170px]">
        <span
          class="inline-flex rounded-full bg-[var(--color-selected-background)] px-4 py-2 text-[length:var(--font-size-xs)] font-extrabold text-[var(--color-selected-text)]"
        >
          {{ childAccountSummary.accountName }}
        </span>
        <p class="mt-5 mb-2 text-[length:var(--font-size-sm)] text-[var(--color-text-secondary)]">
          현재 잔액
        </p>
        <strong
          class="block whitespace-nowrap text-[clamp(34px,9vw,40px)] leading-none font-black text-[var(--color-text-primary)]"
        >
          {{ formatCurrency(childAccountSummary.balance) }}
        </strong>
        <p
          class="mt-5 mb-3 text-[length:var(--font-size-xs)] leading-[1.6] text-[var(--color-text-secondary)]"
        >
          이번 달 사용 {{ formatCurrency(childAccountSummary.monthlySpent) }}<br />
          하루 한도 {{ formatCurrency(childAccountSummary.dailyLimit) }}
        </p>
        <div class="h-2 overflow-hidden rounded-full bg-white/70">
          <div
            class="h-full rounded-full bg-[var(--color-brand-primary)]"
            :style="{ width: `${childAccountSummary.usageProgress}%` }"
          />
        </div>

        <button
          class="mt-6 flex h-12 w-[150px] items-center justify-center rounded-[14px] border-0 bg-[var(--color-brand-primary)] text-[length:var(--font-size-sm)] font-extrabold text-white"
          type="button"
          @click="openTransferSheet"
        >
          이체하기
        </button>
        <RouterLink
          class="mt-3 inline-flex h-11 min-w-[150px] max-w-full items-center justify-center gap-2 rounded-[14px] bg-white px-4 text-[length:var(--font-size-sm)] font-extrabold whitespace-nowrap text-[#3BA9FF]"
          to="/child/allowance"
        >
          <img
            class="h-[28px] w-[42px] shrink-0 object-contain"
            :src="allowanceIconUrl"
            alt=""
            aria-hidden="true"
          />
          용돈 조르기
        </RouterLink>
      </div>

      <img
        class="pointer-events-none absolute right-[-14px] bottom-5 w-[clamp(170px,46vw,198px)] select-none object-contain"
        :src="childHomePigUrl"
        alt=""
        aria-hidden="true"
      />
    </section>

    <section class="mt-8">
      <div class="mb-4 flex items-center justify-between">
        <h1
          class="m-0 text-[length:var(--font-size-lg)] font-extrabold text-[var(--color-text-primary)]"
        >
          최근 돈 기록
        </h1>
        <RouterLink
          class="inline-flex items-center text-[length:var(--font-size-xs)] font-bold text-[var(--color-text-secondary)]"
          to="/child/assets"
        >
          더보기
          <ChevronRight :size="14" :stroke-width="2.5" />
        </RouterLink>
      </div>

      <div
        class="overflow-hidden rounded-[24px] bg-white shadow-[0_14px_32px_rgb(110_122_138_/_10%)]"
      >
        <article
          v-for="transaction in recentTransactions"
          :key="transaction.id"
          class="grid grid-cols-[44px_minmax(0,1fr)_auto] items-center gap-4 border-b border-[var(--color-border)] px-4 py-4 last:border-b-0"
        >
          <div
            class="grid size-11 place-items-center rounded-[14px] text-[20px]"
            :class="transaction.type === 'income' ? 'bg-[#ebf5ff]' : 'bg-[#fff9d9]'"
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
            :class="
              transaction.type === 'income'
                ? 'text-[var(--color-selected-text)]'
                : 'text-[var(--color-text-primary)]'
            "
          >
            {{ formatSignedCurrency(transaction) }}
          </strong>
        </article>
      </div>
    </section>

    <section
      class="relative mt-4 min-h-[158px] overflow-hidden rounded-[24px] bg-white px-6 py-6 shadow-[0_14px_32px_rgb(110_122_138_/_10%)]"
    >
      <div class="relative z-[1] max-w-[188px]">
        <h2
          class="m-0 text-[length:var(--font-size-md)] font-extrabold text-[var(--color-text-primary)]"
        >
          필요한 게 있나요?
        </h2>
        <p class="mt-2 mb-4 text-[length:var(--font-size-sm)] text-[var(--color-text-secondary)]">
          부모님께 용돈을 요청해보세요!
        </p>
        <RouterLink
          class="inline-flex h-11 min-w-[135px] max-w-full items-center justify-center gap-2 rounded-[12px] bg-[var(--color-selected-background)] px-4 text-[length:var(--font-size-sm)] font-extrabold whitespace-nowrap text-[#3BA9FF]"
          to="/child/allowance"
        >
          <img
            class="h-[28px] w-[42px] shrink-0 object-contain"
            :src="allowanceIconUrl"
            alt=""
            aria-hidden="true"
          />
          용돈 조르기
        </RouterLink>
      </div>
      <img
        class="pointer-events-none absolute right-2 bottom-3 w-[176px] select-none object-contain"
        :src="allowanceCardPigUrl"
        alt=""
        aria-hidden="true"
      />
    </section>

    <Teleport to="body">
      <div
        v-if="isTransferSheetOpen"
        class="fixed inset-0 z-[var(--z-index-overlay)] bg-black/35"
        @click.self="closeTransferSheet"
      >
        <section
          class="fixed bottom-0 left-1/2 w-full max-w-[var(--app-max-width)] -translate-x-1/2 rounded-t-[24px] bg-white px-5 pt-4 pb-[calc(20px+env(safe-area-inset-bottom))] shadow-[0_-16px_40px_rgb(51_51_51_/_18%)]"
          role="dialog"
          aria-modal="true"
          aria-labelledby="transfer-sheet-title"
        >
          <div class="mb-4 flex items-center justify-between">
            <h2
              id="transfer-sheet-title"
              class="m-0 text-[length:var(--font-size-lg)] font-extrabold"
            >
              {{ transferSheetMode === 'success' ? '이체 완료' : '이체하기' }}
            </h2>
            <button
              class="grid size-9 place-items-center rounded-full border-0 bg-[var(--color-surface-muted)] p-0 text-[var(--color-unselected-text)]"
              type="button"
              aria-label="닫기"
              @click="closeTransferSheet"
            >
              <X :size="20" />
            </button>
          </div>

          <div
            v-if="transferSheetMode === 'success'"
            class="grid justify-items-center gap-4 py-4 text-center"
          >
            <div
              class="grid size-16 place-items-center rounded-full bg-[var(--color-selected-background)] text-[28px] font-black text-[var(--color-selected-text)]"
              aria-hidden="true"
            >
              ✓
            </div>
            <div>
              <h3
                class="m-0 text-[length:var(--font-size-lg)] font-black text-[var(--color-text-primary)]"
              >
                이체가 완료되었어요!
              </h3>
              <p
                class="mt-2 mb-0 text-[length:var(--font-size-sm)] text-[var(--color-text-secondary)]"
              >
                {{ formatCurrency(completedTransferAmount) }}을 보냈어요.
              </p>
            </div>
            <button
              class="mt-2 h-12 w-full rounded-[14px] border-0 bg-[var(--color-brand-primary)] text-[length:var(--font-size-md)] font-extrabold text-white"
              type="button"
              @click="closeTransferSheet"
            >
              확인
            </button>
          </div>

          <div v-else class="grid gap-4">
            <label class="grid gap-2 text-[length:var(--font-size-sm)] font-bold">
              어디로 보낼까요?
              <button
                class="flex h-12 items-center justify-between rounded-[12px] border border-[var(--color-border)] bg-white px-4 text-left"
                type="button"
              >
                <span>{{ transferDefaults.bankName }}</span>
                <ChevronDown :size="18" />
              </button>
            </label>

            <label class="grid gap-2 text-[length:var(--font-size-sm)] font-bold">
              계좌번호
              <input
                v-model="transferAccountNumber"
                class="h-12 rounded-[12px] border border-[var(--color-border)] px-4 text-[length:var(--font-size-sm)] outline-none focus:border-[var(--color-brand-primary)]"
                placeholder="계좌번호를 입력해주세요"
              />
            </label>

            <label class="grid gap-2 text-[length:var(--font-size-sm)] font-bold">
              얼마를 보낼까요?
              <div class="relative">
                <input
                  :value="formattedTransferAmount"
                  class="h-12 w-full rounded-[12px] border border-[var(--color-border)] px-4 pr-10 text-[length:var(--font-size-xl)] font-extrabold outline-none focus:border-[var(--color-brand-primary)]"
                  inputmode="numeric"
                  placeholder="0"
                  type="text"
                  @input="updateTransferAmount"
                />
                <span
                  class="pointer-events-none absolute top-1/2 right-4 -translate-y-1/2 text-[length:var(--font-size-md)] font-extrabold text-[var(--color-text-primary)]"
                >
                  원
                </span>
              </div>
              <span
                class="text-[length:var(--font-size-xs)] font-normal text-[var(--color-text-secondary)]"
              >
                내 잔액 {{ formatCurrency(transferDefaults.balance) }}
              </span>
            </label>

            <div class="grid grid-cols-3 gap-2">
              <button
                v-for="amount in transferDefaults.quickAmounts"
                :key="amount"
                class="h-10 rounded-full border border-[var(--color-border)] bg-white text-[length:var(--font-size-sm)] font-bold text-[var(--color-text-secondary)]"
                type="button"
                @click="transferAmount = amount"
              >
                {{ formatCurrency(amount) }}
              </button>
            </div>

            <p
              v-if="transferErrorMessage"
              class="m-0 rounded-[12px] bg-[#fff2f2] px-4 py-3 text-[length:var(--font-size-xs)] font-bold text-[#d64545]"
              role="alert"
            >
              {{ transferErrorMessage }}
            </p>

            <button
              class="mt-1 h-12 rounded-[12px] border-0 text-[length:var(--font-size-md)] font-extrabold text-white"
              :class="canSubmitTransfer ? 'bg-[var(--color-brand-primary)]' : 'bg-[#c8d2da]'"
              type="button"
              :aria-disabled="!canSubmitTransfer"
              @click="submitTransfer"
            >
              {{
                canSubmitTransfer
                  ? `${formatCurrency(transferAmountValue)} 보내기`
                  : '입력 내용을 확인해주세요'
              }}
            </button>
          </div>
        </section>
      </div>
    </Teleport>
  </main>
</template>
