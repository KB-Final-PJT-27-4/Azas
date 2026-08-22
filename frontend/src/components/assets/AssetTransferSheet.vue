<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Landmark, Plus, X } from 'lucide-vue-next'

import AssetAccountSelect from '@/components/assets/AssetAccountSelect.vue'
import type { AssetAccountSelectOption } from '@/components/assets/AssetAccountSelect.vue'
import AssetFormSheet from '@/components/assets/AssetFormSheet.vue'

const props = withDefaults(
  defineProps<{
    open: boolean
    targetAccountName?: string
    targetAccountNumber?: string
    initialAmount?: number
    initialMemo?: string
    initialSourceAccountId?: string
    sourceAccounts?: AssetAccountSelectOption[]
    targetAccounts?: AssetAccountSelectOption[]
  }>(),
  {
    targetAccountName: 'KB 아이사랑적금',
    targetAccountNumber: '123-456-789',
    initialAmount: 0,
    initialMemo: '',
    initialSourceAccountId: '',
    sourceAccounts: () => [],
    targetAccounts: () => [],
  },
)

const emit = defineEmits<{
  close: []
  transfer: [payload: { amount: number; memo: string; sourceAccountId: string; targetAccountId: string }]
}>()

const amountInput = ref('0')
const memo = ref('')
const sourceAccountId = ref('')
const targetAccountId = ref('')
const quickAmounts = [10000, 50000, 100000, 500000]

const sourceAccounts = computed(() => props.sourceAccounts)
const targetAccounts = computed(() =>
  props.targetAccounts.filter(({ id }) => id !== sourceAccountId.value),
)
const hasTransferAccounts = computed(
  () => sourceAccounts.value.length > 0 && targetAccounts.value.length > 0,
)
const selectedSourceAccount = computed(
  () => sourceAccounts.value.find(({ id }) => id === sourceAccountId.value) ?? sourceAccounts.value[0],
)

const amount = computed(() => Number(amountInput.value.replace(/\D/g, '')) || 0)

watch(
  () => props.open,
  (open) => {
    if (!open) return
    amountInput.value = props.initialAmount > 0 ? props.initialAmount.toLocaleString('ko-KR') : '0'
    memo.value = props.initialMemo
    sourceAccountId.value = sourceAccounts.value.some(
      ({ id }) => id === props.initialSourceAccountId,
    )
      ? props.initialSourceAccountId
      : sourceAccounts.value[0]?.id ?? ''
    targetAccountId.value = targetAccounts.value[0]?.id ?? ''
  },
)

watch(sourceAccountId, () => {
  if (!props.open) return
  if (!targetAccounts.value.some(({ id }) => id === targetAccountId.value)) {
    targetAccountId.value = targetAccounts.value[0]?.id ?? ''
  }
})

const updateAmount = (event: Event) => {
  const input = event.target as HTMLInputElement
  const digits = input.value.replace(/\D/g, '').replace(/^0+(?=\d)/, '')
  amountInput.value = digits ? Number(digits).toLocaleString('ko-KR') : '0'
  if (input.value !== amountInput.value) input.value = amountInput.value
}

const startAmountEdit = (event: FocusEvent) => {
  const input = event.target as HTMLInputElement
  requestAnimationFrame(() => {
    if (input.value === '0') input.select()
  })
}

const finishAmountEdit = () => {
  amountInput.value = amount.value.toLocaleString('ko-KR')
}

const addAmount = (value: number) => {
  amountInput.value = (amount.value + value).toLocaleString('ko-KR')
}

const clearAmount = () => {
  amountInput.value = '0'
}

const updateMemo = (event: Event) => {
  memo.value = (event.target as HTMLTextAreaElement).value
}

const submitTransfer = () => {
  if (amount.value <= 0 || !sourceAccountId.value || !targetAccountId.value) return
  emit('transfer', {
    amount: amount.value,
    memo: memo.value.trim(),
    sourceAccountId: sourceAccountId.value,
    targetAccountId: targetAccountId.value,
  })
}
</script>

