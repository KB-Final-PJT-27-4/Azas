<script setup lang="ts">
import registrationCompleteImage from '@/assets/images/accounts/registration-complete.png'
import walletIcon from '@/assets/images/accounts/wallet-icon.png'

const emit = defineEmits<{
  createPlan: []
}>()
</script>

<template>
  <section
    class="flex min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] flex-col bg-white"
  >
    <div class="flex flex-1 flex-col px-6 pt-9 pb-6">
      <h1
        class="text-[28px] leading-tight font-bold tracking-[-0.03em] text-[var(--color-text-primary)]"
      >
        계좌 등록이 완료되었어요!
      </h1>
      <p class="mt-3 text-sm leading-relaxed text-[var(--color-text-secondary)]">
        이제 아이의 저축 목표를 설정하고<br />
        함께 성장해봐요
      </p>

      <img
        class="mx-auto mt-6 aspect-square w-full max-w-[340px] object-contain"
        :src="registrationCompleteImage"
        alt="계좌 등록 완료를 나타내는 저금통"
      />

      <div
        class="mt-3 flex items-center gap-4 rounded-2xl bg-[#fff4cc] px-5 py-5 text-sm leading-relaxed text-[var(--color-text-primary)]"
      >
        <img class="size-12 shrink-0 object-contain" :src="walletIcon" alt="" aria-hidden="true" />
        <p>
          등록된 계좌로 목표 저축과<br />
          저축 기록을 관리할 수 있어요.
        </p>
      </div>

      <button
        class="mt-auto min-h-14 rounded-xl bg-[var(--color-brand-primary)] text-base font-bold text-white active:bg-[var(--color-brand-primary-pressed)]"
        type="button"
        @click="emit('createPlan')"
      >
        맞춤 플랜 생성하기
      </button>
    </div>
  </section>
</template>
