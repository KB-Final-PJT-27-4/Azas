<script setup lang="ts">
import { CalendarDays, ChevronRight, EllipsisVertical, Plus } from 'lucide-vue-next'
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import goalEducationIcon from '@/assets/images/goals/goals_1.png'
import goalLumpSumIcon from '@/assets/images/goals/goal-lump-sum.png'
import goalCloudBackground from '@/assets/images/home/home-hero-bg.png'
import { useToast } from '@/composables/useToast'
import { linkedAssetAccounts } from '@/data/assetDummyData'

interface ManagedGoal {
  id: number
  name: string
  targetAmount: number
  targetDate: string
  accountIds: string[]
}

const router = useRouter()
const { showToast } = useToast()
const openMenuId = ref<number | null>(null)
let previousHtmlBackground = ''
let previousBodyBackground = ''
const goals = ref<ManagedGoal[]>([
  {
    id: 1,
    name: '대학자금',
    targetAmount: 30_000_000,
    targetDate: '2045-03',
    accountIds: ['parent-saving-1', 'parent-saving-2'],
  },
  {
    id: 2,
    name: '독립자금',
    targetAmount: 10_000_000,
    targetDate: '2045-03',
    accountIds: ['child-saving-1'],
  },
])

const connectedAccounts = (goal: ManagedGoal) =>
  linkedAssetAccounts.filter(({ id }) => goal.accountIds.includes(id))
const currentAmount = (goal: ManagedGoal) =>
  connectedAccounts(goal).reduce((total, account) => total + account.balance, 0)
const achievementRate = (goal: ManagedGoal) =>
  Math.min((currentAmount(goal) / goal.targetAmount) * 100, 100)
const remainingAmount = (goal: ManagedGoal) => Math.max(goal.targetAmount - currentAmount(goal), 0)

const formatAmount = (amount: number) => `${amount.toLocaleString('ko-KR')}원`
const formatRate = (rate: number) => rate.toFixed(1).replace('.0', '')
const formatTargetDate = (targetDate: string) => {
  const [year, month] = targetDate.split('-')
  return `${year}년 ${Number(month)}월`
}

const editGoal = (goalId: number) => {
  openMenuId.value = null
  router.push({ name: 'MypageGoalEdit', params: { goalsId: goalId } })
}

const toggleGoalMenu = (goalId: number) => {
  openMenuId.value = openMenuId.value === goalId ? null : goalId
}

const deleteGoal = (goalId: number) => {
  goals.value = goals.value.filter((goal) => goal.id !== goalId)
  openMenuId.value = null
  showToast('목표를 삭제했어요.', 'success')
}

const addGoal = () => {
  router.push({ name: 'Goals' })
}

const linkGoalAccount = (goalId: number) => {
  router.push({ name: 'Goals', query: { linkGoal: goalId } })
}

onMounted(() => {
  previousHtmlBackground = document.documentElement.style.backgroundColor
  previousBodyBackground = document.body.style.backgroundColor
  document.documentElement.style.backgroundColor = '#eef9fe'
  document.body.style.backgroundColor = '#eef9fe'
})

