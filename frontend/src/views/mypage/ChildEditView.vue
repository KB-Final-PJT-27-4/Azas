<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { Baby, Camera, Info } from 'lucide-vue-next'
import { useRouter } from 'vue-router'

import childProfileUrl from '@/assets/images/home/home-profile-baby.png'
import BaseDatePicker from '@/components/common/BaseDatePicker.vue'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { showToast } = useToast()

const name = ref('깨비')
const birthDate = ref('2014-07-15')
const profileImage = ref<string | null>(childProfileUrl)
const fileInput = ref<HTMLInputElement | null>(null)
let profileObjectUrl: string | null = null

const currentYear = new Date().getFullYear()
const canSave = computed(() => name.value.trim().length > 0 && Boolean(birthDate.value))
const ageText = computed(() => {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(birthDate.value)
  if (!match) return ''

  const today = new Date()
  const birthYear = Number(match[1])
  const birthMonth = Number(match[2]) - 1
  const birthDay = Number(match[3])
  let age = today.getFullYear() - birthYear
  if (today.getMonth() < birthMonth || (today.getMonth() === birthMonth && today.getDate() < birthDay)) {
    age -= 1
  }
  return age >= 0 ? `만 ${age}세` : ''
})

const changeProfileImage = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    showToast('이미지 파일만 선택할 수 있어요.', 'error')
    input.value = ''
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    showToast('5MB 이하의 이미지를 선택해 주세요.', 'error')
    input.value = ''
    return
  }

  if (profileObjectUrl) URL.revokeObjectURL(profileObjectUrl)
  profileObjectUrl = URL.createObjectURL(file)
  profileImage.value = profileObjectUrl
}

const saveChild = () => {
  if (!name.value.trim()) {
    showToast('자녀 이름을 입력해 주세요.', 'error')
    return
  }
  if (!birthDate.value) {
    showToast('생년월일을 선택해 주세요.', 'error')
    return
  }

  showToast(`${name.value.trim()}의 정보가 저장되었습니다.`, 'success')
  router.push({ name: 'Mypage' })
}

onBeforeUnmount(() => {
  if (profileObjectUrl) URL.revokeObjectURL(profileObjectUrl)
})
</script>

<template>
  <main class="flex min-h-[calc(100dvh-var(--app-header-height)-env(safe-area-inset-top))] flex-col bg-white px-5 pt-6 pb-[calc(24px+env(safe-area-inset-bottom))] text-[var(--color-text-primary)]">
    <header>
      <h1 class="m-0 text-[24px] font-extrabold">자녀 정보 수정</h1>
      <p class="mt-1.5 mb-0 text-[12px] leading-relaxed text-[var(--color-text-secondary)]">
        자녀의 프로필과 기본 정보를 확인하고 변경할 수 있어요.
      </p>
    </header>

    <form class="mt-7 flex flex-1 flex-col" @submit.prevent="saveChild">
      <section class="profile-card" aria-label="자녀 프로필 사진">
        <button
          class="group relative grid size-[132px] place-items-center overflow-visible rounded-full border-0 bg-[#e6f7fe] text-[#6d9ebe] transition-transform active:scale-[0.98]"
          type="button"
          aria-label="자녀 프로필 사진 변경"
          @click="fileInput?.click()"
        >
          <img v-if="profileImage" :src="profileImage" alt="자녀 프로필" class="size-full rounded-full object-cover" />
          <Baby v-else :size="38" :stroke-width="1.8" />
          <span class="absolute right-0 bottom-1 grid size-9 place-items-center rounded-full border-[3px] border-white bg-[var(--color-brand-primary)] text-white shadow-sm">
            <Camera :size="16" :stroke-width="2.3" />
          </span>
        </button>
        <button class="mt-3 border-0 bg-transparent p-0 text-[12px] font-bold text-[var(--color-selected-text)]" type="button" @click="fileInput?.click()">
          프로필 사진 변경
        </button>
        <input ref="fileInput" class="sr-only" type="file" accept="image/jpeg,image/png,image/webp" @change="changeProfileImage" />
      </section>

      <div class="mt-6 space-y-5">
        <label class="block">
          <span class="field-label">이름 <em>*</em></span>
          <span class="relative block">
            <input v-model="name" class="field-input pr-[58px]" type="text" maxlength="20" autocomplete="off" placeholder="자녀 이름을 입력해 주세요" />
            <span class="pointer-events-none absolute right-3.5 bottom-[15px] text-[11px] font-medium text-[#98a3ac]">{{ name.length }}/20</span>
          </span>
        </label>

        <div>
          <div class="flex items-center justify-between">
            <span class="field-label">생년월일 <em>*</em></span>
            <span v-if="ageText" class="rounded-full bg-[#edf8fd] px-2.5 py-1 text-[10px] font-bold text-[var(--color-selected-text)]">{{ ageText }}</span>
          </div>
          <BaseDatePicker
            v-model="birthDate"
            class="birth-date-picker mt-2"
            placeholder="생년월일을 선택해 주세요"
            :min-year="1900"
            :max-year="currentYear"
          />
        </div>
      </div>

      <aside class="mt-5 flex gap-2.5 rounded-2xl bg-[#f6f8fa] p-4 text-[11px] leading-[1.65] text-[var(--color-text-secondary)]">
        <Info class="mt-0.5 shrink-0 text-[#7f99a9]" :size="18" />
        정확한 생년월일을 입력해 주세요. 자녀의 연령에 따라 이용 가능한 기능과 금융상품이 달라질 수 있어요.
      </aside>

      <div class="mt-auto grid grid-cols-2 gap-2.5 pt-8">
        <button class="h-[52px] rounded-xl border border-[var(--color-border)] bg-white text-[14px] font-bold text-[var(--color-unselected-text)] active:bg-[#f5f7f8]" type="button" @click="router.back()">
          취소
        </button>
        <button class="h-[52px] rounded-xl border-0 bg-[var(--color-brand-primary)] text-[14px] font-bold text-white active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:opacity-45" type="submit" :disabled="!canSave">
          변경사항 저장
        </button>
      </div>
    </form>
  </main>
</template>

<style scoped>
.profile-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 26px 20px 20px;
  background: linear-gradient(180deg, #fbfdfe 0%, #fff 100%);
  border: 1px solid #dce7ed;
  border-radius: 20px;
}

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
  box-sizing: border-box;
  width: 100%;
  height: 48px;
  margin-top: 8px;
  padding: 0 14px;
  color: var(--color-text-primary);
  font-size: 14px;
  background: white;
  border: 1px solid #dce7ed;
  border-radius: 12px;
  transition: border-color 160ms ease, box-shadow 160ms ease;
}

.field-input:focus {
  border-color: var(--color-brand-primary);
  box-shadow: 0 0 0 3px rgb(85 192 244 / 14%);
  outline: none;
}

.field-help {
  display: block;
  margin-top: 7px;
  color: #98a3ac;
  font-size: 10px;
  line-height: 1.5;
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
