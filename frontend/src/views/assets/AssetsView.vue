<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import {
  Baby,
  CalendarClock,
  ChevronRight,
  EllipsisVertical,
  Pencil,
  Plus,
  Trash2,
  UserRound,
} from 'lucide-vue-next'
import { useRoute } from 'vue-router'

import AssetTransferResultSheet from '@/components/assets/AssetTransferResultSheet.vue'
import AssetTransferSheet from '@/components/assets/AssetTransferSheet.vue'
import AutoTransferSheet from '@/components/assets/AutoTransferSheet.vue'

type AccountType = '적금' | '입출금'
type AssetsTab = 'accounts' | 'autoTransfers'

type LinkedAccount = {
  id: string
  name: string
  accountNumber: string
  balance: number
  type: AccountType
}

type AccountGroup = {
  id: 'parent' | 'child'
  title: string
  accounts: LinkedAccount[]
}

type AutoTransfer = {
  id: string
  title: string
  sourceAccountId: string
  sourceAccount: string
  targetAccountId: string
  targetAccount: string
  amount: number
  transferDay: number
  enabled: boolean
}

type AutoTransferSheetData = {
  title: string
  sourceAccountId: string
  targetAccountId: string
  amount: number
  transferDay: number
}

const route = useRoute()
const activeAssetsTab = ref<AssetsTab>('accounts')

const accountGroups: AccountGroup[] = [
  {
    id: 'parent',
    title: '부모 계좌',
    accounts: [
      {
        id: 'parent-saving-1',
        name: '아이사랑적금1',
        accountNumber: '952-17362605-43',
        balance: 4_800_000,
        type: '적금',
      },
      {
        id: 'parent-saving-2',
        name: '아이사랑적금2',
        accountNumber: '952-17362605-44',
        balance: 4_800_000,
        type: '적금',
      },
      {
        id: 'parent-account-1',
        name: '아이사랑통장',
        accountNumber: '952-17362605-45',
        balance: 5_000_000,
        type: '입출금',
      },
    ],
  },
  {
    id: 'child',
    title: '자녀 계좌',
    accounts: [
      {
        id: 'child-saving-1',
        name: '아이사랑적금1',
        accountNumber: '952-17362605-46',
        balance: 9_600_000,
        type: '적금',
      },
      {
        id: 'child-account-1',
        name: '아이사랑통장',
        accountNumber: '952-17362605-47',
        balance: 5_000_000,
        type: '입출금',
      },
    ],
  },
]

const accountOptions = accountGroups.flatMap((group) =>
  group.accounts.map((account) => ({
    id: account.id,
    label: account.name,
    tag: group.id === 'parent' ? '부모' : '자녀',
    name: account.name,
    number: account.accountNumber,
    balance: account.balance,
    type: account.type,
  })),
)
const defaultSourceAccount =
  accountOptions.find(({ type }) => type === '입출금') ?? accountOptions[0]!
const defaultTargetAccount =
  accountOptions.find(({ type }) => type === '적금') ?? accountOptions[0]!

const autoTransfers = ref<AutoTransfer[]>([
  {
    id: 'saving-transfer',
    title: '적금 자동저축',
    sourceAccountId: 'parent-account-1',
    sourceAccount: '아이사랑통장',
    targetAccountId: 'parent-saving-1',
    targetAccount: '아이사랑적금1',
    amount: 200_000,
    transferDay: 20,
    enabled: true,
  },
  {
    id: 'allowance-transfer',
    title: '깨비 용돈',
    sourceAccountId: 'parent-account-1',
    sourceAccount: '아이사랑통장',
    targetAccountId: 'child-account-1',
    targetAccount: '아이사랑통장',
    amount: 100_000,
    transferDay: 1,
    enabled: true,
  },
])

const autoTransferSheetMode = ref<'create' | 'edit'>('create')
const selectedAutoTransferId = ref<string | null>(null)
const isAutoTransferSheetOpen = ref(false)
const openAutoTransferMenuId = ref<string | null>(null)
const selectedAutoTransfer = computed(() =>
  autoTransfers.value.find(({ id }) => id === selectedAutoTransferId.value),
)
const autoTransferSheetInitialData = computed(() => ({
  title: selectedAutoTransfer.value?.title ?? '',
  sourceAccountId: selectedAutoTransfer.value?.sourceAccountId ?? defaultSourceAccount.id,
  targetAccountId: selectedAutoTransfer.value?.targetAccountId ?? defaultTargetAccount.id,
  amount: selectedAutoTransfer.value?.amount ?? 0,
  transferDay: selectedAutoTransfer.value?.transferDay ?? 1,
}))

