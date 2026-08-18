<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Check, ContactRound, FileKey2, Smartphone, X } from 'lucide-vue-next'

import babyImage from '@/assets/images/child/baby.png'
import registrationCompleteImage from '@/assets/images/accounts/registration-complete.png'

type Child = {
  id: number
  name: string
}

const children: Child[] = [
  { id: 1, name: '1번 아이' },
  { id: 2, name: '2번 아이' },
]

const router = useRouter()
const selectedChildId = ref<number | null>(null)
const step = ref<1 | 2 | 3>(1)
const selectedAuthMethod = ref<'kakao' | 'sms' | null>(null)
const isAuthDialogOpen = ref(false)
const isAuthenticated = ref(false)
const phoneNumber = ref('')
const verificationCode = ref('')
const isVerificationCodeSent = ref(false)
const isVerificationCodeConfirmed = ref(false)
const hasVerificationError = ref(false)
const remainingSeconds = ref(180)
let timerId: ReturnType<typeof setInterval> | null = null

const selectedChildName = computed(
  () => children.find(({ id }) => id === selectedChildId.value)?.name ?? '아이',
)
const normalizedPhoneNumber = computed(() => phoneNumber.value.replace(/\D/g, ''))
const canRequestVerificationCode = computed(() => /^01\d{9}$/.test(normalizedPhoneNumber.value))
const canConfirmVerificationCode = computed(
  () =>
    isVerificationCodeSent.value &&
    /^\d{6}$/.test(verificationCode.value) &&
    remainingSeconds.value > 0,
)
const timerText = computed(() => {
  const minutes = Math.floor(remainingSeconds.value / 60)
  const seconds = remainingSeconds.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})
const canCompleteAuthentication = computed(
  () => selectedAuthMethod.value === 'kakao' || isVerificationCodeConfirmed.value,
)

const selectChild = (childId: number) => {
  selectedChildId.value = childId
}

const goNext = () => {
  if (selectedChildId.value === null) return

  step.value = 2
}

const selectAuthMethod = (method: 'kakao' | 'sms') => {
  selectedAuthMethod.value = method
  isAuthenticated.value = false
  if (method === 'sms') {
    verificationCode.value = ''
    isVerificationCodeSent.value = false
    isVerificationCodeConfirmed.value = false
    hasVerificationError.value = false
    remainingSeconds.value = 180
  }
  isAuthDialogOpen.value = true
}

const stopVerificationTimer = () => {
  if (timerId === null) return
  clearInterval(timerId)
  timerId = null
}

const updatePhoneNumber = (event: Event) => {
  const digits = (event.target as HTMLInputElement).value.replace(/\D/g, '').slice(0, 11)

  if (digits.length <= 3) {
    phoneNumber.value = digits
  } else if (digits.length <= 7) {
    phoneNumber.value = `${digits.slice(0, 3)}-${digits.slice(3)}`
  } else {
    phoneNumber.value = `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`
  }

  stopVerificationTimer()
  verificationCode.value = ''
  isVerificationCodeSent.value = false
  isVerificationCodeConfirmed.value = false
  hasVerificationError.value = false
  remainingSeconds.value = 180
}

const requestVerificationCode = () => {
  if (!canRequestVerificationCode.value) return

  stopVerificationTimer()
  verificationCode.value = ''
  isVerificationCodeSent.value = true
  isVerificationCodeConfirmed.value = false
  hasVerificationError.value = false
  remainingSeconds.value = 180
  timerId = setInterval(() => {
    if (remainingSeconds.value <= 1) {
      remainingSeconds.value = 0
      stopVerificationTimer()
      return
    }
    remainingSeconds.value -= 1
  }, 1000)
}

const confirmVerificationCode = () => {
  if (!canConfirmVerificationCode.value) return

  if (verificationCode.value !== '123456') {
    hasVerificationError.value = true
    isVerificationCodeConfirmed.value = false
    return
  }

  hasVerificationError.value = false
  isVerificationCodeConfirmed.value = true
  stopVerificationTimer()
}

const updateVerificationCode = (event: Event) => {
  verificationCode.value = (event.target as HTMLInputElement).value.replace(/\D/g, '').slice(0, 6)
  hasVerificationError.value = false
}

