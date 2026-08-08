<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { Check, ChevronDown, Funnel, Landmark, Plus } from 'lucide-vue-next'
import { useRoute } from 'vue-router'

import cloudBackground from '@/assets/images/timeCapsules/preview-cloud-background.png'
import AssetTransferResultSheet from '@/components/assets/AssetTransferResultSheet.vue'
import AssetTransferSheet from '@/components/assets/AssetTransferSheet.vue'
import { assetGoals, assetSummary, assetTransactions } from '@/data/assetDummyData'

const route = useRoute()
const loopedAssetGoals =
  assetGoals.length > 1 && assetGoals[0] ? [...assetGoals, assetGoals[0]] : assetGoals

const activeGoalIndex = ref(0)
const activeAccountId = ref<number | null>(null)
const isTransferFilterOpen = ref(false)
const transferFilterRoot = ref<HTMLElement | null>(null)
const goalCarouselRef = ref<HTMLElement | null>(null)
const isTransferSheetOpen = ref(false)
const transferResult = ref<'success' | 'failure' | null>(null)
const requestedTransferAmount = computed(() => Number(route.query.amount) || 0)
const requestedTransferMemo = computed(() => String(route.query.memo ?? ''))
const requestedTargetName = computed(() =>
  String(route.query.targetName ?? activeGoal.value.accounts[0]?.name ?? ''),
)
const requestedTargetNumber = computed(() =>
  String(route.query.targetNumber ?? activeGoal.value.accounts[0]?.accountNumber ?? ''),
)
const isAnyTransferSheetOpen = computed(
  () => isTransferSheetOpen.value || transferResult.value !== null,
)
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

let previousBodyOverflow = ''
let goalCarouselResetTimer: ReturnType<typeof window.setTimeout> | null = null

if (route.query.allowanceRequest) {
  isTransferSheetOpen.value = true
}

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
  if (goalCarouselResetTimer !== null) window.clearTimeout(goalCarouselResetTimer)
})

const percentage = (current: number, target: number) =>
  target === 0 ? 0 : Math.min((current / target) * 100, 100)

const formatWon = (amount: number) => `${amount.toLocaleString('ko-KR')}원`

const selectGoal = (index: number) => {
  activeGoalIndex.value = index
  activeAccountId.value = null
  isTransferFilterOpen.value = false
  goalCarouselRef.value?.scrollTo({
    left: goalCarouselRef.value.clientWidth * index,
    behavior: 'smooth',
  })
}

