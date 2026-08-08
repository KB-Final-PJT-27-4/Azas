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
const slideDirection = ref<'forward' | 'backward'>('forward')

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
  slideDirection.value = 'forward'
  registrationStep.value = 'complete'
}
</script>

<template>
  <main class="overflow-x-hidden">
    <Transition :name="`account-slide-${slideDirection}`" mode="out-in">
      <div :key="registrationStep">
        <AccountRegistrationForm
          v-if="registrationStep === 'form'"
          v-model:account-number="accountNumber"
          v-model:account-alias="accountAlias"
          :selected-bank="selectedBank"
          :is-bank-selector-open="isBankSelectorOpen"
          @open-bank-selector="isBankSelectorOpen = true"
          @next="goToConfirmation"
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
          @home="router.push('/home')"
          @create-goal="router.push('/goals')"
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
