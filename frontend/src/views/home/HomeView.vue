<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { Check, ChevronLeft, ChevronRight, X } from 'lucide-vue-next'

import homeBubbleUrl from '@/assets/images/accounts/complete-circle.png'
import homeDiamondUrl from '@/assets/images/accounts/complete-diamond.png'
import homeStarUrl from '@/assets/images/accounts/complete-star.png'
import homeMoneyUrl from '@/assets/images/home/money.png'
import homePigUrl from '@/assets/images/login/logo-pig.png'
import goalIconUrl from '@/assets/images/home/icon-goal.png'
import homeProductIconUrl from '@/assets/images/home/home-product.png'
import timeCapsuleIconUrl from '@/assets/images/home/icon-time-capsule.png'
import { api, getApiErrorMessage } from '@/api'
import { requireAuthorizationHeader, resolveCurrentChildId } from '@/api/context'

type HomeGoal = {
  id: number
  tag: string
  currentAmount: number
  targetAmount: number
  progress: number
}

type HomeChecklistItem = {
  id: number
  title: string
  completed: boolean
}

type HomeGuideStep = {
  eyebrow: string
  title: string
  description: string
  target: 'goal' | 'checklist' | 'timeCapsule' | 'mypage'
}

const childName = ref('우리 아이')
const currentAssetAmount = ref(0)
const currentAssetChangeAmount = ref(0)
const goals = ref<HomeGoal[]>([])
const goalSlides = computed(() => goals.value)
const checklistItems = ref<HomeChecklistItem[]>([])
const checklistCompletedCount = ref(0)
const checklistTotalCount = ref(0)
const checklistProgress = ref(0)
const nearestTimeCapsuleDay = ref<number | null>(null)
const errorMessage = ref('')
const isLoading = ref(true)
const isGuideOpen = ref(false)
const guideStepIndex = ref(0)
const guideTargetRect = ref({ top: 0, left: 0, width: 0, height: 0 })
const guideTooltipStyle = ref<Record<string, string>>({})
const guideArrowPosition = ref<'top' | 'bottom'>('top')
const selectedGoalIndex = ref(0)
const goalCarouselRef = ref<HTMLElement | null>(null)
let goalCarouselTimer: ReturnType<typeof window.setInterval> | null = null
let guidePositionFrame: number | null = null
let previousBodyOverflow = ''
const formatCurrency = (amount: number) => `${amount.toLocaleString('ko-KR')}원`

const resolveChecklistStage = (
  birthStatus?: string,
  expectedBirthDate?: string,
  birthDate?: string,
) => {
  const normalizedBirthStatus = birthStatus?.toUpperCase()
  if (normalizedBirthStatus === 'EXPECTED') return 'PREGNANCY'

  const pregnancyDate = normalizedBirthStatus === 'BORN' ? birthDate : expectedBirthDate ?? birthDate
  if (!pregnancyDate) return undefined

  const dueDate = new Date(`${pregnancyDate}T00:00:00`)
  return !Number.isNaN(dueDate.getTime()) && dueDate >= new Date(new Date().setHours(0, 0, 0, 0))
    ? 'PREGNANCY'
    : undefined
}


const guideSteps = computed<HomeGuideStep[]>(() => [
  {
    eyebrow: '타임캡슐',
    title: '소중한 순간을 타임캡슐에 담아요',
    description: '아이에게 전하고 싶은 메시지와 추억을 저장하고 원하는 날에 함께 열어볼 수 있어요.',
    target: 'timeCapsule',
  },
  {
    eyebrow: '목표 설정',
    title: '아이와 함께 저축 목표를 세워요',
    description: '교육비, 여행, 선물처럼 함께 이루고 싶은 목표와 금액을 설정하고 진행 상황을 확인해요.',
    target: 'goal',
  },
  {
    eyebrow: '체크리스트',
    title: '필요한 준비를 하나씩 확인해요',
    description: '아이의 성장 시기에 맞춰 지금 챙겨야 할 금융 준비 항목을 확인하고 완료할 수 있어요.',
    target: 'checklist',
  },
  {
    eyebrow: '마이페이지',
    title: '가족과 함께 아이의 금융생활을 관리해요',
    description: '가족을 초대하면 함께 용돈을 보내고 미션을 만들어 아이의 즐거운 금융 습관을 키울 수 있어요.',
    target: 'mypage',
  },
])
const currentGuideStep = computed(() => guideSteps.value[guideStepIndex.value]!)
const shouldScrollGuideTarget = () => currentGuideStep.value.target === 'checklist'