const completeAuthentication = () => {
  if (selectedAuthMethod.value === null || !canCompleteAuthentication.value) return

  // 실제 카카오페이/SMS 본인 인증 API는 후속 연동 시 이 위치에서 호출합니다.
  isAuthenticated.value = true
  isAuthDialogOpen.value = false
}

const continueAfterAuthentication = () => {
  if (!isAuthenticated.value) return

  step.value = 3
}

onBeforeUnmount(stopVerificationTimer)
</script>

<template>
  <main
    class="flex min-h-[calc(100dvh-var(--app-header-height)-env(safe-area-inset-top))] flex-col bg-white text-[var(--color-text-primary)]"
  >
    <section class="flex flex-1 flex-col overflow-x-hidden px-6 pt-5 pb-[max(32px,env(safe-area-inset-bottom))]">
      <div v-if="step !== 3" class="grid grid-cols-2 gap-2" aria-label="아이 계좌 만들기 진행 단계">
        <span
          class="h-[3px] rounded-full"
          :class="step === 1 ? 'bg-[var(--color-brand-primary)]' : 'bg-[var(--color-border)]'"
          :aria-current="step === 1 ? 'step' : undefined"
        ></span>
        <span
          class="h-[3px] rounded-full"
          :class="step === 2 ? 'bg-[var(--color-brand-primary)]' : 'bg-[var(--color-border)]'"
          :aria-current="step === 2 ? 'step' : undefined"
        ></span>
      </div>

      <Transition name="child-step-slide" mode="out-in">
        <div :key="step" class="flex flex-1 flex-col">
      <template v-if="step === 1">
        <h1 class="mt-8 text-[27px] leading-[1.18] font-bold tracking-[-0.035em]">
          어떤 자녀의<br />
          계좌를 만들까요?
        </h1>

        <form class="mt-9 flex flex-1 flex-col" @submit.prevent="goNext">
          <fieldset>
            <legend class="sr-only">계좌를 만들 자녀 선택</legend>
            <div class="grid gap-4">
              <button
                v-for="child in children"
                :key="child.id"
                class="flex min-h-[72px] w-full items-center rounded-2xl border px-4 text-left transition-colors duration-200"
                :class="
                  selectedChildId === child.id
                    ? 'border-[var(--color-brand-primary)] bg-[#eaf8ff] ring-1 ring-[var(--color-brand-primary)]'
                    : 'border-[var(--color-border)] bg-white  active:bg-[var(--color-surface-muted)]'
                "
                type="button"
                :aria-pressed="selectedChildId === child.id"
                @click="selectChild(child.id)"
              >
                <span
                  class="mr-4 grid size-12 shrink-0 place-items-center overflow-hidden rounded-full bg-[#e2f6ff]"
                  aria-hidden="true"
                >
                  <img class="h-[32px] w-[49px] object-contain" :src="babyImage" alt="" />
                </span>
                <span class="text-lg font-medium tracking-[-0.02em]">{{ child.name }}</span>
              </button>
            </div>
          </fieldset>

          <button
            class="mt-auto min-h-[58px] w-full rounded-2xl bg-[var(--color-brand-primary)] text-base font-bold text-white transition-colors duration-200 active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[#cbd8df] disabled:hover:bg-[#cbd8df]"
            type="submit"
            :disabled="selectedChildId === null"
          >
            다음
          </button>
        </form>
      </template>

      <template v-else-if="step === 2">
        <div class="mt-8">
          <h1 class="text-[27px] leading-[1.18] font-bold tracking-[-0.035em]">
            법정 대리인 확인이<br />
            필요해요
          </h1>
          <p class="mt-3 text-sm leading-relaxed text-[var(--color-text-secondary)]">
            서류 제출 없이 간편인증서로 보호자님의<br />
            법정대리인이 맞는지 확인할게요.
          </p>
        </div>

        <aside
          class="mt-5 flex min-h-[110px] items-start gap-3 rounded-2xl border border-[var(--color-border)] bg-white p-4"
          aria-label="가족관계 확인 안내"
        >
          <ContactRound :size="27" :stroke-width="1.8" class="mt-0.5 shrink-0 text-black" />
          <div>
            <strong class="text-sm font-bold">가족관계 자동 확인</strong>
            <p class="mt-2 text-xs leading-relaxed text-[var(--color-text-secondary)]">
              전자가족관계등록시스템에서 자동 조회돼요. 별도 서류를 촬영해 올릴 필요가 없어요.
            </p>
          </div>
        </aside>

        <form class="mt-6 flex flex-1 flex-col" @submit.prevent="continueAfterAuthentication">
          <fieldset>
            <legend class="mb-4 text-sm text-[var(--color-text-secondary)]">
              인증수단을 선택해주세요
            </legend>
            <div class="grid gap-4">
              <button
                class="flex min-h-[66px] w-full items-center rounded-2xl border px-4 text-left transition-colors duration-200"
                :class="
                  selectedAuthMethod === 'kakao'
                    ? 'border-[var(--color-brand-primary)] bg-[#eaf8ff] ring-1 ring-[var(--color-brand-primary)] hover:bg-[#dcf3ff]'
                    : 'border-[var(--color-border)] bg-white hover:border-[#9ddcf7] hover:bg-[#f4fbff]'
                "
                type="button"
                :aria-pressed="selectedAuthMethod === 'kakao'"
                @click="selectAuthMethod('kakao')"
              >
                <FileKey2 :size="25" :stroke-width="1.9" class="mr-4 shrink-0" />
                <span class="text-base font-medium">카카오페이 인증서</span>
                <span
                  v-if="isAuthenticated && selectedAuthMethod === 'kakao'"
                  class="ml-auto grid size-7 shrink-0 place-items-center rounded-full bg-[var(--color-brand-primary)] text-white"
                  aria-label="인증 완료"
                >
                  <Check :size="17" :stroke-width="3" aria-hidden="true" />
                </span>
              </button>

              <button
                class="flex min-h-[66px] w-full items-center rounded-2xl border px-4 text-left transition-colors duration-200"
                :class="
                  selectedAuthMethod === 'sms'
                    ? 'border-[var(--color-brand-primary)] bg-[#eaf8ff] ring-1 ring-[var(--color-brand-primary)] hover:bg-[#dcf3ff]'
                    : 'border-[var(--color-border)] bg-white hover:border-[#9ddcf7] hover:bg-[#f4fbff]'
                "
                type="button"
                :aria-pressed="selectedAuthMethod === 'sms'"
                @click="selectAuthMethod('sms')"
              >
                <Smartphone :size="25" :stroke-width="1.9" class="mr-4 shrink-0" />
                <span class="text-base font-medium">SMS 휴대폰 인증</span>
                <span
                  v-if="isAuthenticated && selectedAuthMethod === 'sms'"
                  class="ml-auto grid size-7 shrink-0 place-items-center rounded-full bg-[var(--color-brand-primary)] text-white"
                  aria-label="인증 완료"
                >
                  <Check :size="17" :stroke-width="3" aria-hidden="true" />
                </span>
              </button>
            </div>
          </fieldset>

          <button
            class="mt-auto min-h-[58px] w-full rounded-2xl bg-[var(--color-brand-primary)] text-base font-bold text-white transition-colors duration-200 hover:bg-[var(--color-brand-primary-pressed)] active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[#cbd8df] disabled:hover:bg-[#cbd8df]"
            type="submit"
            :disabled="!isAuthenticated"
          >
            {{ isAuthenticated ? '계속하기' : '인증을 완료해주세요' }}
          </button>
        </form>
      </template>

      <template v-else>
        <div class="flex flex-1 flex-col items-center text-center">
          <img
            class="mt-[14dvh] aspect-square w-full max-w-[330px] object-contain"
            :src="registrationCompleteImage"
            alt="아이 통장 개설 완료"
          />

          <h1 class="mt-4 text-[27px] font-bold tracking-[-0.035em]">우리아이통장 개설 완료</h1>
          <p class="mt-2 text-base text-[var(--color-text-secondary)]">
            {{ selectedChildName }} 님의 통장이 만들어졌어요!
          </p>

          <div class="mt-auto grid w-full grid-cols-2 gap-4">
            <button
              class="min-h-[58px] rounded-2xl border border-[var(--color-border)] bg-white text-base font-bold text-[var(--color-brand-primary)] transition-colors hover:bg-[#f4fbff]"
              type="button"
              @click="router.push({ name: 'Home' })"
            >
              홈으로 가기
            </button>
            <button
              class="min-h-[58px] rounded-2xl bg-[var(--color-brand-primary)] text-base font-bold text-white transition-colors hover:bg-[var(--color-brand-primary-pressed)]"
              type="button"
              @click="router.push({ name: 'ParentPermissions' })"
            >
              권한 설정하기
            </button>
          </div>
        </div>
      </template>
        </div>
      </Transition>
    </section>

    <Transition name="auth-sheet">
      <div
        v-if="isAuthDialogOpen"
        class="auth-sheet-backdrop fixed inset-0 left-1/2 z-50 flex w-full max-w-[var(--app-max-width)] -translate-x-1/2 items-end bg-black/40"
        role="presentation"
        @click.self="isAuthDialogOpen = false"
      >
        <section
          class="auth-sheet-panel w-full rounded-t-[30px] bg-white px-6 pt-4 pb-[max(28px,env(safe-area-inset-bottom))] shadow-[0_-10px_35px_rgba(0,0,0,0.12)]"
          role="dialog"
          aria-modal="true"
          aria-labelledby="auth-dialog-title"
        >
          <div class="mx-auto mb-2 h-1.5 w-11 rounded-full bg-[#d7dce1]" aria-hidden="true"></div>
          <div class="flex items-center justify-between">
            <h2 id="auth-dialog-title" class="text-xl font-bold">
              {{ selectedAuthMethod === 'kakao' ? '카카오페이 인증' : 'SMS 휴대폰 인증' }}
            </h2>
            <button
              class="grid size-10 place-items-center rounded-full text-[var(--color-text-secondary)] hover:bg-[var(--color-surface-muted)]"
              type="button"
              aria-label="인증창 닫기"
              @click="isAuthDialogOpen = false"
            >
              <X :size="22" />
            </button>
          </div>

          <template v-if="selectedAuthMethod === 'kakao'">
            <div class="mt-5 rounded-2xl bg-[#fff7d6] px-5 py-5">
              <strong class="text-base">카카오톡으로 인증 요청을 보낼게요</strong>
              <p class="mt-2 text-sm leading-relaxed text-[var(--color-text-secondary)]">
                카카오톡에서 인증을 완료한 뒤 아래 버튼을 눌러주세요.
              </p>
            </div>
          </template>
          <template v-else>
            <label class="mt-5 block">
              <span class="mb-2 block text-sm font-bold">휴대폰 번호</span>
              <span
                class="flex h-14 w-full items-center rounded-xl border border-[var(--color-border)] px-2 pl-4 focus-within:border-[var(--color-brand-primary)]"
              >
                <input
                  class="min-w-0 flex-1 bg-transparent text-base outline-none"
                  type="tel"
                  inputmode="numeric"
                  maxlength="13"
                  :value="phoneNumber"
                  placeholder="010-0000-0000"
                  aria-label="휴대폰 번호"
                  @input="updatePhoneNumber"
                />
                <button
                  class="ml-2 shrink-0 rounded-lg bg-[var(--color-selected-background)] px-3 py-2 text-xs font-bold text-[var(--color-selected-text)] transition-colors hover:bg-[#d8f2ff] disabled:cursor-not-allowed disabled:bg-[#edf0f2] disabled:text-[#a1a9b4]"
                  type="button"
                  :disabled="!canRequestVerificationCode"
                  @click="requestVerificationCode"
                >
                  {{ isVerificationCodeSent ? '재전송' : '인증번호 받기' }}
                </button>
              </span>
            </label>
            <label class="mt-4 block">
              <span class="mb-2 block text-sm font-bold">인증번호</span>
              <span
                class="flex h-14 w-full items-center rounded-xl border px-2 pl-4 focus-within:border-[var(--color-brand-primary)]"
                :class="
                  hasVerificationError
                    ? 'border-[#ef5b5b] bg-[#fff5f5] focus-within:!border-[#ef5b5b]'
                    : isVerificationCodeConfirmed
                      ? 'border-[#45b878] bg-[#f3fff8]'
                      : 'border-[var(--color-border)]'
                "
              >
                <input
                  v-model="verificationCode"
                  class="min-w-0 flex-1 bg-transparent text-base outline-none placeholder:text-[#a1a9b4] disabled:text-[#6f7884]"
                  inputmode="numeric"
                  maxlength="6"
                  placeholder="인증번호 6자리"
                  aria-label="인증번호"
                  :disabled="!isVerificationCodeSent || isVerificationCodeConfirmed"
                  @input="updateVerificationCode"
                />
                <span
                  v-if="
                    isVerificationCodeSent && !isVerificationCodeConfirmed && !hasVerificationError
                  "
                  class="mr-2 shrink-0 text-xs font-medium"
                  :class="remainingSeconds > 0 ? 'text-[#f05d5d]' : 'text-[#a1a9b4]'"
                >
                  {{ timerText }}
                </span>
                <button
                  class="shrink-0 rounded-lg bg-[var(--color-selected-background)] px-3 py-2 text-xs font-bold text-[var(--color-selected-text)] transition-colors hover:bg-[#d8f2ff] disabled:cursor-not-allowed disabled:bg-[#edf0f2] disabled:text-[#a1a9b4]"
                  :class="
                    hasVerificationError ? '!bg-[#eceff1] !text-[#8d969f] hover:!bg-[#eceff1]' : ''
                  "
                  type="button"
                  :disabled="!canConfirmVerificationCode || isVerificationCodeConfirmed"
                  @click="confirmVerificationCode"
                >
                  {{ isVerificationCodeConfirmed ? '확인됨' : '확인' }}
                </button>
              </span>
              <span
                v-if="isVerificationCodeConfirmed"
                class="mt-2 block text-xs font-medium text-[#32a66a]"
              >
                휴대폰 인증이 완료되었어요.
              </span>
              <span
                v-else-if="hasVerificationError"
                class="mt-2 block text-xs font-medium text-[#e54d4d]"
              >
                인증번호가 일치하지 않아요. 인증번호를 다시 받아주세요.
              </span>
              <span
                v-else-if="isVerificationCodeSent && remainingSeconds === 0"
                class="mt-2 block text-xs text-[#f05d5d]"
              >
                인증 시간이 만료되었어요. 인증번호를 다시 받아주세요.
              </span>
            </label>
          </template>

          <button
            class="mt-6 min-h-[56px] w-full rounded-2xl bg-[var(--color-brand-primary)] text-base font-bold text-white transition-colors hover:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:!bg-[#dfe4e8] disabled:!text-[#9aa3ad] disabled:hover:!bg-[#dfe4e8]"
            type="button"
            :disabled="!canCompleteAuthentication"
            @click="completeAuthentication"
          >
            인증 완료하기
          </button>
        </section>
      </div>
    </Transition>
  </main>
