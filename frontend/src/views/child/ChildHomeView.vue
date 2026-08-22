<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ChevronRight } from 'lucide-vue-next'

import childHomePigUrl from '@/assets/images/child/child-home-pig.png'
import { api, getApiErrorMessage } from '@/api'
import { isChildQuizCompletedToday } from '@/utils/childQuizProgress'

const childAccountSummary = reactive({
  childName: '아이',
  balance: 0,
  monthlySpent: 0,
  monthlyLimit: 0,
  usageProgress: 0,
})
const pendingAllowanceCount = ref(0)
const recentTransactionCount = ref(0)
const errorMessage = ref('')
const hasCompletedTodayQuiz = ref(isChildQuizCompletedToday())
const showQuizCompletedModal = ref(false)
type ChildHomeMissionStatus = 'progress' | 'review' | 'completed'

const openQuizCompletedModal = () => {
  showQuizCompletedModal.value = true
}

const closeQuizCompletedModal = () => {
  showQuizCompletedModal.value = false
}

const quickActions = computed(() => [
  {
    title: '용돈 요청 내역',
    description: `승인 대기 ${pendingAllowanceCount.value}건`,
    to: '/child/allowance-requests',
  },
  {
    title: '최근 내역',
    description: `${recentTransactionCount.value}건 확인`,
    to: '/child/assets',
  },
])

const visibleMissions = ref<
  Array<{
    id: number
    title: string
    description: string
    reward: number
    status: ChildHomeMissionStatus
  }>
>([])

const resolveMissionStatus = (status?: string): ChildHomeMissionStatus => {
  if (status === 'APPROVED') return 'completed'
  if (status === 'SUBMITTED') return 'review'
  return 'progress'
}

const getMissionStatusLabel = (status: ChildHomeMissionStatus) => {
  if (status === 'completed') return '완료됨'
  if (status === 'review') return '승인 대기'
  return '진행 중'
}

const getMissionStatusBadgeClass = (status: ChildHomeMissionStatus) => {
  if (status === 'completed') return 'bg-[#eaf8ef] text-[#2f9b62]'
  if (status === 'review') return 'bg-[#fff7dd] text-[#c8951d]'
  return 'bg-[#eaf8ff] text-[var(--color-selected-text)]'
}