const positionGuide = async (scrollTarget = false) => {
  await nextTick()
  const target = document.querySelector<HTMLElement>(
    `[data-home-guide="${currentGuideStep.value.target}"]`,
  )
  if (!target) return

  if (scrollTarget) {
    target.scrollIntoView({ behavior: 'smooth', block: 'center' })
    window.setTimeout(() => void positionGuide(), 320)
  }

  const rect = target.getBoundingClientRect()
  const padding = 6
  guideTargetRect.value = {
    top: rect.top - padding,
    left: rect.left - padding,
    width: rect.width + padding * 2,
    height: rect.height + padding * 2,
  }

  const tooltipWidth = Math.min(340, window.innerWidth - 32)
  const tooltipHeight = 205
  const gap = 28
  const showBelow = rect.bottom + gap + tooltipHeight < window.innerHeight
  guideArrowPosition.value = showBelow ? 'top' : 'bottom'
  const tooltipLeft = Math.min(
    Math.max(rect.left + rect.width / 2 - tooltipWidth / 2, 16),
    window.innerWidth - tooltipWidth - 16,
  )
  const arrowLeft = Math.min(
    Math.max(rect.left + rect.width / 2 - tooltipLeft, 22),
    tooltipWidth - 22,
  )
  guideTooltipStyle.value = {
    width: `${tooltipWidth}px`,
    left: `${tooltipLeft}px`,
    top: showBelow ? `${rect.bottom + gap}px` : 'auto',
    bottom: showBelow ? 'auto' : `${window.innerHeight - rect.top + gap}px`,
    '--guide-arrow-left': `${arrowLeft}px`,
  }
}
const updateGuidePosition = () => {
  if (!isGuideOpen.value || guidePositionFrame !== null) return

  guidePositionFrame = window.requestAnimationFrame(() => {
    guidePositionFrame = null
    void positionGuide()
  })
}

const openGuide = () => {
  if (!isGuideOpen.value) previousBodyOverflow = document.body.style.overflow
  guideStepIndex.value = 0
  isGuideOpen.value = true
  document.body.style.overflow = 'hidden'
  void positionGuide()
}
const closeGuide = () => {
  isGuideOpen.value = false
  if (guidePositionFrame !== null) {
    window.cancelAnimationFrame(guidePositionFrame)
    guidePositionFrame = null
  }
  document.body.style.overflow = previousBodyOverflow
}
const nextGuideStep = () => {
  if (guideStepIndex.value < guideSteps.value.length - 1) {
    guideStepIndex.value += 1
    void positionGuide(shouldScrollGuideTarget())
  }
  else closeGuide()
}
const previousGuideStep = () => {
  if (guideStepIndex.value > 0) {
    guideStepIndex.value -= 1
    void positionGuide()
  }
}

const quickMenus = computed(() => [
  { title: '맞춤 금융상품', subtitle: '추천 받기', icon: 'checklist', to: '/products' },
  {
    title: '타임캡슐',
    subtitle: nearestTimeCapsuleDay.value === null ? '만들러가기' : `D - ${nearestTimeCapsuleDay.value}`,
    icon: 'timeCapsule',
    to: '/time-capsules',
  },
  {
    title: '목표',
    subtitle: goalSlides.value.length ? `${goalSlides.value.length}개 진행 중` : '설정하기',
    icon: 'goal',
    to: goalSlides.value.length ? '/mypage/goals' : '/goals',
  },
])

const quickMenuIconUrls = {
  timeCapsule: timeCapsuleIconUrl,
  goal: goalIconUrl,
}

const toggleChecklistItem = async (item: HomeChecklistItem) => {
  const nextCompleted = !item.completed
  try {
    const authorization = requireAuthorizationHeader()
    await api.updateChecklistItemCompletionUsingPATCH(authorization, item.id, {
      completed: nextCompleted,
    } as never)
    item.completed = nextCompleted
    checklistCompletedCount.value += nextCompleted ? 1 : -1
    checklistProgress.value = checklistTotalCount.value
      ? Math.round((checklistCompletedCount.value / checklistTotalCount.value) * 100)
      : 0
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, '체크리스트를 변경하지 못했어요.')
  }
}

const selectGoal = (index: number, behavior: ScrollBehavior = 'smooth') => {
  const goalCount = goalSlides.value.length
  if (!goalCount) return

  const nextIndex = ((index % goalCount) + goalCount) % goalCount
  selectedGoalIndex.value = nextIndex
  const carousel = goalCarouselRef.value
  carousel?.scrollTo({ left: carousel.clientWidth * nextIndex, behavior })
}

const updateSelectedGoalByScroll = (event: Event) => {
  const carousel = event.currentTarget as HTMLElement
  if (!carousel.clientWidth) return

  selectedGoalIndex.value = Math.min(
    Math.max(Math.round(carousel.scrollLeft / carousel.clientWidth), 0),
    goalSlides.value.length - 1,
  )
}

const stopGoalCarousel = () => {
  if (goalCarouselTimer === null) return
  window.clearInterval(goalCarouselTimer)
  goalCarouselTimer = null
}

const startGoalCarousel = () => {
  stopGoalCarousel()
  if (goalSlides.value.length < 2) return

  goalCarouselTimer = window.setInterval(() => {
    selectGoal(selectedGoalIndex.value + 1)
  }, 4000)
}

