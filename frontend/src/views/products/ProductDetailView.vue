<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  CheckCircle2,
  ChevronDown,
  Gift,
  Heart,
  ShieldCheck,
  Sparkles,
} from 'lucide-vue-next'

const isFavorite = ref(false)
const isBasicInfoOpen = ref(true)
const isRateInfoOpen = ref(false)
const isBenefitsOpen = ref(false)
const isMaturityOpen = ref(false)
const isNoticeOpen = ref(false)
const monthlySavingAmount = ref(300000)

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
  { title: '가족사랑 우대', description: 'KB국민은행 가족고객 등록 가족이 3인 이상', rate: '+0.20%p' },
  { title: '자동이체 우대', description: 'KB 계좌 간 자동이체 입금 8회 이상', rate: '+0.10%p' },
  { title: '아동수당 우대', description: 'KB Young Youth 통장으로 아동수당 3회 이상 수령', rate: '+0.10%p' },
  { title: '주택청약종합저축 우대', description: '신규 가입 및 만기일 기준 보유 조건', rate: '최대 +0.40%p' },
  { title: '성장축하·재등록률 우대', description: '만 0·7·13·16·19세 가입 또는 사진자료등록 조건', rate: '최대 +0.50%p' },
]

const benefits = [
  {
    icon: ShieldCheck,
    title: '무료 보험가입 서비스',
    description: '가입 동의 고객에게 어린이·청소년 단체보험을 제공하며, 연령별 보장 항목이 달라집니다.',
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

const principalAmount = computed(() => monthlySavingAmount.value * 12)
const expectedInterest = computed(() => Math.round(principalAmount.value * 0.034 * (6.5 / 12)))
const expectedMaturityAmount = computed(() => principalAmount.value + expectedInterest.value)

const formatWon = (amount: number) => `${amount.toLocaleString('ko-KR')}원`
const updateMonthlyAmount = (event: Event) => {
  const input = event.target as HTMLInputElement
  monthlySavingAmount.value = Number(input.value.replace(/\D/g, '')) || 0
}

</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-[var(--color-app-background)] px-3 pt-4 pb-6 text-[var(--color-text-primary)]">
    <section class="rounded-[18px] bg-[var(--color-brand-secondary)] p-4">
      <div class="flex flex-wrap gap-1.5">
        <span class="rounded-full bg-[var(--color-brand-primary)] px-3 py-1 text-[10px] font-bold text-[var(--color-text-inverse)]">특별 추천</span>
        <span class="rounded-full bg-[var(--color-surface)] px-3 py-1 text-[10px] font-bold text-[var(--color-text-secondary)]">어린이·청소년 전용</span>
        <span class="rounded-full bg-[var(--color-surface)] px-3 py-1 text-[10px] font-bold text-[var(--color-text-secondary)]">자유적립식</span>
      </div>

      <h1 class="mt-4 mb-0 text-[22px] font-extrabold tracking-[-0.025em]">KB Young Youth 적금</h1>
      <p class="mt-1.5 mb-0 text-[12px] text-[var(--color-text-secondary)]">KB국민은행</p>
      <p class="mt-3 mb-0 text-[12px] leading-[1.65] text-[var(--color-text-secondary)]">
        자녀가 성인이 될 때까지 장기 거래가 가능하고, 어린이·청소년을 위한 무료 보험가입
        서비스를 제공하는 적금이에요.
      </p>

      <dl class="mt-4 grid grid-cols-2 gap-2">
        <div class="rounded-[12px] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-3">
          <dt class="text-[10px] text-[var(--color-text-secondary)]">최고 금리</dt>
          <dd class="mt-1 mb-0 text-[15px] font-extrabold text-[var(--color-selected-text)]">연 3.40%</dd>
        </div>
        <div class="rounded-[12px] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-3">
          <dt class="text-[10px] text-[var(--color-text-secondary)]">기본 금리</dt>
          <dd class="mt-1 mb-0 text-[15px] font-extrabold">연 2.10%</dd>
        </div>
        <div class="rounded-[12px] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-3">
          <dt class="text-[10px] text-[var(--color-text-secondary)]">계약 기간</dt>
          <dd class="mt-1 mb-0 text-[15px] font-extrabold">12개월</dd>
        </div>
        <div class="rounded-[12px] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-3">
          <dt class="text-[10px] text-[var(--color-text-secondary)]">월 저축 한도</dt>
          <dd class="mt-1 mb-0 text-[15px] font-extrabold">최대 300만원</dd>
        </div>
      </dl>

      <button
        class="mt-3 h-11 w-full rounded-[11px] bg-[var(--color-brand-primary)] text-[14px] font-extrabold text-[var(--color-text-inverse)] active:bg-[var(--color-brand-primary-pressed)]"
        type="button"
      >
        가입하기
      </button>
      <button
        class="mt-2 flex h-10 w-full items-center justify-center gap-1 rounded-[11px] border border-[var(--color-border)] bg-[var(--color-surface)] text-[12px] font-bold text-[var(--color-text-secondary)] active:bg-[var(--color-surface-muted)]"
        type="button"
        :aria-pressed="isFavorite"
        @click="isFavorite = !isFavorite"
      >
        <Heart :size="15" :class="isFavorite ? 'fill-[#ff001b] text-[#ff001b]' : ''" />
        {{ isFavorite ? '관심상품 저장됨' : '관심상품 저장' }}
      </button>
    </section>

    <section class="mt-3 rounded-[18px] bg-[var(--color-selected-background)] p-4">
      <div class="flex items-center gap-2">
        <span class="grid size-8 place-items-center rounded-full bg-[var(--color-surface)] text-[var(--color-selected-text)]" aria-hidden="true">
          <Sparkles :size="17" />
        </span>
        <h2 class="m-0 text-[14px] font-extrabold">깨비에게 추천하는 이유</h2>
      </div>
      <p class="mt-3 mb-0 text-[11px] leading-[1.65] text-[var(--color-text-secondary)]">
        깨비는 현재 만 0세이므로 가입 시 성장축하 우대금리 대상이 될 수 있어요. 아동수당 수령,
        가족 등록, 자동이체 조건도 함께 확인해보세요.
      </p>
      <div class="mt-4 flex items-end justify-between border-t border-[var(--color-border)] pt-3">
        <span class="text-[11px] font-bold text-[var(--color-selected-text)]">목표 적합도</span>
        <strong class="text-[22px] text-[var(--color-selected-text)]">92%</strong>
      </div>
    </section>

    <section class="mt-3 overflow-hidden rounded-[18px] border border-[var(--color-border)] bg-[var(--color-surface)] shadow-sm">
      <button
        class="flex h-14 w-full items-center justify-between px-4 text-left"
        type="button"
        :aria-expanded="isBasicInfoOpen"
        @click="isBasicInfoOpen = !isBasicInfoOpen"
      >
        <h2 class="m-0 text-[16px] font-extrabold">상품 기본정보</h2>
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
        <dl v-if="isBasicInfoOpen" class="m-0 border-t border-[var(--color-border)] px-4">
          <div
            v-for="information in basicInformation"
            :key="information.label"
            class="border-b border-[var(--color-border)] py-3 last:border-0"
          >
            <dt class="text-[10px] font-bold text-[var(--color-text-secondary)]">{{ information.label }}</dt>
            <dd class="mt-1.5 mb-0 whitespace-pre-line text-[11px] leading-[1.55]">{{ information.value }}</dd>
          </div>
        </dl>
      </Transition>
    </section>

    <section class="mt-3 overflow-hidden rounded-[18px] border border-[var(--color-border)] bg-[var(--color-surface)] shadow-sm">
      <button
        class="flex h-14 w-full items-center justify-between px-4 text-left"
        type="button"
        :aria-expanded="isRateInfoOpen"
        @click="isRateInfoOpen = !isRateInfoOpen"
      >
        <h2 class="m-0 text-[16px] font-extrabold">금리와 우대조건</h2>
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
        <div v-if="isRateInfoOpen" class="border-t border-[var(--color-border)] px-4 pb-4">
          <div class="mt-4 rounded-[12px] bg-[var(--color-selected-background)] px-3 py-4">
            <span class="text-[10px] text-[var(--color-text-secondary)]">12개월 기준 · 세금공제 전</span>
            <strong class="mt-1 block text-[22px] text-[var(--color-selected-text)]">최고 연 3.40%</strong>
          </div>
          <ul class="mt-3 mb-0 grid list-none gap-2 p-0">
            <li
              v-for="rate in preferentialRates"
              :key="rate.title"
              class="flex items-start justify-between gap-3 rounded-[11px] border border-[var(--color-border)] px-3 py-3"
            >
              <div class="min-w-0">
                <strong class="block text-[11px]">{{ rate.title }}</strong>
                <span class="mt-1 block text-[9px] leading-[1.4] text-[var(--color-text-secondary)]">{{ rate.description }}</span>
              </div>
              <strong class="shrink-0 text-[10px] text-[var(--color-selected-text)]">{{ rate.rate }}</strong>
            </li>
          </ul>
        </div>
      </Transition>
    </section>

    <section class="mt-3 overflow-hidden rounded-[18px] border border-[var(--color-border)] bg-[var(--color-surface)] shadow-sm">
      <button
        class="flex h-14 w-full items-center justify-between px-4 text-left"
        type="button"
        :aria-expanded="isBenefitsOpen"
        @click="isBenefitsOpen = !isBenefitsOpen"
      >
        <h2 class="m-0 text-[16px] font-extrabold">부가혜택</h2>
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
          class="mt-0 mb-0 grid list-none gap-4 border-t border-[var(--color-border)] px-4 py-4"
        >
          <li v-for="benefit in benefits" :key="benefit.title" class="flex gap-3">
            <span class="grid size-8 shrink-0 place-items-center rounded-[10px] bg-[var(--color-surface-muted)] text-[var(--color-unselected-text)]">
              <component :is="benefit.icon" :size="16" />
            </span>
            <div>
              <strong class="block text-[11px]">{{ benefit.title }}</strong>
              <p class="mt-1 mb-0 text-[9px] leading-[1.45] text-[var(--color-text-secondary)]">{{ benefit.description }}</p>
            </div>
          </li>
        </ul>
      </Transition>
    </section>

    <section class="mt-3 overflow-hidden rounded-[18px] border border-[var(--color-border)] bg-[var(--color-surface)] shadow-sm">
      <button
        class="flex h-14 w-full items-center justify-between px-4 text-left"
        type="button"
        :aria-expanded="isMaturityOpen"
        @click="isMaturityOpen = !isMaturityOpen"
      >
        <h2 class="m-0 text-[16px] font-extrabold">예상 만기금액</h2>
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
        <div v-if="isMaturityOpen" class="border-t border-[var(--color-border)] px-4 pb-4">
          <label for="monthly-saving-amount" class="mt-4 block text-[11px] font-bold">월 저축금액</label>
          <input
            id="monthly-saving-amount"
            :value="monthlySavingAmount"
            class="mt-2 h-11 w-full rounded-[11px] border border-[var(--color-border)] px-3 text-[13px] outline-none focus:border-[var(--color-brand-primary)]"
            type="text"
            inputmode="numeric"
            @input="updateMonthlyAmount"
          />
          <label class="mt-4 block text-[11px] font-bold">적용 예상금리</label>
          <div class="mt-2 flex h-11 items-center rounded-[11px] border border-[var(--color-border)] px-3 text-[13px]">
            최고금리 연 3.40%
          </div>
          <dl class="mt-4 rounded-[13px] bg-[var(--color-brand-secondary)] px-4 py-4 text-[11px]">
            <div class="flex justify-between gap-3">
              <dt>총 납입원금</dt>
              <dd class="m-0 font-extrabold">{{ formatWon(principalAmount) }}</dd>
            </div>
            <div class="mt-3 flex justify-between gap-3">
              <dt>예상 세전이자</dt>
              <dd class="m-0 font-extrabold">{{ formatWon(expectedInterest) }}</dd>
            </div>
            <div class="mt-3 flex justify-between gap-3">
              <dt>예상 세후 만기금액</dt>
              <dd class="m-0 font-extrabold">{{ formatWon(expectedMaturityAmount) }}</dd>
            </div>
          </dl>
          <p class="mt-3 mb-0 text-[9px] leading-[1.5] text-[var(--color-text-secondary)]">
            월초 납입을 가정한 단순 계산 예시입니다. 실제 이자는 납입일, 일수, 세율, 우대조건 등에
            따라 달라집니다.
          </p>
        </div>
      </Transition>
    </section>

    <section class="mt-3 overflow-hidden rounded-[18px] border border-[var(--color-border)] bg-[var(--color-surface)] shadow-sm">
      <button
        class="flex h-14 w-full items-center justify-between px-4 text-left"
        type="button"
        :aria-expanded="isNoticeOpen"
        @click="isNoticeOpen = !isNoticeOpen"
      >
        <h2 class="m-0 text-[16px] font-extrabold">가입 전 확인</h2>
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
        <div v-if="isNoticeOpen" class="border-t border-[var(--color-border)] px-4 pb-4">
          <p class="mt-4 mb-0 rounded-[12px] bg-[var(--color-surface-muted)] px-4 py-4 text-[10px] leading-[1.65] text-[var(--color-text-secondary)]">
            최고금리는 모든 우대조건을 충족하고 만기 해지할 때 적용됩니다. 중도해지 시 더 낮은 이율이
            적용될 수 있으며, 납입 혜택은 가입 동의와 재예치 조건 등에 따라 달라집니다.
          </p>
          <p class="mt-3 mb-0 text-[9px] leading-[1.5] text-[var(--color-text-secondary)]">
            상품정보 기준: KB국민은행 공식 상품공시. 실제 가입 전 최신 상품설명서와 약관을 확인해주세요.
          </p>
        </div>
      </Transition>
    </section>

  </main>
</template>
