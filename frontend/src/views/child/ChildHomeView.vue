<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
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

const isTransferSheetOpen = ref(false)
const transferAccountNumber = ref('')
const transferAmount = ref(10_000)
const completedTransferAmount = ref(0)
const transferNotice = ref<{ type: 'success' | 'error'; message: string } | null>(null)
let transferCloseTimer: ReturnType<typeof window.setTimeout> | null = null
const recentTransactions = computed(() => childTransactions.slice(0, 3))
const transferAmountValue = computed(() => Number(transferAmount.value) || 0)
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

const closeTransferSheet = () => {
  if (transferCloseTimer) {
    window.clearTimeout(transferCloseTimer)
    transferCloseTimer = null
  }

  isTransferSheetOpen.value = false
  transferAccountNumber.value = ''
  transferAmount.value = 10_000
  transferNotice.value = null
}

const submitTransfer = () => {
  if (!canSubmitTransfer.value) {
    transferNotice.value = {
      type: 'error',
      message: transferValidationMessage.value,
    }
    return
  }

  completedTransferAmount.value = transferAmountValue.value
  recordChildTransfer({
    amount: transferAmountValue.value,
    bankName: transferDefaults.bankName,
  })
  transferNotice.value = {
    type: 'success',
    message: `${formatCurrency(completedTransferAmount.value)} 이체가 완료되었어요.`,
  }
  transferAccountNumber.value = ''
  transferAmount.value = 10_000
  transferCloseTimer = window.setTimeout(() => {
    closeTransferSheet()
  }, 900)
}

onBeforeUnmount(() => {
  if (transferCloseTimer) {
    window.clearTimeout(transferCloseTimer)
  }
})
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-[#eef8ff] px-5 pt-6 pb-[120px]">
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
          class="child-balance-button child-balance-button--primary"
          type="button"
          @click="isTransferSheetOpen = true"
        >
          이체하기
        </button>
        <RouterLink
          class="child-balance-button child-balance-button--allowance"
          to="/child/allowance"
        >
          <img
            class="child-allowance-icon"
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
            class="size-11 rounded-[14px]"
            :class="transaction.type === 'income' ? 'bg-[#ebf5ff]' : 'bg-[#fff9d9]'"
            aria-hidden="true"
          />
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
          class="child-request-button"
          to="/child/allowance"
        >
          <img
            class="child-allowance-icon"
            :src="allowanceIconUrl"
            alt=""
            aria-hidden="true"
          />
          용돈 조르기
        </RouterLink>
      </div>
      <img
        class="pointer-events-none absolute right-3 bottom-4 w-[166px] select-none object-contain"
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
              이체하기
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

          <div class="grid gap-4">
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
              <input
                v-model.number="transferAmount"
                class="h-12 rounded-[12px] border border-[var(--color-border)] px-4 text-[length:var(--font-size-xl)] font-extrabold outline-none focus:border-[var(--color-brand-primary)]"
                type="number"
              />
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
              v-if="transferNotice"
              class="m-0 rounded-[12px] px-4 py-3 text-[length:var(--font-size-xs)] font-bold"
              :class="
                transferNotice.type === 'success'
                  ? 'bg-[#e9fbf1] text-[#177245]'
                  : 'bg-[#fff2f2] text-[#d64545]'
              "
              role="alert"
            >
              {{ transferNotice.message }}
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

<style scoped>
.child-balance-button {
  --button-width: 150px;
  --button-height: 48px;
  --button-gap: 4px;
  --icon-width: 38px;
  --icon-height: 26px;

  display: inline-flex;
  width: var(--button-width);
  height: var(--button-height);
  align-items: center;
  justify-content: center;
  gap: var(--button-gap);
  border: 0;
  border-radius: 14px;
  padding: 0 12px;
  font-size: var(--font-size-sm);
  font-weight: 800;
  white-space: nowrap;
}

.child-balance-button--primary {
  margin-top: 24px;
  background: var(--color-brand-primary);
  color: #fff;
}

.child-balance-button--allowance {
  margin-top: 12px;
  background: #fff;
  color: var(--color-selected-text);
}

.child-request-button {
  --button-width: 150px;
  --button-height: 44px;
  --button-gap: 4px;
  --icon-width: 38px;
  --icon-height: 26px;

  display: inline-flex;
  width: var(--button-width);
  height: var(--button-height);
  align-items: center;
  justify-content: center;
  gap: var(--button-gap);
  border-radius: 12px;
  padding: 0 12px;
  background: var(--color-selected-background);
  color: var(--color-selected-text);
  font-size: var(--font-size-sm);
  font-weight: 800;
  white-space: nowrap;
}

.child-allowance-icon {
  width: var(--icon-width);
  height: var(--icon-height);
  flex-shrink: 0;
  object-fit: contain;
}
</style>
