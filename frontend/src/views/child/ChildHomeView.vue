<script setup lang="ts">
import { computed, ref } from 'vue'
import { ChevronRight } from 'lucide-vue-next'

import ChildBottomNavigation from '@/components/child/ChildBottomNavigation.vue'
import childHomePigUrl from '@/assets/images/child/child-home-pig.png'
import { childAllowanceRequests } from '@/mocks/childFinanceFlow'
import { childAccountSummary } from '@/mocks/childHome'
import { isChildQuizCompletedToday } from '@/utils/childQuizProgress'

const pendingAllowanceCount = computed(
  () => childAllowanceRequests.filter((request) => request.status === 'pending').length,
)
const hasCompletedTodayQuiz = ref(isChildQuizCompletedToday())

const quickActions = computed(() => [
  {
    title: '용돈 요청 내역',
    description: `승인 대기 ${pendingAllowanceCount.value}건`,
    to: '/child/allowance-requests',
  },
  {
    title: '최근 내역',
    description: '2건 진행 중',
    to: '/child/assets',
  },
])

const visibleMissions = [
  {
    id: 'mission-diary',
    title: '용돈기입장 작성하기',
    description: '이번 주 용돈기입장 쓰기',
    reward: 1_000,
    status: 'completed',
  },
  {
    id: 'mission-spending-plan',
    title: '소비 계획 지키기',
    description: '이번 주 계획한 소비 지키기',
    reward: 2_000,
    status: 'progress',
  },
]

