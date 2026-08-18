<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AccountConnectionMethod from '@/components/accounts/AccountConnectionMethod.vue'
import AccountImportSelection from '@/components/accounts/AccountImportSelection.vue'
import AccountRegistrationComplete from '@/components/accounts/AccountRegistrationComplete.vue'
import AccountRegistrationForm from '@/components/accounts/AccountRegistrationForm.vue'
import AccountRegistrationConfirmation from '@/components/accounts/AccountRegistrationConfirmation.vue'
import BankSelectionSheet from '@/components/accounts/BankSelectionSheet.vue'

const router = useRouter()
const isBankSelectorOpen = ref(false)
const registrationStep = ref<'method' | 'import' | 'empty' | 'form' | 'confirmation' | 'complete'>('method')
const selectedBank = ref('')
const accountNumber = ref('')
const accountAlias = ref('')
const slideDirection = ref<'forward' | 'backward'>('forward')
const importedAccounts = ref([
  { id: 1, bank: 'KB국민은행', number: '1234-567-890123', balance: 12450000 },
  { id: 2, bank: 'KB국민은행', number: '9876-543-210987', balance: 3200000 },
  { id: 3, bank: 'KB국민은행', number: '1111-222-333444', balance: 1520000 },
])
const registeredAccounts = ref<
  { bank: string; accountNumber: string; accountName: string; balance: number }[]
>([])

const startAccountImport = () => {
  slideDirection.value = 'forward'
  registrationStep.value = 'import'
}

const startManualRegistration = () => {
  slideDirection.value = 'forward'
  registrationStep.value = 'form'
}

const showAccountOpeningGuide = () => {
  slideDirection.value = 'forward'
  registrationStep.value = 'empty'
}

const connectImportedAccount = (accounts: (typeof importedAccounts.value)[number][]) => {
  const [primaryAccount] = accounts
  if (!primaryAccount) return

  registeredAccounts.value = accounts.map((account, index) => ({
    bank: account.bank,
    accountNumber: account.number,
    accountName: accounts.length > 1 ? `${account.bank} 계좌 ${index + 1}` : `${account.bank} 계좌`,
    balance: account.balance,
  }))
  slideDirection.value = 'forward'
  registrationStep.value = 'complete'
}

const createKbAccount = () => {
  registeredAccounts.value = [{
    bank: 'KB국민은행',
    accountNumber: '123-456-789012',
    accountName: 'KB국민은행 자녀 계좌',
    balance: 0,
  }]
  slideDirection.value = 'forward'
  registrationStep.value = 'complete'
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

const completeRegistration = () => {
  registeredAccounts.value = [{
    bank: selectedBank.value,
    accountNumber: accountNumber.value,
    accountName: accountAlias.value || `${selectedBank.value} 계좌`,
    balance: 0,
  }]
  slideDirection.value = 'forward'
  registrationStep.value = 'complete'
}
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
