<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { CheckCircle2, ChevronDown, Gift, Heart, ShieldCheck, Sparkles } from 'lucide-vue-next'

import { api, getApiErrorMessage } from '@/api'
import { hasParentDemandDepositAccount, resolveCurrentChildId } from '@/api/context'
import { useToast } from '@/composables/useToast'
import { addOpenedSavingsToGoalSetupDraft } from '@/utils/goalSetupDraft'
import { useRouter } from 'vue-router'

const props = defineProps<{
  productId: string
}>()
const router = useRouter()
const { showToast } = useToast()
const isLoadingProduct = ref(true)
const productName = ref('금융 상품')
const bankName = ref('')
const summary = ref('')
const maxRate = ref<number | undefined>()
const baseRate = ref<number | undefined>()
const periodMonths = ref<number | undefined>()
const monthlyMax = ref<number | undefined>()
const monthlyMin = ref<number | undefined>()
const periodMinMonths = ref<number | undefined>()
const renewalDescription = ref('')
const productSubtype = ref('')
const rateReference = ref('')
const interestPaymentLabel = ref('')
const joinTerminationMethod = ref('')
const productBadges = ref<string[]>([])
const curationReason = ref('')
const currentChildId = ref<number | null>(null)
const childName = ref('아이')

const isFavorite = ref(false)
const isFavoriteUpdating = ref(false)
const isBasicInfoOpen = ref(true)
const isRateInfoOpen = ref(true)
const isBenefitsOpen = ref(true)
const isMaturityOpen = ref(true)
const isNoticeOpen = ref(true)
const monthlySavingAmount = ref(300000)

const productTypeValue = ref('SAVING')
const isDemandDeposit = computed(() =>
  ['ACCOUNT', 'DEMAND_DEPOSIT', 'DEPOSIT'].includes(productTypeValue.value.toUpperCase()),
)
const productType = computed(() => isDemandDeposit.value ? '입출금계좌' : '적금')
const displayBadges = computed(() =>
  Array.from(new Set([...productBadges.value, productType.value])),
)

type DetailItem = { label?: string; content?: string; description?: string; rate?: number | string }
const eligibilityConditions = ref<DetailItem[]>([])
const preferentialConditions = ref<DetailItem[]>([])
const additionalBenefits = ref<DetailItem[]>([])
const cautionItems = ref<DetailItem[]>([])

const toDetailItems = (value: object | undefined): DetailItem[] =>
  Array.isArray(value) ? value.filter((item): item is DetailItem => Boolean(item && typeof item === 'object')) : []

const formatPeriod = () => {
  if (isDemandDeposit.value && !periodMinMonths.value && !periodMonths.value) return '제한 없음'
  if (periodMinMonths.value && periodMonths.value && periodMinMonths.value !== periodMonths.value) {
    return `${periodMinMonths.value}~${periodMonths.value}개월`
  }
  return periodMonths.value ? `${periodMonths.value}개월` : '상품별 안내 확인'
}

const formatDepositRange = () => {
  if (monthlyMin.value != null && monthlyMax.value != null) {
    return `월 ${formatWon(monthlyMin.value)}~${formatWon(monthlyMax.value)}`
  }
  if (monthlyMax.value != null) return `월 최대 ${formatWon(monthlyMax.value)}`
  if (monthlyMin.value != null) return `월 ${formatWon(monthlyMin.value)}부터`
  return isDemandDeposit.value ? '자유롭게 입출금' : '상품별 안내 확인'
}

const basicInformation = computed(() => [
  ...eligibilityConditions.value.map((item) => ({
    label: item.label || '가입 대상',
    value: item.content || item.description || '상품별 안내 확인',
  })),
  { label: '상품 유형', value: productSubtype.value || productType.value },
  { label: '계약 기간', value: [formatPeriod(), renewalDescription.value].filter(Boolean).join(' · ') },
  { label: isDemandDeposit.value ? '입출금 조건' : '저축 금액', value: formatDepositRange() },
  ...(interestPaymentLabel.value
    ? [{ label: '이자 지급', value: interestPaymentLabel.value }]
    : []),
  ...(joinTerminationMethod.value
    ? [{ label: '가입·해지 방법', value: joinTerminationMethod.value }]
    : []),
])

