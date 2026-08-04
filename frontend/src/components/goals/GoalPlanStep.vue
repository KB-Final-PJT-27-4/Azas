<script setup lang="ts">
import { nextTick, ref } from 'vue'

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

const formatTargetDate = (targetDate: string) => {
  const [year, month] = targetDate.split('-')
  return year && month ? `${year}년 ${month}월` : targetDate
}

const updateActiveIndex = () => {
  if (!carousel.value) return
  const cards = Array.from(carousel.value.children) as HTMLElement[]
  const center = carousel.value.scrollLeft + carousel.value.clientWidth / 2
  let closestIndex = 0
  let closestDistance = Number.POSITIVE_INFINITY

  cards.forEach((card, index) => {
    const cardCenter = card.offsetLeft + card.offsetWidth / 2
    const distance = Math.abs(center - cardCenter)
    if (distance < closestDistance) {
      closestDistance = distance
      closestIndex = index
    }
  })
  activeIndex.value = closestIndex
}

const moveTo = async (index: number) => {
  await nextTick()
  const card = carousel.value?.children[index] as HTMLElement | undefined
  card?.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' })
}
</script>

<template>
  <section>
    <h1 class="text-[26px] font-bold tracking-[-0.04em]">깨비 맞춤 플랜이 완성됐어요!</h1>
    <p class="mt-2 text-sm text-[var(--color-text-secondary)]">
      현재 깨비의 나이에 맞춰 목표별 플랜을 확인해보세요.
    </p>

    <div
      ref="carousel"
      class="-mx-6 mt-10 flex snap-x snap-mandatory gap-4 overflow-x-auto px-[8%] pb-3 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
      @scroll="updateActiveIndex"
    >
      <article
        v-for="plan in plans"
        :key="plan.id"
        class="flex min-h-[410px] w-[84%] shrink-0 snap-center flex-col items-center justify-center rounded-3xl border-2 border-[var(--color-brand-primary)] bg-[var(--color-selected-background)] px-6 text-center"
      >
        <img class="size-28 object-contain" :src="icons[plan.id]" alt="" />
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
