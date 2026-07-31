<script setup lang="ts">
import { ref } from 'vue'

import AccountCompleteStep from '@/components/account/AccountCompleteStep.vue'
import AccountConfirmStep from '@/components/account/AccountConfirmStep.vue'
import AccountInputStep from '@/components/account/AccountInputStep.vue'

type RegistrationStep = 1 | 2 | 3

const currentStep = ref<RegistrationStep>(1)

function goToConfirm(): void {
  currentStep.value = 2
}

function goToInput(): void {
  currentStep.value = 1
}

function completeRegistration(): void {
  currentStep.value = 3
}
</script>

<template>
  <main class="account-registration">
    <header class="account-registration__header">우리 아이 자산관리 서비스</header>

    <AccountInputStep v-if="currentStep === 1" @next="goToConfirm" />

    <AccountConfirmStep
      v-else-if="currentStep === 2"
      @edit="goToInput"
      @complete="completeRegistration"
    />

    <AccountCompleteStep v-else />
  </main>
</template>

<style scoped>
.account-registration {
  min-height: 100dvh;
  background: #fff;
}

.account-registration__header {
  height: 58px;
  display: flex;
  align-items: center;
  padding: 0 22px;
  border-bottom: 1px solid #e8edf2;
  font-size: 14px;
  font-weight: 700;
}
</style>
