<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { LoaderCircle } from 'lucide-vue-next'

import { api } from '@/api'
import type { FamilyInvitationInfoResponse } from '@/api/generated'
import logoPigUrl from '@/assets/images/login/logo-pig.png'
import ChildInvitationView from '@/views/auth/ChildInvitationView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'

const props = defineProps<{ inviteToken: string }>()

const invitation = ref<FamilyInvitationInfoResponse | null>(null)
const errorMessage = ref('')

const previewInvitations: Record<string, FamilyInvitationInfoResponse> = {
  'preview-parent': {
    invitee_type: 'PARENT',
    inviter_name: '김하나',
    child_name: '아이',
    status: 'PENDING',
  },
  'preview-child': {
    invitee_type: 'CHILD',
    inviter_name: '김하나',
    child_name: '아이',
    status: 'PENDING',
  },
}

const loadInvitation = async () => {
  errorMessage.value = ''

  const previewInvitation = import.meta.env.DEV
    ? previewInvitations[props.inviteToken]
    : undefined

  if (previewInvitation) {
    invitation.value = previewInvitation
    return
  }

  try {
    const { data } = await api.getFamilyInvitationInfoUsingGET(props.inviteToken)

    if (data.status !== 'PENDING') {
      errorMessage.value = data.status === 'ACCEPTED'
        ? '이미 수락한 초대예요.'
        : '만료되었거나 취소된 초대예요.'
      return
    }

    if (data.invitee_type !== 'PARENT' && data.invitee_type !== 'CHILD') {
      errorMessage.value = '초대 유형을 확인할 수 없어요.'
      return
    }

    invitation.value = data
  } catch {
    errorMessage.value = '초대 정보를 불러오지 못했어요. 링크를 다시 확인해주세요.'
  }
}

onMounted(loadInvitation)
</script>

<template>
  <RegisterView
    v-if="invitation?.invitee_type === 'PARENT'"
    :invitation="invitation"
    :invite-token="inviteToken"
  />
  <ChildInvitationView
    v-else-if="invitation?.invitee_type === 'CHILD'"
    :invitation="invitation"
    :invite-token="inviteToken"
  />

  <main
    v-else
    class="grid min-h-dvh place-items-center bg-[var(--color-selected-background)] px-6 text-center text-[var(--color-text-primary)]"
  >
    <section class="w-full max-w-sm rounded-[28px] border border-[var(--color-border)] bg-white px-6 py-10 shadow-sm">
      <img class="mx-auto size-20 object-contain" :src="logoPigUrl" alt="" aria-hidden="true" />

      <LoaderCircle
        v-if="!errorMessage"
        class="mx-auto mt-6 animate-spin text-[var(--color-brand-primary)]"
        :size="34"
        aria-hidden="true"
      />
      <h1 class="mt-5 text-xl font-bold">
        {{ errorMessage ? '초대를 확인하지 못했어요' : '초대 정보를 확인하고 있어요' }}
      </h1>
      <p class="mt-3 break-keep text-sm leading-6 text-[var(--color-text-secondary)]">
        {{ errorMessage || '잠시만 기다려주세요.' }}
      </p>
      <button
        v-if="errorMessage"
        class="mt-7 h-13 w-full rounded-2xl bg-[var(--color-brand-primary)] text-base font-bold text-white"
        type="button"
        @click="loadInvitation"
      >
        다시 시도하기
      </button>
    </section>
  </main>
</template>
