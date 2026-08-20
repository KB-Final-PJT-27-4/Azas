<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Check, ContactRound, Smartphone, X } from 'lucide-vue-next'

import babyImage from '@/assets/images/child/baby.png'
import completePigUrl from '@/assets/images/accounts/complete-pig.png'
import { api, getApiErrorMessage } from '@/api'
import { useToast } from '@/composables/useToast'

type Child = {
  id: number
  name: string
}

const children = ref<Child[]>([])

const router = useRouter()
const { showToast } = useToast()
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
const authSheetTouchStartY = ref<number | null>(null)
const authSheetDragOffset = ref(0)
const isAuthSheetDragging = ref(false)
let timerId: ReturnType<typeof setInterval> | null = null
const verificationId = ref<number | null>(null)

const selectedChildName = computed(
  () => children.value.find(({ id }) => id === selectedChildId.value)?.name ?? '아이',
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

const startAuthSheetDrag = (event: TouchEvent) => {
  authSheetTouchStartY.value = event.touches[0]?.clientY ?? null
  authSheetDragOffset.value = 0
  isAuthSheetDragging.value = true
}

const moveAuthSheetDrag = (event: TouchEvent) => {
  if (authSheetTouchStartY.value === null) return
  const currentY = event.touches[0]?.clientY ?? authSheetTouchStartY.value
  authSheetDragOffset.value = Math.max(0, currentY - authSheetTouchStartY.value)
}

const endAuthSheetDrag = () => {
  if (authSheetDragOffset.value > 80) isAuthDialogOpen.value = false
  authSheetTouchStartY.value = null
  authSheetDragOffset.value = 0
  isAuthSheetDragging.value = false
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

const requestVerificationCode = async () => {
  if (!canRequestVerificationCode.value) return
  try {
    const { data } = await api.sendVerificationCodeUsingPOST({ phone_number: normalizedPhoneNumber.value })
    verificationId.value = data.verification_id
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
  } catch (error) {
    showToast(getApiErrorMessage(error, '인증번호를 보내지 못했습니다.'), 'error')
  }
}

const confirmVerificationCode = async () => {
  if (!canConfirmVerificationCode.value) return
  if (!verificationId.value) return
  try {
    await api.confirmVerificationCodeUsingPOST(verificationId.value, { verification_code: verificationCode.value })
    hasVerificationError.value = false
    isVerificationCodeConfirmed.value = true
    stopVerificationTimer()
  } catch {
    hasVerificationError.value = true
    isVerificationCodeConfirmed.value = false
  }
}

const updateVerificationCode = (event: Event) => {
  verificationCode.value = (event.target as HTMLInputElement).value.replace(/\D/g, '').slice(0, 6)
  hasVerificationError.value = false
}

const completeAuthentication = () => {
  if (selectedAuthMethod.value === null || !canCompleteAuthentication.value) return
  if (selectedAuthMethod.value === 'kakao') {
    showToast('현재 서버는 카카오페이 본인 인증 API를 제공하지 않습니다.', 'error')
    return
  }
  isAuthenticated.value = true
  isAuthDialogOpen.value = false
}

const continueAfterAuthentication = async () => {
  if (!isAuthenticated.value) return
  if (!selectedChildId.value) return
  try {
    type ProductItem = { financial_product_id?: number; product_type?: string }
    const { data } = await api.getProductsUsingGET(undefined, undefined, 'DEMAND_DEPOSIT', 20)
    const product = ((data.items ?? []) as unknown as ProductItem[])[0]
    if (!product?.financial_product_id) throw new Error('개설 가능한 입출금 상품이 없습니다.')
    await api.openUsingPOST(undefined, {
      child_id: selectedChildId.value,
      financial_product_id: product.financial_product_id,
      initial_deposit_amount: 0,
      owner_type: 'CHILD',
    })
    step.value = 3
  } catch (error) {
    showToast(getApiErrorMessage(error, '아이 통장을 개설하지 못했습니다.'), 'error')
  }
}

onMounted(async () => {
  try {
    const { data } = await api.getChildrenUsingGET()
    children.value = (data.items ?? []).map((child) => ({ id: child.child_id ?? 0, name: child.name ?? '아이' }))
  } catch (error) {
    showToast(getApiErrorMessage(error, '자녀 목록을 불러오지 못했습니다.'), 'error')
  }
})

onBeforeUnmount(stopVerificationTimer)
</script>

<template>
  <main
    class="flex min-h-[calc(100dvh-var(--app-header-height)-env(safe-area-inset-top))] flex-col bg-white text-[var(--color-text-primary)]"
  >
    <section class="flex flex-1 flex-col overflow-x-hidden px-5 pt-5 pb-[max(32px,env(safe-area-inset-bottom))]">
      <div v-if="step !== 3" class="grid grid-cols-2 gap-2" aria-label="아이 계좌 만들기 진행 단계">
        <span
          class="child-progress-step h-1 rounded-full"
          :class="step === 1 ? 'bg-[var(--color-brand-primary)]' : 'bg-[var(--color-border)]'"
          :aria-current="step === 1 ? 'step' : undefined"
        ></span>
        <span
          class="child-progress-step h-1 rounded-full"
          :class="step === 2 ? 'bg-[var(--color-brand-primary)]' : 'bg-[var(--color-border)]'"
          :aria-current="step === 2 ? 'step' : undefined"
        ></span>
      </div>

      <Transition name="child-step-slide" mode="out-in">
        <div :key="step" class="flex flex-1 flex-col">
      <template v-if="step === 1">
        <h1 class="mt-8 text-[26px] leading-[1.2] font-extrabold tracking-[-0.04em]">
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
                class="child-option flex min-h-[72px] w-full items-center rounded-[18px] border px-4 text-left"
                :class="
                  selectedChildId === child.id
                    ? 'border-[var(--color-brand-primary)] bg-[#f1faff] ring-1 ring-[var(--color-brand-primary)]'
                    : 'border-[var(--color-border)] bg-white active:bg-[var(--color-surface-muted)]'
                "
                type="button"
                :aria-pressed="selectedChildId === child.id"
                @click="selectChild(child.id)"
              >
                <span
                  class="mr-4 grid size-12 shrink-0 place-items-center overflow-hidden rounded-full bg-[#eef7fa]"
                  aria-hidden="true"
                >
                  <img class="h-[32px] w-[49px] object-contain" :src="babyImage" alt="" />
                </span>
                <span class="text-lg font-medium tracking-[-0.02em]">{{ child.name }}</span>
                <span
                  v-if="selectedChildId === child.id"
                  class="ml-auto grid size-6 shrink-0 place-items-center rounded-full bg-[var(--color-brand-primary)] text-white"
                  aria-label="선택됨"
                >
                  <Check :size="14" :stroke-width="3" aria-hidden="true" />
                </span>
              </button>
            </div>
          </fieldset>

          <button
            class="primary-action mt-auto min-h-[56px] w-full rounded-[16px] bg-[var(--color-brand-primary)] text-base font-bold text-white disabled:cursor-not-allowed disabled:bg-[#cbd8df]"
            type="submit"
            :disabled="selectedChildId === null"
          >
            다음
          </button>
        </form>
      </template>

      <template v-else-if="step === 2">
        <div class="mt-8">
          <h1 class="text-[26px] leading-[1.2] font-extrabold tracking-[-0.04em]">
            법정 대리인 확인이<br />
            필요해요
          </h1>
          <p class="mt-3 text-sm leading-relaxed text-[var(--color-text-secondary)]">
            서류 제출 없이 간편인증서로 보호자님의<br />
            법정대리인이 맞는지 확인할게요.
          </p>
        </div>

        <aside
          class="mt-5 flex min-h-[110px] items-start gap-3 rounded-[20px] border-0 bg-[#f8f9fa] p-4"
          aria-label="가족관계 확인 안내"
        >
          <span class="grid size-10 shrink-0 place-items-center rounded-xl bg-white text-[var(--color-selected-text)] shadow-sm">
            <ContactRound :size="22" :stroke-width="1.9" />
          </span>
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
            <div class="grid gap-3">
          
              <button
                class="auth-method flex min-h-[66px] w-full items-center rounded-[18px] border px-4 text-left"
                :class="
                  selectedAuthMethod === 'sms'
                    ? 'border-[var(--color-brand-primary)] bg-[#f1faff] ring-1 ring-[var(--color-brand-primary)]'
                    : 'border-[var(--color-border)] bg-white'
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
            class="primary-action mt-auto min-h-[56px] w-full rounded-[16px] bg-[var(--color-brand-primary)] text-base font-bold text-white disabled:cursor-not-allowed disabled:bg-[#cbd8df]"
            type="submit"
            :disabled="!isAuthenticated"
          >
            {{ isAuthenticated ? '계속하기' : '인증을 완료해주세요' }}
          </button>
        </form>
      </template>

      <template v-else>
        <div class="flex flex-1 flex-col items-center text-center">
          <div class="child-complete-scene mt-[18dvh]" aria-label="아이 통장 개설 완료">
            <img class="child-complete-scene__pig" :src="completePigUrl" alt="" />

            <span class="child-complete-check" aria-hidden="true">
              <svg width="38" height="38" viewBox="0 0 24 24" fill="none"><path d="M4 12.5L9.2 17.5L20 6.5" /></svg>
            </span>
          </div>

          <h1 class="mt-5 text-[28px] leading-tight font-extrabold tracking-[-0.035em]">우리아이통장 개설 완료</h1>
          <p class="mt-3 text-sm leading-6 text-[var(--color-text-secondary)]">
            {{ selectedChildName }} 님의 통장이 만들어졌어요!
          </p>

          <div class="mt-auto grid w-full grid-cols-2 gap-3">
            <button
              class="min-h-14 rounded-xl border border-[var(--color-border)] bg-white text-base font-bold text-[var(--color-brand-primary)] transition-colors active:bg-[#f4fbff]"
              type="button"
              @click="router.push({ name: 'Home' })"
            >
              홈으로 가기
            </button>
            <button
              class="min-h-14 rounded-xl bg-[var(--color-brand-primary)] text-base font-bold text-white transition-colors active:bg-[var(--color-brand-primary-pressed)]"
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
            class="auth-sheet-panel w-full rounded-t-[28px] bg-white px-5 pt-3 pb-[max(24px,env(safe-area-inset-bottom))] shadow-[0_-12px_36px_rgba(31,52,62,0.14)]"
            :class="{ 'is-dragging': isAuthSheetDragging }"
            :style="isAuthSheetDragging ? { transform: `translateY(${authSheetDragOffset}px)` } : undefined"
            @touchstart="startAuthSheetDrag"
            @touchmove.prevent="moveAuthSheetDrag"
            @touchend="endAuthSheetDrag"
            @touchcancel="endAuthSheetDrag"
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
            <div class="mt-5 rounded-[20px] bg-[#f8f9fa] px-4 py-4">
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
                class="flex h-14 w-full items-center rounded-[15px] border border-[var(--color-border)] bg-[#f8f9fa] px-2 pl-4 focus-within:border-[var(--color-brand-primary)] focus-within:bg-white"
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
                class="flex h-14 w-full items-center rounded-[15px] border bg-[#f8f9fa] px-2 pl-4 focus-within:border-[var(--color-brand-primary)] focus-within:bg-white"
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
.child-option,
.auth-method,
.primary-action {
  transition:
    transform 180ms ease,
    border-color 180ms ease,
    background-color 180ms ease,
    box-shadow 180ms ease;
}

.child-complete-scene {
  position: relative;
  width: 248px;
  height: 168px;
}

.child-complete-scene__pig {
  position: absolute;
  z-index: 2;
  top: 0;
  left: 9px;
  width: 230px;
  height: 168px;
  object-fit: contain;
}

.child-complete-scene__decoration {
  position: absolute;
  z-index: 1;
  object-fit: contain;
}

.child-complete-scene__star { width: 45px; height: 45px; }
.child-complete-scene__star--left { top: 19px; left: -7px; }
.child-complete-scene__star--right { right: -6px; bottom: 19px; width: 40px; height: 40px; }
.child-complete-scene__diamond { width: 25px; height: 25px; }
.child-complete-scene__diamond--left { left: 8px; bottom: 33px; }
.child-complete-scene__diamond--right { top: 65px; right: 13px; width: 22px; height: 22px; }
.child-complete-scene__circle { width: 18px; height: 18px; }
.child-complete-scene__circle--left { top: 75px; left: 21px; }
.child-complete-scene__circle--right { top: 94px; right: -1px; width: 14px; height: 14px; }

.child-complete-check {
  position: absolute;
  z-index: 3;
  top: 2px;
  right: 10px;
  display: grid;
  width: 58px;
  height: 58px;
  color: white;
  background: linear-gradient(155deg, #61c8f5 2%, #2d8dec 82%);
  border-radius: 50%;
  box-shadow: 0 8px 18px rgb(45 141 236 / 24%);
  place-items: center;
}

.child-complete-check svg path {
  fill: none;
  stroke: currentColor;
  stroke-width: 3.4;
  stroke-linecap: round;
  stroke-linejoin: round;
}

@media (prefers-reduced-motion: no-preference) {
  .child-complete-scene__pig {
    animation: child-complete-pig-arrive 680ms cubic-bezier(0.16, 1, 0.3, 1) both;
  }

  .child-complete-scene__decoration {
    animation: child-complete-spark-arrive 620ms cubic-bezier(0.16, 1, 0.3, 1) both;
  }

  .child-complete-scene__star--left { animation-delay: 180ms; }
  .child-complete-scene__star--right { animation-delay: 290ms; }
  .child-complete-scene__diamond--left { animation-delay: 350ms; }
  .child-complete-scene__diamond--right { animation-delay: 430ms; }
  .child-complete-scene__circle--left { animation-delay: 500ms; }
  .child-complete-scene__circle--right { animation-delay: 570ms; }

  .child-complete-check {
    animation: child-complete-check-arrive 620ms cubic-bezier(0.16, 1, 0.3, 1) 260ms both;
  }

  .child-complete-check svg path {
    stroke-dasharray: 1;
    stroke-dashoffset: 1;
    animation: child-complete-check-draw 300ms cubic-bezier(0.25, 0.8, 0.3, 1) 560ms forwards;
  }
}

@keyframes child-complete-pig-arrive { 0% { opacity: 0; transform: translateY(14px) scale(0.78); } 65% { opacity: 1; transform: translateY(-3px) scale(1.035); } 100% { opacity: 1; transform: translateY(0) scale(1); } }
@keyframes child-complete-spark-arrive { 0% { opacity: 0; transform: translate(0, 10px) scale(0.2) rotate(-25deg); } 70% { opacity: 1; transform: translate(0, -2px) scale(1.18) rotate(8deg); } 100% { opacity: 1; transform: translate(0, 0) scale(1) rotate(0); } }
@keyframes child-complete-check-arrive { 0% { opacity: 0; transform: scale(0.35) rotate(-12deg); } 68% { opacity: 1; transform: scale(1.12) rotate(3deg); } 100% { opacity: 1; transform: scale(1) rotate(0); } }
@keyframes child-complete-check-draw { to { stroke-dashoffset: 0; } }

.child-option:active,
.auth-method:active,
.primary-action:active:not(:disabled) {
  transform: scale(0.985);
}

.child-option:focus-visible,
.auth-method:focus-visible,
.primary-action:focus-visible {
  outline: 3px solid rgb(85 192 244 / 22%);
  outline-offset: 2px;
}

.child-step-slide-enter-active,
.child-step-slide-leave-active {
  transition: transform 360ms cubic-bezier(0.22, 1, 0.36, 1), opacity 240ms ease;
}

.child-step-slide-enter-from {
  transform: translateX(18px);
  opacity: 0;
}

.child-step-slide-leave-to {
  transform: translateX(-18px);
  opacity: 0;
}

.child-progress-step {
  transition: background-color 360ms ease, transform 360ms cubic-bezier(0.22, 1, 0.36, 1);
}

.auth-sheet-enter-active,
.auth-sheet-leave-active {
  transition: background-color 240ms ease;
}

.auth-sheet-enter-active .auth-sheet-panel {
  transition: transform 420ms cubic-bezier(0.22, 1, 0.36, 1);
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

.auth-sheet-panel > div:first-child {
  width: 64px;
  height: 6px;
  margin-bottom: 16px;
  border-radius: 999px;
}

.auth-sheet-panel.is-dragging {
  transition: none;
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

  .child-option,
  .auth-method,
  .primary-action {
    transition-duration: 1ms;
  }
}
</style>
