<script setup lang="ts">
import { computed } from 'vue'
import { CheckSquare, ChevronRight, ClipboardList, Send, Shield, Trophy, Wallet } from 'lucide-vue-next'

import ChildBottomNavigation from '@/components/child/ChildBottomNavigation.vue'
import childHomePigUrl from '@/assets/images/child/child-home-pig.png'
import { childAllowanceRequests } from '@/mocks/childFinanceFlow'
import { childAccountSummary } from '@/mocks/childHome'

const pendingAllowanceCount = computed(
  () => childAllowanceRequests.filter((request) => request.status === 'pending').length,
)

const quickActions = computed(() => [
  {
    title: '용돈 요청 내역',
    description: `승인 대기 ${pendingAllowanceCount.value}건`,
    to: '/child/allowance-requests',
    icon: ClipboardList,
    iconClass: 'bg-[#eef8ff] text-[var(--color-brand-primary)]',
  },
  {
    title: '최근 내역',
    description: '2건 진행 중',
    to: '/child/assets',
    icon: Trophy,
    iconClass: 'bg-[#fff7d7] text-[#c99a13]',
  },
  {
    title: '아이 체크리스트',
    description: '오늘 2/3 완료',
    to: '/child/checklists',
    icon: CheckSquare,
    iconClass: 'bg-[#eef8ff] text-[var(--color-brand-primary)]',
  },
])

const visibleMissions = [
  {
    id: 'mission-diary',
    title: '용돈기입장 작성하기',
    description: '이번 주 용돈기입장 쓰기',
    reward: 1_000,
    status: 'completed',
    icon: CheckSquare,
    iconClass: 'bg-[#eef8ff] text-[#358df7]',
  },
  {
    id: 'mission-spending-plan',
    title: '소비 계획 지키기',
    description: '이번 주 계획한 소비 지키기',
    reward: 2_000,
    status: 'progress',
    icon: Shield,
    iconClass: 'bg-[#fff7d7] text-[#8a6b13]',
  },
]

