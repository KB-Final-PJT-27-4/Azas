<script setup lang="ts">
import { ref } from 'vue'

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

defineProps<{
  plans: GoalPlan[]
}>()

const carousel = ref<HTMLElement | null>(null)
const activeIndex = ref(0)
const icons: Record<string, string> = {
  education: goalEducationIcon,
  'lump-sum': goalLumpSumIcon,
  housing: goalHousingIcon,
  marriage: goalMarriageIcon,
  custom: goalCustomIcon,
}
const cardStyles: Record<string, string> = {
  education: 'border-sky-200 bg-linear-to-br from-sky-100 via-sky-50 to-blue-50',
  housing: 'border-emerald-200 bg-linear-to-br from-emerald-100 via-green-50 to-teal-50',
  marriage: 'border-pink-200 bg-linear-to-br from-pink-100 via-rose-50 to-pink-50',
  'lump-sum': 'border-amber-200 bg-linear-to-br from-amber-100 via-yellow-50 to-orange-50',
  custom: 'border-violet-200 bg-linear-to-br from-violet-100 via-purple-50 to-indigo-50',
}

const defaultCardStyle =
  'border-slate-200 bg-linear-to-br from-slate-100 via-slate-50 to-gray-50'

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
    <h1 class="text-[26px] leading-tight font-bold tracking-[-0.04em]">목표가 설정되었어요!</h1>
    <p class="mt-2 text-sm leading-5 text-[var(--color-text-secondary)]">
      설정한 계획을 확인하고 준비를 시작해보세요.
    </p>

    <div
      ref="carousel"
      class="-mx-6 mt-6 flex snap-x snap-mandatory scroll-px-[7%] gap-4 overflow-x-auto pt-2 pb-8 scroll-smooth [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
      @scroll="updateActiveIndex"
    >
      <article
        v-for="(plan, index) in plans"
        :key="plan.id"
        class="relative flex min-h-[430px] w-[86%] shrink-0 snap-center snap-always flex-col overflow-hidden rounded-[28px] border p-4 shadow-[0_10px_28px_rgb(29_68_89_/_10%)] first:ml-[7%] last:mr-[7%]"
        :class="cardStyles[plan.id] ?? defaultCardStyle"
        :aria-label="`${index + 1}번째 목표 ${plan.name}`"
      >
        <div class="flex items-center">
          <span
            class="rounded-full bg-white/80 px-3 py-1.5 text-xs font-bold text-[var(--color-text-secondary)] shadow-sm"
          >
            목표 {{ index + 1 }}
          </span>
        </div>

        <div
          class="mx-auto mt-2 grid size-26 place-items-center rounded-full bg-white/65 shadow-inner"
        >
          <img class="size-21 object-contain" :src="icons[plan.id]" alt="" />
        </div>

        <div
          class="mt-3 flex flex-1 flex-col rounded-[22px] border border-white/80 bg-white/85 p-4 text-left shadow-[0_6px_18px_rgb(29_68_89_/_7%)]"
        >
          <h2 class="text-[22px] font-bold tracking-[-0.03em]">{{ plan.name }}</h2>
          <p class="mt-3 text-xs font-semibold text-[var(--color-text-secondary)]">목표 금액</p>
          <strong class="mt-1 text-[26px] leading-tight tracking-[-0.04em]">
            {{ plan.amount.toLocaleString('ko-KR') }}원
          </strong>

          <dl
            class="mt-5 divide-y divide-black/8 rounded-2xl bg-[var(--color-surface-muted)] px-4"
          >
            <div class="flex items-center justify-between py-2.5">
              <dt class="text-sm text-[var(--color-text-secondary)]">목표 시기</dt>
              <dd class="text-sm font-bold">{{ formatTargetDate(plan.targetDate) }}</dd>
            </div>
            <div class="flex items-center justify-between py-2.5">
              <dt class="text-sm text-[var(--color-text-secondary)]">월 저축액</dt>
              <dd class="text-sm font-bold">
                약 {{ Math.ceil(plan.amount / 240).toLocaleString('ko-KR') }}원
              </dd>
            </div>
          </dl>
        </div>
      </article>
    </div>

    <div v-if="plans.length > 1" class="flex justify-center gap-2">
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
