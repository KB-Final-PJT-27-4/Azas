<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ChevronRight, EllipsisVertical, Landmark, Trash2, X } from 'lucide-vue-next'
import { useRoute, useRouter } from 'vue-router'

import AssetTransferResultSheet from '@/components/assets/AssetTransferResultSheet.vue'
import AssetTransferSheet from '@/components/assets/AssetTransferSheet.vue'
import type { AssetAccountSelectOption } from '@/components/assets/AssetAccountSelect.vue'
import { api, getApiErrorMessage } from '@/api'
import { resolveCurrentChildId } from '@/api/context'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const { showToast } = useToast()
const accountId = computed(() => Number(route.params.assetId))
const account = ref({
  id: 0,
  name: '',
  accountNumber: '',
  bankName: '',
  ownerName: '',
  type: '입출금' as '적금' | '입출금' | '청약',
  balance: 0,
  ownerType: 'PARENT',
})
const isParentAccount = computed(() => account.value.ownerType === 'PARENT')
const recentTransfers = ref<
  Array<{
    id: string
    transactionId: number
    transactedAt: string
    counterparty: string
    amount: number
    direction: '입금' | '출금'
  }>
>([])
const transferAccounts = ref<AssetAccountSelectOption[]>([])
const sourceTransferAccounts = ref<AssetAccountSelectOption[]>([])
const isTransferSheetOpen = ref(false)
const transferResult = ref<'success' | 'failure' | null>(null)
const isDeleteDialogOpen = ref(false)
const isAccountMenuOpen = ref(false)
const isLoading = ref(true)
const isAnySheetOpen = computed(
  () => isTransferSheetOpen.value || isDeleteDialogOpen.value || transferResult.value !== null,
)

let previousBodyOverflow = ''

watch(
  isAnySheetOpen,
  (isOpen) => {
    if (isOpen) {
      previousBodyOverflow = document.body.style.overflow
      document.body.style.overflow = 'hidden'
      return
    }

    document.body.style.overflow = previousBodyOverflow
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  document.body.style.overflow = previousBodyOverflow
})

const formatWon = (amount: number) => `${amount.toLocaleString('ko-KR')}원`

const completeTransfer = async ({
  amount,
  memo,
  sourceAccountId,
  targetAccountId,
}: {
  amount: number
  memo: string
  sourceAccountId: string
  targetAccountId: string
}) => {
  try {
    await api.createTransferUsingPOST(crypto.randomUUID(), {
      amount,
      destination_account_id: Number(targetAccountId),
      memo,
      source_account_id: Number(sourceAccountId),
    })
    isTransferSheetOpen.value = false
    transferResult.value = 'success'
    await loadAccount()
  } catch {
    isTransferSheetOpen.value = false
    transferResult.value = 'failure'
  }
}

const retryTransfer = () => {
  transferResult.value = null
  isTransferSheetOpen.value = true
}

const openDeleteDialog = () => {
  isAccountMenuOpen.value = false
  isDeleteDialogOpen.value = true
}

const closeAccountMenuOnFocusOut = (event: FocusEvent) => {
  const currentTarget = event.currentTarget as HTMLElement
  const nextTarget = event.relatedTarget as Node | null
  if (!nextTarget || !currentTarget.contains(nextTarget)) isAccountMenuOpen.value = false
}

const deleteAccount = async () => {
  try {
    await api.unlinkAccountUsingDELETE(accountId.value)
    isDeleteDialogOpen.value = false
    await router.replace({ name: route.query.from === 'goals' ? 'MypageGoals' : 'Assets' })
    showToast('계좌를 삭제했어요.', 'success')
  } catch (error) {
    showToast(getApiErrorMessage(error, '계좌를 삭제하지 못했어요.'), 'error')
  }
}

