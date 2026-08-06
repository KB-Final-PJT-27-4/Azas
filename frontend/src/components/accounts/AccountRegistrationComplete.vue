<script setup lang="ts">
import registrationCompleteImage from '@/assets/images/accounts/registration-complete.png'
import walletIcon from '@/assets/images/accounts/wallet-icon.png'

const emit = defineEmits<{
  home: []
  createGoal: []
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

      <div class="mt-auto grid grid-cols-2 gap-4">
        <button
          class="min-h-14 rounded-xl border border-[var(--color-border)] bg-white text-base font-bold text-[var(--color-selected-text)] transition-colors hover:bg-[var(--color-surface-muted)] active:bg-[var(--color-unselected-background)]"
          type="button"
          @click="emit('home')"
        >
          홈으로 가기
        </button>
        <button
          class="min-h-14 rounded-xl bg-[var(--color-brand-primary)] text-base font-bold text-white transition-colors hover:bg-[var(--color-brand-primary-pressed)] active:bg-[var(--color-brand-primary-pressed)]"
          type="button"
          @click="emit('createGoal')"
        >
          목표 설정하기
        </button>
      </div>
    </div>
  </section>
</template>
