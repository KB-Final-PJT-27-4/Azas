<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LoaderCircle } from 'lucide-vue-next'

import { api } from '@/api'
import { getOAuthErrorMessage, loginWithOAuthCode } from '@/api/auth'
import logoPigUrl from '@/assets/images/login/logo-pig.png'
import { consumeOAuthInvitation, consumeOAuthState, getOAuthRedirectUri, isOAuthProvider } from '@/utils/oauth'

const route = useRoute()
const router = useRouter()
const errorMessage = ref('')

const moveToLogin = () => router.replace({ name: 'Login' })

onMounted(async () => {
  const provider = route.params.provider
  const code = typeof route.query.code === 'string' ? route.query.code : ''
  const state = typeof route.query.state === 'string' ? route.query.state : null
  const providerError = typeof route.query.error === 'string' ? route.query.error : ''

  if (!isOAuthProvider(provider)) {
    errorMessage.value = '지원하지 않는 로그인 방식이에요.'
    return
  }

  if (providerError) {
    consumeOAuthState(provider, state)
    errorMessage.value = '소셜 로그인이 취소되었어요.'
    return
  }

  if (!code || !consumeOAuthState(provider, state)) {
    errorMessage.value = '로그인 요청을 확인할 수 없어요. 처음부터 다시 시도해주세요.'
    return
  }

  try {
    const invitation = consumeOAuthInvitation(provider)
    const response = await loginWithOAuthCode(provider, code, getOAuthRedirectUri(provider), invitation)
    const isChildInvitation = invitation?.inviteeType === 'CHILD'
    if (isChildInvitation) await api.acceptFamilyInvitationUsingPOST(invitation.inviteToken)

    const isChildMember = response.member?.member_type === 'CHILD'
    const nextRouteName = response.is_new_member && !invitation
      ? 'Register'
      : isChildInvitation || isChildMember
        ? 'ChildHome'
        : 'Home'

    await router.replace({ name: nextRouteName })
  } catch (error) {
    errorMessage.value = getOAuthErrorMessage(error)
  }
})
</script>

<template>
  <main
    class="grid min-h-dvh place-items-center bg-[var(--color-selected-background)] px-6 text-center text-[var(--color-text-primary)]"
  >
    <section class="w-full max-w-sm rounded-[28px] border border-[var(--color-border)] bg-white px-6 py-10 shadow-sm">
      <img class="mx-auto size-20 object-contain" :src="logoPigUrl" alt="" aria-hidden="true" />

      <template v-if="!errorMessage">
        <LoaderCircle
          class="mx-auto mt-6 animate-spin text-[var(--color-brand-primary)]"
          :size="34"
          :stroke-width="2.4"
          aria-hidden="true"
        />
        <h1 class="mt-5 text-xl font-bold">로그인하고 있어요</h1>
        <p class="mt-2 text-sm leading-6 text-[var(--color-text-secondary)]">
          잠시만 기다려주세요.
        </p>
      </template>

      <template v-else>
        <h1 class="mt-5 text-xl font-bold">로그인을 완료하지 못했어요</h1>
        <p class="mt-3 break-keep text-sm leading-6 text-[var(--color-text-secondary)]">
          {{ errorMessage }}
        </p>
        <button
          class="mt-7 h-13 w-full rounded-2xl bg-[var(--color-brand-primary)] text-base font-bold text-white active:bg-[var(--color-brand-primary-pressed)]"
          type="button"
          @click="moveToLogin"
        >
          로그인 화면으로 돌아가기
        </button>
      </template>
    </section>
  </main>
</template>