</template>

<style scoped>
.child-step-slide-enter-active,
.child-step-slide-leave-active {
  transition: transform 150ms cubic-bezier(0.25, 0.8, 0.25, 1), opacity 120ms ease-out;
}

.child-step-slide-enter-from {
  transform: translateX(18px);
  opacity: 0;
}

.child-step-slide-leave-to {
  transform: translateX(-18px);
  opacity: 0;
}

.auth-sheet-enter-active,
.auth-sheet-leave-active {
  transition: background-color 240ms ease;
}

.auth-sheet-enter-active .auth-sheet-panel {
  transition: transform 360ms cubic-bezier(0.22, 1, 0.36, 1);
}

.auth-sheet-leave-active .auth-sheet-panel {
  transition: transform 240ms cubic-bezier(0.4, 0, 1, 1);
}

.auth-sheet-enter-from,
.auth-sheet-leave-to {
  background-color: rgb(0 0 0 / 0%);
}

.auth-sheet-enter-from .auth-sheet-panel,
.auth-sheet-leave-to .auth-sheet-panel {
  transform: translateY(100%);
}

@media (prefers-reduced-motion: reduce) {
  .child-step-slide-enter-active,
  .child-step-slide-leave-active,
  .auth-sheet-enter-active,
  .auth-sheet-leave-active,
  .auth-sheet-enter-active .auth-sheet-panel,
  .auth-sheet-leave-active .auth-sheet-panel {
    transition-duration: 1ms;
  }
}
</style>
