<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { CheckCircle2, ChevronDown, Gift, Heart, ShieldCheck, Sparkles } from 'lucide-vue-next'

import { api, getApiErrorMessage } from '@/api'
import { hasParentDemandDepositAccount, resolveCurrentChildId } from '@/api/context'
import { useToast } from '@/composables/useToast'
import { useRouter } from 'vue-router'

const props = defineProps<{
  productId: string
}>()
const router = useRouter()
const { showToast } = useToast()
const productName = ref('금융 상품')
const bankName = ref('')
const summary = ref('')
const maxRate = ref<number | undefined>()
const baseRate = ref<number | undefined>()
const periodMonths = ref(12)
const monthlyMax = ref<number | undefined>()

const isFavorite = ref(false)
const isBasicInfoOpen = ref(true)
const isRateInfoOpen = ref(true)
const isBenefitsOpen = ref(true)
const isMaturityOpen = ref(true)
const isNoticeOpen = ref(true)
const monthlySavingAmount = ref(300000)

const productTypeValue = ref('SAVINGS')
const productType = computed(() => productTypeValue.value === 'DEMAND_DEPOSIT' ? '입출금계좌' : '적금')

const basicInformation = [
  { label: '가입 대상', value: '만 19세 미만 실명의 개인, 1인 1계좌' },
  { label: '상품 유형', value: '자유적립식 예금' },
  { label: '계약 기간', value: '1년, 신청 시 1년 단위 자동 재예치' },
  { label: '저축 금액', value: '신규 가입 시 1만원~300만원\n2회차부터 월 1천원~300만원' },
  { label: '이자 지급', value: '만기일시지급식' },
  {
    label: '가입·해지 방법',
    value: '영업점 또는 KB스타뱅킹\n법정대리인이 비대면으로 신규한 경우 해지는 영업점 방문 필요',
  },
]

const preferentialRates = [
  {
    title: '가족사랑 우대',
    description: 'KB국민은행 가족고객 등록 가족이 3인 이상',
    rate: '+0.20%p',
  },
  { title: '자동이체 우대', description: 'KB 계좌 간 자동이체 입금 8회 이상', rate: '+0.10%p' },
  {
    title: '아동수당 우대',
    description: 'KB Young Youth 통장으로 아동수당 3회 이상 수령',
    rate: '+0.10%p',
  },
  {
    title: '주택청약종합저축 우대',
    description: '신규 가입 및 만기일 기준 보유 조건',
    rate: '최대 +0.40%p',
  },
  {
    title: '성장축하·재등록률 우대',
    description: '만 0·7·13·16·19세 가입 또는 사진자료등록 조건',
    rate: '최대 +0.50%p',
  },
]

const benefits = [
  {
    icon: ShieldCheck,
    title: '무료 보험가입 서비스',
    description:
      '가입 동의 고객에게 어린이·청소년 단체보험을 제공하며, 연령별 보장 항목이 달라집니다.',
  },
  {
    icon: CheckCircle2,
    title: '1년 단위 자동 재예치',
    description: '신청한 계약기간까지 계속 구조로 활용할 수 있습니다.',
  },
  {
    icon: Sparkles,
    title: '재예치 계약 일부 인출',
    description: '원금 범위 내에서 재예치 기간 중 일부 인출이 가능합니다.',
  },
  {
    icon: Gift,
    title: '예금자보호',
    description: '동일 은행 보호상품과 합산해 1인당 최고 보호한도까지 보호됩니다.',
  },
]

const principalAmount = ref(0)
const expectedInterest = ref(0)
const expectedMaturityAmount = ref(0)

const formatWon = (amount: number) => `${amount.toLocaleString('ko-KR')}원`
const updateMonthlyAmount = (event: Event) => {
  const input = event.target as HTMLInputElement
  monthlySavingAmount.value = Number(input.value.replace(/\D/g, '')) || 0
}

const toggleFavorite = async () => {
  try {
    const next = !isFavorite.value
    const childId = await resolveCurrentChildId()
    await api.updateBookmarkUsingPUT(childId, Number(props.productId), { is_bookmarked: next })
    isFavorite.value = next
  } catch (error) {
    showToast(getApiErrorMessage(error, '관심상품을 변경하지 못했습니다.'), 'error')
  }
}

