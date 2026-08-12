<script setup lang="ts">
import { computed, ref } from 'vue'
import { ChevronRight, Heart } from 'lucide-vue-next'

import { recommendedProducts } from '@/data/productDummyData'

const favoriteProductIds = ref(new Set<string>(['kb-child-love-saving-1']))
type ProductFilter = '전체' | '적금' | '입출금계좌'

const productFilters: ProductFilter[] = ['전체', '적금', '입출금계좌']
const selectedProductFilter = ref<ProductFilter>('전체')

const filteredProducts = computed(() => {
  if (selectedProductFilter.value === '전체') return recommendedProducts

  return recommendedProducts.filter((product) => product.type === selectedProductFilter.value)
})

const toggleFavorite = (productId: string) => {
  const nextFavorites = new Set(favoriteProductIds.value)
  if (nextFavorites.has(productId)) nextFavorites.delete(productId)
  else nextFavorites.add(productId)
  favoriteProductIds.value = nextFavorites
}
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-[var(--color-surface)] px-5 pt-6 pb-8 text-[var(--color-text-primary)]"
  >
    <section>
      <fieldset>
        <legend class="sr-only">상품 유형 필터</legend>
        <div class="flex gap-2 overflow-x-auto">
          <button
            v-for="filter in productFilters"
            :key="filter"
            class="h-10 shrink-0 rounded-full border px-4 text-[13px] font-bold transition-colors"
            :class="
              selectedProductFilter === filter
                ? 'border-[var(--color-brand-primary)] bg-[var(--color-selected-background)] text-[var(--color-selected-text)]'
                : 'border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text-secondary)]'
            "
            type="button"
            :aria-pressed="selectedProductFilter === filter"
            @click="selectedProductFilter = filter"
          >
            {{ filter }}
          </button>
        </div>
      </fieldset>

      <h2 class="mt-6 mb-0 text-[15px] font-extrabold">
        추천 상품 {{ filteredProducts.length }}개
      </h2>

      <ul v-if="filteredProducts.length" class="mt-3 mb-0 grid list-none gap-3 p-0">
        <li v-for="product in filteredProducts" :key="product.id">
          <article
            class="relative rounded-[18px] border border-[var(--color-border)] bg-[var(--color-surface)] p-5 shadow-sm"
          >
            <div class="flex items-start gap-2 pr-10">
              <h3 class="m-0 truncate text-[17px] font-extrabold">{{ product.name }}</h3>
              <span
                class="shrink-0 rounded-full bg-[var(--color-selected-background)] px-3 py-1 text-[10px] font-bold text-[var(--color-selected-text)]"
              >
                {{ product.type }}
              </span>
            </div>
            <p class="mt-1.5 mb-0 text-[12px] text-[var(--color-text-secondary)]">
              {{ product.bankName }}
            </p>

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

            <div class="mt-5 grid grid-cols-[1fr_0.9fr_0.9fr_88px] items-start gap-2">
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
                <span class="block text-[10px] text-[var(--color-text-secondary)]"
                  >월 납입한도</span
                >
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
              class="mt-1 flex h-10 items-center justify-center gap-2 rounded-[10px] border border-[var(--color-border)] text-[14px] font-bold active:bg-[var(--color-surface-muted)]"
              :to="{ name: 'ProductDetail', params: { productId: product.id } }"
            >
              상품 자세히 보기
              <ChevronRight :size="15" />
            </RouterLink>
          </article>
        </li>
      </ul>

      <div
        v-else
        class="mt-3 rounded-[18px] border border-[var(--color-border)] bg-[var(--color-surface)] px-5 py-16 text-center"
        role="status"
      >
        <p class="m-0 text-[14px] font-bold">해당 유형의 추천 상품이 없어요.</p>
        <p class="mt-2 mb-0 text-[12px] text-[var(--color-text-secondary)]">
          다른 상품 유형을 선택해 주세요.
        </p>
      </div>

      <button
        v-if="filteredProducts.length"
        class="mt-3 h-12 w-full rounded-[12px] border border-[var(--color-border)] bg-[var(--color-surface)] text-[14px] font-extrabold text-[var(--color-text-secondary)] active:bg-[var(--color-surface-muted)]"
        type="button"
      >
        추천 상품 더 보기
      </button>
    </section>

    <aside
      class="mt-6 rounded-[14px] bg-[var(--color-surface-muted)] p-4 text-[10px] leading-[1.7] text-[var(--color-text-secondary)]"
    >
      위 금리는 기본금리 기준이며, 우대금리 적용 시 변동될 수 있습니다.<br />
      상품 가입 전 금융기관의 최신 안내를 확인해 주세요.
    </aside>
  </main>
</template>
