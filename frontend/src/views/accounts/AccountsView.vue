<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AccountConnectionMethod from '@/components/accounts/AccountConnectionMethod.vue'
import AccountImportEmptyGuide from '@/components/accounts/AccountImportEmptyGuide.vue'
import AccountImportSelection from '@/components/accounts/AccountImportSelection.vue'
import AccountRegistrationComplete from '@/components/accounts/AccountRegistrationComplete.vue'
import AccountRegistrationForm from '@/components/accounts/AccountRegistrationForm.vue'
import AccountRegistrationConfirmation from '@/components/accounts/AccountRegistrationConfirmation.vue'
import BankSelectionSheet from '@/components/accounts/BankSelectionSheet.vue'
import { api, getApiErrorMessage } from '@/api'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const route = useRoute()
const { showToast } = useToast()
const currentChildId = ref<number | null>(null)
const accountOwnerType = ref<'PARENT' | 'CHILD'>('CHILD')
const isBankSelectorOpen = ref(false)
const registrationStep = ref<
  'method' | 'import' | 'import-empty' | 'empty' | 'form' | 'confirmation' | 'complete'
>('method')
const selectedBank = ref('')
const accountNumber = ref('')
const accountAlias = ref('')
const slideDirection = ref<'forward' | 'backward'>('forward')
const importedAccounts = ref<
  {
    id: number
    bank: string
    number: string
    balance: number
    productType: string
    accountName?: string
  }[]
>([])
const registeredAccounts = ref<
  { bank: string; accountNumber: string; accountName: string; balance: number }[]
>([])

const startAccountImport = () => {
  slideDirection.value = 'forward'
  registrationStep.value = importedAccounts.value.length > 0 ? 'import' : 'import-empty'
}

const showAccountOpeningGuide = () => {
  slideDirection.value = 'forward'
  registrationStep.value = 'empty'
}

const connectImportedAccount = async (accounts: (typeof importedAccounts.value)[number][]) => {
  const [primaryAccount] = accounts
  if (!primaryAccount) return
  if (
    accountOwnerType.value === 'PARENT' &&
    !accounts.some(({ productType }) => productType === 'DEMAND_DEPOSIT')
  ) {
    showToast('자녀 계좌보다 먼저 부모 입출금계좌를 연결해 주세요.', 'error')
    return
  }
  try {
    const { data } = await api.linkUsingPOST(undefined, {
      account_ids: accounts.map(({ id }) => id),
      child_id: accountOwnerType.value === 'CHILD' ? currentChildId.value ?? undefined : undefined,
      owner_type: accountOwnerType.value,
    })
    registeredAccounts.value = (data.accounts ?? []).map((account, index) => ({
      bank: account.bank_name ?? accounts[index]?.bank ?? '',
      accountNumber: account.account_number ?? accounts[index]?.number ?? '',
      accountName:
        account.account_name ?? accounts[index]?.accountName ?? `${account.bank_name ?? '연결'} 계좌`,
      balance: account.balance ?? 0,
    }))
    slideDirection.value = 'forward'
    registrationStep.value = 'complete'
  } catch (error) {
    showToast(getApiErrorMessage(error, '계좌를 연결하지 못했어요.'), 'error')
  }
}

const createKbAccount = async (product: { id: number; name: string; bankName: string }) => {
  try {
    const { data } = await api.openUsingPOST(undefined, {
      child_id: accountOwnerType.value === 'CHILD' ? currentChildId.value ?? undefined : undefined,
      financial_product_id: product.id,
      initial_deposit_amount: 0,
      owner_type: accountOwnerType.value,
    })
    registeredAccounts.value = [{
      bank: data.bank_name ?? product.bankName,
      accountNumber: data.account_number ?? '',
      accountName: product.name,
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
    accountOwnerType.value = 'PARENT'
    currentChildId.value = null
    const { data } = await api.getDiscoveredAccountsUsingGET(
      'PARENT',
      undefined,
      undefined,
    )
    importedAccounts.value = data.accounts.map((account) => ({
      id: account.account_id,
      bank: account.bank_name,
      number: account.account_number,
      balance: account.balance,
      productType: account.account_product_type,
      accountName:
        account.account_product_type === 'SAVINGS'
          ? `${account.bank_name.replace(/은행$/, '')} 적금`
          : `${account.bank_name.replace(/은행$/, '')} 입출금통장`,
    }))
  } catch (error) {
    showToast(getApiErrorMessage(error, '연결 가능한 계좌를 불러오지 못했어요.'), 'error')
  }
}

const leaveRegistration = () => {
  const next =
    typeof route.query.next === 'string' && route.query.next.startsWith('/')
      ? route.query.next
      : '/home'
  void router.push(next)
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
          :owner-type="accountOwnerType"
          @connect="connectImportedAccount"
          @create-account="createKbAccount"
          @later="leaveRegistration"
        />

        <AccountImportEmptyGuide
          v-else-if="registrationStep === 'import-empty'"
          @create="showAccountOpeningGuide"
          @later="leaveRegistration"
        />

        <AccountImportSelection
          v-else-if="registrationStep === 'empty'"
          :accounts="[]"
          :owner-type="accountOwnerType"
          @create-account="createKbAccount"
          @later="leaveRegistration"
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
          @home="leaveRegistration"
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