const estimateMaturity = async () => {
  if (!monthlySavingAmount.value) return
  try {
    const { data } = await api.estimateMaturityUsingPOST(Number(props.productId), {
      monthly_amount: monthlySavingAmount.value,
      period_months: periodMonths.value,
    })
    principalAmount.value = data.principal_amount ?? 0
    expectedInterest.value = data.estimated_interest_after_tax ?? data.estimated_interest_before_tax ?? 0
    expectedMaturityAmount.value = data.estimated_maturity_amount ?? 0
  } catch {
    principalAmount.value = monthlySavingAmount.value * periodMonths.value
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
    await api.openUsingPOST(undefined, {
      child_id: childId,
      financial_product_id: Number(props.productId),
      initial_deposit_amount: 0,
      owner_type: 'CHILD',
    })
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
    const { data } = await api.getProductDetailUsingGET(Number(props.productId), undefined, childId)
    productName.value = data.name ?? '금융 상품'
    bankName.value = data.bank_name ?? ''
    summary.value = data.summary ?? data.curation_reason ?? ''
    productTypeValue.value = data.product_type ?? 'SAVINGS'
    maxRate.value = data.interest_rate?.max_rate
    baseRate.value = data.interest_rate?.base_rate
    periodMonths.value = data.contract_period?.max_months ?? 12
    monthlyMax.value = data.monthly_deposit?.max_amount
    isFavorite.value = data.is_bookmarked ?? false
    await estimateMaturity()
  } catch (error) {
    showToast(getApiErrorMessage(error, '상품 정보를 불러오지 못했습니다.'), 'error')
  }
})
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-[var(--color-app-background)] px-[18px] pt-4 pb-6 text-[var(--color-text-primary)]"
  >
    <section class="rounded-[18px] bg-[var(--color-brand-secondary)] p-4">
      <div class="flex flex-wrap gap-1.5">
        <span
          class="rounded-full bg-[var(--color-brand-primary)] px-3 py-1 text-[10px] font-semibold text-[var(--color-text-inverse)]"
          >특별 추천</span
        >
        <span
          class="rounded-full bg-[var(--color-surface)] px-3 py-1 text-[10px] font-semibold text-[var(--color-text-secondary)]"
          >어린이·청소년 전용</span
        >
        <span
          class="rounded-full px-3 py-1 text-[10px] font-semibold"
          :class="
            productType === '입출금계좌'
              ? 'bg-[#fff4cf] text-[#a67d18]'
              : 'bg-[var(--color-selected-background)] text-[var(--color-selected-text)]'
          "
          >{{ productType }}</span
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
            {{ maxRate === undefined ? '-' : `연 ${maxRate}%` }}
          </dd>
        </div>
        <div
          class="rounded-[12px] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-3"
        >
          <dt class="text-[10px] text-[var(--color-text-secondary)]">기본 금리</dt>
          <dd class="mt-1 mb-0 text-[15px] font-semibold">{{ baseRate === undefined ? '-' : `연 ${baseRate}%` }}</dd>
        </div>
        <div
          class="rounded-[12px] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-3"
        >
          <dt class="text-[10px] text-[var(--color-text-secondary)]">계약 기간</dt>
          <dd class="mt-1 mb-0 text-[15px] font-semibold">{{ periodMonths }}개월</dd>
        </div>
        <div
          class="rounded-[12px] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-3"
        >
          <dt class="text-[10px] text-[var(--color-text-secondary)]">월 저축 한도</dt>
          <dd class="mt-1 mb-0 text-[15px] font-semibold">{{ monthlyMax === undefined ? '-' : `최대 ${formatWon(monthlyMax)}` }}</dd>
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
        class="mt-2 flex h-10 w-full items-center justify-center gap-1 rounded-[11px] border border-[var(--color-border)] bg-[var(--color-surface)] text-[12px] font-bold text-[var(--color-text-secondary)] active:bg-[var(--color-surface-muted)]"
        type="button"
        :aria-pressed="isFavorite"
        @click="toggleFavorite"
      >
        <Heart :size="15" :class="isFavorite ? 'fill-[#ff001b] text-[#ff001b]' : ''" />
        {{ isFavorite ? '관심상품 저장됨' : '관심상품 저장' }}
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
        <h2 class="m-0 text-[14px] font-bold">깨비에게 추천하는 이유</h2>
      </div>
      <p class="mt-3 mb-0 text-[11px] leading-[1.65] text-[var(--color-text-secondary)]">
        깨비는 현재 만 0세이므로 가입 시 성장축하 우대금리 대상이 될 수 있어요. 아동수당 수령, 가족
        등록, 자동이체 조건도 함께 확인해보세요.
      </p>
      <div class="mt-4 flex items-end justify-between border-t border-[var(--color-border)] pt-3">
        <span class="text-[11px] font-bold text-[var(--color-selected-text)]">목표 적합도</span>
        <strong class="text-[22px] text-[var(--color-selected-text)]">92%</strong>
      </div>
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
            <span class="text-[10px] text-[var(--color-text-secondary)]"
              >12개월 기준 · 세금공제 전</span
            >
            <strong class="mt-1 block text-[22px] text-[#2babe8]">최고 연 3.40%</strong>
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

    <section class="mt-3 overflow-hidden rounded-[18px] border border-[#e2e9ed] bg-white shadow-sm">
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

    <section class="mt-3 overflow-hidden rounded-[18px] border border-[#e2e9ed] bg-white shadow-sm">
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
            :value="monthlySavingAmount"
            class="mt-2 h-11 w-full rounded-[11px] border border-[#dfe7ec] px-3 text-[13px] outline-none focus:border-[#2babe8]"
            type="text"
            inputmode="numeric"
            @input="updateMonthlyAmount"
          />
          <label class="mt-4 block text-[11px] font-bold">적용 예상금리</label>
          <div
            class="mt-2 flex h-11 items-center rounded-[11px] border border-[#dfe7ec] px-3 text-[13px]"
          >
            최고금리 연 3.40%
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
          <p
            class="mt-4 mb-0 rounded-[12px] bg-[#f7f8fa] px-4 py-4 text-[10px] leading-[1.65] text-[#798693]"
          >
            최고금리는 모든 우대조건을 충족하고 만기 해지할 때 적용됩니다. 중도해지 시 더 낮은
            이율이 적용될 수 있으며, 납입 혜택은 가입 동의와 재예치 조건 등에 따라 달라집니다.
          </p>
          <p class="mt-3 mb-0 text-[9px] leading-[1.5] text-[#9ba5b0]">
            상품정보 기준: KB국민은행 공식 상품공시. 실제 가입 전 최신 상품설명서와 약관을
            확인해주세요.
          </p>
        </div>
      </Transition>
    </section>
  </main>
</template>