const loadAccount = async () => {
  try {
    const childId = await resolveCurrentChildId()
    const [detailResponse, transactionsResponse, parentResponse, childResponse] = await Promise.all(
      [
        api.getAccountDetailUsingGET(accountId.value),
        api.getTransactionsUsingGET(accountId.value, undefined, undefined, 20),
        api.getMyAccountsUsingGET(),
        api.getChildAccountsUsingGET(childId),
      ],
    )
    const detail = detailResponse.data
    account.value = {
      id: detail.account_id,
      name: detail.account_name,
      accountNumber: detail.account_number,
      bankName: detail.bank_name,
      ownerName: detail.account_holder_name,
      type:
        detail.account_product_type === 'DEMAND_DEPOSIT'
          ? '입출금'
          : detail.account_product_type === 'SAVINGS'
            ? '적금'
            : '청약',
      balance: detail.balance,
      ownerType: detail.owner_type,
    }
    recentTransfers.value = transactionsResponse.data.transactions.map((transaction) => ({
      id: String(transaction.account_transaction_id),
      transactionId: transaction.account_transaction_id,
      transactedAt: new Date(transaction.occurred_at).toLocaleString('ko-KR'),
      counterparty: transaction.counterparty_name ?? '거래 상대',
      amount: transaction.amount,
      direction: transaction.direction === 'CREDIT' ? '입금' : '출금',
    }))
    transferAccounts.value = [
      ...parentResponse.data.accounts.map((item) => ({
        id: String(item.account_id),
        name: item.account_name,
        number: item.account_number,
        balance: item.balance,
        tag: '부모',
      })),
      ...childResponse.data.accounts.map((item) => ({
        id: String(item.account_id),
        name: item.account_name,
        number: item.account_number,
        balance: item.balance,
        tag: '자녀',
      })),
    ]
    sourceTransferAccounts.value = parentResponse.data.accounts
      .filter(({ account_product_type }) =>
        account_product_type === 'DEMAND_DEPOSIT' || account_product_type === 'SAVINGS',
      )
      .map((item) => ({
        id: String(item.account_id),
        name: item.account_name,
        number: item.account_number,
        balance: item.balance,
        tag: '부모',
      }))
  } catch (error) {
    showToast(getApiErrorMessage(error, '계좌 상세를 불러오지 못했어요.'), 'error')
  } finally {
    isLoading.value = false
  }
}