onBeforeUnmount(() => {
  document.documentElement.style.backgroundColor = previousHtmlBackground
  document.body.style.backgroundColor = previousBodyBackground
})
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-[#eef9fe] bg-cover bg-top bg-no-repeat px-[18px] pt-5 pb-10 text-[var(--color-text-primary)]"
    :style="{
      backgroundImage: `linear-gradient(rgba(247, 250, 252, 0.28), rgba(247, 250, 252, 0.42)), url(${goalCloudBackground})`,
    }"
    @click="openMenuId = null"
  >
    <section aria-labelledby="registered-goals-title">
      <div class="flex items-end justify-between gap-4">
        <div>
          <h2 id="registered-goals-title" class="m-0 text-[20px] font-extrabold tracking-[-0.03em]">
            나의 목표
          </h2>
          <p class="mt-1 mb-0 text-[11px] text-[var(--color-text-secondary)]">
            목표별 연결 계좌와 저축 현황이에요.
          </p>
        </div>
        <span
          class="rounded-full bg-[#eaf8ff] px-2.5 py-1 text-[11px] font-bold text-[var(--color-selected-text)]"
        >
          {{ goals.length }}개
        </span>
      </div>

      <div v-if="goals.length" class="mt-4 grid gap-4">
        <article
          v-for="goal in goals"
          :key="goal.id"
          class="relative rounded-[22px] border border-[var(--color-border)] bg-white shadow-[0_5px_18px_rgba(45,76,92,0.04)]"
        >
          <div class="p-5">
            <div class="flex items-start justify-between gap-3">
              <div class="flex min-w-0 items-center gap-3">
                <span
                  class="grid size-10 shrink-0 place-items-center rounded-[14px]"
                  :class="
                    goal.id === 1
                      ? 'bg-[#eaf8ff] text-[var(--color-selected-text)]'
                      : 'bg-[var(--color-accent-yellow-surface)] text-[var(--color-accent-yellow-text)]'
                  "
                  aria-hidden="true"
                >
                  <img
                    class="size-8 object-contain"
                    :src="goal.id === 1 ? goalEducationIcon : goalLumpSumIcon"
                    alt=""
                  />
                </span>
                <div class="min-w-0">
                  <h3 class="m-0 truncate text-[17px] font-extrabold">
                    {{ goal.name }}
                  </h3>
                </div>
              </div>

              <div class="relative shrink-0">
                <button
                  class="grid size-8 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[var(--color-surface-muted)]"
                  type="button"
                  :aria-label="`${goal.name} 관리 메뉴`"
                  :aria-expanded="openMenuId === goal.id"
                  aria-haspopup="menu"
                  @click.stop="toggleGoalMenu(goal.id)"
                >
                  <EllipsisVertical :size="19" :stroke-width="2.3" aria-hidden="true" />
                </button>

                <Transition
                  enter-active-class="transition duration-150 ease-out"
                  enter-from-class="-translate-y-1 opacity-0"
                  leave-active-class="transition duration-100 ease-in"
                  leave-to-class="-translate-y-1 opacity-0"
                >
                  <div
                    v-if="openMenuId === goal.id"
                    class="absolute top-[calc(100%+4px)] right-0 z-20 w-[116px] overflow-hidden rounded-[12px] border border-[var(--color-border)] bg-white p-1.5 shadow-[0_10px_28px_rgba(45,77,94,0.16)]"
                    role="menu"
                    @click.stop
                  >
                    <button
                      class="h-9 w-full rounded-[8px] px-3 text-left text-[11px] font-bold active:bg-[var(--color-surface-muted)]"
                      type="button"
                      role="menuitem"
                      @click="editGoal(goal.id)"
                    >
                      목표 수정
                    </button>
                    <button
                      class="h-9 w-full rounded-[8px] px-3 text-left text-[11px] font-bold text-[var(--color-danger)] active:bg-[var(--color-danger-background)]"
                      type="button"
                      role="menuitem"
                      @click="deleteGoal(goal.id)"
                    >
                      목표 삭제
                    </button>
                  </div>
                </Transition>
              </div>
            </div>

            <div class="mt-5 flex items-end justify-between gap-3">
              <div class="min-w-0">
                <span class="block text-[10px] font-semibold text-[var(--color-text-secondary)]">
                  현재 모은 금액
                </span>
                <strong class="mt-1 block text-[21px] tracking-[-0.03em]">
                  {{ formatAmount(currentAmount(goal)) }}
                </strong>
              </div>
              <strong class="shrink-0 text-[18px] text-[var(--color-selected-text)]">
                {{ formatRate(achievementRate(goal)) }}%
              </strong>
            </div>

            <div class="mt-3 h-2 overflow-hidden rounded-full bg-[#eaf0f3]">
              <div
                class="h-full rounded-full bg-[var(--color-brand-primary)] transition-[width] duration-500"
                :style="{ width: `${achievementRate(goal)}%` }"
              ></div>
            </div>

            <div class="mt-3 flex items-center justify-between gap-3 text-[11px]">
              <span class="inline-flex items-center gap-1 text-[var(--color-text-secondary)]">
                <CalendarDays :size="13" :stroke-width="2.1" aria-hidden="true" />
                {{ formatTargetDate(goal.targetDate) }}까지
              </span>
              <span class="text-[var(--color-text-secondary)]">
                목표까지
                <strong class="text-[var(--color-text-primary)]">
                  {{ formatAmount(remainingAmount(goal)) }}
                </strong>
              </span>
            </div>
          </div>

          <section
            class="rounded-b-[22px] border-t border-[#edf1f3] bg-[#fbfcfd] px-4 py-4"
            :aria-labelledby="`goal-${goal.id}-accounts-title`"
          >
            <div class="flex items-center justify-between gap-3">
              <h4 :id="`goal-${goal.id}-accounts-title`" class="m-0 text-[12px] font-bold">
                연결된 계좌
              </h4>
              <span class="text-[10px] font-semibold text-[var(--color-text-secondary)]">
                {{ connectedAccounts(goal).length }}개
              </span>
            </div>

            <ul v-if="connectedAccounts(goal).length" class="mt-3 mb-0 grid list-none gap-2 p-0">
              <li v-for="account in connectedAccounts(goal)" :key="account.id">
                <RouterLink
                  class="grid min-h-[62px] grid-cols-[minmax(0,1fr)_auto] items-center gap-2.5 rounded-[14px] border border-[#e4ebef] bg-white px-4 py-2.5 !text-[var(--color-text-primary)] shadow-[0_2px_7px_rgba(54,112,139,0.025)] active:bg-[#f7fbfd]"
                  :to="{
                    name: 'AssetDetail',
                    params: { assetId: account.id },
                    query: { from: 'goals' },
                  }"
                  :aria-label="`${account.name} 계좌 상세 보기`"
                >
                  <span class="min-w-0">
                    <strong class="block truncate text-[12px]">{{ account.name }}</strong>
                    <span
                      class="mt-0.5 block truncate text-[9px] text-[var(--color-text-secondary)]"
                    >
                      {{ account.bankName }} · {{ account.accountNumber }}
                    </span>
                  </span>
                  <span class="flex shrink-0 items-center gap-1">
                    <strong class="text-[14px] tracking-[-0.02em]">{{
                      formatAmount(account.balance)
                    }}</strong>
                    <ChevronRight
                      class="text-[#91a1ad]"
                      :size="15"
                      :stroke-width="2.3"
                      aria-hidden="true"
                    />
                  </span>
                </RouterLink>
              </li>
            </ul>

            <div
              v-else
              class="mt-3 rounded-[16px] border border-dashed border-[#cfe7f2] bg-[#f6fcff] px-4 py-5 text-center"
            >
              <strong class="mt-3 block text-[13px]">연결된 적금이 없어요.</strong>
              <p class="mt-1 mb-0 text-[10px] leading-4 text-[var(--color-text-secondary)]">
                이 목표와 함께 모을 적금 계좌를 연결해보세요.
              </p>
              <button
                class="mt-4 inline-flex h-9 items-center justify-center gap-1 rounded-[11px] border border-[#cdebf9] bg-white px-4 text-[11px] font-bold text-[var(--color-selected-text)] active:bg-[#edf9ff]"
                type="button"
                @click="linkGoalAccount(goal.id)"
              >
                <Plus :size="13" :stroke-width="2.6" aria-hidden="true" />
                적금 연결하기
              </button>
            </div>
          </section>
        </article>
      </div>

      <div
        v-else
        class="mt-4 rounded-[22px] border border-[#d9edf7] bg-white px-6 py-10 text-center shadow-[0_6px_20px_rgba(45,76,92,0.05)]"
      >
        <strong class="mt-4 block text-[16px]">아직 설정된 목표가 없어요.</strong>
        <p class="mt-2 mb-0 text-[11px] leading-5 text-[var(--color-text-secondary)]">
          아이의 미래를 위한 첫 번째 저축 목표를<br />지금 만들어보세요.
        </p>
        <button
          class="mx-auto mt-5 flex h-11 items-center justify-center gap-1.5 rounded-[13px] bg-[var(--color-brand-primary)] px-6 text-[12px] font-bold text-white shadow-[0_4px_12px_rgba(43,171,232,0.14)] active:bg-[var(--color-brand-primary-pressed)]"
          type="button"
          @click="addGoal"
        >
          <Plus :size="15" :stroke-width="2.6" aria-hidden="true" />
          첫 목표 만들기
        </button>
      </div>
    </section>

    <button
      v-if="goals.length"
      class="mt-5 flex h-13 w-full items-center justify-center gap-1.5 rounded-[15px] bg-[var(--color-brand-primary)] text-[14px] font-bold text-white shadow-[0_5px_14px_rgba(43,171,232,0.16)] active:bg-[var(--color-brand-primary-pressed)]"
      type="button"
      @click="addGoal"
    >
      <Plus :size="17" :stroke-width="2.6" aria-hidden="true" />
      새 목표 추가하기
    </button>
  </main>
</template>
