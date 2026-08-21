<script setup lang="ts">
import { Check, Landmark } from 'lucide-vue-next'
import { computed, ref } from 'vue'

type ImportedAccount = {
  id: number
  bank: string
  number: string
  balance: number
}

const props = defineProps<{
  accounts: ImportedAccount[]
}>()

const emit = defineEmits<{
  connect: [accounts: ImportedAccount[]]
  createAccount: []
  later: []
}>()

const selectedAccountIds = ref<number[]>(props.accounts[0] ? [props.accounts[0].id] : [])
const selectedAccounts = computed(() =>
  props.accounts.filter(({ id }) => selectedAccountIds.value.includes(id)),
)

const recommendedProducts = [
  {
    name: 'KB Young Youth 적금',
    badge: '자녀 추천',
    rate: '최고 연 3.65%',
    period: '12개월',
    description: '아이의 미래를 위해 차곡차곡 준비하는 어린이·청소년 적금이에요.',
    tags: ['#자유적립', '#아이미래', '#목돈마련'],
  },
  {
    name: 'KB아이사랑적금',
    badge: '가족 추천',
    rate: '최고 연 10.00%',
    period: '12개월',
    description: '아이를 키우는 가정의 든든한 목돈 마련을 도와줘요.',
    tags: ['#아이사랑', '#육아응원', '#월30만원'],
  },
]
const selectedRecommendedProduct = ref<string | null>(null)

const toggleAccount = (accountId: number) => {
  selectedAccountIds.value = selectedAccountIds.value.includes(accountId)
    ? selectedAccountIds.value.filter((id) => id !== accountId)
    : [...selectedAccountIds.value, accountId]
}
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
    class="flex min-h-[calc(100dvh-var(--app-header-height))] flex-col px-5 pt-5 pb-10"
    aria-labelledby="empty-account-title"
  >
    <div class="empty-account-content flex flex-col pb-8">
      <p class="text-sm font-semibold text-[var(--color-text-primary)]">본인 명의의 계좌가 없어요</p>
      <h1
        id="empty-account-title"
        class="mt-1 text-[25px] leading-tight font-extrabold tracking-[-0.03em] text-[var(--color-text-primary)]"
      >
        자산 관리를 시작해볼까요?
      </h1>
      <p class="mt-4 text-sm leading-6 text-[var(--color-text-secondary)]">
        자녀의 미래 자산을 준비하려면<br />먼저 관리에 사용할 계좌가 필요해요.
      </p>

      <article class="empty-account-benefits mt-6 rounded-[24px] border border-[var(--color-border)] bg-white p-5">
        <h2 class="text-[18px] font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]">
          계좌가 있으면 이렇게 활용할 수 있어요
        </h2>
        <ul class="mt-5 grid gap-5">
          <li class="flex gap-3.5">
            <span class="grid size-8 shrink-0 place-items-center rounded-full bg-[#eaf8ff] text-[#27a9eb]"><Check :size="16" :stroke-width="3" /></span>
            <span><strong class="block text-[15px] font-extrabold">아이를 위한 자산 관리</strong><small class="mt-1 block text-[13px] leading-5 text-[var(--color-text-secondary)]">아이를 위해 모으는 돈과<br />저축 현황을 한눈에 확인해요.</small></span>
          </li>
          <li class="flex gap-3.5">
            <span class="grid size-8 shrink-0 place-items-center rounded-full bg-[#eaf8ff] text-[#27a9eb]"><Check :size="16" :stroke-width="3" /></span>
            <span><strong class="block text-[15px] font-extrabold">목표와 연결해 꾸준히 저축</strong><small class="mt-1 block text-[13px] leading-5 text-[var(--color-text-secondary)]">대학교육, 독립자금 등 미래 목표와 연결해요.</small></span>
          </li>
          <li class="flex gap-3.5">
            <span class="grid size-8 shrink-0 place-items-center rounded-full bg-[#eaf8ff] text-[#27a9eb]"><Check :size="16" :stroke-width="3" /></span>
            <span><strong class="block text-[15px] font-extrabold">자녀에게 간편하게 이체</strong><small class="mt-1 block text-[13px] leading-5 text-[var(--color-text-secondary)]">연결한 계좌에서 자녀 계좌로<br />필요한 돈을 보낼 수 있어요.</small></span>
          </li>
        </ul>
      </article>

      <section class="mt-7" aria-labelledby="recommended-products-title">
        <div class="mt-3 grid gap-3">
          <button
            v-for="product in recommendedProducts"
            :key="product.name"
            class="recommended-product w-full rounded-[20px] border bg-white p-4 text-left"
            :class="selectedRecommendedProduct === product.name ? 'recommended-product--selected border-[var(--color-brand-primary)] bg-[#f1faff]' : 'border-[var(--color-border)]'"
            type="button"
            :aria-pressed="selectedRecommendedProduct === product.name"
            @click="selectedRecommendedProduct = product.name"
          >
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0">
                <span class="inline-flex h-7 items-center rounded-full bg-[#eaf7ff] px-3 text-[11px] font-bold text-[#179fdf]">
                  {{ product.badge }}
                </span>
                <h3 class="mt-2.5 text-[16px] font-extrabold tracking-[-0.02em]">{{ product.name }}</h3>
              </div>
              <span
                class="grid size-7 shrink-0 place-items-center rounded-full"
                :class="selectedRecommendedProduct === product.name ? 'bg-[var(--color-brand-primary)] text-white' : 'bg-[#f3f7f9] text-[var(--color-text-secondary)]'"
              >
                <Check v-if="selectedRecommendedProduct === product.name" :size="15" :stroke-width="3" />
                <span v-else>›</span>
              </span>
            </div>
            <div class="mt-3 flex items-center gap-2 text-xs font-bold">
              <strong class="text-[var(--color-selected-text)]">{{ product.rate }}</strong>
              <span class="text-[#c7d0d5]">·</span>
              <span>{{ product.period }}</span>
            </div>
            <p class="mt-2 text-[12px] leading-5 text-[var(--color-text-secondary)]">{{ product.description }}</p>
            <div class="mt-3 flex flex-wrap gap-1.5">
              <span v-for="tag in product.tags" :key="tag" class="rounded-full bg-[#f5f7f8] px-2.5 py-1 text-[10px] font-semibold text-[var(--color-text-secondary)]">
                {{ tag }}
              </span>
            </div>
          </button>
        </div>
      </section>
    </div>

    <div class="mt-auto grid gap-2">
      <button class="min-h-[54px] rounded-xl bg-[var(--color-brand-primary)] text-sm font-bold text-white shadow-[0_6px_16px_rgba(39,169,235,0.2)] active:bg-[var(--color-brand-primary-pressed)]" type="button" @click="emit('createAccount')">
        KB국민은행 계좌 만들기
      </button>
    </div>
  </section>
</template>

<style scoped>
.imported-account {
  transition: border-color 150ms ease, background-color 150ms ease, transform 150ms ease;
}

.imported-account:active { transform: scale(0.985); }
.imported-account:focus-visible { outline: 3px solid rgb(45 169 232 / 20%); outline-offset: 2px; }

@media (prefers-reduced-motion: reduce) {
  .imported-account { transition-duration: 1ms; }
}
</style>
