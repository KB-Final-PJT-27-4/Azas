<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import ChildBottomNavigation from '@/components/child/ChildBottomNavigation.vue'
import { api, getApiErrorMessage } from '@/api'
import { resolveCurrentChildId } from '@/api/context'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { showToast } = useToast()
type AccountOption = { id: number; name: string; bankName: string; accountNumber: string; balance: number }
const sourceAccount = ref<AccountOption | null>(null)
const contacts = ref<AccountOption[]>([])
const selectedContactId = ref<number | null>(null)
const transferAmount = ref('')
const transferMessage = ref('')

const selectedContact = computed(
  () => contacts.value.find((contact) => contact.id === selectedContactId.value) ?? null,
)
const transferAmountValue = computed(() => Number(transferAmount.value.replace(/\D/g, '')) || 0)
const formattedTransferAmount = computed(() =>
  transferAmountValue.value > 0 ? transferAmountValue.value.toLocaleString('ko-KR') : '',
)
const canSubmit = computed(
  () =>
    Boolean(selectedContactId.value) &&
    transferAmountValue.value > 0 &&
    transferAmountValue.value <= (sourceAccount.value?.balance ?? 0) &&
    Boolean(selectedContact.value),
)

const updateAmount = (event: Event) => {
  const input = event.target as HTMLInputElement
  transferAmount.value = input.value.replace(/\D/g, '')
}

const addAmount = (amount: number) => {
  transferAmount.value = String(transferAmountValue.value + amount)
}

const transferAll = () => {
  transferAmount.value = String(sourceAccount.value?.balance ?? 0)
}

const submitTransfer = async () => {
  if (!canSubmit.value) return
  try {
    await api.createTransferUsingPOST(crypto.randomUUID(), {
      source_account_id: sourceAccount.value!.id,
      destination_account_id: selectedContact.value!.id,
      amount: transferAmountValue.value,
      memo: transferMessage.value.trim() || undefined,
    })
    await router.push('/child/home')
  } catch (error) {
    showToast(getApiErrorMessage(error, '이체하지 못했습니다.'), 'error')
  }
}

onMounted(async () => {
  try {
    const childId = await resolveCurrentChildId()
    const { data } = await api.getChildAccountsUsingGET(childId)
    const accounts = data.accounts.map((account) => ({
      id: account.account_id,
      name: account.account_name,
      bankName: account.account_name,
      accountNumber: account.account_number,
      balance: account.balance,
    }))
    sourceAccount.value = accounts[0] ?? null
    contacts.value = accounts.slice(1)
    selectedContactId.value = contacts.value[0]?.id ?? null
  } catch (error) {
    showToast(getApiErrorMessage(error, '계좌를 불러오지 못했습니다.'), 'error')
  }
})
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-5 pb-24">
    <section class="grid gap-6">
      <label class="grid gap-3 text-[length:var(--font-size-md)] font-bold">
        누구에게 보낼까요?
        <select
          v-model="selectedContactId"
          class="h-12 rounded-[10px] border border-[var(--color-border)] bg-white px-4 text-[length:var(--font-size-sm)] outline-none focus:border-[var(--color-brand-primary)]"
        >
          <option v-for="contact in contacts" :key="contact.id" :value="contact.id">
            {{ contact.name }} · {{ contact.bankName }}
          </option>
        </select>
      </label>

      <label class="grid gap-3 text-[length:var(--font-size-md)] font-bold">
        얼마를 보낼까요?
        <div class="relative">
          <input
            :value="formattedTransferAmount"
            class="h-14 w-full border-0 border-b border-[var(--color-border)] px-0 pr-9 text-[32px] font-bold outline-none focus:border-[var(--color-brand-primary)]"
            inputmode="numeric"
            placeholder="0"
            type="text"
            @input="updateAmount"
          />
          <span
            class="pointer-events-none absolute top-1/2 right-0 -translate-y-1/2 text-[length:var(--font-size-md)] font-bold"
          >
            원
          </span>
        </div>
      </label>

      <div class="grid grid-cols-4 gap-2">
        <button
          class="h-9 rounded-[8px] border border-[var(--color-border)] bg-white text-[length:var(--font-size-xs)] font-bold"
          type="button"
          @click="addAmount(1000)"
        >
          +1,000
        </button>
        <button
          class="h-9 rounded-[8px] border border-[var(--color-border)] bg-white text-[length:var(--font-size-xs)] font-bold"
          type="button"
          @click="addAmount(5000)"
        >
          +5,000
        </button>
        <button
          class="h-9 rounded-[8px] border border-[var(--color-border)] bg-white text-[length:var(--font-size-xs)] font-bold"
          type="button"
          @click="addAmount(10000)"
        >
          +10,000
        </button>
        <button
          class="h-9 rounded-[8px] border border-[var(--color-border)] bg-white text-[length:var(--font-size-xs)] font-bold"
          type="button"
          @click="transferAll"
        >
          전액
        </button>
      </div>

      <label class="grid gap-3 text-[length:var(--font-size-md)] font-bold">
        메시지 <span class="text-[length:var(--font-size-xs)] text-[var(--color-text-secondary)]">(선택)</span>
        <textarea
          v-model="transferMessage"
          class="min-h-[88px] resize-none rounded-[10px] border border-[var(--color-border)] px-4 py-3 text-[length:var(--font-size-sm)] font-normal outline-none focus:border-[var(--color-brand-primary)]"
          maxlength="100"
          placeholder="메시지를 입력하세요"
        />
        <span class="text-right text-[11px] font-normal text-[var(--color-text-secondary)]">
          {{ transferMessage.length }} / 100
        </span>
      </label>
    </section>

    <button
      class="fixed bottom-[92px] left-1/2 h-14 w-[calc(100%-40px)] max-w-[calc(var(--app-max-width)-40px)] -translate-x-1/2 rounded-[14px] border-0 text-[length:var(--font-size-md)] font-bold text-white"
      :class="canSubmit ? 'bg-[var(--color-brand-primary)]' : 'bg-[#cbd8df]'"
      type="button"
      :disabled="!canSubmit"
      @click="submitTransfer"
    >
      이체하기
    </button>

    <ChildBottomNavigation />
  </main>
</template>
