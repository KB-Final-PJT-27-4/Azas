<script setup lang="ts">
import { EllipsisVertical } from 'lucide-vue-next'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, getApiErrorMessage } from '@/api'
import { useToast } from '@/composables/useToast'

interface ManagedAccount {
  id: number
  bank: string
  bankMark: string
  bankColor: string
  name: string
  accountNumber: string
  owner: string
  balance: number
  isPrimary: boolean
}

const router = useRouter()
const { showToast } = useToast()
const openMenuId = ref<number | null>(null)
const accounts = ref<ManagedAccount[]>([])

const formatAmount = (amount: number) => `${amount.toLocaleString('ko-KR')}원`

const setPrimaryAccount = async (accountId: number) => {
  try {
    await api.setPrimaryAccountUsingPATCH(accountId)
    accounts.value = accounts.value.map((account) => ({ ...account, isPrimary: account.id === accountId }))
    openMenuId.value = null
    showToast('대표 계좌를 변경했습니다.', 'success')
  } catch (error) {
    showToast(getApiErrorMessage(error, '대표 계좌를 변경하지 못했습니다.'), 'error')
  }
}

const toggleAccountMenu = (accountId: number) => {
  openMenuId.value = openMenuId.value === accountId ? null : accountId
}

const editAccount = () => {
  openMenuId.value = null
}

const deleteAccount = async (accountId: number) => {
  try {
    await api.unlinkAccountUsingDELETE(accountId)
    const deletedAccount = accounts.value.find((account) => account.id === accountId)
    accounts.value = accounts.value.filter((account) => account.id !== accountId)
    if (deletedAccount?.isPrimary && accounts.value.length > 0) accounts.value[0]!.isPrimary = true
    openMenuId.value = null
    showToast('계좌 연결을 해제했습니다.', 'success')
  } catch (error) {
    showToast(getApiErrorMessage(error, '계좌 연결을 해제하지 못했습니다.'), 'error')
  }
}

const addAccount = () => {
  router.push({ name: 'Accounts' })
}

onMounted(async () => {
  try {
    const { data } = await api.getMyAccountsUsingGET()
    const details = await Promise.all(data.accounts.map(({ account_id }) => api.getAccountDetailUsingGET(account_id)))
    accounts.value = details.map(({ data: account }, index) => ({
      id: account.account_id,
      bank: account.bank_name,
      bankMark: account.bank_name.slice(0, 2),
      bankColor: '#FBC629',
      name: account.account_name,
      accountNumber: account.account_number,
      owner: account.account_holder_name,
      balance: account.balance,
      isPrimary: index === 0,
    }))
  } catch (error) {
    showToast(getApiErrorMessage(error, '계좌 목록을 불러오지 못했습니다.'), 'error')
  }
})
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-[var(--color-surface)] px-6 pt-8 pb-10 text-[var(--color-text-primary)]"
    @click="openMenuId = null"
  >
    <section aria-labelledby="accounts-title">
      <h1 id="accounts-title" class="text-[28px] leading-tight font-bold tracking-[-0.04em]">
        계좌 관리
      </h1>
      <p class="mt-3 text-sm text-[var(--color-text-secondary)]">
        계좌를 추가하고 대표 계좌를 설정할 수 있어요.
      </p>
    </section>

    <section class="mt-8" aria-labelledby="registered-accounts-title">
      <div class="flex items-center justify-between">
        <h2 id="registered-accounts-title" class="text-xl font-bold tracking-[-0.03em]">
          등록 계좌
        </h2>
        <span class="text-sm text-[var(--color-text-secondary)]">{{ accounts.length }}개</span>
      </div>

      <div class="mt-4 grid gap-4">
        <article
          v-for="account in accounts"
          :key="account.id"
          class="relative rounded-[20px] border border-[var(--color-border)] bg-[var(--color-surface)] p-5 shadow-sm"
        >
          <div class="flex items-start gap-4">
            <div
              class="grid size-[54px] shrink-0 place-items-center rounded-2xl text-base font-bold text-white"
              :style="{ backgroundColor: account.bankColor }"
              aria-hidden="true"
            >
              {{ account.bankMark }}
            </div>

            <div class="min-w-0 flex-1 flex flex-col">
              <div class="flex flex-wrap items-center gap-2">
                <h3 class="truncate text-base font-bold tracking-[-0.02em]">
                  {{ account.name }}
                </h3>
                <span
                  v-if="account.isPrimary"
                  class="shrink-0 rounded-full bg-[#FFF3BD] px-3 py-1 text-xs font-semibold text-[#8A6900]"
                >
                  대표 계좌
                </span>
              </div>
              <p class="mt-2 truncate text-xs text-[var(--color-text-secondary)]">
                {{ account.bank }} · {{ account.accountNumber }}
              </p>
            </div>

            <button
              class="grid size-8 shrink-0 place-items-center rounded-md bg-[var(--color-surface-muted)] text-[var(--color-text-secondary)] active:bg-[var(--color-selected-background)]"
              type="button"
              :aria-label="`${account.name} 더보기`"
              :aria-expanded="openMenuId === account.id"
              @click.stop="toggleAccountMenu(account.id)"
            >
              <EllipsisVertical :size="18" />
            </button>

            <div
              v-if="openMenuId === account.id"
              class="absolute top-[68px] right-5 z-10 w-40 overflow-hidden rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] py-1 shadow-sm"
              role="menu"
              @click.stop
            >
              <button
                class="h-11 w-full px-4 text-left text-sm font-semibold transition-colors hover:bg-[var(--color-surface-muted)]"
                type="button"
                role="menuitem"
                @click="editAccount"
              >
                수정
              </button>
              <button
                class="h-11 w-full px-4 text-left text-sm font-semibold transition-colors hover:bg-[var(--color-surface-muted)] disabled:cursor-default disabled:text-[var(--color-unselected-text)] disabled:hover:bg-white"
                type="button"
                role="menuitem"
                :disabled="account.isPrimary"
                @click="setPrimaryAccount(account.id)"
              >
                대표 계좌 설정
              </button>
              <button
                class="h-11 w-full px-4 text-left text-sm font-semibold text-[#E5484D] transition-colors hover:bg-[#FFF1F2]"
                type="button"
                role="menuitem"
                @click="deleteAccount(account.id)"
              >
                삭제
              </button>
            </div>
          </div>

          <div class="my-4 h-px bg-[var(--color-border)]"></div>

          <dl class="grid grid-cols-2 gap-4">
            <div>
              <dt class="text-xs text-[var(--color-text-secondary)]">예금주</dt>
              <dd class="mt-1 text-sm font-semibold">{{ account.owner }}</dd>
            </div>
            <div>
              <dt class="text-xs text-[var(--color-text-secondary)]">잔액</dt>
              <dd class="mt-1 text-sm font-semibold">{{ formatAmount(account.balance) }}</dd>
            </div>
          </dl>
        </article>
      </div>
    </section>

    <button
      class="mt-6 flex h-14 w-full items-center justify-center gap-2 rounded-2xl bg-[var(--color-brand-primary)] text-base font-bold text-white transition-colors active:bg-[var(--color-brand-primary-pressed)]"
      type="button"
      @click="addAccount"
    >
      계좌 추가하기
    </button>
  </main>
</template>