const restartGoalCarousel = () => {
  startGoalCarousel()
}

const loadHome = async () => {
  isLoading.value = true
  try {
    errorMessage.value = ''
    const childId = await resolveCurrentChildId()
    const authorization = requireAuthorizationHeader()
    const childResponse = await api.getChildUsingGET(childId, authorization).catch(() => null)
    childName.value = childResponse?.data.name?.trim() || childName.value
    const checklistStage = resolveChecklistStage(
      childResponse?.data.birth_status,
      childResponse?.data.expected_birth_date,
      childResponse?.data.birth_date,
    )

    const [dashboardResponse, goalsResponse, checklistResponse] = await Promise.all([
      api.getDashboardUsingGET1(authorization, childId),
      api.getGoalsUsingGET(childId, authorization),
      api.getChecklistItemsUsingGET(authorization, childId, checklistStage),
    ])
    const dashboard = dashboardResponse.data
    childName.value = childResponse?.data.name?.trim() || dashboard.child?.name?.trim() || '우리 아이'
    currentAssetAmount.value = dashboard.asset_summary?.total_asset_amount ?? 0
    currentAssetChangeAmount.value = dashboard.asset_summary?.total_asset_change_amount ?? 0
    nearestTimeCapsuleDay.value = dashboard.quick_summary?.nearest_time_capsule?.dday ?? null
    goals.value = goalsResponse.data.financial_goals.map((goal) => ({
      id: goal.financial_goal_id ?? 0,
      tag: goal.title ?? '저축 목표',
      currentAmount: goal.current_amount ?? 0,
      targetAmount: goal.target_amount ?? 0,
      progress: goal.achievement_rate ?? 0,
    }))

    const checklist = checklistResponse.data
    checklistItems.value = ((checklist.items ?? []) as unknown as Array<{
      checklist_item_id?: number
      title?: string
      status?: string
      completed?: boolean
    }>).slice(0, 3).map((item) => ({
      id: item.checklist_item_id ?? 0,
      title: item.title ?? '준비 항목',
      completed: item.completed ?? item.status === 'COMPLETED',
    }))
    checklistCompletedCount.value = checklist.completed_count ?? 0
    checklistTotalCount.value = checklist.total_count ?? checklistItems.value.length
    checklistProgress.value = checklist.progress_percent ?? 0
    const guideStorageKey = 'azas_home_service_guide_seen_v1'
    if (localStorage.getItem(guideStorageKey) !== 'true') {
      openGuide()
      localStorage.setItem(guideStorageKey, 'true')
    }
    startGoalCarousel()
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, '홈 정보를 불러오지 못했어요.')
  } finally {
    isLoading.value = false
  }
}

let previousHtmlBackground = ''
let previousBodyBackground = ''

onMounted(() => {
  window.addEventListener('azas:open-home-guide', openGuide)
  window.addEventListener('resize', updateGuidePosition)
  window.addEventListener('scroll', updateGuidePosition, { passive: true })
  void loadHome()
  startGoalCarousel()
  previousHtmlBackground = document.documentElement.style.backgroundColor
  previousBodyBackground = document.body.style.backgroundColor
  document.documentElement.style.backgroundColor = '#eef9ff'
  document.body.style.backgroundColor = '#eef9ff'
})

onBeforeUnmount(() => {
  window.removeEventListener('azas:open-home-guide', openGuide)
  window.removeEventListener('resize', updateGuidePosition)
  window.removeEventListener('scroll', updateGuidePosition)
  if (guidePositionFrame !== null) window.cancelAnimationFrame(guidePositionFrame)
  stopGoalCarousel()
  if (isGuideOpen.value) document.body.style.overflow = previousBodyOverflow
  document.documentElement.style.backgroundColor = previousHtmlBackground
  document.body.style.backgroundColor = previousBodyBackground
})
</script>

