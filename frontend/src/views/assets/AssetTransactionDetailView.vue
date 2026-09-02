<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { Pencil, X } from 'lucide-vue-next'
import { useRoute, useRouter } from 'vue-router'

import { api, getApiErrorMessage } from '@/api'
import { updateChildcareTransactionTag } from '@/api/childcareTransactions'
import { getChildren } from '@/api/context'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const { showToast } = useToast()
const transaction = ref({
  accountLabel: '',
  amount: 0,
  depositName: '',
  depositAccountNumber: '',
  withdrawalName: '',
  withdrawalAccountNumber: '',
  transactedAt: '',
  balanceAfterTransaction: 0,
  direction: '',
  isParentAccount: false,
})

const memo = ref('')
const memoDraft = ref(memo.value)
const isEditingMemo = ref(false)
const memoInput = ref<HTMLTextAreaElement | null>(null)
const children = ref<Array<{ childId: number; name: string }>>([])
const selectedChildId = ref<number | null>(null)
const childcareIncluded = ref(route.query.childcareIncluded === 'true')
const isChildcareSheetOpen = ref(false)
const isUpdatingChildcare = ref(false)
const canManageChildcare = computed(() =>
  transaction.value.direction === 'DEBIT' && transaction.value.isParentAccount,
)

const startMemoEdit = async () => {
  memoDraft.value = memo.value
  isEditingMemo.value = true
  await nextTick()
  memoInput.value?.focus()
  memoInput.value?.select()
}

const saveMemo = () => {
  memo.value = memoDraft.value.trim()
  isEditingMemo.value = false
}

const cancelMemoEdit = () => {
  memoDraft.value = memo.value
  isEditingMemo.value = false
}

const updateMemoDraft = (event: Event) => {
  memoDraft.value = (event.target as HTMLTextAreaElement).value
}

const goToAccount = () =>
  router.push({ name: 'AssetDetail', params: { assetId: String(route.params.assetId) } })
const formatWon = (amount: number) => `${amount.toLocaleString('ko-KR')} 원`

const loadTransaction = async () => {
  try {
    const [transactionResponse, accountResponse] = await Promise.all([
      api.getTransactionDetailUsingGET(Number(route.params.transactionId)),
      api.getAccountDetailUsingGET(Number(route.params.assetId)),
    ])
    const detail = transactionResponse.data
    transaction.value = {
      accountLabel: accountResponse.data.account_name,
      amount: detail.amount,
      depositName: detail.deposit_account.account_name ?? detail.deposit_account.bank_name ?? '',
      depositAccountNumber: detail.deposit_account.account_number ?? '',
      withdrawalName: detail.withdrawal_account.account_name ?? detail.withdrawal_account.bank_name ?? '',
      withdrawalAccountNumber: detail.withdrawal_account.account_number ?? '',
      transactedAt: new Date(detail.occurred_at).toLocaleString('ko-KR'),
      balanceAfterTransaction: detail.balance_after ?? 0,
      direction: detail.direction,
      isParentAccount: accountResponse.data.owner_type === 'PARENT',
    }
    memo.value = detail.memo ?? ''
    memoDraft.value = memo.value
  } catch (error) {
    showToast(getApiErrorMessage(error, '거래 상세를 불러오지 못했어요.'), 'error')
  }
}

const openChildcareSheet = async () => {
  if (!canManageChildcare.value) return
  try {
    const result = await getChildren()
    children.value = result.flatMap((child) =>
      child.child_id && child.name ? [{ childId: child.child_id, name: child.name }] : [],
    )
    selectedChildId.value = children.value[0]?.childId ?? null
    isChildcareSheetOpen.value = true
  } catch (error) {
    showToast(getApiErrorMessage(error, '자녀 목록을 불러오지 못했어요.'), 'error')
  }
}

const updateChildcare = async (childId: number | null) => {
  isUpdatingChildcare.value = true
  try {
    const { data } = await updateChildcareTransactionTag(Number(route.params.transactionId), childId)
    childcareIncluded.value = data.childcare_included
    isChildcareSheetOpen.value = false
    showToast(data.childcare_included ? '양육비에 포함했어요.' : '양육비 포함을 해제했어요.', 'success')
  } catch (error) {
    showToast(getApiErrorMessage(error, '부모 계좌의 외부 출금만 양육비에 포함할 수 있어요.'), 'error')
  } finally {
    isUpdatingChildcare.value = false
  }
}

