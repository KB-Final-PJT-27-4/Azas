<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ChevronRight, Heart } from 'lucide-vue-next'

import { api, getApiErrorMessage } from '@/api'
import { useToast } from '@/composables/useToast'
import { resolveCurrentChildId } from '@/api/context'
import productMascot1 from '@/assets/images/products/product-1.png'
import productMascot2 from '@/assets/images/products/product-2.png'
import productMascot3 from '@/assets/images/products/product-3.png'
import productMascot4 from '@/assets/images/products/product-4.png'

type Product = { id: string; name: string; bankName: string; type: '적금' | '입출금계좌'; rate: string; period: string; monthlyLimit: string; mascot: string }
type ProductApiItem = {
  financial_product_id?: number; name?: string; bank_name?: string; product_type?: string
  target_owner_type?: string
  interest_rate?: { max_rate?: number }; contract_period?: { max_months?: number }
  monthly_deposit?: { max_amount?: number }
}
type BookmarkApiItem = { financial_product_id?: number }
const { showToast } = useToast()
const recommendedProducts = ref<Product[]>([])
const childId = ref<number | null>(null)
const PRODUCT_PAGE_SIZE = 3
const nextProductCursor = ref<string | undefined>()
const hasNextProductPage = ref(true)
const isLoadingMoreProducts = ref(false)

const favoriteProductIds = ref(new Set<string>())
type ProductFilter = '전체' | '적금' | '입출금계좌' | '관심상품'

const productFilters: ProductFilter[] = ['전체', '적금', '입출금계좌', '관심상품']
const selectedProductFilter = ref<ProductFilter>('전체')

const filteredProducts = computed(() => {
  if (selectedProductFilter.value === '전체') return recommendedProducts.value
  if (selectedProductFilter.value === '관심상품') {
    return recommendedProducts.value.filter((product) => favoriteProductIds.value.has(product.id))
  }

  return recommendedProducts.value.filter((product) => product.type === selectedProductFilter.value)
})

const productSectionTitle = computed(() =>
  selectedProductFilter.value === '관심상품' ? '관심 상품' : '추천 상품',
)

const mapProduct = (product: ProductApiItem): Product => {
  const mascots = [productMascot1, productMascot2, productMascot3, productMascot4]
  const productId = product.financial_product_id ?? 0

  return {
    id: String(product.financial_product_id ?? ''),
    name: product.name ?? '금융 상품',
    bankName: product.bank_name ?? '',
    type: product.product_type === 'DEMAND_DEPOSIT' ? '입출금계좌' : '적금',
    rate:
      product.interest_rate?.max_rate !== undefined
        ? `${product.interest_rate.max_rate}%`
        : '-',
    period: product.contract_period?.max_months
      ? `${product.contract_period.max_months}개월`
      : '-',
    monthlyLimit: product.monthly_deposit?.max_amount
      ? `${Math.round(product.monthly_deposit.max_amount / 10000)}만원`
      : '-',
    mascot: mascots[productId % mascots.length]!,
  }
}

const loadMoreProducts = async () => {
  if (isLoadingMoreProducts.value || !hasNextProductPage.value) return

  isLoadingMoreProducts.value = true

  try {
    const newProducts: Product[] = []

    // 부모 전용 상품은 화면에서 제외되므로, 실제 표시할 상품이 채워질 때까지 다음 커서를 조회해요.
    while (hasNextProductPage.value && newProducts.length < PRODUCT_PAGE_SIZE) {
      const { data } = await api.getProductsUsingGET(
        undefined,
        nextProductCursor.value,
        undefined,
        PRODUCT_PAGE_SIZE,
      )
      const items = (data.items ?? []) as unknown as ProductApiItem[]
      const childEligibleItems = items.filter(
        ({ target_owner_type }) =>
          !target_owner_type || target_owner_type === 'CHILD' || target_owner_type === 'BOTH',
      )

      newProducts.push(...childEligibleItems.map(mapProduct))
      nextProductCursor.value = data.next_cursor
      hasNextProductPage.value = Boolean(data.has_next && data.next_cursor)
    }

    const existingIds = new Set(recommendedProducts.value.map(({ id }) => id))
    recommendedProducts.value.push(
      ...newProducts.filter(({ id }) => id && !existingIds.has(id)),
    )
  } catch (error) {
    showToast(getApiErrorMessage(error, '상품 목록을 더 불러오지 못했습니다.'), 'error')
  } finally {
    isLoadingMoreProducts.value = false
  }
}