<template>
  <main
    class="home-shell min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] px-[18px] pt-4 pb-7 text-[var(--color-text-primary)]"
  >
    <template v-if="isLoading">
      <section class="home-hero" aria-label="홈 정보 불러오는 중" aria-busy="true">
        <div class="home-hero-copy gap-2.5">
          <span class="home-skeleton block h-3 w-28 rounded-full"></span>
          <span class="home-skeleton block h-5 w-36 rounded-full"></span>
          <span class="home-skeleton block h-5 w-24 rounded-full"></span>
        </div>
        <div class="absolute right-2 bottom-1 size-[112px] rounded-full bg-white/35 p-3">
          <span class="home-skeleton block size-full rounded-full"></span>
        </div>
      </section>

      <section class="overflow-hidden rounded-[20px] border border-[#dbe8ef] bg-white/80">
        <div class="min-h-[138px] px-5 py-4">
          <span class="home-skeleton block h-3 w-12 rounded-full"></span>
          <span class="home-skeleton mt-3 block h-7 w-36 rounded-lg"></span>
          <span class="home-skeleton mt-3 block h-3 w-28 rounded-full"></span>
          <div class="mt-3 flex justify-end gap-2">
            <span class="home-skeleton block h-12 w-5 rounded-t-md"></span>
            <span class="home-skeleton block h-16 w-5 rounded-t-md"></span>
            <span class="home-skeleton block h-20 w-5 rounded-t-md"></span>
          </div>
        </div>
        <div class="flex min-h-[92px] items-center gap-3 border-t border-[#e4edf2] px-5 py-3.5">
          <span class="home-skeleton block size-10 shrink-0 rounded-full"></span>
          <span class="min-w-0 flex-1">
            <span class="home-skeleton block h-4 w-28 rounded-full"></span>
            <span class="home-skeleton mt-2 block h-3 w-40 max-w-full rounded-full"></span>
            <span class="home-skeleton mt-3 block h-1.5 w-full rounded-full"></span>
          </span>
        </div>
      </section>

      <section class="home-quick-grid" aria-label="빠른 메뉴 불러오는 중">
        <div
          v-for="index in 3"
          :key="index"
          class="grid min-h-[132px] place-items-center content-center rounded-[17px] border border-[#dfe9ef] bg-white px-3"
        >
          <span class="home-skeleton block size-9 rounded-xl"></span>
          <span class="home-skeleton mt-3 block h-3.5 w-16 rounded-full"></span>
          <span class="home-skeleton mt-2 block h-2.5 w-12 rounded-full"></span>
        </div>
      </section>

      <section class="mt-4 overflow-hidden rounded-[20px] border border-[#dce8ee] bg-white">
        <div class="px-5 pt-5 pb-4">
          <span class="home-skeleton block h-3 w-24 rounded-full"></span>
          <div class="mt-3 flex items-center justify-between">
            <span class="home-skeleton block h-6 w-28 rounded-md"></span>
            <span class="home-skeleton block h-7 w-12 rounded-full"></span>
          </div>
          <span class="home-skeleton mt-4 block h-2 w-full rounded-full"></span>
        </div>
        <div class="border-t border-[#e7eef2] px-5 py-2">
          <div v-for="index in 3" :key="index" class="flex min-h-14 items-center gap-3 border-b border-[#edf2f5] last:border-0">
            <span class="home-skeleton block size-4.5 shrink-0 rounded-full"></span>
            <span class="home-skeleton block h-3.5 rounded-full" :class="index === 2 ? 'w-32' : 'w-44'"></span>
          </div>
        </div>
      </section>
    </template>

    <template v-else>
    <section class="home-hero" aria-labelledby="home-title">
      <div class="home-hero-copy">
        <p class="m-0 text-[12px] font-medium text-[var(--color-text-secondary)]">
          우리 아이 자산관리 서비스
        </p>
        <h1
          id="home-title"
          class="mt-1.5 mb-0 text-[20px] leading-[1.22] font-extrabold tracking-[-0.04em]"
        >
          <span class="text-[var(--color-brand-primary)]">{{ childName }}</span
          >의 미래를<br />함께 준비해요
        </h1>
      </div>

      <div class="home-decoration" aria-hidden="true">
        <img class="home-hero-pig" :src="homePigUrl" alt="" />
        <img class="home-float home-float--money" :src="homeMoneyUrl" alt="" />
        <img class="home-float home-float--bubble" :src="homeBubbleUrl" alt="" />
        <img class="home-float home-float--bubble-right" :src="homeBubbleUrl" alt="" />
        <img class="home-float home-float--diamond" :src="homeDiamondUrl" alt="" />
        <img class="home-float home-float--star" :src="homeStarUrl" alt="" />
      </div>
    </section>

    <section class="home-asset-card" aria-label="미래자산 요약">
      <div class="home-asset-overview">
        <RouterLink class="home-asset-copy" :to="{ name: 'Assets' }">
          <span class="text-[13px] font-extrabold">현재</span>
          <strong class="mt-2 block text-[26px] leading-none tracking-[-0.045em]">
            {{ formatCurrency(currentAssetAmount) }}
          </strong>
          <p class="mt-3 mb-0 text-[11px] text-[var(--color-text-secondary)]">
            지난달보다
            <strong class="text-[var(--color-selected-text)]">{{ formatCurrency(currentAssetChangeAmount) }}</strong>
          </p>
        </RouterLink>

        <RouterLink class="home-asset-more" :to="{ name: 'Reports' }">
          자산 리포트 보기 <ChevronRight :size="11" :stroke-width="2.5" />
        </RouterLink>

        <RouterLink
          class="home-mini-chart-wrap"
          :to="{ name: 'Reports' }"
          aria-label="자산 변화 리포트 보기"
        >
          <svg class="home-mini-chart" width="148" height="91" viewBox="0 0 148 91">
            <defs>
              <linearGradient id="home-chart-bar" x1="0" x2="0" y1="0" y2="1">
                <stop offset="0%" stop-color="#4b9dff" />
                <stop offset="100%" stop-color="#9bd6ff" />
              </linearGradient>
            </defs>
            <g fill="url(#home-chart-bar)">
              <path class="home-chart-bar" d="M15 91V72a6 6 0 0 1 6-6h8a6 6 0 0 1 6 6v19Z" />
              <path
                class="home-chart-bar home-chart-bar--2"
                d="M47 91V61a6 6 0 0 1 6-6h8a6 6 0 0 1 6 6v30Z"
              />
              <path
                class="home-chart-bar home-chart-bar--3"
                d="M79 91V45a6 6 0 0 1 6-6h8a6 6 0 0 1 6 6v46Z"
              />
              <path
                class="home-chart-bar home-chart-bar--4"
                d="M111 91V27a6 6 0 0 1 6-6h8a6 6 0 0 1 6 6v64Z"
              />
            </g>
          </svg>
        </RouterLink>
      </div>

      <div
        v-if="goalSlides.length"
        class="home-goal-carousel"
        @pointerdown="stopGoalCarousel"
        @pointerup="restartGoalCarousel"
        @pointercancel="restartGoalCarousel"
        @mouseenter="stopGoalCarousel"
        @mouseleave="startGoalCarousel"
      >
        <div
          ref="goalCarouselRef"
          class="home-goal-track"
          @scroll.passive="updateSelectedGoalByScroll"
        >
          <RouterLink
            v-for="goal in goalSlides"
            :key="goal.id"
            class="home-goal-row"
            :to="{ name: 'MypageGoals' }"
          >
            <span class="grid size-10 shrink-0 place-items-center rounded-full bg-[#eaf8ff]">
              <img
                class="size-7 object-contain"
                :src="goalIconUrl"
                alt=""
                aria-hidden="true"
              />
            </span>
            <span class="min-w-0 flex-1">
              <strong class="block truncate text-[14px]">{{ goal.tag }}</strong>
              <span class="mt-1 flex min-w-0 items-end justify-between gap-2">
                <span class="truncate text-[10px] text-[var(--color-text-secondary)]">
                  {{ formatCurrency(goal.currentAmount) }} /
                  {{ formatCurrency(goal.targetAmount) }}
                </span>
                <strong class="shrink-0 text-[13px] text-[var(--color-selected-text)]">
                  {{ goal.progress }}%
                </strong>
              </span>
              <span class="mt-2 block h-1.5 overflow-hidden rounded-full bg-[#edf2f5]">
                <i
                  class="home-goal-progress block h-full rounded-full bg-[var(--color-brand-primary)] not-italic"
                  :style="{ width: `${goal.progress}%` }"
                ></i>
              </span>
            </span>
          </RouterLink>
        </div>

        <div v-if="goalSlides.length > 1" class="home-goal-dots" aria-label="목표 선택">
          <button
            v-for="(_, index) in goalSlides"
            :key="index"
            type="button"
            :class="{ 'is-active': selectedGoalIndex === index }"
            :aria-label="`${index + 1}번째 목표 보기`"
            :aria-current="selectedGoalIndex === index ? 'true' : undefined"
            @click="selectGoal(index)"
          ></button>
        </div>
      </div>

      <RouterLink
        v-else
        class="flex min-h-[78px] items-center justify-between gap-3 border-t border-[#e4edf2] bg-white px-5 py-3 !text-[var(--color-text-primary)]"
        :to="{ name: 'Goals' }"
      >
        <span>
          <strong class="block text-sm">첫 저축 목표를 만들어보세요</strong>
          <span class="mt-1 block text-[11px] text-[var(--color-text-secondary)]"
            >작은 목표부터 함께 시작해요.</span
          >
        </span>
        <ChevronRight class="text-[var(--color-text-secondary)]" :size="20" />
      </RouterLink>
    </section>

    <section class="home-quick-grid" aria-label="홈 빠른 메뉴">
      <RouterLink
        v-for="menu in quickMenus"
        :key="menu.title"
        class="home-quick-card"
        :data-home-guide="menu.icon === 'goal' ? 'goal' : menu.icon === 'timeCapsule' ? 'timeCapsule' : undefined"
        :to="menu.to"
      >
        <img
          v-if="menu.icon === 'checklist'"
          class="block size-[36px] object-contain"
          :src="homeProductIconUrl"
          alt=""
          aria-hidden="true"
        />
        <img
          v-else
          class="home-quick-icon"
          :src="quickMenuIconUrls[menu.icon as keyof typeof quickMenuIconUrls]"
          alt=""
          aria-hidden="true"
        />
        <strong class="mt-3 truncate text-[13px]">{{ menu.title }}</strong>
        <span class="mt-1 truncate text-[10px] text-[var(--color-text-secondary)]">
          {{ menu.subtitle }}
        </span>
      </RouterLink>
    </section>

    <section class="mt-[16px]" aria-labelledby="home-checklist-title" data-home-guide="checklist">
      <div
        class="block overflow-hidden rounded-[20px] border border-[#dce8ee] bg-white text-[var(--color-text-primary)] shadow-[0_6px_18px_rgba(64,106,126,0.04)]"
      >
        <div class="px-5 pb-4 pt-5">
          <div class="flex items-end justify-between gap-3">
            <div>
              <span class="text-[11px] font-semibold text-[var(--color-text-secondary)]">
                체크리스트 현황
              </span>
              <p class="mt-1 text-[22px] font-extrabold leading-none">
                {{ checklistCompletedCount
                }}<span class="text-[14px] text-[var(--color-text-secondary)]">
                  / {{ checklistTotalCount }} 완료</span
                >
              </p>
            </div>
            <span
              class="rounded-full bg-[#eaf8ff] px-3 py-1.5 text-[11px] font-bold text-[var(--color-selected-text)]"
            >
              {{ checklistProgress }}%
            </span>
          </div>

          <div class="mt-4 h-2 overflow-hidden rounded-full bg-[#e8eef2]">
            <span
              class="block h-full rounded-full bg-[var(--color-brand-primary)] transition-[width] duration-700"
              :style="{ width: `${checklistProgress}%` }"
            />
          </div>
        </div>

        <ul class="m-0 list-none border-t border-[#e7eef2] px-5 py-2">
          <li
            v-for="item in checklistItems"
            :key="item.id"
            class="border-b border-[#edf2f5] last:border-b-0"
          >
            <button
              class="flex min-h-10 w-full items-center gap-3 py-2 text-left transition-colors active:bg-[#f7fbfe]"
              type="button"
              :aria-pressed="item.completed"
              :aria-label="item.completed ? `${item.title} 완료 취소` : `${item.title} 완료`"
              @click="toggleChecklistItem(item)"
            >
              <span
                class="grid size-4.5 shrink-0 place-items-center rounded-full"
                :class="
                  item.completed
                    ? 'bg-[var(--color-brand-primary)] text-white'
                    : 'border-2 border-[#b8b8b8] bg-white'
                "
              >
                <Check v-if="item.completed" :size="12" :stroke-width="3" />
              </span>
              <span
                class="min-w-0 flex-1 truncate text-[12px] font-semibold"
                :class="item.completed ? 'text-[var(--color-text-secondary)] line-through' : ''"
              >
                {{ item.title }}
              </span>
            </button>
          </li>
        </ul>

        <RouterLink
          to="/checklists"
          class="flex items-center justify-between bg-[#f8fbfd] px-5 py-3 text-[11px] font-bold !text-[var(--color-text-primary)] transition-colors active:bg-[#eef5f8]"
        >
          <span>남은 준비 항목 {{ checklistTotalCount - checklistCompletedCount }}개</span>
          <span class="inline-flex items-center gap-1 text-[var(--color-selected-text)]">
            전체 확인하기 <ChevronRight :size="14" />
          </span>
        </RouterLink>
      </div>
    </section>
    </template>
  </main>

  <Teleport to="body">
    <Transition name="home-guide">
      <div
        v-if="isGuideOpen"
        class="pointer-events-none fixed inset-0 z-[calc(var(--z-index-overlay)+1)]"
        role="dialog"
        aria-modal="true"
        aria-labelledby="home-guide-title"
      >
        <div class="pointer-events-auto absolute inset-0 touch-none" aria-hidden="true" @click="closeGuide"></div>
        <div
          class="home-guide__spotlight absolute rounded-[20px]"
          :style="{
            top: `${guideTargetRect.top}px`,
            left: `${guideTargetRect.left}px`,
            width: `${guideTargetRect.width}px`,
            height: `${guideTargetRect.height}px`,
          }"
        ></div>

        <section
          class="home-guide__tooltip pointer-events-auto fixed rounded-[18px] bg-white px-5 pb-5 pt-4 shadow-[0_16px_42px_rgba(13,35,47,0.26)]"
          :class="`home-guide__tooltip--arrow-${guideArrowPosition}`"
          :style="guideTooltipStyle"
        >
            <div class="flex items-center justify-between">
              <span class="text-[12px] font-extrabold text-[var(--color-brand-primary)]">
                {{ guideStepIndex + 1 }}/{{ guideSteps.length }} · {{ currentGuideStep.eyebrow }}
              </span>
              <button
                class="-mr-2 ml-auto grid size-8 shrink-0 place-items-center rounded-full border-0 bg-transparent text-[#8b9aa2] active:bg-[#f0f4f6]"
                type="button"
                aria-label="사용 안내 닫기"
                @click="closeGuide"
              >
                <X :size="17" />
              </button>
            </div>
            <h2 id="home-guide-title" class="mt-3 text-[17px] font-extrabold tracking-[-0.03em]">
              {{ currentGuideStep.title }}
            </h2>
            <p class="mt-1.5 text-[12px] leading-5 text-[var(--color-text-secondary)]">
              {{ currentGuideStep.description }}
            </p>

            <div class="mt-4 flex justify-end gap-2">
              <button
                v-if="guideStepIndex > 0"
                class="grid size-9 shrink-0 place-items-center rounded-xl border border-[#dce7ed] bg-white text-[var(--color-text-secondary)]"
                type="button"
                aria-label="이전 안내"
                @click="previousGuideStep"
              >
                <ChevronLeft :size="22" />
              </button>
              <button
                class="min-h-9 rounded-xl border-0 bg-[var(--color-brand-primary)] px-5 text-[12px] font-extrabold text-white active:opacity-90"
                type="button"
                @click="nextGuideStep"
              >
                {{ guideStepIndex === guideSteps.length - 1 ? '시작하기' : '다음' }}
              </button>
            </div>
            <div
              class="pointer-events-none absolute bottom-[35px] left-1/2 flex -translate-x-1/2 gap-1.5"
              aria-hidden="true"
            >
              <span
                v-for="(_, index) in guideSteps"
                :key="index"
                class="size-1.5 rounded-full transition-colors"
                :class="index === guideStepIndex ? 'bg-[var(--color-brand-primary)]' : 'bg-[#dce5ea]'"
              />
            </div>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.home-guide-enter-active,