const formatNumber = (amount: number) => Math.abs(amount).toLocaleString('ko-KR')
const formatCurrency = (amount: number) => `${formatNumber(amount)}원`
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-[#eef9ff] px-[18px] pt-3 pb-[112px]"
  >
    <section class="relative overflow-visible pt-5" aria-label="아이 자산 요약">
      <div class="relative z-[2] max-w-[58%] px-2">
        <p class="m-0 text-[13px] font-semibold text-[#628096]">
          {{ childAccountSummary.childName }}의 사용 가능 금액
        </p>
        <div class="mt-2 flex items-end gap-1">
          <strong
            class="text-[32px] leading-none font-extrabold tracking-[-0.035em] text-[var(--color-text-primary)]"
          >
            {{ formatNumber(childAccountSummary.balance) }}
          </strong>
          <span class="pb-0.5 text-[18px] leading-none font-bold text-[var(--color-text-primary)]"
            >원</span
          >
        </div>
      </div>

      <img
        class="pointer-events-none absolute top-[20px] right-3 z-[3] w-[166px] select-none object-contain drop-shadow-[0_10px_18px_rgba(255,154,181,0.18)]"
        :src="childHomePigUrl"
        alt=""
        aria-hidden="true"
      />

      <div
        class="relative z-[1] mt-9 rounded-[22px] bg-white px-4 pt-5 pb-4 shadow-[0_8px_24px_rgba(54,112,139,0.06)]"
      >
        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="m-0 text-[15px] font-bold text-[var(--color-text-primary)]">이번 달 사용</p>
            <strong class="mt-0.5 block text-[15px] text-[var(--color-text-primary)]">
              {{ formatCurrency(childAccountSummary.monthlySpent) }}
              <span class="font-medium text-[var(--color-text-secondary)]"
                >/ {{ formatCurrency(childAccountSummary.monthlyLimit) }}</span
              >
            </strong>
          </div>
        </div>
        <div class="mt-3 h-2 overflow-hidden rounded-full bg-[#e3edf2]">
          <div
            class="h-full rounded-full bg-[var(--color-brand-primary)] transition-[width] duration-500"
            :style="{ width: `${Math.min(childAccountSummary.usageProgress, 100)}%` }"
          />
        </div>
      </div>
    </section>

    <section class="mt-8" aria-label="아이 빠른 메뉴">
      <div
        class="overflow-hidden rounded-[20px] border border-[#dce8ee] bg-white shadow-[0_8px_24px_rgba(54,112,139,0.06)]"
      >
        <RouterLink
          v-for="action in quickActions"
          :key="action.title"
          class="grid min-h-[72px] grid-cols-[minmax(0,1fr)_20px] items-center gap-3 border-b border-[#edf1f3] px-5 py-3 no-underline last:border-b-0 active:bg-[#f8fbfc]"
          :to="action.to"
        >
          <div class="min-w-0">
            <strong
              class="block truncate text-[15px] leading-snug font-bold text-[var(--color-text-primary)]"
              >{{ action.title }}</strong
            >
            <span
              class="mt-1 block truncate text-[13px] leading-snug text-[var(--color-text-secondary)]"
              >{{ action.description }}</span
            >
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
          class="flex items-center gap-0.5 text-[13px] font-semibold text-[var(--color-text-secondary)] no-underline"
          to="/child/missions"
        >
          더보기
          <ChevronRight :size="15" :stroke-width="2.7" aria-hidden="true" />
        </RouterLink>
      </div>

      <div class="grid gap-3">
        <article
          v-for="mission in visibleMissions"
          :key="mission.id"
          class="home-mission-ticket"
          :class="
            mission.status === 'completed'
              ? 'home-mission-ticket--completed'
              : 'home-mission-ticket--active'
          "
        >
          <div class="home-mission-ticket__content">
            <div class="min-w-0">
              <strong
                class="block truncate text-[16px] font-bold"
                :class="
                  mission.status === 'completed'
                    ? 'text-[#7d8790]'
                    : 'text-[var(--color-text-primary)]'
                "
              >
                {{ mission.title }}
              </strong>
              <span
                class="mt-1 block truncate text-[13px]"
                :class="
                  mission.status === 'completed'
                    ? 'text-[#9aa4ad]'
                    : 'text-[var(--color-text-secondary)]'
                "
              >
                {{ mission.description }}
              </span>
            </div>
          </div>

          <div class="home-mission-ticket__reward">
            <strong
              class="text-[17px] leading-tight font-extrabold"
              :class="
                mission.status === 'completed'
                  ? 'text-[#9aa4ad]'
                  : 'text-[var(--color-brand-primary)]'
              "
            >
              {{ formatCurrency(mission.reward) }}
            </strong>
            <span
              class="text-[12px] leading-none font-bold"
              :class="
                mission.status === 'completed'
                  ? 'text-[#77828c]'
                  : 'text-[var(--color-text-secondary)]'
              "
            >
              {{ mission.status === 'completed' ? '완료됨' : '진행 중' }}
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
        <h2 class="m-0 text-[18px] font-bold text-[var(--color-text-primary)]">오늘의 퀴즈</h2>
        <p class="mt-1 mb-0 text-[15px] text-[var(--color-text-secondary)]">
          {{
            hasCompletedTodayQuiz
              ? '오늘의 퀴즈를 풀었어요'
              : '짧은 금융 퀴즈로 습관을 배워봐요'
          }}
        </p>
      </div>
      <RouterLink
        class="grid h-11 shrink-0 place-items-center rounded-[13px] border px-4 text-[14px] font-bold no-underline active:bg-[#fffdf4]"
        :class="
          hasCompletedTodayQuiz
            ? 'border-[#d8e7f0] bg-white text-[var(--color-brand-primary)]'
            : 'border-[#f0dfa1] bg-white text-[#9e7812]'
        "
        to="/child/quiz"
      >
        {{ hasCompletedTodayQuiz ? '완료 확인' : '퀴즈 풀러 가기' }}
      </RouterLink>
    </section>

    <ChildBottomNavigation />
  </main>
</template>

<style scoped>
.home-mission-ticket {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 88px;
  min-height: 86px;
  overflow: hidden;
  border: 1px solid #e1eaee;
  border-radius: 18px;
  background: white;
}

.home-mission-ticket__content {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  align-items: center;
  min-width: 0;
  padding: 16px 18px;
}

.home-mission-ticket__reward {
  position: relative;
  display: grid;
  align-content: center;
  justify-items: center;
  gap: 8px;
  min-width: 0;
  padding: 14px 10px;
}

.home-mission-ticket__reward::before {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  width: 1px;
  content: '';
  border-left: 1px dashed #cfd9df;
}

.home-mission-ticket--completed {
  border-color: #e5e9ed;
  background: #f5f7f8;
  opacity: 0.78;
}
</style>
