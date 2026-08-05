<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { useRouter } from 'vue-router'

import allowanceRequestPigUrl from '@/assets/images/child/child-allowance-request-pig.png'
import { BaseToast } from '@/components/feedback'
import { allowanceOptions } from '@/mocks/childHome'

const router = useRouter()
const selectedAmount = ref(10_000)
const customAmount = ref('')
const reason = ref('')
const toastMessage = ref('')
const toastVariant = ref<'success' | 'error'>('error')
let toastTimer: ReturnType<typeof window.setTimeout> | null = null

const formatCurrency = (amount: number) => `${amount.toLocaleString('ko-KR')}원`
const requestAmount = computed(() => Number(customAmount.value) || selectedAmount.value)
const allowanceValidationMessage = computed(() => {
  if (requestAmount.value <= 0) {
    return '필요한 금액을 입력해주세요.'
  }

  if (!reason.value.trim()) {
    return '용돈이 필요한 이유를 입력해주세요.'
  }

  return ''
})
const canSubmitAllowanceRequest = computed(() => allowanceValidationMessage.value === '')

const showToast = (message: string, variant: 'success' | 'error') => {
  if (toastTimer) {
    window.clearTimeout(toastTimer)
  }

  toastMessage.value = message
  toastVariant.value = variant
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
    toastTimer = null
  }, 1800)
}

const selectAmount = (amount: number) => {
  selectedAmount.value = amount
  customAmount.value = ''
}

const submitAllowanceRequest = () => {
  if (!canSubmitAllowanceRequest.value) {
    showToast(allowanceValidationMessage.value, 'error')
    return
  }

  showToast('용돈 요청을 보냈어요.', 'success')
  window.setTimeout(() => {
    router.push('/child/allowance-done')
  }, 450)
}

onBeforeUnmount(() => {
  if (toastTimer) {
    window.clearTimeout(toastTimer)
  }
})
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-[#eef8ff] px-5 pt-8 pb-8">
    <section class="text-center">
      <img
        class="mx-auto w-[190px] select-none object-contain"
        :src="allowanceRequestPigUrl"
        alt=""
        aria-hidden="true"
      />
      <h1 class="mt-4 mb-3 text-[24px] leading-[1.35] font-black text-[var(--color-text-primary)]">
        부모님께<br />
        용돈을 요청해볼까요?
      </h1>
      <p class="m-0 text-[length:var(--font-size-sm)] leading-[1.5] text-[var(--color-text-secondary)]">
        하고 싶은 게 있다면<br />
        부모님께 용돈을 요청해보세요!
      </p>
    </section>

    <section class="mt-6 rounded-[24px] bg-white px-5 py-5 shadow-[0_14px_32px_rgb(110_122_138_/_10%)]">
      <fieldset class="m-0 border-0 p-0">
        <legend class="mb-3 text-[length:var(--font-size-md)] font-extrabold text-[var(--color-text-primary)]">
          얼마나 필요한가요?
        </legend>
        <div class="grid grid-cols-3 gap-2">
          <button
            v-for="amount in allowanceOptions"
            :key="amount"
            class="h-11 rounded-full border text-[length:var(--font-size-sm)] font-extrabold"
            :class="
              selectedAmount === amount && !customAmount
                ? 'border-[var(--color-brand-primary)] bg-[var(--color-brand-primary)] text-white'
                : 'border-[var(--color-border)] bg-white text-[var(--color-text-secondary)]'
            "
            type="button"
            @click="selectAmount(amount)"
          >
            {{ formatCurrency(amount) }}
          </button>
        </div>
        <input
          v-model="customAmount"
          class="mt-3 h-11 w-full rounded-full border border-[var(--color-border)] px-4 text-center text-[length:var(--font-size-sm)] outline-none focus:border-[var(--color-brand-primary)]"
          inputmode="numeric"
          placeholder="직접 입력하기"
        />
      </fieldset>

      <div class="my-5 h-px bg-[var(--color-border)]" />

      <label class="grid gap-3 text-[length:var(--font-size-md)] font-extrabold text-[var(--color-text-primary)]">
        어떤 이유인가요?
        <input
          v-model="reason"
          class="h-11 rounded-full border border-[var(--color-border)] px-4 text-center text-[length:var(--font-size-sm)] font-normal outline-none focus:border-[var(--color-brand-primary)]"
          placeholder="직접 입력하기"
        />
      </label>
    </section>

    <button
      class="mt-5 h-14 w-full rounded-[14px] border-0 text-[length:var(--font-size-md)] font-extrabold text-white"
      :class="canSubmitAllowanceRequest ? 'bg-[var(--color-brand-primary)]' : 'bg-[#c8d2da]'"
      type="button"
      @click="submitAllowanceRequest"
    >
      요청 보내기
    </button>

    <BaseToast
      v-if="toastMessage"
      :message="toastMessage"
      :variant="toastVariant"
    />
  </main>
</template>
