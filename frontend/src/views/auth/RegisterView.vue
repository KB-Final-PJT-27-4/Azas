<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'

type GuardianRole = 'father' | 'mother' | 'guardian'
type ChildGender = 'male' | 'female' | 'unknown'

const route = useRoute()

// 기본: /register, 공동 보호자 초대: /register?invited=true
const isGuardianInvitation = computed(() => route.query.invited === 'true')

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

const form = reactive({
  guardianRole: 'father' as GuardianRole,
  childName: isGuardianInvitation.value ? '김깨비' : '',
  birthDate: isGuardianInvitation.value ? '2025.07.15' : '',
  gender: 'male' as ChildGender,
})

const submitRegistration = () => {
  // TODO: 회원가입 또는 공동 보호자 초대 수락 API 연결
}
</script>

<template>
  <main class="flex min-h-dvh flex-col bg-[var(--color-surface)] text-[var(--color-text-primary)]">
    <header class="border-b border-[var(--color-border)] px-6 py-6">
      <p class="text-lg font-bold">우리 아이 자산관리 서비스</p>
    </header>

    <form class="flex flex-1 flex-col px-6 pt-14 pb-10" @submit.prevent="submitRegistration">
      <section aria-labelledby="register-title">
        <template v-if="isGuardianInvitation">
          <h1 id="register-title" class="text-[30px] leading-[1.35] font-bold tracking-[-0.04em]">
            <span class="text-[var(--color-selected-text)]">{{ inviterName }}님</span>
            이
            <span class="text-[var(--color-selected-text)]">깨비</span>
            의 공동 보호자로<br />
            초대했습니다.
          </h1>
          <p class="mt-3 text-base text-[var(--color-text-secondary)]">
            아이의 정보를 확인해주세요.
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

      <div class="mt-10 grid gap-8">
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

        <label class="grid gap-3">
          <span class="text-base font-bold">생년월일 또는 출생예정일</span>
          <input
            v-model="form.birthDate"
            class="h-14 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 text-lg outline-none transition-colors focus:border-[var(--color-brand-primary-pressed)] focus:ring-2 focus:ring-[var(--color-selected-background)] disabled:cursor-not-allowed disabled:border-[var(--color-disabled-border)] disabled:bg-[var(--color-disabled-background)] disabled:text-[var(--color-unselected-text)]"
            type="text"
            inputmode="numeric"
            placeholder="YYYY.MM.DD"
            :disabled="isGuardianInvitation"
          />
        </label>

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
        class="mt-auto h-14 rounded-xl bg-[var(--color-brand-primary)] text-lg font-bold text-[var(--color-text-inverse)] transition-colors active:bg-[var(--color-brand-primary-pressed)]"
        type="submit"
      >
        {{ isGuardianInvitation ? '수락' : '다음' }}
      </button>
    </form>
  </main>
</template>