.home-guide-leave-active {
  transition: opacity 180ms ease;
}

.home-guide-enter-from,
.home-guide-leave-to {
  opacity: 0;
}

.home-guide__spotlight {
  z-index: 1;
  border: 2px solid rgb(255 255 255 / 92%);
  box-shadow: 0 0 0 9999px rgb(25 34 40 / 68%);
  pointer-events: none;
  transition: top 220ms ease, left 220ms ease, width 220ms ease, height 220ms ease;
}

.home-guide__tooltip {
  z-index: 2;
}

.home-guide__tooltip::before {
  position: absolute;
  left: var(--guide-arrow-left, 50%);
  width: 16px;
  height: 16px;
  background: #fff;
  content: '';
  transform: translateX(-50%) rotate(45deg);
}

.home-guide__tooltip--arrow-top::before {
  top: -7px;
}

.home-guide__tooltip--arrow-bottom::before {
  bottom: -7px;
}

.home-shell {
  background: linear-gradient(180deg, #eef9ff 0%, #f6fbfe 58%, #eef9ff 100%);
}

.home-skeleton {
  position: relative;
  overflow: hidden;
  background: #e6eef2;
}

.home-skeleton::after {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgb(255 255 255 / 72%), transparent);
  content: '';
  transform: translateX(-100%);
  animation: home-skeleton-shimmer 1.35s ease-in-out infinite;
}

