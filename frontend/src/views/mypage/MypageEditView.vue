<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { CheckCircle2 } from 'lucide-vue-next'
import { useRouter } from 'vue-router'

import BaseDatePicker from '@/components/common/BaseDatePicker.vue'
import { useToast } from '@/composables/useToast'

type FamilyRole = '부' | '모' | '보호자'

const router = useRouter()
const { showToast } = useToast()

const name = ref('김하나')
const role = ref<FamilyRole>('부')
const phone = ref('010-1234-5678')
const birthDate = ref('1995-05-18')
const profileImage = ref<string | null>(null)
const phoneVerified = ref(true)
const verificationCodeSent = ref(false)
const verificationCode = ref('')
const verificationError = ref('')
const fileInput = ref<HTMLInputElement | null>(null)
let profileObjectUrl: string | null = null

const canSave = computed(() => name.value.trim().length > 0 && phoneVerified.value)

const formatPhone = (event: Event) => {
  const input = event.target as HTMLInputElement
  const digits = input.value.replace(/\D/g, '').slice(0, 11)
  const formatted =
    digits.length <= 3
      ? digits
      : digits.length <= 7
        ? `${digits.slice(0, 3)}-${digits.slice(3)}`
        : `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`

  phone.value = formatted
  input.value = formatted
  phoneVerified.value = false
  verificationCodeSent.value = false
  verificationCode.value = ''
  verificationError.value = ''
}

const sendVerificationCode = () => {
  if (!/^010-\d{4}-\d{4}$/.test(phone.value)) {
    showToast('휴대폰 번호를 정확히 입력해 주세요.', 'error')
    return
  }

  phoneVerified.value = false
  verificationCodeSent.value = true
  verificationCode.value = ''
  verificationError.value = ''
  showToast('인증번호를 전송했습니다.', 'info')
}

const formatVerificationCode = (event: Event) => {
  const input = event.target as HTMLInputElement
  const digits = input.value.replace(/\D/g, '').slice(0, 6)
  verificationCode.value = digits
  input.value = digits
  verificationError.value = ''
}

const confirmVerificationCode = () => {
  if (verificationCode.value.length !== 6) {
    verificationError.value = '인증번호 6자리를 입력해 주세요.'
    return
  }
  if (verificationCode.value !== '123456') {
    verificationError.value = '인증번호가 일치하지 않습니다.'
    return
  }

  phoneVerified.value = true
  verificationCodeSent.value = false
  verificationCode.value = ''
  verificationError.value = ''
  showToast('휴대폰 번호가 인증되었습니다.', 'success')
}

const changeProfileImage = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    showToast('이미지 파일만 선택할 수 있어요.', 'error')
    return
  }

  if (profileObjectUrl) URL.revokeObjectURL(profileObjectUrl)
  profileObjectUrl = URL.createObjectURL(file)
  profileImage.value = profileObjectUrl
}

const saveProfile = () => {
  if (!name.value.trim()) {
    showToast('이름을 입력해 주세요.', 'error')
    return
  }
  if (!phoneVerified.value) {
    showToast('변경한 휴대폰 번호를 인증해 주세요.', 'error')
    return
  }

  showToast('내 정보가 저장되었습니다.', 'success')
  router.push({ name: 'Mypage' })
}

