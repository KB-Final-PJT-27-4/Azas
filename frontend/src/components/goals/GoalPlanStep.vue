<script setup lang="ts">
import { ref } from 'vue'

import goalEducationIcon from '@/assets/images/goals/goals_1.png'
import goalIndependenceIcon from '@/assets/images/goals/goals_2.png'
import goalHousingIcon from '@/assets/images/goals/goals_3.png'
import goalMarriageIcon from '@/assets/images/goals/goals_4.png'
import goalInvestmentIcon from '@/assets/images/goals/goals_5.png'
import goalCustomIcon from '@/assets/images/goals/goals_6.png'

type GoalPlan = {
  id: string
  name: string
  amount: number
  targetDate: string
}

defineProps<{
  plans: GoalPlan[]
}>()

const carousel = ref<HTMLElement | null>(null)
const activeIndex = ref(0)
const icons: Record<string, string> = {
  education: goalEducationIcon,
  independence: goalIndependenceIcon,
  housing: goalHousingIcon,
  marriage: goalMarriageIcon,
  investment: goalInvestmentIcon,
  custom: goalCustomIcon,
}
const cardStyles = [
  'border-[var(--color-brand-primary)] bg-linear-to-b from-sky-100 to-sky-50',
  'border-amber-300 bg-linear-to-b from-amber-100 to-amber-50',
  'border-lime-400 bg-linear-to-b from-lime-100 to-lime-50',
  'border-pink-300 bg-linear-to-b from-pink-100 to-pink-50',
]

const formatTargetDate = (targetDate: string) => {
  const [year, month] = targetDate.split('-')
  return year && month ? `${year}년 ${month}월` : targetDate
}

const updateActiveIndex = () => {
  if (!carousel.value) return
  const cards = Array.from(carousel.value.children) as HTMLElement[]
  const carouselRect = carousel.value.getBoundingClientRect()
  const scrollLeft = carousel.value.scrollLeft
  const center = scrollLeft + carousel.value.clientWidth / 2
  let closestIndex = 0
  let closestDistance = Number.POSITIVE_INFINITY

  cards.forEach((card, index) => {
    const cardRect = card.getBoundingClientRect()
    const cardCenter = cardRect.left - carouselRect.left + scrollLeft + cardRect.width / 2
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

  const carouselRect = carousel.value.getBoundingClientRect()
  const cardRect = card.getBoundingClientRect()
  const cardCenterInScroll =
    cardRect.left - carouselRect.left + carousel.value.scrollLeft + cardRect.width / 2
  const centeredPosition = cardCenterInScroll - carousel.value.clientWidth / 2
  carousel.value.scrollTo({ left: centeredPosition, behavior: 'smooth' })
}
</script>

<template>
  <section>
    <h1 class="text-[26px] font-bold tracking-[-0.04em]">목표가 설정되었어요!</h1>
    <p class="mt-2 text-sm text-[var(--color-text-secondary)]">
      목표 금액과 달성 시기를 확인하고 준비를 시작해보세요.
    </p>

    <div
      ref="carousel"
      class="-mx-6 mt-12 flex snap-x snap-mandatory scroll-px-[12%] gap-7 overflow-x-auto pb-3 scroll-smooth [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
      @scroll="updateActiveIndex"
    >
      <article
        v-for="(plan, index) in plans"
        :key="plan.id"
        class="flex min-h-[470px] w-[76%] shrink-0 snap-center snap-always flex-col items-center justify-center rounded-3xl border-2 px-6 text-center first:ml-[12%] last:mr-[12%]"
        :class="cardStyles[index % cardStyles.length]"
      >
        <img class="size-40 object-contain" :src="icons[plan.id]" alt="" />
        <h2 class="mt-8 text-3xl font-bold">{{ plan.name }}</h2>
        <strong class="mt-6 text-2xl">{{ plan.amount.toLocaleString('ko-KR') }}원</strong>
        <p class="mt-5 text-sm text-[var(--color-text-secondary)]">
          {{ formatTargetDate(plan.targetDate) }}까지 · 월 약
          {{ Math.ceil(plan.amount / 240).toLocaleString('ko-KR') }}원
        </p>
      </article>
    </div>

    <div class="mt-2 flex justify-center gap-2">
      <button
        v-for="(_, index) in plans"
        :key="index"
        class="size-2 rounded-full transition-colors"
        :class="
          activeIndex === index ? 'bg-[var(--color-brand-primary)]' : 'bg-[var(--color-border)]'
        "
        type="button"
        :aria-label="`${index + 1}번째 목표 보기`"
        @click="moveTo(index)"
      ></button>
    </div>
  </section>
</template>