@keyframes home-skeleton-shimmer {
  100% {
    transform: translateX(100%);
  }
}

@media (prefers-reduced-motion: reduce) {
  .home-skeleton::after {
    animation: none;
  }
}

.home-hero {
  position: relative;
  z-index: 2;
  min-height: 122px;
  overflow: visible;
}

.home-hero-copy {
  position: relative;
  z-index: 1;
  display: flex;
  width: 62%;
  min-height: 110px;
  flex-direction: column;
  justify-content: center;
  transform: translateY(-8px);
}

.home-decoration {
  position: absolute;
  top: 0;
  right: 10px;
  width: 210px;
  height: 122px;
  pointer-events: none;
}

.home-hero-pig {
  position: absolute;
  right: 0;
  bottom: -18px;
  z-index: 3;
  display: block;
  width: 150px;
  height: 100px;
  object-fit: contain;
  filter: drop-shadow(0 7px 10px rgb(241 134 160 / 15%));
}

.home-asset-card {
  position: relative;
  z-index: 1;
  overflow: hidden;
  border: 1px solid #dbe8ef;
  border-radius: 20px;
  background: rgb(255 255 255 / 78%);
  box-shadow: 0 8px 24px rgb(73 126 151 / 7%);
  backdrop-filter: blur(8px);
}

