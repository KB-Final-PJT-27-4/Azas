<script setup lang="ts">
import { CalendarDays, CheckCircle2, ChevronRight, Landmark, PiggyBank, Sparkles, TrendingUp } from 'lucide-vue-next'
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

type ReportTab = 'assets' | 'allowance'

type GoalReport = {
  id: number
  name: string
  targetAmount: number
  accounts: Array<{
    id: number
    name: string
    number: string
    balance: number
  }>
}

const route = useRoute()
const router = useRouter()
const activeTab = ref<ReportTab>(route.query.tab === 'allowance' ? 'allowance' : 'assets')
const activeGoalIndex = ref(0)
const goalCarousel = ref<HTMLElement | null>(null)
const goalCarouselHeight = ref<number | null>(null)

const setReportTab = (tab: ReportTab) => {
  activeTab.value = tab
  void router.replace({ query: tab === 'allowance' ? { tab } : {} })
}

const goalReports: GoalReport[] = [
  {
    id: 1,
    name: '대학자금',
    targetAmount: 30_000_000,
    accounts: [
      { id: 1, name: 'KB 아이사랑적금 1', number: '952-17362605-43', balance: 9_600_000 },
      { id: 2, name: 'KB 아이사랑적금 2', number: '952-17362605-57', balance: 5_000_000 },
    ],
  },
  {
    id: 2,
    name: '목돈 마련',
    targetAmount: 20_000_000,
    accounts: [
      { id: 3, name: 'KB Young Youth 적금', number: '952-17362605-68', balance: 6_150_000 },
    ],
  },
]

const currentAmount = (goal: GoalReport) =>
  goal.accounts.reduce((total, account) => total + account.balance, 0)
const achievementRate = (goal: GoalReport) =>
  Math.min((currentAmount(goal) / goal.targetAmount) * 100, 100)
const totalAssets = computed(() =>
  goalReports.reduce((total, goal) => total + currentAmount(goal), 0),
)
const totalTarget = computed(() =>
  goalReports.reduce((total, goal) => total + goal.targetAmount, 0),
)
const totalRate = computed(() => (totalAssets.value / totalTarget.value) * 100)
const formatWon = (amount: number) => `${amount.toLocaleString('ko-KR')}원`

const syncGoalCarouselHeight = () => {
  nextTick(() => {
    const activeCard = goalCarousel.value?.children[activeGoalIndex.value] as HTMLElement | undefined
    if (activeCard) goalCarouselHeight.value = activeCard.offsetHeight
  })
}

const updateActiveGoal = (event: Event) => {
  const carousel = event.currentTarget as HTMLElement
  if (!carousel.clientWidth) return
  const nextIndex = Math.min(
    Math.max(Math.round(carousel.scrollLeft / carousel.clientWidth), 0),
    goalReports.length - 1,
  )
  if (activeGoalIndex.value === nextIndex) return
  activeGoalIndex.value = nextIndex
  syncGoalCarouselHeight()
}

onMounted(syncGoalCarouselHeight)
</script>

