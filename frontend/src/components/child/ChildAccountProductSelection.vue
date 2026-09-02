<script setup lang="ts">
import { Check, Landmark } from 'lucide-vue-next'

export type ChildAccountProduct = {
  id: number
  name: string
  bankName: string
  badge: string
  rate: string
  description: string
  tags: string[]
}

defineProps<{
  products: ChildAccountProduct[]
  selectedProductId: number | null
  childName: string
  loading: boolean
  opening: boolean
}>()

const emit = defineEmits<{
  select: [productId: number]
  open: []
}>()

const badgeClasses = [
  'bg-[#e8f6ff] text-[#168fca]',
  'bg-[#eaf8ef] text-[#258b58]',
  'bg-[#fff5dc] text-[#ad7915]',
  'bg-[#f2edff] text-[#7657bd]',
  'bg-[#fff0f2] text-[#ef4d61]',
]

const getBadgeClass = (index: number) => badgeClasses[index % badgeClasses.length]
</script>

<template>
  <section
    class="flex flex-1 flex-col pt-7 pb-[calc(92px+env(safe-area-inset-bottom))]"
    aria-labelledby="child-account-product-title"
  >
    <header>
      <p class="text-[13px] font-extrabold text-[var(--color-selected-text)]">
        {{ childName }} 맞춤 계좌
      </p>
      <h1
        id="child-account-product-title"
        class="mt-2 text-[26px] leading-[1.25] font-extrabold tracking-[-0.04em]"
      >
        만들고 싶은 계좌를<br />선택해주세요
      </h1>
      <p class="mt-3 text-sm leading-6 text-[var(--color-text-secondary)]">
        용돈과 생활비를 안전하게 관리할<br />아이 명의 입출금계좌예요.
      </p>
    </header>

    <div v-if="loading" class="mt-7 grid gap-3" aria-label="계좌 상품 불러오는 중" aria-busy="true">
      <div
        v-for="index in 2"
        :key="index"
        class="h-[188px] animate-pulse rounded-[20px] border border-[#e1e9ed] bg-[#f5f8fa]"
        aria-hidden="true"
      ></div>
    </div>

    <div
      v-else-if="products.length === 0"
      class="mt-7 rounded-[20px] border border-[#d7e9f2] bg-[#f3faff] px-5 py-8 text-center"
    >
      <Landmark class="mx-auto text-[var(--color-brand-primary)]" :size="34" />
      <strong class="mt-4 block text-base">개설 가능한 계좌가 아직 없어요</strong>
      <p class="mt-2 text-[13px] text-[var(--color-text-secondary)]">
        잠시 후 다시 확인해주세요.
      </p>
    </div>

    <ul v-else class="mt-7 grid list-none gap-3 p-0">
      <li v-for="(product, productIndex) in products" :key="product.id">
        <button
          class="w-full rounded-[20px] border p-5 text-left transition-[border-color,background-color,transform,box-shadow] active:scale-[0.99]"
          :class="
            selectedProductId === product.id
              ? 'border-[var(--color-brand-primary)] bg-[#eaf8ff] shadow-[0_7px_22px_rgba(39,169,235,0.12)]'
              : 'border-[var(--color-border)] bg-white shadow-[0_6px_20px_rgba(55,96,118,0.05)]'
          "
          type="button"
          :aria-pressed="selectedProductId === product.id"
          @click="emit('select', product.id)"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0">
              <span
                class="inline-flex rounded-full px-3 py-1.5 text-xs font-bold"
                :class="getBadgeClass(productIndex)"
              >
                {{ product.badge }}
              </span>
              <h2 class="mt-3 text-[18px] leading-tight font-extrabold tracking-[-0.02em]">
                {{ product.name }}
              </h2>
              <span class="mt-1 block text-xs text-[var(--color-text-secondary)]">
                {{ product.bankName }}
              </span>
            </div>
            <span
              class="grid size-8 shrink-0 place-items-center rounded-full transition-colors"
              :class="
                selectedProductId === product.id
                  ? 'bg-[var(--color-brand-primary)] text-white'
                  : 'border border-[#d7e0e5] bg-white text-transparent'
              "
              aria-hidden="true"
            >
              <Check :size="16" :stroke-width="3" />
            </span>
          </div>
          <strong class="mt-4 block text-sm text-[var(--color-selected-text)]">
            {{ product.rate }}
          </strong>
          <p class="mt-3 text-[13px] leading-5 text-[var(--color-text-secondary)]">
            {{ product.description }}
          </p>
          <div v-if="product.tags.length" class="mt-4 flex flex-wrap gap-2">
            <span
              v-for="tag in product.tags"
              :key="tag"
              class="inline-flex min-h-7 items-center rounded-full border bg-white px-3 py-1 text-[11px] font-semibold text-[var(--color-text-secondary)]"
              :class="
                selectedProductId === product.id
                  ? 'border-[#b9dfef] shadow-[0_2px_6px_rgba(39,169,235,0.06)]'
                  : 'border-[#dce5e9]'
              "
            >
              {{ tag }}
            </span>
          </div>
        </button>
      </li>
    </ul>

    <Teleport to="body">
      <div
        class="fixed bottom-0 left-1/2 z-[var(--z-index-bottom-nav)] w-full max-w-[var(--app-max-width)] -translate-x-1/2 bg-white/95 px-5 pt-3 pb-[calc(16px+env(safe-area-inset-bottom))] backdrop-blur-sm"
      >
        <button
          class="primary-action min-h-[56px] w-full rounded-[16px] bg-[var(--color-brand-primary)] text-base font-bold text-white shadow-[0_7px_18px_rgba(39,169,235,0.2)] disabled:cursor-not-allowed disabled:bg-[#cbd8df] disabled:shadow-none"
          type="button"
          :disabled="selectedProductId === null || opening"
          @click="emit('open')"
        >
          {{ opening ? '계좌를 만들고 있어요' : selectedProductId ? '선택한 계좌 만들기' : '계좌를 선택해주세요' }}
        </button>
      </div>
    </Teleport>
  </section>
</template>
