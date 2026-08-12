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
  connect: [account: ImportedAccount]
  createAccount: []
  later: []
}>()

const selectedAccountId = ref<number | null>(props.accounts[0]?.id ?? null)
const selectedAccount = computed(() =>
  props.accounts.find(({ id }) => id === selectedAccountId.value),
)
</script>

<template>
  <section
    v-if="accounts.length"
    class="flex min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] flex-col px-5 pt-7 pb-5"
    aria-labelledby="imported-account-title"
  >
    <div>
      <h1
        id="imported-account-title"
        class="text-[22px] leading-tight font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]"
      >
        연결할 계좌를 선택해주세요
      </h1>
      <p class="mt-3 text-sm text-[var(--color-text-secondary)]">{{ accounts.length }}개의 계좌</p>

      <div class="mt-3 grid gap-3" role="radiogroup" aria-label="불러온 계좌 목록">
        <button
          v-for="account in accounts"
          :key="account.id"
          class="imported-account flex min-h-[88px] w-full items-center rounded-2xl border px-4 text-left"
          :class="
            selectedAccountId === account.id
              ? 'border-[#8cd7fa] bg-[#f2fbff]'
              : 'border-[var(--color-border)] bg-white'
          "
          type="button"
          role="radio"
          :aria-checked="selectedAccountId === account.id"
          @click="selectedAccountId = account.id"
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
              selectedAccountId === account.id
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
      :disabled="!selectedAccount"
      @click="selectedAccount && emit('connect', selectedAccount)"
    >
      선택한 계좌 연결하기
    </button>
  </section>

  <section
    v-else
    class="flex min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] flex-col px-5 pt-10 pb-6"
    aria-labelledby="empty-account-title"
  >
    <div>
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

      <article class="mt-6 rounded-2xl border border-[var(--color-border)] bg-white p-5">
        <h2 class="text-sm font-bold text-[var(--color-text-primary)]">
          계좌가 있으면 이렇게 활용할 수 있어요
        </h2>
        <ul class="mt-4 grid gap-4">
          <li class="flex gap-3">
            <span class="grid size-6 shrink-0 place-items-center rounded-full bg-[#eaf8ff] text-[#27a9eb]"><Check :size="14" :stroke-width="3" /></span>
            <span><strong class="block text-sm">아이를 위한 자산 관리</strong><small class="mt-1 block leading-5 text-[var(--color-text-secondary)]">아이를 위해 모으는 돈과<br />저축 현황을 한눈에 확인해요.</small></span>
          </li>
          <li class="flex gap-3">
            <span class="grid size-6 shrink-0 place-items-center rounded-full bg-[#eaf8ff] text-[#27a9eb]"><Check :size="14" :stroke-width="3" /></span>
            <span><strong class="block text-sm">목표와 연결해 꾸준히 저축</strong><small class="mt-1 block leading-5 text-[var(--color-text-secondary)]">대학교육, 독립자금 등 미래 목표와 연결해요.</small></span>
          </li>
          <li class="flex gap-3">
            <span class="grid size-6 shrink-0 place-items-center rounded-full bg-[#eaf8ff] text-[#27a9eb]"><Check :size="14" :stroke-width="3" /></span>
            <span><strong class="block text-sm">자녀에게 간편하게 이체</strong><small class="mt-1 block leading-5 text-[var(--color-text-secondary)]">연결한 계좌에서 자녀 계좌로<br />필요한 돈을 보낼 수 있어요.</small></span>
          </li>
        </ul>
      </article>
    </div>

    <div class="mt-auto grid gap-2">
      <button class="min-h-[54px] rounded-xl bg-[var(--color-brand-primary)] text-sm font-bold text-white shadow-[0_6px_16px_rgba(39,169,235,0.2)] active:bg-[var(--color-brand-primary-pressed)]" type="button" @click="emit('createAccount')">
        KB국민은행 계좌 만들기
      </button>
      <button class="min-h-11 text-sm font-medium text-[var(--color-text-secondary)]" type="button" @click="emit('later')">
        나중에 할게요
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
