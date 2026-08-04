<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AccountRegistrationComplete from '@/components/accounts/AccountRegistrationComplete.vue'
import AccountRegistrationForm from '@/components/accounts/AccountRegistrationForm.vue'
import AccountRegistrationConfirmation from '@/components/accounts/AccountRegistrationConfirmation.vue'
import BankSelectionSheet from '@/components/accounts/BankSelectionSheet.vue'

const router = useRouter()
const isBankSelectorOpen = ref(false)
const registrationStep = ref<'form' | 'confirmation' | 'complete'>('form')
const selectedBank = ref('')
const accountNumber = ref('')
const accountAlias = ref('')

const selectBank = (bank: string) => {
  selectedBank.value = bank
  isBankSelectorOpen.value = false
}
</script>

<template>
  <main>
    <AccountRegistrationForm
      v-if="registrationStep === 'form'"
      v-model:account-number="accountNumber"
      v-model:account-alias="accountAlias"
      :selected-bank="selectedBank"
      @open-bank-selector="isBankSelectorOpen = true"
      @next="registrationStep = 'confirmation'"
    />

    <AccountRegistrationConfirmation
      v-else-if="registrationStep === 'confirmation'"
      :selected-bank="selectedBank"
      :account-number="accountNumber"
      :account-alias="accountAlias"
      @edit="registrationStep = 'form'"
      @register="registrationStep = 'complete'"
    />

    <AccountRegistrationComplete v-else @create-plan="router.push('/goals')" />

    <BankSelectionSheet
      v-if="isBankSelectorOpen"
      @close="isBankSelectorOpen = false"
      @select="selectBank"
    />
  </main>
</template>