onMounted(loadTransaction)
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-[var(--color-surface)]"
  >
    <article class="px-5 pt-7 pb-10 text-[var(--color-text-primary)]">
      <h1 class="m-0 text-[22px] leading-tight font-bold tracking-[-0.02em]">
        {{ transaction.accountLabel }}
      </h1>

      <form v-if="isEditingMemo" class="mt-3" aria-label="메모 수정" @submit.prevent="saveMemo">
        <div class="relative">
          <textarea
            ref="memoInput"
            :value="memoDraft"
            class="block h-24 w-full resize-none rounded-lg border border-[var(--color-brand-primary)] px-3 pt-2.5 pb-7 text-[14px] leading-6 text-[var(--color-text-secondary)] outline-none focus:ring-2 focus:ring-[var(--color-selected-background)]"
            aria-label="메모"
            maxlength="50"
            @input="updateMemoDraft"
            @keydown.esc="cancelMemoEdit"
          ></textarea>
          <span
            class="pointer-events-none absolute right-3 bottom-2 text-[11px] tabular-nums text-[var(--color-text-secondary)]"
          >
            {{ memoDraft.length }}/50
          </span>
        </div>
        <div class="mt-1.5 flex justify-end">
          <button
            class="h-9 shrink-0 rounded-lg bg-[var(--color-brand-primary)] px-4 text-[13px] font-bold text-[var(--color-text-inverse)] active:bg-[var(--color-brand-primary-pressed)]"
            type="submit"
            aria-label="메모 저장"
          >
            완료
          </button>
        </div>
      </form>

      <div v-else-if="memo" class="mt-3 flex min-w-0 items-start gap-1.5">
        <p class="m-0 min-w-0 text-[14px] leading-7 text-[var(--color-text-secondary)]">
          {{ memo }}
        </p>
        <button
          class="grid size-8 shrink-0 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[var(--color-unselected-background)]"
          type="button"
          aria-label="메모 수정"
          @click="startMemoEdit"
        >
          <Pencil :size="19" :stroke-width="2.8" />
        </button>
      </div>

      <button
        v-else
        class="mt-3 inline-flex h-9 items-center gap-1.5 rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] px-3 text-[13px] font-semibold text-[var(--color-text-secondary)] active:bg-[var(--color-unselected-background)]"
        type="button"
        @click="startMemoEdit"
      >
        메모 추가
        <Pencil :size="16" :stroke-width="2.5" />
      </button>

      <div class="mt-5 border-t border-[var(--color-border)] pt-6">
        <dl class="m-0 grid grid-cols-[minmax(92px,1fr)_minmax(0,2fr)] gap-x-5 gap-y-6">
          <dt class="text-[15px] font-semibold text-[var(--color-text-secondary)]">거래 금액</dt>
          <dd class="m-0 text-right text-[18px] font-semibold text-[var(--color-selected-text)]">
            +{{ formatWon(transaction.amount) }}
          </dd>

          <dt class="text-[15px] font-semibold text-[var(--color-text-secondary)]">입금처</dt>
          <dd class="m-0 text-right text-[15px] leading-7">
            <strong class="block font-semibold">{{ transaction.depositName }}</strong>
            <span class="block text-[var(--color-text-secondary)]">
              {{ transaction.depositAccountNumber }}
            </span>
          </dd>

          <dt class="text-[15px] font-semibold text-[var(--color-text-secondary)]">출금처</dt>
          <dd class="m-0 text-right text-[15px] leading-7">
            <strong class="block font-semibold">{{ transaction.withdrawalName }}</strong>
            <span class="block text-[var(--color-text-secondary)]">
              {{ transaction.withdrawalAccountNumber }}
            </span>
          </dd>

          <dt class="text-[15px] font-semibold text-[var(--color-text-secondary)]">거래시각</dt>
          <dd class="m-0 text-right text-[15px] font-medium">{{ transaction.transactedAt }}</dd>

          <dt class="text-[15px] font-semibold text-[var(--color-text-secondary)]">거래 후 잔액</dt>
          <dd class="m-0 text-right text-[15px] font-medium">
            {{ formatWon(transaction.balanceAfterTransaction) }}
          </dd>

          <template v-if="canManageChildcare">
            <dt class="text-[15px] font-semibold text-[var(--color-text-secondary)]">양육비</dt>
            <dd class="m-0 text-right">
              <button
                class="rounded-full px-3 py-1.5 text-[12px] font-bold"
                :class="childcareIncluded ? 'bg-[#fff4cd] text-[#a87500]' : 'bg-[#edf2f5] text-[var(--color-text-secondary)]'"
                type="button"
                @click="childcareIncluded ? updateChildcare(null) : openChildcareSheet()"
              >{{ childcareIncluded ? '양육비 포함됨' : '양육비에 포함하기' }}</button>
            </dd>
          </template>
        </dl>
      </div>

      <button
        class="mt-16 h-14 w-full rounded-[14px] bg-[var(--color-brand-primary)] text-[16px] font-extrabold text-[var(--color-text-inverse)] transition-colors active:bg-[var(--color-brand-primary-pressed)]"
        type="button"
        @click="goToAccount"
      >
        확인
      </button>
    </article>

    <Teleport to="body">
      <div v-if="isChildcareSheetOpen" class="fixed inset-0 z-[var(--z-index-overlay)] flex items-end bg-black/40" @click.self="isChildcareSheetOpen = false">
        <section class="w-full rounded-t-[24px] bg-white px-5 pt-4 pb-[calc(24px+env(safe-area-inset-bottom))]" role="dialog" aria-modal="true" aria-labelledby="childcare-sheet-title">
          <div class="flex items-center justify-between"><h2 id="childcare-sheet-title" class="m-0 text-[18px] font-extrabold">양육비에 포함할까요?</h2><button class="grid size-9 place-items-center rounded-full text-[var(--color-text-secondary)]" type="button" aria-label="닫기" @click="isChildcareSheetOpen = false"><X :size="20" /></button></div>
          <p class="mt-2 mb-0 text-[12px] leading-5 text-[var(--color-text-secondary)]">선택한 자녀의 양육비 리포트에 이 거래가 반영돼요.</p>
          <label class="mt-5 block text-[12px] font-bold" for="childcare-child">자녀 선택</label>
          <select id="childcare-child" v-model="selectedChildId" class="mt-2 h-12 w-full rounded-xl border border-[var(--color-border)] bg-white px-3 text-[14px]" :disabled="isUpdatingChildcare"><option v-for="child in children" :key="child.childId" :value="child.childId">{{ child.name }}</option></select>
          <button class="mt-5 h-13 w-full rounded-[14px] bg-[var(--color-brand-primary)] text-[15px] font-extrabold text-white disabled:opacity-50" type="button" :disabled="selectedChildId === null || isUpdatingChildcare" @click="updateChildcare(selectedChildId)">포함하기</button>
        </section>
      </div>
    </Teleport>
  </main>
</template>
