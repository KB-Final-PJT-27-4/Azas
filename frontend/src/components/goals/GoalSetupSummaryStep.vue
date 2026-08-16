<script setup lang="ts">
import { Landmark } from 'lucide-vue-next'
import { ref } from 'vue'

import completeCircleUrl from '@/assets/images/accounts/complete-circle.png'
import completeDiamondUrl from '@/assets/images/accounts/complete-diamond.png'
import completePigUrl from '@/assets/images/accounts/complete-pig.png'
import completeStarUrl from '@/assets/images/accounts/complete-star.png'
import goalEducationIcon from '@/assets/images/goals/goals_1.png'
import goalLumpSumIcon from '@/assets/images/goals/goal-lump-sum.png'
import goalHousingIcon from '@/assets/images/goals/goals_2.png'
import goalMarriageIcon from '@/assets/images/goals/goals_4.png'
import goalCustomIcon from '@/assets/images/goals/goals_6.png'

type GoalPlan = {
  id: string
  name: string
  amount: number
  targetDate: string
}

const props = defineProps<{
  plans: GoalPlan[]
  linkedSavings: Record<string, string[]>
}>()

const goalIcons: Record<string, string> = {
  education: goalEducationIcon,
  'lump-sum': goalLumpSumIcon,
  housing: goalHousingIcon,
  marriage: goalMarriageIcon,
  custom: goalCustomIcon,
}

const goalStyles: Record<string, string> = {
  education: 'border-sky-200 bg-sky-50',
  housing: 'border-emerald-200 bg-emerald-50',
  marriage: 'border-pink-200 bg-pink-50',
  'lump-sum': 'border-amber-200 bg-amber-50',
  custom: 'border-violet-200 bg-violet-50',
}

const savingsNames: Record<string, string> = {
  'child-love-1': 'KB 아이사랑적금',
  'young-youth': 'KB Young Youth 적금',
  'child-love-2': 'KB 아이사랑적금 2',
}

const carousel = ref<HTMLElement | null>(null)
const activeIndex = ref(0)

const formatTargetDate = (targetDate: string) => {
  const [year, month] = targetDate.split('-')
  return year && month ? `${year}년 ${Number(month)}월` : targetDate
}

const updateActiveIndex = () => {
  if (!carousel.value) return
  const cards = Array.from(carousel.value.children) as HTMLElement[]
  const viewport = carousel.value.getBoundingClientRect()
  const center = carousel.value.scrollLeft + carousel.value.clientWidth / 2
  let closestIndex = 0
  let closestDistance = Number.POSITIVE_INFINITY

  cards.forEach((card, index) => {
    const rect = card.getBoundingClientRect()
    const cardCenter = rect.left - viewport.left + carousel.value!.scrollLeft + rect.width / 2
    const distance = Math.abs(center - cardCenter)
    if (distance < closestDistance) {
      closestDistance = distance
      closestIndex = index
    }
  })
  activeIndex.value = closestIndex
}

const moveTo = (index: number) => {
  const card = carousel.value?.children[index] as HTMLElement | undefined
  if (!carousel.value || !card) return
  const viewport = carousel.value.getBoundingClientRect()
  const rect = card.getBoundingClientRect()
  const cardCenter = rect.left - viewport.left + carousel.value.scrollLeft + rect.width / 2
  carousel.value.scrollTo({
    left: cardCenter - carousel.value.clientWidth / 2,
    behavior: 'smooth',
  })
}
</script>

<template>
  <section class="w-full py-2 text-center">
    <div class="complete-scene mx-auto" aria-label="목표 설정 완료">
      <img class="complete-scene__pig" :src="completePigUrl" alt="" />
      <img class="complete-scene__decoration complete-scene__star complete-scene__star--left" :src="completeStarUrl" alt="" />
      <img class="complete-scene__decoration complete-scene__star complete-scene__star--right" :src="completeStarUrl" alt="" />
      <img class="complete-scene__decoration complete-scene__diamond complete-scene__diamond--left" :src="completeDiamondUrl" alt="" />
      <img class="complete-scene__decoration complete-scene__diamond complete-scene__diamond--right" :src="completeDiamondUrl" alt="" />
      <img class="complete-scene__decoration complete-scene__circle complete-scene__circle--left" :src="completeCircleUrl" alt="" />
      <img class="complete-scene__decoration complete-scene__circle complete-scene__circle--right" :src="completeCircleUrl" alt="" />
      <span class="complete-scene__check" aria-hidden="true">
        <svg width="29" height="29" viewBox="0 0 24 24" fill="none">
          <path pathLength="1" d="M4 12.5L9.2 17.5L20 6.5" />
        </svg>
      </span>
    </div>

    <h1 class="mt-2 text-[27px] leading-tight font-extrabold tracking-[-0.04em]">
      목표 준비가 끝났어요!
    </h1>
    <p class="mt-2 text-sm text-[var(--color-text-secondary)]">
      목표마다 연결된 적금을 확인해보세요.
    </p>

    <div
      ref="carousel"
      class="-mx-6 mt-5 flex snap-x snap-mandatory scroll-px-[7%] gap-4 overflow-x-auto pb-3 text-left scroll-smooth [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
      @scroll="updateActiveIndex"
    >
      <article
        v-for="(plan) in plans"
        :key="plan.id"
        class="flex min-h-[260px] w-[86%] shrink-0 snap-center snap-always flex-col overflow-hidden rounded-[26px] border first:ml-[7%] last:mr-[7%]"
        :class="goalStyles[plan.id] ?? 'border-slate-200 bg-slate-50'"
      >
        <div class="flex items-center gap-4 p-5">
          <span class="grid size-16 shrink-0 place-items-center rounded-[20px] bg-white/90">
            <img class="size-12 object-contain" :src="goalIcons[plan.id]" alt="" />
          </span>
          <div class="min-w-0 flex-1">
            <h2 class="mt-0.5 truncate text-[22px] font-extrabold">{{ plan.name }}</h2>
            <p class="mt-2 text-sm font-bold">{{ plan.amount.toLocaleString('ko-KR') }}원</p>
            <p class="mt-0.5 text-xs text-[var(--color-text-secondary)]">
              {{ formatTargetDate(plan.targetDate) }}까지
            </p>
          </div>
        </div>

        <div class="flex flex-1 flex-col border-t border-black/6 bg-white/88 px-5 py-4">
          <div class="mb-3 flex items-center justify-between">
            <strong class="text-sm">연결된 적금</strong>
            <span class="text-xs font-bold text-[var(--color-selected-text)]">
              {{ linkedSavings[plan.id]?.length ?? 0 }}개
            </span>
          </div>
          <div v-if="linkedSavings[plan.id]?.length" class="grid gap-2.5">
            <div
              v-for="savingsId in linkedSavings[plan.id]"
              :key="savingsId"
              class="flex min-w-0 items-center gap-2.5"
            >
              <span
                class="grid size-8 shrink-0 place-items-center rounded-full bg-[#fff5cf] text-[#e7a400]"
              >
                <Landmark :size="17" :stroke-width="2.2" aria-hidden="true" />
              </span>
              <span class="truncate text-sm font-semibold">
                {{ savingsNames[savingsId] ?? '연결된 적금' }}
              </span>
            </div>
          </div>
          <p v-else class="my-auto text-center text-sm leading-5 text-[var(--color-text-secondary)]">
            연결된 적금이 없어요.<br />목표 관리에서 언제든 연결할 수 있어요.
          </p>
        </div>
      </article>
    </div>

    <div v-if="plans.length > 1" class="mt-1 flex justify-center gap-2">
      <button
        v-for="(_, index) in plans"
        :key="index"
        class="size-2 rounded-full transition-colors"
        :class="activeIndex === index ? 'bg-[var(--color-brand-primary)]' : 'bg-[var(--color-border)]'"
        type="button"
        :aria-label="`${index + 1}번째 목표 보기`"
        @click="moveTo(index)"
      ></button>
    </div>
  </section>
