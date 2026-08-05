<script setup lang="ts">
import { computed, ref } from 'vue'
import { Check, ChevronDown, Funnel, Landmark } from 'lucide-vue-next'

import cloudBackground from '@/assets/images/timeCapsules/preview-cloud-background.png'
import AssetTransferResultSheet from '@/components/assets/AssetTransferResultSheet.vue'
import AssetTransferSheet from '@/components/assets/AssetTransferSheet.vue'
import { assetGoals, assetSummary, assetTransactions } from '@/data/assetDummyData'

const activeGoalIndex = ref(0)
const activeAccountId = ref<number | null>(null)
const isTransferFilterOpen = ref(false)
const transferFilterRoot = ref<HTMLElement | null>(null)
const isTransferSheetOpen = ref(false)
const transferResult = ref<'success' | 'failure' | null>(null)
const activeGoal = computed(() => assetGoals[activeGoalIndex.value]!)
const filteredTransactions = computed(() =>
  assetTransactions.filter(
    ({ goalId, accountId }) =>
      goalId === activeGoal.value.id &&
      (activeAccountId.value === null || accountId === activeAccountId.value),
  ),
)
const activeAccountFilterLabel = computed(
  () =>
    activeGoal.value.accounts.find(({ id }) => id === activeAccountId.value)?.name ?? '전체 계좌',
)

const percentage = (current: number, target: number) =>
  target === 0 ? 0 : Math.min((current / target) * 100, 100)

const formatWon = (amount: number) => `${amount.toLocaleString('ko-KR')}원`

const selectGoal = (index: number) => {
  activeGoalIndex.value = index
  activeAccountId.value = null
  isTransferFilterOpen.value = false
}

const selectAccountFilter = (accountId: number | null) => {
  activeAccountId.value = accountId
  isTransferFilterOpen.value = false
}

