<script setup lang="ts">
import { ArrowUpRight } from 'lucide-vue-next'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const selectedProductName = ref<string | null>(null)

const savingsProducts = [
  {
    name: 'KB Young Youth 적금',
    badge: '자녀 추천',
    badgeClass: 'bg-[#eaf7ff] text-[#179fdf]',
    rate: '최고 연 3.65%',
    period: '12개월',
    description: '자녀가 성년이 될 때까지 오래 함께할 수 있는 어린이·청소년 적금이에요.',
    tags: ['#만19세미만', '#자유적립', '#무료보험'],
    href: 'https://obank.kbstar.com/quics?page=C020702&cc=b061761:b061770&isNew=N&prcode=DP000940#',
  },
  {
    name: 'KB아이사랑적금',
    badge: '최고 금리',
    badgeClass: 'bg-[#fff0f2] text-[#ef4d61]',
    rate: '최고 연 10.00%',
    period: '12개월',
    description: '아이 키우는 가정의 목돈 마련을 응원하는 가족 맞춤형 적금이에요.',
    tags: ['#아이사랑', '#육아응원', '#월30만원'],
    href: 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=Y&prcode=DP01001587',
  },
  {
    name: '내 아이를 위한 280일 적금',
    badge: '출산 준비',
    badgeClass: 'bg-[#fff7df] text-[#d99520]',
    rate: '최고 연 3.55%',
    period: '6~12개월',
    description: '아이를 기다리는 280일 동안 즐겁게 저축하는 출산 준비 특화 적금이에요.',
    tags: ['#임산부추천', '#태명저축', '#출산준비'],
    href: 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=Y&prcode=DP01000944',
  },
] as const
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-8 pb-[calc(28px+env(safe-area-inset-bottom))] text-[var(--color-text-primary)]"
  >
    <header>
      <span class="text-sm font-bold text-[var(--color-selected-text)]">깨비 맞춤 추천</span>
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
            class="savings-card group relative rounded-[20px] border bg-white p-5"
            :class="
              selectedProductName === product.name
                ? 'savings-card--selected border-[var(--color-brand-primary)] bg-[#f7fcff]'
                : 'border-[var(--color-border)]'
            "
          >
            <button
              class="absolute inset-0 z-0 rounded-[20px]"
              type="button"
              :aria-label="`${product.name} 선택`"
              :aria-pressed="selectedProductName === product.name"
              @click="selectedProductName = product.name"
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
                class="rounded-full bg-[#f5f7f8] px-3 py-1.5 text-[11px] font-semibold text-[var(--color-text-secondary)]"
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
        @click="
          router.push({
            name: 'SavingsOpenComplete',
            query: { product: selectedProductName },
          })
        "
      >
        {{ selectedProductName ? '선택한 적금 등록하기' : '적금을 선택해주세요' }}
      </button>
    </div>
  </main>
</template>

<style scoped>
.savings-card {
  box-shadow: 0 6px 20px rgb(55 96 118 / 5%);
  transition: border-color 160ms ease, box-shadow 160ms ease, transform 160ms ease;
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
