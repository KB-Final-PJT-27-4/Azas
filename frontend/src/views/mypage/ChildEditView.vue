<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import childProfileUrl from '@/assets/images/home/home-profile-baby.png'
import BaseDatePicker from '@/components/common/BaseDatePicker.vue'
import { useToast } from '@/composables/useToast'
import { api, getApiErrorMessage } from '@/api'
import { resolveCurrentChildId } from '@/api/context'

const router = useRouter()
const { showToast } = useToast()
const isLoading = ref(true)

const childId = ref<number | null>(null)
const name = ref('')
const birthDate = ref('')
const gender = ref<'남자' | '여자' | '아직 모름'>('남자')
const genders = ['남자', '여자', '아직 모름'] as const

const currentYear = new Date().getFullYear()
const canSave = computed(
  () => name.value.trim().length > 0 && Boolean(birthDate.value) && Boolean(gender.value),
)
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

const saveChild = async () => {
  if (!name.value.trim()) {
    showToast('자녀 이름을 입력해 주세요.', 'error')
    return
  }
  if (!birthDate.value) {
    showToast('생년월일을 선택해 주세요.', 'error')
    return
  }

  if (!childId.value) return
  try {
    await api.updateChildUsingPATCH(childId.value, {
      name: name.value.trim(),
      birth_date: birthDate.value,
      birth_status: 'BORN',
      gender: gender.value === '남자' ? 'MALE' : gender.value === '여자' ? 'FEMALE' : 'UNKNOWN',
    })
    showToast(`${name.value.trim()}의 정보가 저장되었습니다.`, 'success')
    await router.push({ name: 'Mypage' })
  } catch (error) {
    showToast(getApiErrorMessage(error, '자녀 정보를 저장하지 못했습니다.'), 'error')
  }
}

onMounted(async () => {
  try {
    childId.value = await resolveCurrentChildId()
    const { data } = await api.getChildUsingGET(childId.value)
    name.value = data.name ?? ''
    birthDate.value = data.birth_date ?? data.expected_birth_date ?? ''
    gender.value = data.gender === 'FEMALE' ? '여자' : data.gender === 'UNKNOWN' ? '아직 모름' : '남자'
  } catch (error) {
    showToast(getApiErrorMessage(error, '자녀 정보를 불러오지 못했습니다.'), 'error')
  } finally {
    isLoading.value = false
  }
})

</script>

