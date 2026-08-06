<script setup lang="ts">
import { ref } from 'vue'
import { ChevronRight, Heart } from 'lucide-vue-next'
import { useRouter } from 'vue-router'

import { productRecommendationGoal, recommendedProducts } from '@/data/productDummyData'

const router = useRouter()
const favoriteProductIds = ref(new Set<string>(['kb-child-love-saving-1']))

const formatWon = (amount: number) => `${amount.toLocaleString('ko-KR')}원`

const toggleFavorite = (productId: string) => {
  const nextFavorites = new Set(favoriteProductIds.value)
  if (nextFavorites.has(productId)) nextFavorites.delete(productId)
  else nextFavorites.add(productId)
  favoriteProductIds.value = nextFavorites
}

const editGoal = () => router.push({ name: 'Goals' })
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-[var(--color-surface)] px-4 pt-5 pb-6 text-[var(--color-text-primary)]"
  >
    <section class="rounded-[20px] bg-[var(--color-brand-secondary)] px-[18px] py-5">
      <div class="flex items-center gap-4">
        <span
          class="grid size-13 shrink-0 place-items-center overflow-hidden rounded-full bg-[var(--color-surface)]"
          aria-hidden="true"
        >
          <img class="size-10 object-contain" :src="productRecommendationGoal.icon" alt="" />
        </span>
        <div class="min-w-0">
          <span class="block text-[11px] text-[var(--color-text-secondary)]">현재 목표</span>
          <h1 class="mt-0.5 mb-0 text-[20px] leading-tight font-extrabold">
            {{ productRecommendationGoal.title }}
          </h1>
          <p class="mt-1 mb-0 text-[12px] text-[var(--color-text-secondary)]">
            목표금액 {{ formatWon(productRecommendationGoal.targetAmount) }}
          </p>
        </div>
      </div>

      <div class="mt-5 grid grid-cols-2 border-t border-[var(--color-border)] pt-4">
        <div>
          <span class="block text-[11px] text-[var(--color-text-secondary)]">목표 달성 시기</span>
          <strong class="mt-1 block text-[14px]">{{ productRecommendationGoal.targetDate }}</strong>
        </div>
        <div>
          <span class="block text-[11px] text-[var(--color-text-secondary)]"
            >월 저축 가능 금액</span
          >
          <strong class="mt-1 block text-[14px]">
            {{ formatWon(productRecommendationGoal.monthlySavingAmount) }}
          </strong>
        </div>
      </div>

      <button
        class="mt-4 h-10 w-full rounded-[11px] border border-[var(--color-border)] bg-[var(--color-surface)] text-[14px] font-medium active:bg-[var(--color-surface-muted)]"
        type="button"
        @click="editGoal"
      >
        수정하기
      </button>
    </section>

    <section class="mt-4">
      <h2 class="m-0 px-1 text-[15px] font-extrabold">
        추천 상품 {{ recommendedProducts.length }}개
      </h2>

      <ul class="mt-4 mb-0 grid list-none gap-4 p-0">
        <li v-for="product in recommendedProducts" :key="product.id">
          <article
            class="relative rounded-[18px] border border-[var(--color-border)] bg-[var(--color-surface)] px-[18px] py-5 shadow-sm"
          >
            <div class="flex items-start gap-2">
              <h3 class="m-0 truncate text-[17px] font-extrabold">{{ product.name }}</h3>
              <span
                class="shrink-0 rounded-full bg-[var(--color-selected-background)] px-3 py-1 text-[10px] font-bold text-[var(--color-selected-text)]"
              >
                {{ product.type }}
              </span>
            </div>
            <p class="mt-2 mb-0 text-[12px] text-[var(--color-text-secondary)]">{{ product.bankName }}</p>

            <button
              class="absolute top-5 right-5 grid size-8 place-items-center rounded-full active:bg-[var(--color-selected-background)]"
              type="button"
              :aria-label="`${product.name} 찜하기`"
              :aria-pressed="favoriteProductIds.has(product.id)"
              @click="toggleFavorite(product.id)"
            >
              <Heart
                :size="24"
                :stroke-width="1.7"
                :class="
                  favoriteProductIds.has(product.id)
                    ? 'fill-[#ff001b] text-[#ff001b]'
                    : 'text-[var(--color-unselected-text)]'
                "
              />
            </button>

            <div class="mt-4 grid grid-cols-[1fr_0.9fr_0.9fr_94px] items-start gap-1.5">
              <div>
                <span class="block text-[10px] text-[var(--color-text-secondary)]">최고 연</span>
                <strong class="mt-1 block text-[21px] text-[var(--color-selected-text)]">
                  {{ product.rate }}
                </strong>
              </div>
              <div>
                <span class="block text-[10px] text-[var(--color-text-secondary)]">가입기간</span>
                <strong class="mt-2 block text-[13px]">{{ product.period }}</strong>
              </div>
              <div>
                <span class="block text-[10px] text-[var(--color-text-secondary)]">월 납입한도</span>
                <strong class="mt-2 block text-[13px]">{{ product.monthlyLimit }}</strong>
              </div>
              <div class="relative h-[64px]" aria-hidden="true">
                <img
                  class="absolute right-0 bottom-4 max-h-[78px] w-full object-contain"
                  :src="product.mascot"
                  alt=""
                />
              </div>
            </div>

            <RouterLink
              class="flex h-10 items-center justify-center gap-2 rounded-[10px] border border-[var(--color-border)] text-[14px] font-bold active:bg-[var(--color-surface-muted)]"
              :to="{ name: 'ProductDetail', params: { productId: product.id } }"
            >
              상품 자세히 보기
              <ChevronRight :size="15" />
            </RouterLink>
          </article>
        </li>
      </ul>

      <button
        class="mt-4 h-12 w-full rounded-[12px] border border-[var(--color-border)] bg-[var(--color-surface)] text-[14px] font-extrabold text-[var(--color-text-secondary)] active:bg-[var(--color-surface-muted)]"
        type="button"
      >
        추천 상품 더 보기
      </button>
    </section>

    <aside
      class="mt-4 rounded-[14px] bg-[var(--color-surface-muted)] px-4 py-5 text-[10px] leading-[1.7] text-[var(--color-text-secondary)]"
    >
      위 금리는 기본금리 기준이며, 우대금리 적용 시 변동될 수 있습니다.<br />
      상품 가입 전 금융기관의 최신 안내를 확인해 주세요.
    </aside>
  </main>
</template>
