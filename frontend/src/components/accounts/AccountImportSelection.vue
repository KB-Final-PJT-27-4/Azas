<script setup lang="ts">
import { Check, Landmark } from 'lucide-vue-next'
import { computed, onMounted, ref } from 'vue'
import { api, getApiErrorMessage } from '@/api'
import { useToast } from '@/composables/useToast'

type ImportedAccount = {
  id: number
  bank: string
  number: string
  balance: number
  productType: string
}

const props = defineProps<{
  accounts: ImportedAccount[]
  ownerType?: 'PARENT' | 'CHILD'
}>()

const emit = defineEmits<{
  connect: [accounts: ImportedAccount[]]
  createAccount: [productId: number]
  later: []
}>()

const selectedAccountIds = ref<number[]>(props.accounts[0] ? [props.accounts[0].id] : [])
const selectedAccounts = computed(() =>
  props.accounts.filter(({ id }) => selectedAccountIds.value.includes(id)),
)

type ProductApiItem = {
  financial_product_id?: number
  bank_name?: string
  name?: string
  target_owner_type?: string
  highlight_label?: string
  summary?: string
  hashtags?: string[]
  max_interest_rate?: number
  contract_period?: { min_months?: number; max_months?: number }
}

type RecommendedAccount = {
  id: number
  bankName: string
  name: string
  badge: string
  badgeClass: string
  rate: string
  period: string
  description: string
  tags: string[]
}

const { showToast } = useToast()
const recommendedAccounts = ref<RecommendedAccount[]>([])
const selectedRecommendedAccountId = ref<number | null>(null)
const isRecommendationsLoading = ref(false)
const recommendationBadgeClasses = [
  'bg-[#e8f6ff] text-[#168fca]',
  'bg-[#eaf8ef] text-[#258b58]',
  'bg-[#fff5dc] text-[#ad7915]',
  'bg-[#f2edff] text-[#7657bd]',
  'bg-[#fff0f2] text-[#ef4d61]',
]

const formatPeriod = (period?: ProductApiItem['contract_period']) => {
  if (!period?.min_months && !period?.max_months) return '입출금 자유'
  if (period.min_months && period.max_months && period.min_months !== period.max_months) {
    return `${period.min_months}~${period.max_months}개월`
  }
  return `${period.max_months ?? period.min_months}개월`
}

const loadRecommendedAccounts = async () => {
  if (props.accounts.length > 0) return
  isRecommendationsLoading.value = true
  try {
    const { data } = await api.getProductsUsingGET(undefined, undefined, 'DEMAND_DEPOSIT', 20)
    const ownerType = props.ownerType ?? 'PARENT'
    const visibleProducts = ((data.items ?? []) as unknown as ProductApiItem[]).filter(
      (product) =>
        Boolean(product.financial_product_id) &&
        (!product.target_owner_type ||
          product.target_owner_type === 'BOTH' ||
          product.target_owner_type === ownerType),
    )

    recommendedAccounts.value = visibleProducts.map((product, index) => ({
      id: product.financial_product_id!,
      bankName: product.bank_name ?? 'KB국민은행',
      name: product.name ?? 'KB 입출금통장',
      badge: product.highlight_label ?? (ownerType === 'CHILD' ? '자녀 추천' : '부모 추천'),
      badgeClass: recommendationBadgeClasses[index % recommendationBadgeClasses.length]!,
      rate:
        product.max_interest_rate == null
          ? '금리 확인 필요'
          : `최고 연 ${product.max_interest_rate}%`,
      period: formatPeriod(product.contract_period),
      description:
        product.summary ?? '자금을 편리하게 관리할 수 있는 KB국민은행 입출금계좌예요.',
      tags: product.hashtags ?? [],
    }))
  } catch (error) {
    showToast(getApiErrorMessage(error, '추천 계좌를 불러오지 못했습니다.'), 'error')
  } finally {
    isRecommendationsLoading.value = false
  }
}

const toggleAccount = (accountId: number) => {
  selectedAccountIds.value = selectedAccountIds.value.includes(accountId)
    ? selectedAccountIds.value.filter((id) => id !== accountId)
    : [...selectedAccountIds.value, accountId]
}

onMounted(loadRecommendedAccounts)
</script>

