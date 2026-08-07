<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ChevronRight } from 'lucide-vue-next'

import { BaseCard } from '@/components/common'
import { BaseProgressBar } from '@/components/feedback'
import homeBabyNuttiUrl from '@/assets/images/home/home-baby-nutti.png'
import homeHeroBgUrl from '@/assets/images/home/home-hero-bg.png'
import checklistIconUrl from '@/assets/images/home/icon-checklist.png'
import goalIconUrl from '@/assets/images/home/icon-goal.png'
import timeCapsuleIconUrl from '@/assets/images/home/icon-time-capsule.png'
import { recommendedProducts } from '@/data/productDummyData'
import { currentHomeMemberType, homeDataByMemberType } from '@/mocks/home'

const homeData = computed(() => homeDataByMemberType[currentHomeMemberType])
const isExistingMember = computed(() => homeData.value.memberType === 'existing')

const formatCurrency = (amount: number) => `${amount.toLocaleString('ko-KR')}원`

const selectedGoalIndex = ref(0)
const goalCarouselRef = ref<HTMLElement | null>(null)
const selectedProductIndex = ref(0)
const productCarouselRef = ref<HTMLElement | null>(null)
let productCarouselTimer: ReturnType<typeof window.setInterval> | null = null
const goalSlides = computed(() => homeData.value.goals ?? [])
const selectedGoal = computed(() => goalSlides.value[selectedGoalIndex.value])
const hasGoalSlides = computed(() => isExistingMember.value && goalSlides.value.length > 0)
const quickMenus = computed(() => {
  const goal = selectedGoal.value

  if (!goal) {
    return homeData.value.quickMenus
  }

  return homeData.value.quickMenus.map((menu) => {
    if (menu.icon === 'checklist') {
      return { ...menu, subtitle: goal.checklistStatus }
    }

    if (menu.icon === 'timeCapsule') {
      return { ...menu, subtitle: goal.timeCapsuleStatus }
    }

    if (menu.icon === 'goal') {
      return { ...menu, subtitle: formatCurrency(goal.currentAmount) }
    }

    return menu
  })
})

const updateSelectedGoalByScroll = (event: Event) => {
  const target = event.currentTarget as HTMLElement

  if (!target.clientWidth) {
    return
  }

  const nextIndex = Math.round(target.scrollLeft / target.clientWidth)
  selectedGoalIndex.value = Math.min(Math.max(nextIndex, 0), goalSlides.value.length - 1)
}

const selectGoal = (index: number) => {
  selectedGoalIndex.value = index
  goalCarouselRef.value?.scrollTo({
    left: goalCarouselRef.value.clientWidth * index,
    behavior: 'smooth',
  })
}

const updateSelectedProductByScroll = (event: Event) => {
  const target = event.currentTarget as HTMLElement
  if (!target.clientWidth) return

  selectedProductIndex.value = Math.min(
    Math.max(Math.round(target.scrollLeft / target.clientWidth), 0),
    recommendedProducts.length - 1,
  )
}

const selectProduct = (index: number, behavior: ScrollBehavior = 'smooth') => {
  selectedProductIndex.value = index
  productCarouselRef.value?.scrollTo({
    left: productCarouselRef.value.clientWidth * index,
    behavior,
  })
}

const stopProductCarousel = () => {
  if (productCarouselTimer === null) return
  window.clearInterval(productCarouselTimer)
  productCarouselTimer = null
}

const startProductCarousel = () => {
  stopProductCarousel()
  if (recommendedProducts.length < 2) return

  productCarouselTimer = window.setInterval(() => {
    const nextIndex = (selectedProductIndex.value + 1) % recommendedProducts.length
    selectProduct(nextIndex)
  }, 3500)
}

const quickMenuIconUrls = {
  checklist: checklistIconUrl,
  timeCapsule: timeCapsuleIconUrl,
  goal: goalIconUrl,
}