const toggleFavorite = async (productId: string) => {
  const nextFavorites = new Set(favoriteProductIds.value)
  const isBookmarked = !nextFavorites.has(productId)
  try {
    if (!childId.value) childId.value = await resolveCurrentChildId()
    const { data } = await api.updateBookmarkUsingPUT(childId.value, Number(productId), {
      is_bookmarked: isBookmarked,
    })
    const savedBookmarkState = data.is_bookmarked ?? isBookmarked
    if (savedBookmarkState) nextFavorites.add(productId)
    else nextFavorites.delete(productId)
    favoriteProductIds.value = nextFavorites
  } catch (error) {
    showToast(getApiErrorMessage(error, '관심상품을 변경하지 못했습니다.'), 'error')
  }
}

onMounted(async () => {
  try {
    childId.value = await resolveCurrentChildId()
    const [, { data: bookmarks }] = await Promise.all([
      loadMoreProducts(),
      api.getBookmarksUsingGET(childId.value, undefined, 0, undefined, 50),
    ])
    const bookmarkedItems = (bookmarks.content ?? []) as unknown as BookmarkApiItem[]
    favoriteProductIds.value = new Set(
      bookmarkedItems
        .map(({ financial_product_id }) => financial_product_id)
        .filter((productId): productId is number => productId != null)
        .map(String),
    )
  } catch (error) {
    showToast(getApiErrorMessage(error, '상품 목록을 불러오지 못했습니다.'), 'error')
  }
})
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-[var(--color-surface)] px-5 pt-4 pb-8 text-[var(--color-text-primary)]"
  >
    <section>
      <header
        class="relative overflow-hidden rounded-[22px] bg-[var(--color-selected-background)] p-5"
      >
        <div class="relative flex items-start gap-3">
          <div>
            <p class="m-0 text-[11px] font-bold text-[var(--color-selected-text)]">
              우리 아이를 위한 금융상품
            </p>
            <h1 class="mt-1 mb-0 text-[21px] leading-[1.35] font-bold tracking-[-0.025em]">
              조건에 맞는 상품을<br />한눈에 비교해 보세요
            </h1>
          </div>
        </div>
      </header>

      <h2 class="mt-5 mb-0 flex items-center gap-2 text-[16px] font-extrabold">
        <span>{{ productSectionTitle }}</span>
        <span
          class="rounded-full bg-[var(--color-selected-background)] px-2.5 py-1 text-[11px] font-bold text-[var(--color-selected-text)]"
        >
          {{ filteredProducts.length }}개
        </span>
      </h2>

      <fieldset class="mt-3">
        <legend class="sr-only">상품 유형 필터</legend>
        <div
          class="grid grid-cols-4 rounded-[14px] border border-[var(--color-border)] bg-[var(--color-surface-muted)] p-1"
        >
          <button
            v-for="filter in productFilters"
            :key="filter"
            class="flex h-10 min-w-0 items-center justify-center gap-1 rounded-[10px] px-1 text-[11px] font-bold whitespace-nowrap transition-all duration-200"
            :class="
              selectedProductFilter === filter
                ? 'bg-[var(--color-surface)] text-[var(--color-selected-text)] shadow-[0_2px_8px_rgb(43_171_232_/_16%)]'
                : 'text-[var(--color-text-secondary)] active:bg-[var(--color-surface)]'
            "
            type="button"
            :aria-pressed="selectedProductFilter === filter"
            @click="selectedProductFilter = filter"
          >
            {{ filter }}
          </button>
        </div>
      </fieldset>

      <ul v-if="filteredProducts.length" class="mt-4 mb-0 grid list-none gap-3 p-0">
        <li v-for="product in filteredProducts" :key="product.id">
          <article
            class="relative overflow-hidden rounded-[18px] border border-[var(--color-border)] bg-[var(--color-surface)] p-5 shadow-sm transition-shadow hover:shadow-md"
          >
            <RouterLink
              class="absolute inset-0 z-10 rounded-[18px] transition-colors outline-none active:bg-[rgb(43_171_232_/_5%)] focus-visible:ring-2 focus-visible:ring-[var(--color-brand-primary)] focus-visible:ring-inset"
              :to="{ name: 'ProductDetail', params: { productId: product.id } }"
              :aria-label="`${product.name} 상세 보기`"
            />

            <div class="flex items-start gap-2 pr-10">
              <h3 class="m-0 truncate text-[17px] font-extrabold">{{ product.name }}</h3>
              <span
                class="shrink-0 rounded-full px-3 py-1 text-[10px] font-bold"
                :class="
                  product.type === '입출금계좌'
                    ? 'bg-[var(--color-brand-secondary)] text-[#a67d18]'
                    : 'bg-[var(--color-selected-background)] text-[var(--color-selected-text)]'
                "
              >
                {{ product.type }}
              </span>
            </div>
            <p class="mt-1.5 mb-0 text-[12px] text-[var(--color-text-secondary)]">
              {{ product.bankName }}
            </p>

            <button
              class="absolute top-5 right-5 z-20 grid size-8 place-items-center rounded-full active:bg-[var(--color-selected-background)]"
              type="button"
              :aria-label="
                favoriteProductIds.has(product.id)
                  ? `${product.name} 관심상품 해제`
                  : `${product.name} 관심상품 추가`
              "
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

            <div
              class="mt-1 flex items-center justify-between border-t border-[var(--color-border)] pt-3 text-[11px] font-semibold text-[var(--color-text-secondary)]"
            >
              <span>카드를 눌러 상세 정보를 확인하세요</span>
              <span
                class="grid size-7 place-items-center rounded-full bg-[var(--color-selected-background)] text-[var(--color-selected-text)]"
                aria-hidden="true"
              >
                <ChevronRight :size="16" :stroke-width="2.2" />
              </span>
            </div>
          </article>
        </li>
      </ul>

      <div
        v-else
        class="mt-4 rounded-[18px] border border-[var(--color-border)] bg-[var(--color-surface)] px-5 py-16 text-center"
        role="status"
      >
        <p class="m-0 text-[14px] font-bold">
          {{
            selectedProductFilter === '관심상품'
              ? '저장한 관심 상품이 없어요.'
              : '해당 유형의 추천 상품이 없어요.'
          }}
        </p>
        <p class="mt-2 mb-0 text-[12px] text-[var(--color-text-secondary)]">
          {{
            selectedProductFilter === '관심상품'
              ? '상품 카드의 하트를 눌러 관심 상품을 모아보세요.'
              : '다른 상품 유형을 선택해 주세요.'
          }}
        </p>
      </div>

      <button
        v-if="hasNextProductPage && selectedProductFilter !== '관심상품'"
        class="mt-3 h-12 w-full rounded-[12px] border border-[var(--color-border)] bg-[var(--color-surface)] text-[14px] font-extrabold text-[var(--color-text-secondary)] active:bg-[var(--color-surface-muted)]"
        type="button"
        :disabled="isLoadingMoreProducts"
        @click="loadMoreProducts"
      >
        {{ isLoadingMoreProducts ? '상품을 불러오는 중이에요' : '추천 상품 더 보기' }}
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