const preferentialRates = computed(() => preferentialConditions.value.map((item) => ({
  title: item.label || '우대 조건',
  description: item.content || item.description || '상품별 세부 조건을 확인해 주세요.',
  rate: item.rate == null ? '' : `+${Number(item.rate).toFixed(2)}%p`,
})))

const benefitIcons = [ShieldCheck, CheckCircle2, Sparkles, Gift]
const benefits = computed(() => additionalBenefits.value.map((item, index) => ({
  icon: benefitIcons[index % benefitIcons.length],
  title: item.label || '추가 혜택',
  description: item.content || item.description || '상품별 상세 혜택을 확인해 주세요.',
})))

const principalAmount = ref(0)
const expectedInterest = ref(0)
const expectedMaturityAmount = ref(0)

const formatWon = (amount: number) => `${amount.toLocaleString('ko-KR')}원`
const updateMonthlyAmount = (event: Event) => {
  const input = event.target as HTMLInputElement
  monthlySavingAmount.value = Number(input.value.replace(/\D/g, '')) || 0
}

const toggleFavorite = async () => {
  if (isFavoriteUpdating.value) return
  const financialProductId = Number(props.productId)
  if (!Number.isInteger(financialProductId) || financialProductId < 1) {
    showToast('상품 정보를 확인할 수 없습니다.', 'error')
    return
  }

  isFavoriteUpdating.value = true
  const previousFavoriteState = isFavorite.value
  const nextFavoriteState = !previousFavoriteState
  isFavorite.value = nextFavoriteState
  try {
    const childId = currentChildId.value ?? (await resolveCurrentChildId())
    currentChildId.value = childId
    const response = await api.updateBookmarkUsingPUT(childId, financialProductId, {
      is_bookmarked: nextFavoriteState,
    })
    if (typeof response.data?.is_bookmarked === 'boolean') {
      isFavorite.value = response.data.is_bookmarked
    }
    showToast(isFavorite.value ? '관심상품에 저장했어요.' : '관심상품에서 해제했어요.', 'success')
  } catch (error) {
    isFavorite.value = previousFavoriteState
    showToast(getApiErrorMessage(error, '관심상품을 변경하지 못했습니다.'), 'error')
  } finally {
    isFavoriteUpdating.value = false
  }
}

const estimateMaturity = async () => {
  if (isDemandDeposit.value || !monthlySavingAmount.value) return
  try {
    const { data } = await api.estimateMaturityUsingPOST(Number(props.productId), {
      monthly_amount: monthlySavingAmount.value,
      period_months: periodMonths.value ?? 12,
    })
    principalAmount.value = data.principal_amount ?? 0
    expectedInterest.value = data.estimated_interest_after_tax ?? data.estimated_interest_before_tax ?? 0
    expectedMaturityAmount.value = data.estimated_maturity_amount ?? 0
  } catch {
    principalAmount.value = monthlySavingAmount.value * (periodMonths.value ?? 12)
  }
}

const openProduct = async () => {
  try {
    if (!await hasParentDemandDepositAccount()) {
      showToast('자녀 상품 가입 전에 부모 입출금계좌를 먼저 등록해 주세요.', 'error')
      await router.push({ name: 'Accounts', query: { next: router.currentRoute.value.fullPath } })
      return
    }
    const childId = await resolveCurrentChildId()
    const { data: openedAccount } = await api.openUsingPOST(undefined, {
      child_id: childId,
      financial_product_id: Number(props.productId),
      initial_deposit_amount: 0,
      owner_type: 'CHILD',
    })
    addOpenedSavingsToGoalSetupDraft(openedAccount.account_id)
    await router.push({ name: 'SavingsOpenComplete', query: { product: productName.value } })
  } catch (error) {
    showToast(getApiErrorMessage(error, '상품에 가입하지 못했습니다.'), 'error')
  }
}