const closeTransferFilterOnFocusOut = (event: FocusEvent) => {
  const nextTarget = event.relatedTarget as Node | null
  if (!nextTarget || !transferFilterRoot.value?.contains(nextTarget)) {
    isTransferFilterOpen.value = false
  }
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
    class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-[#eef9ff] bg-top bg-no-repeat px-[18px] pt-[18px] pb-6 text-[var(--color-text-primary)]"
    :style="{
      backgroundImage: `url(${cloudBackground})`,
      backgroundSize: 'auto 100dvh',
    }"
  >
    <section class="flex items-start justify-between gap-4">
      <div>
        <h1 class="m-0 text-[21px] leading-tight font-extrabold tracking-[-0.025em]">자산 관리</h1>
        <p class="mt-1.5 mb-0 text-[12px] text-[var(--color-text-secondary)]">
          목표별 자산과 연결된 계좌를 함께 관리해요.
        </p>
      </div>
      <button
        class="asset-transfer-button fixed z-40 grid size-[42px] place-items-center rounded-full bg-[#2babe8] text-[12px] font-bold text-white shadow-sm active:bg-[#159bd8]"
        type="button"
        @click="isTransferSheetOpen = true"
      >
        이체
      </button>
    </section>

    <section class="mt-[18px] rounded-[20px] bg-white px-4 py-[18px] shadow-[0_5px_20px_rgba(80,140,170,0.06)]">
      <h2 class="m-0 text-[14px] font-bold text-[var(--color-text-secondary)]">전체 목표 현황</h2>
      <p class="mt-2 mb-0 text-[21px] leading-tight font-extrabold">
        총 목표
        <strong class="text-[#2babe8]">{{ formatWon(assetSummary.totalTargetAmount) }}</strong>
      </p>
      <p class="mt-2.5 mb-0 text-[12px] text-[var(--color-text-secondary)]">
        현재 {{ formatWon(assetSummary.totalCurrentAmount) }} · 전체 달성률
        {{ percentage(assetSummary.totalCurrentAmount, assetSummary.totalTargetAmount).toFixed(1) }}%
      </p>
      <div class="mt-2 h-2 overflow-hidden rounded-full bg-[#edf1f4]" role="progressbar">
        <div
          class="h-full rounded-full bg-[#2babe8]"
          :style="{
            width: `${percentage(assetSummary.totalCurrentAmount, assetSummary.totalTargetAmount)}%`,
          }"
        ></div>
      </div>
    </section>

    <section class="mt-[19px]">
      <h2 class="m-0 text-[18px] font-extrabold">목표별 자산 현황</h2>

      <article class="mx-[5px] mt-[18px] rounded-[18px] border border-[#dce8ee] bg-white px-[15px] py-4 shadow-sm">
        <div class="flex items-center justify-between gap-3">
          <h3 class="m-0 truncate text-[20px] font-extrabold">{{ activeGoal.title }}</h3>
          <span
            class="shrink-0 rounded-full bg-[#e9f8ff] px-[18px] py-1.5 text-[11px] font-bold text-[#2babe8]"
          >
            {{ activeGoal.status }}
          </span>
        </div>
        <p class="mt-2.5 mb-0 text-[14px] font-extrabold">
          {{ formatWon(activeGoal.currentAmount) }}
          <span class="font-normal text-[var(--color-text-secondary)]">
            / 목표 {{ formatWon(activeGoal.targetAmount) }}
          </span>
        </p>
        <div class="mt-3 flex items-center gap-4">
          <div class="h-2 flex-1 overflow-hidden rounded-full bg-[#e4edf2]">
            <div
              class="h-full rounded-full bg-[#2babe8]"
              :style="{ width: `${percentage(activeGoal.currentAmount, activeGoal.targetAmount)}%` }"
            ></div>
          </div>
          <strong class="w-12 text-right text-[15px] text-[#2babe8]">
            {{ percentage(activeGoal.currentAmount, activeGoal.targetAmount).toFixed(1) }}%
          </strong>
        </div>

        <p class="mt-5 mb-2.5 text-[12px] font-medium">연결된 계좌 {{ activeGoal.accounts.length }}개</p>
        <ul
          v-if="activeGoal.accounts.length"
          class="m-0 overflow-hidden rounded-[16px] border border-[#dce8ee] p-0"
        >
          <li
            v-for="account in activeGoal.accounts"
            :key="account.id"
            class="[&+&]:border-t [&+&]:border-[#dce8ee]"
          >
            <div class="flex w-full items-center gap-2.5 bg-white px-3 py-2 text-left">
              <span
                class="grid size-6 shrink-0 place-items-center rounded-full border border-[#ffad20] text-[#ff9f00]"
                aria-hidden="true"
              >
                <Landmark :size="14" :stroke-width="2" />
              </span>
              <span class="min-w-0 flex-1">
                <span class="block truncate text-[10px] font-bold text-[#8c98a7]">{{ account.name }}</span>
                <strong class="block truncate text-[14px]">{{ account.accountNumber }}</strong>
              </span>
              <strong class="shrink-0 text-[12px] text-[#20a8eb]">{{ formatWon(account.balance) }}</strong>
            </div>
          </li>
        </ul>
        <p v-else class="m-0 rounded-xl bg-[#f6f9fb] px-4 py-5 text-center text-sm text-[#8c98a7]">
          연결된 계좌가 없어요.
        </p>
      </article>

      <div class="mt-2.5 flex justify-center gap-1.5" aria-label="목표 선택">
        <button
          v-for="(goal, index) in assetGoals"
          :key="goal.id"
          class="size-2 rounded-full"
          :class="index === activeGoalIndex ? 'bg-[#2babe8]' : 'bg-[#dce8ee]'"
          type="button"
          :aria-label="`${goal.title} 보기`"
          :aria-current="index === activeGoalIndex ? 'true' : undefined"
          @click="selectGoal(index)"
        ></button>
      </div>
    </section>

    <section class="mt-[18px]">
      <div class="flex items-center justify-between gap-3">
        <h2 class="m-0 text-[18px] font-extrabold">최근 이체 내역</h2>
        <div
          ref="transferFilterRoot"
          class="relative"
          @focusout="closeTransferFilterOnFocusOut"
        >
          <button
            class="flex h-8 max-w-[145px] items-center gap-1.5 rounded-full border border-[#dce8ee] bg-white px-3 text-[11px] font-bold text-[var(--color-text-secondary)] shadow-sm active:bg-[#f6f9fb]"
            type="button"
            aria-label="이체 내역 계좌 필터"
            :aria-expanded="isTransferFilterOpen"
            aria-haspopup="listbox"
            @click="isTransferFilterOpen = !isTransferFilterOpen"
          >
            <Funnel :size="13" :stroke-width="2.2" class="shrink-0" />
            <span class="truncate">{{ activeAccountFilterLabel }}</span>
            <ChevronDown
              :size="13"
              class="shrink-0 transition-transform"
              :class="isTransferFilterOpen ? 'rotate-180' : ''"
            />
          </button>

          <Transition
            enter-active-class="transition duration-150 ease-out"
            enter-from-class="-translate-y-1 opacity-0"
            leave-active-class="transition duration-100 ease-in"
            leave-to-class="-translate-y-1 opacity-0"
          >
            <ul
              v-if="isTransferFilterOpen"
              class="absolute top-[calc(100%+6px)] right-0 z-20 m-0 w-44 list-none rounded-[12px] border border-[#dce8ee] bg-white p-1.5 shadow-[0_10px_25px_rgba(45,77,94,0.16)]"
              role="listbox"
              aria-label="이체 내역 계좌 선택"
            >
              <li role="option" :aria-selected="activeAccountId === null">
                <button
                  class="flex w-full items-center justify-between rounded-lg px-3 py-2 text-left text-[12px] font-bold"
                  :class="activeAccountId === null ? 'bg-[#effaff] text-[#20a8eb]' : 'active:bg-[#f6f9fb]'"
                  type="button"
                  @click="selectAccountFilter(null)"
                >
                  전체 계좌
                  <Check v-if="activeAccountId === null" :size="14" :stroke-width="2.7" />
                </button>
              </li>
              <li
                v-for="account in activeGoal.accounts"
                :key="account.id"
                role="option"
                :aria-selected="activeAccountId === account.id"
              >
                <button
                  class="flex w-full items-center justify-between gap-2 rounded-lg px-3 py-2 text-left text-[12px] font-bold"
                  :class="activeAccountId === account.id ? 'bg-[#effaff] text-[#20a8eb]' : 'active:bg-[#f6f9fb]'"
                  type="button"
                  @click="selectAccountFilter(account.id)"
                >
                  <span class="truncate">{{ account.name }}</span>
                  <Check
                    v-if="activeAccountId === account.id"
                    :size="14"
                    :stroke-width="2.7"
                    class="shrink-0"
                  />
                </button>
              </li>
            </ul>
          </Transition>
        </div>
      </div>
      <ul class="mt-[18px] mb-0 grid list-none gap-2.5 p-0">
        <li v-for="transaction in filteredTransactions" :key="transaction.id">
          <RouterLink
            class="flex h-[55px] items-center justify-between gap-4 rounded-[16px] border border-[#dce8ee] bg-white px-4 py-2 !text-[var(--color-text-primary)] shadow-sm"
            :to="{ name: 'AssetDetail', params: { assetId: transaction.id } }"
          >
            <div class="min-w-0">
              <strong class="block truncate text-[12px]">{{ transaction.accountLabel }}</strong>
              <time class="mt-1 block text-[10px] text-[var(--color-text-secondary)]">
                {{ transaction.transactedAt }}
              </time>
            </div>
            <div class="min-w-0 text-right">
              <strong class="block text-[15px] text-[#20a8eb]">+{{ formatWon(transaction.amount) }}</strong>
              <span class="mt-0.5 block truncate text-[10px] text-[var(--color-text-secondary)]">
                {{ transaction.depositName }}
              </span>
            </div>
          </RouterLink>
        </li>
        <li
          v-if="filteredTransactions.length === 0"
          class="rounded-[16px] border border-[#dce8ee] bg-white px-4 py-6 text-center text-[12px] text-[var(--color-text-secondary)]"
        >
          이체 내역이 없어요.
        </li>
      </ul>
    </section>

    <AssetTransferSheet
      :open="isTransferSheetOpen"
      :target-account-name="activeGoal.accounts[0]?.name"
      :target-account-number="activeGoal.accounts[0]?.accountNumber"
      @close="isTransferSheetOpen = false"
      @transfer="completeTransfer"
    />
    <AssetTransferResultSheet
      v-if="transferResult"
      :open="true"
      :status="transferResult"
      @close="transferResult = null"
      @retry="retryTransfer"
    />
  </main>
</template>

<style scoped>
.asset-transfer-button {
  top: calc(var(--app-header-height) + env(safe-area-inset-top) + 18px);
  right: max(18px, calc((100vw - var(--app-max-width)) / 2 + 18px));
}
</style>
