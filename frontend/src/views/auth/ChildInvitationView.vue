<script setup lang="ts">
import { Baby, UserRound } from 'lucide-vue-next'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import logoPigUrl from '@/assets/images/login/logo-pig.png'
import type { FamilyInvitationInfoResponse } from '@/api/generated'

const props = defineProps<{
  invitation: FamilyInvitationInfoResponse
  inviteToken: string
}>()

const router = useRouter()
const isAccepting = ref(false)

const guardian = {
  name: props.invitation.inviter_name ?? '',
}

const child = {
  name: props.invitation.child_name ?? '',
}

const acceptInvitation = () => {
  if (isAccepting.value) return
  isAccepting.value = true
  window.setTimeout(() => router.push({
    name: 'Login',
    query: { inviteToken: props.inviteToken, inviteeType: 'CHILD' },
  }), 450)
}

const declineInvitation = () => {
  if (isAccepting.value) return
  router.push({ name: 'Login' })
}
</script>

<template>
  <main class="flex min-h-dvh flex-col text-[var(--color-text-primary)]">
    <header class="flex h-16 shrink-0 items-center gap-1 border-b border-[var(--color-border)] bg-white px-2">
      <img
        class="size-12 shrink-0 object-contain"
        :src="logoPigUrl"
        alt=""
        aria-hidden="true"
      />
      <strong class="text-[16px] tracking-[-0.02em] text-[var(--color-text-primary)]">
        우리 <span class="text-[#f28faa]">아</span>이
        <span class="text-[#f28faa]">자</span>산관리 서비<span class="text-[#f28faa]">스</span>
      </strong>
    </header>

    <div class="flex flex-1 flex-col px-5 pt-3 pb-[max(24px,env(safe-area-inset-bottom))]">
      <section aria-labelledby="child-invitation-title">
        <h1 id="child-invitation-title" class="break-keep text-[30px] leading-[1.35] font-bold tracking-[-0.04em]">
          <span class="text-[var(--color-selected-text)]">{{ guardian.name }}</span>님이<br />당신을 자녀로 초대했어요
        </h1>
        <p class="mt-3 text-base text-[var(--color-text-secondary)]">
          아래 가족 정보를 확인하고 초대를 수락해주세요.
        </p>
      </section>

      <section class="mt-10" aria-labelledby="guardian-info-title">
        <div class="mb-3 flex items-center justify-between">
          <h2 id="guardian-info-title" class="text-base font-bold">초대한 보호자</h2>
        </div>
        <article class="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
          <div class="flex items-center gap-4">
            <span class="grid size-14 shrink-0 place-items-center rounded-full bg-[#eaf8ff] text-[var(--color-selected-text)]">
              <UserRound :size="27" :stroke-width="2.1" aria-hidden="true" />
            </span>
            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-2">
                <strong class="text-lg">{{ guardian.name }}</strong>
                <span class="rounded-full bg-[#eaf8ff] px-2 py-1 text-[10px] font-bold text-[var(--color-selected-text)]">보호자</span>
              </div>
              <p class="mt-2 text-xs text-[var(--color-text-secondary)]">가족 초대를 보냈어요</p>
            </div>
          </div>
        </article>
      </section>

      <section class="mt-8" aria-labelledby="child-info-title">
        <div class="mb-3 flex items-center justify-between">
          <h2 id="child-info-title" class="text-base font-bold">내 정보</h2>
        </div>
        <article class="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
          <div class="flex items-center gap-4">
            <span class="grid size-14 shrink-0 place-items-center rounded-full bg-[#eaf8ff] text-[var(--color-selected-text)]">
              <Baby :size="28" :stroke-width="2.1" aria-hidden="true" />
            </span>
            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-2">
                <strong class="text-lg">{{ child.name }}</strong>
              </div>
              <p class="mt-2 text-xs text-[var(--color-text-secondary)]">연결할 자녀 계정</p>
            </div>
          </div>
        </article>
      </section>

      <div class="mt-auto grid grid-cols-2 gap-3 pt-7">
        <button
          class="h-14 rounded-xl border border-[var(--color-border)] bg-white text-lg font-bold text-[var(--color-text-secondary)] transition-colors active:bg-[var(--color-surface-muted)] disabled:opacity-60"
          type="button"
          :disabled="isAccepting"
          @click="declineInvitation"
        >
          거절하기
        </button>
        <button
          class="h-14 rounded-xl bg-[var(--color-brand-primary)] text-lg font-bold text-[var(--color-text-inverse)] transition-colors active:bg-[var(--color-brand-primary-pressed)] disabled:opacity-60"
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
