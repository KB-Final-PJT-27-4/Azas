<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { CheckCircle2, ChevronRight, Landmark, Plus, Smartphone } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import { api, getApiErrorMessage } from '@/api'

defineEmits<{
  import: []
  create: []
}>()

const { showToast } = useToast()
const phone = ref('')
const verificationCode = ref('')
const isCodeSent = ref(false)
const isPhoneVerified = ref(false)
const verificationError = ref('')
const verificationId = ref<number | null>(null)
const isSendingCode = ref(false)
const isVerifyingCode = ref(false)
const remainingSeconds = ref(180)
let verificationTimer: ReturnType<typeof setInterval> | null = null
const canSendCode = computed(() => /^010-\d{4}-\d{4}$/.test(phone.value))
const canVerify = computed(() =>
  isCodeSent.value && remainingSeconds.value > 0 && verificationCode.value.length === 6,
)
const timerText = computed(() => {
  const minutes = Math.floor(remainingSeconds.value / 60)
  const seconds = String(remainingSeconds.value % 60).padStart(2, '0')
  return `${minutes}:${seconds}`
})

const stopVerificationTimer = () => {
  if (verificationTimer === null) return
  clearInterval(verificationTimer)
  verificationTimer = null
}

const startVerificationTimer = () => {
  stopVerificationTimer()
  remainingSeconds.value = 180
  verificationTimer = setInterval(() => {
    if (remainingSeconds.value <= 1) {
      remainingSeconds.value = 0
      stopVerificationTimer()
      return
    }
    remainingSeconds.value -= 1
  }, 1000)
}

const formatPhone = (event: Event) => {
  const input = event.target as HTMLInputElement
  const digits = input.value.replace(/\D/g, '').slice(0, 11)
  phone.value = digits.length <= 3
    ? digits
    : digits.length <= 7
      ? `${digits.slice(0, 3)}-${digits.slice(3)}`
      : `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`
  input.value = phone.value
  stopVerificationTimer()
  remainingSeconds.value = 180
  isCodeSent.value = false
  verificationCode.value = ''
  isPhoneVerified.value = false
}

const sendVerificationCode = async () => {
  if (!canSendCode.value || isSendingCode.value) {
    showToast('휴대폰 번호를 정확히 입력해 주세요.', 'error')
    return
  }
  isSendingCode.value = true
  try {
    const { data } = await api.sendVerificationCodeUsingPOST({
      phone_number: phone.value.replace(/-/g, ''),
    })
    verificationId.value = data.verification_id
    isCodeSent.value = true
    verificationCode.value = ''
    verificationError.value = ''
    startVerificationTimer()
    showToast('인증번호를 전송했습니다.', 'info')
  } catch (error) {
    showToast(getApiErrorMessage(error, '인증번호를 전송하지 못했어요.'), 'error')
  } finally {
    isSendingCode.value = false
  }
}

const verifyPhone = async () => {
  if (!canVerify.value || isVerifyingCode.value) return
  if (!verificationId.value) {
    verificationError.value = '인증번호를 먼저 전송해 주세요.'
    return
  }
  isVerifyingCode.value = true
  try {
    await api.confirmVerificationCodeUsingPOST(verificationId.value, {
      verification_code: verificationCode.value,
    })
    stopVerificationTimer()
    isPhoneVerified.value = true
    verificationError.value = ''
    showToast('휴대폰 인증이 완료되었습니다.', 'success', 2200, 'slightly-above')
  } catch (error) {
    verificationError.value = getApiErrorMessage(error, '인증번호가 일치하지 않습니다.')
  } finally {
    isVerifyingCode.value = false
  }
}

const updateVerificationCode = (event: Event) => {
  verificationCode.value = (event.target as HTMLInputElement).value.replace(/\D/g, '').slice(0, 6)
  verificationError.value = ''
}

onBeforeUnmount(stopVerificationTimer)
</script>