<template>
  <main class="min-h-full text-[var(--color-text-primary)]">
    <nav
      class="sticky top-[var(--app-header-height)] z-10 grid grid-cols-2 border-b border-[var(--color-border)] bg-white"
      aria-label="리포트 종류"
    >
      <button
        class="relative h-12 text-sm font-semibold transition-colors"
        :class="
          activeTab === 'assets'
            ? 'text-[var(--color-selected-text)]'
            : 'text-[var(--color-text-secondary)]'
        "
        type="button"
        @click="setReportTab('assets')"
      >
        자산 리포트
        <span
          v-if="activeTab === 'assets'"
          class="absolute right-5 bottom-0 left-5 h-0.5 rounded-full bg-[var(--color-brand-primary)]"
        ></span>
      </button>
      <button
        class="relative h-12 text-sm font-semibold transition-colors"
        :class="
          activeTab === 'allowance'
            ? 'text-[var(--color-accent-yellow-text)]'
            : 'text-[var(--color-text-secondary)]'
        "
        type="button"
        @click="setReportTab('allowance')"
      >
        양육비 리포트
        <span
          v-if="activeTab === 'allowance'"
          class="absolute right-5 bottom-0 left-5 h-0.5 rounded-full bg-[var(--color-accent-yellow)]"
        ></span>
      </button>
    </nav>

    <div v-if="activeTab === 'assets'" class="px-[18px] py-5">
      <RouterLink
        :to="{ name: 'ReportAssetHistory' }"
        class="block rounded-[22px] border border-[#d9edf7] bg-[#eaf8ff] p-5 !text-[var(--color-text-primary)] transition-colors active:bg-[#ddf3fd]"
        aria-label="총 자산 상세 리포트 보기"
      >
        <div class="flex items-start justify-between gap-4">
          <div>
            <p class="text-sm font-semibold text-[var(--color-text-secondary)]">총 자산</p>
            <strong class="mt-2 block text-[28px] leading-none tracking-[-0.04em]">{{
              formatWon(totalAssets)
            }}</strong>
            <p class="mt-3 text-xs text-[var(--color-text-secondary)]">
              지난달보다
              <strong class="text-[var(--color-selected-text)]">350,000원</strong> 늘었어요
            </p>
          </div>
          <span class="grid size-11 place-items-center rounded-2xl bg-white/80 text-[var(--color-selected-text)]">
            <ChevronRight :size="23" :stroke-width="2.4" aria-hidden="true" />
          </span>
        </div>
        <div class="mt-5 h-2 overflow-hidden rounded-full bg-white/90">
          <div
            class="h-full rounded-full bg-[var(--color-brand-primary)]"
            :style="{ width: `${totalRate}%` }"
          ></div>
        </div>
        <div class="mt-2 flex justify-between text-xs text-[var(--color-text-secondary)]">
          <span>전체 목표 달성률</span><strong>{{ totalRate.toFixed(1) }}%</strong>
        </div>
      </RouterLink>

      <section
        class="mt-3 flex items-center justify-between rounded-[20px] border border-[var(--color-border)] bg-white p-5"
      >
        <div>
          <p class="text-sm font-bold">이번 달 저축</p>
          <strong class="mt-2 block text-[23px] text-[var(--color-selected-text)]"
            >+1,250,000원</strong
          >
          <p class="mt-1 text-xs text-[var(--color-text-secondary)]">
            이번 달 목표의 25%를 채웠어요
          </p>
        </div>
        <div class="flex h-16 items-end gap-2" aria-hidden="true">
          <span class="h-7 w-3 rounded-t-full bg-[#73cbd5]"></span>
          <span class="h-11 w-3 rounded-t-full bg-[#b7d6fa]"></span>
          <span class="h-15 w-3 rounded-t-full bg-[#91baf1]"></span>
        </div>
      </section>

      <section class="mt-7">
        <div>
          <div>
            <h1 class="text-[21px] font-extrabold tracking-[-0.03em]">목표별 달성률</h1>
            <p class="mt-1 text-xs text-[var(--color-text-secondary)]">
              연결된 적금별 잔액을 함께 확인해보세요.
            </p>
          </div>
        </div>

        <div
          ref="goalCarousel"
          class="mt-4 flex w-full items-start snap-x snap-mandatory overflow-x-auto overflow-y-hidden scroll-smooth transition-[height] duration-300 ease-out [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
          :style="goalCarouselHeight ? { height: `${goalCarouselHeight}px` } : undefined"
          @scroll.passive="updateActiveGoal"
        >
          <article
            v-for="goal in goalReports"
            :key="goal.id"
            class="w-full shrink-0 snap-center self-start overflow-hidden rounded-[22px] border border-[var(--color-border)] bg-white"
          >
            <div class="p-5">
              <h2 class="mt-1 text-xl font-extrabold">{{ goal.name }}</h2>
              <div class="mt-3 flex min-w-0 items-end justify-between gap-3">
                <p class="min-w-0 text-sm font-semibold tracking-[-0.02em]">
                  {{ formatWon(currentAmount(goal)) }}
                  <span class="font-normal text-[var(--color-text-secondary)]">
                    / {{ formatWon(goal.targetAmount) }}
                  </span>
                </p>
                <strong class="shrink-0 text-lg text-[var(--color-selected-text)]">
                  {{ achievementRate(goal).toFixed(1) }}%
                </strong>
              </div>
              <div class="mt-3 h-2 overflow-hidden rounded-full bg-[#eaf0f3]">
                <div
                  class="h-full rounded-full bg-[var(--color-brand-primary)] transition-[width] duration-500"
                  :style="{ width: `${achievementRate(goal)}%` }"
                ></div>
              </div>
            </div>

            <div class="border-t border-[#edf1f3] bg-[#fbfcfd] px-5 py-4">
              <div class="mb-3 flex items-center justify-between">
                <h3 class="text-sm font-bold">연결된 적금</h3>
                <span
                  class="rounded-full bg-[#eaf8ff] px-2.5 py-1 text-xs font-semibold text-[var(--color-selected-text)]"
                  >{{ goal.accounts.length }}개</span
                >
              </div>
              <ul
                class="divide-y divide-[#e8edf0] overflow-hidden rounded-2xl border border-[#e2e9ed] bg-white"
              >
                <li
                  v-for="account in goal.accounts"
                  :key="account.id"
                  class="flex items-center gap-3 px-4 py-3.5"
                >
                  <span
                    class="grid size-9 shrink-0 place-items-center rounded-full bg-[#fff5cf] text-[#e2a300]"
                  >
                    <Landmark :size="18" :stroke-width="2.2" aria-hidden="true" />
                  </span>
                  <span class="min-w-0 flex-1">
                    <strong class="block truncate text-sm">{{ account.name }}</strong>
                    <span class="mt-0.5 block text-xs text-[var(--color-text-secondary)]">{{
                      account.number
                    }}</span>
                  </span>
                  <strong class="shrink-0 text-sm text-[var(--color-selected-text)]">{{
                    formatWon(account.balance)
                  }}</strong>
                </li>
              </ul>
            </div>
          </article>
        </div>
        <div
          v-if="goalReports.length > 1"
          class="mt-3 flex justify-center gap-2"
          aria-label="목표 카드 위치"
        >
          <span
            v-for="(_, index) in goalReports"
            :key="index"
            class="size-2 rounded-full"
            :class="
              activeGoalIndex === index
                ? 'bg-[var(--color-brand-primary)]'
                : 'bg-[var(--color-border)]'
            "
          ></span>
        </div>
      </section>

      <section class="mt-7 pb-2">
        <div class="flex items-center gap-2">
          <h2 class="text-[21px] font-extrabold tracking-[-0.03em]">이번 달 인사이트</h2>
        </div>
        <div class="mt-4 grid gap-3">
          <article class="flex items-center gap-4 rounded-[18px] bg-[#eaf8ff] p-4"><TrendingUp class="shrink-0 text-[#ef6c8f]" :size="27" :stroke-width="2.2" /><div><strong class="text-sm">지난달보다 90,000원을 더 저축했어요.</strong><p class="mt-1 text-xs text-[var(--color-text-secondary)]">꾸준한 저축 흐름이 아주 좋아요.</p></div></article>
          <article class="flex items-center gap-4 rounded-[18px] bg-[#eaf8ff] p-4"><CheckCircle2 class="shrink-0 text-[var(--color-selected-text)]" :size="27" :stroke-width="2.2" /><div><strong class="text-sm">대학자금 목표의 절반에 가까워졌어요.</strong><p class="mt-1 text-xs text-[var(--color-text-secondary)]">현재 속도라면 계획대로 달성할 수 있어요.</p></div></article>
          <article class="flex items-center gap-4 rounded-[18px] bg-[#eaf8ff] p-4"><CalendarDays class="shrink-0 text-[#65bd73]" :size="27" :stroke-width="2.2" /><div><strong class="text-sm">목표 달성 시기를 4개월 앞당길 수 있어요.</strong><p class="mt-1 text-xs text-[var(--color-text-secondary)]">지금처럼 저축을 이어가 보세요.</p></div></article>
        </div>
      </section>
    </div>

    <ChildcareReportOverview v-else />
  </main>
</template>
