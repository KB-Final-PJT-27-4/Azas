<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { Camera, CheckCircle2, ShieldCheck, UserRound } from 'lucide-vue-next'
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

const roles: FamilyRole[] = ['부', '모', '보호자']
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
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-6 pb-[calc(24px+env(safe-area-inset-bottom))] text-[var(--color-text-primary)]"
  >
    <header>
      <h1 class="m-0 text-[24px] font-extrabold">내 정보 수정</h1>
      <p class="mt-1.5 mb-0 text-[12px] leading-relaxed text-[var(--color-text-secondary)]">
        이름과 연락처, 가족 내 역할을 확인하고 변경할 수 있어요.
      </p>
    </header>

    <form class="mt-7" @submit.prevent="saveProfile">
      <section class="flex flex-col items-center" aria-label="프로필 사진">
        <button
          class="group relative grid size-[124px] place-items-center overflow-visible rounded-full border-0 bg-[#e6f7fe] text-[#6d9ebe] transition-transform active:scale-[0.98]"
          type="button"
          aria-label="프로필 사진 변경"
          @click="fileInput?.click()"
        >
          <img
            v-if="profileImage"
            :src="profileImage"
            alt="선택한 프로필"
            class="size-full rounded-full object-cover"
          />
          <UserRound v-else :size="36" :stroke-width="1.8" />
          <span
            class="absolute right-0 bottom-1 grid size-9 place-items-center rounded-full border-[3px] border-white bg-[var(--color-brand-primary)] text-white shadow-sm"
          >
            <Camera :size="16" :stroke-width="2.3" />
          </span>
        </button>
        <button
          class="mt-3 border-0 bg-transparent p-0 text-[12px] font-bold text-[var(--color-selected-text)]"
          type="button"
          @click="fileInput?.click()"
        >
          프로필 사진 변경
        </button>
        <input
          ref="fileInput"
          class="sr-only"
          type="file"
          accept="image/*"
          @change="changeProfileImage"
        />
      </section>

      <div class="mt-7 space-y-5">
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

        <fieldset class="m-0 border-0 p-0">
          <legend class="field-label">가족 내 역할 <em>*</em></legend>
          <div class="mt-2 grid grid-cols-3 gap-2.5">
            <label v-for="item in roles" :key="item" class="cursor-pointer">
              <input
                v-model="role"
                class="peer sr-only"
                type="radio"
                name="family-role"
                :value="item"
              />
              <span
                class="grid h-12 place-items-center rounded-xl bg-[#f4f7f9] text-[13px] font-bold text-[#7b8995] transition peer-checked:bg-[#e8f8ff] peer-checked:text-[var(--color-selected-text)] peer-focus-visible:ring-2 peer-focus-visible:ring-[#9cddfa]"
              >
                {{ item }}
              </span>
            </label>
          </div>
        </fieldset>

        <label class="block">
          <span class="field-label">이메일</span>
          <input
            class="field-input text-[#97a3ad]"
            type="email"
            value="hana.kim@example.com"
            disabled
          />
        </label>

        <div>
          <label class="field-label" for="phone">휴대폰 번호 <em>*</em></label>
          <div class="verification-row mt-2">
            <input
              id="phone"
              :value="phone"
              class="field-input mt-0 min-w-0 flex-1"
              type="tel"
              inputmode="numeric"
              autocomplete="tel"
              @input="formatPhone"
            />
            <button
              class="shrink-0 rounded-xl border border-[#b9e2f5] bg-[#edf9ff] px-4 text-[12px] font-bold text-[var(--color-selected-text)] active:bg-[#ddf4ff]"
              type="button"
              @click="sendVerificationCode"
            >
              {{ verificationCodeSent ? '재전송' : phoneVerified ? '재인증' : '인증번호 받기' }}
            </button>
          </div>
          <div
            v-if="verificationCodeSent"
            class="mt-2.5 rounded-xl border border-[#d9e8ef] bg-[#f8fbfc] p-3"
          >
            <div class="verification-row">
              <input
                :value="verificationCode"
                class="field-input mt-0 min-w-0 flex-1 bg-white"
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
                class="shrink-0 rounded-xl border-0 bg-[var(--color-brand-primary)] px-4 text-[12px] font-bold text-white disabled:opacity-45"
                type="button"
                :disabled="verificationCode.length !== 6"
                @click="confirmVerificationCode"
              >
                인증 확인
              </button>
            </div>
            <p
              v-if="verificationError"
              class="mt-2 mb-0 text-[11px] font-semibold text-[var(--color-danger)]"
            >
              {{ verificationError }}
            </p>
          </div>
          <div
            v-if="phoneVerified"
            class="mt-2.5 flex items-center gap-1.5 rounded-xl bg-[#ebf9f4] px-3 py-2.5 text-[11px] font-semibold text-[#27966d]"
          >
            <CheckCircle2 :size="15" :stroke-width="2.4" />
            휴대폰 번호가 인증되어 있습니다.
          </div>
          <p v-else-if="!verificationCodeSent" class="mt-2 mb-0 text-[11px] text-[#e17a50]">
            변경한 번호는 저장 전에 인증이 필요해요.
          </p>
        </div>

        <div class="block">
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

      <aside
        class="mt-5 flex gap-2.5 rounded-2xl bg-[#f6f8fa] p-4 text-[11px] leading-[1.65] text-[var(--color-text-secondary)]"
      >
        <ShieldCheck class="mt-0.5 shrink-0 text-[#7f99a9]" :size="18" />
        개인정보는 서비스 운영과 가족 계정 관리에 필요한 범위에서만 안전하게 사용됩니다.
      </aside>

      <div class="mt-5 grid grid-cols-2 gap-2.5">
        <button
          class="h-[52px] rounded-xl border border-[var(--color-border)] bg-white text-[14px] font-bold text-[var(--color-unselected-text)] active:bg-[#f5f7f8]"
          type="button"
          @click="router.back()"
        >
          취소
        </button>
        <button
          class="h-[52px] rounded-xl border-0 bg-[var(--color-brand-primary)] text-[14px] font-2bold text-white active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:opacity-45"
          type="submit"
          :disabled="!canSave"
        >
          변경사항 저장
        </button>
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

.field-input {
  width: 100%;
  height: 48px;
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
  display: flex;
  align-items: stretch;
  gap: 8px;
  height: 48px;
}

.verification-row > input,
.verification-row > button {
  box-sizing: border-box;
  height: 48px;
  min-height: 48px;
  margin-top: 0;
  line-height: 1;
}

.birth-date-picker :deep(> button) {
  height: 48px;
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
