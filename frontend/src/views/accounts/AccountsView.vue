<script setup lang="ts">
import { ref } from 'vue'
import AccountRegistrationForm from '@/components/accounts/AccountRegistrationForm.vue'
import BankSelectionSheet from '@/components/accounts/BankSelectionSheet.vue'

const isBankSelectorOpen = ref(false)
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
      v-model:account-number="accountNumber"
      v-model:account-alias="accountAlias"
      :selected-bank="selectedBank"
      @open-bank-selector="isBankSelectorOpen = true"
    />

    <BankSelectionSheet
      v-if="isBankSelectorOpen"
      @close="isBankSelectorOpen = false"
      @select="selectBank"
    />
  </main>
</template>
