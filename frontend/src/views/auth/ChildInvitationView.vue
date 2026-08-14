<script setup lang="ts">
import { Baby, CalendarDays, ShieldCheck, UserRound } from 'lucide-vue-next'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isAccepting = ref(false)

const guardian = {
  name: '김하나',
  role: '부',
  phone: '010-1234-5678',
}

const child = {
  name: '깨비',
  gender: '남자',
  birthDate: '2025.07.15',
}

const acceptInvitation = () => {
  if (isAccepting.value) return
  isAccepting.value = true
  window.setTimeout(() => router.push({ name: 'Login' }), 450)
}

const declineInvitation = () => {
  if (isAccepting.value) return
  router.push({ name: 'Login' })
}
</script>

<template>
  <main class="flex min-h-dvh flex-col text-[var(--color-text-primary)]">
    <header class="flex h-16 shrink-0 items-center border-b border-[var(--color-border)] bg-white px-6">
      <strong class="text-[16px] tracking-[-0.02em]">우리 아이 자산관리 서비스</strong>
    </header>

    <div class="flex flex-1 flex-col px-5 pt-1 pb-[max(24px,env(safe-area-inset-bottom))]">
      <section aria-labelledby="child-invitation-title">
        <h1 id="child-invitation-title" class="mt-5 break-keep text-[27px] leading-[1.35] font-extrabold tracking-[-0.04em]">
          <span class="text-[var(--color-selected-text)]">{{ guardian.name }}님</span>이<br />당신을 자녀로 초대했어요
        </h1>
        <p class="mt-3 text-sm leading-6 text-[var(--color-text-secondary)]">
          아래 가족 정보를 확인하고 초대를 수락해주세요.
        </p>
      </section>

      <section class="mt-8" aria-labelledby="guardian-info-title">
        <div class="mb-3 flex items-center justify-between">
          <h2 id="guardian-info-title" class="text-sm font-bold">초대한 보호자</h2>
        </div>
        <article class="rounded-[20px] border border-[#d9eaf2] bg-white p-4">
          <div class="flex items-center gap-4">
            <span class="grid size-14 shrink-0 place-items-center rounded-full bg-[#eaf8ff] text-[var(--color-selected-text)]">
              <UserRound :size="27" :stroke-width="2.1" aria-hidden="true" />
            </span>
            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-2">
                <strong class="text-lg">{{ guardian.name }}</strong>
                <span class="rounded-full bg-[#eaf8ff] px-2 py-1 text-[10px] font-bold text-[var(--color-selected-text)]">보호자</span>
              </div>
              <dl class="mt-2 grid gap-1.5 text-xs">
                <div class="grid grid-cols-[56px_1fr] items-center">
                  <dt class="text-[var(--color-text-secondary)]">관계</dt>
                  <dd class=" text-[var(--color-text-secondary)]">{{ guardian.role }}</dd>
                </div>
                <div class="grid grid-cols-[56px_1fr] items-center">
                  <dt class="text-[var(--color-text-secondary)]">연락처</dt>
                  <dd class="text-[var(--color-text-secondary)]">{{ guardian.phone }}</dd>
                </div>
              </dl>
            </div>
          </div>
        </article>
      </section>

      <section class="mt-7" aria-labelledby="child-info-title">
        <div class="mb-3 flex items-center justify-between">
          <h2 id="child-info-title" class="text-sm font-bold">내 정보</h2>
        </div>
        <article class="rounded-[20px] border border-[#d9eaf2] bg-[#f1faff] p-4">
          <div class="flex items-center gap-4">
            <span class="grid size-14 shrink-0 place-items-center rounded-full bg-white text-[var(--color-selected-text)]">
              <Baby :size="28" :stroke-width="2.1" aria-hidden="true" />
            </span>
            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-2">
                <strong class="text-lg">{{ child.name }}</strong>
              </div>
              <dl class="mt-2 grid gap-1.5 text-xs text-[var(--color-text-secondary)]">
                <div class="grid grid-cols-[56px_1fr] items-center">
                  <dt>생년월일</dt>
                  <dd>{{ child.birthDate }}</dd>
                </div>
                <div class="grid grid-cols-[56px_1fr] items-center">
                  <dt>성별</dt>
                  <dd>{{ child.gender }}</dd>
                </div>
              </dl>
            </div>
          </div>
        </article>
      </section>

      <div class="mt-auto grid grid-cols-2 gap-3">
        <button
          class="h-14 rounded-2xl border border-[var(--color-border)] bg-white text-base font-bold text-[var(--color-text-secondary)] transition-colors active:bg-[var(--color-surface-muted)] disabled:opacity-60"
          type="button"
          :disabled="isAccepting"
          @click="declineInvitation"
        >
          거절하기
        </button>
        <button
          class="h-14 rounded-2xl bg-[var(--color-brand-primary)] text-base font-bold text-[var(--color-text-inverse)] transition-colors active:bg-[var(--color-brand-primary-pressed)] disabled:opacity-60"
          type="button"
          :disabled="isAccepting"
          @click="acceptInvitation"
        >
          {{ isAccepting ? '연결 중...' : '수락하기' }}
        </button>
      </div>
    </div>
  </main>
</template>
