<script setup lang="ts">
import { computed } from 'vue'
import { ChevronRight } from 'lucide-vue-next'

import { BaseCard } from '@/components/common'
import { BaseProgressBar } from '@/components/feedback'
import homeBabyNuttiUrl from '@/assets/images/home/home-baby-nutti.png'
import homeHeroBgUrl from '@/assets/images/home/home-hero-bg.png'
import checklistIconUrl from '@/assets/images/home/icon-checklist.png'
import goalIconUrl from '@/assets/images/home/icon-goal.png'
import timeCapsuleIconUrl from '@/assets/images/home/icon-time-capsule.png'
import { currentHomeMemberType, homeDataByMemberType } from '@/mocks/home'

const homeData = computed(() => homeDataByMemberType[currentHomeMemberType])
const isExistingMember = computed(() => homeData.value.memberType === 'existing')

const formatCurrency = (amount: number) => `${amount.toLocaleString('ko-KR')}원`

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
</script>

<template>
  <main
    class="grid min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] content-start gap-3 bg-[#eaf8ff] px-[18px] pt-4 pb-[14px]"
    :style="homeBackgroundStyle"
  >
    <section
      class="grid min-h-[216px] grid-cols-[minmax(0,1fr)_156px] items-center overflow-hidden rounded-[var(--radius-lg)] px-4 pt-2 max-[360px]:grid-cols-[minmax(0,1fr)_128px] max-[360px]:px-[14px]"
      aria-label="홈 상단 요약"
    >
      <div class="relative z-10 grid min-w-0 gap-[6px]">
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
          class="m-0 text-[clamp(12px,3.4vw,var(--font-size-sm))] leading-[1.45] text-[var(--color-text-secondary)]"
        >
          {{ homeData.heroDescription }}
        </p>
      </div>
      <img
        class="pointer-events-none z-0 w-[156px] max-w-full select-none justify-self-end object-contain max-[360px]:w-[128px]"
        :src="homeBabyNuttiUrl"
        alt=""
        aria-hidden="true"
      />
    </section>

    <section class="grid grid-cols-3 gap-[var(--space-3)]" aria-label="홈 빠른 메뉴">
      <RouterLink
        v-for="menu in homeData.quickMenus"
        :key="menu.title"
        class="grid min-h-[98px] place-items-center gap-[6px] rounded-[var(--radius-lg)] border border-[var(--color-border)] bg-[var(--color-surface)] px-[var(--space-2)] py-[10px] text-center shadow-[0_8px_20px_rgb(85_192_244_/_10%)]"
        :to="menu.to"
      >
        <img
          class="size-[34px] object-contain"
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

    <BaseCard v-if="isExistingMember && homeData.goal" class="home-goal-card">
      <template #header>
        <div class="flex items-center justify-between">
          <h2 class="m-0 text-[length:var(--font-size-md)]">{{ homeData.goal.title }}</h2>
          <span
            class="rounded-full bg-[var(--color-selected-background)] px-[var(--space-3)] py-[var(--space-1)] text-[length:var(--font-size-xs)] font-bold text-[var(--color-selected-text)]"
          >
            {{ homeData.goal.tag }}
          </span>
        </div>
      </template>

      <div class="grid gap-[var(--space-2)]">
        <strong class="text-[length:var(--font-size-xl)] text-[var(--color-text-primary)]">
          {{ formatCurrency(homeData.goal.currentAmount) }}
        </strong>
        <p class="m-0 text-[var(--color-text-secondary)]">
          목표 금액 {{ formatCurrency(homeData.goal.targetAmount) }} · 달성률
          {{ homeData.goal.progress }}% · 달성 시기 {{ homeData.goal.targetDate }}
        </p>
        <BaseProgressBar :value="homeData.goal.progress" />
      </div>
    </BaseCard>

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

    <section class="grid gap-[14px] rounded-[var(--radius-lg)] bg-[var(--color-brand-secondary)] p-4">
      <div class="flex items-center justify-between">
        <h2 class="m-0 text-[length:var(--font-size-md)] font-extrabold">추천 금융상품</h2>
        <RouterLink
          class="text-[length:var(--font-size-xs)] text-[var(--color-text-secondary)]"
          :to="homeData.productsMoreTo"
        >
          더보기
        </RouterLink>
      </div>

      <div class="grid grid-cols-2 gap-[var(--space-3)]">
        <article
          v-for="product in homeData.products"
          :key="product.id"
          class="grid gap-[10px] rounded-[var(--radius-md)] bg-[var(--color-surface)] p-3"
        >
          <div class="flex items-center justify-between gap-[var(--space-2)]">
            <strong class="text-[length:var(--font-size-xs)] leading-tight text-[var(--color-text-primary)]">
              {{ product.name }}
            </strong>
            <span
              class="rounded-full bg-[var(--color-selected-background)] px-[var(--space-2)] py-0.5 text-[10px] font-bold text-[var(--color-selected-text)]"
            >
              {{ product.type }}
            </span>
          </div>
          <p class="m-0 text-[length:var(--font-size-xs)] text-[var(--color-text-secondary)]">
            {{ product.rate }}
          </p>
          <p class="m-0 text-[length:var(--font-size-xs)] text-[var(--color-text-secondary)]">
            {{ product.period }}
          </p>
          <RouterLink
            class="inline-flex min-h-[30px] items-center justify-center rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] px-[var(--space-3)] text-[length:var(--font-size-xs)] font-bold text-[var(--color-unselected-text)]"
            :to="product.to"
          >
            상품 보기
          </RouterLink>
        </article>
      </div>
    </section>
  </main>
</template>

<style scoped>
:deep(.base-card.home-goal-card),
:deep(.base-card.home-goal-empty-card) {
  padding: 16px;
}

:deep(.base-card.home-goal-card .base-card__header) {
  margin-bottom: 10px;
}

:deep(.base-card.home-goal-card .base-card__body) {
  line-height: 1.45;
}
</style>