.home-asset-overview {
  position: relative;
  display: block;
  min-height: 138px;
  overflow: hidden;
  padding: 16px 20px 12px;
  color: var(--color-text-primary) !important;
}

.home-asset-copy {
  position: relative;
  z-index: 2;
  display: block;
  max-width: 60%;
  color: var(--color-text-primary) !important;
}

.home-asset-more {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 3;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgb(255 255 255 / 82%);
  color: var(--color-text-secondary);
  font-size: 10px;
  font-weight: 500;
}

.home-mini-chart-wrap {
  position: absolute;
  right: 10px;
  bottom: 0;
  z-index: 1;
  display: block;
  width: 148px;
  height: 91px;
  overflow: hidden;
}

.home-mini-chart {
  display: block;
  width: 148px !important;
  min-width: 148px;
  max-width: 148px;
  height: 91px !important;
  max-height: 91px;
  overflow: hidden;
}

.home-goal-carousel {
  position: relative;
  overflow: hidden;
  border-top: 1px solid #e4edf2;
  background: #fff;
}

.home-goal-track {
  display: flex;
  overflow-x: auto;
  overscroll-behavior-inline: contain;
  scroll-behavior: smooth;
  scroll-snap-type: x mandatory;
  scrollbar-width: none;
  touch-action: pan-x;
}