onBeforeUnmount(() => {
  if (profileObjectUrl) URL.revokeObjectURL(profileObjectUrl)
})
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-5 pb-[calc(24px+env(safe-area-inset-bottom))] text-[var(--color-text-primary)]"
  >
    <header class="px-0.5">
      <h1 class="m-0 text-[26px] leading-tight font-extrabold tracking-[-0.04em]">내 정보 수정</h1>
      <p class="mt-2 mb-0 text-sm leading-6 text-[var(--color-text-secondary)]">
        가족에게 보이는 내 정보와 연락처를 관리해요.
      </p>
    </header>

    <form class="mt-5" @submit.prevent="saveProfile">

      <section class="mt-5 rounded-[24px] border border-[#d9e2e7] bg-white p-5">
        <div class="mb-5">
          <h2 class="text-lg font-extrabold">기본 정보</h2>
        </div>

        <div class="space-y-5">
          <label class="block">
            <span class="field-label">이름 <em>*</em></span>
            <input
              v-model="name"
              class="field-input"
              type="text"
              maxlength="30"
              autocomplete="name"
            />
          </label>

          <div>
            <span class="field-label">가족 내 역할</span>
            <div class="role-display mt-2" aria-label="가족 내 역할">
              {{ role }}
            </div>
          </div>

          <label class="block">
            <span class="field-label">이메일</span>
            <input class="field-input text-[#97a3ad]" type="email" value="hana.kim@example.com" disabled />
          </label>

          <div>
            <span class="field-label">생년월일</span>
            <BaseDatePicker
              v-model="birthDate"
              class="birth-date-picker mt-2"
              placeholder="생년월일을 선택해 주세요"
              :min-year="1900"
              :max-year="new Date().getFullYear()"
            />
          </div>
        </div>
      </section>

      <section class="mt-5 rounded-[24px] border border-[#d9e2e7] bg-white p-5">
        <div class="mb-5 flex items-start justify-between gap-3">
          <div>
            <h2 class="text-lg font-extrabold">휴대폰 인증</h2>
            <p class="mt-1 text-xs text-[var(--color-text-secondary)]">안전한 정보 변경을 위해 확인해요.</p>
          </div>
          <span
            v-if="phoneVerified"
            class="shrink-0 rounded-full bg-[#ebf9f4] px-2.5 py-1 text-[11px] font-bold text-[#27966d]"
          >인증 완료</span>
        </div>

        <div>
          <label class="field-label" for="phone">휴대폰 번호 <em>*</em></label>
          <div class="verification-row mt-2">
            <input
              id="phone"
              :value="phone"
              class="field-input mt-0 min-w-0"
              type="tel"
              inputmode="numeric"
              autocomplete="tel"
              @input="formatPhone"
            />
            <button
              class="rounded-xl border border-[#b9e2f5] bg-[#edf9ff] text-xs font-bold text-[var(--color-selected-text)] active:bg-[#ddf4ff]"
              type="button"
              @click="sendVerificationCode"
            >
              {{ verificationCodeSent ? '재전송' : phoneVerified ? '재인증' : '인증 요청' }}
            </button>
          </div>

          <div v-if="verificationCodeSent" class="mt-3 rounded-[16px] border border-[#d9e8ef] bg-[#f7fafb] p-3">
            <div class="verification-row">
              <input
                :value="verificationCode"
                class="field-input mt-0 min-w-0 bg-white"
                type="text"
                inputmode="numeric"
                autocomplete="one-time-code"
                maxlength="6"
                placeholder="인증번호 6자리"
                aria-label="인증번호"
                @input="formatVerificationCode"
                @keyup.enter="confirmVerificationCode"
              />
              <button
                class="rounded-xl border-0 bg-[var(--color-brand-primary)] text-xs font-bold text-white disabled:opacity-45"
                type="button"
                :disabled="verificationCode.length !== 6"
                @click="confirmVerificationCode"
              >인증 확인</button>
            </div>
            <p v-if="verificationError" class="mt-2 mb-0 text-[11px] font-semibold text-[var(--color-danger)]">
              {{ verificationError }}
            </p>
            <p v-else class="mt-2 mb-0 text-[11px] text-[var(--color-text-secondary)]">
              테스트 인증번호는 <strong>123456</strong>이에요.
            </p>
          </div>

          <div v-if="phoneVerified" class="mt-3 flex items-center gap-1.5 text-xs font-semibold text-[#27966d]">
            <CheckCircle2 :size="15" :stroke-width="2.4" />
            현재 번호로 인증되어 있어요.
          </div>
          <p v-else-if="!verificationCodeSent" class="mt-3 mb-0 text-xs text-[#d76f4a]">
            변경한 번호는 저장 전에 인증이 필요해요.
          </p>
        </div>
      </section>

      <div class="mt-5 grid grid-cols-2 gap-3">
        <button
          class="h-14 rounded-2xl border border-[#d5dfe5] bg-white text-sm font-bold text-[var(--color-text-secondary)] active:bg-[#f5f7f8]"
          type="button"
          @click="router.back()"
        >취소</button>
        <button
          class="h-14 rounded-2xl border-0 bg-[var(--color-brand-primary)] text-sm font-bold text-white active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:opacity-45"
          type="submit"
          :disabled="!canSave"
        >변경사항 저장</button>
      </div>
    </form>
  </main>
</template>

<style scoped>
.field-label {
  display: block;
  color: var(--color-text-primary);
  font-size: 13px;
  font-style: normal;
  font-weight: 800;
}

.field-label em {
  color: #ef5f65;
  font-style: normal;
}

.role-display {
  display: flex;
  align-items: center;
  width: 100%;
  height: 52px;
  padding: 0 14px;
  color: #667681;
  font-size: 14px;
  font-weight: 700;
  background: #f4f6f8;
  border: 1px solid #dce7ed;
  border-radius: 12px;
  cursor: not-allowed;
}

.field-input {
  width: 100%;
  height: 52px;
  margin-top: 8px;
  padding: 0 14px;
  color: var(--color-text-primary);
  font-size: 14px;
  background: white;
  border: 1px solid #dce7ed;
  border-radius: 12px;
  transition:
    border-color 160ms ease,
    box-shadow 160ms ease;
}

.field-input:focus {
  border-color: var(--color-brand-primary);
  box-shadow: 0 0 0 3px rgb(85 192 244 / 14%);
  outline: none;
}

.field-input:disabled {
  background: #f4f6f8;
  cursor: not-allowed;
}

.field-help {
  display: block;
  margin-top: 7px;
  color: #98a3ac;
  font-size: 10px;
  line-height: 1.5;
}

.verification-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 104px;
  gap: 8px;
  height: 52px;
}

.verification-row > input,
.verification-row > button {
  box-sizing: border-box;
  width: 100%;
  height: 52px;
  min-height: 52px;
  margin-top: 0;
  line-height: 1;
}

.birth-date-picker :deep(> button) {
  height: 52px;
  padding: 0 14px;
  font-size: 14px;
  border-color: #dce7ed;
}

.birth-date-picker :deep(> div[role='dialog']) {
  top: auto;
  bottom: 100%;
  z-index: 30;
  max-height: min(430px, calc(100dvh - 84px));
  margin-top: 0;
  margin-bottom: 8px;
  overflow-y: auto;
}
</style>