onMounted(loadAccount)
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-white px-[18px] pt-5 pb-9 text-[var(--color-text-primary)]"
  >
    <div
      v-if="isLoading"
      class="animate-pulse"
      aria-label="계좌 상세 정보 불러오는 중"
      aria-busy="true"
    >
      <section
        class="h-[238px] overflow-hidden rounded-[17px] border border-[#e2e9ed] border-b-[6px] border-b-[#dce9ef] bg-[#f8fbfc] px-4 pt-4 pb-5"
        aria-hidden="true"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <span class="block size-6 rounded-full bg-[#e1e9ed]"></span>
            <span class="block h-2.5 w-16 rounded-full bg-[#e1e9ed]"></span>
          </div>
          <span class="block size-8 rounded-full bg-[#e8eef1]"></span>
        </div>
        <span class="mt-4 block h-6 w-36 rounded-lg bg-[#dce6ea]"></span>
        <div class="mt-2 flex items-start justify-between gap-3">
          <span class="block h-3 w-32 rounded-full bg-[#e5ecef]"></span>
          <span class="block h-8 w-16 -translate-y-2 rounded-full bg-[#dce8ed]"></span>
        </div>
        <div class="mt-4 grid gap-3 border-t border-[#dfe8ed] pt-4">
          <div v-for="width in ['w-14', 'w-12', 'w-20']" :key="width" class="flex justify-between">
            <span class="block h-3 w-12 rounded-full bg-[#e5ecef]"></span>
            <span class="block h-3 rounded-full bg-[#dce6ea]" :class="width"></span>
          </div>
        </div>
      </section>

      <section class="mt-8" aria-hidden="true">
        <div class="flex items-center justify-between">
          <span class="block h-5 w-28 rounded-md bg-[#dce6ea]"></span>
          <span class="block h-2.5 w-12 rounded-full bg-[#e5ecef]"></span>
        </div>
        <div class="mt-3 grid gap-2.5">
          <div
            v-for="index in 4"
            :key="index"
            class="flex min-h-[54px] items-center gap-3 rounded-[13px] border border-[#e2e9ed] px-4 py-2.5"
          >
            <div class="min-w-0 flex-1">
              <span class="block h-2 w-24 rounded-full bg-[#e8eef1]"></span>
              <span class="mt-2 block h-3 w-20 rounded-full bg-[#dfe8ec]"></span>
            </div>
            <span class="block h-3.5 w-20 rounded-full bg-[#dce6ea]"></span>
            <span class="block size-4 rounded bg-[#e8eef1]"></span>
          </div>
        </div>
      </section>
    </div>

    <template v-else>
      <section
        class="overflow-hidden rounded-[17px] border border-[#e2e9ed] shadow-[0_5px_18px_rgba(43,83,105,0.05)]"
        :class="
          isParentAccount
            ? 'border-b-[6px] border-b-[var(--color-brand-primary)] bg-[#f7fcff]'
            : 'border-b-[6px] border-b-[#ffb400] bg-[#fffdf5]'
        "
        aria-labelledby="account-detail-title"
      >
        <div class="px-4 pt-4 pb-5">
          <div class="flex items-center justify-between gap-3">
            <div class="flex min-w-0 items-center gap-2">
              <span
                class="grid size-6 shrink-0 place-items-center rounded-full"
                :class="
                  isParentAccount
                    ? 'bg-[#e5f6ff] text-[var(--color-selected-text)]'
                    : 'bg-[#fff4cd] text-[#c78e0c]'
                "
                aria-hidden="true"
              >
                <Landmark :size="14" :stroke-width="2.1" />
              </span>
              <span class="truncate text-[10px] font-bold text-[var(--color-text-secondary)]">
                {{ account.bankName }}
              </span>
            </div>
            <div class="relative shrink-0" @focusout="closeAccountMenuOnFocusOut">
              <button
                class="grid size-8 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-black/5"
                type="button"
                aria-label="계좌 관리 메뉴"
                :aria-expanded="isAccountMenuOpen"
                aria-haspopup="menu"
                @click="isAccountMenuOpen = !isAccountMenuOpen"
              >
                <EllipsisVertical :size="20" :stroke-width="2.3" aria-hidden="true" />
              </button>

              <Transition
                enter-active-class="transition duration-150 ease-out"
                enter-from-class="-translate-y-1 opacity-0"
                leave-active-class="transition duration-100 ease-in"
                leave-to-class="-translate-y-1 opacity-0"
              >
                <div
                  v-if="isAccountMenuOpen"
                  class="absolute top-[calc(100%+4px)] right-0 z-20 w-[120px] overflow-hidden rounded-[12px] border border-[#dce8ee] bg-white p-1.5 shadow-[0_10px_28px_rgba(45,77,94,0.16)]"
                  role="menu"
                >
                  <button
                    class="flex h-9 w-full items-center gap-2 rounded-[8px] px-2.5 text-left text-[11px] font-bold text-[#ef4f5f] active:bg-[#fff1f3]"
                    type="button"
                    role="menuitem"
                    @click="openDeleteDialog"
                  >
                    <Trash2 :size="14" :stroke-width="2.1" aria-hidden="true" />
                    계좌 삭제
                  </button>
                </div>
              </Transition>
            </div>
          </div>

          <h1
            id="account-detail-title"
            class="mt-3 mb-0 truncate text-[20px] leading-tight font-extrabold tracking-[-0.02em]"
          >
            {{ account.name }}
          </h1>
          <div class="mt-1.5 flex min-w-0 items-center justify-between gap-3">
            <p
              class="m-0 min-w-0 truncate text-[11px] font-medium text-[var(--color-text-secondary)]"
            >
              {{ account.accountNumber }}
            </p>
            <button
              v-if="account.type !== '청약'"
              class="h-8 w-[64px] -translate-y-[9px] shrink-0 rounded-full text-[11px] font-bold text-white shadow-[0_4px_10px_rgba(255,177,0,0.15)] active:opacity-80"
              :class="isParentAccount ? 'bg-[var(--color-brand-primary)]' : 'bg-[#ffb000]'"
              type="button"
              @click="isTransferSheetOpen = true"
            >
              이체
            </button>
          </div>

          <dl class="mt-5 mb-0 grid gap-3 border-t border-[#dfe8ed] pt-4">
            <div class="flex items-center justify-between gap-4">
              <dt class="text-[11px] font-medium text-[var(--color-text-secondary)]">예금주명</dt>
              <dd class="m-0 text-[12px] font-bold">{{ account.ownerName }}</dd>
            </div>
            <div class="flex items-center justify-between gap-4">
              <dt class="text-[11px] font-medium text-[var(--color-text-secondary)]">계좌 유형</dt>
              <dd class="m-0 text-[12px] font-bold">{{ account.type }}</dd>
            </div>
            <div class="flex items-center justify-between gap-4">
              <dt class="text-[11px] font-medium text-[var(--color-text-secondary)]">잔액</dt>
              <dd class="m-0 text-[15px] font-extrabold">{{ formatWon(account.balance) }}</dd>
            </div>
          </dl>
        </div>
      </section>

      <section class="mt-8" aria-labelledby="recent-transfers-title">
        <div class="flex items-center justify-between gap-3">
          <h2 id="recent-transfers-title" class="m-0 text-[16px] font-extrabold">최근 이체 내역</h2>
          <span class="text-[10px] font-semibold text-[var(--color-text-secondary)]">
            최근 {{ recentTransfers.length }}건
          </span>
        </div>

        <ul class="mt-3 mb-0 grid list-none gap-2.5 p-0">
          <li v-for="transfer in recentTransfers" :key="transfer.id">
            <RouterLink
              class="flex min-h-[54px] items-center gap-3 rounded-[13px] border border-[#e2e9ed] bg-white px-4 py-2.5 !text-[var(--color-text-primary)] shadow-[0_2px_8px_rgba(54,112,139,0.025)] transition-colors active:bg-[#f7fbfd]"
              :to="{
                name: 'AssetTransactionDetail',
                params: { assetId: account.id, transactionId: transfer.transactionId },
              }"
              :aria-label="`${transfer.counterparty} ${formatWon(transfer.amount)} 거래 상세 보기`"
            >
              <div class="min-w-0 flex-1">
                <time
                  class="block text-[9px] font-medium text-[var(--color-text-secondary)]"
                  :datetime="transfer.transactedAt"
                >
                  {{ transfer.transactedAt }}
                </time>
                <strong class="mt-1 block truncate text-[11px] font-extrabold">
                  {{ transfer.counterparty }}
                </strong>
              </div>
              <strong
                class="shrink-0 text-[13px] font-extrabold"
                :class="
                  transfer.direction === '입금'
                    ? 'text-[var(--color-selected-text)]'
                    : 'text-[#ef5968]'
                "
              >
                {{ transfer.direction === '입금' ? '+' : '-' }}{{ formatWon(transfer.amount) }}
              </strong>
              <ChevronRight
                class="shrink-0 text-[#91a1ad]"
                :size="17"
                :stroke-width="2.3"
                aria-hidden="true"
              />
            </RouterLink>
          </li>
        </ul>
      </section>
    </template>

    <Teleport to="body">
      <Transition name="account-delete-sheet">
        <div
          v-if="isDeleteDialogOpen"
          class="fixed inset-0 z-[var(--z-index-overlay)] flex items-end justify-center bg-black/40"
          @click.self="isDeleteDialogOpen = false"
        >
          <section
            class="account-delete-sheet__panel w-full max-w-[var(--app-max-width)] rounded-t-[26px] bg-white px-5 pt-3 pb-[calc(24px+env(safe-area-inset-bottom))]"
            role="alertdialog"
            aria-modal="true"
            aria-labelledby="account-delete-title"
            aria-describedby="account-delete-description"
          >
            <span class="mx-auto block h-1 w-10 rounded-full bg-[#d7dfe4]"></span>

            <div class="mt-5 flex items-start justify-between gap-3">
              <div class="min-w-0">
                <h2 id="account-delete-title" class="m-0 text-[19px] font-bold">
                  계좌를 삭제할까요?
                </h2>
                <p
                  id="account-delete-description"
                  class="mt-1.5 mb-0 text-xs leading-relaxed text-[var(--color-text-secondary)]"
                >
                  삭제하면 연결된 계좌 목록과 이체 내역에서 더 이상 확인할 수 없어요.
                </p>
              </div>
              <button
                class="grid size-9 shrink-0 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[#f2f5f7]"
                type="button"
                aria-label="계좌 삭제 확인창 닫기"
                @click="isDeleteDialogOpen = false"
              >
                <X :size="20" :stroke-width="2.3" />
              </button>
            </div>

            <div class="mt-5 rounded-2xl bg-[#f7f9fa] px-4 py-3.5">
              <strong class="block truncate text-sm">{{ account.name }}</strong>
              <span class="mt-1 block text-[11px] text-[var(--color-text-secondary)]">
                {{ account.bankName }} · {{ account.accountNumber }}
              </span>
            </div>

            <div class="mt-5 grid grid-cols-2 gap-3">
              <button
                class="h-[52px] rounded-xl border border-[var(--color-border)] bg-white text-sm font-bold text-[var(--color-text-secondary)] active:bg-[#f5f7f8]"
                type="button"
                @click="isDeleteDialogOpen = false"
              >
                취소
              </button>
              <button
                class="flex h-[52px] items-center justify-center gap-1.5 rounded-xl bg-[#e85b61] text-sm font-bold text-white active:bg-[#cf484e]"
                type="button"
                @click="deleteAccount"
              >
                삭제하기
              </button>
            </div>
          </section>
        </div>
      </Transition>
    </Teleport>

    <AssetTransferSheet
      :open="isTransferSheetOpen"
      :target-account-name="account.name"
      :target-account-number="account.accountNumber"
      :initial-source-account-id="String(account.id)"
      :source-accounts="sourceTransferAccounts"
      :target-accounts="transferAccounts"
      @close="isTransferSheetOpen = false"
      @transfer="completeTransfer"
    />
    <AssetTransferResultSheet
      :open="transferResult !== null"
      :status="transferResult ?? 'success'"
      @close="transferResult = null"
      @retry="retryTransfer"
    />
  </main>
</template>

<style scoped>
.account-delete-sheet-enter-active,
.account-delete-sheet-leave-active {
  transition: background-color 180ms ease;
}

.account-delete-sheet-enter-active .account-delete-sheet__panel,
.account-delete-sheet-leave-active .account-delete-sheet__panel {
  transition: transform 220ms cubic-bezier(0.22, 1, 0.36, 1);
}

.account-delete-sheet-enter-from,
.account-delete-sheet-leave-to {
  background-color: transparent;
}

.account-delete-sheet-enter-from .account-delete-sheet__panel,
.account-delete-sheet-leave-to .account-delete-sheet__panel {
  transform: translateY(100%);
}
</style>
