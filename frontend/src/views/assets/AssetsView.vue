<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { Baby, ChevronRight, Plus, UserRound } from 'lucide-vue-next'
import { useRoute } from 'vue-router'

import AssetTransferResultSheet from '@/components/assets/AssetTransferResultSheet.vue'
import AssetTransferSheet from '@/components/assets/AssetTransferSheet.vue'

type AccountType = '적금' | '입출금'

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

const route = useRoute()

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
        우리 가족의 연결된 계좌를 한눈에 확인하고 관리해요.
      </p>
    </header>

    <div class="mt-4 grid gap-4">
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
                  class="text-[15px] font-extrabold tracking-[-0.02em] text-[var(--color-selected-text)]"
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

    <button
      class="asset-transfer-button group fixed z-[60] h-10 w-20 rounded-t-full bg-[var(--color-surface)]/80 shadow-sm"
      type="button"
      aria-label="이체하기"
      @click="isTransferSheetOpen = true"
    >
      <span
        class="absolute right-1 bottom-0 left-1 grid h-9 place-items-center rounded-t-full bg-[var(--color-brand-primary)] pt-1 text-[var(--color-text-inverse)] transition-colors group-active:bg-[var(--color-brand-primary-pressed)]"
      >
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
  bottom: calc(var(--app-bottom-nav-height) + env(safe-area-inset-bottom) - 1px);
  left: 50%;
  transform: translateX(-50%);
}
</style>
