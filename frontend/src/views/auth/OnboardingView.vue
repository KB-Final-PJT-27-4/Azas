<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'

import onboardingImage from '@/assets/images/login/bg-hero.png'
import { loadRegistrationDraft } from '@/utils/registrationDraft'

const router = useRouter()
const registration = loadRegistrationDraft()

const guardianRoleLabels = {
  father: '부',
  mother: '모',
  guardian: '보호자',
}
const genderLabels = {
  male: '남자',
  female: '여자',
  unknown: '아직 모름',
}

const formattedBirthDate = computed(() => {
  if (!registration?.birthDate) return '-'
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(registration.birthDate)
  if (!match) return registration.birthDate

  return `${match[1]}년 ${Number(match[2])}월 ${Number(match[3])}일`
})

const startService = () => {
  router.push({ name: 'Accounts' })
}

const editRegistration = () => {
  router.push({
    name: 'Register',
    query: {
      edit: 'true',
      ...(registration?.invited ? { invited: 'true' } : {}),
    },
  })
}
</script>

<template>
  <main
    class="relative isolate min-h-dvh overflow-hidden bg-[#EAF7FE] text-[var(--color-text-primary)]"
  >
    <div class="absolute inset-x-0 top-0 -z-2 h-30 bg-[#EAF7FE]" aria-hidden="true"></div>

    <img
      class="absolute inset-x-0 top-40 bottom-20 -z-1 h-[calc(100%-15rem)] w-full object-cover object-[94%_100%]"
      :src="onboardingImage"
      alt="아이와 저금통이 함께 있는 자산관리 서비스 일러스트"
    />

    <div class="absolute inset-x-0 bottom-0 -z-1 h-20 bg-[#C7E7FE]" aria-hidden="true"></div>

    <section
      class="absolute top-[12%] right-6 left-6 text-center"
      aria-labelledby="onboarding-title"
    >
      <h1 id="onboarding-title" class="text-[32px] leading-[1.3] font-bold tracking-[-0.04em]">
        준비가 모두 끝났어요!
      </h1>
      <p class="mt-2 text-base text-[var(--color-text-secondary)]">
        우리 아이의 성장과 자산을 함께 기록해 보세요.
      </p>

      <dl
        v-if="registration"
        class="mt-6 grid gap-4 rounded-2xl border border-white/80 bg-white/90 p-5 text-left shadow-[0_12px_32px_rgb(63_135_174_/_12%)] backdrop-blur-sm"
      >
        <div class="flex items-center justify-between">
          <dt class="text-sm text-[var(--color-text-secondary)]">본인 정보</dt>
          <dd class="font-bold">{{ guardianRoleLabels[registration.guardianRole] }}</dd>
        </div>
        <div class="h-px bg-[var(--color-border)]"></div>
        <div class="flex items-center justify-between">
          <dt class="text-sm text-[var(--color-text-secondary)]">아이 이름 또는 태명</dt>
          <dd class="font-bold">{{ registration.childName }}</dd>
        </div>
        <div class="h-px bg-[var(--color-border)]"></div>
        <div class="flex items-center justify-between">
          <dt class="text-sm text-[var(--color-text-secondary)]">생년월일 또는 출생예정일</dt>
          <dd class="font-bold">{{ formattedBirthDate }}</dd>
        </div>
        <div class="h-px bg-[var(--color-border)]"></div>
        <div class="flex items-center justify-between">
          <dt class="text-sm text-[var(--color-text-secondary)]">아이 성별</dt>
          <dd class="font-bold">{{ genderLabels[registration.gender] }}</dd>
        </div>
      </dl>
    </section>

    <div class="absolute right-6 bottom-10 left-6 grid grid-cols-2 gap-3">
      <button
        class="h-14 rounded-xl border border-[var(--color-brand-primary)] bg-white text-lg font-bold text-[var(--color-brand-primary)] transition-colors active:bg-[var(--color-selected-background)]"
        type="button"
        @click="editRegistration"
      >
        수정하기
      </button>
      <button
        class="h-14 rounded-xl bg-[var(--color-brand-primary)] text-lg font-bold text-[var(--color-text-inverse)] transition-colors active:bg-[var(--color-brand-primary-pressed)]"
        type="button"
        @click="startService"
      >
        시작하기
      </button>
    </div>
  </main>
</template>