</template>

<style scoped>
.complete-scene { position: relative; width: 190px; height: 126px; }
.complete-scene__pig { position: absolute; z-index: 2; inset: 0; width: 190px; height: 126px; object-fit: contain; }
.complete-scene__decoration { position: absolute; z-index: 1; object-fit: contain; }
.complete-scene__star { width: 34px; height: 34px; }
.complete-scene__star--left { top: 13px; left: -5px; }
.complete-scene__star--right { right: -5px; bottom: 13px; width: 30px; height: 30px; }
.complete-scene__diamond { width: 19px; height: 19px; }
.complete-scene__diamond--left { left: 7px; bottom: 24px; }
.complete-scene__diamond--right { top: 48px; right: 8px; width: 17px; height: 17px; }
.complete-scene__circle { width: 14px; height: 14px; }
.complete-scene__circle--left { top: 56px; left: 16px; }
.complete-scene__circle--right { top: 70px; right: 0; width: 11px; height: 11px; }
.complete-scene__check { position: absolute; z-index: 3; top: 1px; right: 7px; display: grid; width: 44px; height: 44px; color: white; background: linear-gradient(155deg, #61c8f5 2%, #2d8dec 82%); border-radius: 50%; box-shadow: 0 6px 14px rgb(45 141 236 / 22%); place-items: center; }
.complete-scene__check path { fill: none; stroke: currentColor; stroke-width: 3.4; stroke-linecap: round; stroke-linejoin: round; }

@media (prefers-reduced-motion: no-preference) {
  .complete-scene__pig { animation: pig-arrive 680ms cubic-bezier(0.16, 1, 0.3, 1) both; }
  .complete-scene__decoration { animation: spark-arrive 620ms cubic-bezier(0.16, 1, 0.3, 1) both, spark-float 2.8s ease-in-out 1.15s infinite alternate; }
  .complete-scene__star--left { animation-delay: 180ms, 1.15s; }
  .complete-scene__star--right { animation-delay: 290ms, 1.35s; }
  .complete-scene__diamond--left { animation-delay: 350ms, 1.25s; }
  .complete-scene__diamond--right { animation-delay: 430ms, 1.5s; }
  .complete-scene__circle--left { animation-delay: 500ms, 1.4s; }
  .complete-scene__circle--right { animation-delay: 570ms, 1.6s; }
  .complete-scene__check { animation: check-arrive 620ms cubic-bezier(0.16, 1, 0.3, 1) 260ms both; }
  .complete-scene__check path { stroke-dasharray: 1; stroke-dashoffset: 1; animation: check-draw 300ms ease 560ms forwards; }
}

@keyframes pig-arrive { 0% { opacity: 0; transform: translateY(12px) scale(.8); } 65% { opacity: 1; transform: translateY(-2px) scale(1.03); } 100% { transform: none; } }
@keyframes spark-arrive { 0% { opacity: 0; transform: translateY(8px) scale(.2) rotate(-25deg); } 70% { opacity: 1; transform: translateY(-2px) scale(1.15) rotate(8deg); } 100% { opacity: 1; transform: none; } }
@keyframes spark-float { from { transform: translateY(0) rotate(-3deg); } to { transform: translateY(-4px) rotate(5deg); } }
@keyframes check-arrive { 0% { opacity: 0; transform: scale(.35) rotate(-12deg); } 68% { opacity: 1; transform: scale(1.1) rotate(3deg); } 100% { opacity: 1; transform: none; } }
@keyframes check-draw { to { stroke-dashoffset: 0; } }
</style>
