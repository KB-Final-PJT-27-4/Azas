<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Baby, CalendarDays, ShieldCheck, UserRound } from 'lucide-vue-next'
import { BaseDatePicker } from '@/components/common'
import {
  loadRegistrationDraft,
  saveRegistrationDraft,
  type ChildGender,
  type GuardianRole,
} from '@/utils/registrationDraft'

const route = useRoute()
const router = useRouter()

// 기본: /register, 공동 보호자 초대: /register?invited=true
const isGuardianInvitation = computed(() => route.query.invited === 'true')
const isEditing = computed(() => route.query.edit === 'true')

const inviterName = ref('김하나')
const guardianRoles: { label: string; value: GuardianRole }[] = [
  { label: '부', value: 'father' },
  { label: '모', value: 'mother' },
  { label: '보호자', value: 'guardian' },
]
const genderOptions: { label: string; value: ChildGender }[] = [
  { label: '남자', value: 'male' },
  { label: '여자', value: 'female' },
  { label: '아직 모름', value: 'unknown' },
]

const savedDraft = isEditing.value ? loadRegistrationDraft() : null

const form = reactive({
  guardianRole: savedDraft?.guardianRole ?? ('father' as GuardianRole),
  childName: savedDraft?.childName ?? (isGuardianInvitation.value ? '김깨비' : ''),
  birthDate: savedDraft?.birthDate ?? (isGuardianInvitation.value ? '2025-07-15' : ''),
  gender: savedDraft?.gender ?? ('male' as ChildGender),
})

const isSubmitDisabled = computed(() => !form.childName.trim() || !form.birthDate.trim())

const submitRegistration = () => {
  if (isSubmitDisabled.value) return

  saveRegistrationDraft({ ...form, invited: isGuardianInvitation.value })

  // TODO: 회원가입 또는 공동 보호자 초대 수락 API 연결
  router.push({ name: isGuardianInvitation.value ? 'Login' : 'Onboarding' })
}

const declineInvitation = () => router.push({ name: 'Login' })
</script>

