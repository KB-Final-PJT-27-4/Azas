<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { Baby, CalendarClock, ChevronRight, Pencil, Plus, UserRound } from 'lucide-vue-next'
import { useRoute } from 'vue-router'

import AssetTransferResultSheet from '@/components/assets/AssetTransferResultSheet.vue'
import AssetTransferSheet from '@/components/assets/AssetTransferSheet.vue'

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
  sourceAccount: string
  targetAccount: string
  amount: number
  transferDay: number
  enabled: boolean
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
    label: `${group.title} · ${account.name}`,
    name: account.name,
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
    sourceAccount: '아이사랑통장',
    targetAccount: '아이사랑적금1',
    amount: 200_000,
    transferDay: 20,
    enabled: true,
  },
  {
    id: 'allowance-transfer',
    title: '깨비 용돈',
    sourceAccount: '아이사랑통장',
    targetAccount: '깨비 입출금계좌',
    amount: 100_000,
    transferDay: 1,
    enabled: true,
  },
])

const editingAutoTransferId = ref<string | null>(null)
const editingAmount = ref(0)
const editingTransferDay = ref(1)
const isAddingAutoTransfer = ref(false)
const newAutoTransfer = reactive({
  title: '',
  sourceAccount: defaultSourceAccount.name,
  targetAccount: defaultTargetAccount.name,
  amount: 0,
  transferDay: 1,
})
const activeAutoTransferCount = computed(
  () => autoTransfers.value.filter((transfer) => transfer.enabled).length,
)
const isAutoTransferEditValid = computed(
  () =>
    Number.isFinite(editingAmount.value) &&
    editingAmount.value > 0 &&
    Number.isInteger(editingTransferDay.value) &&
    editingTransferDay.value >= 1 &&
    editingTransferDay.value <= 28,
)
const isNewAutoTransferValid = computed(
  () =>
    newAutoTransfer.title.trim().length > 0 &&
    newAutoTransfer.sourceAccount.length > 0 &&
    newAutoTransfer.targetAccount.length > 0 &&
    Number.isFinite(newAutoTransfer.amount) &&
    newAutoTransfer.amount > 0 &&
    Number.isInteger(newAutoTransfer.transferDay) &&
    newAutoTransfer.transferDay >= 1 &&
    newAutoTransfer.transferDay <= 28,
)