<template>
  <section
    class="flex min-h-[calc(100dvh-var(--app-header-height))] flex-col px-5 pt-7 pb-5"
    aria-labelledby="account-connection-title"
  >
    <header>
      <h1
        id="account-connection-title"
        class="text-[22px] leading-tight font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]"
      >
        계좌를 연결해 볼까요?                                                                                                                              
      </h1>
      <p class="mt-2 text-sm leading-6 text-[var(--color-text-secondary)]">
        {{ isPhoneVerified ? '내게 맞는 방법으로 계좌를 연결해주세요.' : '먼저 휴대폰 인증으로 본인 확인을 진행해주세요.' }}
      </p>
    </header>

    <div class="account-auth-step mt-3 flex flex-1 flex-col">
        <div
          class="account-auth-card rounded-[24px] p-4"
          :class="isPhoneVerified ? 'bg-[#f0faf5]' : 'bg-[#f8f9fa]'"
        >
          <div class="flex items-start gap-3">
            <span
              class="grid shrink-0 place-items-center rounded-[20px] bg-white shadow-[0_2px_5px_rgba(31,52,62,0.1)] transition-all duration-300"
              :class="isPhoneVerified ? 'size-11 rounded-[16px] text-[#27966d]' : 'size-11 rounded-[16px] text-[var(--color-selected-text)]'"
            >
              <CheckCircle2 v-if="isPhoneVerified" :size="27" :stroke-width="2.3" />
              <Smartphone v-else :size="25" :stroke-width="2.1" />
            </span>
            <div class="pt-0.5">
              <h2
                class="text-[17px] leading-tight font-bold tracking-[-0.04em]"
              >
                {{ isPhoneVerified ? '인증이 완료되었어요' : '휴대폰 인증' }}
              </h2>
              <p
                class="mt-1.5 text-[11px] leading-5 text-[var(--color-text-secondary)]"
              >
                {{ isPhoneVerified ? '계좌 연결 방법을 선택해주세요.' : '안전한 계좌 연결을 위해 본인 확인이 필요해요.' }}
              </p>
            </div>
          </div>

          <Transition name="account-auth-details">
          <div v-if="!isPhoneVerified" class="account-auth-details">
            <label class="mt-5 block text-[14px] font-extrabold" for="account-phone">휴대폰 번호</label>
            <div class="mt-2.5 flex flex-col gap-2">
            <input
              id="account-phone"
              class="h-14 w-full rounded-[15px] border border-[#dce7ed] bg-white px-4 text-[16px] outline-none focus:border-[var(--color-brand-primary)] focus:ring-2 focus:ring-[#dff4fc]"
              :value="phone"
              type="tel"
              inputmode="numeric"
              autocomplete="tel"
              placeholder="010-0000-0000"
              @input="formatPhone"
            />
            <button
              class="h-12 w-full rounded-[15px] bg-[var(--color-brand-primary)] text-[14px] font-bold text-white disabled:cursor-not-allowed disabled:opacity-45"
              type="button"
              :disabled="!canSendCode || isSendingCode"
              @click="sendVerificationCode"
            >{{ isSendingCode ? '전송 중...' : isCodeSent ? '인증번호 재전송' : '인증번호 받기' }}</button>
            </div>

            <Transition name="account-reveal">
              <div v-if="isCodeSent" key="verification" class="mt-4">
              <label class="block text-xs font-bold" for="account-verification-code">인증번호</label>
              <div class="mt-2 flex h-12 gap-2">
                <div class="relative min-w-0 flex-1">
                  <input
                    id="account-verification-code"
                    :value="verificationCode"
                    class="h-full w-full rounded-xl border border-[#dce7ed] bg-white px-3 pr-14 text-sm outline-none focus:border-[var(--color-brand-primary)] focus:ring-2 focus:ring-[#dff4fc]"
                    type="text"
                    inputmode="numeric"
                    maxlength="6"
                    placeholder="6자리 입력"
                    @input="updateVerificationCode"
                  />
                  <span
                    class="pointer-events-none absolute top-1/2 right-3 -translate-y-1/2 text-[11px] font-bold tabular-nums"
                    :class="remainingSeconds > 0 ? 'text-[#ef5b5b]' : 'text-[#a1a9b4]'"
                  >
                    {{ timerText }}
                  </span>
                </div>
                <button
                  class="w-[92px] shrink-0 rounded-xl border border-[#b9e2f5] bg-[#edf9ff] text-xs font-bold text-[var(--color-selected-text)] disabled:cursor-not-allowed disabled:opacity-45"
                  type="button"
                  :disabled="!canVerify || isVerifyingCode"
                  @click="verifyPhone"
                >{{ isVerifyingCode ? '확인 중' : '인증 확인' }}</button>
              </div>
              <p v-if="verificationError" class="mt-2 text-[11px] font-semibold text-[var(--color-danger)]">{{ verificationError }}</p>
              <p
                v-else-if="remainingSeconds === 0"
                class="mt-2 text-[11px] font-semibold text-[var(--color-danger)]"
              >
                인증 시간이 만료되었어요. 인증번호를 재전송해 주세요.
              </p>
              </div>
            </Transition>
          </div>
          </Transition>
        </div>

      <div v-if="isPhoneVerified" class="mt-3 flex min-h-0 flex-1 flex-col gap-3.5">
      <button
        class="account-method-card account-method-card--import connection-option connection-option--primary group flex min-h-[180px] flex-1 w-full flex-col items-stretch justify-between overflow-hidden rounded-[24px] border border-[#cfe8f4] bg-white p-5 text-left"
        type="button"
        @click="$emit('import')"
      >
        <span class="flex items-start justify-between gap-4">
          <span class="inline-flex h-7 items-center rounded-full bg-[#e9f8ff] px-3 text-[12px] font-bold text-[var(--color-selected-text)]">
            가장 간편해요
          </span>
          <span class="grid size-14 shrink-0 place-items-center rounded-[18px] bg-[#eef9fe] text-[var(--color-selected-text)]">
            <Landmark :size="28" :stroke-width="2.1" aria-hidden="true" />
          </span>
        </span>

        <span class="my-3 block min-w-0">
          <strong class="block text-[21px] font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]">계좌 정보 불러오기</strong>
          <span class="mt-2 block break-keep text-sm leading-6 text-[var(--color-text-secondary)]">
            이미 가지고 있는 KB국민은행 계좌를<br />안전하고 빠르게 연결해요.
          </span>
        </span>

        <span class="flex h-11 items-center justify-between rounded-[14px] bg-[#f0faff] px-4 text-sm font-bold text-[var(--color-selected-text)]">
          내 계좌 확인하기
          <ChevronRight class="transition-transform group-active:translate-x-1" :size="20" :stroke-width="2.4" aria-hidden="true" />
        </span>
      </button>

      <button
        class="account-method-card account-method-card--create connection-option group flex min-h-[180px] flex-1 w-full flex-col items-stretch justify-between overflow-hidden rounded-[24px] border border-[#f0e5c5] bg-white p-5 text-left"
        type="button"
        @click="$emit('create')"
      >
        <span class="flex items-start justify-between gap-4">
          <span class="inline-flex h-7 items-center rounded-full bg-[#fff7df] px-3 text-[12px] font-bold text-[#b68017]">
            계좌가 없다면
          </span>
          <span class="grid size-14 shrink-0 place-items-center rounded-[18px] bg-[#fff7df] text-[#e7a52c]">
            <Plus :size="29" :stroke-width="2.4" aria-hidden="true" />
          </span>
        </span>

        <span class="my-3 block min-w-0">
          <strong class="block text-[21px] font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]">계좌 개설하기</strong>
          <span class="mt-2 block break-keep text-sm leading-6 text-[var(--color-text-secondary)]">
            아이의 자산 관리를 시작할<br />KB국민은행 계좌를 새로 만들어요.
          </span>
        </span>

        <span class="flex h-11 items-center justify-between rounded-[14px] bg-[#fff9e9] px-4 text-sm font-bold text-[#9f7418]">
          새 계좌 만들기
          <ChevronRight class="text-[#b69244] transition-transform group-active:translate-x-1" :size="20" :stroke-width="2.4" aria-hidden="true" />
        </span>
      </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.connection-option {
  box-shadow: 0 5px 18px rgb(43 83 105 / 3%);
  transition: border-color 150ms ease, box-shadow 150ms ease, transform 150ms ease;
}