<template>
  <main class="flex min-h-dvh flex-col text-[var(--color-text-primary)]">
    <header class="flex h-16 shrink-0 items-center border-b border-[var(--color-border)] bg-white px-6">
      <strong class="text-[16px] tracking-[-0.02em]">우리 아이 자산관리 서비스</strong>
    </header>

    <form class="flex flex-1 flex-col px-5 pt-1 pb-[max(24px,env(safe-area-inset-bottom))]" @submit.prevent="submitRegistration">
      <section aria-labelledby="register-title">
        <template v-if="isGuardianInvitation">
          <h1 id="register-title" class="mt-5 break-keep text-[27px] leading-[1.35] font-extrabold tracking-[-0.04em]">
            <span class="text-[var(--color-selected-text)]">{{ inviterName }}</span>님이<br />공동 보호자로 초대했어요
          </h1>
          <p class="mt-3 text-sm leading-6 text-[var(--color-text-secondary)]">
            가족 정보를 확인하고 깨비와 함께할 관계를 선택해주세요.
          </p>
        </template>

        <template v-else>
          <h1 id="register-title" class="text-[30px] leading-[1.35] font-bold tracking-[-0.04em]">
            아이의 정보를 알려주세요
          </h1>
          <p class="mt-3 text-base text-[var(--color-text-secondary)]">
            입력한 정보는 언제든 마이페이지에서 수정할 수 있어요.
          </p>
        </template>
      </section>

      <template v-if="isGuardianInvitation">
        <section class="mt-8" aria-labelledby="inviter-info-title">
          <div class="mb-3 flex items-center justify-between">
            <h2 id="inviter-info-title" class="text-sm font-bold">초대한 보호자</h2>
          </div>
          <article class="rounded-[20px] border border-[#d9eaf2] bg-white p-4">
            <div class="flex items-center gap-4">
              <span class="grid size-14 shrink-0 place-items-center rounded-full bg-[#eaf8ff] text-[var(--color-selected-text)]">
                <UserRound :size="27" :stroke-width="2.1" aria-hidden="true" />
              </span>
              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-2">
                  <strong class="text-lg">{{ inviterName }}</strong>
                  <span class="rounded-full bg-[#eaf8ff] px-2 py-1 text-[10px] font-bold text-[var(--color-selected-text)]">대표 보호자</span>
                </div>
                <dl class="mt-2 grid gap-1.5 text-xs">
                  <div class="grid grid-cols-[56px_1fr] items-center">
                    <dt class="text-[var(--color-text-secondary)]">관계</dt>
                    <dd class=" text-[var(--color-text-secondary)]">부</dd>
                  </div>
                  <div class="grid grid-cols-[56px_1fr] items-center">
                    <dt class="text-[var(--color-text-secondary)]">연락처</dt>
                    <dd class="text-[var(--color-text-secondary)]">010-1234-5678</dd>
                  </div>
                </dl>
              </div>
            </div>
          </article>
        </section>

        <section class="mt-7" aria-labelledby="invited-child-title">
          <div class="mb-3 flex items-center justify-between">
            <h2 id="invited-child-title" class="text-sm font-bold">함께 관리할 아이</h2>
          </div>
          <article class="rounded-[20px] border border-[#d9eaf2] bg-[#f1faff] p-4">
            <div class="flex items-center gap-4">
              <span class="grid size-14 shrink-0 place-items-center rounded-full bg-white text-[var(--color-selected-text)]">
                <Baby :size="28" :stroke-width="2.1" aria-hidden="true" />
              </span>
              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-2">
                  <strong class="text-lg">{{ form.childName }}</strong>
                </div>
                <dl class="mt-2 grid gap-1.5 text-xs text-[var(--color-text-secondary)]">
                  <div class="grid grid-cols-[56px_1fr] items-center">
                    <dt>생년월일</dt>
                    <dd>{{ form.birthDate.replaceAll('-', '.') }}</dd>
                  </div>
                  <div class="grid grid-cols-[56px_1fr] items-center">
                    <dt>성별</dt>
                    <dd>{{ genderOptions.find(({ value }) => value === form.gender)?.label }}</dd>
                  </div>
                </dl>
              </div>
            </div>
          </article>
        </section>

        <fieldset class="mt-7">
          <legend class="mb-3 text-base font-bold">본인 정보</legend>
          <p class="-mt-2 mb-3 text-xs text-[var(--color-text-secondary)]">아이와의 관계를 선택해주세요.</p>
          <div class="grid grid-cols-3 gap-3">
            <button
              v-for="role in guardianRoles"
              :key="role.value"
              class="h-12 rounded-xl border text-sm font-bold transition-colors"
              :class="
                form.guardianRole === role.value
                  ? 'border-[var(--color-brand-primary)] bg-[var(--color-selected-background)] text-[var(--color-selected-text)]'
                  : 'border-[var(--color-border)] bg-white text-[var(--color-text-secondary)]'
              "
              type="button"
              :aria-pressed="form.guardianRole === role.value"
              @click="form.guardianRole = role.value"
            >
              {{ role.label }}
            </button>
          </div>
        </fieldset>

        <div class="mt-auto grid grid-cols-2 gap-3 pt-7">
          <button
            class="h-14 rounded-2xl border border-[var(--color-border)] bg-white text-base font-bold text-[var(--color-text-secondary)] active:bg-[var(--color-surface-muted)]"
            type="button"
            @click="declineInvitation"
          >
            거절하기
          </button>
          <button
            class="h-14 rounded-2xl bg-[var(--color-brand-primary)] text-base font-bold text-[var(--color-text-inverse)] active:bg-[var(--color-brand-primary-pressed)]"
            type="submit"
          >
            수락하기
          </button>
        </div>
      </template>

      <div v-else class="mt-10 grid gap-8">
        <fieldset>
          <legend class="mb-3 text-base font-bold">본인 정보</legend>
          <div class="grid grid-cols-3 gap-3">
            <button
              v-for="role in guardianRoles"
              :key="role.value"
              class="h-14 rounded-xl text-lg font-bold transition-colors"
              :class="
                form.guardianRole === role.value
                  ? 'bg-[var(--color-selected-background)] text-[var(--color-selected-text)]'
                  : 'bg-[var(--color-unselected-background)] text-[var(--color-unselected-text)]'
              "
              type="button"
              :aria-pressed="form.guardianRole === role.value"
              @click="form.guardianRole = role.value"
            >
              {{ role.label }}
            </button>
          </div>
        </fieldset>

        <label class="grid gap-3">
          <span class="text-base font-bold">아이 이름 또는 태명</span>
          <input
            v-model="form.childName"
            class="h-14 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 text-lg outline-none transition-colors placeholder:text-[var(--color-text-secondary)] focus:border-[var(--color-brand-primary-pressed)] focus:ring-2 focus:ring-[var(--color-selected-background)] disabled:cursor-not-allowed disabled:border-[var(--color-disabled-border)] disabled:bg-[var(--color-disabled-background)] disabled:text-[var(--color-unselected-text)]"
            type="text"
            autocomplete="name"
            placeholder="김깨비"
            :disabled="isGuardianInvitation"
          />
        </label>

        <BaseDatePicker
          v-model="form.birthDate"
          label="생년월일 또는 출생예정일"
          :disabled="isGuardianInvitation"
          :min-year="1900"
          :max-year="new Date().getFullYear() + 20"
        />

        <fieldset>
          <legend class="mb-3 text-base font-bold">아이 성별</legend>
          <div class="grid grid-cols-3 gap-3">
            <button
              v-for="gender in genderOptions"
              :key="gender.value"
              class="h-14 rounded-xl text-lg font-bold transition-colors"
              :class="[
                form.gender === gender.value
                  ? 'bg-[var(--color-selected-background)] text-[var(--color-selected-text)]'
                  : 'bg-[var(--color-unselected-background)] text-[var(--color-unselected-text)]',
                { 'cursor-not-allowed': isGuardianInvitation },
              ]"
              type="button"
              :aria-pressed="form.gender === gender.value"
              :disabled="isGuardianInvitation"
              @click="form.gender = gender.value"
            >
              {{ gender.label }}
            </button>
          </div>
        </fieldset>
      </div>

      <button
        v-if="!isGuardianInvitation"
        class="mt-auto h-14 rounded-xl bg-[var(--color-brand-primary)] text-lg font-bold text-[var(--color-text-inverse)] transition-colors active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[var(--color-disabled-background)] disabled:text-[var(--color-unselected-text)]"
        type="submit"
        :disabled="isSubmitDisabled"
      >
        다음
      </button>
    </form>
  </main>
</template>