const homeBackgroundStyle = {
  backgroundImage: `url(${homeHeroBgUrl})`,
  backgroundPosition: 'top center',
  backgroundRepeat: 'no-repeat',
  backgroundSize: '100% 100%',
}

onMounted(startProductCarousel)
onBeforeUnmount(stopProductCarousel)
</script>

<template>
  <main
    class="grid min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] content-start gap-4 bg-[#eaf8ff] px-[18px] pt-2 pb-[18px]"
    :style="homeBackgroundStyle"
  >
    <section
      class="grid min-h-[190px] grid-cols-[minmax(0,1fr)_174px] items-center overflow-hidden rounded-[var(--radius-lg)] px-4 max-[360px]:grid-cols-[minmax(0,1fr)_140px] max-[360px]:px-[14px]"
      aria-label="홈 상단 요약"
    >
      <div class="relative z-[1] grid min-w-0 gap-[6px]">
        <p
          class="m-0 text-[clamp(12px,3.4vw,var(--font-size-sm))] leading-[1.45] text-[var(--color-text-secondary)]"
        >
          하나님의 우리 아이,
        </p>
        <h1
          class="m-0 text-[clamp(17px,5.2vw,20px)] leading-[1.3] font-extrabold text-[var(--color-text-primary)]"
        >
          {{ homeData.heroTitle }}
        </h1>
        <p
          class="m-0 whitespace-nowrap text-[clamp(11px,3vw,12px)] leading-[1.45] tracking-[-0.025em] text-[var(--color-text-secondary)]"
        >
          {{ homeData.heroDescription }}
        </p>
      </div>
      <img
        class="pointer-events-none relative z-0 w-[174px] max-w-full translate-x-3 scale-[1.16] select-none justify-self-end object-contain max-[360px]:w-[140px] max-[360px]:translate-x-1.5 max-[360px]:scale-[1.12]"
        :src="homeBabyNuttiUrl"
        alt=""
        aria-hidden="true"
      />
    </section>

    <section class="grid grid-cols-3 gap-[var(--space-3)]" aria-label="홈 빠른 메뉴">
      <RouterLink
        v-for="menu in quickMenus"
        :key="menu.title"
        class="grid h-[132px] place-items-center gap-[7px] rounded-2xl border border-[#e5edf2] bg-[var(--color-surface)] px-[var(--space-2)] py-[14px] text-center shadow-[0_8px_22px_rgba(54,112,139,0.10)] transition-[transform,box-shadow] duration-200 hover:-translate-y-0.5 hover:shadow-[0_11px_26px_rgba(54,112,139,0.14)] active:translate-y-0"
        :to="menu.to"
      >
        <img
          class="size-[38px] object-contain"
          :src="quickMenuIconUrls[menu.icon]"
          alt=""
          aria-hidden="true"
        />
        <strong class="text-[15px] leading-tight text-[var(--color-text-primary)]">
          {{ menu.title }}
        </strong>
        <span class="text-[length:var(--font-size-xs)] text-[var(--color-text-secondary)]">
          {{ menu.subtitle }}
        </span>
      </RouterLink>
    </section>

    <section v-if="hasGoalSlides" class="grid gap-[10px]" aria-label="목표 카드 슬라이드">
      <div
        ref="goalCarouselRef"
        class="flex snap-x snap-mandatory overflow-x-auto scroll-smooth [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
        @scroll.passive="updateSelectedGoalByScroll"
      >
        <BaseCard
          v-for="goal in goalSlides"
          :key="goal.id"
          class="home-goal-card w-full flex-none snap-center"
        >
          <template #header>
            <div class="flex items-center justify-between">
              <h2 class="m-0 text-[length:var(--font-size-md)]">{{ goal.title }}</h2>
              <span
                class="rounded-full bg-[var(--color-selected-background)] px-[var(--space-3)] py-[var(--space-1)] text-[length:var(--font-size-xs)] font-bold text-[var(--color-selected-text)]"
              >
                {{ goal.tag }}
              </span>
            </div>
          </template>

          <div class="grid gap-[var(--space-2)]">
            <span class="text-[11px] font-medium text-[var(--color-text-secondary)]">
              현재 모은 금액
            </span>
            <strong class="text-[length:var(--font-size-xl)] leading-none text-[var(--color-text-primary)]">
              {{ formatCurrency(goal.currentAmount) }}
            </strong>
            <div class="mt-1 flex items-center justify-between gap-3 text-xs">
              <span class="text-[var(--color-text-secondary)]">
                목표 {{ formatCurrency(goal.targetAmount) }}
              </span>
              <strong class="shrink-0 text-[var(--color-selected-text)]">{{ goal.progress }}%</strong>
            </div>
            <BaseProgressBar :value="goal.progress" />
            <p class="m-0 text-right text-[11px] text-[var(--color-text-secondary)]">
              {{ goal.targetDate }}까지
            </p>
          </div>
        </BaseCard>
      </div>

      <div
        v-if="goalSlides.length > 1"
        class="flex items-center justify-center gap-[7px] pt-[2px] pb-[var(--space-3)]"
        aria-label="목표 카드 위치"
      >
        <button
          v-for="(_, index) in goalSlides"
          :key="index"
          class="size-[9px] cursor-pointer rounded-full border-0 p-0 transition-colors"
          :class="
            selectedGoalIndex === index
              ? 'bg-[var(--color-brand-primary)]'
              : 'bg-[var(--color-disabled-border)]'
          "
          type="button"
          :aria-label="`${index + 1}번째 목표 보기`"
          :aria-current="selectedGoalIndex === index ? 'true' : undefined"
          @click="selectGoal(index)"
        />
      </div>
    </section>

    <BaseCard v-else class="home-goal-empty-card">
      <RouterLink
        class="flex items-center justify-between gap-[var(--space-4)]"
        :to="homeData.goalCtaTo"
      >
        <div>
          <h2 class="m-0 mb-[var(--space-2)] text-[length:var(--font-size-lg)] text-[var(--color-text-primary)]">
            우리 아이의 첫 저축 목표를 만들어보세요!
          </h2>
          <p class="m-0 text-[var(--color-text-secondary)]">작은 목표부터 함께 시작해요.</p>
        </div>
        <ChevronRight class="shrink-0 text-[#b8dcff]" :size="36" :stroke-width="2.5" />
      </RouterLink>
    </BaseCard>

    <section
      class="grid gap-3 rounded-2xl border border-[#f5e8b9] bg-[var(--color-brand-secondary)] p-[14px] shadow-[0_8px_22px_rgba(176,142,43,0.06)]"
    >
      <div class="flex items-start justify-between gap-3 px-0.5">
        <div>
          <h2 class="m-0 text-[length:var(--font-size-md)] font-extrabold">추천 금융상품</h2>
          <p class="mt-1 mb-0 text-[11px] text-[var(--color-text-secondary)]">
            아이의 목표에 맞는 상품을 골라봤어요
          </p>
        </div>
        <RouterLink
          class="mt-0.5 inline-flex shrink-0 items-center gap-0.5 text-[length:var(--font-size-xs)] font-semibold text-[var(--color-selected-text)]"
          :to="homeData.productsMoreTo"
        >
          더보기
          <ChevronRight :size="14" />
        </RouterLink>
      </div>

      <div
        ref="productCarouselRef"
        class="flex snap-x snap-mandatory overflow-x-auto scroll-smooth [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
        @scroll.passive="updateSelectedProductByScroll"
        @pointerenter="stopProductCarousel"
        @pointerleave="startProductCarousel"
        @touchstart.passive="stopProductCarousel"
        @touchend.passive="startProductCarousel"
      >
        <article
          v-for="product in recommendedProducts"
          :key="product.id"
          class="grid w-full flex-none snap-center gap-3 rounded-2xl border border-[#f0ead8] bg-[var(--color-surface)] p-4 shadow-[0_5px_16px_rgba(107,93,50,0.055)]"
        >
          <div class="flex min-h-[82px] items-center justify-between gap-2">
            <div class="min-w-0 self-start">
              <div class="mb-1.5 flex items-center gap-1.5">
                <span
                  class="rounded-full bg-[var(--color-selected-background)] px-2 py-0.5 text-[10px] font-bold text-[var(--color-selected-text)]"
                >
                  {{ product.type }}
                </span>
                <span class="truncate text-[10px] text-[var(--color-text-secondary)]">
                  {{ product.bankName }}
                </span>
              </div>
              <h3 class="m-0 text-[15px] leading-tight font-extrabold text-[var(--color-text-primary)]">
                {{ product.name }}
              </h3>
              <p class="mt-1.5 mb-0 text-[11px] text-[var(--color-text-secondary)]">
                아이의 미래를 위한 든든한 첫 저축
              </p>
            </div>
            <div class="relative grid size-[80px] shrink-0 place-items-center">
              <!-- <span class="absolute inset-1 rounded-full bg-[#fff8dc]"></span> -->
              <img
                class="relative z-[1] size-[76px] object-contain drop-shadow-[0_5px_7px_rgba(84,122,151,0.12)]"
                :src="product.mascot"
                :alt="`${product.name} 추천 이미지`"
              />
            </div>
          </div>

          <div class="flex items-end justify-between gap-3 border-t border-[#f2eee3] pt-3">
            <div>
              <span class="block text-[10px] text-[var(--color-text-secondary)]">최고 연</span>
              <strong class="text-[24px] leading-none font-extrabold tracking-[-0.04em] text-[var(--color-brand-primary)]">
                {{ product.rate }}
              </strong>
            </div>
            <p class="m-0 text-right text-[10px] leading-[1.6] text-[var(--color-text-secondary)]">
              {{ product.period }} · 자유적립식<br />월 최대 {{ product.monthlyLimit }}
            </p>
          </div>

          <RouterLink
            class="inline-flex min-h-[38px] items-center justify-center gap-1 rounded-xl border border-[var(--color-border)] bg-[#fbfcfd] px-3 text-[length:var(--font-size-xs)] font-bold text-[var(--color-unselected-text)] transition-colors hover:bg-[var(--color-unselected-background)] active:bg-[#e9eef2]"
            :to="{ name: 'ProductDetail', params: { productId: product.id } }"
          >
            상품 자세히 보기
            <ChevronRight :size="14" />
          </RouterLink>
        </article>
      </div>

      <div
        v-if="recommendedProducts.length > 1"
        class="flex justify-center gap-1.5"
        aria-label="추천 금융상품 위치"
      >
        <button
          v-for="(_, index) in recommendedProducts"
          :key="`product-dot-${index}`"
          class="size-2 rounded-full border-0 p-0 transition-colors"
          :class="
            selectedProductIndex === index
              ? 'bg-[var(--color-brand-primary)]'
              : 'bg-[#d9e1e5]'
          "
          type="button"
          :aria-label="`${index + 1}번째 추천 상품 보기`"
          :aria-current="selectedProductIndex === index ? 'true' : undefined"
          @click="selectProduct(index); startProductCarousel()"
        ></button>
      </div>
    </section>
  </main>
</template>

<style scoped>
:deep(.base-card.home-goal-card),
:deep(.base-card.home-goal-empty-card) {
  padding: 16px;
  border-color: #e5edf2;
  border-radius: 16px;
  box-shadow: none;
}

:deep(.base-card.home-goal-card .base-card__header) {
  margin-bottom: 10px;
}

:deep(.base-card.home-goal-card .base-card__body) {
  line-height: 1.45;
}
</style>
