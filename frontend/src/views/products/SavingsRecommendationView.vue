<script setup lang="ts">
import { ArrowUpRight, LoaderCircle, PiggyBank, RotateCw } from 'lucide-vue-next'
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, getApiErrorMessage } from '@/api'
import { hasParentDemandDepositAccount, resolveCurrentChildId } from '@/api/context'
import { useToast } from '@/composables/useToast'
import { addOpenedSavingsToGoalSetupDraft } from '@/utils/goalSetupDraft'

const router = useRouter()
const route = useRoute()
const { showToast } = useToast()
const selectedProductName = ref<string | null>(null)
const selectedProductId = ref<number | null>(null)
const selectedProductOwnerType = ref<'PARENT' | 'CHILD' | null>(null)
const isLoading = ref(true)
const isOpening = ref(false)

type ProductApiItem = {
  financial_product_id?: number
  name?: string
  bank_name?: string
  interest_rate?: { base_rate?: number; max_rate?: number }
  contract_period?: { min_months?: number; max_months?: number }
  monthly_deposit?: { max_amount?: number }
  summary?: string
  curation_reason?: string
  detail_url?: string
  badges?: Array<{ label?: string }>
  hashtags?: string[]
  highlight_label?: string
  max_interest_rate?: number
  target_owner_type?: string
}

type SavingsProduct = {
  id: number
  name: string
  bankName: string
  badge: string
  badgeClass: string
  maxRate: string
  baseRate: string | null
  period: string
  monthlyLimit: string | null
  description: string
  tags: string[]
  href: string | null
  ownerType: 'PARENT' | 'CHILD'
}

const savingsProducts = ref<SavingsProduct[]>([])

const selectProduct = (product: { id: number; name: string; ownerType: 'PARENT' | 'CHILD' }) => {
  selectedProductId.value = product.id
  selectedProductName.value = product.name
  selectedProductOwnerType.value = product.ownerType
}

const openSelectedProduct = async () => {
  if (
    selectedProductId.value === null ||
    !selectedProductName.value ||
    selectedProductOwnerType.value === null ||
    isOpening.value
  ) return
  isOpening.value = true
  try {
    if (!(await hasParentDemandDepositAccount())) {
      showToast('자녀 적금 가입 전에 부모 입출금계좌를 먼저 등록해 주세요.', 'error')
      await router.push({
        name: 'Accounts',
        query: { next: router.currentRoute.value.fullPath },
      })
      return
    }
    const ownerType = selectedProductOwnerType.value
    const childId = ownerType === 'CHILD' ? await resolveCurrentChildId() : undefined
    const { data: openedAccount } = await api.openUsingPOST(undefined, {
      child_id: childId,
      financial_product_id: selectedProductId.value,
      initial_deposit_amount: 0,
      owner_type: ownerType,
    })
    addOpenedSavingsToGoalSetupDraft(openedAccount.account_id)
    await router.push({
      name: 'SavingsOpenComplete',
      query: {
        product: selectedProductName.value,
        ...(route.query.from === 'goal-setup' ? { resumeGoal: 'true' } : {}),
      },
    })
  } catch (error) {
    showToast(getApiErrorMessage(error, '적금을 등록하지 못했습니다.'), 'error')
  } finally {
    isOpening.value = false
  }
}

const formatPeriod = (period?: ProductApiItem['contract_period']) => {
  if (!period?.min_months && !period?.max_months) return '기간 정보 확인 필요'
  if (period.min_months && period.max_months && period.min_months !== period.max_months) {
    return `${period.min_months}~${period.max_months}개월`
  }
  return `${period.max_months ?? period.min_months}개월`
}

