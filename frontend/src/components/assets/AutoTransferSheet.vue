<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { X } from 'lucide-vue-next'

import AssetAccountSelect from '@/components/assets/AssetAccountSelect.vue'
import AssetFormSheet from '@/components/assets/AssetFormSheet.vue'

type AccountOption = {
  id: string
  label: string
  tag: string
  name: string
  number: string
  balance: number
}

type AutoTransferFormData = {
  title: string
  sourceAccountId: string
  targetAccountId: string
  amount: number
  transferDay: number
}

const props = defineProps<{
  open: boolean
  mode: 'create' | 'edit'
  sourceAccountOptions: AccountOption[]
  targetAccountOptions: AccountOption[]
  initialData: AutoTransferFormData
}>()

const emit = defineEmits<{
  close: []
  save: [payload: AutoTransferFormData]
}>()

const title = ref('')
const sourceAccountId = ref('')
const targetAccountId = ref('')
const amountInput = ref('0')
const transferDay = ref(1)
const quickAmounts = [10_000, 50_000, 100_000, 500_000]

const amount = computed(() => Number(amountInput.value.replace(/\D/g, '')) || 0)
const isValid = computed(
  () =>
    title.value.trim().length > 0 &&
    sourceAccountId.value.length > 0 &&
    targetAccountId.value.length > 0 &&
    amount.value > 0 &&
    Number.isInteger(transferDay.value) &&
    transferDay.value >= 1 &&
    transferDay.value <= 28,
)

watch(
  () => props.open,
  (open) => {
    if (!open) return

    title.value = props.initialData.title
    sourceAccountId.value = props.initialData.sourceAccountId
    targetAccountId.value = props.initialData.targetAccountId
    amountInput.value = props.initialData.amount.toLocaleString('ko-KR')
    transferDay.value = props.initialData.transferDay
  },
)

const updateAmount = (event: Event) => {
  const input = event.target as HTMLInputElement
  const digits = input.value.replace(/\D/g, '')
  amountInput.value = digits || '0'
  if (input.value !== amountInput.value) input.value = amountInput.value
}

const startAmountEdit = (event: FocusEvent) => {
  amountInput.value = String(amount.value)
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

const submit = () => {
  if (!isValid.value) return

  emit('save', {
    title: title.value.trim(),
    sourceAccountId: sourceAccountId.value,
    targetAccountId: targetAccountId.value,
    amount: amount.value,
    transferDay: transferDay.value,
  })
}
</script>

<template>
  <AssetFormSheet
    :open="open"
    :title="mode === 'create' ? '자동이체 설정' : '자동이체 수정'"
    title-id="auto-transfer-sheet-title"
    close-label="자동이체 설정 창 닫기"
    @close="emit('close')"
  >
    <form class="mt-5" @submit.prevent="submit">
      <label for="auto-transfer-name" class="block text-[12px] font-semibold">
        자동이체 이름 <span class="text-[#f04444]">*</span>
      </label>
      <input
        id="auto-transfer-name"
        v-model="title"
        class="mt-2 h-10 w-full rounded-[12px] border border-[#dce8ee] px-3 text-[13px] outline-none focus:border-[var(--color-brand-primary)]"
        type="text"
        maxlength="20"
        placeholder="예: 매달 적금"
      />

      <label class="mt-4 block text-[12px] font-semibold">
        출금 계좌 <span class="text-[#f04444]">*</span>
      </label>
      <AssetAccountSelect
        v-model="sourceAccountId"
        class="mt-2"
        :options="
          sourceAccountOptions.map((account) => ({
            id: account.id,
            name: account.label,
            number: account.number,
            balance: account.balance,
            tag: account.tag,
          }))
        "
        label="자동이체 출금 계좌 선택"
        show-balance
      />

      <label class="mt-4 block text-[12px] font-semibold">
        받는 계좌 <span class="text-[#f04444]">*</span>
      </label>
      <AssetAccountSelect
        v-model="targetAccountId"
        class="mt-2"
        :options="
          targetAccountOptions.map((account) => ({
            id: account.id,
            name: account.label,
            number: account.number,
            tag: account.tag,
          }))
        "
        label="자동이체 받는 계좌 선택"
      />

      <label for="auto-transfer-amount" class="mt-4 block text-[12px] font-semibold">
        이체 금액 <span class="text-[#f04444]">*</span>
      </label>
      <div class="relative mt-2">
        <input
          id="auto-transfer-amount"
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
          aria-label="자동이체 금액 지우기"
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
          +{{ quickAmount / 10_000 }}만
        </button>
      </div>

      <label for="auto-transfer-day" class="mt-4 block text-[12px] font-semibold">
        이체일 <span class="text-[#f04444]">*</span>
      </label>
      <div class="relative mt-2">
        <input
          id="auto-transfer-day"
          v-model.number="transferDay"
          class="h-10 w-full rounded-[12px] border border-[#dce8ee] px-3 pr-10 text-[13px] outline-none focus:border-[var(--color-brand-primary)]"
          type="number"
          inputmode="numeric"
          min="1"
          max="28"
        />
        <span
          class="pointer-events-none absolute top-1/2 right-3 -translate-y-1/2 text-[13px] text-[var(--color-text-secondary)]"
        >
          일
        </span>
      </div>
      <p class="mt-2 mb-0 text-[10px] text-[var(--color-text-secondary)]">
        자동이체일은 매월 1일부터 28일 사이로 설정할 수 있어요.
      </p>

      <button
        class="mt-5 h-12 w-full rounded-[13px] bg-[var(--color-brand-primary)] text-[15px] font-semibold text-white active:bg-[var(--color-brand-primary-pressed)] disabled:bg-[#cbd8df]"
        type="submit"
        :disabled="!isValid"
      >
        {{ mode === 'create' ? '자동이체 등록' : '변경 내용 저장' }}
      </button>
    </form>
  </AssetFormSheet>
</template>