const isTransferSheetOpen = ref(Boolean(route.query.allowanceRequest))
const transferResult = ref<'success' | 'failure' | null>(null)
const isAnyTransferSheetOpen = computed(
  () => isTransferSheetOpen.value || isAutoTransferSheetOpen.value || transferResult.value !== null,
)
const requestedTransferAmount = computed(() => Number(route.query.amount) || 0)
const requestedTransferMemo = computed(() => String(route.query.memo ?? ''))
const firstAccount = accountGroups[0]!.accounts[0]!
const requestedTargetName = computed(() => String(route.query.targetName ?? firstAccount.name))
const requestedTargetNumber = computed(() =>
  String(route.query.targetNumber ?? firstAccount.accountNumber),
)

let previousBodyOverflow = ''

watch(
  isAnyTransferSheetOpen,
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
const getGroupBalance = (group: AccountGroup) =>
  group.accounts.reduce((total, account) => total + account.balance, 0)

const startEditingAutoTransfer = (transfer: AutoTransfer) => {
  openAutoTransferMenuId.value = null
  selectedAutoTransferId.value = transfer.id
  autoTransferSheetMode.value = 'edit'
  isAutoTransferSheetOpen.value = true
}

const openAutoTransferForm = () => {
  openAutoTransferMenuId.value = null
  selectedAutoTransferId.value = null
  autoTransferSheetMode.value = 'create'
  isAutoTransferSheetOpen.value = true
}

const closeAutoTransferForm = () => {
  isAutoTransferSheetOpen.value = false
}

const toggleAutoTransferMenu = (transferId: string) => {
  openAutoTransferMenuId.value = openAutoTransferMenuId.value === transferId ? null : transferId
}

const closeAutoTransferMenuOnFocusOut = (event: FocusEvent) => {
  const currentTarget = event.currentTarget as HTMLElement
  const nextTarget = event.relatedTarget as Node | null
  if (!nextTarget || !currentTarget.contains(nextTarget)) openAutoTransferMenuId.value = null
}

const deleteAutoTransfer = (transferId: string) => {
  autoTransfers.value = autoTransfers.value.filter(({ id }) => id !== transferId)
  openAutoTransferMenuId.value = null
}

const saveAutoTransfer = (payload: AutoTransferSheetData) => {
  const source = accountOptions.find(({ id }) => id === payload.sourceAccountId)
  const target = accountOptions.find(({ id }) => id === payload.targetAccountId)
  if (!source || !target) return

  const transferData = {
    ...payload,
    sourceAccount: source.name,
    targetAccount: target.name,
  }

  if (autoTransferSheetMode.value === 'edit' && selectedAutoTransfer.value) {
    Object.assign(selectedAutoTransfer.value, transferData)
    isAutoTransferSheetOpen.value = false
    return
  }

  autoTransfers.value.push({
    id: `auto-transfer-${Date.now()}`,
    ...transferData,
    enabled: true,
  })
  isAutoTransferSheetOpen.value = false
}

const completeTransfer = ({ success }: { success: boolean }) => {
  isTransferSheetOpen.value = false
  transferResult.value = success ? 'success' : 'failure'
}

const retryTransfer = () => {
  transferResult.value = null
  isTransferSheetOpen.value = true
}
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-white px-[18px] pt-4 pb-9 text-[var(--color-text-primary)]"
  >
    <header class="px-0.5 py-1">
      <h1 class="m-0 text-[22px] leading-tight font-extrabold tracking-[-0.025em]">계좌</h1>
      <p class="mt-2 mb-0 text-[12px] leading-relaxed text-[#628091]">
        우리 가족의 계좌와 자동이체를 한눈에 확인하고 관리해요.
      </p>
    </header>

    <div
      class="mt-4 grid grid-cols-2 rounded-[14px] border border-[var(--color-border)] bg-[#f3f7f9] p-1"
      role="tablist"
      aria-label="계좌 관리 목록 선택"
    >
      <button
        id="accounts-tab"
        class="h-11 rounded-[10px] text-[13px] font-semibold transition-all"
        :class="
          activeAssetsTab === 'accounts'
            ? 'bg-white text-[var(--color-selected-text)] shadow-[0_2px_8px_rgba(43,171,232,0.16)]'
            : 'text-[var(--color-text-secondary)] active:bg-white/70'
        "
        type="button"
        role="tab"
        :aria-selected="activeAssetsTab === 'accounts'"
        aria-controls="accounts-panel"
        @click="activeAssetsTab = 'accounts'"
      >
        계좌 목록
      </button>
      <button
        id="auto-transfers-tab"
        class="h-11 rounded-[10px] text-[13px] font-semibold transition-all"
        :class="
          activeAssetsTab === 'autoTransfers'
            ? 'bg-white text-[var(--color-selected-text)] shadow-[0_2px_8px_rgba(43,171,232,0.16)]'
            : 'text-[var(--color-text-secondary)] active:bg-white/70'
        "
        type="button"
        role="tab"
        :aria-selected="activeAssetsTab === 'autoTransfers'"
        aria-controls="auto-transfers-panel"
        @click="activeAssetsTab = 'autoTransfers'"
      >
        자동이체
      </button>
    </div>

    <div
      v-if="activeAssetsTab === 'accounts'"
      id="accounts-panel"
      class="mt-3 grid gap-4"
      role="tabpanel"
      aria-labelledby="accounts-tab"
    >
      <section
        v-for="group in accountGroups"
        :key="group.id"
        class="overflow-hidden rounded-[22px] border border-[#e2edf2] bg-white shadow-[0_8px_24px_rgba(54,112,139,0.07)]"
        :aria-labelledby="`${group.id}-accounts-title`"
      >
        <div
          class="px-5 pt-[18px] pb-4"
          :class="group.id === 'parent' ? 'bg-[#f7fcff]' : 'bg-[#fffdf5]'"
        >
          <div class="flex items-center justify-between gap-3">
            <div class="flex min-w-0 items-center gap-3">
              <span
                class="grid size-10 shrink-0 place-items-center rounded-[13px]"
                :class="
                  group.id === 'parent'
                    ? 'bg-[#e5f6ff] text-[var(--color-selected-text)]'
                    : 'bg-[#fff4cd] text-[#c78e0c]'
                "
                aria-hidden="true"
              >
                <UserRound v-if="group.id === 'parent'" :size="21" :stroke-width="2.2" />
                <Baby v-else :size="22" :stroke-width="2.2" />
              </span>
              <div class="min-w-0">
                <h2
                  :id="`${group.id}-accounts-title`"
                  class="m-0 truncate text-[16px] font-extrabold"
                >
                  {{ group.title }}
                </h2>
                <p class="mt-0.5 mb-0 text-[11px] font-medium text-[var(--color-text-secondary)]">
                  연결된 계좌 {{ group.accounts.length }}개
                </p>
              </div>
            </div>

            <RouterLink
              class="flex h-8 w-[82px] shrink-0 items-center justify-center gap-0.5 rounded-[10px] border border-[#cdebf9] bg-white text-[11px] font-bold !text-[var(--color-selected-text)] shadow-[0_2px_7px_rgba(43,171,232,0.08)] active:bg-[#edf9ff]"
              :to="{ name: 'Accounts' }"
            >
              <Plus :size="13" :stroke-width="2.8" aria-hidden="true" />
              계좌 연결
            </RouterLink>
          </div>

          <div class="mt-4 border-t border-[#e8f0f4] pt-3.5">
            <div>
              <span class="block text-[10px] font-semibold text-[var(--color-text-secondary)]">
                총 잔액
              </span>
              <strong
                class="mt-1 block text-[24px] leading-tight font-extrabold tracking-[-0.02em]"
              >
                {{ formatWon(getGroupBalance(group)) }}
              </strong>
            </div>
          </div>
        </div>

        <ul
          class="m-0 grid list-none gap-2.5 p-3"
          :class="group.id === 'parent' ? 'bg-[#f8fbfd]' : 'bg-[#fffdf5]'"
        >
          <li v-for="account in group.accounts" :key="account.id">
            <RouterLink
              class="group/account flex min-h-[70px] items-center gap-3 rounded-[15px] border border-[#e5edf1] bg-white px-4 py-2.5 !text-[var(--color-text-primary)] shadow-[0_2px_8px_rgba(54,112,139,0.035)] transition-colors active:border-[#cfeaf7] active:bg-[#fbfeff]"
              :to="{ name: 'Accounts' }"
              :aria-label="`${account.name} 계좌 관리`"
            >
              <span class="min-w-0 flex-1">
                <span class="flex min-w-0 items-center gap-1.5">
                  <strong class="truncate text-[13px] font-extrabold">{{ account.name }}</strong>
                  <span
                    class="shrink-0 rounded-full px-2 py-0.5 text-[9px] font-bold"
                    :class="
                      account.type === '적금'
                        ? 'bg-[var(--color-selected-background)] text-[var(--color-selected-text)]'
                        : 'bg-[var(--color-brand-secondary)] text-[#a67d18]'
                    "
                  >
                    {{ account.type }}
                  </span>
                </span>
                <span
                  class="mt-1 block truncate text-[10px] font-medium text-[var(--color-text-secondary)]"
                >
                  {{ account.accountNumber }}
                </span>
              </span>

              <span class="flex shrink-0 items-center gap-1">
                <strong
                  class="text-[15px] font-extrabold tracking-[-0.02em] text-[var(--color-text-primary)]"
                >
                  {{ formatWon(account.balance) }}
                </strong>
                <ChevronRight
                  class="text-[#9aabb5] transition-transform group-active/account:translate-x-0.5"
                  :size="17"
                  :stroke-width="2.2"
                  aria-hidden="true"
                />
              </span>
            </RouterLink>
          </li>
        </ul>
      </section>
    </div>

    <div
      v-else
      id="auto-transfers-panel"
      class="mt-3"
      role="tabpanel"
      aria-labelledby="auto-transfers-tab"
    >
      <section
        class="overflow-hidden rounded-[22px] border border-[#e2edf2] bg-white shadow-[0_8px_24px_rgba(54,112,139,0.07)]"
        aria-labelledby="auto-transfer-title"
      >
        <header class="flex items-center justify-between gap-3 bg-[#f7fcff] px-5 py-[18px]">
          <div class="flex min-w-0 items-center gap-3">
            <span
              class="grid size-10 shrink-0 place-items-center rounded-[13px] bg-[#e5f6ff] text-[var(--color-selected-text)]"
              aria-hidden="true"
            >
              <CalendarClock :size="21" :stroke-width="2.2" />
            </span>
            <div class="min-w-0">
              <div class="flex items-center gap-2">
                <h2 id="auto-transfer-title" class="m-0 text-[16px] font-extrabold">
                  자동이체 관리
                </h2>
                <span
                  class="rounded-full bg-[#e5f6ff] px-2 py-0.5 text-[10px] font-bold text-[var(--color-selected-text)]"
                >
                  총 {{ autoTransfers.length }}건
                </span>
              </div>
              <p class="mt-1 mb-0 text-[11px] leading-relaxed text-[var(--color-text-secondary)]">
                이체 금액과 날짜를 확인하고 변경할 수 있어요.
              </p>
            </div>
          </div>
          <button
            class="flex h-8 w-[82px] shrink-0 items-center justify-center gap-0.5 rounded-[10px] border border-[#cdebf9] bg-white text-[11px] font-bold text-[var(--color-selected-text)] shadow-[0_2px_7px_rgba(43,171,232,0.08)] active:bg-[#edf9ff]"
            type="button"
            :aria-expanded="isAutoTransferSheetOpen && autoTransferSheetMode === 'create'"
            @click="openAutoTransferForm"
          >
            <Plus :size="13" :stroke-width="2.8" aria-hidden="true" />
            자동이체
          </button>
        </header>

        <ul class="m-0 grid list-none gap-2.5 bg-[#f8fbfd] p-3">
          <li v-for="transfer in autoTransfers" :key="transfer.id">
            <article
              class="relative rounded-[15px] border border-[#e5edf1] bg-white shadow-[0_2px_8px_rgba(54,112,139,0.035)]"
            >
              <div class="px-4 py-3.5">
                <div class="flex items-start justify-between gap-3">
                  <div class="min-w-0">
                    <strong class="block truncate text-[14px] font-extrabold">
                      {{ transfer.title }}
                    </strong>
                    <span class="mt-1 block text-[11px] text-[var(--color-text-secondary)]">
                      매월 {{ transfer.transferDay }}일
                    </span>
                  </div>

                  <div class="relative shrink-0" @focusout="closeAutoTransferMenuOnFocusOut">
                    <button
                      class="grid size-8 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[#eef4f7]"
                      type="button"
                      :aria-label="`${transfer.title} 관리 메뉴`"
                      :aria-expanded="openAutoTransferMenuId === transfer.id"
                      aria-haspopup="menu"
                      @click="toggleAutoTransferMenu(transfer.id)"
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
                        v-if="openAutoTransferMenuId === transfer.id"
                        class="absolute top-[calc(100%+4px)] right-0 z-20 w-[112px] overflow-hidden rounded-[12px] border border-[#dce8ee] bg-white p-1.5 shadow-[0_10px_28px_rgba(45,77,94,0.16)]"
                        role="menu"
                      >
                        <button
                          class="flex h-9 w-full items-center gap-2 rounded-[8px] px-2.5 text-left text-[11px] font-bold text-[var(--color-text-primary)] active:bg-[#f1f6f8]"
                          type="button"
                          role="menuitem"
                          @click="startEditingAutoTransfer(transfer)"
                        >
                          <Pencil :size="14" :stroke-width="2.1" aria-hidden="true" />
                          수정
                        </button>
                        <button
                          class="flex h-9 w-full items-center gap-2 rounded-[8px] px-2.5 text-left text-[11px] font-bold text-[#ef4f5f] active:bg-[#fff1f3]"
                          type="button"
                          role="menuitem"
                          @click="deleteAutoTransfer(transfer.id)"
                        >
                          <Trash2 :size="14" :stroke-width="2.1" aria-hidden="true" />
                          삭제
                        </button>
                      </div>
                    </Transition>
                  </div>
                </div>

                <strong
                  class="mt-3 block text-[18px] font-extrabold tracking-[-0.02em] text-[var(--color-text-primary)]"
                >
                  {{ formatWon(transfer.amount) }}
                </strong>

                <div class="mt-3 border-t border-[#edf2f5] pt-3">
                  <p
                    class="m-0 truncate text-[10px] font-medium text-[var(--color-text-secondary)]"
                  >
                    {{ transfer.sourceAccount }}
                    <span class="mx-1 text-[#a8b5bd]" aria-hidden="true">→</span>
                    {{ transfer.targetAccount }}
                  </p>
                </div>
              </div>
            </article>
          </li>
        </ul>
      </section>
    </div>

    <button
      class="asset-transfer-button fixed z-[40]"
      type="button"
      aria-label="이체하기"
      @click="isTransferSheetOpen = true"
    >
      <span class="asset-transfer-button__surface">
        <Plus :size="23" :stroke-width="3" aria-hidden="true" />
      </span>
    </button>

    <AssetTransferSheet
      :open="isTransferSheetOpen"
      :target-account-name="requestedTargetName"
      :target-account-number="requestedTargetNumber"
      :initial-amount="requestedTransferAmount"
      :initial-memo="requestedTransferMemo"
      @close="isTransferSheetOpen = false"
      @transfer="completeTransfer"
    />
    <AutoTransferSheet
      :open="isAutoTransferSheetOpen"
      :mode="autoTransferSheetMode"
      :account-options="accountOptions"
      :initial-data="autoTransferSheetInitialData"
      @close="closeAutoTransferForm"
      @save="saveAutoTransfer"
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
.asset-transfer-button {
  bottom: calc(var(--app-bottom-nav-height) + env(safe-area-inset-bottom) - 7px);
  left: 50%;
  width: 88px;
  height: 47px;
  padding: 7px 8px 0;
  color: var(--color-text-inverse);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-bottom: 0;
  border-radius: 48px 48px 0 0;
  box-shadow: 0 -5px 16px rgb(45 91 116 / 8%);
  transform: translateX(-50%);
}

.asset-transfer-button__surface {
  position: relative;
  z-index: 1;
  display: grid;
  width: 100%;
  height: 40px;
  padding-top: 3px;
  place-items: center;
  background: var(--color-brand-primary);
  border-radius: 40px 40px 0 0;
  box-shadow: 0 -3px 10px rgb(39 169 235 / 18%);
  transition:
    background-color 140ms ease,
    transform 140ms ease;
}

.asset-transfer-button:active .asset-transfer-button__surface {
  background: var(--color-brand-primary-pressed);
  transform: translateY(2px);
}

.asset-transfer-button:focus-visible {
  outline: 3px solid rgb(39 169 235 / 24%);
  outline-offset: 3px;
}
</style>
