<script setup lang="ts">
import { computed } from 'vue'
import { CheckSquare, ChevronRight, ClipboardList, Send, Shield, Trophy, Wallet } from 'lucide-vue-next'

import ChildBottomNavigation from '@/components/child/ChildBottomNavigation.vue'
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
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-5 pb-[104px]">
    <section class="rounded-[18px] bg-[#F0FAFE] px-5 py-5" aria-label="아이 자산 요약">
      <p class="m-0 text-[17px] leading-none font-bold text-[var(--color-text-primary)]">
        현재 사용 가능 금액
      </p>

      <div class="mt-5 flex items-end gap-2">
        <strong class="text-[34px] leading-none font-bold tracking-normal text-black">
          {{ formatNumber(childAccountSummary.balance) }}
        </strong>
        <span class="pb-[2px] text-[22px] leading-none font-bold text-black">원</span>
      </div>

      <div class="mt-5 flex gap-7 text-[15px] leading-none text-[var(--color-text-secondary)]">
        <span>이번 달 사용 {{ formatCurrency(childAccountSummary.monthlySpent) }}</span>
        <span>한도 {{ formatCurrency(childAccountSummary.monthlyLimit) }}</span>
      </div>

      <div class="mt-3 h-[7px] overflow-hidden rounded-full bg-[#d7edf9]">
        <div
          class="h-full rounded-full bg-[var(--color-brand-primary)]"
          :style="{ width: `${childAccountSummary.usageProgress}%` }"
        />
      </div>
    </section>

    <section class="mt-5 grid grid-cols-2 gap-3" aria-label="주요 금융 행동">
      <RouterLink
        class="flex h-[54px] items-center justify-center gap-2 rounded-[10px] bg-[var(--color-brand-primary)] text-[16px] font-bold !text-white no-underline shadow-[0_10px_22px_rgb(85_192_244_/_20%)]"
        to="/child/transfer"
      >
        <Send :size="21" :stroke-width="2.8" aria-hidden="true" />
        돈 보내기
      </RouterLink>

      <RouterLink
        class="flex h-[54px] items-center justify-center gap-2 rounded-[10px] border border-[var(--color-border)] bg-white text-[16px] font-bold text-[var(--color-text-primary)] no-underline shadow-[0_8px_18px_rgb(110_122_138_/_5%)]"
        to="/child/allowance"
      >
        <Wallet :size="21" :stroke-width="2.5" aria-hidden="true" />
        용돈 요청하기
      </RouterLink>
    </section>

    <section class="mt-8" aria-labelledby="child-quick-title">
      <h1
        id="child-quick-title"
        class="mb-4 text-[22px] leading-none font-bold text-[var(--color-text-primary)]"
      >
        지금 할 수 있어요
      </h1>

      <div class="grid grid-cols-3 gap-3">
        <RouterLink
          v-for="action in quickActions"
          :key="action.title"
          class="min-h-[106px] rounded-[12px] border border-[var(--color-border)] bg-white px-3 py-3.5 no-underline shadow-[0_8px_20px_rgb(110_122_138_/_6%)]"
          :to="action.to"
        >
          <div class="mb-3 grid size-9 place-items-center rounded-[10px]" :class="action.iconClass">
            <component :is="action.icon" :size="20" :stroke-width="2.4" />
          </div>
          <strong class="block text-[15px] leading-snug font-bold text-[var(--color-text-primary)]">
            {{ action.title }}
          </strong>
          <span class="mt-1.5 block text-[14px] leading-snug text-[var(--color-text-secondary)]">
            {{ action.description }}
          </span>
        </RouterLink>
      </div>
    </section>

    <section class="mt-8" aria-labelledby="child-mission-title">
      <div class="mb-4 flex items-center justify-between">
        <h2
          id="child-mission-title"
          class="m-0 text-[22px] leading-none font-bold text-[var(--color-text-primary)]"
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
        class="overflow-hidden rounded-[18px] border border-[var(--color-border)] bg-white shadow-[0_12px_28px_rgb(110_122_138_/_8%)]"
      >
        <article
          v-for="mission in visibleMissions"
          :key="mission.id"
          class="grid grid-cols-[44px_minmax(0,1fr)_auto] items-center gap-3 border-b border-[var(--color-border)] px-4 py-4 last:border-b-0"
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
              class="h-[34px] rounded-[10px] border border-[#bfeaff] bg-[#f7fdff] px-3 text-[14px] font-bold text-[var(--color-brand-primary)]"
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
      class="mt-6 flex items-center justify-between gap-4 rounded-[16px] bg-gradient-to-r from-[#f2fbff] to-[#fff7d7] px-4 py-4"
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
        class="grid h-11 shrink-0 place-items-center rounded-[13px] border border-[#cfeeff] bg-white px-4 text-[16px] font-bold text-[var(--color-brand-primary)] no-underline"
        to="/child/quiz"
      >
        퀴즈 풀러 가기
      </RouterLink>
    </section>

    <ChildBottomNavigation />
  </main>
</template>
