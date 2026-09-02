<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { LoaderCircle } from 'lucide-vue-next'

import { BaseDatePicker } from '@/components/common'
import { api, getApiErrorMessage } from '@/api'
import {
  requireAuthorizationHeader,
  resolveCurrentChildId,
  setCurrentChildId,
} from '@/api/context'
import { useToast } from '@/composables/useToast'

type GuardianRole = 'father' | 'mother' | 'guardian'
type ChildGender = 'male' | 'female' | 'unknown'

const router = useRouter()
const { showToast } = useToast()
const isSubmitting = ref(false)
const isRelationLoading = ref(true)

const guardianRoles: Array<{ label: string; value: GuardianRole }> = [
  { label: '부', value: 'father' },
  { label: '모', value: 'mother' },
  { label: '보호자', value: 'guardian' },
]
const genderOptions: Array<{ label: string; value: ChildGender }> = [
  { label: '남자', value: 'male' },
  { label: '여자', value: 'female' },
  { label: '아직 모름', value: 'unknown' },
]

const form = reactive({
  guardianRole: 'guardian' as GuardianRole,
  childName: '',
  date: '',
  gender: 'unknown' as ChildGender,
})

const isSubmitDisabled = computed(
  () => isSubmitting.value || isRelationLoading.value || !form.childName.trim() || !form.date,
)

onMounted(async () => {
  try {
    const authorization = requireAuthorizationHeader()
    const currentChildId = await resolveCurrentChildId()
    const { data: child } = await api.getChildUsingGET(currentChildId, authorization)
    const relationByType: Record<string, GuardianRole> = {
      FATHER: 'father',
      MOTHER: 'mother',
      GUARDIAN: 'guardian',
    }

    form.guardianRole = relationByType[child.relation_type ?? ''] ?? 'guardian'
  } catch {
    form.guardianRole = 'guardian'
  } finally {
    isRelationLoading.value = false
  }
})

const addChild = async () => {
  if (isSubmitDisabled.value) return

  isSubmitting.value = true
  try {
    const authorization = requireAuthorizationHeader()
    const today = new Date()
    const todayKey = [
      today.getFullYear(),
      String(today.getMonth() + 1).padStart(2, '0'),
      String(today.getDate()).padStart(2, '0'),
    ].join('-')
    const isExpected = form.date > todayKey

    const { data: child } = await api.createChildUsingPOST(
      {
        name: form.childName.trim(),
        birth_status: isExpected ? 'EXPECTED' : 'BORN',
        birth_date: isExpected ? undefined : form.date,
        expected_birth_date: isExpected ? form.date : undefined,
        gender: form.gender.toUpperCase() as 'MALE' | 'FEMALE' | 'UNKNOWN',
        relation_type: form.guardianRole.toUpperCase() as 'FATHER' | 'MOTHER' | 'GUARDIAN',
      },
      authorization,
    )

    if (child.child_id) setCurrentChildId(child.child_id)
    showToast(`${child.name ?? form.childName.trim()}의 프로필을 추가했어요.`, 'success')
    await router.replace({ name: 'Home' })
  } catch (error) {
    showToast(getApiErrorMessage(error, '아이 정보를 추가하지 못했습니다.'), 'error')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-[#f7fbfd] px-5 pt-7 pb-[max(32px,env(safe-area-inset-bottom))] text-[var(--color-text-primary)]">
    <header class="px-0.5">
      <h1 class="m-0 text-[26px] leading-tight font-extrabold tracking-[-0.04em]">
        아이의 정보를 알려주세요
      </h1>
      <p class="mt-2 text-[14px] leading-6 text-[var(--color-text-secondary)]">
        추가한 정보는 언제든 마이페이지에서 수정할 수 있어요.
      </p>
    </header>

    <form class="mt-6 grid gap-6 rounded-[22px] border border-[#d9e2e7] bg-white p-4" @submit.prevent="addChild">
      <fieldset>
        <legend class="mb-3 text-[13px] font-bold">아이와의 관계 <span class="text-[#ff6470]">*</span></legend>
        <div v-if="isRelationLoading" class="grid grid-cols-3 gap-2.5" aria-label="관계 정보 불러오는 중">
          <span v-for="index in 3" :key="index" class="h-13 animate-pulse rounded-xl bg-[#f0f3f5]"></span>
        </div>
        <div v-else class="grid grid-cols-3 gap-2.5">
          <button
            v-for="role in guardianRoles"
            :key="role.value"
            class="h-13 rounded-xl border text-[14px] font-bold transition-colors"
            :class="
              form.guardianRole === role.value
                ? 'border-[var(--color-brand-primary)] bg-[var(--color-selected-background)] text-[var(--color-selected-text)]'
                : 'border-transparent bg-[var(--color-unselected-background)] text-[var(--color-unselected-text)]'
            "
            type="button"
            :aria-pressed="form.guardianRole === role.value"
            @click="form.guardianRole = role.value"
          >
            {{ role.label }}
          </button>
        </div>
      </fieldset>

      <label class="grid gap-2">
        <span class="flex items-center justify-between text-[13px] font-bold">
          <span>아이 이름 또는 태명 <span class="text-[#ff6470]">*</span></span>
          <span class="font-medium text-[#9aa6b2]">{{ form.childName.length }}/20</span>
        </span>
        <input
          v-model="form.childName"
          class="h-13 rounded-xl border border-[var(--color-border)] bg-white px-4 text-[15px] outline-none transition placeholder:text-[#a8b0b8] focus:border-[var(--color-brand-primary)] focus:ring-2 focus:ring-[#dff4ff]"
          type="text"
          maxlength="20"
          autocomplete="name"
          placeholder="이름을 입력해주세요"
        />
      </label>

      <BaseDatePicker
        v-model="form.date"
        label="생년월일 또는 출생예정일"
        class="child-add-date-picker"
        :min-year="1900"
        :max-year="new Date().getFullYear() + 20"
      />

      <fieldset>
        <legend class="mb-3 text-[13px] font-bold">아이 성별 <span class="text-[#ff6470]">*</span></legend>
        <div class="grid grid-cols-3 gap-2.5">
          <button
            v-for="gender in genderOptions"
            :key="gender.value"
            class="h-13 rounded-xl border text-[14px] font-bold transition-colors"
            :class="
              form.gender === gender.value
                ? 'border-[var(--color-brand-primary)] bg-[var(--color-selected-background)] text-[var(--color-selected-text)]'
                : 'border-transparent bg-[var(--color-unselected-background)] text-[var(--color-unselected-text)]'
            "
            type="button"
            :aria-pressed="form.gender === gender.value"
            @click="form.gender = gender.value"
          >
            {{ gender.label }}
          </button>
        </div>
      </fieldset>

      <button
        class="mt-1 flex h-14 w-full items-center justify-center gap-2 rounded-[14px] border-0 bg-[var(--color-brand-primary)] text-[16px] font-bold text-white transition active:bg-[var(--color-brand-primary-pressed)] disabled:bg-[#cbd8df]"
        type="submit"
        :disabled="isSubmitDisabled"
      >
        <LoaderCircle v-if="isSubmitting" class="animate-spin" :size="19" aria-hidden="true" />
        {{ isSubmitting ? '추가하는 중...' : '아이 추가하기' }}
      </button>
    </form>
  </main>
</template>

<style scoped>
.child-add-date-picker :deep(> span) {
  font-size: 13px;
}

.child-add-date-picker :deep(> span::after) {
  content: ' *';
  color: #ff6470;
}

.child-add-date-picker :deep(> button) {
  min-height: 52px;
  border-radius: 12px;
  font-size: 15px;
}
</style>