.account-auth-card {
  transition:
    padding 400ms cubic-bezier(0.22, 1, 0.36, 1),
    background-color 400ms ease;
}

.account-auth-details-enter-active,
.account-auth-details-leave-active {
  overflow: hidden;
  transition:
    max-height 400ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 260ms ease,
    transform 360ms cubic-bezier(0.22, 1, 0.36, 1);
}

.account-auth-details-enter-from,
.account-auth-details-leave-to {
  max-height: 0;
  opacity: 0;
  transform: translateY(-8px);
}

.account-auth-details-enter-to,
.account-auth-details-leave-from {
  max-height: 360px;
  opacity: 1;
  transform: translateY(0);
}

.account-auth-complete-content {
  animation: account-auth-complete-rise 360ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

.connection-option--primary {
  box-shadow: 0 5px 18px rgb(43 83 105 / 3%);
}

.connection-option:active {
  border-color: rgb(45 169 232 / 32%);
  box-shadow: 0 3px 12px rgb(43 83 105 / 6%);
  transform: scale(0.985);
}

.connection-option:focus-visible {
  outline: 3px solid rgb(45 169 232 / 20%);
  outline-offset: 2px;
}

.connection-step-enter-active,
.connection-step-leave-active {
  transition: opacity 220ms ease, transform 260ms cubic-bezier(0.22, 1, 0.36, 1);
}

.connection-step-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.connection-step-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.account-method-card {
  animation: account-method-rise 520ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

.account-auth-complete {
  animation: account-auth-complete-rise 420ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

.account-method-card--create {
  animation-delay: 140ms;
}

@keyframes account-method-rise {
  from {
    opacity: 0;
    transform: translateY(24px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes account-auth-complete-rise {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.98);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.account-reveal-enter-active,
.account-reveal-leave-active {
  transition: opacity 260ms ease, transform 320ms cubic-bezier(0.22, 1, 0.36, 1);
}

.account-reveal-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.account-reveal-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

.account-auth-card {
  animation: account-auth-rise 520ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

.account-auth-note {
  animation: account-auth-rise 520ms 100ms cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes account-auth-rise {
  from {
    opacity: 0;
    transform: translateY(14px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .connection-option,
  .connection-option :deep(svg) {
    transition-duration: 1ms;
  }

  .account-auth-card,
  .account-auth-note,
  .account-method-card,
  .account-auth-complete {
    animation-duration: 1ms;
    animation-delay: 0ms;
  }
}
</style>