.home-goal-track::-webkit-scrollbar {
  display: none;
}

.home-goal-row {
  display: flex;
  min-width: 100%;
  min-height: 92px;
  flex: 0 0 100%;
  align-items: center;
  gap: 12px;
  padding: 14px 20px 26px;
  background: #fff;
  color: var(--color-text-primary) !important;
  scroll-snap-align: start;
  scroll-snap-stop: always;
}

.home-goal-dots {
  position: absolute;
  right: 0;
  bottom: 5px;
  left: 0;
  z-index: 2;
  display: flex;
  justify-content: center;
  gap: 5px;
  pointer-events: none;
}

.home-goal-dots button {
  width: 6px;
  height: 6px;
  padding: 0;
  border: 0;
  border-radius: 999px;
  background: #dce8ee;
  pointer-events: auto;
  transition: background-color 220ms ease;
}

.home-goal-dots button.is-active {
  background: var(--color-brand-primary);
}

.home-quick-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: 16px;
}

.home-quick-card {
  display: flex;
  min-width: 0;
  min-height: 132px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 8px;
  border: 1px solid #dfe9ef;
  border-radius: 17px;
  background: #fff;
  box-shadow: 0 5px 16px rgb(65 111 132 / 4.5%);
  color: var(--color-text-primary) !important;
  text-align: center;
  transition: transform 200ms ease;
}

.home-quick-card:active {
  transform: scale(0.98);
}

.home-quick-icon {
  display: block;
  width: 34px !important;
  min-width: 34px;
  max-width: 34px;
  height: 34px !important;
  min-height: 34px;
  max-height: 34px;
  object-fit: contain;
}

.home-float {
  position: absolute;
  z-index: 4;
  display: grid;
  place-items: center;
  filter: drop-shadow(0 2px 2px rgb(218 160 0 / 18%));
  animation: home-float 3.2s ease-in-out infinite;
  transform-origin: center;
}

.home-float--money {
  top: 5px;
  left: 115px;
  width: 35px !important;
  height: 33px !important;
  object-fit: contain;
  animation-delay: -0.7s;
}

.home-float--bubble {
  top: 34px;
  left: 70px;
  width: 11px !important;
  height: 11px !important;
  object-fit: contain;
  animation-delay: -1.8s;
  animation-duration: 2.8s;
}

.home-float--bubble-right {
  top: 82px;
  right: 1px;
  width: 11px !important;
  height: 11px !important;
  object-fit: contain;
  animation-delay: -0.35s;
  animation-duration: 3s;
}

.home-float--diamond {
  top: 30px;
  right: 5px;
  width: 31px !important;
  height: 31px !important;
  object-fit: contain;
  animation-delay: -1.1s;
  animation-duration: 3.7s;
}

.home-float--star {
  top: 65px;
  left: 38px;
  width: 33px !important;
  height: 33px !important;
  object-fit: contain;
  animation-delay: -2.2s;
  animation-duration: 3.4s;
}

@media (max-width: 360px) {
  .home-decoration {
    right: 2px;
    transform: scale(0.9);
    transform-origin: right bottom;
  }

  .home-hero-copy {
    width: 58%;
  }
}

.home-chart-bar {
  transform: scaleY(0);
  transform-box: fill-box;
  transform-origin: bottom center;
  animation: grow-home-bar 620ms cubic-bezier(0.22, 1, 0.36, 1) 160ms forwards;
}

.home-chart-bar--2 {
  animation-delay: 230ms;
}

.home-chart-bar--3 {
  animation-delay: 300ms;
}

.home-chart-bar--4 {
  animation-delay: 370ms;
}

.home-goal-progress {
  transform: scaleX(0);
  transform-origin: left center;
  animation: grow-home-progress 900ms cubic-bezier(0.22, 1, 0.36, 1) 280ms forwards;
}

@keyframes home-float {
  0%,
  100% {
    transform: translate3d(0, 0, 0) rotate(-3deg);
  }

  50% {
    transform: translate3d(2px, -7px, 0) rotate(5deg);
  }
}

@keyframes grow-home-bar {
  to {
    transform: scaleY(1);
  }
}

@keyframes grow-home-progress {
  to {
    transform: scaleX(1);
  }
}

@media (prefers-reduced-motion: reduce) {
  .home-float,
  .home-chart-bar,
  .home-goal-progress {
    animation: none;
    opacity: 1;
    stroke-dashoffset: 0;
    transform: none;
  }
}
</style>
