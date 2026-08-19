<script setup lang="ts">
import { computed, ref } from 'vue'
import { Baby, Check, ChevronRight, Copy, Link2, UserRound, X } from 'lucide-vue-next'

import childProfileUrl from '@/assets/images/home/home-profile-baby.png'
import { useToast } from '@/composables/useToast'

type InviteType = 'guardian' | 'child'

const { showToast } = useToast()
const inviteType = ref<InviteType | null>(null)
const copied = ref(false)

const familyMembers = [
  {
    id: 1,
    name: '김하나',
    relation: '모',
    initials: '하',
    isMe: true,
    color: 'bg-[#e8f8ff] text-[#339dcc]',
  },
  {
    id: 2,
    name: '김민수',
    relation: '부',
    initials: '민',
    isMe: false,
    color: 'bg-[#fff5dc] text-[#b58a2d]',
  },
]

const invitationTitle = computed(() =>
  inviteType.value === 'guardian' ? '보호자 초대 링크' : '자녀 초대 링크',
)
const invitationDescription = computed(() =>
  inviteType.value === 'guardian'
    ? '초대받은 보호자는 가족의 자산과 목표를 함께 관리할 수 있어요.'
    : '자녀가 링크로 가입하면 이 가족 계정에 안전하게 연결돼요.',
)
const invitationLink = computed(() => {
  if (!inviteType.value) return ''
  if (inviteType.value === 'guardian') {
    return `${window.location.origin}/register?invited=true&role=guardian`
  }
  return `${window.location.origin}/register/child?invited=true`
})

const openInvitation = (type: InviteType) => {
  inviteType.value = type
  copied.value = false
}

const closeInvitation = () => {
  inviteType.value = null
  copied.value = false
}

const copyInvitationLink = async () => {
  try {
    await navigator.clipboard.writeText(invitationLink.value)
    copied.value = true
    showToast('초대 링크를 복사했습니다.', 'success')
  } catch {
    showToast('링크를 복사하지 못했습니다. 다시 시도해 주세요.', 'error')
  }
}
</script>