onMounted(async () => {
  try {
    const { data } = await api.getDashboardUsingGET()
    const spending = data.spending_summary
    childAccountSummary.childName = data.child?.name ?? '아이'
    childAccountSummary.balance = spending?.display_available_amount ?? 0
    childAccountSummary.monthlySpent = spending?.current_month_spent_amount ?? 0
    childAccountSummary.monthlyLimit = spending?.monthly_budget_amount ?? 0
    childAccountSummary.usageProgress = spending?.usage_rate ?? 0
    pendingAllowanceCount.value = data.activity_summary?.pending_allowance_request_count ?? 0
    recentTransactionCount.value = data.activity_summary?.current_month_transaction_count ?? 0
    visibleMissions.value = (data.mission_summary?.items ?? []).slice(0, 2).map((mission) => ({
      id: mission.mission_id ?? 0,
      title: mission.title ?? '용돈 미션',
      description: mission.description ?? '',
      reward: mission.reward_amount ?? 0,
      status: resolveMissionStatus(mission.status),
    }))
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, '자녀 홈 정보를 불러오지 못했습니다.')
  }
})

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
              <div class="flex min-w-0 items-center gap-2">
                <strong
                  class="min-w-0 truncate text-[16px] font-bold"
                  :class="
                    mission.status === 'completed'
                      ? 'text-[#7d8790]'
                      : 'text-[var(--color-text-primary)]'
                  "
                >
                  {{ mission.title }}
                </strong>
                <span
                  class="shrink-0 rounded-full px-2.5 py-1 text-[11px] leading-none font-bold"
                  :class="getMissionStatusBadgeClass(mission.status)"
                >
                  {{ getMissionStatusLabel(mission.status) }}
                </span>
              </div>
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
              class="home-mission-ticket__reward-amount font-extrabold"
              :class="
                mission.status === 'completed'
                  ? 'text-[#9aa4ad]'
                  : 'text-[var(--color-brand-primary)]'
              "
            >
              {{ formatCurrency(mission.reward) }}
            </strong>
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
      <button
        v-if="hasCompletedTodayQuiz"
        class="grid h-11 shrink-0 place-items-center rounded-[13px] border border-[#d8e7f0] bg-white px-4 text-[14px] font-bold text-[var(--color-brand-primary)] active:bg-[#fffdf4]"
        type="button"
        @click="openQuizCompletedModal"
      >
        완료 확인
      </button>
      <RouterLink
        v-else
        class="grid h-11 shrink-0 place-items-center rounded-[13px] border border-[#f0dfa1] bg-white px-4 text-[14px] font-bold text-[#9e7812] no-underline active:bg-[#fffdf4]"
        to="/child/quiz"
      >
        퀴즈 풀러 가기
      </RouterLink>
    </section>

    <Transition name="home-quiz-modal">
      <div
        v-if="showQuizCompletedModal"
        class="fixed inset-0 z-[60] grid place-items-center bg-black/35 px-6"
        role="dialog"
        aria-modal="true"
        aria-labelledby="home-quiz-completed-modal-title"
        @click.self="closeQuizCompletedModal"
      >
        <section
          class="home-quiz-modal-panel w-full max-w-[320px] rounded-[22px] bg-white px-6 py-7 text-center shadow-[0_18px_48px_rgba(22,45,61,0.18)]"
        >
          <h2
            id="home-quiz-completed-modal-title"
            class="m-0 text-[21px] font-extrabold text-[var(--color-text-primary)]"
          >
            오늘의 퀴즈를 다 풀었어요
          </h2>
          <p class="mt-3 mb-6 text-[15px] leading-[1.6] text-[var(--color-text-secondary)]">
            내일 다시 새로운 금융 퀴즈를 풀 수 있어요.
          </p>
          <button
            class="grid h-12 w-full place-items-center rounded-[14px] border-0 bg-[var(--color-brand-primary)] text-[16px] font-bold !text-white"
            type="button"
            @click="closeQuizCompletedModal"
          >
            확인
          </button>
        </section>
      </div>
    </Transition>

  </main>
</template>

<style scoped>
.home-mission-ticket {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(112px, 30%);
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
  gap: 0;
  min-width: 0;
  padding: 14px 10px;
}

.home-mission-ticket__reward-amount {
  font-size: clamp(14px, 4vw, 17px);
  line-height: 1;
  letter-spacing: 0;
  white-space: nowrap;
  word-break: keep-all;
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

.home-quiz-modal-enter-active,
.home-quiz-modal-leave-active {
  transition: opacity 180ms ease;
}

.home-quiz-modal-enter-active .home-quiz-modal-panel {
  animation: home-quiz-modal-wiggle 360ms cubic-bezier(0.2, 1, 0.3, 1) both;
}

.home-quiz-modal-leave-active .home-quiz-modal-panel {
  transition:
    opacity 160ms ease,
    transform 160ms ease;
}

.home-quiz-modal-enter-from,
.home-quiz-modal-leave-to {
  opacity: 0;
}

.home-quiz-modal-leave-to .home-quiz-modal-panel {
  opacity: 0;
  transform: translateY(10px) scale(0.97);
}

@keyframes home-quiz-modal-wiggle {
  0% {
    opacity: 0;
    transform: translateY(12px) scale(0.96) rotate(0deg);
  }
  45% {
    opacity: 1;
    transform: translateY(0) scale(1.01) rotate(-1.1deg);
  }
  65% {
    transform: translateY(0) scale(1) rotate(0.9deg);
  }
  82% {
    transform: translateY(0) scale(1) rotate(-0.45deg);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1) rotate(0deg);
  }
}
</style>