const isTransferSheetOpen = ref(Boolean(route.query.allowanceRequest))
const transferResult = ref<'success' | 'failure' | null>(null)
const isAnyTransferSheetOpen = computed(
  () => isTransferSheetOpen.value || transferResult.value !== null,
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

const toggleAutoTransfer = (transferId: string) => {
  const transfer = autoTransfers.value.find(({ id }) => id === transferId)
  if (transfer) transfer.enabled = !transfer.enabled
}

const startEditingAutoTransfer = (transfer: AutoTransfer) => {
  editingAutoTransferId.value = transfer.id
  editingAmount.value = transfer.amount
  editingTransferDay.value = transfer.transferDay
}

const cancelEditingAutoTransfer = () => {
  editingAutoTransferId.value = null
}

const saveAutoTransfer = () => {
  if (!isAutoTransferEditValid.value || editingAutoTransferId.value === null) return

  const transfer = autoTransfers.value.find(({ id }) => id === editingAutoTransferId.value)
  if (!transfer) return

  transfer.amount = Math.trunc(editingAmount.value)
  transfer.transferDay = editingTransferDay.value
  editingAutoTransferId.value = null
}

const openAutoTransferForm = () => {
  editingAutoTransferId.value = null
  isAddingAutoTransfer.value = true
}

const closeAutoTransferForm = () => {
  isAddingAutoTransfer.value = false
}

const addAutoTransfer = () => {
  if (!isNewAutoTransferValid.value) return

  autoTransfers.value.push({
    id: `auto-transfer-${Date.now()}`,
    title: newAutoTransfer.title.trim(),
    sourceAccount: newAutoTransfer.sourceAccount,
    targetAccount: newAutoTransfer.targetAccount,
    amount: Math.trunc(newAutoTransfer.amount),
    transferDay: newAutoTransfer.transferDay,
    enabled: true,
  })

  newAutoTransfer.title = ''
  newAutoTransfer.sourceAccount = defaultSourceAccount.name
  newAutoTransfer.targetAccount = defaultTargetAccount.name
  newAutoTransfer.amount = 0
  newAutoTransfer.transferDay = 1
  isAddingAutoTransfer.value = false
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
              class="flex h-8 shrink-0 items-center gap-0.5 rounded-[10px] border border-[#cdebf9] bg-white px-3 text-[11px] font-bold !text-[var(--color-selected-text)] shadow-[0_2px_7px_rgba(43,171,232,0.08)] active:bg-[#edf9ff]"
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
        <header class="flex items-start justify-between gap-3 bg-[#f7fcff] px-5 py-[18px]">
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
                  {{ activeAutoTransferCount }}건 사용 중
                </span>
              </div>
              <p class="mt-1 mb-0 text-[11px] leading-relaxed text-[var(--color-text-secondary)]">
                이체 금액과 날짜를 확인하고 변경할 수 있어요.
              </p>
            </div>
          </div>
          <button
            class="flex h-8 shrink-0 items-center gap-0.5 rounded-[10px] border border-[#cdebf9] bg-white px-3 text-[11px] font-bold text-[var(--color-selected-text)] shadow-[0_2px_7px_rgba(43,171,232,0.08)] active:bg-[#edf9ff]"
            type="button"
            :aria-expanded="isAddingAutoTransfer"
            @click="isAddingAutoTransfer ? closeAutoTransferForm() : openAutoTransferForm()"
          >
            <Plus :size="13" :stroke-width="2.8" aria-hidden="true" />
            자동이체
          </button>
        </header>

        <form
          v-if="isAddingAutoTransfer"
          class="border-t border-[#e4edf2] bg-[#f7fbfd] px-4 py-4"
          @submit.prevent="addAutoTransfer"
        >
          <h3 class="m-0 text-[13px] font-extrabold">새 자동이체</h3>
          <p class="mt-1 mb-0 text-[10px] text-[var(--color-text-secondary)]">
            자동으로 보낼 계좌와 일정을 설정해 주세요.
          </p>

          <label class="mt-3 block">
            <span class="block text-[10px] font-bold text-[var(--color-text-secondary)]">
              자동이체 이름
            </span>
            <input
              v-model="newAutoTransfer.title"
              class="mt-1.5 h-10 w-full rounded-[10px] border border-[#dce8ee] bg-white px-3 text-[12px] font-bold outline-none focus:border-[var(--color-brand-primary)]"
              type="text"
              maxlength="20"
              placeholder="예: 매달 적금"
            />
          </label>

          <div class="mt-3 grid gap-2.5">
            <label>
              <span class="block text-[10px] font-bold text-[var(--color-text-secondary)]">
                출금 계좌
              </span>
              <select
                v-model="newAutoTransfer.sourceAccount"
                class="mt-1.5 h-10 w-full rounded-[10px] border border-[#dce8ee] bg-white px-3 text-[11px] font-bold outline-none focus:border-[var(--color-brand-primary)]"
              >
                <option
                  v-for="account in accountOptions"
                  :key="`source-${account.id}`"
                  :value="account.name"
                >
                  {{ account.label }}
                </option>
              </select>
            </label>

            <label>
              <span class="block text-[10px] font-bold text-[var(--color-text-secondary)]">
                입금 계좌
              </span>
              <select
                v-model="newAutoTransfer.targetAccount"
                class="mt-1.5 h-10 w-full rounded-[10px] border border-[#dce8ee] bg-white px-3 text-[11px] font-bold outline-none focus:border-[var(--color-brand-primary)]"
              >
                <option
                  v-for="account in accountOptions"
                  :key="`target-${account.id}`"
                  :value="account.name"
                >
                  {{ account.label }}
                </option>
              </select>
            </label>
          </div>

          <div class="mt-3 grid grid-cols-[minmax(0,1fr)_92px] gap-2.5">
            <label class="min-w-0">
              <span class="block text-[10px] font-bold text-[var(--color-text-secondary)]">
                이체 금액
              </span>
              <span
                class="mt-1.5 flex h-10 items-center rounded-[10px] border border-[#dce8ee] bg-white px-3 focus-within:border-[var(--color-brand-primary)]"
              >
                <input
                  v-model.number="newAutoTransfer.amount"
                  class="min-w-0 flex-1 bg-transparent text-[12px] font-bold outline-none"
                  type="number"
                  inputmode="numeric"
                  min="1"
                  step="1000"
                  aria-label="새 자동이체 금액"
                />
                <span class="ml-1 text-[11px] text-[var(--color-text-secondary)]">원</span>
              </span>
            </label>

            <label>
              <span class="block text-[10px] font-bold text-[var(--color-text-secondary)]">
                이체일
              </span>
              <span
                class="mt-1.5 flex h-10 items-center rounded-[10px] border border-[#dce8ee] bg-white px-3 focus-within:border-[var(--color-brand-primary)]"
              >
                <input
                  v-model.number="newAutoTransfer.transferDay"
                  class="min-w-0 flex-1 bg-transparent text-[12px] font-bold outline-none"
                  type="number"
                  inputmode="numeric"
                  min="1"
                  max="28"
                  aria-label="새 자동이체 이체일"
                />
                <span class="ml-1 text-[11px] text-[var(--color-text-secondary)]">일</span>
              </span>
            </label>
          </div>

          <p class="mt-2 mb-0 text-[9px] leading-relaxed text-[var(--color-text-secondary)]">
            자동이체일은 매월 1일부터 28일 사이로 설정할 수 있어요.
          </p>

          <div class="mt-3 grid grid-cols-2 gap-2">
            <button
              class="h-9 rounded-[10px] border border-[#dce8ee] bg-white text-[11px] font-bold text-[var(--color-text-secondary)] active:bg-[#f2f6f8]"
              type="button"
              @click="closeAutoTransferForm"
            >
              취소
            </button>
            <button
              class="h-9 rounded-[10px] bg-[var(--color-brand-primary)] text-[11px] font-bold text-white active:bg-[var(--color-brand-primary-pressed)] disabled:bg-[#cad8df]"
              type="submit"
              :disabled="!isNewAutoTransferValid"
            >
              자동이체 추가
            </button>
          </div>
        </form>

        <ul class="m-0 grid list-none gap-2.5 bg-[#f8fbfd] p-3">
          <li v-for="transfer in autoTransfers" :key="transfer.id">
            <article
              class="overflow-hidden rounded-[15px] border border-[#e5edf1] bg-white shadow-[0_2px_8px_rgba(54,112,139,0.035)]"
              :class="!transfer.enabled ? 'opacity-65' : ''"
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

                  <button
                    class="relative h-7 w-12 shrink-0 rounded-full transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--color-brand-primary)]"
                    :class="transfer.enabled ? 'bg-[var(--color-brand-primary)]' : 'bg-[#dfe8ed]'"
                    type="button"
                    role="switch"
                    :aria-label="`${transfer.title} 자동이체 사용`"
                    :aria-checked="transfer.enabled"
                    @click="toggleAutoTransfer(transfer.id)"
                  >
                    <span
                      class="absolute top-1 size-5 rounded-full bg-white shadow-[0_1px_4px_rgba(31,52,62,0.18)] transition-transform"
                      :class="transfer.enabled ? 'left-6' : 'left-1'"
                    ></span>
                  </button>
                </div>

                <strong
                  class="mt-3 block text-[18px] font-extrabold tracking-[-0.02em] text-[var(--color-text-primary)]"
                >
                  {{ formatWon(transfer.amount) }}
                </strong>

                <div
                  class="mt-3 flex items-end justify-between gap-3 border-t border-[#edf2f5] pt-3"
                >
                  <p
                    class="m-0 min-w-0 truncate text-[10px] font-medium text-[var(--color-text-secondary)]"
                  >
                    {{ transfer.sourceAccount }}
                    <span class="mx-1 text-[#a8b5bd]" aria-hidden="true">→</span>
                    {{ transfer.targetAccount }}
                  </p>
                  <button
                    class="flex h-7 shrink-0 items-center gap-1 rounded-lg bg-[#f1f8fc] px-2.5 text-[10px] font-bold text-[var(--color-selected-text)] active:bg-[#e5f4fb]"
                    type="button"
                    :aria-expanded="editingAutoTransferId === transfer.id"
                    @click="startEditingAutoTransfer(transfer)"
                  >
                    <Pencil :size="12" :stroke-width="2.2" aria-hidden="true" />
                    수정
                  </button>
                </div>
              </div>

              <form
                v-if="editingAutoTransferId === transfer.id"
                class="border-t border-[#e4edf2] bg-[#f7fbfd] px-4 py-4"
                @submit.prevent="saveAutoTransfer"
              >
                <div class="grid grid-cols-[minmax(0,1fr)_92px] gap-2.5">
                  <label class="min-w-0">
                    <span class="block text-[10px] font-bold text-[var(--color-text-secondary)]">
                      이체 금액
                    </span>
                    <span
                      class="mt-1.5 flex h-10 items-center rounded-[10px] border border-[#dce8ee] bg-white px-3 focus-within:border-[var(--color-brand-primary)]"
                    >
                      <input
                        v-model.number="editingAmount"
                        class="min-w-0 flex-1 bg-transparent text-[12px] font-bold outline-none"
                        type="number"
                        inputmode="numeric"
                        min="1"
                        step="1000"
                        aria-label="이체 금액"
                      />
                      <span class="ml-1 text-[11px] text-[var(--color-text-secondary)]">원</span>
                    </span>
                  </label>

                  <label>
                    <span class="block text-[10px] font-bold text-[var(--color-text-secondary)]">
                      이체일
                    </span>
                    <span
                      class="mt-1.5 flex h-10 items-center rounded-[10px] border border-[#dce8ee] bg-white px-3 focus-within:border-[var(--color-brand-primary)]"
                    >
                      <input
                        v-model.number="editingTransferDay"
                        class="min-w-0 flex-1 bg-transparent text-[12px] font-bold outline-none"
                        type="number"
                        inputmode="numeric"
                        min="1"
                        max="28"
                        aria-label="매월 이체일"
                      />
                      <span class="ml-1 text-[11px] text-[var(--color-text-secondary)]">일</span>
                    </span>
                  </label>
                </div>

                <p class="mt-2 mb-0 text-[9px] leading-relaxed text-[var(--color-text-secondary)]">
                  자동이체일은 매월 1일부터 28일 사이로 설정할 수 있어요.
                </p>

                <div class="mt-3 grid grid-cols-2 gap-2">
                  <button
                    class="h-9 rounded-[10px] border border-[#dce8ee] bg-white text-[11px] font-bold text-[var(--color-text-secondary)] active:bg-[#f2f6f8]"
                    type="button"
                    @click="cancelEditingAutoTransfer"
                  >
                    취소
                  </button>
                  <button
                    class="h-9 rounded-[10px] bg-[var(--color-brand-primary)] text-[11px] font-bold text-white active:bg-[var(--color-brand-primary-pressed)] disabled:bg-[#cad8df]"
                    type="submit"
                    :disabled="!isAutoTransferEditValid"
                  >
                    변경 저장
                  </button>
                </div>
              </form>
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