<template>
  <main
    class="flex h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height)-env(safe-area-inset-bottom))] flex-col overflow-hidden px-5 pt-4 pb-3 text-[var(--color-text-primary)]"
  >
    <header class="px-0.5">
      <h1 class="m-0 text-[26px] leading-tight font-extrabold tracking-[-0.04em]">우리 가족</h1>
      <p class="mt-2 mb-0 text-sm leading-6 text-[var(--color-text-secondary)]">
        깨비와 함께 자산을 관리하는 가족을 확인해요.
      </p>
    </header>

    <section class="mt-5" aria-labelledby="family-members-title">
      <div class="flex items-center justify-between px-0.5">
        <h2 id="family-members-title" class="m-0 text-[18px] font-extrabold">함께 관리하는 가족</h2>
        <span class="text-xs font-semibold text-[var(--color-text-secondary)]">{{ familyMembers.length }}명</span>
      </div>

      <ul class="mt-2.5 m-0 list-none overflow-hidden rounded-[20px] border border-[#d9e2e7] bg-white px-4 p-0">
        <li
          v-for="(member, index) in familyMembers"
          :key="member.id"
          class="flex min-h-[68px] items-center gap-3.5"
          :class="index ? 'border-t border-[#edf1f3]' : ''"
        >
          <span
            class="grid size-11 shrink-0 place-items-center rounded-full text-[13px] font-extrabold"
            :class="member.color"
            aria-hidden="true"
          >
            {{ member.initials }}
          </span>
          <span class="min-w-0 flex-1">
            <strong class="block text-[15px] font-extrabold">{{ member.name }}</strong>
            <span class="mt-1 block text-xs text-[var(--color-text-secondary)]"
              >{{ member.relation }} · 보호자</span
            >
          </span>
          <span
            v-if="member.isMe"
            class="ml-auto shrink-0 rounded-full bg-[#fff4cf] px-2.5 py-1 text-[10px] font-bold text-[#a67d18]"
            >본인</span
          >
        </li>
      </ul>
    </section>

    <section class="mt-5" aria-labelledby="family-invite-title">
      <h2 id="family-invite-title" class="m-0 px-0.5 text-[18px] font-extrabold">가족 초대</h2>
      <p class="mt-1.5 mb-0 px-0.5 text-xs text-[var(--color-text-secondary)]">역할에 맞는 초대 링크를 만들어보세요.</p>

      <div class="mt-3 grid gap-2.5">
        <button
          class="flex min-h-[68px] w-full items-center gap-3 rounded-[18px] border border-[#cfe8f3] bg-[#eef9fe] px-4 py-3 text-left active:bg-[#e3f5fc]"
          type="button"
          @click="openInvitation('guardian')"
        >
          <span class="grid size-11 shrink-0 place-items-center rounded-full bg-white text-[var(--color-selected-text)]"><UserRound :size="21" :stroke-width="2.2" /></span>
          <span class="min-w-0 flex-1">
            <strong class="block text-[15px]">보호자 초대</strong>
            <span class="mt-1 block text-xs text-[var(--color-text-secondary)]">자산과 목표를 함께 관리해요.</span>
          </span>
          <ChevronRight class="shrink-0 text-[#8da2ad]" :size="19" />
        </button>
        <button
          class="flex min-h-[68px] w-full items-center gap-3 rounded-[18px] border border-[#eadfbf] bg-[#fffaf0] px-4 py-3 text-left active:bg-[#fff5df]"
          type="button"
          @click="openInvitation('child')"
        >
          <span class="grid size-11 shrink-0 place-items-center rounded-full bg-white text-[#d89b2b]"><Baby :size="22" :stroke-width="2.1" /></span>
          <span class="min-w-0 flex-1">
            <strong class="block text-[15px]">자녀 초대</strong>
            <span class="mt-1 block text-xs text-[var(--color-text-secondary)]">아이 계정을 가족에 연결해요.</span>
          </span>
          <ChevronRight class="shrink-0 text-[#a79b7f]" :size="19" />
        </button>
      </div>
    </section>

    <Teleport to="body">
      <Transition name="invite-sheet">
        <div
          v-if="inviteType"
          class="fixed inset-0 z-[var(--z-index-overlay)] flex items-end justify-center bg-black/40"
          @click.self="closeInvitation"
        >
          <section
            class="invite-sheet-panel w-full max-w-[var(--app-max-width)] rounded-t-[26px] bg-white px-5 pt-3 pb-[calc(24px+env(safe-area-inset-bottom))]"
            role="dialog"
            aria-modal="true"
            :aria-labelledby="`${inviteType}-invite-title`"
          >
            <span class="mx-auto block h-1 w-10 rounded-full bg-[#d7dfe4]"></span>
            <header class="mt-4 flex items-start justify-between gap-4">
              <div>
                <h2 :id="`${inviteType}-invite-title`" class="m-0 text-[20px] font-bold">
                  {{ invitationTitle }}
                </h2>
                <p
                  class="mt-1.5 mb-0 text-[11px] leading-relaxed text-[var(--color-text-secondary)]"
                >
                  {{ invitationDescription }}
                </p>
              </div>
              <button
                class="grid size-9 shrink-0 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[#f2f5f7]"
                type="button"
                aria-label="초대 링크 닫기"
                @click="closeInvitation"
              >
                <X :size="20" />
              </button>
            </header>

            <div class="mt-5 flex items-center gap-3 rounded-2xl bg-[#f3f8fa] p-4">
              <span
                class="grid size-10 shrink-0 place-items-center rounded-full bg-white text-[var(--color-selected-text)] shadow-sm"
                ><Link2 :size="20"
              /></span>
              <p class="m-0 min-w-0 flex-1 truncate text-[12px] font-medium text-[#647783]">
                {{ invitationLink }}
              </p>
            </div>

            <button
              class="mt-5 flex h-[54px] w-full items-center justify-center gap-2 rounded-2xl bg-[var(--color-brand-primary)] text-[14px] font-bold text-white active:bg-[var(--color-brand-primary-pressed)]"
              type="button"
              @click="copyInvitationLink"
            >
              <Check v-if="copied" :size="18" />
              <Copy v-else :size="17" />
              {{ copied ? '링크 복사 완료' : '초대 링크 복사하기' }}
            </button>
          </section>
        </div>
      </Transition>
    </Teleport>
  </main>
</template>

<style scoped>
.invite-sheet-enter-active,
.invite-sheet-leave-active {
  transition: background-color 180ms ease;
}

.invite-sheet-enter-active .invite-sheet-panel,
.invite-sheet-leave-active .invite-sheet-panel {
  transition: transform 220ms cubic-bezier(0.22, 1, 0.36, 1);
}

.invite-sheet-enter-from,
.invite-sheet-leave-to {
  background-color: transparent;
}

.invite-sheet-enter-from .invite-sheet-panel,
.invite-sheet-leave-to .invite-sheet-panel {
  transform: translateY(100%);
}

@media (prefers-reduced-motion: reduce) {
  .invite-sheet-enter-active,
  .invite-sheet-leave-active,
  .invite-sheet-enter-active .invite-sheet-panel,
  .invite-sheet-leave-active .invite-sheet-panel {
    transition-duration: 1ms;
  }
}
</style>
