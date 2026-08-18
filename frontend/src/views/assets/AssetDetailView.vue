<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ChevronRight, EllipsisVertical, Landmark, Trash2, X } from 'lucide-vue-next'
import { useRoute, useRouter } from 'vue-router'

import AssetTransferResultSheet from '@/components/assets/AssetTransferResultSheet.vue'
import AssetTransferSheet from '@/components/assets/AssetTransferSheet.vue'
import { useToast } from '@/composables/useToast'
import {
  getLinkedAssetAccount,
  getLinkedAssetTransfers,
  removeLinkedAssetAccount,
} from '@/data/assetDummyData'

const route = useRoute()
const router = useRouter()
const { showToast } = useToast()
const account = computed(() => getLinkedAssetAccount(String(route.params.assetId ?? '')))
const isParentAccount = computed(() => account.value.id.startsWith('parent-'))
const recentTransfers = computed(() => getLinkedAssetTransfers(account.value.id))
const isTransferSheetOpen = ref(false)
const transferResult = ref<'success' | 'failure' | null>(null)
const isDeleteDialogOpen = ref(false)
const isAccountMenuOpen = ref(false)
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

const completeTransfer = ({ success }: { success: boolean }) => {
  isTransferSheetOpen.value = false
  transferResult.value = success ? 'success' : 'failure'
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
  removeLinkedAssetAccount(account.value.id)
  isDeleteDialogOpen.value = false
  await router.replace({ name: route.query.from === 'goals' ? 'MypageGoals' : 'Assets' })
  showToast('계좌를 삭제했어요.', 'success')
}
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-white px-[18px] pt-5 pb-9 text-[var(--color-text-primary)]"
  >
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
