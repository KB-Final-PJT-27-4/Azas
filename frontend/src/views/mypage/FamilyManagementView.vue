<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Baby, Check, ChevronRight, Copy, Link2, UserRound, X } from 'lucide-vue-next'

import { useToast } from '@/composables/useToast'
import { api, getApiErrorMessage } from '@/api'
import { resolveCurrentChildId } from '@/api/context'

type InviteType = 'guardian' | 'child'

const { showToast } = useToast()
const inviteType = ref<InviteType | null>(null)
const copied = ref(false)
const isLoading = ref(true)
const isCreatingInvitation = ref(false)

const childId = ref<number | null>(null)
const familyMembers = ref<Array<{ id: number; name: string; relation: string; initials: string; isMe: boolean; color: string }>>([])
const createdInvitationLink = ref('')

const invitationTitle = computed(() =>
  inviteType.value === 'guardian' ? '보호자 초대 링크' : '자녀 초대 링크',
)
const invitationDescription = computed(() =>
  inviteType.value === 'guardian'
    ? '초대받은 보호자는 가족의 자산과 목표를 함께 관리할 수 있어요.'
    : '자녀가 링크로 가입하면 이 가족 계정에 안전하게 연결돼요.',
)
const invitationLink = computed(() => {
  return createdInvitationLink.value
})

const openInvitation = async (type: InviteType) => {
  inviteType.value = type
  copied.value = false
  createdInvitationLink.value = ''
  if (!childId.value) return
  isCreatingInvitation.value = true
  try {
    const invitationRequest = {
      invitee_type: type === 'guardian' ? 'PARENT' : 'CHILD',
      expires_in_hours: 24,
    }
    const { data } = await api.createChildFamilyInvitationUsingPOST(
      childId.value,
      undefined,
      invitationRequest,
    )
    createdInvitationLink.value = data.invite_url
      ?? `${window.location.origin}/family-invitations/${data.invite_token ?? ''}`
  } catch (error) {
    showToast(getApiErrorMessage(error, '초대 링크를 만들지 못했습니다.'), 'error')
    closeInvitation()
  } finally {
    isCreatingInvitation.value = false
  }
}

const closeInvitation = () => {
  inviteType.value = null
  copied.value = false
}

onMounted(async () => {
  try {
    childId.value = await resolveCurrentChildId()
    const { data } = await api.getFamilyMembersUsingGET(childId.value)
    familyMembers.value = (data.items ?? []).map((member, index) => ({
      id: member.member_id ?? index,
      name: member.name ?? '보호자',
      relation: member.relation_type === 'FATHER' ? '부' : member.relation_type === 'MOTHER' ? '모' : '보호자',
      initials: (member.name ?? '보').slice(-1),
      isMe: member.is_me ?? false,
      color: index % 2 === 0 ? 'bg-[#e8f8ff] text-[#339dcc]' : 'bg-[#fff5dc] text-[#b58a2d]',
    }))
  } catch (error) {
    showToast(getApiErrorMessage(error, '가족 정보를 불러오지 못했습니다.'), 'error')
  } finally {
    isLoading.value = false
  }
})

const copyInvitationLink = async () => {
  if (isCreatingInvitation.value || !invitationLink.value) return

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
        <span
          v-if="isLoading"
          class="h-3 w-8 animate-pulse rounded-full bg-[#e4ecef]"
          aria-hidden="true"
        ></span>
        <span v-else class="text-xs font-semibold text-[var(--color-text-secondary)]">
          {{ familyMembers.length }}명
        </span>
      </div>

      <ul class="mt-2.5 m-0 list-none overflow-hidden rounded-[20px] border border-[#d9e2e7] bg-white px-4 p-0">
        <template v-if="isLoading">
          <li
            v-for="index in 2"
            :key="`family-member-skeleton-${index}`"
            class="flex min-h-[68px] items-center gap-3.5"
            :class="index > 1 ? 'border-t border-[#edf1f3]' : ''"
            aria-hidden="true"
          >
            <span class="size-11 shrink-0 animate-pulse rounded-full bg-[#edf3f6]"></span>
            <span class="min-w-0 flex-1">
              <span class="block h-4 w-20 animate-pulse rounded-md bg-[#e5ecef]"></span>
              <span class="mt-2 block h-3 w-24 animate-pulse rounded-full bg-[#edf2f4]"></span>
            </span>
            <span class="h-5 w-10 shrink-0 animate-pulse rounded-full bg-[#edf2f4]"></span>
          </li>
        </template>
        <li
          v-else
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

            <div
              class="mt-5 flex min-h-[72px] items-center gap-3 rounded-2xl bg-[#f3f8fa] p-4"
              :aria-busy="isCreatingInvitation"
            >
              <span
                class="grid size-10 shrink-0 place-items-center rounded-full bg-white text-[var(--color-selected-text)] shadow-sm"
              >
                <Link2 :size="20" />
              </span>
              <div v-if="isCreatingInvitation" class="min-w-0 flex-1" aria-hidden="true">
                <span class="block h-3 w-[88%] animate-pulse rounded-full bg-[#dfe8ec]"></span>
              </div>
              <p v-else class="m-0 min-w-0 flex-1 truncate text-[12px] font-medium text-[#647783]">
                {{ invitationLink }}
              </p>
            </div>

            <button
              class="mt-5 flex h-[54px] w-full items-center justify-center gap-2 rounded-2xl bg-[var(--color-brand-primary)] text-[14px] font-bold text-white active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-wait disabled:bg-[#cbd8df]"
              type="button"
              :disabled="isCreatingInvitation || !invitationLink"
              @click="copyInvitationLink"
            >
              <Check v-if="copied" :size="18" />
              <Copy v-else-if="!isCreatingInvitation" :size="17" />
              {{
                isCreatingInvitation
                  ? '초대 링크 만드는 중...'
                  : copied
                    ? '링크 복사 완료'
                    : '초대 링크 복사하기'
              }}
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
