<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { Check, ChevronRight } from 'lucide-vue-next'

import homeBubbleUrl from '@/assets/images/accounts/complete-circle.png'
import homeDiamondUrl from '@/assets/images/accounts/complete-diamond.png'
import homeStarUrl from '@/assets/images/accounts/complete-star.png'
import homeMoneyUrl from '@/assets/images/home/money.png'
import homePigUrl from '@/assets/images/login/logo-pig.png'
import goalIconUrl from '@/assets/images/home/icon-goal.png'
import homeProductIconUrl from '@/assets/images/home/home-product.png'
import timeCapsuleIconUrl from '@/assets/images/home/icon-time-capsule.png'
import { productRecommendationGoal } from '@/data/productDummyData'
import { currentHomeMemberType, homeDataByMemberType } from '@/mocks/home'

const homeData = computed(() => homeDataByMemberType[currentHomeMemberType])
const goalSlides = computed(() => homeData.value.goals ?? [])
const currentAssetAmount = computed(() => goalSlides.value[0]?.currentAmount ?? 0)
const selectedGoalIndex = ref(0)
const goalCarouselRef = ref<HTMLElement | null>(null)
let goalCarouselTimer: ReturnType<typeof window.setInterval> | null = null
const formatCurrency = (amount: number) => `${amount.toLocaleString('ko-KR')}원`

const quickMenus = computed(() =>
  homeData.value.quickMenus.map((menu) => {
    if (menu.icon === 'checklist') {
      return {
        ...menu,
        title: '맞춤 금융상품',
        subtitle: '추천 받기',
        to: '/products',
      }
    }
    if (menu.icon === 'goal' && goalSlides.value.length > 0) {
      return {
        ...menu,
        subtitle: `${goalSlides.value.length}개 진행 중`,
        to: '/mypage/goals',
      }
    }
    return menu
  }),
)

const quickMenuIconUrls = {
  timeCapsule: timeCapsuleIconUrl,
  goal: goalIconUrl,
}

const checklistItems = computed(() => [
  { title: '아이 통장 준비하기', completed: currentHomeMemberType === 'existing' },
  { title: '우리 아이 저축 목표 세우기', completed: currentHomeMemberType === 'existing' },
  { title: '가족 금융 계획 점검하기', completed: false },
])

const checklistCompletedCount = computed(() => (currentHomeMemberType === 'existing' ? 3 : 0))

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

let previousHtmlBackground = ''
let previousBodyBackground = ''

onMounted(() => {
  startGoalCarousel()
  previousHtmlBackground = document.documentElement.style.backgroundColor
  previousBodyBackground = document.body.style.backgroundColor
  document.documentElement.style.backgroundColor = '#eef9ff'
  document.body.style.backgroundColor = '#eef9ff'
})

onBeforeUnmount(() => {
  stopGoalCarousel()
  document.documentElement.style.backgroundColor = previousHtmlBackground
  document.body.style.backgroundColor = previousBodyBackground
})
</script>

<template>
  <main
    class="home-shell min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] px-[18px] pt-4 pb-7 text-[var(--color-text-primary)]"
  >
    <section class="home-hero" aria-labelledby="home-title">
      <div class="home-hero-copy">
        <p class="m-0 text-[12px] font-medium text-[var(--color-text-secondary)]">
          우리 아이 자산관리 서비스
        </p>
        <h1
          id="home-title"
          class="mt-1.5 mb-0 text-[20px] leading-[1.22] font-extrabold tracking-[-0.04em]"
        >
          <span class="text-[var(--color-brand-primary)]">{{ homeData.childName }}</span
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
            <strong class="text-[var(--color-selected-text)]">+350,000원</strong>
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
            :to="homeData.goalCtaTo"
          >
            <span class="grid size-10 shrink-0 place-items-center rounded-full bg-[#eaf8ff]">
              <img
                class="size-7 object-contain"
                :src="productRecommendationGoal.icon"
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
        :to="homeData.goalCtaTo"
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

    <section class="mt-[16px]" aria-labelledby="home-checklist-title">
      <RouterLink
        to="/checklists"
        class="block overflow-hidden rounded-[20px] border border-[#dce8ee] bg-white !text-[var(--color-text-primary)] shadow-[0_6px_18px_rgba(64,106,126,0.04)] transition-transform active:scale-[0.99]"
      >
        <div class="px-5 pb-4 pt-5">
          <div class="flex items-end justify-between gap-3">
            <div>
              <span class="text-[11px] font-semibold text-[var(--color-text-secondary)]">
                체크리스트 현황
              </span>
              <p class="mt-1 text-[22px] font-extrabold leading-none">
                {{ checklistCompletedCount
                }}<span class="text-[14px] text-[var(--color-text-secondary)]"> / 6 완료</span>
              </p>
            </div>
            <span
              class="rounded-full bg-[#eaf8ff] px-3 py-1.5 text-[11px] font-bold text-[var(--color-selected-text)]"
            >
              {{ Math.round((checklistCompletedCount / 6) * 100) }}%
            </span>
          </div>

          <div class="mt-4 h-2 overflow-hidden rounded-full bg-[#e8eef2]">
            <span
              class="block h-full rounded-full bg-[var(--color-brand-primary)] transition-[width] duration-700"
              :style="{ width: `${(checklistCompletedCount / 6) * 100}%` }"
            />
          </div>
        </div>

        <ul class="m-0 list-none border-t border-[#e7eef2] px-5 py-2">
          <li
            v-for="item in checklistItems"
            :key="item.title"
            class="flex min-h-10 items-center gap-3 border-b border-[#edf2f5] py-2 last:border-b-0"
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
            <!-- <span
              v-if="!item.completed"
              class="text-[10px] font-semibold text-[var(--color-selected-text)]"
            >
              준비하기
            </span> -->
          </li>
        </ul>

        <div class="flex items-center justify-between bg-[#f8fbfd] px-5 py-3 text-[11px] font-bold">
          <span>남은 준비 항목 {{ 6 - checklistCompletedCount }}개</span>
          <span class="inline-flex items-center gap-1 text-[var(--color-selected-text)]">
            이어서 확인하기 <ChevronRight :size="14" />
          </span>
        </div>
      </RouterLink>
    </section>
  </main>
</template>

<style scoped>
.home-shell {
  background: linear-gradient(180deg, #eef9ff 0%, #f6fbfe 58%, #eef9ff 100%);
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
