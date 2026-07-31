<script setup lang="ts">
import { computed, reactive } from 'vue'

import { useAccountStore } from '@/stores/account'
import type { AccountRegistrationForm, Bank } from '@/types/account'

const emit = defineEmits<{
  next: []
}>()

const accountStore = useAccountStore()

const banks: Bank[] = [
  { code: 'KAKAO', name: '카카오뱅크' },
  { code: 'KB', name: 'KB국민은행' },
  { code: 'SHINHAN', name: '신한은행' },
]

const form = reactive<AccountRegistrationForm>({
  bankCode: accountStore.form.bankCode,
  bankName: accountStore.form.bankName,
  accountNumber: accountStore.form.accountNumber,
  accountAlias: accountStore.form.accountAlias,
})

const isAccountVerified = reactive({
  value: false,
})

const isNextDisabled = computed(() => {
  return (
    !form.bankCode || !form.accountNumber || !form.accountAlias.trim() || !isAccountVerified.value
  )
})

function selectBank(): void {
  const selectedBank = banks.find((bank) => bank.code === form.bankCode)

  form.bankName = selectedBank?.name ?? ''
  isAccountVerified.value = false
}

function verifyAccount(): void {
  if (!form.bankCode || !form.accountNumber) {
    return
  }

  // TODO: 계좌 확인 API 연결
  isAccountVerified.value = true
}

function submitForm(): void {
  if (isNextDisabled.value) {
    return
  }

  accountStore.setAccountForm({ ...form })
  emit('next')
}
</script>

<template>
  <section class="step">
    <div class="step-indicator">
      <span class="step-indicator__bar is-active" />
      <span class="step-indicator__bar" />
    </div>

    <h1>계좌를 등록하고 저축을 시작해요</h1>
    <p class="description">아이의 목표 저축을 함께 관리할 계좌를 등록해 주세요.</p>

    <form class="form" @submit.prevent="submitForm">
      <label class="field">
        <span class="field__label">은행 선택</span>

        <select v-model="form.bankCode" class="field__control" @change="selectBank">
          <option value="">은행을 선택해주세요</option>
          <option v-for="bank in banks" :key="bank.code" :value="bank.code">
            {{ bank.name }}
          </option>
        </select>
      </label>

      <label class="field">
        <span class="field__label">계좌번호 입력</span>

        <div class="account-number">
          <input
            v-model="form.accountNumber"
            inputmode="numeric"
            placeholder="-없이 숫자만 입력해주세요"
            maxlength="20"
            @input="isAccountVerified.value = false"
          />

          <button type="button" @click="verifyAccount">계좌 확인</button>
        </div>
      </label>

      <label class="field">
        <span class="field__label">계좌별칭(선택)</span>

        <div class="alias-input">
          <input v-model.trim="form.accountAlias" maxlength="20" placeholder="예) 아이 저축계좌" />

          <span>{{ form.accountAlias.length }} / 20</span>
        </div>
      </label>

      <div class="bottom-actions">
        <button type="button" class="button button--secondary">건너뛰기</button>

        <button type="submit" class="button button--primary" :disabled="isNextDisabled">
          다음
        </button>
      </div>
    </form>
  </section>
</template>