<template>
  <main
    class="flex min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height)-env(safe-area-inset-bottom))] flex-col px-5 pt-5 pb-5 text-[var(--color-text-primary)]"
  >
    <header class="px-0.5">
      <h1 class="m-0 text-[26px] leading-tight font-extrabold tracking-[-0.04em]">자녀 정보 수정</h1>
      <p class="mt-2 mb-0 text-sm leading-6 text-[var(--color-text-secondary)]">
        아이에게 맞는 서비스를 위해 기본 정보를 확인해 주세요.
      </p>
    </header>

    <div v-if="isLoading" class="mt-3 flex flex-1 flex-col" aria-label="자녀 정보를 불러오는 중" aria-busy="true">
      <section class="flex min-h-[104px] items-center gap-4 rounded-[24px] border border-[#cfe8f3] bg-[#eaf8fe] p-4">
        <span class="size-[70px] shrink-0 animate-pulse rounded-full bg-white/80"></span>
        <span class="min-w-0 flex-1">
          <span class="block h-3 w-28 animate-pulse rounded-full bg-[#d6ebf4]"></span>
          <span class="mt-3 block h-6 w-24 animate-pulse rounded-md bg-[#d6ebf4]"></span>
          <span class="mt-2 block h-3 w-20 animate-pulse rounded-full bg-[#dceef5]"></span>
        </span>
      </section>

      <section class="mt-4 rounded-[22px] border border-[#d9e2e7] bg-white p-4" aria-hidden="true">
        <span class="block h-[22px] w-20 animate-pulse rounded-md bg-[#e3eaed]"></span>
        <div class="mt-5">
          <span class="block h-4 w-12 animate-pulse rounded-md bg-[#e5ecef]"></span>
          <span class="mt-2 block h-[52px] w-full animate-pulse rounded-xl bg-[#edf2f4]"></span>
        </div>
        <div class="mt-4">
          <span class="block h-4 w-12 animate-pulse rounded-md bg-[#e5ecef]"></span>
          <span class="mt-2 grid grid-cols-3 gap-2.5">
            <span v-for="index in 3" :key="index" class="h-13 animate-pulse rounded-xl bg-[#edf2f4]"></span>
          </span>
        </div>
        <div class="mt-4">
          <div class="flex items-center justify-between">
            <span class="block h-4 w-20 animate-pulse rounded-md bg-[#e5ecef]"></span>
            <span class="block h-6 w-14 animate-pulse rounded-full bg-[#edf2f4]"></span>
          </div>
          <span class="mt-2 block h-[52px] w-full animate-pulse rounded-xl bg-[#edf2f4]"></span>
        </div>
      </section>

      <div class="mt-auto grid grid-cols-2 gap-3 pt-3" aria-hidden="true">
        <span class="h-14 animate-pulse rounded-2xl bg-[#edf2f4]"></span>
        <span class="h-14 animate-pulse rounded-2xl bg-[#dce8ed]"></span>
      </div>
    </div>

    <form v-else class="mt-3 flex flex-1 flex-col" @submit.prevent="saveChild">
      <section class="flex items-center gap-4 rounded-[24px] border border-[#cfe8f3] bg-[#eaf8fe] p-4" aria-label="자녀 프로필">
        <span class="size-[70px] shrink-0 overflow-hidden rounded-full bg-white">
          <img :src="childProfileUrl" :alt="`${name || '아이'} 프로필`" class="size-full object-cover" />
        </span>
        <div class="min-w-0 flex-1">
          <span class="text-xs font-semibold text-[var(--color-selected-text)]">함께 관리 중인 아이</span>
          <strong class="mt-1 block truncate text-[22px]">{{ name || '이름을 입력해주세요' }}</strong>
          <span class="mt-1 block text-xs text-[var(--color-text-secondary)]">
            {{ [ageText, gender].filter(Boolean).join(' · ') }}
          </span>
        </div>
      </section>

      <section class="mt-4 rounded-[22px] border border-[#d9e2e7] bg-white p-4">
        <div class="mb-4">
          <h2 class="text-lg font-extrabold">기본 정보</h2>
        </div>

        <div class="space-y-4">
          <label class="block">
            <span class="field-label">이름 <em>*</em></span>
            <span class="relative block">
              <input v-model="name" class="field-input pr-[58px]" type="text" maxlength="20" autocomplete="off" placeholder="자녀 이름을 입력해 주세요" />
              <span class="pointer-events-none absolute right-3.5 bottom-[17px] text-[11px] font-medium text-[#98a3ac]">{{ name.length }}/20</span>
            </span>
          </label>

          <fieldset class="m-0 border-0 p-0">
            <legend class="field-label">성별 <em>*</em></legend>
            <div class="mt-2 grid grid-cols-3 gap-2.5">
              <label v-for="item in genders" :key="item" class="cursor-pointer">
                <input v-model="gender" class="peer sr-only" type="radio" name="gender" :value="item" />
                <span class="grid h-13 place-items-center rounded-xl border border-transparent bg-[#f4f6f7] text-sm font-bold text-[#7b8995] transition peer-checked:border-[var(--color-brand-primary)] peer-checked:bg-[#e8f8ff] peer-checked:text-[var(--color-selected-text)] peer-focus-visible:ring-2 peer-focus-visible:ring-[#9cddfa]">
                  {{ item }}
                </span>
              </label>
            </div>
          </fieldset>

          <div class="!mt-4">
            <div class="flex items-center justify-between">
              <span class="field-label">생년월일 <em>*</em></span>
              <span
                v-if="ageText"
                class="rounded-full bg-[#edf8fd] px-2.5 py-1 text-[11px] font-bold text-[var(--color-selected-text)]"
              >
                {{ ageText }}
              </span>
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
      </section>

      <div class="mt-auto grid grid-cols-2 gap-3 pt-3">
        <button class="h-14 rounded-2xl border border-[#d5dfe5] bg-white text-sm font-bold text-[var(--color-text-secondary)] active:bg-[#f5f7f8]" type="button" @click="router.back()">
          취소
        </button>
        <button class="h-14 rounded-2xl border-0 bg-[var(--color-brand-primary)] text-sm font-bold text-white active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:opacity-45" type="submit" :disabled="!canSave">
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
  box-sizing: border-box;
  width: 100%;
  height: 52px;
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