const updateActiveGoalByScroll = (event: Event) => {
  const target = event.currentTarget as HTMLElement
  if (!target.clientWidth) return

  const slideIndex = Math.min(
    Math.max(Math.round(target.scrollLeft / target.clientWidth), 0),
    loopedAssetGoals.length - 1,
  )
  const nextIndex = slideIndex % assetGoals.length

  if (nextIndex !== activeGoalIndex.value) {
    activeGoalIndex.value = nextIndex
    activeAccountId.value = null
    isTransferFilterOpen.value = false
  }

  if (goalCarouselResetTimer !== null) window.clearTimeout(goalCarouselResetTimer)
  goalCarouselResetTimer = window.setTimeout(() => {
    if (slideIndex !== assetGoals.length) return

    target.style.scrollBehavior = 'auto'
    target.scrollLeft = 0
    requestAnimationFrame(() => target.style.removeProperty('scroll-behavior'))
  }, 120)
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
    class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-[var(--color-selected-background)] bg-top bg-no-repeat px-[18px] pt-[18px] pb-6 text-[var(--color-text-primary)]"
    :style="{
      backgroundImage: `url(${cloudBackground})`,
      backgroundSize: 'auto 100dvh',
    }"
  >
    <section>
      <div>
        <h1 class="m-0 text-[21px] leading-tight font-extrabold tracking-[-0.025em]">자산 관리</h1>
        <p class="mt-1.5 mb-0 text-[12px] text-[var(--color-text-secondary)]">
          목표별 자산과 연결된 계좌를 함께 관리해요.
        </p>
      </div>
    </section>

    <section class="mt-[18px] rounded-[20px] bg-[var(--color-surface)] px-4 py-[18px] shadow-sm">
      <h2 class="m-0 text-[14px] font-bold text-[var(--color-text-secondary)]">전체 목표 현황</h2>
      <p class="mt-2 mb-0 text-[21px] leading-tight font-extrabold">
        총 목표
        <strong class="text-[var(--color-selected-text)]">
          {{ formatWon(assetSummary.totalTargetAmount) }}
        </strong>
      </p>
      <p class="mt-2.5 mb-0 text-[12px] text-[var(--color-text-secondary)]">
        현재 {{ formatWon(assetSummary.totalCurrentAmount) }} · 전체 달성률
        {{
          percentage(assetSummary.totalCurrentAmount, assetSummary.totalTargetAmount).toFixed(1)
        }}%
      </p>
      <div
        class="mt-2 h-2 overflow-hidden rounded-full bg-[var(--color-border)]"
        role="progressbar"
      >
        <div
          class="h-full rounded-full bg-[var(--color-brand-primary)]"
          :style="{
            width: `${percentage(assetSummary.totalCurrentAmount, assetSummary.totalTargetAmount)}%`,
          }"
        ></div>
      </div>
    </section>

    <section class="mt-[19px]">
      <h2 class="m-0 text-[18px] font-extrabold">목표별 자산 현황</h2>

      <div
        ref="goalCarouselRef"
        class="flex snap-x snap-mandatory overflow-x-auto scroll-smooth overscroll-x-contain [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
        @scroll.passive="updateActiveGoalByScroll"
      >
        <article
          v-for="(goal, slideIndex) in loopedAssetGoals"
          :key="`${goal.id}-${slideIndex}`"
          class="mx-[5px] mt-[18px] w-[calc(100%-10px)] flex-none snap-center rounded-[18px] border border-[var(--color-border)] bg-[var(--color-surface)] px-[15px] py-4 shadow-sm"
          :aria-hidden="slideIndex === assetGoals.length ? 'true' : undefined"
        >
          <div class="flex items-center justify-between gap-3">
            <h3 class="m-0 truncate text-[20px] font-extrabold">{{ goal.title }}</h3>
            <span
              class="shrink-0 rounded-full bg-[var(--color-selected-background)] px-[18px] py-1.5 text-[11px] font-bold text-[var(--color-selected-text)]"
            >
              {{ goal.status }}
            </span>
          </div>
          <p class="mt-2.5 mb-0 text-[14px] font-extrabold">
            {{ formatWon(goal.currentAmount) }}
            <span class="font-normal text-[var(--color-text-secondary)]">
              / 목표 {{ formatWon(goal.targetAmount) }}
            </span>
          </p>
          <div class="mt-3 flex items-center gap-4">
            <div class="h-2 flex-1 overflow-hidden rounded-full bg-[var(--color-border)]">
              <div
                class="h-full rounded-full bg-[var(--color-brand-primary)]"
                :style="{ width: `${percentage(goal.currentAmount, goal.targetAmount)}%` }"
              ></div>
            </div>
            <strong class="w-12 text-right text-[15px] text-[var(--color-selected-text)]">
              {{ percentage(goal.currentAmount, goal.targetAmount).toFixed(1) }}%
            </strong>
          </div>

          <p class="mt-5 mb-2.5 text-[12px] font-medium">
            연결된 계좌 {{ goal.accounts.length }}개
          </p>
          <ul
            v-if="goal.accounts.length"
            class="m-0 overflow-hidden rounded-[16px] border border-[var(--color-border)] p-0"
          >
            <li
              v-for="account in goal.accounts"
              :key="account.id"
              class="[&+&]:border-t [&+&]:border-[var(--color-border)]"
            >
              <div
                class="flex w-full items-center gap-2.5 bg-[var(--color-surface)] px-3 py-2 text-left"
              >
                <span
                  class="grid size-6 shrink-0 place-items-center rounded-full border border-[#ffad20] text-[#ff9f00]"
                  aria-hidden="true"
                >
                  <Landmark :size="14" :stroke-width="2" />
                </span>
                <span class="min-w-0 flex-1">
                  <span
                    class="block truncate text-[10px] font-bold text-[var(--color-text-secondary)]"
                    >{{ account.name }}</span
                  >
                  <strong class="block truncate text-[14px]">{{ account.accountNumber }}</strong>
                </span>
                <strong class="shrink-0 text-[12px] text-[var(--color-selected-text)]">{{
                  formatWon(account.balance)
                }}</strong>
              </div>
            </li>
          </ul>
          <p
            v-else
            class="m-0 rounded-xl bg-[var(--color-surface-muted)] px-4 py-5 text-center text-sm text-[var(--color-text-secondary)]"
          >
            연결된 계좌가 없어요.
          </p>
        </article>
      </div>

      <div class="mt-2.5 flex justify-center gap-1.5" aria-label="목표 선택">
        <button
          v-for="(goal, index) in assetGoals"
          :key="goal.id"
          class="size-2 rounded-full"
          :class="
            index === activeGoalIndex
              ? 'bg-[var(--color-brand-primary)]'
              : 'bg-[var(--color-border)]'
          "
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
        <div ref="transferFilterRoot" class="relative" @focusout="closeTransferFilterOnFocusOut">
          <button
            class="flex h-8 max-w-[145px] items-center gap-1.5 rounded-full border border-[var(--color-border)] bg-[var(--color-surface)] px-3 text-[11px] font-bold text-[var(--color-text-secondary)] shadow-sm active:bg-[var(--color-surface-muted)]"
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
              class="absolute top-[calc(100%+6px)] right-0 z-20 m-0 w-44 list-none rounded-[12px] border border-[var(--color-border)] bg-[var(--color-surface)] p-1.5 shadow-sm"
              role="listbox"
              aria-label="이체 내역 계좌 선택"
            >
              <li role="option" :aria-selected="activeAccountId === null">
                <button
                  class="flex w-full items-center justify-between rounded-lg px-3 py-2 text-left text-[12px] font-bold"
                  :class="
                    activeAccountId === null
                      ? 'bg-[var(--color-selected-background)] text-[var(--color-selected-text)]'
                      : 'active:bg-[var(--color-surface-muted)]'
                  "
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
                  :class="
                    activeAccountId === account.id
                      ? 'bg-[var(--color-selected-background)] text-[var(--color-selected-text)]'
                      : 'active:bg-[var(--color-surface-muted)]'
                  "
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
            class="flex h-[55px] items-center justify-between gap-4 rounded-[16px] border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-2 !text-[var(--color-text-primary)] shadow-sm"
            :to="{ name: 'AssetDetail', params: { assetId: transaction.id } }"
          >
            <div class="min-w-0">
              <strong class="block truncate text-[12px]">{{ transaction.accountLabel }}</strong>
              <time class="mt-1 block text-[10px] text-[var(--color-text-secondary)]">
                {{ transaction.transactedAt }}
              </time>
            </div>
            <div class="min-w-0 text-right">
              <strong class="block text-[15px] text-[var(--color-selected-text)]"
                >+{{ formatWon(transaction.amount) }}</strong
              >
              <span class="mt-0.5 block truncate text-[10px] text-[var(--color-text-secondary)]">
                {{ transaction.depositName }}
              </span>
            </div>
          </RouterLink>
        </li>
        <li
          v-if="filteredTransactions.length === 0"
          class="rounded-[16px] border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-6 text-center text-[12px] text-[var(--color-text-secondary)]"
        >
          이체 내역이 없어요.
        </li>
      </ul>
    </section>

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
