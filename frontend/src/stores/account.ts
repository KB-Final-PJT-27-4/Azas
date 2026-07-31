import { reactive } from 'vue'
import { defineStore } from 'pinia'

import type { AccountRegistrationForm } from '@/types/account'

const initialForm = (): AccountRegistrationForm => ({
  bankCode: '',
  bankName: '',
  accountNumber: '',
  accountAlias: '',
})

export const useAccountStore = defineStore('account', () => {
  const form = reactive<AccountRegistrationForm>(initialForm())

  function setAccountForm(data: AccountRegistrationForm): void {
    Object.assign(form, data)
  }

  function resetAccountForm(): void {
    Object.assign(form, initialForm())
  }

  return {
    form,
    setAccountForm,
    resetAccountForm,
  }
})