const normalizeExternalUrl = (url?: string) => {
  const trimmedUrl = url?.trim()
  if (!trimmedUrl) return null
  if (/^https?:\/\//i.test(trimmedUrl)) return trimmedUrl
  if (trimmedUrl.startsWith('//')) return `https:${trimmedUrl}`
  return `https://${trimmedUrl.replace(/^\/+/, '')}`
}

const getBadgeClass = (label: string, index: number) => {
  if (label.includes('최고') || label.includes('금리')) {
    return 'bg-[#fff5d8] text-[#a96f00]'
  }
  if (label.includes('출산') || label.includes('준비')) {
    return 'bg-[#e9f8ef] text-[#168a56]'
  }
  if (label.includes('부모') || label.includes('가족')) {
    return 'bg-[#f2edff] text-[#7253c7]'
  }
  if (label.includes('자녀')) {
    return 'bg-[#fff0f2] text-[#ef4d61]'
  }
  if (label.includes('아이')) {
    return 'bg-[#eaf7ff] text-[#179fdf]'
  }

  const fallbackClasses = [
    'bg-[#eaf7ff] text-[#179fdf]',
    'bg-[#fff0f2] text-[#ef4d61]',
    'bg-[#fff5d8] text-[#a96f00]',
    'bg-[#e9f8ef] text-[#168a56]',
    'bg-[#f2edff] text-[#7253c7]',
  ]
  return fallbackClasses[index % fallbackClasses.length]!
}

const formatRate = (baseRate?: number | null, maxRate?: number | null) => {
  if (baseRate != null && maxRate != null && baseRate !== maxRate) {
    return `연 ${baseRate.toFixed(2)}~${maxRate.toFixed(2)}%`
  }
  if (maxRate != null) return `최고 연 ${maxRate.toFixed(2)}%`
  if (baseRate != null) return `연 ${baseRate.toFixed(2)}%`
  return '금리 확인 필요'
}

const loadSavingsProducts = async () => {
  isLoading.value = true
  try {
    const { data } = await api.getProductsUsingGET(undefined, undefined, 'SAVINGS', 20)
    const items = (data.items ?? []) as unknown as ProductApiItem[]
    const productsWithDetails = await Promise.all(
      items.map(async (product) => {
        if (!product.financial_product_id) return product
        try {
          const { data: detail } = await api.getProductDetailUsingGET(
            product.financial_product_id,
          )
          return { ...product, ...(detail as ProductApiItem) }
        } catch {
          return product
        }
      }),
    )

    savingsProducts.value = productsWithDetails.flatMap((product, index) => {
      if (!product.financial_product_id) return []
      const badge = product.badges?.[0]?.label ?? product.highlight_label ?? '아이 맞춤'

      return [{
      id: product.financial_product_id,
      name: product.name ?? '저축 상품',
      bankName: product.bank_name ?? 'KB국민은행',
      badge,
      badgeClass: getBadgeClass(badge, index),
      maxRate: formatRate(
        product.interest_rate?.base_rate,
        product.interest_rate?.max_rate ?? product.max_interest_rate,
      ),
      baseRate:
        product.interest_rate?.base_rate == null
          ? null
          : `기본 연 ${product.interest_rate.base_rate}%`,
      period: formatPeriod(product.contract_period),
      monthlyLimit: product.monthly_deposit?.max_amount
        ? `월 최대 ${product.monthly_deposit.max_amount.toLocaleString('ko-KR')}원`
        : null,
      description: product.curation_reason?.trim() || product.summary?.trim() || '상품 상세 혜택과 가입 조건을 확인해보세요.',
      tags: product.hashtags ?? [],
      href: normalizeExternalUrl(product.detail_url),
      ownerType: product.target_owner_type === 'PARENT' ? 'PARENT' : 'CHILD',
    }]
    })
  } catch (error) {
    savingsProducts.value = []
    showToast(getApiErrorMessage(error, '추천 상품을 불러오지 못했습니다.'), 'error')
  } finally {
    isLoading.value = false
  }
}

onMounted(loadSavingsProducts)
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-3 pb-[calc(104px+env(safe-area-inset-bottom))] text-[var(--color-text-primary)]">
    <header>
      <h1 class="mt-2 text-[28px] leading-[1.3] font-extrabold tracking-[-0.035em]">
        아이의 미래에 꼭 맞는<br />KB 적금을 골라봤어요
      </h1>
      <p class="mt-3 text-sm leading-6 text-[var(--color-text-secondary)]">
        상품별 혜택을 비교하고 원하는 적금을 선택해보세요.
      </p>
    </header>

    <section class="mt-7" aria-labelledby="recommended-savings-title">
      <h2 id="recommended-savings-title" class="sr-only">추천 적금 상품</h2>

      <div v-if="isLoading" class="grid gap-3" aria-label="추천 적금 불러오는 중" aria-busy="true">
        <article v-for="index in 3" :key="index" class="rounded-[20px] border border-[#e3eaee] bg-white p-5">
          <div class="savings-skeleton h-7 w-20 rounded-full"></div>
          <div class="savings-skeleton mt-4 h-6 w-3/4 rounded"></div>
          <div class="savings-skeleton mt-4 h-4 w-1/2 rounded"></div>
          <div class="savings-skeleton mt-4 h-4 w-full rounded"></div>
        </article>
      </div>

      <div v-else-if="savingsProducts.length === 0" class="rounded-[20px] border border-[var(--color-border)] bg-white px-5 py-10 text-center">
        <PiggyBank class="mx-auto text-[var(--color-brand-primary)]" :size="38" aria-hidden="true" />
        <strong class="mt-4 block text-base">추천할 적금이 아직 없어요</strong>
        <button class="mt-4 inline-flex items-center gap-1 border-0 bg-transparent text-sm font-bold text-[var(--color-selected-text)] underline underline-offset-4" type="button" @click="loadSavingsProducts">
          <RotateCw :size="15" /> 다시 불러오기
        </button>
      </div>

      <ul v-else class="grid list-none gap-3 p-0">
        <li v-for="product in savingsProducts" :key="product.id">
          <article class="savings-card group relative rounded-[20px] border p-5" :class="selectedProductId === product.id ? 'savings-card--selected border-[var(--color-brand-primary)] bg-[#e8f7ff]' : 'border-[var(--color-border)] bg-white'">
            <button class="absolute inset-0 z-0 rounded-[20px]" type="button" :aria-label="`${product.name} 선택`" :aria-pressed="selectedProductId === product.id" @click="selectProduct(product)"></button>

            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0">
                <span class="inline-flex h-7 items-center rounded-full px-3 text-xs font-bold" :class="product.badgeClass">{{ product.badge }}</span>
                <h3 class="mt-3 text-[18px] leading-tight font-extrabold tracking-[-0.02em]">{{ product.name }}</h3>
              </div>
              <a v-if="product.href" class="relative z-10 grid size-10 shrink-0 place-items-center rounded-full bg-[#f3f7f9] text-[var(--color-text-secondary)] transition-colors active:bg-[#e7f6fd] active:text-[var(--color-selected-text)]" :href="product.href" target="_blank" rel="noopener noreferrer" :aria-label="`${product.name} 상품 사이트로 이동`" @click.stop>
                <ArrowUpRight :size="19" :stroke-width="2.2" />
              </a>
            </div>

            <div class="mt-3 flex flex-wrap items-center gap-2 text-sm font-bold">
              <strong class="text-[var(--color-selected-text)]">{{ product.maxRate }}</strong>
              <span class="text-[#c7d0d5]" aria-hidden="true">·</span>
              <span>{{ product.period }}</span>
            </div>
            <p class="mt-3 text-[13px] leading-5 text-[var(--color-text-secondary)]">{{ product.description }}</p>
            <div v-if="product.tags.length" class="mt-4 flex flex-wrap gap-2" aria-label="상품 특징">
              <span
                v-for="tag in product.tags"
                :key="tag"
                class="rounded-full px-3 py-1.5 text-[11px] font-semibold text-[var(--color-text-secondary)]"
                :class="selectedProductId === product.id ? 'bg-white' : 'bg-[#f5f7f8]'"
              >
                {{ tag }}
              </span>
            </div>
          </article>
        </li>
      </ul>
    </section>

    <Teleport to="body">
      <div class="fixed bottom-0 left-1/2 z-[var(--z-index-bottom-nav)] w-full max-w-[var(--app-max-width)] -translate-x-1/2 bg-white/95 px-5 pt-3 pb-[calc(16px+env(safe-area-inset-bottom))] backdrop-blur-sm">
        <button class="flex min-h-14 w-full items-center justify-center gap-2 rounded-xl bg-[var(--color-brand-primary)] text-base font-bold text-white shadow-[0_7px_18px_rgba(39,169,235,0.2)] disabled:bg-[#cbd8df] disabled:shadow-none" type="button" :disabled="!selectedProductName || isOpening" @click="openSelectedProduct">
          <LoaderCircle v-if="isOpening" class="animate-spin" :size="19" />
          {{ isOpening ? '등록하는 중...' : selectedProductName ? '선택한 적금 등록하기' : '적금을 선택해주세요' }}
        </button>
      </div>
    </Teleport>
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

.savings-skeleton {
  background: linear-gradient(90deg, #e9eef1 25%, #f7f9fa 50%, #e9eef1 75%);
  background-size: 200% 100%;
  animation: savings-skeleton-shimmer 1.35s ease-in-out infinite;
}

@keyframes savings-skeleton-shimmer {
  from { background-position: 200% 0; }
  to { background-position: -200% 0; }
}

@media (prefers-reduced-motion: reduce) {
  .savings-card,
  .savings-card :deep(*) {
    transition-duration: 1ms;
  }

  .savings-skeleton { animation: none; }
}
</style>
