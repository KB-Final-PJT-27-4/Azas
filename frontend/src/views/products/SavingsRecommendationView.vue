<script setup lang="ts">
import { ArrowUpRight } from 'lucide-vue-next'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, getApiErrorMessage } from '@/api'
import { resolveCurrentChildId } from '@/api/context'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { showToast } = useToast()
const selectedProductName = ref<string | null>(null)
const selectedProductId = ref<number | null>(null)

type ProductApiItem = { financial_product_id?: number; name?: string; interest_rate?: { max_rate?: number }; contract_period?: { min_months?: number; max_months?: number }; summary?: string; detail_url?: string; badges?: Array<{ name?: string }>; product_type?: string }
const savingsProducts = ref<Array<{ id: number; name: string; badge: string; badgeClass: string; rate: string; period: string; description: string; tags: string[]; href: string }>>([])

const selectProduct = (product: { id: number; name: string }) => {
  selectedProductId.value = product.id
  selectedProductName.value = product.name
}

const openSelectedProduct = async () => {
  if (!selectedProductId.value || !selectedProductName.value) return
  try {
    const childId = await resolveCurrentChildId()
    await api.openUsingPOST(undefined, {
      child_id: childId,
      financial_product_id: selectedProductId.value,
      initial_deposit_amount: 0,
      owner_type: 'CHILD',
    })
    await router.push({ name: 'SavingsOpenComplete', query: { product: selectedProductName.value } })
  } catch (error) {
    showToast(getApiErrorMessage(error, '적금을 등록하지 못했습니다.'), 'error')
  }
}

onMounted(async () => {
  try {
    const { data } = await api.getProductsUsingGET(undefined, undefined, 'SAVINGS', 20)
    const items = (data.items ?? []) as unknown as ProductApiItem[]
    savingsProducts.value = items.map((product, index) => ({
      id: product.financial_product_id ?? 0,
      name: product.name ?? '저축 상품',
      badge: product.badges?.[0]?.name ?? '자녀 추천',
      badgeClass: index % 2 ? 'bg-[#fff0f2] text-[#ef4d61]' : 'bg-[#eaf7ff] text-[#179fdf]',
      rate: product.interest_rate?.max_rate === undefined ? '-' : `최고 연 ${product.interest_rate.max_rate}%`,
      period: product.contract_period?.max_months ? `${product.contract_period.max_months}개월` : '-',
      description: product.summary ?? '',
      tags: [],
      href: product.detail_url ?? '#',
    }))
  } catch (error) {
    showToast(getApiErrorMessage(error, '추천 상품을 불러오지 못했습니다.'), 'error')
  }
})
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-3 pb-[calc(28px+env(safe-area-inset-bottom))] text-[var(--color-text-primary)]"
  >
    <header>
      <h1 class="mt-2 text-[28px] leading-[1.3] font-extrabold tracking-[-0.035em]">
        아이의 미래에 꼭 맞는<br />KB 적금을 골라봤어요
      </h1>
      <p class="mt-3 text-sm leading-6 text-[var(--color-text-secondary)]">
        상품별 혜택을 비교하고 원하는 적금을 선택해보세요.
      </p>
    </header>

    <section class="mt-7" aria-labelledby="recommended-savings-title">
      <h2 id="recommended-savings-title" class="sr-only">추천 적금 상품 3개</h2>
      <ul class="grid list-none gap-3 p-0">
        <li v-for="product in savingsProducts" :key="product.name">
          <article
            class="savings-card group relative rounded-[20px] border p-5"
            :class="
              selectedProductName === product.name
                ? 'savings-card--selected border-[var(--color-brand-primary)] bg-[#e8f7ff]'
                : 'border-[var(--color-border)] bg-white'
            "
          >
            <button
              class="absolute inset-0 z-0 rounded-[20px]"
              type="button"
              :aria-label="`${product.name} 선택`"
              :aria-pressed="selectedProductName === product.name"
              @click="selectProduct(product)"
            ></button>
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0">
                <span
                  class="inline-flex h-7 items-center rounded-full px-3 text-xs font-bold"
                  :class="product.badgeClass"
                >
                  {{ product.badge }}
                </span>
                <h3 class="mt-3 text-[18px] leading-tight font-extrabold tracking-[-0.02em]">
                  {{ product.name }}
                </h3>
              </div>
              <a
                class="relative z-10 grid size-10 shrink-0 place-items-center rounded-full bg-[#f3f7f9] text-[var(--color-text-secondary)] transition-colors active:bg-[#e7f6fd] active:text-[var(--color-selected-text)]"
                :href="product.href"
                target="_blank"
                rel="noopener noreferrer"
                :aria-label="`${product.name} KB국민은행 상품 페이지에서 보기`"
              >
                <ArrowUpRight :size="19" :stroke-width="2.2" />
              </a>
            </div>

            <div class="mt-3 flex items-center gap-2 text-sm font-bold">
              <strong class="text-[var(--color-selected-text)]">{{ product.rate }}</strong>
              <span class="text-[#c7d0d5]" aria-hidden="true">·</span>
              <span>{{ product.period }}</span>
            </div>

            <p class="mt-3 text-[13px] leading-5 text-[var(--color-text-secondary)]">
              {{ product.description }}
            </p>

            <div class="mt-4 flex flex-wrap gap-2" aria-label="상품 특징">
              <span
                v-for="tag in product.tags"
                :key="tag"
                class="rounded-full px-3 py-1.5 text-[11px] font-semibold text-[var(--color-text-secondary)] transition-colors"
                :class="selectedProductName === product.name ? 'bg-white' : 'bg-[#f5f7f8]'"
              >
                {{ tag }}
              </span>
            </div>
          </article>
        </li>
      </ul>
    </section>


    <div class="sticky bottom-0 z-20 -mx-5 mt-3 bg-white/95 px-5 pt-3 pb-[calc(12px+env(safe-area-inset-bottom))] backdrop-blur-sm">
      <button
        class="min-h-14 w-full rounded-xl bg-[var(--color-brand-primary)] text-base font-bold text-white shadow-[0_7px_18px_rgba(39,169,235,0.2)] transition-colors active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[#cbd8df] disabled:shadow-none"
        type="button"
        :disabled="!selectedProductName"
        @click="openSelectedProduct"
      >
        {{ selectedProductName ? '선택한 적금 등록하기' : '적금을 선택해주세요' }}
      </button>
    </div>
  </main>
</template>

<style scoped>
.savings-card {
  box-shadow: 0 6px 20px rgb(55 96 118 / 5%);
  transition: border-color 160ms ease, background-color 160ms ease, box-shadow 160ms ease,
    transform 160ms ease;
}

.savings-card:active {
  border-color: rgb(39 169 235 / 35%);
  box-shadow: 0 3px 12px rgb(55 96 118 / 7%);
  transform: scale(0.985);
}

.savings-card--selected {
  box-shadow: 0 7px 22px rgb(39 169 235 / 12%);
}

.savings-card:focus-visible {
  outline: 3px solid rgb(39 169 235 / 20%);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .savings-card,
  .savings-card :deep(*) {
    transition-duration: 1ms;
  }
}
</style>