<template>
  <section
    v-if="accounts.length"
    class="flex min-h-[calc(100dvh-var(--app-header-height))] flex-col px-5 pt-7 pb-5"
    aria-labelledby="imported-account-title"
  >
    <div>
      <h1
        id="imported-account-title"
        class="text-[22px] leading-tight font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]"
      >
        연결할 계좌를 선택해주세요
      </h1>
      <div class="mt-3 flex items-center justify-between text-sm">
        <p class="text-[var(--color-text-secondary)]">{{ accounts.length }}개의 계좌</p>
        <span class="font-bold text-[var(--color-selected-text)]">{{ selectedAccounts.length }}개 선택</span>
      </div>

      <div class="mt-3 grid gap-3" role="group" aria-label="불러온 계좌 목록">
        <button
          v-for="account in accounts"
          :key="account.id"
          class="imported-account flex min-h-[88px] w-full items-center rounded-2xl border px-4 text-left"
          :class="
            selectedAccountIds.includes(account.id)
              ? 'border-[#8cd7fa] bg-[#f2fbff]'
              : 'border-[var(--color-border)] bg-white'
          "
          type="button"
          :aria-pressed="selectedAccountIds.includes(account.id)"
          @click="toggleAccount(account.id)"
        >
          <span class="grid size-11 shrink-0 place-items-center rounded-full bg-[#d8dadd] text-[#8d949a]">
            <Landmark :size="21" :stroke-width="2" aria-hidden="true" />
          </span>
          <span class="ml-3 min-w-0 flex-1">
            <strong class="block truncate text-sm font-bold text-[var(--color-text-primary)]">
              {{ account.bank }}
            </strong>
            <span class="mt-0.5 block text-xs text-[var(--color-text-secondary)]">
              {{ account.number }}
            </span>
            <span class="mt-1 block text-sm font-bold text-[var(--color-text-primary)]">
              {{ account.balance.toLocaleString('ko-KR') }}원
            </span>
          </span>
          <span
            class="ml-3 grid size-6 shrink-0 place-items-center rounded-full border"
            :class="
              selectedAccountIds.includes(account.id)
                ? 'border-[var(--color-brand-primary)] bg-[var(--color-brand-primary)] text-white'
                : 'border-[#cbd5db] bg-white text-transparent'
            "
            aria-hidden="true"
          >
            <Check :size="14" :stroke-width="3" />
          </span>
        </button>
      </div>
    </div>

    <button
      class="mt-auto min-h-[54px] w-full rounded-xl bg-[var(--color-brand-primary)] text-sm font-bold text-white shadow-[0_6px_16px_rgba(39,169,235,0.2)] transition-colors active:bg-[var(--color-brand-primary-pressed)] disabled:bg-[#cbd8df] disabled:shadow-none"
      type="button"
      :disabled="selectedAccounts.length === 0"
      @click="selectedAccounts.length && emit('connect', selectedAccounts)"
    >
      선택한 계좌 {{ selectedAccounts.length }}개 연결하기
    </button>
  </section>

  <section
    v-else
    class="flex min-h-[calc(100dvh-var(--app-header-height))] flex-col bg-white px-5 pt-3 pb-[calc(104px+env(safe-area-inset-bottom))]"
    aria-labelledby="empty-account-title"
  >
    <header>
      <h1
        id="empty-account-title"
        class="mt-2 text-[28px] leading-[1.3] font-extrabold tracking-[-0.035em] text-[var(--color-text-primary)]"
      >
        보호자에게 꼭 맞는<br />KB 계좌를 골라봤어요
      </h1>
      <p class="mt-3 text-sm leading-6 text-[var(--color-text-secondary)]">
        아이의 자산 관리에 연결할 부모 명의 계좌를 선택해보세요.
      </p>
    </header>

    <section class="mt-7" aria-labelledby="recommended-accounts-title">
      <h2 id="recommended-accounts-title" class="sr-only">부모 명의 추천 입출금 계좌</h2>
      <div
        v-if="isRecommendationsLoading"
        class="grid gap-3"
        aria-label="추천 계좌 불러오는 중"
        aria-busy="true"
      >
        <div
          v-for="index in 2"
          :key="index"
          class="h-[208px] animate-pulse rounded-[20px] border border-[#e2e9ed] bg-[#f5f8fa]"
          aria-hidden="true"
        ></div>
      </div>
      <div
        v-else-if="recommendedAccounts.length === 0"
        class="rounded-[20px] border border-[#d7e9f2] bg-[#f3faff] px-5 py-8 text-center"
      >
        <Landmark class="mx-auto text-[var(--color-brand-primary)]" :size="34" />
        <strong class="mt-4 block text-base">추천 가능한 계좌가 아직 없어요</strong>
        <p class="mt-2 text-[13px] text-[var(--color-text-secondary)]">
          잠시 후 다시 확인해주세요.
        </p>
      </div>
      <ul v-else class="grid list-none gap-3 p-0">
        <li v-for="account in recommendedAccounts" :key="account.id">
          <button
            class="recommended-account w-full rounded-[20px] border p-5 text-left"
            :class="selectedRecommendedAccountId === account.id ? 'recommended-account--selected border-[var(--color-brand-primary)] bg-[#e8f7ff]' : 'border-[var(--color-border)] bg-white'"
            type="button"
            :aria-pressed="selectedRecommendedAccountId === account.id"
            @click="selectedRecommendedAccountId = account.id"
          >
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0">
                <span
                  class="inline-flex h-7 items-center rounded-full px-3 text-xs font-bold"
                  :class="account.badgeClass"
                >
                  {{ account.badge }}
                </span>
                <h3 class="mt-3 text-[18px] leading-tight font-extrabold tracking-[-0.02em]">
                  {{ account.name }}
                </h3>
                <span class="mt-1 block text-xs text-[var(--color-text-secondary)]">
                  {{ account.bankName }}
                </span>
              </div>
              <span
                class="grid size-8 shrink-0 place-items-center rounded-full transition-colors"
                :class="selectedRecommendedAccountId === account.id ? 'bg-[var(--color-brand-primary)] text-white' : 'border border-[#d7e0e5] bg-white text-transparent'"
                aria-hidden="true"
              >
                <Check :size="16" :stroke-width="3" />
              </span>
            </div>
            <div class="mt-3 flex items-center gap-2 text-sm font-bold">
              <strong class="text-[var(--color-selected-text)]">{{ account.rate }}</strong>
              <span class="text-[#c7d0d5]">·</span>
              <span>{{ account.period }}</span>
            </div>
            <p class="mt-3 text-[13px] leading-5 text-[var(--color-text-secondary)]">
              {{ account.description }}
            </p>
            <div class="mt-4 flex flex-wrap gap-2" aria-label="계좌 특징">
              <span
                v-for="tag in account.tags"
                :key="tag"
                class="inline-flex min-h-7 items-center rounded-full border bg-white px-3 py-1 text-[11px] font-semibold text-[var(--color-text-secondary)]"
                :class="
                  selectedRecommendedAccountId === account.id
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
    </section>

    <Teleport to="body">
      <div class="fixed bottom-0 left-1/2 z-[var(--z-index-bottom-nav)] w-full max-w-[var(--app-max-width)] -translate-x-1/2 bg-white/95 px-5 pt-3 pb-[calc(16px+env(safe-area-inset-bottom))] backdrop-blur-sm">
        <button
          class="min-h-14 w-full rounded-xl bg-[var(--color-brand-primary)] text-base font-bold text-white shadow-[0_7px_18px_rgba(39,169,235,0.2)] transition-colors active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[#cbd8df] disabled:shadow-none"
          type="button"
          :disabled="selectedRecommendedAccountId === null || isRecommendationsLoading"
          @click="selectedRecommendedAccountId !== null && emit('createAccount', selectedRecommendedAccountId)"
        >
          {{ selectedRecommendedAccountId !== null ? '선택한 계좌 만들기' : '계좌를 선택해주세요' }}
        </button>
      </div>
    </Teleport>
  </section>
</template>

<style scoped>
.imported-account {
  transition: border-color 150ms ease, background-color 150ms ease, transform 150ms ease;
}

.imported-account:active { transform: scale(0.985); }
.imported-account:focus-visible { outline: 3px solid rgb(45 169 232 / 20%); outline-offset: 2px; }

.recommended-account {
  box-shadow: 0 6px 20px rgb(55 96 118 / 5%);
  transition: border-color 160ms ease, background-color 160ms ease, box-shadow 160ms ease,
    transform 160ms ease;
}

.recommended-account:active {
  border-color: rgb(39 169 235 / 35%);
  box-shadow: 0 3px 12px rgb(55 96 118 / 7%);
  transform: scale(0.985);
}

.recommended-account--selected { box-shadow: 0 7px 22px rgb(39 169 235 / 12%); }
.recommended-account:focus-visible { outline: 3px solid rgb(39 169 235 / 20%); outline-offset: 2px; }

@media (prefers-reduced-motion: reduce) {
  .imported-account,
  .recommended-account { transition-duration: 1ms; }
}
</style>