const formatNumber = (amount: number) => Math.abs(amount).toLocaleString('ko-KR')
const formatCurrency = (amount: number) => `${formatNumber(amount)}원`
const remainingLimit = computed(() =>
  Math.max(childAccountSummary.monthlyLimit - childAccountSummary.monthlySpent, 0),
)
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-[#FAFAF8] px-[18px] pt-3 pb-[112px]">
    <section
      class="relative overflow-hidden rounded-[26px] border border-[#dceef6] bg-[#eaf8ff] px-5 pt-5 pb-5 shadow-[0_10px_30px_rgba(54,112,139,0.08)]"
      aria-label="아이 자산 요약"
    >
      <div class="relative z-[1] max-w-[64%]">
        <p class="m-0 text-[13px] font-semibold text-[#628096]">
          {{ childAccountSummary.childName }}의 사용 가능 금액
        </p>
        <div class="mt-2 flex items-end gap-1">
          <strong class="text-[32px] leading-none font-extrabold tracking-[-0.035em] text-[var(--color-text-primary)]">
            {{ formatNumber(childAccountSummary.balance) }}
          </strong>
          <span class="pb-0.5 text-[18px] leading-none font-bold text-[var(--color-text-primary)]">원</span>
        </div>
        <span class="mt-3 inline-flex rounded-full bg-white/85 px-3 py-1.5 text-[12px] font-bold text-[var(--color-selected-text)]">
          {{ childAccountSummary.accountName }}
        </span>
      </div>

      <img
        class="pointer-events-none absolute top-2 right-0 w-[152px] translate-x-4 select-none object-contain"
        :src="childHomePigUrl"
        alt=""
        aria-hidden="true"
      />

      <div class="relative z-[1] mt-5 rounded-[18px] bg-white/90 px-4 py-3.5 backdrop-blur-sm">
        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="m-0 text-[12px] text-[var(--color-text-secondary)]">이번 달 사용</p>
            <strong class="mt-0.5 block text-[15px] text-[var(--color-text-primary)]">
              {{ formatCurrency(childAccountSummary.monthlySpent) }}
              <span class="font-medium text-[var(--color-text-secondary)]">/ {{ formatCurrency(childAccountSummary.monthlyLimit) }}</span>
            </strong>
          </div>
          <span class="shrink-0 text-[12px] font-bold text-[var(--color-selected-text)]">
            {{ formatCurrency(remainingLimit) }} 남음
          </span>
        </div>
        <div class="mt-3 h-2 overflow-hidden rounded-full bg-[#e3edf2]">
          <div
            class="h-full rounded-full bg-[var(--color-brand-primary)] transition-[width] duration-500"
            :style="{ width: `${Math.min(childAccountSummary.usageProgress, 100)}%` }"
          />
        </div>
      </div>
    </section>

    <section class="mt-4 grid grid-cols-2 gap-3" aria-label="주요 금융 행동">
      <RouterLink
        class="flex h-[56px] items-center justify-center gap-2 rounded-[16px] bg-[var(--color-brand-primary)] text-[15px] font-bold !text-white no-underline transition-transform active:scale-[0.98]"
        to="/child/transfer"
      >
        <Send :size="21" :stroke-width="2.8" aria-hidden="true" />
        돈 보내기
      </RouterLink>

      <RouterLink
        class="flex h-[56px] items-center justify-center gap-2 rounded-[16px] border border-[#dce8ee] bg-white text-[15px] font-bold text-[var(--color-text-primary)] no-underline transition-transform active:scale-[0.98]"
        to="/child/allowance"
      >
        <Wallet :size="21" :stroke-width="2.5" aria-hidden="true" />
        용돈 요청하기
      </RouterLink>
    </section>

    <section class="mt-8" aria-labelledby="child-quick-title">
      <h1
        id="child-quick-title"
        class="mb-4 text-[21px] leading-none font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]"
      >
        지금 할 수 있어요
      </h1>

      <div class="overflow-hidden rounded-[20px] border border-[#e1eaee] bg-white">
        <RouterLink
          v-for="action in quickActions"
          :key="action.title"
          class="grid min-h-[76px] grid-cols-[44px_minmax(0,1fr)_20px] items-center gap-3 border-b border-[#edf1f3] px-4 py-3 no-underline last:border-b-0 active:bg-[#f8fbfc]"
          :to="action.to"
        >
          <div class="grid size-11 place-items-center rounded-[14px]" :class="action.iconClass">
            <component :is="action.icon" :size="22" :stroke-width="2.4" />
          </div>
          <div class="min-w-0">
            <strong class="block truncate text-[15px] leading-snug font-bold text-[var(--color-text-primary)]">{{ action.title }}</strong>
            <span class="mt-1 block truncate text-[13px] leading-snug text-[var(--color-text-secondary)]">{{ action.description }}</span>
          </div>
          <ChevronRight :size="19" :stroke-width="2.4" class="text-[#9caab4]" aria-hidden="true" />
        </RouterLink>
      </div>
    </section>

    <section class="mt-8" aria-labelledby="child-mission-title">
      <div class="mb-4 flex items-center justify-between">
        <h2
          id="child-mission-title"
          class="m-0 text-[21px] leading-none font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]"
        >
          용돈 미션
        </h2>
        <RouterLink
          class="flex items-center gap-1 text-[15px] font-semibold text-[var(--color-text-secondary)] no-underline"
          to="/child/missions"
        >
          더보기
          <ChevronRight :size="17" :stroke-width="2.7" aria-hidden="true" />
        </RouterLink>
      </div>

      <div
        class="overflow-hidden rounded-[20px] border border-[#e1eaee] bg-white"
      >
        <article
          v-for="mission in visibleMissions"
          :key="mission.id"
          class="grid grid-cols-[44px_minmax(0,1fr)_auto] items-center gap-3 border-b border-[#edf1f3] px-4 py-4 last:border-b-0"
        >
          <div class="grid size-10 place-items-center rounded-[12px]" :class="mission.iconClass">
            <component :is="mission.icon" :size="20" :stroke-width="2.4" aria-hidden="true" />
          </div>
          <div class="min-w-0">
            <strong class="block truncate text-[16px] font-bold text-[var(--color-text-primary)]">
              {{ mission.title }}
            </strong>
            <span class="mt-1 block truncate text-[14px] text-[var(--color-text-secondary)]">
              {{ mission.description }}
            </span>
          </div>
          <div class="grid justify-items-end gap-2">
            <strong class="text-[14px] font-bold text-[var(--color-brand-primary)]">
              {{ formatCurrency(mission.reward) }}
            </strong>
            <button
              v-if="mission.status === 'completed'"
              class="h-[34px] rounded-[10px] border-0 bg-[#e9f8ff] px-3 text-[13px] font-bold text-[var(--color-selected-text)] active:bg-[#d9f2fd]"
              type="button"
            >
              완료 요청하기
            </button>
            <span v-else class="text-[14px] font-bold text-[var(--color-text-secondary)]">
              진행 중
            </span>
          </div>
        </article>
      </div>
    </section>

    <section
      class="mt-6 flex items-center justify-between gap-4 rounded-[20px] border border-[#f1e5b8] bg-[#fff9df] px-4 py-4"
      aria-label="오늘의 금융 퀴즈"
    >
      <div>
        <h2 class="m-0 text-[18px] font-bold text-[var(--color-text-primary)]">
          오늘의 퀴즈
        </h2>
        <p class="mt-1 mb-0 text-[15px] text-[var(--color-text-secondary)]">
          돈을 불리려면 무엇이 필요할까요?
        </p>
      </div>
      <RouterLink
        class="grid h-11 shrink-0 place-items-center rounded-[13px] border border-[#f0dfa1] bg-white px-4 text-[14px] font-bold text-[#9e7812] no-underline active:bg-[#fffdf4]"
        to="/child/quiz"
      >
        퀴즈 풀러 가기
      </RouterLink>
    </section>

    <ChildBottomNavigation />
  </main>
</template>
