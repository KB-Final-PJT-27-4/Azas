<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AccountConnectionMethod from '@/components/accounts/AccountConnectionMethod.vue'
import AccountImportSelection from '@/components/accounts/AccountImportSelection.vue'
import AccountRegistrationComplete from '@/components/accounts/AccountRegistrationComplete.vue'
import AccountRegistrationForm from '@/components/accounts/AccountRegistrationForm.vue'
import AccountRegistrationConfirmation from '@/components/accounts/AccountRegistrationConfirmation.vue'
import BankSelectionSheet from '@/components/accounts/BankSelectionSheet.vue'
import { api, getApiErrorMessage } from '@/api'
import { resolveCurrentChildId } from '@/api/context'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { showToast } = useToast()
const currentChildId = ref<number | null>(null)
const isBankSelectorOpen = ref(false)
const registrationStep = ref<'method' | 'import' | 'empty' | 'form' | 'confirmation' | 'complete'>('method')
const selectedBank = ref('')
const accountNumber = ref('')
const accountAlias = ref('')
const slideDirection = ref<'forward' | 'backward'>('forward')
const importedAccounts = ref<{ id: number; bank: string; number: string; balance: number }[]>([])
const registeredAccounts = ref<
  { bank: string; accountNumber: string; accountName: string; balance: number }[]
>([])

const startAccountImport = () => {
  slideDirection.value = 'forward'
  registrationStep.value = 'import'
}

const showAccountOpeningGuide = () => {
  slideDirection.value = 'forward'
  registrationStep.value = 'empty'
}

const connectImportedAccount = async (accounts: (typeof importedAccounts.value)[number][]) => {
  const [primaryAccount] = accounts
  if (!primaryAccount) return
  try {
    const { data } = await api.linkUsingPOST(undefined, {
      account_ids: accounts.map(({ id }) => id),
      child_id: currentChildId.value ?? undefined,
      owner_type: 'CHILD',
    })
    registeredAccounts.value = (data.accounts ?? []).map((account, index) => ({
      bank: account.bank_name ?? accounts[index]?.bank ?? '',
      accountNumber: accounts[index]?.number ?? '',
      accountName: account.account_name ?? `${account.bank_name ?? '연결'} 계좌`,
      balance: account.balance ?? 0,
    }))
    slideDirection.value = 'forward'
    registrationStep.value = 'complete'
  } catch (error) {
    showToast(getApiErrorMessage(error, '계좌를 연결하지 못했어요.'), 'error')
  }
}

const createKbAccount = async () => {
  try {
    const products = await api.getProductsUsingGET(undefined, undefined, 'DEMAND_DEPOSIT', 1)
    const product = (products.data.items?.[0] ?? {}) as unknown as { financial_product_id?: number }
    if (!product.financial_product_id) throw new Error('개설 가능한 상품을 찾을 수 없어요.')
    const { data } = await api.openUsingPOST(undefined, {
      child_id: currentChildId.value ?? undefined,
      financial_product_id: product.financial_product_id,
      initial_deposit_amount: 0,
      owner_type: 'CHILD',
    })
    registeredAccounts.value = [{
      bank: data.bank_name ?? 'KB국민은행',
      accountNumber: data.account_number ?? '',
      accountName: data.account_name ?? '자녀 계좌',
      balance: data.balance ?? 0,
    }]
    slideDirection.value = 'forward'
    registrationStep.value = 'complete'
  } catch (error) {
    showToast(getApiErrorMessage(error, '계좌를 개설하지 못했어요.'), 'error')
  }
}

const selectBank = (bank: string) => {
  selectedBank.value = bank
  isBankSelectorOpen.value = false
}

const goToConfirmation = () => {
  slideDirection.value = 'forward'
  registrationStep.value = 'confirmation'
}

const goToForm = () => {
  slideDirection.value = 'backward'
  registrationStep.value = 'form'
}

const completeRegistration = async () => {
  const normalizedNumber = accountNumber.value.replace(/\D/g, '')
  const account = importedAccounts.value.find(
    ({ number }) => number.replace(/\D/g, '') === normalizedNumber,
  )
  if (!account) {
    showToast('연결 가능한 계좌 목록에서 계좌번호를 확인해 주세요.', 'error')
    return
  }
  await connectImportedAccount([account])
}

const loadDiscoveredAccounts = async () => {
  try {
    currentChildId.value = await resolveCurrentChildId()
    const { data } = await api.getDiscoveredAccountsUsingGET(
      'CHILD',
      undefined,
      currentChildId.value,
    )
    importedAccounts.value = data.accounts.map((account) => ({
      id: account.account_id,
      bank: account.bank_name,
      number: account.account_number,
      balance: account.balance,
    }))
  } catch (error) {
    showToast(getApiErrorMessage(error, '연결 가능한 계좌를 불러오지 못했어요.'), 'error')
  }
}

onMounted(loadDiscoveredAccounts)
</script>

<template>
  <main class="overflow-x-hidden">
    <Transition :name="`account-slide-${slideDirection}`" mode="out-in">
      <div :key="registrationStep">
        <AccountConnectionMethod
          v-if="registrationStep === 'method'"
          @import="startAccountImport"
          @create="showAccountOpeningGuide"
        />

        <AccountRegistrationForm
          v-else-if="registrationStep === 'form'"
          v-model:account-number="accountNumber"
          v-model:account-alias="accountAlias"
          :selected-bank="selectedBank"
          :is-bank-selector-open="isBankSelectorOpen"
          @open-bank-selector="isBankSelectorOpen = true"
          @next="goToConfirmation"
        />

        <AccountImportSelection
          v-else-if="registrationStep === 'import'"
          :accounts="importedAccounts"
          @connect="connectImportedAccount"
          @create-account="createKbAccount"
          @later="router.push('/home')"
        />

        <AccountImportSelection
          v-else-if="registrationStep === 'empty'"
          :accounts="[]"
          @create-account="createKbAccount"
          @later="router.push('/home')"
        />

        <AccountRegistrationConfirmation
          v-else-if="registrationStep === 'confirmation'"
          :selected-bank="selectedBank"
          :account-number="accountNumber"
          :account-alias="accountAlias"
          @edit="goToForm"
          @register="completeRegistration"
        />

        <AccountRegistrationComplete
          v-else
          :accounts="registeredAccounts"
          @home="router.push('/home')"
        />
      </div>
    </Transition>

    <BankSelectionSheet
      :open="isBankSelectorOpen"
      @close="isBankSelectorOpen = false"
      @select="selectBank"
    />
  </main>
</template>

<style scoped>
.account-slide-forward-enter-active,
.account-slide-forward-leave-active,
.account-slide-backward-enter-active,
.account-slide-backward-leave-active {
  transition: transform 150ms cubic-bezier(0.25, 0.8, 0.25, 1), opacity 120ms ease-out;
}

.account-slide-forward-enter-from,
.account-slide-backward-leave-to {
  transform: translateX(18px);
  opacity: 0;
}

.account-slide-forward-leave-to,
.account-slide-backward-enter-from {
  transform: translateX(-18px);
  opacity: 0;
}

@media (prefers-reduced-motion: reduce) {
  .account-slide-forward-enter-active,
  .account-slide-forward-leave-active,
  .account-slide-backward-enter-active,
  .account-slide-backward-leave-active {
    transition-duration: 1ms;
  }
}
</style>
