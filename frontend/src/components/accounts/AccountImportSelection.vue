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

const recommendedAccounts = [
  {
    name: 'KB Young Youth 어린이통장',
    badge: '자녀 추천',
    badgeClass: 'bg-[#eaf7ff] text-[#179fdf]',
    selectedBadgeClass:
      'bg-[var(--color-brand-primary)] text-white shadow-[0_3px_8px_rgba(39,169,235,0.2)]',
    rate: '연 0.10%',
    period: '입출금 자유',
    description: '용돈과 생활비를 편리하게 관리할 수 있는 어린이·청소년 전용 계좌예요.',
    tags: ['#용돈관리', '#입출금자유', '#아이명의'],
  },
  {
    name: 'KB 꿈나무통장',
    badge: '첫 계좌 추천',
    badgeClass: 'bg-[#fff0f2] text-[#ef4d61]',
    rate: '연 0.10%',
    period: '입출금 자유',
    description: '아이의 첫 금융 습관을 시작하고 자산 흐름을 한눈에 관리할 수 있어요.',
    tags: ['#첫통장', '#금융습관', '#자산관리'],
  },
]
const selectedRecommendedAccount = ref<string | null>(null)

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
    class="flex min-h-[calc(100dvh-var(--app-header-height))] flex-col bg-white px-5 pt-5 pb-[calc(104px+env(safe-area-inset-bottom))]"
    aria-labelledby="empty-account-title"
  >
    <header>
      <h1
        id="empty-account-title"
        class="mt-2 text-[28px] leading-[1.3] font-extrabold tracking-[-0.035em] text-[var(--color-text-primary)]"
      >
        아이에게 꼭 맞는<br />KB 계좌를 골라봤어요
      </h1>
      <p class="mt-3 text-sm leading-6 text-[var(--color-text-secondary)]">
        계좌별 특징을 비교하고 자산 관리에 사용할 계좌를 선택해보세요.
      </p>
    </header>

    <section class="mt-7" aria-labelledby="recommended-accounts-title">
      <h2 id="recommended-accounts-title" class="sr-only">추천 입출금 계좌</h2>
      <ul class="grid list-none gap-3 p-0">
        <li v-for="account in recommendedAccounts" :key="account.name">
          <button
            class="recommended-account w-full rounded-[20px] border p-5 text-left"
            :class="selectedRecommendedAccount === account.name ? 'recommended-account--selected border-[var(--color-brand-primary)] bg-[#e8f7ff]' : 'border-[var(--color-border)] bg-white'"
            type="button"
            :aria-pressed="selectedRecommendedAccount === account.name"
            @click="selectedRecommendedAccount = account.name"
          >
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0">
                <span
                  class="inline-flex h-7 items-center rounded-full px-3 text-xs font-bold"
                  :class="
                    selectedRecommendedAccount === account.name
                      ? account.selectedBadgeClass ?? account.badgeClass
                      : account.badgeClass
                  "
                >
                  {{ account.badge }}
                </span>
                <h3 class="mt-3 text-[18px] leading-tight font-extrabold tracking-[-0.02em]">
                  {{ account.name }}
                </h3>
              </div>
              <span
                class="grid size-8 shrink-0 place-items-center rounded-full transition-colors"
                :class="selectedRecommendedAccount === account.name ? 'bg-[var(--color-brand-primary)] text-white' : 'border border-[#d7e0e5] bg-white text-transparent'"
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
                class="rounded-full px-3 py-1.5 text-[11px] font-semibold text-[var(--color-text-secondary)]"
                :class="selectedRecommendedAccount === account.name ? 'bg-white' : 'bg-[#f5f7f8]'"
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
          :disabled="!selectedRecommendedAccount"
          @click="emit('createAccount')"
        >
          {{ selectedRecommendedAccount ? '선택한 계좌 만들기' : '계좌를 선택해주세요' }}
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