<template>
  <AssetFormSheet
    :open="open"
    title="이체하기"
    title-id="asset-transfer-title"
    close-label="이체 창 닫기"
    @close="emit('close')"
  >
    <section
      v-if="!hasTransferAccounts"
      class="mt-5 rounded-[20px] border border-dashed border-[#cfe3ed] bg-[#f7fcff] px-5 py-8 text-center"
      aria-labelledby="empty-transfer-accounts-title"
    >
      <span
        class="mx-auto grid size-14 place-items-center rounded-full bg-[#e5f6ff] text-[var(--color-brand-primary)]"
        aria-hidden="true"
      >
        <Landmark :size="28" :stroke-width="2.2" />
      </span>
      <h3 id="empty-transfer-accounts-title" class="mt-4 text-[16px] font-extrabold">
        연결된 계좌가 없어요
      </h3>
      <p class="mt-2 text-[12px] leading-5 text-[var(--color-text-secondary)]">
        이체하려면 먼저 사용할 계좌를 연결해 주세요.<br />연결 후 출금·받는 계좌를 선택할 수 있어요.
      </p>
      <RouterLink
        :to="{ name: 'Accounts' }"
        class="mx-auto mt-5 inline-flex min-h-11 items-center justify-center gap-1.5 rounded-[13px] bg-[var(--color-brand-primary)] px-5 text-[13px] font-bold !text-white active:bg-[var(--color-brand-primary-pressed)]"
        @click="emit('close')"
      >
        <Plus :size="16" :stroke-width="2.6" />
        계좌 연결하기
      </RouterLink>
    </section>

    <form v-else class="mt-5" @submit.prevent="submitTransfer">
      <label class="block text-[12px] font-semibold">
        출금 계좌 <span class="text-[#f04444]">*</span>
      </label>
      <AssetAccountSelect
        v-model="sourceAccountId"
        class="mt-2"
        :options="sourceAccounts"
        label="출금 계좌 선택"
        show-balance
      />

      <label class="mt-4 block text-[12px] font-semibold">
        받는 계좌 <span class="text-[#f04444]">*</span>
      </label>
      <AssetAccountSelect
        v-model="targetAccountId"
        class="mt-2"
        :options="targetAccounts"
        label="받는 계좌 선택"
      />

      <label for="transfer-amount" class="mt-4 block text-[12px] font-semibold">
        이체 금액 <span class="text-[#f04444]">*</span>
      </label>
      <div class="relative mt-2">
        <input
          id="transfer-amount"
          :value="amountInput"
          class="h-10 w-full rounded-[12px] border border-[#dce8ee] pr-16 pl-3 text-[15px] outline-none focus:border-[var(--color-brand-primary)]"
          type="text"
          inputmode="numeric"
          @focus="startAmountEdit"
          @input="updateAmount"
          @blur="finishAmountEdit"
        />
        <span
          class="pointer-events-none absolute top-1/2 -translate-y-1/2 text-[15px]"
          :class="amount > 0 ? 'right-10' : 'right-3'"
        >
          원
        </span>
        <button
          v-if="amount > 0"
          class="absolute top-1/2 right-2 grid size-7 -translate-y-1/2 place-items-center rounded-full text-[#9aa6b2] active:bg-[#eef3f6]"
          type="button"
          aria-label="이체 금액 지우기"
          @click="clearAmount"
        >
          <X :size="16" :stroke-width="2.5" />
        </button>
      </div>
      <div class="mt-2.5 grid grid-cols-4 gap-2">
        <button
          v-for="quickAmount in quickAmounts"
          :key="quickAmount"
          class="h-[22px] rounded-full bg-[#e6eef3] text-[10px] text-[var(--color-text-secondary)] active:bg-[#d7e4eb]"
          type="button"
          @click="addAmount(quickAmount)"
        >
          +{{ quickAmount / 10000 }}만
        </button>
      </div>

      <label for="transfer-memo" class="mt-4 block text-[12px] font-semibold">메모</label>
      <div class="relative mt-2">
        <textarea
          id="transfer-memo"
          :value="memo"
          class="block h-[114px] w-full resize-none rounded-[12px] border border-[#dce8ee] px-3 pt-2.5 pb-7 text-[13px] leading-[1.35] outline-none focus:border-[var(--color-brand-primary)]"
          maxlength="50"
          placeholder="메모를 입력해 주세요."
          @input="updateMemo"
        ></textarea>
        <span
          class="pointer-events-none absolute right-3 bottom-2 text-[10px] tabular-nums text-[var(--color-text-secondary)]"
        >
          {{ memo.length }}/50
        </span>
      </div>

      <button
        class="mt-5 h-12 w-full rounded-[13px] bg-[var(--color-brand-primary)] text-[15px] font-semibold text-white active:bg-[var(--color-brand-primary-pressed)] disabled:bg-[#cbd8df]"
        type="submit"
        :disabled="amount <= 0 || !selectedSourceAccount || !targetAccountId"
      >
        이체하기
      </button>
    </form>
  </AssetFormSheet>
</template>