let estimateTimer: number | undefined
watch(monthlySavingAmount, () => {
  window.clearTimeout(estimateTimer)
  estimateTimer = window.setTimeout(estimateMaturity, 300)
})

onMounted(async () => {
  try {
    const childId = await resolveCurrentChildId()
    currentChildId.value = childId
    const [{ data }, { data: child }] = await Promise.all([
      api.getProductDetailUsingGET(Number(props.productId), undefined, childId),
      api.getChildUsingGET(childId),
    ])
    childName.value = child.name?.trim() || '아이'
    productName.value = data.name ?? '금융 상품'
    bankName.value = data.bank_name ?? ''
    summary.value = data.summary ?? data.curation_reason ?? ''
    productTypeValue.value = data.product_type ?? 'SAVING'
    productSubtype.value = data.product_subtype ?? ''
    productBadges.value = (data.badges ?? []).flatMap(({ label }) => label ? [label] : [])
    curationReason.value = data.curation_reason ?? ''
    maxRate.value = data.interest_rate?.max_rate
    baseRate.value = data.interest_rate?.base_rate
    rateReference.value = data.interest_rate?.reference ?? ''
    periodMinMonths.value = data.contract_period?.min_months
    periodMonths.value = data.contract_period?.max_months
    renewalDescription.value = data.contract_period?.renewal_description ?? ''
    monthlyMin.value = data.monthly_deposit?.min_amount
    monthlyMax.value = data.monthly_deposit?.max_amount
    interestPaymentLabel.value = data.interest_payment_method?.label ?? ''
    joinTerminationMethod.value = data.join_termination_method ?? ''
    eligibilityConditions.value = toDetailItems(data.eligibility_conditions)
    preferentialConditions.value = toDetailItems(data.preferential_conditions)
    additionalBenefits.value = toDetailItems(data.additional_benefits)
    cautionItems.value = toDetailItems(data.cautions)
    isFavorite.value = data.is_bookmarked ?? false
    if (!isDemandDeposit.value) await estimateMaturity()
  } catch (error) {
    showToast(getApiErrorMessage(error, '상품 정보를 불러오지 못했습니다.'), 'error')
  } finally {
    isLoadingProduct.value = false
  }
})
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-[var(--color-app-background)] px-[18px] pt-4 pb-6 text-[var(--color-text-primary)]"
  >
    <section v-if="isLoadingProduct" aria-label="상품 상세 정보를 불러오는 중" aria-busy="true">
      <div
        class="animate-pulse rounded-[18px] bg-[var(--color-brand-secondary)] p-4"
        aria-hidden="true"
      >
        <div class="flex gap-1.5">
          <span class="h-6 w-16 rounded-full bg-white/70"></span>
          <span class="h-6 w-24 rounded-full bg-white/70"></span>
          <span class="h-6 w-12 rounded-full bg-white/70"></span>
        </div>
        <span class="mt-5 block h-7 w-[58%] rounded-lg bg-white/75"></span>
        <span class="mt-3 block h-4 w-24 rounded-md bg-white/60"></span>
        <span class="mt-4 block h-3 w-full rounded-full bg-white/55"></span>
        <span class="mt-2 block h-3 w-[76%] rounded-full bg-white/55"></span>

        <div class="mt-5 grid grid-cols-2 gap-2">
          <div v-for="index in 4" :key="index" class="h-[72px] rounded-[12px] bg-white/80 p-3">
            <span class="block h-3 w-14 rounded bg-[#e9eef0]"></span>
            <span class="mt-3 block h-5 w-20 rounded-md bg-[#dfe7ea]"></span>
          </div>
        </div>
        <span class="mt-3 block h-11 rounded-[11px] bg-[#b9dded]"></span>
        <span class="mt-2 block h-10 rounded-[11px] bg-white/80"></span>
      </div>

      <div
        v-for="index in 4"
        :key="`detail-skeleton-${index}`"
        class="mt-3 flex h-14 animate-pulse items-center justify-between rounded-[18px] border border-[#e2e9ed] bg-white px-4"
        aria-hidden="true"
      >
        <span class="h-4 w-28 rounded-md bg-[#e7edf0]"></span>
        <span class="size-5 rounded-full bg-[#edf1f3]"></span>
      </div>
    </section>

    <template v-else>
    <section class="rounded-[18px] bg-[var(--color-brand-secondary)] p-4">
      <div class="flex flex-wrap gap-1.5">
        <span
          v-for="(badge, index) in displayBadges"
          :key="badge"
          class="rounded-full px-3 py-1 text-[10px] font-semibold"
          :class="
            badge === productType
              ? productType === '입출금계좌'
                ? 'bg-[#edae32] text-white shadow-[0_3px_8px_rgb(190_129_15_/_18%)]'
                : 'bg-[var(--color-selected-background)] text-[var(--color-selected-text)]'
              : index === 0
                ? 'bg-[var(--color-brand-primary)] text-[var(--color-text-inverse)]'
                : 'bg-[var(--color-surface)] text-[var(--color-text-secondary)]'
          "
          >{{ badge }}</span
        >
      </div>

      <h1 class="mt-4 mb-0 text-[22px] font-bold tracking-[-0.025em]">{{ productName }}</h1>
      <p class="mt-1.5 mb-0 text-[12px] text-[var(--color-text-secondary)]">{{ bankName }}</p>
      <p class="mt-3 mb-0 text-[12px] leading-[1.65] text-[var(--color-text-secondary)]">
        {{ summary }}
      </p>

      <dl class="mt-4 grid grid-cols-2 gap-2">
        <div
          class="rounded-[12px] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-3"
        >
          <dt class="text-[10px] text-[var(--color-text-secondary)]">최고 금리</dt>
          <dd class="mt-1 mb-0 text-[15px] font-semibold text-[var(--color-selected-text)]">
            {{ maxRate === undefined ? '상품별 안내' : `연 ${maxRate.toFixed(2)}%` }}
          </dd>
        </div>
        <div
          class="rounded-[12px] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-3"
        >
          <dt class="text-[10px] text-[var(--color-text-secondary)]">기본 금리</dt>
          <dd class="mt-1 mb-0 text-[15px] font-semibold">{{ baseRate === undefined ? '상품별 안내' : `연 ${baseRate.toFixed(2)}%` }}</dd>
        </div>
        <div
          class="rounded-[12px] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-3"
        >
          <dt class="text-[10px] text-[var(--color-text-secondary)]">계약 기간</dt>
          <dd class="mt-1 mb-0 text-[15px] font-semibold">{{ formatPeriod() }}</dd>
        </div>
        <div
          class="rounded-[12px] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-3"
        >
          <dt class="text-[10px] text-[var(--color-text-secondary)]">
            {{ isDemandDeposit ? '입출금 조건' : '월 저축 한도' }}
          </dt>
          <dd class="mt-1 mb-0 text-[15px] font-semibold">{{ formatDepositRange() }}</dd>
        </div>
      </dl>

      <button
        class="mt-3 h-11 w-full rounded-[11px] bg-[var(--color-brand-primary)] text-[14px] font-bold text-[var(--color-text-inverse)] active:bg-[var(--color-brand-primary-pressed)]"
        type="button"
        @click="openProduct"
      >
        가입하기
      </button>
      <button
        class="mt-2 flex h-10 w-full items-center justify-center gap-1.5 rounded-[11px] border text-[12px] font-bold transition duration-200 active:scale-[0.99]"
        :class="
          isFavorite
            ? 'border-[#ffc8cf] bg-[#fff4f5] text-[#e94355]'
            : 'border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text-secondary)] active:bg-[var(--color-surface-muted)]'
        "
        type="button"
        :aria-pressed="isFavorite"
        :aria-busy="isFavoriteUpdating"
        :disabled="isFavoriteUpdating"
        @click="toggleFavorite"
      >
        <Heart
          :size="16"
          class="transition duration-200"
          :class="isFavorite ? 'scale-110 fill-[#ff4d5f] text-[#ff4d5f]' : ''"
        />
        {{ isFavorite ? '관심상품에 저장됨' : '관심상품 저장' }}
      </button>
    </section>

    <section class="mt-3 rounded-[18px] bg-[var(--color-selected-background)] p-4">
      <div class="flex items-center gap-2">
        <span
          class="grid size-8 place-items-center rounded-full bg-[var(--color-surface)] text-[var(--color-selected-text)]"
          aria-hidden="true"
        >
          <Sparkles :size="17" />
        </span>
        <h2 class="m-0 text-[14px] font-bold">{{ childName }}에게 추천하는 이유</h2>
      </div>
      <p class="mt-3 mb-0 text-[11px] leading-[1.65] text-[var(--color-text-secondary)]">
        {{ curationReason || `${childName}의 자산관리 목적에 맞춰 살펴볼 수 있는 상품이에요.` }}
      </p>
    </section>

    <section class="mt-3 overflow-hidden rounded-[18px] border border-[#e2e9ed] bg-white shadow-sm">
      <button
        class="flex h-14 w-full items-center justify-between px-4 text-left"
        type="button"
        :aria-expanded="isBasicInfoOpen"
        @click="isBasicInfoOpen = !isBasicInfoOpen"
      >
        <h2 class="m-0 text-[16px] font-bold">상품 기본정보</h2>
        <ChevronDown
          :size="20"
          class="transition-transform"
          :class="isBasicInfoOpen ? 'rotate-180' : ''"
        />
      </button>
      <Transition
        enter-active-class="transition duration-200 ease-out"
        enter-from-class="-translate-y-1 opacity-0"
        leave-active-class="transition duration-150 ease-in"
        leave-to-class="-translate-y-1 opacity-0"
      >
        <dl v-if="isBasicInfoOpen" class="m-0 border-t border-[#e5ebef] px-4">
          <div
            v-for="information in basicInformation"
            :key="information.label"
            class="border-b border-[#e9eef1] py-3 last:border-0"
          >
            <dt class="text-[10px] font-bold text-[var(--color-text-secondary)]">
              {{ information.label }}
            </dt>
            <dd class="mt-1.5 mb-0 whitespace-pre-line text-[11px] leading-[1.55]">
              {{ information.value }}
            </dd>
          </div>
        </dl>
      </Transition>
    </section>

    <section class="mt-3 overflow-hidden rounded-[18px] border border-[#e2e9ed] bg-white shadow-sm">
      <button
        class="flex h-14 w-full items-center justify-between px-4 text-left"
        type="button"
        :aria-expanded="isRateInfoOpen"
        @click="isRateInfoOpen = !isRateInfoOpen"
      >
        <h2 class="m-0 text-[16px] font-bold">금리와 우대조건</h2>
        <ChevronDown
          :size="20"
          class="transition-transform"
          :class="isRateInfoOpen ? 'rotate-180' : ''"
        />
      </button>
      <Transition
        enter-active-class="transition duration-200 ease-out"
        enter-from-class="-translate-y-1 opacity-0"
        leave-active-class="transition duration-150 ease-in"
        leave-to-class="-translate-y-1 opacity-0"
      >
        <div v-if="isRateInfoOpen" class="border-t border-[#e5ebef] px-4 pb-4">
          <div class="mt-4 rounded-[12px] bg-[#e8f8ff] px-3 py-4">
            <span class="text-[10px] text-[var(--color-text-secondary)]">
              {{ rateReference || '상품 공시 기준' }}
            </span>
            <strong class="mt-1 block text-[22px] text-[#2babe8]">
              {{ maxRate === undefined ? '금리 상세 안내 확인' : `최고 연 ${maxRate.toFixed(2)}%` }}
            </strong>
          </div>
          <ul class="mt-3 mb-0 grid list-none gap-2 p-0">
            <li
              v-for="rate in preferentialRates"
              :key="rate.title"
              class="flex items-start justify-between gap-3 rounded-[11px] border border-[#e5ebef] px-3 py-3"
            >
              <div class="min-w-0">
                <strong class="block text-[11px]">{{ rate.title }}</strong>
                <span class="mt-1 block text-[9px] leading-[1.4] text-[#96a1ad]">{{
                  rate.description
                }}</span>
              </div>
              <strong class="shrink-0 text-[10px] text-[#2babe8]">{{ rate.rate }}</strong>
            </li>
          </ul>
        </div>
      </Transition>
    </section>

    <section
      v-if="benefits.length"
      class="mt-3 overflow-hidden rounded-[18px] border border-[#e2e9ed] bg-white shadow-sm"
    >
      <button
        class="flex h-14 w-full items-center justify-between px-4 text-left"
        type="button"
        :aria-expanded="isBenefitsOpen"
        @click="isBenefitsOpen = !isBenefitsOpen"
      >
        <h2 class="m-0 text-[16px] font-bold">부가혜택</h2>
        <ChevronDown
          :size="20"
          class="transition-transform"
          :class="isBenefitsOpen ? 'rotate-180' : ''"
        />
      </button>
      <Transition
        enter-active-class="transition duration-200 ease-out"
        enter-from-class="-translate-y-1 opacity-0"
        leave-active-class="transition duration-150 ease-in"
        leave-to-class="-translate-y-1 opacity-0"
      >
        <ul
          v-if="isBenefitsOpen"
          class="mt-0 mb-0 grid list-none gap-4 border-t border-[#e5ebef] px-4 py-4"
        >
          <li v-for="benefit in benefits" :key="benefit.title" class="flex gap-3">
            <span
              class="grid size-8 shrink-0 place-items-center rounded-[10px] bg-[#f4f7f9] text-[#7894a4]"
            >
              <component :is="benefit.icon" :size="16" />
            </span>
            <div>
              <strong class="block text-[11px]">{{ benefit.title }}</strong>
              <p class="mt-1 mb-0 text-[9px] leading-[1.45] text-[#96a1ad]">
                {{ benefit.description }}
              </p>
            </div>
          </li>
        </ul>
      </Transition>
    </section>

    <section
      v-if="!isDemandDeposit"
      class="mt-3 overflow-hidden rounded-[18px] border border-[#e2e9ed] bg-white shadow-sm"
    >
      <button
        class="flex h-14 w-full items-center justify-between px-4 text-left"
        type="button"
        :aria-expanded="isMaturityOpen"
        @click="isMaturityOpen = !isMaturityOpen"
      >
        <h2 class="m-0 text-[16px] font-bold">예상 만기금액</h2>
        <ChevronDown
          :size="20"
          class="transition-transform"
          :class="isMaturityOpen ? 'rotate-180' : ''"
        />
      </button>
      <Transition
        enter-active-class="transition duration-200 ease-out"
        enter-from-class="-translate-y-1 opacity-0"
        leave-active-class="transition duration-150 ease-in"
        leave-to-class="-translate-y-1 opacity-0"
      >
        <div v-if="isMaturityOpen" class="border-t border-[#e5ebef] px-4 pb-4">
          <label for="monthly-saving-amount" class="mt-4 block text-[11px] font-bold"
            >월 저축금액</label
          >
          <input
            id="monthly-saving-amount"
            :value="monthlySavingAmount.toLocaleString('ko-KR')"
            class="mt-2 h-11 w-full rounded-[11px] border border-[#dfe7ec] px-3 text-[13px] outline-none focus:border-[#2babe8]"
            type="text"
            inputmode="numeric"
            @input="updateMonthlyAmount"
          />
          <label class="mt-4 block text-[11px] font-bold">적용 예상금리</label>
          <div
            class="mt-2 flex h-11 items-center rounded-[11px] border border-[#dfe7ec] px-3 text-[13px]"
          >
            {{ maxRate === undefined ? '상품별 금리 안내 확인' : `최고금리 연 ${maxRate.toFixed(2)}%` }}
          </div>
          <dl class="mt-4 rounded-[13px] bg-[#fff8dd] px-4 py-4 text-[11px]">
            <div class="flex justify-between gap-3">
              <dt>총 납입원금</dt>
              <dd class="m-0 font-bold">{{ formatWon(principalAmount) }}</dd>
            </div>
            <div class="mt-3 flex justify-between gap-3">
              <dt>예상 세전이자</dt>
              <dd class="m-0 font-bold">{{ formatWon(expectedInterest) }}</dd>
            </div>
            <div class="mt-3 flex justify-between gap-3">
              <dt>예상 세후 만기금액</dt>
              <dd class="m-0 font-bold">{{ formatWon(expectedMaturityAmount) }}</dd>
            </div>
          </dl>
          <p class="mt-3 mb-0 text-[9px] leading-[1.5] text-[#9ba5b0]">
            월초 납입을 가정한 단순 계산 예시입니다. 실제 이자는 납입일, 일수, 세율, 우대조건 등에
            따라 달라집니다.
          </p>
        </div>
      </Transition>
    </section>

    <section class="mt-3 overflow-hidden rounded-[18px] border border-[#e2e9ed] bg-white shadow-sm">
      <button
        class="flex h-14 w-full items-center justify-between px-4 text-left"
        type="button"
        :aria-expanded="isNoticeOpen"
        @click="isNoticeOpen = !isNoticeOpen"
      >
        <h2 class="m-0 text-[16px] font-bold">가입 전 확인</h2>
        <ChevronDown
          :size="20"
          class="transition-transform"
          :class="isNoticeOpen ? 'rotate-180' : ''"
        />
      </button>
      <Transition
        enter-active-class="transition duration-200 ease-out"
        enter-from-class="-translate-y-1 opacity-0"
        leave-active-class="transition duration-150 ease-in"
        leave-to-class="-translate-y-1 opacity-0"
      >
        <div v-if="isNoticeOpen" class="border-t border-[#e5ebef] px-4 pb-4">
          <div class="mt-4 grid gap-2">
            <p
              v-for="(caution, index) in cautionItems"
              :key="`${caution.label}-${index}`"
              class="m-0 rounded-[12px] bg-[#f7f8fa] px-4 py-4 text-[10px] leading-[1.65] text-[#798693]"
            >
              <strong v-if="caution.label" class="mb-1 block text-[11px] text-[var(--color-text-primary)]">
                {{ caution.label }}
              </strong>
              {{ caution.content || caution.description || '상품 가입 전 상세 조건을 확인해 주세요.' }}
            </p>
            <p
              v-if="!cautionItems.length"
              class="m-0 rounded-[12px] bg-[#f7f8fa] px-4 py-4 text-[10px] leading-[1.65] text-[#798693]"
            >
              상품 가입 전 최신 상품설명서와 약관을 확인해 주세요.
            </p>
          </div>
          <p class="mt-3 mb-0 text-[9px] leading-[1.5] text-[#9ba5b0]">
            상품정보 기준: KB국민은행 공식 상품공시. 실제 가입 전 최신 상품설명서와 약관을
            확인해주세요.
          </p>
        </div>
      </Transition>
    </section>
    </template>
  </main>
</template>
